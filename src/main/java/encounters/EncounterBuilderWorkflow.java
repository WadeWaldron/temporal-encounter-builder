package encounters;

import io.temporal.workflow.QueryMethod;
import io.temporal.workflow.WorkflowInterface;
import io.temporal.workflow.WorkflowMethod;

@WorkflowInterface
public interface EncounterBuilderWorkflow {
    @WorkflowMethod
    Encounter generateEncounter(EncounterOptions options);

    @QueryMethod
    String getStatus();
}