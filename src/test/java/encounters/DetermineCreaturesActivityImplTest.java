package encounters;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class DetermineCreaturesActivityImplTest {

    private DetermineCreaturesActivityImpl activity;
    private StubCreatureRepository stubRepository;

    @BeforeEach
    public void setup() {
        stubRepository = new StubCreatureRepository();
        activity = new DetermineCreaturesActivityImpl(stubRepository);
    }

    @Test
    public void determineCreatures_respectsXpThreshold() {
        // Setup a variety of creatures at different XP levels
        stubRepository.insert(new CreatureBuilder().withXp(50).build());
        stubRepository.insert(new CreatureBuilder().withXp(100).build());
        stubRepository.insert(new CreatureBuilder().withXp(200).build());
        stubRepository.insert(new CreatureBuilder().withXp(300).build());
        stubRepository.insert(new CreatureBuilder().withXp(500).build());
        stubRepository.insert(new CreatureBuilder().withXp(1000).build());

        int threshold = 1000;
        List<Creature> creatures = activity.determineCreatures(threshold);

        // Core guarantee: total XP never exceeds threshold
        int totalXp = creatures.stream().mapToInt(Creature::xp).sum();
        assertTrue(totalXp <= threshold,
            "Encounter total XP must not exceed threshold. Got " + totalXp + " > " + threshold);
        
        // Core guarantee: always returns an encounter
        assertTrue(creatures.size() > 0, "Should always return at least one creature");
    }

    @Test
    public void determineCreatures_alwaysFindsEncounter() {
        // Even with sparse/challenging XP distribution, should find creatures by widening scope
        stubRepository.insert(new CreatureBuilder().withXp(50).build());
        stubRepository.insert(new CreatureBuilder().withXp(5000).build());
        stubRepository.insert(new CreatureBuilder().withXp(10000).build());

        int threshold = 500;
        List<Creature> creatures = activity.determineCreatures(threshold);

        // Core guarantee: always returns an encounter
        assertTrue(creatures.size() > 0, "Should always build an encounter of some kind");
        
        // Core guarantee: respects budget
        int totalXp = creatures.stream().mapToInt(Creature::xp).sum();
        assertTrue(totalXp <= threshold,
            "Encounter XP " + totalXp + " must not exceed threshold " + threshold);
    }

    /**
     * Stub repository for testing that allows tests to insert exactly what they need.
     * Follows arrange-act-assert pattern by letting each test control the data.
     */
    static class StubCreatureRepository implements CreatureRepository {
        private final java.util.List<Creature> creatures = new java.util.ArrayList<>();

        void insert(Creature creature) {
            creatures.add(creature);
        }

        void insert(java.util.List<Creature> creatureslist) {
            creatures.addAll(creatureslist);
        }

        @Override
        public List<Creature> getCreaturesByXp(int minXp, int maxXp) {
            return creatures.stream()
                .filter(c -> c.xp() >= minXp && c.xp() <= maxXp)
                .sorted((a, b) -> {
                    int xpCompare = Integer.compare(b.xp(), a.xp());
                    return xpCompare != 0 ? xpCompare : a.name().compareTo(b.name());
                })
                .toList();
        }
    }
}
