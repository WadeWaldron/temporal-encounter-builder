package encounters;

import io.temporal.activity.ActivityOptions;
import io.temporal.common.RetryOptions;
import io.temporal.workflow.Saga;
import io.temporal.workflow.Workflow;
import java.time.Duration;

import org.slf4j.Logger;

public class EncounterBuilderWorkflowImpl implements EncounterBuilderWorkflow {
    private static final Logger log = Workflow.getLogger(EncounterBuilderWorkflowImpl.class);
    private static final int TOKEN_COST = 50;
    private String status = "NOT STARTED";

    private final DeductTokensActivity deductTokensActivity = Workflow.newActivityStub(
        DeductTokensActivity.class,
        ActivityOptions.newBuilder()
            .setStartToCloseTimeout(Duration.ofSeconds(10))
            .build()
    );

    private final CalculateXPThresholdActivity xpActivity = Workflow.newActivityStub(
        CalculateXPThresholdActivity.class,
        ActivityOptions.newBuilder()
            .setStartToCloseTimeout(Duration.ofSeconds(10))
            .build()
    );

    private final DetermineCreaturesActivity creaturesActivity = Workflow.newActivityStub(
        DetermineCreaturesActivity.class,
        ActivityOptions.newBuilder()
            .setStartToCloseTimeout(Duration.ofSeconds(10))
            .build()
    );

    private final GenerateDescriptionActivity descriptionActivity = Workflow.newActivityStub(
        GenerateDescriptionActivity.class,
        ActivityOptions.newBuilder()
            .setStartToCloseTimeout(Duration.ofSeconds(300))
            .setRetryOptions(
                RetryOptions.newBuilder()
                    .setMaximumAttempts(5)
                    .setInitialInterval(Duration.ofSeconds(10))
                    .setMaximumInterval(Duration.ofSeconds(60))
                    .setBackoffCoefficient(2.0)
                    .build()
            )
            .build()
    );

    private final RefundTokensActivity refundTokensActivity = Workflow.newActivityStub(
        RefundTokensActivity.class,
        ActivityOptions.newBuilder()
            .setStartToCloseTimeout(Duration.ofSeconds(10))
            .build()
    );

    @Override   
    public String getStatus() {
        return status;
    }

    @Override
    public Encounter generateEncounter(EncounterOptions options) {
        Saga saga = new Saga(new Saga.Options.Builder().setParallelCompensation(false).build());
        status = "IN PROGRESS";

        try {
            String deductionId = deductTokensActivity.deductTokens(TOKEN_COST);
            saga.addCompensation(refundTokensActivity::refundTokens, deductionId);
            log.info("Tokens deducted with ID: {}", deductionId);

            status = "CALCULATING XP THRESHOLD";
            var xpThreshold = xpActivity.calculateXPThreshold(options);
            log.info("XP Threshold calculated: {}", xpThreshold);

            status = "DETERMINING CREATURES";
            var creatures = creaturesActivity.determineCreatures(xpThreshold);
            log.info("Creatures determined: {}", creatures);

            status = "GENERATING DESCRIPTION";
            var description = descriptionActivity.generateDescription(creatures);
            log.info("Description generated");

            status = "COMPLETED";
            return new Encounter(xpThreshold, creatures, description);
        } catch (Exception e) {
            log.warn("Activity failed. Applying compensations for the saga.");
            
            status = "COMPENSATING";
            saga.compensate();
            status = "FAILED";

            throw e;
        }
    }
}
