package edu.csye6225.neu.webapp.controller;

import edu.csye6225.neu.webapp.entity.FileMetadata;
import edu.csye6225.neu.webapp.service.FileService;
import edu.csye6225.neu.webapp.service.MetricsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/v1/file")
public class FileController {
    private static final Logger logger = LoggerFactory.getLogger(FileController.class);

    // Define allowed content types for security
    private static final List<String> ALLOWED_CONTENT_TYPES = Arrays.asList(
        "image/jpeg", "image/png", "image/gif", "image/webp", 
        "application/pdf", "application/msword", "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
        "application/vnd.ms-excel", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
        "application/vnd.ms-powerpoint", "application/vnd.openxmlformats-officedocument.presentationml.presentation",
        "text/plain", "text/csv", "application/json", "application/xml"
    );
    
    // Maximum file size in bytes (default 10MB)
    @Value("${app.max-file-size:10485760}")
    private long maxFileSize;

    private final FileService fileService;
    private final MetricsService metricsService;

    @Autowired
    public FileController(FileService fileService, MetricsService metricsService) {
        this.fileService = fileService;
        this.metricsService = metricsService;
    }

    private HttpHeaders getSecurityHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setCacheControl("no-cache, no-store, must-revalidate");
        headers.setPragma("no-cache");
        headers.set("X-Content-Type-Options", "nosniff");
        return headers;
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Map<String, Object>> uploadFile(@RequestParam("file") MultipartFile file) {
        logger.info("Received file upload request for file: {}", file.getOriginalFilename());
        metricsService.incrementApiCounter("file_upload");
        
        return metricsService.timeApiExecution("file_upload", () -> {
            try {
                // Validate file is not empty
                if (file.isEmpty()) {
                    logger.warn("Empty file upload attempt");
                    return ResponseEntity.badRequest()
                            .headers(getSecurityHeaders())
                            .body(Map.of("error", "File cannot be empty"));
                }
                
                // Validate file size
                if (file.getSize() > maxFileSize) {
                    logger.warn("File size exceeds limit: {} bytes", file.getSize());
                    return ResponseEntity.status(HttpStatus.PAYLOAD_TOO_LARGE)
                            .headers(getSecurityHeaders())
                            .body(Map.of("error", "File size exceeds the maximum allowed limit of " + (maxFileSize / 1024 / 1024) + " MB"));
                }
                
                // Validate content type for security
                String contentType = file.getContentType();
                if (contentType == null || !ALLOWED_CONTENT_TYPES.contains(contentType.toLowerCase())) {
                    logger.warn("Unsupported content type: {}", contentType);
                    return ResponseEntity.badRequest()
                            .headers(getSecurityHeaders())
                            .body(Map.of("error", "File type not supported"));
                }
                
                FileMetadata metadata = fileService.uploadFile(file);
                logger.info("File uploaded successfully: {}", metadata.getS3BucketPath());
                
                // Format response according to API specification
                Map<String, Object> response = new HashMap<>();
                response.put("id", metadata.getId());
                response.put("file_name", metadata.getOriginalFileName());
                response.put("url", metadata.getS3BucketPath());
                response.put("upload_date", metadata.getUploadDate().toLocalDate().format(DateTimeFormatter.ISO_DATE));
                
                return ResponseEntity.status(HttpStatus.CREATED)
                        .headers(getSecurityHeaders())
                        .body(response);
                
            } catch (IOException e) {
                logger.error("Failed to upload file", e);
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .headers(getSecurityHeaders())
                        .body(Map.of("error", "Failed to upload file: " + e.getMessage()));
            }
        });
    }

    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> getFile(@PathVariable UUID id) {
        logger.info("Received request to get file with ID: {}", id);
        metricsService.incrementApiCounter("file_get");
        
        return metricsService.timeApiExecution("file_get", () -> {
            try {
                return fileService.getFileById(id)
                        .map(metadata -> {
                            // Format response according to API specification
                            Map<String, Object> response = new HashMap<>();
                            response.put("id", metadata.getId());
                            response.put("file_name", metadata.getOriginalFileName());
                            response.put("url", metadata.getS3BucketPath());
                            response.put("upload_date", metadata.getUploadDate().toLocalDate().format(DateTimeFormatter.ISO_DATE));
                            logger.info("Retrieved file metadata for ID: {}", id);
                            
                            return ResponseEntity.ok()
                                    .headers(getSecurityHeaders())
                                    .body(response);
                        })
                        .orElse(ResponseEntity.notFound()
                                .headers(getSecurityHeaders())
                                .build());
                
            } catch (Exception e) {
                logger.error("Error retrieving file with ID: {}", id, e);
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .headers(getSecurityHeaders())
                        .body(Map.of("error", "Failed to retrieve file: " + e.getMessage()));
            }
        });
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteFile(@PathVariable UUID id) {
        logger.info("Received request to delete file with ID: {}", id);
        metricsService.incrementApiCounter("file_delete");
        
        return metricsService.timeApiExecution("file_delete", () -> {
            try {
                fileService.deleteFile(id);
                logger.info("File deleted successfully with ID: {}", id);
                
                // Return 204 No Content as per API specification
                return ResponseEntity.noContent()
                        .headers(getSecurityHeaders())
                        .build();
                
            } catch (IllegalArgumentException e) {
                logger.warn("File not found with ID: {}", id);
                return ResponseEntity.notFound()
                        .headers(getSecurityHeaders())
                        .build();
                
            } catch (Exception e) {
                logger.error("Failed to delete file with ID: {}", id, e);
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .headers(getSecurityHeaders())
                        .build();
            }
        });
    }
    
    // Handle non-supported methods for /v1/file path with 405 Method Not Allowed response
    @RequestMapping(value = "", method = {
            RequestMethod.PUT, RequestMethod.PATCH,
            RequestMethod.HEAD, RequestMethod.OPTIONS,
            RequestMethod.TRACE
    })
    public ResponseEntity<Void> methodNotAllowedForFilePath() {
        logger.warn("Unsupported HTTP method attempted on /v1/file path");
        return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED)
                .headers(getSecurityHeaders())
                .build();
    }
    
    // Handle non-supported methods for /v1/file/{id} path with 405 Method Not Allowed response
    @RequestMapping(value = "/{id}", method = {
            RequestMethod.PUT, RequestMethod.PATCH,
            RequestMethod.HEAD, RequestMethod.OPTIONS,
            RequestMethod.TRACE, RequestMethod.POST
    })
    public ResponseEntity<Void> methodNotAllowedForFileIdPath() {
        logger.warn("Unsupported HTTP method attempted on /v1/file/{id} path");
        return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED)
                .headers(getSecurityHeaders())
                .build();
    }
    
    // Handle GET /v1/file (without id) which should return 400 Bad Request
    @GetMapping
    public ResponseEntity<Map<String, String>> getAllFiles() {
        logger.warn("GET without ID is not supported");
        metricsService.incrementApiCounter("file_get_all");
        return ResponseEntity.badRequest()
                .headers(getSecurityHeaders())
                .build();
    }
    
    // Add this method to handle DELETE without ID
    @DeleteMapping
    public ResponseEntity<Map<String, String>> deleteFileWithoutId() {
        logger.warn("DELETE without ID is not supported");
        metricsService.incrementApiCounter("file_delete_all");
        return ResponseEntity.badRequest()
                .headers(getSecurityHeaders())
                .build();
    }
    
    // Exception handler for method not allowed
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<Void> handleMethodNotAllowed() {
        logger.warn("Method not allowed exception thrown");
        return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED)
                .headers(getSecurityHeaders())
                .build();
    }
}