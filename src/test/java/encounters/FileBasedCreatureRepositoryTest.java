package encounters;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

public class FileBasedCreatureRepositoryTest {

    private static FileBasedCreatureRepository repository;

    @BeforeAll
    public static void setupAll() {
        repository = new FileBasedCreatureRepository();
    }

    @Test
    public void getCreaturesByXp_shouldThrowAnExceptionWhenRangeIsInvalid() {
        try {
            repository.getCreaturesByXp(100, 50);
        } catch (IllegalArgumentException e) {
            assertArrayEquals(new String[]{
                "Minimum XP cannot be greater than maximum XP"
            }, new String[]{e.getMessage()}, "Expected an exception for invalid XP range");
        }
    } 

    @Test
    public void getCreaturesByXp_shouldReturnAnEmptyListWhenNoCreaturesInRange() {
        var creatures = repository.getCreaturesByXp(10, 20);

        assertArrayEquals(new Creature[]{}, creatures.toArray(), "Expected no creatures in this XP range");
    }

    @Test
    public void getCreaturesByXp_shouldReturnOnlyOneCreatureWhenOnlyOneInRange() {
        var creatures = repository.getCreaturesByXp(33000, 33000);

        assertArrayEquals(new Creature[]{
            new Creature("Lich", 21, 33000, "Undead", "Medium", "Powerful wizard who achieved immortality through dark magic and the use of a phylactery.")
        }, creatures.toArray(), "Expected only the Lich in this XP range");
    }

    @Test
    public void getCreaturesByXp_shouldReturnMultipleCreaturesWhenMultipleInRange() {
        var creatures = repository.getCreaturesByXp(7200, 8400);

        assertArrayEquals(new Creature[]{
            new Creature("Erinyes", 12, 8400, "Fiend", "Medium", "Infernal fury disguised as winged woman. Hunts targets relentlessly for revenge."),
            new Creature("Marid", 11, 7200, "Elemental", "Large", "Genie of water wielding elemental power. Proud, temperamental, and noble."),
            new Creature("Remorhaz", 11, 7200, "Monstrosity", "Gargantuan", "Massive worm tunneling through frozen wastes. Hibernates for decades between hunts.")
        }, creatures.toArray(), "Expected creatures sorted by XP descending, then by name ascending");
    }

    @Test
    public void getCreaturesByXp_shouldReturnAllCreaturesWhenRangeIsMax() {
        var creatures = repository.getCreaturesByXp(0, Integer.MAX_VALUE);
        
        assertEquals(100, creatures.size(), "Expected exactly 100 creatures to be loaded from file");
    }

    @Test
    public void getCreaturesByXp_shouldFilterCreaturesByXpRange() {
        var creatures = repository.getCreaturesByXp(100, 300);
        
        assertTrue(creatures.size() > 1, "Expected multiple creatures in the 100-300 XP range");
        
        for (Creature creature : creatures) {
            assertTrue(creature.xp() >= 100 && creature.xp() <= 300, 
                "Creature " + creature.name() + " has XP " + creature.xp() + " outside expected range");
        }
    }

    @Test
    public void getCreaturesByXp_shouldContainIconicCreatures() {
        var creatures = repository.getCreaturesByXp(0, Integer.MAX_VALUE);
        
        assertTrue(creatures.stream().anyMatch(c -> c.name().equals("Beholder")), 
            "Expected to find Beholder in creatures");
        assertTrue(creatures.stream().anyMatch(c -> c.name().equals("Lich")), 
            "Expected to find Lich in creatures");
        assertTrue(creatures.stream().anyMatch(c -> c.name().equals("Adult Red Dragon")), 
            "Expected to find Adult Red Dragon in creatures");
    }
}
