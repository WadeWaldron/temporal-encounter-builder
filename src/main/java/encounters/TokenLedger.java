package encounters;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.File;
import java.io.IOException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

public class TokenLedger {
    private static final String DEFAULT_LEDGER_FILE = "data/token_ledger.json";
    private static final int INITIAL_TOKENS = 1000;
    private static final ObjectMapper mapper = new ObjectMapper();

    private final String ledgerFile;

    public TokenLedger() {
        this(DEFAULT_LEDGER_FILE);
    }

    public TokenLedger(String ledgerFile) {
        this.ledgerFile = ledgerFile;
    }

    public record TokenEntry(String id, String type, int amount, long timestamp) {}
    public record Ledger(int balance, List<TokenEntry> entries) {}

    public static class InsufficientTokensException extends RuntimeException {
        public InsufficientTokensException(int balance, int requested) {
            super("Insufficient tokens. Balance: " + balance + ", Requested: " + requested);
        }
    }

    public synchronized String deductTokens(int amount, String transactionId) throws IOException {
        if (amount <= 0) {
            throw new IllegalArgumentException("Amount must be positive");
        }

        Ledger ledger = readLedger();

        boolean alreadyDeducted = ledger.entries.stream()
                .anyMatch(e -> e.id().equals(transactionId) && "DEDUCT".equals(e.type()));
        if (alreadyDeducted) {
            return transactionId;
        }

        if (ledger.balance < amount) {
            throw new InsufficientTokensException(ledger.balance, amount);
        }

        ledger.entries.add(new TokenEntry(transactionId, "DEDUCT", amount, Instant.now().getEpochSecond()));

        Ledger updated = new Ledger(ledger.balance - amount, ledger.entries);
        writeLedger(updated);

        return transactionId;
    }

    public synchronized void refundTokens(String deductionId) throws IOException {
        Ledger ledger = readLedger();

        TokenEntry deduction =
                ledger.entries.stream()
                        .filter(e -> e.id().equals(deductionId) && "DEDUCT".equals(e.type()))
                        .findFirst()
                        .orElseThrow(
                                () ->
                                        new RuntimeException(
                                                "Deduction not found: " + deductionId));

        boolean alreadyRefunded =
                ledger.entries.stream()
                        .anyMatch(
                                e ->
                                        "REFUND".equals(e.type())
                                                && e.id().startsWith(deductionId));
        if (alreadyRefunded) {
            return; // Idempotent - already refunded
        }

        String refundId = deductionId + "_refund";
        ledger.entries.add(
                new TokenEntry(refundId, "REFUND", deduction.amount(), Instant.now().getEpochSecond()));

        Ledger updated = new Ledger(ledger.balance + deduction.amount(), ledger.entries);
        writeLedger(updated);
    }

    public synchronized int getBalance() throws IOException {
        return readLedger().balance;
    }

    private Ledger readLedger() throws IOException {
        File file = new File(ledgerFile);
        if (!file.exists()) {
            return new Ledger(INITIAL_TOKENS, new ArrayList<>());
        }
        return mapper.readValue(file, Ledger.class);
    }

    private void writeLedger(Ledger ledger) throws IOException {
        new File(ledgerFile).getParentFile().mkdirs();
        mapper.writerWithDefaultPrettyPrinter().writeValue(new File(ledgerFile), ledger);
    }
}
