package com.dailycodework.buynowdotcom.service.image;

import com.dailycodework.buynowdotcom.dtos.ImageDto;
import com.dailycodework.buynowdotcom.model.Image;
import com.dailycodework.buynowdotcom.model.Product;
import com.dailycodework.buynowdotcom.repository.ImageRepository;
import com.dailycodework.buynowdotcom.request.EmbeddingsDeleteRequest;
import com.dailycodework.buynowdotcom.service.chroma.ChromaService;
import com.dailycodework.buynowdotcom.service.embeddings.ImageSearchService;
import com.dailycodework.buynowdotcom.service.product.IProductService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.sql.rowset.serial.SerialBlob;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ImageService implements IImageService {
    private final ImageRepository imageRepository;
    private final IProductService productService;
    private final ImageSearchService imageSearchService;
    private final ChromaService chromaService;

    @Value("${spring.ai.vectorstore.chroma.collection-name}")
    private String collectionName;


    @Override
    public Image getImageById(Long imageId) {
        return imageRepository.findById(imageId)
                .orElseThrow(() -> new EntityNotFoundException("Image not found!"));
    }

    @Override
    public void deleteImageById(Long imageId) {
        imageRepository.findById(imageId).ifPresentOrElse(image -> {
            // 1. Delete image from DB
            imageRepository.delete(image);
            // 2. Delete embeddings via chromaService
            EmbeddingsDeleteRequest request = EmbeddingsDeleteRequest.builder()
                    .collectionName(collectionName)
                    .imageId(imageId.toString())
                    .build();

            chromaService.deleteEmbeddingsByCollectionId(request);
            log.info("Deleted image {} and its embeddings", imageId);
        }, () -> {
            throw new EntityNotFoundException("Image not found!");
        });
    }

    @Transactional
    @Override
    public void updateImage(MultipartFile file, Long imageId, Long productId) throws IOException {
        Image image = getImageById(imageId);
        try {
            image.setFileName(file.getOriginalFilename());
            image.setFileType(file.getContentType());
            image.setImage(new SerialBlob(file.getBytes()));
            var savedImage = imageRepository.save(image);

            // Updating the existing image embeddings
            EmbeddingsDeleteRequest deleteEmbeddings = EmbeddingsDeleteRequest.builder()
                    .collectionName(collectionName)
                    .imageId(imageId.toString())
                    .build();
            chromaService.deleteEmbeddingsByCollectionId(deleteEmbeddings);

            String imageSummary = getImageSummary(productId, file, savedImage);
            log.info("Updated image summary embedded ids : {} ", imageSummary);

        } catch (IOException | SQLException e) {
            throw new RuntimeException(e.getMessage());
        }

    }

    @Override
    public List<ImageDto> saveImages(Long productId, List<MultipartFile> files) {
        Product product = productService.getProductById(productId);

        List<ImageDto> savedImages = new ArrayList<>();

        for (MultipartFile file : files) {
            try {
                Image image = new Image();
                image.setFileName(file.getOriginalFilename());
                image.setFileType(file.getContentType());
                image.setImage(new SerialBlob(file.getBytes()));
                image.setProduct(product);

                // Code has been restructured to remove unnecessary repetitions.
                Image savedImage = imageRepository.save(image);
                String buildDownloadUrl = "/api/v1/images/image/download/";
                savedImage.setDownloadUrl(buildDownloadUrl + savedImage.getId());
                savedImage = imageRepository.save(savedImage);

                // The image ID has been added to chroma DB embeddings
                String imageSummary = getImageSummary(productId, file, savedImage);
                log.info("Stored image summary embedded ids : {} ", imageSummary);

                ImageDto imageDto = new ImageDto();
                imageDto.setId(savedImage.getId());
                imageDto.setFileName(savedImage.getFileName());
                imageDto.setDownloadUrl(savedImage.getDownloadUrl());
                savedImages.add(imageDto);
            } catch (IOException | SQLException e) {
                throw new RuntimeException(e.getMessage());
            }
        }
        return savedImages;
    }

    private String getImageSummary(Long productId, MultipartFile file, Image savedImage) throws IOException {
        return String.valueOf(imageSearchService.saveEmbeddings(file, productId, savedImage.getId()));
    }
}
