package it.polimi.astalavista.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import it.polimi.astalavista.model.Article;
import it.polimi.astalavista.model.Auction;
import it.polimi.astalavista.model.User;
import it.polimi.astalavista.repository.ArticleRepository;
import it.polimi.astalavista.repository.AuctionRepository;

@Service
public class AuctionService {
    
    @Autowired
    private AuctionRepository auctionRepository;

    @Autowired
    private ArticleRepository articleRepository;

    @Autowired
    private OfferService offerService;

    public void addAuction(User user, List<Integer> articleIds, LocalDateTime endDate, int bidStep) {
        Auction auction = new Auction();
        
        List<Article> articles = getAllArticlesByIds(articleIds);
        setStartPrice(auction, articles);

        auction.setEndDate(endDate);
        auction.setBidStep(bidStep);
        auction.setUser(user);
        auction.start();

        auctionRepository.save(auction);
    }

    private List<Article> getAllArticlesByIds(List<Integer> articleIds) {
        List<Article> articles = new ArrayList<>();

        for (Integer id : articleIds) {
            Article article = articleRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Articolo non trovato"));
            
            articles.add(article);
        }

        return articles;
    }

    private void setStartPrice(Auction auction, List<Article> articles) {
        for (Article article : articles) {
            article.setAuction(auction);

            float price = auction.getStartPrice() + article.getPrice();
            auction.setStartPrice(price);
        }
    }

    public List<Auction> getOpenAuctions(User user) {
        List<Auction> auctions = auctionRepository.findOpenAuctionsByUser(user);

        List<Auction> closedAuctions = auctions.stream()
            .filter(a -> a.isClosed())
            .toList();
        
        for (Auction closed : closedAuctions) {
            closeAuction(closed);
        }
        auctions.removeAll(closedAuctions);

        return auctions;
    }

    public List<Auction> getClosedAuctions(User user) {
        return auctionRepository.findClosedAuctionsByUser(user);
    }

    public Optional<Auction> getAuctionById(int id) {
        Optional<Auction> auction = auctionRepository.findById(id);

        auction.ifPresent(a -> {
            if (a.isClosed()) {
                closeAuction(a);
            }
        });

        return auction;
    }

    public List<Auction> getAllOpenAuctions() {
        List<Auction> auctions = auctionRepository.findByIsClosedOrderByEndDateDesc("N");

        List<Auction> closedAuctions = auctions.stream()
            .filter(a -> a.isClosed())
            .toList();
        
        for (Auction closed : closedAuctions) {
            closeAuction(closed);
        }
        auctions.removeAll(closedAuctions);

        return auctions;
    }

    public List<Auction> getAllAuctionsByKeyword(String keyword) {
        List<Auction> auctions = auctionRepository.findOpenAuctionsByKeyword(keyword);

        List<Auction> closedAuctions = auctions.stream()
            .filter(a -> a.isClosed())
            .toList();
        
        for (Auction closed : closedAuctions) {
            closeAuction(closed);
        }
        auctions.removeAll(closedAuctions);

        return auctions;
    }

    public List<Auction> getAllAuctionsWonByUser(User user) {
        return auctionRepository.findClosedAuctionsWonByUser(user);
    }

    @Transactional
    public void closeAuction(Auction auction) {
        auction.close();

        List<Article> articles = articleRepository.findByAuction(Optional.of(auction));
        boolean hasWinner = offerService.getLastOfferByAuction(auction).isPresent();

        if (hasWinner) {
            for (Article article : articles) {
                article.sell();
            }
        }
    }
}
