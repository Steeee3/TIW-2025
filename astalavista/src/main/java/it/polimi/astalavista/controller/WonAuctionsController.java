package it.polimi.astalavista.controller;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import it.polimi.astalavista.model.Article;
import it.polimi.astalavista.model.Auction;
import it.polimi.astalavista.model.Image;
import it.polimi.astalavista.model.User;
import it.polimi.astalavista.repository.UserRepository;
import it.polimi.astalavista.service.ArticleService;
import it.polimi.astalavista.service.AuctionService;
import it.polimi.astalavista.service.ImageService;
import it.polimi.astalavista.service.OfferService;

@Controller
public class WonAuctionsController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AuctionService auctionService;

    @Autowired
    private ArticleService articleService;

    @Autowired
    private OfferService offerService;

    @Autowired
    private ImageService imageService;

    private User user;
    
    @GetMapping("/won")
    public String showWonAuctions(Model model, @AuthenticationPrincipal UserDetails userDetails) {
        String username = userDetails.getUsername();
        user = userRepository.findByUsername(username).get();

        uploadWonAuctions(model);

        return "wonAuctions";
    }

    private void uploadWonAuctions(Model model) {
        List<Auction> auctions = auctionService.getAllAuctionsWonByUser(user);
        Map<Integer, Float> auctionToLastOffer = new HashMap<>();
        Map<Integer, List<Article>> auctionToArticles = new LinkedHashMap<>();
        Map<Integer, String> previews = new HashMap<>();

        for (Auction auction : auctions) {
            List<Article> articles = articleService.getArticleByAuction(auction);
            auctionToArticles.put(auction.getId(), articles);

            float lastOffer = offerService.getLastOfferByAuction(auction)
                .map(o -> o.getPrice())
                .orElse(0f);
            
            auctionToLastOffer.put(auction.getId(), lastOffer);

            for (Article article : articles) {
                List<Image> imgs = imageService.getImagesForArticle(article);

                if (!imgs.isEmpty()) {
                    previews.put(article.getId(), imgs.get(0).getPath());
                }
            }
        }

        model.addAttribute("closedAuctions", auctions);
        model.addAttribute("closedAuctionLastOffers", auctionToLastOffer);
        model.addAttribute("closedAuctionArticlesMap", auctionToArticles);
        model.addAttribute("closedAuctionPreviews", previews);
    }
    
}
