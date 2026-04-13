package encounters;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class GenerateDescriptionActivityImplTest {

    class MockLLM implements LLM {
        private String prompt;
        private String response;

        @Override
        public String executePrompt(String prompt) {
            this.prompt = prompt;
            return response;
        }

        public String getPrompt() {
            return prompt;
        }

        public void setResponse(String response) {
            this.response = response;
        }
    }

    private MockLLM mockLLM;
    private GenerateDescriptionActivityImpl activity;

    @BeforeEach
    public void setup() {
        mockLLM = new MockLLM();
        activity = new GenerateDescriptionActivityImpl(mockLLM);
    }

    @Test
    public void generateDescription_shouldCallLLMWithExpectedPrompt() {
        Creature creature1 = new CreatureBuilder().build();
        Creature creature2 = new CreatureBuilder().build();
        var creatures = List.of(creature1, creature2);

        String expectedResponse = "A fearsome encounter with a Goblin and an Orc!";
        mockLLM.setResponse(expectedResponse);

        String actualResult = activity.generateDescription(creatures);

        assertEquals(expectedResponse, actualResult);
        
        String actualPrompt = mockLLM.getPrompt();
        assertTrue(
            actualPrompt.contains("exactly 2 creature(s)"),
            "Prompt should correctly state the number of creatures"
        );
        assertTrue(
            actualPrompt.contains(creature1.name()) &&
            actualPrompt.contains(creature1.summary()) &&
            actualPrompt.contains(creature2.name()) &&
            actualPrompt.contains(creature2.summary()),
            "Prompt should include details of all creatures"
        );
    }
}
