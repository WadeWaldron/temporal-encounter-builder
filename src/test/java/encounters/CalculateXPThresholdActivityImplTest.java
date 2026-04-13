package encounters;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class CalculateXPThresholdActivityImplTest {

    CalculateXPThresholdActivityImpl activity;

    @BeforeEach
    public void setUp() {
        activity = new CalculateXPThresholdActivityImpl();
    }

    @Test
    public void calculateXPThreshold_shouldThrowAnException_forLessThanOneCharacter() {
        EncounterOptions options = new EncounterOptions(
            0, 
            1
        );

        assertThrows(IllegalArgumentException.class, () -> {
            activity.calculateXPThreshold(options);
        });
    }

    @Test
    public void calculateXPThreshold_shouldThrowAnException_forZeroLevelCharacters() {
        EncounterOptions options = new EncounterOptions(
            1, 
            0
        );

        assertThrows(IllegalArgumentException.class, () -> {
            activity.calculateXPThreshold(options);
        });
    }

    @Test
    public void calculateXPThreshold_shouldThrowAnException_forCharactersAboveMaxLevel() {
        EncounterOptions options = new EncounterOptions(
            1, 
            21
        );

        assertThrows(IllegalArgumentException.class, () -> {
            activity.calculateXPThreshold(options);
        });
    }

    @Test
    public void calculateXPThreshold_shouldReturnTheCorrectValues_forEveryCharacterLevel() {

        assertEquals(25, activity.calculateXPThreshold(
            new EncounterOptions(1, 1)
        ));

        assertEquals(50, activity.calculateXPThreshold(
            new EncounterOptions(1, 2)
        ));

        assertEquals(75, activity.calculateXPThreshold(
            new EncounterOptions(1, 3)
        ));

        assertEquals(125, activity.calculateXPThreshold(
            new EncounterOptions(1, 4)
        ));

        assertEquals(250, activity.calculateXPThreshold(
            new EncounterOptions(1, 5)
        ));

        assertEquals(300, activity.calculateXPThreshold(
            new EncounterOptions(1, 6)
        ));

        assertEquals(350, activity.calculateXPThreshold(
            new EncounterOptions(1, 7)
        ));

        assertEquals(450, activity.calculateXPThreshold(
            new EncounterOptions(1, 8)
        ));

        assertEquals(550, activity.calculateXPThreshold(
            new EncounterOptions(1, 9)
        ));

        assertEquals(600, activity.calculateXPThreshold(
            new EncounterOptions(1, 10)
        ));

        assertEquals(800, activity.calculateXPThreshold(
            new EncounterOptions(1, 11)
        ));

        assertEquals(1000, activity.calculateXPThreshold(
            new EncounterOptions(1, 12)
        ));

        assertEquals(1100, activity.calculateXPThreshold(
            new EncounterOptions(1, 13)
        ));

        assertEquals(1250, activity.calculateXPThreshold(
            new EncounterOptions(1, 14)
        ));

        assertEquals(1400, activity.calculateXPThreshold(
            new EncounterOptions(1, 15)
        ));

        assertEquals(1600, activity.calculateXPThreshold(
            new EncounterOptions(1, 16)
        ));

        assertEquals(2000, activity.calculateXPThreshold(
            new EncounterOptions(1, 17)
        ));

        assertEquals(2100, activity.calculateXPThreshold(
            new EncounterOptions(1, 18)
        ));

        assertEquals(2400, activity.calculateXPThreshold(
            new EncounterOptions(1, 19)
        ));

        assertEquals(2800, activity.calculateXPThreshold(
            new EncounterOptions(1, 20)
        ));
    }

    @Test
    public void calculateXPThreshold_shouldSumTheXPThresholds_forMultipleCharacters() {
        EncounterOptions options = new EncounterOptions(
            4,
            1
        );

        var result = activity.calculateXPThreshold(options);

        assertEquals(100, result);
    }

    @Test
    public void calculateXPThreshold_shouldReturnTheCorrectSum_forAComplexEncounter() {
        EncounterOptions options = new EncounterOptions(
            3,
            5
        );

        var result = activity.calculateXPThreshold(options);

        assertEquals(750, result);
    }
}
