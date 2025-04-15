package edu.csye6225.neu.webapp;

import io.github.cdimascio.dotenv.Dotenv;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
class WebAppApplicationTests {

    @BeforeAll
    static void loadEnvVariables() {
        Dotenv dotenv = Dotenv.configure().ignoreIfMissing().load();

        System.setProperty("DB_USER_NAME", dotenv.get("DB_USER_NAME", "default_user"));
        System.setProperty("DB_PASSWORD", dotenv.get("DB_PASSWORD", "default_password"));
        System.setProperty("DB_URL", dotenv.get("DB_URL", "default_url"));
    }

    @Test
    void contextLoads() {

    }
}