package encounters;

import java.util.List;

public interface CreatureRepository {
    public List<Creature> getCreaturesByXp(int minXp, int maxXp);
}
