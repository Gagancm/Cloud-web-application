package edu.csye6225.neu.webapp.service;

import edu.csye6225.neu.webapp.entity.FileMetadata;
import edu.csye6225.neu.webapp.repository.FileMetadataRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class FileService {
    private static final Logger logger = LoggerFactory.getLogger(FileService.class);

    private final String bucketName;
    private final FileMetadataRepository fileMetadataRepository;
    private final S3Service s3Service;
    private final MetricsService metricsService;

    @Autowired
    public FileService(@Value("${aws.s3.bucket}") String bucketName,
                       FileMetadataRepository fileMetadataRepository,
                       S3Service s3Service,
                       MetricsService metricsService) {
        this.bucketName = bucketName;
        this.fileMetadataRepository = fileMetadataRepository;
        this.s3Service = s3Service;
        this.metricsService = metricsService;
        logger.info("FileService initialized with bucket: {}", bucketName);
    }

    /**
     * Upload a file to S3 and store its metadata in the database
     *
     * @param file The file to upload
     * @return The file metadata entity
     * @throws IOException If the file cannot be read
     */
    @Transactional
    public FileMetadata uploadFile(MultipartFile file) throws IOException {
        if (file == null || file.isEmpty()) {
            logger.warn("Attempted to upload empty or null file");
            throw new IllegalArgumentException("File is empty or null");
        }

        try {
            logger.info("Starting file upload process for file: {}", file.getOriginalFilename());
            
            // Upload file to S3 with metrics
            String s3Url = metricsService.timeS3Operation("uploadFile", () -> {
                try {
                    return s3Service.uploadFile(file);
                } catch (IOException e) {
                    logger.error("S3 upload failed", e);
                    throw new RuntimeException("Failed to upload file to S3", e);
                }
            });
            
            logger.debug("File uploaded to S3, URL: {}", s3Url);
            
            // Create file metadata
            final FileMetadata fileMetadata = new FileMetadata();
            fileMetadata.setFileName(extractKeyFromUrl(s3Url));
            fileMetadata.setOriginalFileName(file.getOriginalFilename());
            fileMetadata.setS3BucketPath(s3Url);
            fileMetadata.setContentType(file.getContentType());
            fileMetadata.setSizeBytes(file.getSize());
            
            // Save metadata to database with metrics
            logger.debug("Saving file metadata to database");
            FileMetadata savedMetadata = metricsService.timeDatabaseQuery("saveFileMetadata", () -> 
                fileMetadataRepository.save(fileMetadata)
            );
            
            logger.info("File metadata saved to database: {}", savedMetadata.getId());
            
            return savedMetadata;
        } catch (Exception e) {
            logger.error("Error uploading file and saving metadata", e);
            throw new RuntimeException("Failed to process file upload: " + e.getMessage(), e);
        }
    }
    
    /**
     * Get file metadata by ID
     *
     * @param id The file ID
     * @return Optional containing file metadata if found
     */
    public Optional<FileMetadata> getFileById(UUID id) {
        if (id == null) {
            logger.warn("Attempted to get file with null ID");
            throw new IllegalArgumentException("File ID cannot be null");
        }
        
        logger.debug("Fetching file metadata for ID: {}", id);
        return metricsService.timeDatabaseQuery("getFileById", () -> 
            fileMetadataRepository.findById(id)
        );
    }
    
    /**
     * Get all file metadata entries
     *
     * @return List of all file metadata
     */
    public List<FileMetadata> getAllFiles() {
        logger.debug("Fetching all file metadata");
        return metricsService.timeDatabaseQuery("getAllFiles", () -> 
            fileMetadataRepository.findAll()
        );
    }
    
    /**
     * Delete a file from S3 and its metadata from the database
     *
     * @param id The file ID
     * @throws IllegalArgumentException if file not found
     */
    @Transactional
    public void deleteFile(UUID id) {
        if (id == null) {
            logger.warn("Attempted to delete file with null ID");
            throw new IllegalArgumentException("File ID cannot be null");
        }
        
        logger.debug("Looking up file metadata for deletion, ID: {}", id);
        
        // Get file metadata with metrics
        final FileMetadata metadata = metricsService.timeDatabaseQuery("findFileForDeletion", () -> 
            fileMetadataRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("File not found with ID: " + id))
        );
        
        try {
            logger.info("Deleting file from S3: {}", metadata.getS3BucketPath());
            
            // Delete from S3 with metrics
            metricsService.timeS3Operation("deleteFile", () -> {
                s3Service.deleteFile(metadata.getS3BucketPath());
                return null; // Need to return something due to generics
            });
            
            // Delete from database with metrics
            logger.debug("Deleting file metadata from database, ID: {}", id);
            metricsService.timeDatabaseQuery("deleteFileMetadata", () -> {
                fileMetadataRepository.delete(metadata);
                return null; // Need to return something due to generics
            });
            
            logger.info("File deleted with ID: {}", id);
        } catch (Exception e) {
            logger.error("Error deleting file with ID: {}", id, e);
            throw new RuntimeException("Failed to delete file: " + e.getMessage(), e);
        }
    }
    
    /**
     * Extract the key from a S3 URL
     *
     * @param url The S3 URL
     * @return The key part of the URL
     */
    private String extractKeyFromUrl(String url) {
        if (url == null) {
            return null;
        }
        
        // Handle URLs in format https://bucket-name.s3.amazonaws.com/key
        if (url.contains(bucketName + ".s3.amazonaws.com/")) {
            return url.substring(url.indexOf(bucketName + ".s3.amazonaws.com/") 
                    + (bucketName + ".s3.amazonaws.com/").length());
        }
        
        // Handle URLs in format s3://bucket-name/key
        if (url.startsWith("s3://" + bucketName + "/")) {
            return url.substring(("s3://" + bucketName + "/").length());
        }
        
        // Handle URLs in format https://s3.amazonaws.com/bucket-name/key
        if (url.contains("s3.amazonaws.com/" + bucketName + "/")) {
            return url.substring(url.indexOf("s3.amazonaws.com/" + bucketName + "/") 
                    + ("s3.amazonaws.com/" + bucketName + "/").length());
        }
        
        logger.warn("Could not extract key from URL: {}", url);
        return url; // Return the URL as a fallback
    }
}