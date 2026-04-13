package encounters;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

public class FileBasedCreatureRepository implements CreatureRepository {

    private final List<Creature> creatures;
    private static final String CREATURES_FILE = "/monsters.json";

    static record CreaturesWrapper(List<Creature> creatures) {}

    public FileBasedCreatureRepository() {
        this.creatures = loadCreaturesFromFile();
    }

    @Override
    public List<Creature> getCreaturesByXp(int minXp, int maxXp) {
        if (minXp > maxXp) {
            throw new IllegalArgumentException("Minimum XP cannot be greater than maximum XP");
        }

        return creatures.stream()
            .filter(creature -> creature.xp() >= minXp && creature.xp() <= maxXp)
            .sorted((a, b) -> {
                int xpCompare = Integer.compare(b.xp(), a.xp());

                if(xpCompare == 0)
                    return a.name().compareTo(b.name());
                else
                    return xpCompare;
            })
            .toList();
    }

    private List<Creature> loadCreaturesFromFile() {
        ObjectMapper mapper = new ObjectMapper();

        try (InputStream inputStream = getClass().getResourceAsStream(CREATURES_FILE)) {
            if (inputStream == null) {
                throw new IOException("Resource not found: " + CREATURES_FILE);
            }

            CreaturesWrapper wrapper = mapper.readValue(inputStream, CreaturesWrapper.class);
            return wrapper.creatures;

        } catch (IOException e) {
            throw new RuntimeException("Failed to load creatures from file: " + CREATURES_FILE, e);
        }
    }
}
