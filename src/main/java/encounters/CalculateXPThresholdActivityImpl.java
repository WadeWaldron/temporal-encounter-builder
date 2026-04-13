package encounters;

import java.util.Map;

public class CalculateXPThresholdActivityImpl implements CalculateXPThresholdActivity {

    // D&D 5e Easy difficulty XP thresholds by character level
    private static final Map<Integer, Integer> XP_THRESHOLDS = Map.ofEntries(
        Map.entry(1, 25),
        Map.entry(2, 50),
        Map.entry(3, 75),
        Map.entry(4, 125),
        Map.entry(5, 250),
        Map.entry(6, 300),
        Map.entry(7, 350),
        Map.entry(8, 450),
        Map.entry(9, 550),
        Map.entry(10, 600),
        Map.entry(11, 800),
        Map.entry(12, 1000),
        Map.entry(13, 1100),
        Map.entry(14, 1250),
        Map.entry(15, 1400),
        Map.entry(16, 1600),
        Map.entry(17, 2000),
        Map.entry(18, 2100),
        Map.entry(19, 2400),
        Map.entry(20, 2800)
    );

    @Override
    public int calculateXPThreshold(EncounterOptions options) {
        if (options.numCharacters() <= 0) {
            throw new IllegalArgumentException("Number of characters must be positive");
        }

        if (options.characterLevel() <= 0) {
            throw new IllegalArgumentException("Character level must be positive");
        }

        if (!XP_THRESHOLDS.containsKey(options.characterLevel())) {
            throw new IllegalArgumentException("Character max level is 20");
        }

        int xpPerCharacter = XP_THRESHOLDS.get(options.characterLevel());
        return xpPerCharacter * options.numCharacters();
    }
    
}
