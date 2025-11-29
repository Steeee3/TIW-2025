package it.polimi.astalavista.service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import it.polimi.astalavista.model.Article;
import it.polimi.astalavista.model.Image;
import it.polimi.astalavista.repository.ImageRepository;

@Service
public class ImageService {

    private static final String DEFAULT_UPLOAD_DIRECTORY = "uploads/";

    @Autowired
    private ImageRepository imageRepository;

    public List<Image> getImagesForArticle(Article article) {
        return imageRepository.findByArticleOrderByPriorityAsc(article);
    }
    
    public void addImages(MultipartFile[] images, Article article) {
        int priority = images.length - 1;
        for (MultipartFile fileImage : images) {
            Image image = new Image();
            
            image.setPriority(priority);
            image.setArticle(article);

            String path = saveImageInDefaultFolder(fileImage, priority--, article);
            image.setPath(path);

            imageRepository.save(image);
        }
    }

    private String saveImageInDefaultFolder(MultipartFile image, int priority, Article article) {
        String imageName = generateUniqueImageName(image.getOriginalFilename(), priority, article);

        try {
            Path directoryPath = Paths.get(DEFAULT_UPLOAD_DIRECTORY);
            Path destinationPath = directoryPath.resolve(imageName);

            if (!Files.exists(directoryPath)) {
                Files.createDirectories(directoryPath);
            }

            Files.copy(image.getInputStream(), destinationPath, StandardCopyOption.REPLACE_EXISTING);

            return DEFAULT_UPLOAD_DIRECTORY + imageName;
        } catch (IOException e) {
            throw new RuntimeException("Errore durante il salvataggio dell'immagine", e);
        }
    }

    private String generateUniqueImageName(String imageName, int priority, Article article) {
        String extension = getImageExtension(imageName);
        String uuid = UUID.randomUUID().toString().replaceAll("-", "").substring(0, 8);
        String fileName = sanitizeFileName(article.getName());

        return fileName + "-" + article.getId() + "-img-" + priority + "-" + uuid + extension;
    }

    private String sanitizeFileName(String originalName) {
        return originalName.replaceAll("[\\\\/:*?\"<>|]", "_");
    }

    private String getImageExtension(String imageName) {
        String extension = "";

        int dotIndex = imageName.lastIndexOf('.');
        extension = imageName.substring(dotIndex);

        return extension;
    }
}
