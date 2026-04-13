package encounters;

import io.temporal.testing.TestActivityEnvironment;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

public class DeductTokensActivityImplTest {
    @TempDir
    Path tempDir;

    private TestActivityEnvironment testEnv;
    private DeductTokensActivity activity;
    private TokenLedger ledger;

    @BeforeEach
    public void setup() {
        String ledgerFile = tempDir.resolve("token_ledger.json").toString();
        ledger = new TokenLedger(ledgerFile);
        testEnv = TestActivityEnvironment.newInstance();
        testEnv.registerActivitiesImplementations(new DeductTokensActivityImpl(ledger));
        activity = testEnv.newActivityStub(DeductTokensActivity.class);
    }

    @AfterEach
    public void tearDown() {
        testEnv.close();
    }

    @Test
    public void deductTokens_shouldReturnDeductionId() {
        String deductionId = activity.deductTokens(100);

        assertNotNull(deductionId);
        assertFalse(deductionId.isEmpty());
    }

    @Test
    public void deductTokens_shouldReduceBalance() throws Exception {
        activity.deductTokens(100);

        assertEquals(900, ledger.getBalance());
    }

    @Test
    public void deductTokens_withInsufficientBalance_shouldThrow() {
        activity.deductTokens(800);

        assertThrows(RuntimeException.class, () -> activity.deductTokens(300));
    }

    @Test
    public void multipleDeductions_shouldResultInTheExpectedBalance() throws Exception {
        String id1 = activity.deductTokens(100);
        String id2 = activity.deductTokens(200);

        assertNotNull(id1);
        assertNotNull(id2);
        assertNotEquals(id1, id2);
        assertEquals(700, ledger.getBalance());
    }
}
