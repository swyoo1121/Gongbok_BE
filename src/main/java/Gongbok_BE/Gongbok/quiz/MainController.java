package Gongbok_BE.Gongbok.quiz;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

@RestController
public class MainController {

    @PostMapping("/extract-topic")
    public String extractMainTopic(@RequestBody String input) {

        String apiKey = System.getenv("OPENAI_API_KEY");

        try {
            String mainTopic = extractMainTopicFromInput(input, apiKey);
            String question = generateQuestion(mainTopic, apiKey);

            return "Main topic: " + mainTopic + "\nGenerated question: " + question;
        } catch (IOException e) {
            return "Error: " + e.getMessage();
        }
    }

    private String extractMainTopicFromInput(String input, String apiKey) throws IOException {
        String apiUrl = "https://api.openai.com/v1/completions";

        String requestData = "{\n" +
                "  \"prompt\": \"Extract the main topic word from the following input:\\n\\n" + input + "\\n\\nMain topic word:\",\n" +
                "  \"max_tokens\": 7,\n" +
                "  \"n\": 1,\n" +
                "  \"stop\": \"\\n\",\n" +
                "  \"model\": \"text-davinci-003\"\n" +
                "}";


        URL url = new URL(apiUrl);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("POST");
        connection.setRequestProperty("Content-Type", "application/json");
        connection.setRequestProperty("Authorization", "Bearer " + apiKey);
        connection.setDoOutput(true);

        connection.getOutputStream().write(requestData.getBytes());

        int responseCode = connection.getResponseCode();
        if (responseCode == HttpURLConnection.HTTP_OK) {
            BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            String response = reader.readLine();
            reader.close();
            connection.disconnect();

            // Extract the main topic word from the API response
            int startIndex = response.indexOf("text\":\"") + 7;
            int endIndex = response.indexOf("\",\"");
            return response.substring(startIndex, endIndex).trim();
        } else {
            throw new IOException("Request failed with status code " + responseCode);
        }
    }

    private String generateQuestion(String answer, String apiKey) throws IOException {
            String apiUrl = "https://api.openai.com/v1/completions";

        // Compose the data for the API request
        String requestData = "{\n" +
                "  \"prompt\": \"What is the main topic of this paragraph?\",\n" +
                "  \"max_tokens\": 32,\n" +
                "  \"temperature\": 0.3,\n" +
                "  \"n\": 1,\n" +
                "  \"stop\": \"\\n\",\n" +
                "  \"model\": \"text-davinci-003\",\n" +
                "  \"documents\": [\n" +
                "    \"" + answer + "\"\n" +
                "  ]\n" +
                "}";

        // Make the API request
        URL url = new URL(apiUrl);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("POST");
        connection.setRequestProperty("Content-Type", "application/json");
        connection.setRequestProperty("Authorization", "Bearer " + apiKey);
        connection.setDoOutput(true);

        connection.getOutputStream().write(requestData.getBytes());

        int responseCode = connection.getResponseCode();
        if (responseCode == HttpURLConnection.HTTP_OK) {
            BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            String response = reader.readLine();
            reader.close();
            connection.disconnect();

            // Extract the generated question from the API response
            int startIndex = response.indexOf("text\":\"") + 7;
            int endIndex = response.indexOf("\",\"");
            return response.substring(startIndex, endIndex).trim();
        } else {
            throw new IOException("Request failed with status code " + responseCode);
        }
    }
}
