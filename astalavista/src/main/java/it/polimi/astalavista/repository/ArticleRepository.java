package it.polimi.astalavista.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import it.polimi.astalavista.model.Article;
import it.polimi.astalavista.model.User;
import it.polimi.astalavista.model.Auction;
import java.util.Optional;


public interface ArticleRepository extends JpaRepository<Article, Integer> {

    List<Article> findByUser(User user);
    List<Article> findByUserAndIsSold(User user, String isSold);
    List<Article> findByAuction(Optional<Auction> auction);
}
