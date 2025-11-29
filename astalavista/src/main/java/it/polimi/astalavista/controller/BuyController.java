package it.polimi.astalavista.controller;

import java.security.Principal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import it.polimi.astalavista.model.Article;
import it.polimi.astalavista.model.Auction;
import it.polimi.astalavista.model.Image;
import it.polimi.astalavista.service.ArticleService;
import it.polimi.astalavista.service.AuctionService;
import it.polimi.astalavista.service.ImageService;


@Controller
public class BuyController {

    @Autowired
    private AuctionService auctionService;

    @Autowired
    private ArticleService articleService;

    @Autowired
    private ImageService imageService;

    @GetMapping("/buy")
    public String showSearchResults(@RequestParam(required=false) String keyword, Model model, Principal principal) {
        List<Auction> auctions;
        Map<Integer, List<Article>> articleMap = new HashMap<>();
        Map<Integer, String> previews = new HashMap<>();

        if (keyword != null && !keyword.isBlank()) {
            auctions = auctionService.getAllAuctionsByKeyword(keyword);
        } else {
            auctions = auctionService.getAllOpenAuctions();
        }

        for (Auction auction : auctions) {
            articleMap.put(auction.getId(), articleService.getArticleByAuction(auction));
        }
        for (List<Article> articleList : articleMap.values()) {
            for (Article article : articleList) {
                Image preview = imageService.getImagesForArticle(article).get(0);
                previews.put(article.getId(), preview.getPath());
            }
        }
        
        model.addAttribute("auctions", auctions);
        model.addAttribute("openAuctionArticlesMap", articleMap);
        model.addAttribute("openAuctionPreviews", previews);
        model.addAttribute("keyword", keyword);
        return "buy";
    }
    
}
