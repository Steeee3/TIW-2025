package it.polimi.astalavista.dto;

import java.time.LocalDateTime;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record AuctionDTO(

    int id,

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    LocalDateTime startDate,

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    LocalDateTime endDate,

    float startPrice,
    int bidStep,

    boolean isClosed,

    Long remainingSeconds,

    UserDTO seller,
    UserDTO winner,

    List<ArticleCardDTO> articles,

    Float currentPrice,

    List<OfferDTO> offers,

    String detailUrl
) {}
