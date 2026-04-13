package encounters;

import java.util.UUID;

import io.temporal.client.WorkflowClient;
import io.temporal.client.WorkflowOptions;
import io.temporal.serviceclient.WorkflowServiceStubs;
import io.temporal.worker.Worker;
import io.temporal.worker.WorkerFactory;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

@Command(
    name = "encounter-builder",
    description = "D&D Encounter Builder using Temporal",
    version = "1.0-SNAPSHOT",
    mixinStandardHelpOptions = true
)
public class Main implements Runnable {

    private static final String TASK_QUEUE = "ENCOUNTERS_TASK_QUEUE";

    @Option(
        names = {"--characters", "-c"},
        description = "Number of player characters (positive integer)",
        required = true
    )
    private int characterCount;

    @Option(
        names = {"--level", "-l"},
        description = "Level of the characters (positive integer)",
        required = true
    )
    private int characterLevel;

    public static void main(String[] args) {
        int exitCode = new CommandLine(new Main()).execute(args);
        System.exit(exitCode);
    }

    @Override
    public void run() {
        if (characterCount <= 0 || characterLevel <= 0) {
            System.err.println("Error: Character count and level must be positive integers.");
            System.exit(1);
        }

        CreatureRepository creatureRepository = new FileBasedCreatureRepository();
        LLM llm = new Ollama();
        TokenLedger tokenLedger = new TokenLedger();

        // Initialize Temporal
        WorkflowServiceStubs service = WorkflowServiceStubs.newLocalServiceStubs();
        WorkflowClient client = WorkflowClient.newInstance(service);

        // Start worker in a background thread
        WorkerFactory factory = WorkerFactory.newInstance(client);
        Worker worker = factory.newWorker(TASK_QUEUE);
        worker.registerWorkflowImplementationTypes(EncounterBuilderWorkflowImpl.class);
        worker.registerActivitiesImplementations(
            new DeductTokensActivityImpl(tokenLedger),
            new CalculateXPThresholdActivityImpl(),
            new DetermineCreaturesActivityImpl(creatureRepository),
            new GenerateDescriptionActivityImpl(llm),
            new RefundTokensActivityImpl(tokenLedger)
        );
        factory.start();

        try {
            EncounterBuilderWorkflow encounterWorkflow = client.newWorkflowStub(
                EncounterBuilderWorkflow.class,
                WorkflowOptions.newBuilder()
                    .setTaskQueue(TASK_QUEUE)
                    .setWorkflowId("encounter-builder-" + UUID.randomUUID())

                    .build()
            );

            var result = encounterWorkflow.generateEncounter(
                new EncounterOptions(characterCount, characterLevel)
            );

            System.out.println(result);
            
        } catch (Exception e) {
            e.printStackTrace();  
            System.out.println("**************************************");
            System.out.println("Workflow failed with exception:");
            System.out.println(getExceptionMessage(e));
            System.out.println("**************************************");
        } finally {
            service.shutdown();
        }
    }

    private String getExceptionMessage(Throwable e) {
        String message = "- " +e.getMessage();

        if(e.getCause() != null) {
            message += "\nCaused by:\n";
            message += getExceptionMessage(e.getCause());
        }
        return message; 
    }

    private void printExceptionDetails(Throwable e) {
        System.out.println(e.getMessage());

        if(e.getCause() != null) {
            System.out.println("Caused by:");
                printExceptionDetails(e.getCause());
        }
    }
}

