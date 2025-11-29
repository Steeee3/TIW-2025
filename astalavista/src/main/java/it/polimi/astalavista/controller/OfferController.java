package it.polimi.astalavista.controller;

import java.security.Principal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

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
import it.polimi.astalavista.service.UserService;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;


@Controller
public class OfferController {

    @Autowired
    private AuctionService auctionService;
    
    @Autowired
    private OfferService offerService;

    @Autowired
    private ArticleService articleService;

    @Autowired
    private ImageService imageService;

    @Autowired
    private UserService userService;

    private int id;
    
    @GetMapping("/offer/{id}")
    public String showAuctionOffers(@PathVariable int id, Principal principal, Model model) {
        Auction auction = auctionService.getAuctionById(id).get();
        String seller = userService.getUserById(auction.getUser().getId())
            .map(u -> u.getUsername())
            .orElse("user");

        if (seller.equals(principal.getName())) {
            return "redirect:/details/" + id;
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

        this.id = id;

        model.addAttribute("auction", auction);
        model.addAttribute("seller", seller);
        model.addAttribute("offers", offers);
        model.addAttribute("images", images);
        model.addAttribute("articles", articles);
        model.addAttribute("winner", winner);

        return "offer";
    }

    @PostMapping("/placeOffer")
    public String placeOffer(
        @RequestParam int auctionId,
        @RequestParam float offer,
        Principal principal,
        RedirectAttributes redirectAttributes
        ) {

        Auction auction = auctionService.getAuctionById(auctionId).get();
        Optional<Offer> higestOffer = offerService.getLastOfferByAuction(auction);
        double bidStep = auction.getBidStep();

        if (offer < auction.getStartPrice()) {
            redirectAttributes.addFlashAttribute("error", "L'offerta deve essere almeno pari al prezzo iniziale.");
            return "redirect:/offer/" + id;
        }

        if (higestOffer.isPresent() && offer < higestOffer.get().getPrice() + bidStep) {
            redirectAttributes.addFlashAttribute("error", "L'offerta deve superare l'attuale offerta massima di almeno €" + bidStep);
            return "redirect:/offer/" + id;
        }

        User user = userService.getUserByUsername(principal.getName()).get();
        offerService.placeOffer(user, auction, offer);

        redirectAttributes.addFlashAttribute("success", "Offerta piazzata con successo!");
        return "redirect:/offer/" + id;
    }
    
}
