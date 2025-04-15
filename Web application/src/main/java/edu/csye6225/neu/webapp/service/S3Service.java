package edu.csye6225.neu.webapp.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Exception;

import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

@Service
public class S3Service {
    private static final Logger logger = LoggerFactory.getLogger(S3Service.class);

    private final S3Client s3Client;
    private final String bucketName;
    private final MetricsService metricsService;

    @Autowired
    public S3Service(S3Client s3Client, 
                    @Value("${cloud.aws.s3.bucket}") String bucketName,
                    MetricsService metricsService) {
        this.s3Client = s3Client;
        this.bucketName = bucketName;
        this.metricsService = metricsService;
        logger.info("S3Service initialized with bucket: {}", bucketName);
    }

    /**
     * Upload a file to S3 bucket with metrics tracking
     * 
     * @param file The file to upload
     * @return The S3 URI of the uploaded file
     * @throws IOException If the file cannot be read
     */
    public String uploadFile(MultipartFile file) throws IOException {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("File is empty or null");
        }

        Instant startTime = Instant.now();
        try {
            String fileName = generateUniqueFileName(file.getOriginalFilename());
            String contentType = file.getContentType();

            logger.debug("Preparing to upload file to S3: {}", fileName);
            
            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(fileName)
                    .contentType(contentType)
                    .build();

            s3Client.putObject(putObjectRequest, RequestBody.fromBytes(file.getBytes()));
            
            // Record metrics for S3 upload time
            Duration duration = Duration.between(startTime, Instant.now());
            metricsService.timeS3Operation("upload", duration.toMillis());
            
            logger.info("Successfully uploaded file to S3: {}, took: {}ms", 
                    fileName, duration.toMillis());
            
            // Return the S3 URI
            return String.format("https://%s.s3.amazonaws.com/%s", bucketName, fileName);
            
        } catch (S3Exception e) {
            // Record failure metrics
            Duration duration = Duration.between(startTime, Instant.now());
            metricsService.incrementApiCounter("s3.upload.error");
            
            logger.error("Error uploading file to S3, took: {}ms", duration.toMillis(), e);
            throw new RuntimeException("Failed to upload file to S3: " + e.getMessage(), e);
        }
    }
    
    /**
     * Delete a file from S3 bucket with metrics tracking
     * 
     * @param fileUrl The URL of the file to delete
     */
    public void deleteFile(String fileUrl) {
        if (fileUrl == null || fileUrl.isEmpty()) {
            logger.warn("Attempted to delete a null or empty file URL");
            return;
        }

        Instant startTime = Instant.now();
        try {
            // Extract the key from the URL
            String key = extractKeyFromUrl(fileUrl);
            if (key == null) {
                logger.warn("Could not extract key from URL: {}", fileUrl);
                return;
            }

            logger.debug("Deleting file from S3 bucket: {}, key: {}", bucketName, key);
            
            DeleteObjectRequest deleteRequest = DeleteObjectRequest.builder()
                    .bucket(bucketName)
                    .key(key)
                    .build();

            s3Client.deleteObject(deleteRequest);
            
            // Record metrics for S3 delete time
            Duration duration = Duration.between(startTime, Instant.now());
            metricsService.timeS3Operation("delete", duration.toMillis());
            
            logger.info("Successfully deleted file from S3: {}, took: {}ms", 
                    key, duration.toMillis());
            
        } catch (S3Exception e) {
            // Record failure metrics
            Duration duration = Duration.between(startTime, Instant.now());
            metricsService.incrementApiCounter("s3.delete.error");
            
            logger.error("Error deleting file from S3: {}, took: {}ms", 
                    fileUrl, duration.toMillis(), e);
            throw new RuntimeException("Failed to delete file from S3: " + e.getMessage(), e);
        }
    }
    
    /**
     * Generate a unique filename to prevent overwriting files in S3
     * 
     * @param originalFilename The original file name
     * @return A unique filename
     */
    private String generateUniqueFileName(String originalFilename) {
        String extension = "";
        if (originalFilename != null && originalFilename.contains(".")) {
            extension = originalFilename.substring(originalFilename.lastIndexOf("."));
        }
        return UUID.randomUUID().toString() + extension;
    }
    
    /**
     * Extract the key from a S3 URL
     * 
     * @param url The S3 URL
     * @return The key part of the URL
     */
    private String extractKeyFromUrl(String url) {
        if (url == null) return null;
        
        // Handle URLs in format https://bucket-name.s3.amazonaws.com/key
        if (url.contains(bucketName + ".s3.amazonaws.com/")) {
            return url.substring(url.indexOf(bucketName + ".s3.amazonaws.com/") 
                    + (bucketName + ".s3.amazonaws.com/").length());
        }
        
        // Handle URLs in format s3://bucket-name/key
        if (url.startsWith("s3://" + bucketName + "/")) {
            return url.substring(("s3://" + bucketName + "/").length());
        }
        
        return null;
    }
}