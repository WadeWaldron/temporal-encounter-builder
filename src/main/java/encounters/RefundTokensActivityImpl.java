package encounters;

public class RefundTokensActivityImpl implements RefundTokensActivity {
    private final TokenLedger ledger;

    public RefundTokensActivityImpl(TokenLedger ledger) {
        this.ledger = ledger;
    }

    @Override
    public void refundTokens(String deductionId) {
        try {
            ledger.refundTokens(deductionId);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
