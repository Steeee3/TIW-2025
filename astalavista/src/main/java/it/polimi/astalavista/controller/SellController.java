package it.polimi.astalavista.controller;

import java.time.LocalDateTime;
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
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

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
public class SellController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ArticleService articleService;

    @Autowired
    private ImageService imageService;

    @Autowired
    private AuctionService auctionService;

    @Autowired
    private OfferService offerService;

    private User user;
    
    @GetMapping("/sell")
    public String uploadUserDetails(Model model, @AuthenticationPrincipal UserDetails userDetails) {
        String username = userDetails.getUsername();
        user = userRepository.findByUsername(username).get();

        uploadUserOpenAuctions(model);
        uploadUserClosedAuctions(model);
        uploadUserUnsoldArticles(model);

        return "sell";
    }

    private void uploadUserOpenAuctions(Model model) {
        List<Auction> auctions = auctionService.getOpenAuctions(user);
        Map<Integer, List<Article>> auctionToArticles = new LinkedHashMap<>();
        Map<Integer, String> previews = new HashMap<>();

        for (Auction auction : auctions) {
            List<Article> articles = articleService.getArticleByAuction(auction);
            auctionToArticles.put(auction.getId(), articles);

            for (Article article : articles) {
                List<Image> imgs = imageService.getImagesForArticle(article);
                if (!imgs.isEmpty()) {
                    previews.put(article.getId(), imgs.get(0).getPath());
                }
            }
        }

        model.addAttribute("openAuctions", auctions);
        model.addAttribute("openAuctionArticlesMap", auctionToArticles);
        model.addAttribute("openAuctionPreviews", previews);
    }

    private void uploadUserClosedAuctions(Model model) {
        List<Auction> auctions = auctionService.getClosedAuctions(user);
        Map<Integer, Float> auctionToLastOffer = new HashMap<>();
        Map<Integer, User> auctionToWinner = new HashMap<>();
        Map<Integer, List<Article>> auctionToArticles = new LinkedHashMap<>();
        Map<Integer, String> previews = new HashMap<>();

        for (Auction auction : auctions) {
            List<Article> articles = articleService.getArticleByAuction(auction);
            auctionToArticles.put(auction.getId(), articles);

            float lastOffer = offerService.getLastOfferByAuction(auction)
                .map(o -> o.getPrice())
                .orElse(0f);
            
            auctionToLastOffer.put(auction.getId(), lastOffer);
        
            User winningUser = offerService.getWinnerByAuction(auction)
                .orElse(null);
            
            auctionToWinner.put(auction.getId(), winningUser);

            for (Article article : articles) {
                List<Image> imgs = imageService.getImagesForArticle(article);

                if (!imgs.isEmpty()) {
                    previews.put(article.getId(), imgs.get(0).getPath());
                }
            }
        }

        model.addAttribute("closedAuctions", auctions);
        model.addAttribute("closedAuctionLastOffers", auctionToLastOffer);
        model.addAttribute("closedAuctionWinners", auctionToWinner);
        model.addAttribute("closedAuctionArticlesMap", auctionToArticles);
        model.addAttribute("closedAuctionPreviews", previews);
    }

    private void uploadUserUnsoldArticles(Model model) {
        List<Article> unsoldArticles = articleService.getUnsoldArticlesByUser(user);
        unsoldArticles = unsoldArticles.stream()
            .filter(a -> a.getAuction().isEmpty() || a.getAuction().get().isClosed())
            .toList();

        Map<Integer, String> previews = new HashMap<>();
        for (Article article : unsoldArticles) {
            List<Image> imgs = imageService.getImagesForArticle(article);

            if (!imgs.isEmpty()) {
                previews.put(article.getId(), imgs.get(0).getPath());
            }
        }

        model.addAttribute("articles", unsoldArticles);
        model.addAttribute("previews", previews);
    }
    
    @PostMapping("/addArticle")
    public String addArticle(
        @RequestParam String name,
        @RequestParam("images") MultipartFile[] images,
        @RequestParam String description,
        @RequestParam float price,
        @AuthenticationPrincipal UserDetails userDetails
    ) {
        articleService.addArticle(user, name, description, price, images);

        return "redirect:/sell";
    }

    @PostMapping("/newAuction")
    public String addAuction(
        @RequestParam("selectedArticles") List<Integer> selectedIds,
        @RequestParam LocalDateTime endDate,
        @RequestParam int bidStep
    ) {
        auctionService.addAuction(user, selectedIds, endDate, bidStep);

        return "redirect:/sell"; 
    } 
}
