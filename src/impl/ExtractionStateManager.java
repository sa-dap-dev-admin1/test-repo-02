package com.blueoptima.connectors.scr.managers;

import com.blueoptima.connectors.common.workflow.ExtractionState;
import java.util.List;

public class ExtractionStateManager {
    private final List<ExtractionState> workflow;
    private int currentStateIndex;

    public ExtractionStateManager(List<ExtractionState> workflow) {
        this.workflow = workflow;
        this.currentStateIndex = 0;
    }

    public boolean hasNextState() {
        return currentStateIndex < workflow.size();
    }

    public String getCurrentState() {
        return workflow.get(currentStateIndex).getValue().name();
    }

    public void moveToNextState() {
        if (hasNextState()) {
            currentStateIndex++;
        }
    }
}