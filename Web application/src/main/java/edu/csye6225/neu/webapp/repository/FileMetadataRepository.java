package edu.csye6225.neu.webapp.repository;

import edu.csye6225.neu.webapp.entity.FileMetadata;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface FileMetadataRepository extends JpaRepository<FileMetadata, UUID> {
    
    Optional<FileMetadata> findByS3BucketPath(String s3BucketPath);
    
    void deleteByS3BucketPath(String s3BucketPath);
}