package encounters;

import io.temporal.activity.Activity;
import io.temporal.failure.ApplicationFailure;

public class DeductTokensActivityImpl implements DeductTokensActivity {
    private final TokenLedger ledger;

    public DeductTokensActivityImpl(TokenLedger ledger) {
        this.ledger = ledger;
    }

    @Override
    public String deductTokens(int amount) {
        String transactionId = Activity.getExecutionContext().getInfo().getActivityId();
        try {
            return ledger.deductTokens(amount, transactionId);
        } catch (TokenLedger.InsufficientTokensException e) {
            // Non-retryable for insufficient tokens
            throw ApplicationFailure.newNonRetryableFailure(
                    e.getMessage(),
                    "InsufficientTokens"
            );
        } catch (IllegalArgumentException e) {
            // Non-retryable for invalid arguments
            throw ApplicationFailure.newNonRetryableFailure(
                    e.getMessage(),
                    "InvalidArgument"
            );
         } catch (Exception exception) {
            // Wrap other exceptions as retryable
            throw ApplicationFailure.newFailure(exception.getMessage(), exception.getClass().getSimpleName());
        }
    }
}
