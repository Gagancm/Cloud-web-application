package edu.csye6225.neu.webapp;

import io.github.cdimascio.dotenv.Dotenv;
import io.restassured.RestAssured;
import org.junit.jupiter.api.*;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;
import static io.restassured.RestAssured.given;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class HealthCheckApiTest {

    @BeforeAll
    static void loadEnvVariables() {
        Dotenv dotenv = Dotenv.configure().ignoreIfMissing().load();

        System.setProperty("DB_USER_NAME", dotenv.get("DB_USER_NAME", "default_user"));
        System.setProperty("DB_PASSWORD", dotenv.get("DB_PASSWORD", "default_password"));
        System.setProperty("DB_URL", dotenv.get("DB_URL", "default_url"));
    }

    @LocalServerPort
    private int port;

    @BeforeEach
    public void setup() {
        RestAssured.baseURI = "http://localhost";
        RestAssured.port = port;
    }

    @Test
    @Order(1)
    @DisplayName("GET /healthz - Success")
    public void validateHealthCheckEndpoint() {
        given()
                .when()
                .get("/healthz")
                .then()
                .assertThat()
                .statusCode(200)
                .header("Cache-Control", "no-cache, no-store, must-revalidate")
                .header("Pragma", "no-cache")
                .header("X-Content-Type-Options", "nosniff");
    }

    @Test
    @Order(2)
    @DisplayName("Ensure query parameters are not allowed for /healthz")
    public void rejectQueryParamsInHealthCheck() {
        given()
                .queryParam("extra", "test")
                .when()
                .get("/healthz")
                .then()
                .assertThat()
                .statusCode(400)
                .header("Cache-Control", "no-cache, no-store, must-revalidate")
                .header("Pragma", "no-cache")
                .header("X-Content-Type-Options", "nosniff");
    }

    @Test
    @Order(3)
    @DisplayName("Ensure request body is not supported for /healthz")
    public void rejectRequestBodyInHealthCheck() {
        given()
                .body("{\"unexpected\": \"data\"}")
                .contentType("application/json")
                .when()
                .get("/healthz")
                .then()
                .assertThat()
                .statusCode(400)
                .header("Cache-Control", "no-cache, no-store, must-revalidate")
                .header("Pragma", "no-cache")
                .header("X-Content-Type-Options", "nosniff");
    }

    @Test
    @Order(4)
    @DisplayName("Validate POST method returns 405")
    public void verifyPostMethodNotAllowed() {
        given()
                .when()
                .post("/healthz")
                .then()
                .assertThat()
                .statusCode(405)
                .header("Cache-Control", "no-cache, no-store, must-revalidate")
                .header("Pragma", "no-cache")
                .header("X-Content-Type-Options", "nosniff");
    }

    @Test
    @Order(5)
    @DisplayName("Validate PUT method returns 405")
    public void verifyPutMethodNotAllowed() {
        given()
                .when()
                .put("/healthz")
                .then()
                .assertThat()
                .statusCode(405)
                .header("Cache-Control", "no-cache, no-store, must-revalidate")
                .header("Pragma", "no-cache")
                .header("X-Content-Type-Options", "nosniff");
    }

    @Test
    @Order(6)
    @DisplayName("Validate DELETE method returns 405")
    public void verifyDeleteMethodNotAllowed() {
        given()
                .when()
                .delete("/healthz")
                .then()
                .assertThat()
                .statusCode(405)
                .header("Cache-Control", "no-cache, no-store, must-revalidate")
                .header("Pragma", "no-cache")
                .header("X-Content-Type-Options", "nosniff");
    }

    @Test
    @Order(7)
    @DisplayName("Validate PATCH method returns 405")
    public void verifyPatchMethodNotAllowed() {
        given()
                .when()
                .patch("/healthz")
                .then()
                .assertThat()
                .statusCode(405)
                .header("Cache-Control", "no-cache, no-store, must-revalidate")
                .header("Pragma", "no-cache")
                .header("X-Content-Type-Options", "nosniff");
    }

    @Test
    @Order(8)
    @DisplayName("Confirm invalid endpoints return 404")
    public void ensureInvalidEndpointFails() {
        given()
                .when()
                .get("/invalid-url")
                .then()
                .assertThat()
                .statusCode(404);
    }
}