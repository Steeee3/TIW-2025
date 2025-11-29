package it.polimi.astalavista.api;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import it.polimi.astalavista.dto.ArticleCardDTO;
import it.polimi.astalavista.model.Image;
import it.polimi.astalavista.model.User;
import it.polimi.astalavista.repository.UserRepository;
import it.polimi.astalavista.service.ArticleService;
import it.polimi.astalavista.service.ImageService;

@RestController
@RequestMapping("/api/articles")
public class ArticlesApi {

    private final UserRepository userRepository;
    private final ArticleService articleService;
    private final ImageService imageService;

    public ArticlesApi(
        UserRepository userRepository,
        ArticleService articleService,
        ImageService imageService
    ) {
        this.userRepository = userRepository;
        this.articleService = articleService;
        this.imageService = imageService;
    }

    private User me(@AuthenticationPrincipal UserDetails ud) {
        return userRepository.findByUsername(ud.getUsername()).orElseThrow();
    }
    
    @GetMapping("/unsold")
    public List<ArticleCardDTO> getUnsoldArticles(@AuthenticationPrincipal UserDetails ud) {
        User user = me(ud);

        return articleService.getUnsoldArticlesByUser(user).stream()
        .filter(a -> a.getAuction().isEmpty() || a.getAuction().get().isClosed())
        .map(ar -> {
            List<String> images = imageService.getImagesForArticle(ar).stream()
                .sorted(Comparator.comparingInt(img -> Optional.ofNullable(img.getPriority()).orElse(0)))
                .map(Image::getPath)
                .map(p -> p.startsWith("/") ? p : "/" + p)
                .toList();

            String preview = images.isEmpty() ? "/images/placeholder.png" : images.get(0);
            List<String> imagesOrPlaceholder = images.isEmpty()
                    ? java.util.Collections.singletonList(preview)
                    : images;

            return new ArticleCardDTO(
                ar.getId(),
                ar.getName(),
                ar.getDescription(),
                (float) ar.getPrice(),
                preview,
                imagesOrPlaceholder
            );
        })
        .toList();
    }
}
