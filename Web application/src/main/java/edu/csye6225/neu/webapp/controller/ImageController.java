package edu.csye6225.neu.webapp.controller;

import edu.csye6225.neu.webapp.service.S3Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/images")
public class ImageController {
    private static final Logger logger = LoggerFactory.getLogger(ImageController.class);

    private final S3Service s3Service;

    @Autowired
    public ImageController(S3Service s3Service) {
        this.s3Service = s3Service;
    }

    private HttpHeaders getSecurityHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setCacheControl("no-cache, no-store, must-revalidate");
        headers.setPragma("no-cache");
        headers.set("X-Content-Type-Options", "nosniff");
        return headers;
    }

    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Map<String, String>> uploadImage(@RequestParam("file") MultipartFile file) {
        try {
            if (file.isEmpty()) {
                return ResponseEntity.badRequest()
                        .headers(getSecurityHeaders())
                        .body(Map.of("error", "File cannot be empty"));
            }
            
            // Check if the file is an image
            String contentType = file.getContentType();
            if (contentType == null || !contentType.startsWith("image/")) {
                return ResponseEntity.badRequest()
                        .headers(getSecurityHeaders())
                        .body(Map.of("error", "File must be an image"));
            }
            
            // Upload to S3
            String fileUrl = s3Service.uploadFile(file);
            logger.info("File uploaded successfully: {}", fileUrl);
            
            Map<String, String> response = new HashMap<>();
            response.put("message", "Image successfully uploaded");
            response.put("url", fileUrl);
            
            return ResponseEntity.status(HttpStatus.CREATED)
                    .headers(getSecurityHeaders())
                    .body(response);
            
        } catch (IOException e) {
            logger.error("Failed to upload image", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .headers(getSecurityHeaders())
                    .body(Map.of("error", "Failed to upload image: " + e.getMessage()));
        }
    }
    
    @DeleteMapping("/delete")
    public ResponseEntity<Map<String, String>> deleteImage(@RequestParam("url") String imageUrl) {
        try {
            if (imageUrl == null || imageUrl.isEmpty()) {
                return ResponseEntity.badRequest()
                        .headers(getSecurityHeaders())
                        .body(Map.of("error", "Image URL cannot be empty"));
            }
            
            s3Service.deleteFile(imageUrl);
            logger.info("File deleted successfully: {}", imageUrl);
            
            return ResponseEntity.ok()
                    .headers(getSecurityHeaders())
                    .body(Map.of("message", "Image successfully deleted"));
            
        } catch (Exception e) {
            logger.error("Failed to delete image", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .headers(getSecurityHeaders())
                    .body(Map.of("error", "Failed to delete image: " + e.getMessage()));
        }
    }
}