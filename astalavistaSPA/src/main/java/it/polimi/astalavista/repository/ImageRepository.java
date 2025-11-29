package it.polimi.astalavista.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import it.polimi.astalavista.model.Article;
import it.polimi.astalavista.model.Image;

public interface ImageRepository extends JpaRepository<Image, Integer> {
    
    List<Image> findByArticleOrderByPriorityAsc(Article article);
}
