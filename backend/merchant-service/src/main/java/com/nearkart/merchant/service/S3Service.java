package com.nearkart.merchant.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;

import java.io.IOException;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class S3Service {

    private final S3Client s3Client;

    @Value("${aws.s3.bucket-name}")
    private String bucketName;

    public String uploadKycDocument(MultipartFile file, UUID merchantId, String documentType) {
        String key = String.format("kyc/%s/%s/%s-%s",
                merchantId, documentType, UUID.randomUUID(),
                file.getOriginalFilename());
        try {
            PutObjectRequest request = PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(key)
                    .contentType(file.getContentType())
                    .build();
            s3Client.putObject(request, RequestBody.fromInputStream(file.getInputStream(), file.getSize()));
            log.info("Uploaded KYC document: {}", key);
            return key;
        } catch (IOException e) {
            throw new RuntimeException("Failed to upload document to S3", e);
        }
    }

    public String uploadShopImage(MultipartFile file, UUID shopId, String imageType) {
        String key = String.format("shops/%s/%s/%s-%s",
                shopId, imageType, UUID.randomUUID(),
                file.getOriginalFilename());
        try {
            PutObjectRequest request = PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(key)
                    .contentType(file.getContentType())
                    .build();
            s3Client.putObject(request, RequestBody.fromInputStream(file.getInputStream(), file.getSize()));
            return key;
        } catch (IOException e) {
            throw new RuntimeException("Failed to upload shop image to S3", e);
        }
    }

    public String getPresignedUrl(String s3Key) {
        return String.format("https://%s.s3.amazonaws.com/%s", bucketName, s3Key);
    }

    public void deleteObject(String s3Key) {
        DeleteObjectRequest request = DeleteObjectRequest.builder()
                .bucket(bucketName)
                .key(s3Key)
                .build();
        s3Client.deleteObject(request);
        log.info("Deleted S3 object: {}", s3Key);
    }
}
