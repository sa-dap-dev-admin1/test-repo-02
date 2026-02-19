package com.blueoptima.connectors.scr.services;

import com.blueoptima.connectors.common.message.RequestDetails;
import com.blueoptima.connectors.common.workflow.ExtractionState;
import com.blueoptima.connectors.common.constants.integrations.Extractor;
import com.blueoptima.connectors.util.IntegratorConfigUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.*;

public class ConfigurationManager {
    private static final Logger logger = LogManager.getLogger(ConfigurationManager.class);

    private static final List<ExtractionState> DEFAULT_WORKFLOW = Arrays.asList(
        ExtractionState.QUEUED,
        ExtractionState.VALIDATE_DETAIL,
        ExtractionState.INIT,
        ExtractionState.CONNECT,
        ExtractionState.AUTHENTICATE,
        ExtractionState.EXTRACT_METADATA,
        ExtractionState.BUILD_REVISION_SET,
        ExtractionState.GENERATE_METRICS,
        ExtractionState.SAVE_DATA_TO_FILE,
        ExtractionState.DISCONNECT,
        ExtractionState.EXTRACTION_DONE
    );

    private static final Map<String, List<ExtractionState>> INFRA_SPECIFIC_WORKFLOWS = new HashMap<>();

    static {
        INFRA_SPECIFIC_WORKFLOWS.put(Extractor.GIT.getType(), Arrays.asList(
            ExtractionState.QUEUED,
            ExtractionState.VALIDATE_DETAIL,
            ExtractionState.INIT,
            ExtractionState.DOWNLOAD_REPOSITORY,
            ExtractionState.AUTHENTICATE,
            ExtractionState.EXTRACT_METADATA,
            ExtractionState.BUILD_REVISION_SET,
            ExtractionState.EXTRACT_PR_DATA,
            ExtractionState.GENERATE_METRICS,
            ExtractionState.SAVE_DATA_TO_FILE,
            ExtractionState.DISCONNECT,
            ExtractionState.EXTRACTION_DONE
        ));

        // Add more infrastructure-specific workflows as needed
    }

    public List<ExtractionState> getWorkflow(RequestDetails request) {
        String infraName = request.getInfraDetails().getInfraName().toLowerCase();
        List<ExtractionState> workflow = INFRA_SPECIFIC_WORKFLOWS.getOrDefault(infraName, DEFAULT_WORKFLOW);

        // Apply any request-specific modifications
        workflow = applyRequestSpecificModifications(workflow, request);

        logger.info("Configured workflow for request {}: {}", request.getRequestID(), workflow);
        return workflow;
    }

    private List<ExtractionState> applyRequestSpecificModifications(List<ExtractionState> baseWorkflow, RequestDetails request) {
        List<ExtractionState> modifiedWorkflow = new ArrayList<>(baseWorkflow);

        // Example: Add EXTRACT_SECRETS_DATA if secrets detection is enabled
        if (isSecretsDetectionEnabled(request)) {
            modifiedWorkflow.add(modifiedWorkflow.indexOf(ExtractionState.GENERATE_METRICS), ExtractionState.EXTRACT_SECRETS_DATA);
        }

        // Example: Add EXTRACT_SVD_ON_FULL_CHECKOUT if vulnerability detection is enabled
        if (isVulnerabilityDetectionEnabled(request)) {
            modifiedWorkflow.add(modifiedWorkflow.indexOf(ExtractionState.GENERATE_METRICS), ExtractionState.EXTRACT_SVD_ON_FULL_CHECKOUT);
        }

        // Add more request-specific modifications as needed

        return modifiedWorkflow;
    }

    private boolean isSecretsDetectionEnabled(RequestDetails request) {
        return IntegratorConfigUtil.isSecretsDetectionEnabled(request);
    }

    private boolean isVulnerabilityDetectionEnabled(RequestDetails request) {
        return IntegratorConfigUtil.isVulnerabilityDetectionEnabled(request);
    }
}