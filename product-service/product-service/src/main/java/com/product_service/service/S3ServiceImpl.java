package com.product_service.service;

import com.product_service.entity.Brand;
import com.product_service.entity.Image;
import com.product_service.repository.BrandRepository;
import com.product_service.repository.ImageRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

    private static final Logger logger = LoggerFactory.getLogger(S3ServiceImpl.class);

    private final S3Client s3Client;
    private final ImageRepository imageRepository;
    private final BrandRepository brandRepository;

    @Value("${aws.s3.bucket.name}")
    private String bucketName;

    // BUG FIX: The original code was missing the '$' sign here!
    @Value("${aws.s3.region}")
    private String region;

    public S3ServiceImpl(S3Client s3Client, ImageRepository imageRepository, BrandRepository brandRepository) {
        this.s3Client = s3Client;
        this.imageRepository = imageRepository;
        this.brandRepository = brandRepository;
    }

    @Override
    public String uploadImage(MultipartFile file, int brandId) throws IOException {

        // BUG FIX: Prevent NoSuchElementException if the brand doesn't exist
        Brand brand = brandRepository.findById(brandId)
                .orElseThrow(() -> new RuntimeException("Brand not found with ID: " + brandId));

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
        logger.info("Uploading image {} to S3 bucket {}", uniqueFileName, bucketName);
        s3Client.putObject(putObjectRequest,
                RequestBody.fromInputStream(file.getInputStream(), file.getSize()));

        String url ="https://" + bucketName + ".s3." + region + ".amazonaws.com/" + uniqueFileName;

        // Save data inside Image
        Image image = new Image();
        image.setBrand(brand);
        image.setUrl(url);
        imageRepository.save(image);

        logger.info("Successfully uploaded image and saved to database. URL: {}", url);
        return url;
    }
}