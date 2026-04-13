package encounters;

import io.temporal.client.WorkflowClient;
import io.temporal.testing.TestWorkflowEnvironment;
import io.temporal.worker.Worker;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class EncounterBuilderWorkflowImplTest {
    @TempDir
    Path tempDir;

    class MockLLM implements LLM {
        private String prompt;
        private String response = "";
        private boolean shouldThrow = false;

        @Override
        public String executePrompt(String prompt) {
            this.prompt = prompt;
            if (shouldThrow) {
                throw new RuntimeException("LLM service unavailable");
            }
            return response;
        }

        public String getPrompt() {
            return prompt;
        }

        public void setResponse(String response) {
            this.response = response;
        }

        public void setShouldThrow(boolean shouldThrow) {
            this.shouldThrow = shouldThrow;
        }
    }

    private TestWorkflowEnvironment testEnv;
    private EncounterBuilderWorkflow workflow;
    private TokenLedger tokenLedger;
    private MockLLM mockLLM;
    private static final String EXPECTED_LLM_RESPONSE = "The gnomes ambush from the underbrush, chittering excitedly.";

    @BeforeEach
    public void setup() {
        testEnv = TestWorkflowEnvironment.newInstance();
        WorkflowClient client = testEnv.getWorkflowClient();
        Worker worker = testEnv.newWorker("ENCOUNTERS_TASK_QUEUE");

        String ledgerFile = tempDir.resolve("token_ledger.json").toString();
        tokenLedger = new TokenLedger(ledgerFile);

        mockLLM = new MockLLM();
        mockLLM.setResponse(EXPECTED_LLM_RESPONSE);

        worker.registerWorkflowImplementationTypes(EncounterBuilderWorkflowImpl.class);

        worker.registerActivitiesImplementations(
            new DeductTokensActivityImpl(tokenLedger),
            new CalculateXPThresholdActivityImpl(),
            new DetermineCreaturesActivityImpl(new FileBasedCreatureRepository()),
            new GenerateDescriptionActivityImpl(mockLLM),
            new RefundTokensActivityImpl(tokenLedger)
        );

        testEnv.start();

        workflow = client.newWorkflowStub(
            EncounterBuilderWorkflow.class,
            io.temporal.client.WorkflowOptions.newBuilder()
                .setTaskQueue("ENCOUNTERS_TASK_QUEUE")
                .build()
        );
    }

    @AfterEach
    public void teardown() {
        testEnv.close();
    }

    @Test
    public void generateEncounter_shouldSucceedAndDeductTokens() throws Exception {
        int initialBalance = tokenLedger.getBalance();
        
        Encounter result = workflow.generateEncounter(new EncounterOptions(4, 5));

        assertNotNull(result);
        assertFalse(result.creatures().isEmpty());
        assertEquals(1000, result.xpThreshold()); // 4 characters * 250 (level 5 threshold)
        assertEquals(EXPECTED_LLM_RESPONSE, result.description());
        
        assertEquals(initialBalance - 50, tokenLedger.getBalance());
    }

    @Test
    public void generateEncounter_shouldReturnValidStructure() throws Exception {
        Encounter result = workflow.generateEncounter(new EncounterOptions(3, 3));

        assertNotNull(result);
        assertTrue(result.creatures().size() > 0);
        assertEquals(225, result.xpThreshold()); // 3 characters * 75 (level 3 threshold)
        assertEquals(EXPECTED_LLM_RESPONSE, result.description());
    }

    @Test
    public void generateEncounter_shouldRefundTokensOnFailure() throws Exception {
        int initialBalance = tokenLedger.getBalance();
        mockLLM.setShouldThrow(true);
        
        Exception exception = assertThrows(Exception.class, 
            () -> workflow.generateEncounter(new EncounterOptions(2, 2)));
        
        assertEquals(initialBalance, tokenLedger.getBalance(), 
            "Tokens should be refunded when workflow fails");
    }
}
