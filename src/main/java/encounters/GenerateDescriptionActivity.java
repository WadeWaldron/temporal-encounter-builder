package encounters;

import java.util.List;

import io.temporal.activity.ActivityInterface;

@ActivityInterface
public interface GenerateDescriptionActivity {
    public String generateDescription(List<Creature> creatures);
}
