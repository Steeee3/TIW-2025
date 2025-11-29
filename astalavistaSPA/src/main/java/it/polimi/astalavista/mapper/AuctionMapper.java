package it.polimi.astalavista.mapper;

import it.polimi.astalavista.dto.*;
import it.polimi.astalavista.model.*;
import it.polimi.astalavista.service.ArticleService;
import it.polimi.astalavista.service.ImageService;
import it.polimi.astalavista.service.OfferService;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@Component
public class AuctionMapper {

    private final ArticleService articleService;
    private final ImageService imageService;
    private final OfferService offerService;
    private final UserMapper userMapper;
    private final OfferMapper offerMapper;

    public AuctionMapper(ArticleService articleService,
                         ImageService imageService,
                         OfferService offerService,
                         UserMapper userMapper,
                         OfferMapper offerMapper
                         ) {
        this.articleService = articleService;
        this.imageService = imageService;
        this.offerService = offerService;
        this.userMapper = userMapper;
        this.offerMapper = offerMapper;
    }

    public AuctionDTO toDto(Auction a) {
        List<ArticleCardDTO> articleCards = articleService.getArticleByAuction(a).stream()
            .map(ar -> {
                List<String> images = imageService.getImagesForArticle(ar).stream()
                    .sorted(Comparator.comparingInt(img -> Optional.ofNullable(img.getPriority()).orElse(0)))
                    .map(Image::getPath)
                    .map(p -> p.startsWith("/") ? p : "/" + p)
                    .toList();

                String preview = images.isEmpty() ? "/images/placeholder.png" : images.get(0);
                List<String> imagesOrPlaceholder = images.isEmpty() ? java.util.List.of(preview) : images;

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
        
        User seller = a.getUser();
        User winner = offerService.getWinnerByAuction(a).orElse(null);

        Float currentPrice = offerService.getLastOfferByAuction(a)
            .map(o -> (float) o.getPrice())
            .orElse(null);

        boolean closed = a.isClosed();
        Long remainingSeconds = null;
        if (!closed && a.getEndDate() != null) {
            long sec = Duration.between(LocalDateTime.now(), a.getEndDate()).getSeconds();
            remainingSeconds = Math.max(0, sec);
        }

        return new AuctionDTO(
            a.getId(),
            a.getStartDate(),
            a.getEndDate(),
            (float) a.getStartPrice(),
            a.getBidStep(),
            closed,
            remainingSeconds,
            userMapper.toDto(seller),
            winner != null ? userMapper.toDto(winner) : null,
            articleCards,
            currentPrice,
            offerMapper.toDto(offerService.getOffers(a)),
            "/details/" + a.getId()
        );
    }
}