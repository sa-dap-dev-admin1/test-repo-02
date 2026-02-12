package com.blueoptima.connectors.scr;

import com.blueoptima.connectors.common.workflow.ExtractionState;
import java.util.List;

public class ExtractionStateManager {
    private final List<ExtractionState> workflow;

    public ExtractionStateManager(List<ExtractionState> workflow) {
        this.workflow = workflow;
    }

    public List<ExtractionState> getWorkflow() {
        return workflow;
    }

    public void executeState(ExtractionState state, SCR scr) throws Exception {
        // Implementation of state execution logic
    }
}