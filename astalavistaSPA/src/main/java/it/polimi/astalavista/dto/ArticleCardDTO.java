package it.polimi.astalavista.dto;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ArticleCardDTO(
    int id,
    String name,
    String description,
    float price,
    String previewPath,
    List<String> images
) {}
