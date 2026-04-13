package encounters;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.URI;

public class Ollama implements LLM {
    private static final String OLLAMA_BASE_URL = "http://localhost:11434/api/generate";
    private static final String MODEL = "mistral";
    private static final ObjectMapper mapper = new ObjectMapper();
    private static final HttpClient client = HttpClient.newHttpClient();

    public static class OllamaHttpException extends RuntimeException {
        public OllamaHttpException(int statusCode, String responseBody) {
            super("Ollama API returned status: " + statusCode + ", response: " + responseBody);
        }
    }

    public static class OllamaException extends RuntimeException {
        public OllamaException(Throwable cause) { 
            super("Calling Ollama at " + OLLAMA_BASE_URL + " failed. Please ensure Ollama is running and accessible.", cause);
        }
    }

    private record GenerateRequest(String model, String prompt, boolean stream) {}

    @Override
    public String executePrompt(String prompt) {
        try {
            String requestBody = mapper.writeValueAsString(new GenerateRequest(MODEL, prompt, false));
            
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(OLLAMA_BASE_URL))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                    .build();
            
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            
            if (response.statusCode() != 200) {
                throw new OllamaHttpException(response.statusCode(), response.body());
            }
            
            JsonNode root = mapper.readTree(response.body());
            return root.get("response").asText();
        } catch (Exception e) {
            throw new OllamaException(e);
        }
    }
}
