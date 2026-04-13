package encounters;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

public class RefundTokensActivityImplTest {
    
    @TempDir
    Path tempDir;

    private RefundTokensActivityImpl activity;
    private TokenLedger ledger;

    @BeforeEach
    public void setup() {
        String ledgerFile = tempDir.resolve("token_ledger.json").toString();
        ledger = new TokenLedger(ledgerFile);
        activity = new RefundTokensActivityImpl(ledger);
    }

    @Test
    public void refundTokens_shouldRestoreBalance() throws Exception {
        ledger.deductTokens(100, "tx-1");
        assertEquals(900, ledger.getBalance());

        activity.refundTokens("tx-1");
        assertEquals(1000, ledger.getBalance());
    }

    @Test
    public void refundTokens_isIdempotent() throws Exception {
        ledger.deductTokens(100, "tx-1");
        activity.refundTokens("tx-1");
        activity.refundTokens("tx-1"); // Call twice

        assertEquals(1000, ledger.getBalance());
    }

    @Test
    public void multipleDeductionsAndRefunds() throws Exception {
        ledger.deductTokens(100, "tx-1");
        ledger.deductTokens(200, "tx-2");
        assertEquals(700, ledger.getBalance());

        activity.refundTokens("tx-1");
        assertEquals(800, ledger.getBalance());

        activity.refundTokens("tx-2");
        assertEquals(1000, ledger.getBalance());
    }

    @Test
    public void refundNonexistentDeduction_shouldThrow() {
        assertThrows(RuntimeException.class, () -> activity.refundTokens("fake-id"));
    }
}
