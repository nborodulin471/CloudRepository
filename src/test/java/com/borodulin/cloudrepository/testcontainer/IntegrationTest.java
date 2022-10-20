package com.borodulin.cloudrepository.testcontainer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.ext.ScriptUtils;
import org.testcontainers.jdbc.JdbcDatabaseDelegate;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.http.MediaType.MULTIPART_FORM_DATA_VALUE;

@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class IntegrationTest {
    @Container
    private final static PostgreSQLContainer<?> postgresDB = new PostgreSQLContainer<>("postgres:latest")
            .withExposedPorts(5432, 5432)
            .withUsername("postgres")
            .withPassword("myPassword");
    private static boolean isDbInit;
    @Autowired
    private TestRestTemplate restTemplate;

    @DynamicPropertySource
    public static void properties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgresDB::getJdbcUrl);
        registry.add("spring.datasource.username", postgresDB::getUsername);
        registry.add("spring.datasource.password", postgresDB::getPassword);
    }

    public static Resource getTestFile() {
        Path file = Paths.get("src/test/resources").resolve("test.txt");
        return new FileSystemResource(file);
    }

    @BeforeEach
    void init() throws IOException {
        var containerDelegate = new JdbcDatabaseDelegate(postgresDB, "");
        if (!isDbInit) {
            ScriptUtils.runInitScript(containerDelegate, "init-data-user.sql");
            isDbInit = true;
        }
        Files.copy(getTestFile().getInputStream(), Paths.get("files-upload").resolve("test.txt"), StandardCopyOption.REPLACE_EXISTING);
        ScriptUtils.runInitScript(containerDelegate, "init-data-file.sql");
    }

    @Test
    public void login() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(APPLICATION_JSON);
        HttpEntity<String> requestEntity = new HttpEntity<>(
                "{" +
                        "\"login\": \"admin\"," +
                        "\"password\":\"password\"" +
                        "}", headers);

        ResponseEntity<String> forEntity = restTemplate.postForEntity(
                "/login",
                requestEntity,
                String.class
        );

        assertSame(forEntity.getStatusCode(), OK);
    }

    @Test
    public void getFile() throws IOException {
        HttpHeaders headers = new HttpHeaders();
        headers.set("auth-token", getAuthToken());

        ResponseEntity<Resource> forEntity = restTemplate.exchange(
                "/file?filename=test.txt", HttpMethod.GET, new HttpEntity<>(headers),
                Resource.class);

        assertSame(forEntity.getStatusCode(), OK);
    }

    @Test
    public void getList() throws IOException {
        HttpHeaders headers = new HttpHeaders();
        headers.add("auth-token", getAuthToken());

        ResponseEntity<List> forEntity = restTemplate.exchange(
                "/list?limit=10", HttpMethod.GET, new HttpEntity<>(headers),
                List.class);

        assertSame(forEntity.getStatusCode(), OK);
    }

    @Test
    public void postFile() throws IOException {
        HttpHeaders headers = new HttpHeaders();
        headers.add("auth-token", getAuthToken());
        headers.add("content-type", MULTIPART_FORM_DATA_VALUE);
        MultiValueMap<String, Object> form = new LinkedMultiValueMap<>();
        form.add("file", getTestFile());

        ResponseEntity<Void> forEntity = restTemplate.postForEntity(
                "/file?filename=test.txt",
                new HttpEntity<>(form, headers),
                Void.class);

        assertSame(forEntity.getStatusCode(), OK);
    }

    @Test
    public void putFile() throws IOException {
        HttpHeaders headers = new HttpHeaders();
        headers.add("auth-token", getAuthToken());
        headers.setContentType(APPLICATION_JSON);
        String body = "{" +
                "\"name\":\"ajdkfnjkad\"" +
                "}";

        ResponseEntity<Void> forEntity = restTemplate.exchange(
                "/file?filename=test.txt",
                HttpMethod.PUT,
                new HttpEntity<>(body, headers),
                Void.class);

        assertSame(forEntity.getStatusCode(), OK);
    }

    @Test
    public void deleteFile() throws IOException {
        HttpHeaders headers = new HttpHeaders();
        headers.add("auth-token", getAuthToken());

        ResponseEntity<Void> forEntity = restTemplate.exchange(
                "/file?filename=test.txt",
                HttpMethod.DELETE,
                new HttpEntity<>(headers),
                Void.class);

        assertSame(forEntity.getStatusCode(), OK);
    }

    private String getAuthToken() throws JsonProcessingException {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(APPLICATION_JSON);
        HttpEntity<String> requestEntity = new HttpEntity<>(
                "{" +
                        "\"login\": \"admin\"," +
                        "\"password\":\"password\"" +
                        "}", headers);

        ResponseEntity<String> forEntity = restTemplate.postForEntity(
                "/login",
                requestEntity,
                String.class
        );

        Map res = new ObjectMapper().readValue(forEntity.getBody(), Map.class);
        return "Bearer " + res.get("auth-token");
    }
}
