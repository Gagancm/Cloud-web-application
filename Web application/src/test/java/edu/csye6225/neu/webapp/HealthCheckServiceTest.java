package edu.csye6225.neu.webapp;

import io.github.cdimascio.dotenv.Dotenv;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@AutoConfigureMockMvc
@SpringBootTest
public class HealthCheckServiceTest {

    @Autowired
    private MockMvc mockMvc;


    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);
    }

    @BeforeAll
    static void loadEnvVariables() {
        Dotenv dotenv = Dotenv.configure().ignoreIfMissing().load();

        System.setProperty("DB_USER_NAME", dotenv.get("DB_USER_NAME", "default_user"));
        System.setProperty("DB_PASSWORD", dotenv.get("DB_PASSWORD", "default_password"));
        System.setProperty("DB_URL", dotenv.get("DB_URL", "default_url"));
    }

    @Test
    public void testHealthCheck_Success() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/healthz"))
                .andExpect(status().isOk())
                .andExpect(content().string(""))
                .andExpect(header().string("Cache-Control", "no-cache, no-store, must-revalidate"))
                .andExpect(header().string("Pragma", "no-cache"))
                .andExpect(header().string("X-Content-Type-Options", "nosniff"));
    }

}