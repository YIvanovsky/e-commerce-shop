package com.dailycodework.buynowdotcom.service.image;
import com.dailycodework.buynowdotcom.dtos.ImageDto;
import com.dailycodework.buynowdotcom.model.Image;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

public interface IImageService {
    Image getImageById(Long imageId);
    void deleteImageById(Long imageId);
    void updateImage(MultipartFile file, Long imageId, Long productId) throws IOException;
    List<ImageDto> saveImages(Long productId, List<MultipartFile> files);

}
