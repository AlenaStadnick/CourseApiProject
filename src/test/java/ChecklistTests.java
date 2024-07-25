import org.apache.http.client.methods.*;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import io.qameta.allure.Allure;
import io.qameta.allure.Step;
import io.qameta.allure.Description;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ChecklistTests {

    private static String baseUrl;
    private static String token;
    private static String taskId;
    private static String checklistId;

    @BeforeAll
    public static void setUp() throws IOException {
        Properties prop = new Properties();
        InputStream input = new FileInputStream("src/main/resources/config.properties");
        prop.load(input);
        baseUrl = prop.getProperty("base_url");
        token = prop.getProperty("token");
        taskId = prop.getProperty("task_id");
    }

    @Test
    @Description("Test case for creating a checklist")
    public void testCreateChecklist() throws IOException {
        String url = baseUrl + "task/" + taskId + "/checklist";
        try (CloseableHttpClient client = HttpClients.createDefault()) {
            HttpPost httpPost = new HttpPost(url);
            httpPost.setHeader("Authorization", token);
            httpPost.setHeader("Content-Type", "application/json");

            String json = "{ \"name\": \"MyNewChecklistTest\" }";
            StringEntity entity = new StringEntity(json);
            httpPost.setEntity(entity);

            CloseableHttpResponse response = client.execute(httpPost);
            int statusCode = response.getStatusLine().getStatusCode();
            String responseBody = EntityUtils.toString(response.getEntity());

            System.out.println("Response Code: " + statusCode);
            System.out.println("Response Body: " + responseBody);

            assertEquals(200, statusCode);
            checklistId = extractChecklistId(responseBody);

            // Перевірка наявності чекліста в відповіді
            assertTrue(responseBody.contains("\"name\":\"MyNewChecklistTest\""));

            Allure.step("Created checklist with ID: " + checklistId);
        }
    }

    @Test
    @Description("Test for editing a checklist")
    public void testEditChecklist() throws IOException {
        String url = baseUrl + "checklist/" + checklistId;
        try (CloseableHttpClient client = HttpClients.createDefault()) {
            HttpPut httpPut = new HttpPut(url);
            httpPut.setHeader("Authorization", token);
            httpPut.setHeader("Content-Type", "application/json");

            String json = "{ \"name\": \"Updated Checklist New Test\", \"position\": 1 }";
            StringEntity entity = new StringEntity(json);
            httpPut.setEntity(entity);

            CloseableHttpResponse response = client.execute(httpPut);
            int statusCode = response.getStatusLine().getStatusCode();
            String responseBody = EntityUtils.toString(response.getEntity());

            System.out.println("Response Code: " + statusCode);
            System.out.println("Response Body: " + responseBody);

            assertEquals(200, statusCode);

            // Використання JSON парсеру для перевірки оновленої назви чекліста
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode jsonNode = objectMapper.readTree(responseBody);
            String updatedName = jsonNode.path("checklist").path("name").asText();
            int position = jsonNode.path("checklist").path("orderindex").asInt();

            assertEquals("Updated Checklist New Test", updatedName);
            assertEquals(2, position);

            Allure.step("Edited checklist with ID: " + checklistId);
        }
    }

    @Test
    @Description("Test for deleting a checklist")
    public void testDeleteChecklist() throws IOException {
        String url = baseUrl + "checklist/" + checklistId;
        try (CloseableHttpClient client = HttpClients.createDefault()) {
            HttpDelete httpDelete = new HttpDelete(url);
            httpDelete.setHeader("Authorization", token);

            CloseableHttpResponse response = client.execute(httpDelete);
            int statusCode = response.getStatusLine().getStatusCode();
            String responseBody = EntityUtils.toString(response.getEntity());

            System.out.println("Response Code: " + statusCode);
            System.out.println("Response Body: " + responseBody);

            assertEquals(200, statusCode);

            Allure.step("Deleted checklist with ID: " + checklistId);
        }
    }

    private String extractChecklistId(String responseBody) throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode jsonNode = objectMapper.readTree(responseBody);
        return jsonNode.path("checklist").path("id").asText();
    }
}
