package encounters;

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

public class OllamaTest {

    private static Ollama ollama = new Ollama();

    @Test
    public void executePrompt_shouldContactOllamaAndReturnAResponse() {
        String prompt = "Repeat after me: Hello World!";
        String response = ollama.executePrompt(prompt);

        assertTrue(response.contains("Hello World"), "Response should contain the expected text");
    }
}
