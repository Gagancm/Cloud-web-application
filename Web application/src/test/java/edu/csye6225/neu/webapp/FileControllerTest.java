package edu.csye6225.neu.webapp;

import edu.csye6225.neu.webapp.entity.FileMetadata;
import edu.csye6225.neu.webapp.repository.FileMetadataRepository;
import edu.csye6225.neu.webapp.service.FileService;
import io.github.cdimascio.dotenv.Dotenv;
import org.junit.jupiter.api.*;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@TestPropertySource(properties = {
    "app.max-file-size=10485760",
    "aws.s3.bucket=test-bucket"
})
public class FileControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private FileService fileService;

    @MockBean
    private FileMetadataRepository fileMetadataRepository;

    private static UUID testFileId;
    private static FileMetadata testFileMetadata;

    @BeforeAll
    static void setup() {
        // Load environment variables safely
        try {
            Dotenv dotenv = Dotenv.configure().ignoreIfMissing().load();
            if (dotenv != null) {
                System.setProperty("DB_USER_NAME", dotenv.get("DB_USER_NAME", "default_user"));
                System.setProperty("DB_PASSWORD", dotenv.get("DB_PASSWORD", "default_password"));
                System.setProperty("DB_URL", dotenv.get("DB_URL", "default_url"));
            } else {
                // Set default values if dotenv is not available
                System.setProperty("DB_USER_NAME", "default_user");
                System.setProperty("DB_PASSWORD", "default_password");
                System.setProperty("DB_URL", "default_url");
            }
        } catch (Exception e) {
            System.err.println("Warning: Could not load dotenv, using defaults: " + e.getMessage());
            System.setProperty("DB_USER_NAME", "default_user");
            System.setProperty("DB_PASSWORD", "default_password");
            System.setProperty("DB_URL", "default_url");
        }
        
        // Create test data
        testFileId = UUID.randomUUID();
        testFileMetadata = new FileMetadata();
        testFileMetadata.setId(testFileId);
        testFileMetadata.setFileName("test-file.jpg");
        testFileMetadata.setOriginalFileName("original-test-file.jpg");
        testFileMetadata.setS3BucketPath("https://test-bucket.s3.amazonaws.com/test-file.jpg");
        testFileMetadata.setContentType("image/jpeg");
        testFileMetadata.setSizeBytes(1024L);
        testFileMetadata.setUploadDate(LocalDateTime.now());
        testFileMetadata.setLastModified(LocalDateTime.now());
    }

    @Test
    @Order(1)
    @DisplayName("Test file upload")
    public void testFileUpload() throws Exception {
        // Create a mock file
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "test-image.jpg",
                MediaType.IMAGE_JPEG_VALUE,
                "test image content".getBytes()
        );

        // Mock the service response
        when(fileService.uploadFile(any(MockMultipartFile.class))).thenReturn(testFileMetadata);

        // Perform upload request and check result
        mockMvc.perform(MockMvcRequestBuilders.multipart("/api/files/upload")
                .file(file))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.fileName").value("original-test-file.jpg"))
                .andExpect(jsonPath("$.fileUrl").value("https://test-bucket.s3.amazonaws.com/test-file.jpg"))
                .andExpect(jsonPath("$.contentType").value("image/jpeg"))
                .andExpect(jsonPath("$.size").value(1024))
                .andExpect(header().string("Cache-Control", "no-cache, no-store, must-revalidate"))
                .andExpect(header().string("Pragma", "no-cache"))
                .andExpect(header().string("X-Content-Type-Options", "nosniff"));
    }

    @Test
    @Order(2)
    @DisplayName("Test get file by ID")
    public void testGetFileById() throws Exception {
        when(fileService.getFileById(testFileId)).thenReturn(Optional.of(testFileMetadata));

        mockMvc.perform(MockMvcRequestBuilders.get("/api/files/{id}", testFileId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(testFileId.toString()))
                .andExpect(jsonPath("$.fileName").value("original-test-file.jpg"))
                .andExpect(jsonPath("$.fileUrl").value("https://test-bucket.s3.amazonaws.com/test-file.jpg"))
                .andExpect(header().string("Cache-Control", "no-cache, no-store, must-revalidate"))
                .andExpect(header().string("Pragma", "no-cache"))
                .andExpect(header().string("X-Content-Type-Options", "nosniff"));
    }

    @Test
    @Order(3)
    @DisplayName("Test get all files")
    public void testGetAllFiles() throws Exception {
        when(fileService.getAllFiles()).thenReturn(Arrays.asList(testFileMetadata));

        mockMvc.perform(MockMvcRequestBuilders.get("/api/files"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(testFileId.toString()))
                .andExpect(jsonPath("$[0].fileName").value("original-test-file.jpg"))
                .andExpect(jsonPath("$[0].fileUrl").value("https://test-bucket.s3.amazonaws.com/test-file.jpg"))
                .andExpect(header().string("Cache-Control", "no-cache, no-store, must-revalidate"))
                .andExpect(header().string("Pragma", "no-cache"))
                .andExpect(header().string("X-Content-Type-Options", "nosniff"));
    }

    @Test
    @Order(4)
    @DisplayName("Test delete file")
    public void testDeleteFile() throws Exception {
        // Mock service method
        Mockito.doNothing().when(fileService).deleteFile(testFileId);

        mockMvc.perform(MockMvcRequestBuilders.delete("/api/files/{id}", testFileId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("File successfully deleted"))
                .andExpect(header().string("Cache-Control", "no-cache, no-store, must-revalidate"))
                .andExpect(header().string("Pragma", "no-cache"))
                .andExpect(header().string("X-Content-Type-Options", "nosniff"));
    }

    @Test
    @Order(5)
    @DisplayName("Test get file not found")
    public void testGetFileNotFound() throws Exception {
        UUID nonExistentId = UUID.randomUUID();
        when(fileService.getFileById(nonExistentId)).thenReturn(Optional.empty());

        mockMvc.perform(MockMvcRequestBuilders.get("/api/files/{id}", nonExistentId))
                .andExpect(status().isNotFound())
                .andExpect(header().string("Cache-Control", "no-cache, no-store, must-revalidate"))
                .andExpect(header().string("Pragma", "no-cache"))
                .andExpect(header().string("X-Content-Type-Options", "nosniff"));
    }

    @Test
    @Order(6)
    @DisplayName("Test upload empty file")
    public void testUploadEmptyFile() throws Exception {
        MockMultipartFile emptyFile = new MockMultipartFile(
                "file",
                "empty.jpg",
                MediaType.IMAGE_JPEG_VALUE,
                new byte[0]
        );

        mockMvc.perform(MockMvcRequestBuilders.multipart("/api/files/upload")
                .file(emptyFile))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").exists())
                .andExpect(header().string("Cache-Control", "no-cache, no-store, must-revalidate"))
                .andExpect(header().string("Pragma", "no-cache"))
                .andExpect(header().string("X-Content-Type-Options", "nosniff"));
    }
    
    @Test
    @Order(7)
    @DisplayName("Test upload unsupported file type")
    public void testUploadUnsupportedFileType() throws Exception {
        MockMultipartFile unsupportedFile = new MockMultipartFile(
                "file",
                "malicious.exe",
                "application/x-msdownload",
                "fake executable content".getBytes()
        );

        mockMvc.perform(MockMvcRequestBuilders.multipart("/api/files/upload")
                .file(unsupportedFile))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("File type not supported"))
                .andExpect(header().string("Cache-Control", "no-cache, no-store, must-revalidate"))
                .andExpect(header().string("Pragma", "no-cache"))
                .andExpect(header().string("X-Content-Type-Options", "nosniff"));
    }
}