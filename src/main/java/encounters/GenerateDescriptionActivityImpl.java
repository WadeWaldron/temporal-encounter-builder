package encounters;

import java.util.List;

public class GenerateDescriptionActivityImpl implements GenerateDescriptionActivity {
    private final LLM llm;

    public GenerateDescriptionActivityImpl(LLM llm) {
        this.llm = llm;
    }
    
    @Override
    public String generateDescription(List<Creature> creatures) {
        String prompt = """
                You are a dungeon master creating an encounter for a Dungeons and Dragons game.
                
                There are exactly %d creature(s) in this encounter.
                Your players are encountering exactly these creatures: %s
                
                CRITICAL CONSTRAINTS:
                - Use ONLY the creatures listed. Do not add any additional creatures, duplicates, or variations.
                - Do not modify any creature names, counts, or characteristics.
                - Do not make any assumptions about the party.
                
                Consider:
                - What is a likely setting for this encounter based on the creature habitats?
                - What do these creatures look like and how do they behave?
                - How do the creatures react when they see the party?
                
                OUTPUT: Respond with ONLY a single paragraph, 3-5 sentences maximum. No preamble, no explanation, just the description.
                """.formatted(creatures.size(), creatures.toString());
                
        String response = llm.executePrompt(prompt);

        return response;
    }
}
