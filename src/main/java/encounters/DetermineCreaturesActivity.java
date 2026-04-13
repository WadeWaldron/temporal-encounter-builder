package encounters;

import io.temporal.activity.ActivityInterface;
import java.util.List;

@ActivityInterface
public interface DetermineCreaturesActivity {
    public List<Creature> determineCreatures(Integer xpThreshold);
}
