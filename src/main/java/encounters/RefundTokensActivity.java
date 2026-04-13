package encounters;

import io.temporal.activity.ActivityInterface;

@ActivityInterface
public interface RefundTokensActivity {
    void refundTokens(String deductionId);
}
