package it.polimi.astalavista.api;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import it.polimi.astalavista.model.Article;
import it.polimi.astalavista.model.Auction;
import it.polimi.astalavista.model.Image;
import it.polimi.astalavista.service.ArticleService;
import it.polimi.astalavista.service.AuctionService;
import it.polimi.astalavista.service.ImageService;

@RestController
@RequestMapping("/api/buy")
public class BuyApi {
  private final AuctionService auctionService;
  private final ArticleService articleService;
  private final ImageService imageService;

  public BuyApi(AuctionService auctionService, ArticleService articleService, ImageService imageService) {
    this.auctionService = auctionService;
    this.articleService = articleService;
    this.imageService = imageService;
  }

  @GetMapping
  public List<Map<String, Object>> list(@RequestParam(required = false) String keyword) {
    // 1) prendi le aste (entity)
    List<Auction> auctions = (keyword != null && !keyword.isBlank())
        ? auctionService.getAllAuctionsByKeyword(keyword)
        : auctionService.getAllOpenAuctions();

    // 2) per ogni asta, prendi articoli + preview e costruisci una Map (niente DTO classi)
    return auctions.stream().map(a -> {
      List<Article> articles = articleService.getArticleByAuction(a);

      List<Map<String, Object>> articleCards = articles.stream().map(ar -> {
        List<Image> imgs = imageService.getImagesForArticle(ar);
        String preview = (imgs != null && !imgs.isEmpty()) ? imgs.get(0).getPath() : "/images/placeholder.png";
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", ar.getId());
        m.put("name", ar.getName());
        m.put("price", ar.getPrice());
        m.put("previewPath", preview);
        return m;
      }).collect(Collectors.toList());

      Map<String, Object> m = new LinkedHashMap<>();
      m.put("id", a.getId());
      m.put("endDate", a.getEndDate());
      m.put("articles", articleCards);
      return m;
    }).toList();
  }
}