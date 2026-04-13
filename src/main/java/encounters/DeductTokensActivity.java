package encounters;

import io.temporal.activity.ActivityInterface;

@ActivityInterface
public interface DeductTokensActivity {
    String deductTokens(int amount);
}
