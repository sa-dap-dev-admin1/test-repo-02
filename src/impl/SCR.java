package com.blueoptima.connectors.scr;

import com.blueoptima.connectors.BOpInfraExtractor;
import com.blueoptima.connectors.common.Housekeeper;
import com.blueoptima.connectors.common.message.RequestDetails;
import com.blueoptima.connectors.common.workflow.ExtractionState;
import com.blueoptima.connectors.common.error.BOpException;
import com.blueoptima.connectors.common.message.BOpExtractionData;
import com.blueoptima.connectors.common.constants.ExtractionStatus;
import com.blueoptima.telemetry.annotation.MarkTimer;
import com.blueoptima.telemetry.TelemetryNames;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;

public class SCR extends BOpInfraExtractor {
    private static final Logger logger = LogManager.getLogger(SCR.class);

    private final SCRConfiguration configuration;
    private final SCRAuthenticator authenticator;
    private final SCRExtractor extractor;
    private final SCRMetricsGenerator metricsGenerator;
    private final SCRDataProcessor dataProcessor;
    private final SCRFileOperations fileOperations;
    private final SCRExceptionHandler exceptionHandler;

    public SCR(RequestDetails request, Housekeeper housekeeper, List<ExtractionState> workflow) throws Exception {
        super(request, housekeeper, workflow);
        this.configuration = new SCRConfiguration(request);
        this.authenticator = new SCRAuthenticator(configuration);
        this.extractor = SCRExtractorFactory.create(configuration);
        this.metricsGenerator = new SCRMetricsGenerator(extractor, configuration);
        this.dataProcessor = new SCRDataProcessor(configuration);
        this.fileOperations = new SCRFileOperations(configuration);
        this.exceptionHandler = new SCRExceptionHandler();
    }

    @Override
    public void init(String dir) throws IllegalAccessException, BOpException {
        configuration.initDirectories(dir);
        setExtractionLog(configuration.getLogFilePath(), configuration.getLogLevel());
        logger.info("Integrator Version : {}", configuration.getIntegratorVersion());
        logger.info("LogDir : {}", configuration.getLogsDir());
        logger.info("Update Dir : {}", configuration.getUpdateDir());
    }

    @Override
    public boolean validateRequest() throws BOpException {
        return configuration.validateRequest();
    }

    @MarkTimer(key = TelemetryNames.EXTRACTOR_KEY, type = TelemetryNames.EXTRACT_TYPE, tag = TelemetryNames.SCR)
    public BOpExtractionData extract() throws Exception {
        try {
            for (ExtractionState state : workflow) {
                status = state.getValue();
                ExtractionStatus currentStatus = getStatus();

                switch (currentStatus) {
                    case QUEUED:
                        break;
                    case VALIDATE_DETAIL:
                        if (!validateRequest()) {
                            return null;
                        }
                        break;
                    case INIT:
                        extractor.init();
                        break;
                    case CONNECT:
                        extractor.connect();
                        break;
                    case AUTHENTICATE:
                        authenticator.authenticate();
                        break;
                    case EXTRACT_METADATA:
                        dataProcessor.extractMetadata();
                        break;
                    case GENERATE_METRICS:
                        metricsGenerator.generateMetrics();
                        break;
                    case SAVE_DATA_TO_FILE:
                        fileOperations.saveDataToFile();
                        break;
                    case DISCONNECT:
                        disconnect();
                        break;
                    case EXTRACTION_DONE:
                        logger.info("Extraction was successful :-)");
                        return createExtractionData();
                    default:
                        logger.warn("Unhandled extraction state: {}", currentStatus);
                }
            }
        } catch (BOpException e) {
            return exceptionHandler.handleException(e, this);
        }

        return null;
    }

    @Override
    public void disconnect() {
        extractor.disconnect();
        logger.info("Disconnecting.....");
    }

    private BOpExtractionData createExtractionData() {
        BOpExtractionData bOpExtractionData = new BOpExtractionData();
        bOpExtractionData.setRequestID(requestID);
        bOpExtractionData.setFileName(configuration.getExtractionDataFile());
        return bOpExtractionData;
    }
}