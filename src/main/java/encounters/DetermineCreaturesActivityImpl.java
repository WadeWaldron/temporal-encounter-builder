package encounters;

import java.util.Collections;
import java.util.List;
import java.util.Random;

// TODO: Revisit this implementation.
// It seems like there's an opportunity for recursion or something to make it easier.
public class DetermineCreaturesActivityImpl implements DetermineCreaturesActivity {

    private final CreatureRepository creatureRepository;
    private final Random rnd = new Random(System.currentTimeMillis());

    public DetermineCreaturesActivityImpl(CreatureRepository creatureRepository) {
        this.creatureRepository = creatureRepository;
    }


    private List<Creature> getCreaturesByXp(int minXp, int maxXp) {
        List<Creature> creatures = creatureRepository.getCreaturesByXp(minXp, maxXp);

        if(creatures.isEmpty()) {
            return getCreaturesByXp((int) (minXp * 0.8), maxXp);
        } else {
            return creatures;
        }
    }

    @Override
    public List<Creature> determineCreatures(Integer xpThreshold) {
        int creatureCount = rnd.nextInt(1, 5);
        int perCreatureXp = xpThreshold / creatureCount;

        List<Creature> potentialCreatures = getCreaturesByXp(perCreatureXp, perCreatureXp);

        int uniqueTypeCount = rnd.nextInt(1, Math.min(creatureCount + 1, potentialCreatures.size() + 1));

        List<Creature> shuffledCreatures = shuffled(potentialCreatures);
        List<Creature> selectedTypes = shuffledCreatures.stream().limit(uniqueTypeCount).toList();

        List<Creature> encounter = new java.util.ArrayList<>();
    
        int totalXp = 0;
        int totalCreatures = 0;

        while(totalXp < xpThreshold) {
            Creature creature = selectedTypes.get(totalCreatures % uniqueTypeCount);
            if(totalXp + creature.xp() <= xpThreshold) {
                encounter.add(creature);
                totalCreatures ++;
                totalXp += creature.xp();
            } else 
                break;
        }

        return encounter;
    }

    /**
     * Returns a shuffled copy of the provided list.
     * The original list is not modified.
     */
    private List<Creature> shuffled(List<Creature> creatures) {
        var mutableList = new java.util.ArrayList<>(creatures);
        Collections.shuffle(mutableList);
        return mutableList;
    }
}
