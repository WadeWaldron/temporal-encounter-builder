package encounters;

import io.temporal.activity.ActivityInterface;

@ActivityInterface
public interface CalculateXPThresholdActivity {
    int calculateXPThreshold(EncounterOptions options);
}
