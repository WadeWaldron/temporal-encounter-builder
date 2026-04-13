package encounters;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

public class TokenLedgerTest {
    @TempDir
    Path tempDir;

    private TokenLedger ledger;
    private String ledgerFile;

    @BeforeEach
    public void setup() {
        ledgerFile = tempDir.resolve("token_ledger.json").toString();
        ledger = new TokenLedger(ledgerFile);
    }

    @Test
    public void initialBalance_shouldBe1000() throws Exception {
        assertEquals(1000, ledger.getBalance());
    }

    @Test
    public void deductTokens_shouldReduceBalance() throws Exception {
        String deductionId = ledger.deductTokens(100, "tx-1");

        assertNotNull(deductionId);
        assertEquals(900, ledger.getBalance());
    }

    @Test
    public void deductTokens_withInsufficientBalance_shouldThrow() throws Exception {
        ledger.deductTokens(800, "tx-1");

        assertThrows(RuntimeException.class, () -> ledger.deductTokens(300, "tx-2"));
    }

    @Test
    public void deductTokens_isIdempotent() throws Exception {
        ledger.deductTokens(100, "tx-1");
        ledger.deductTokens(100, "tx-1"); // same transaction ID — should not deduct twice

        assertEquals(900, ledger.getBalance());
    }

    @Test
    public void refundTokens_shouldRestoreBalance() throws Exception {
        String deductionId = ledger.deductTokens(100, "tx-1");
        assertEquals(900, ledger.getBalance());

        ledger.refundTokens(deductionId);
        assertEquals(1000, ledger.getBalance());
    }

    @Test
    public void refundTokens_isIdempotent() throws Exception {
        String deductionId = ledger.deductTokens(100, "tx-1");
        ledger.refundTokens(deductionId);
        ledger.refundTokens(deductionId); // Call twice

        assertEquals(1000, ledger.getBalance());
    }

    @Test
    public void multipleDeductions_andRefunds() throws Exception {
        String id1 = ledger.deductTokens(100, "tx-1");
        String id2 = ledger.deductTokens(200, "tx-2");
        assertEquals(700, ledger.getBalance());

        ledger.refundTokens(id1);
        assertEquals(800, ledger.getBalance());

        ledger.refundTokens(id2);
        assertEquals(1000, ledger.getBalance());
    }

    @Test
    public void refundNonexistentDeduction_shouldThrow() throws Exception {
        assertThrows(RuntimeException.class, () -> ledger.refundTokens("fake-id"));
    }
}
