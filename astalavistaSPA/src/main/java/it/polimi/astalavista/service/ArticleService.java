package it.polimi.astalavista.service;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import it.polimi.astalavista.model.Article;
import it.polimi.astalavista.model.Auction;
import it.polimi.astalavista.model.User;
import it.polimi.astalavista.repository.ArticleRepository;

@Service
public class ArticleService {

    @Autowired
    private ArticleRepository articleRepository;

    @Autowired
    private ImageService imageService;

    public List<Article> getAllArticlesByUser(User user) {
        return articleRepository.findByUser(user);
    }

    public List<Article> getUnsoldArticlesByUser(User user) {
        return articleRepository.findByUserAndIsSold(user, "N");
    }

    public List<Article> getArticleByAuction(Auction auction) {
        return articleRepository.findByAuction(Optional.of(auction));
    }
    
    public void addArticle(User user, String name, String description, float price, MultipartFile[] images) {
        Article article = new Article();

        article.setUser(user);
        article.setName(name);
        article.setDescription(description);
        article.setPrice(price);

        articleRepository.save(article);

        imageService.addImages(images, article);
    }
}
