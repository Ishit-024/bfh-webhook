package com.bajajfinserv.test.bfh_webhook;

import java.util.HashMap;
import java.util.Map;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@SpringBootApplication
public class BfhWebhookApplication {

    public static void main(String[] args) {
        SpringApplication.run(BfhWebhookApplication.class, args);
    }

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }

    @Bean
    public CommandLineRunner run(RestTemplate restTemplate) {
        return args -> {
            ObjectMapper mapper = new ObjectMapper();

            try {
                Map<String, String> generateBody = new HashMap<>();
                generateBody.put("name", "Ishit Setia");
                generateBody.put("regNo", "22BLC1039");
                generateBody.put("email", "ishitsetia@gmail.com");

                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.APPLICATION_JSON);

                String generateUrl = "https://bfhldevapigw.healthrx.co.in/hiring/generateWebhook/JAVA";
                HttpEntity<String> genRequest = new HttpEntity<>(mapper.writeValueAsString(generateBody), headers);

                String genResponse = restTemplate.postForObject(generateUrl, genRequest, String.class);
                System.out.println("generateWebhook raw response: " + genResponse);

                JsonNode root = mapper.readTree(genResponse);
                String webhookUrl = root.path("webhook").asText();
                String accessToken = root.path("accessToken").asText();
                System.out.println("webhook: " + webhookUrl);
                System.out.println("accessToken: " + accessToken);

                String finalQuery = "SELECT p.AMOUNT AS SALARY, " +
                        "CONCAT(e.FIRST_NAME, ' ', e.LAST_NAME) AS NAME, " +
                        "TIMESTAMPDIFF(YEAR, e.DOB, CURDATE()) AS AGE, " +
                        "d.DEPARTMENT_NAME " +
                        "FROM PAYMENTS p " +
                        "JOIN EMPLOYEE e ON p.EMP_ID = e.EMP_ID " +
                        "JOIN DEPARTMENT d ON e.DEPARTMENT = d.DEPARTMENT_ID " +
                        "WHERE DAY(p.PAYMENT_TIME) <> 1 " +
                        "AND p.AMOUNT = (SELECT MAX(AMOUNT) FROM PAYMENTS WHERE DAY(PAYMENT_TIME) <> 1);";

                Map<String, String> submitBody = new HashMap<>();
                submitBody.put("finalQuery", finalQuery);

                HttpHeaders submitHeaders = new HttpHeaders();
                submitHeaders.setContentType(MediaType.APPLICATION_JSON);
                submitHeaders.set("Authorization", accessToken);   // <-- fixed


                HttpEntity<String> submitRequest = new HttpEntity<>(mapper.writeValueAsString(submitBody), submitHeaders);

                ResponseEntity<String> submitResponse = restTemplate.postForEntity(webhookUrl, submitRequest, String.class);

                System.out.println("Submission status: " + submitResponse.getStatusCode());
                System.out.println("Submission response: " + submitResponse.getBody());

            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                System.exit(0);
            }
        };
    }
}
