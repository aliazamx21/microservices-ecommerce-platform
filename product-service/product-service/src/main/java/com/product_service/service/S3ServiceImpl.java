package com.product_service.service;

import com.product_service.entity.Brand;
import com.product_service.entity.Image;
import com.product_service.repository.BrandRepository;
import com.product_service.repository.ImageRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.IOException;
import java.util.UUID;

@Service
public class S3ServiceImpl implements S3Service {
    private final S3Client s3Client;
    private ImageRepository imageRepository;
    private BrandRepository brandRepository;

    @Value("${aws.s3.bucket.name}")
    private String bucketName;

    @Value("{aws.s3.region}")
    private String region;

    public S3ServiceImpl(S3Client s3Client, ImageRepository imageRepository, BrandRepository brandRepository) {
        this.s3Client = s3Client;
        this.imageRepository = imageRepository;
        this.brandRepository = brandRepository;
    }

    public String uploadImage(MultipartFile file, int brandId) throws IOException {
        // 1. Generate a unique file name to prevent overwriting
        String originalFilename = file.getOriginalFilename();
        String uniqueFileName = UUID.randomUUID() + "_" + originalFilename;

        // 2. Prepare the S3 request
        PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                .bucket(bucketName)
                .key(uniqueFileName)
                .contentType(file.getContentType())
                .build();

        // 3. Upload the file
        s3Client.putObject(putObjectRequest,
                RequestBody.fromInputStream(file.getInputStream(), file.getSize()));

        String url ="https://" + bucketName + ".s3." + region + ".amazonaws.com/" + uniqueFileName;
        // Save data inside Image
       Brand brand = brandRepository.findById(brandId).get();
        Image image = new Image();
        image.setBrand(brand);
        image.setUrl(url);

        imageRepository.save(image);

        // 4. Return the public URL of the uploaded image
        // (Assuming your S3 bucket is public-read or you are using CloudFront)
        return url;
    }
}