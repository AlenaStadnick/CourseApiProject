import org.apache.http.client.methods.*;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class ChecklistTests {

    private String baseUrl;
    private String token;
    private String taskId;
    private String checklistId;

    @BeforeClass
    public void setUp() throws IOException {
        Properties prop = new Properties();
        InputStream input = new FileInputStream("src/main/resources/config.properties");
        prop.load(input);
        baseUrl = prop.getProperty("base_url");
        token = prop.getProperty("token");
        taskId = prop.getProperty("task_id");
    }

    @Test(priority = 1)
    public void testCreateChecklist() throws IOException {
        String url = baseUrl + "task/" + taskId + "/checklist";
        CloseableHttpClient client = HttpClients.createDefault();
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

        Assert.assertEquals(statusCode, 200);

        checklistId = extractChecklistId(responseBody);

        // Перевірка наявності чекліста в відповіді
        Assert.assertTrue(responseBody.contains("\"name\":\"MyNewChecklistTest\""));

        client.close();
    }

    @Test(priority = 2, dependsOnMethods = "testCreateChecklist")
    public void testEditChecklist() throws IOException {
        String url = baseUrl + "checklist/" + checklistId;
        CloseableHttpClient client = HttpClients.createDefault();
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

        Assert.assertEquals(statusCode, 200);

        // Використання JSON парсеру для перевірки оновленої назви чекліста
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode jsonNode = objectMapper.readTree(responseBody);
        String updatedName = jsonNode.path("checklist").path("name").asText();
        int position = jsonNode.path("checklist").path("orderindex").asInt();

        Assert.assertEquals(updatedName, "Updated Checklist New Test");
        Assert.assertEquals(position, 2);

        client.close();
    }

    @Test(priority = 3, dependsOnMethods = "testCreateChecklist")
    public void testDeleteChecklist() throws IOException {
        String url = baseUrl + "checklist/" + checklistId;
        CloseableHttpClient client = HttpClients.createDefault();
        HttpDelete httpDelete = new HttpDelete(url);

        httpDelete.setHeader("Authorization", token);

        CloseableHttpResponse response = client.execute(httpDelete);
        int statusCode = response.getStatusLine().getStatusCode();
        String responseBody = EntityUtils.toString(response.getEntity());

        System.out.println("Response Code: " + statusCode);
        System.out.println("Response Body: " + responseBody);

        Assert.assertEquals(statusCode, 200);

        client.close();
    }

    private String extractChecklistId(String responseBody) throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode jsonNode = objectMapper.readTree(responseBody);
        return jsonNode.path("checklist").path("id").asText();
    }
}
