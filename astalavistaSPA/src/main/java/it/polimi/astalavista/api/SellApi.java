package it.polimi.astalavista.api;

import java.time.LocalDateTime;
import java.util.*;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import it.polimi.astalavista.model.User;
import it.polimi.astalavista.repository.UserRepository;
import it.polimi.astalavista.service.ArticleService;
import it.polimi.astalavista.service.AuctionService;

@RestController
@RequestMapping("/api/sell")
public class SellApi {

  private final UserRepository userRepository;
  private final AuctionService auctionService;
  private final ArticleService articleService;

  public SellApi(UserRepository userRepository,
                 AuctionService auctionService,
                 ArticleService articleService) {
    this.userRepository = userRepository;
    this.auctionService = auctionService;
    this.articleService = articleService;
  }

  private User me(@AuthenticationPrincipal UserDetails ud) {
    return userRepository.findByUsername(ud.getUsername()).orElseThrow();
  }

  @PostMapping(path = "/articles", consumes = { "multipart/form-data" })
  public ResponseEntity<?> addArticle(@AuthenticationPrincipal UserDetails ud,
                                      @RequestParam String name,
                                      @RequestParam("images") org.springframework.web.multipart.MultipartFile[] images,
                                      @RequestParam String description,
                                      @RequestParam float price) {
    User user = me(ud);
    articleService.addArticle(user, name, description, price, images);
    return ResponseEntity.ok().build();
  }

  @PostMapping("/auctions")
  public ResponseEntity<?> addAuction(@AuthenticationPrincipal UserDetails ud,
                                      @RequestBody Map<String, Object> body) {
    User user = me(ud);

    @SuppressWarnings("unchecked")
    List<Integer> selected = (List<Integer>) body.getOrDefault("selectedArticles", List.of());
    String endDateStr = String.valueOf(body.get("endDate")); 
    Integer bidStep = Integer.valueOf(String.valueOf(body.get("bidStep")));

    LocalDateTime endDate = LocalDateTime.parse(endDateStr);
    auctionService.addAuction(user, selected, endDate, bidStep);
    return ResponseEntity.ok().build();
  }
}