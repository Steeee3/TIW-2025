package it.polimi.astalavista.controller;

import java.security.Principal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import it.polimi.astalavista.model.Article;
import it.polimi.astalavista.model.Auction;
import it.polimi.astalavista.model.Image;
import it.polimi.astalavista.model.Offer;
import it.polimi.astalavista.model.User;
import it.polimi.astalavista.service.ArticleService;
import it.polimi.astalavista.service.AuctionService;
import it.polimi.astalavista.service.ImageService;
import it.polimi.astalavista.service.OfferService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;


@Controller
public class DetailsController {

    @Autowired
    private AuctionService auctionService;
    
    @Autowired
    private OfferService offerService;

    @Autowired
    private ArticleService articleService;

    @Autowired
    private ImageService imageService;
    
    @GetMapping("/details/{id}")
    public String showAuctionDetails(@PathVariable int id, Principal principal, Model model) {
        Auction auction = auctionService.getAuctionById(id).get();

        if (!auction.getUser().getUsername().equals(principal.getName())) {
            return "redirect:/offer/" + id;
        }

        List<Offer> offers = offerService.getOffers(auction);
        List<Article> articles = articleService.getArticleByAuction(auction);
        User winner = auction.isClosed() ? offerService.getWinnerByAuction(auction).orElse(null) : null;
        Map<Integer, List<String>> images = new HashMap<>();

        for (Article article : articles) {
            List<Image> imgs = imageService.getImagesForArticle(article);
            
            images.put(article.getId(), imgs.stream()
                .map(i -> i.getPath())
                .toList()
            );
        }

        model.addAttribute("auction", auction);
        model.addAttribute("offers", offers);
        model.addAttribute("images", images);
        model.addAttribute("articles", articles);
        model.addAttribute("winner", winner);

        return "details";
    }

    @PostMapping("/closeAuction")
    public String closeAuction(@RequestParam int auctionId) {
        Auction auction = auctionService.getAuctionById(auctionId).get();

        auctionService.closeAuction(auction);

        return "redirect:/details/" + auctionId;
    }
}
