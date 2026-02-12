package com.blueoptima.connectors.scr;

import com.blueoptima.connectors.common.error.BOpException;
import com.blueoptima.connectors.scr.MetricEngine.BOpMetricsGenerator;
import com.blueoptima.connectors.scr.common.BOpSCRData;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class SCRMetricsGenerator {
    private static final Logger logger = LogManager.getLogger(SCRMetricsGenerator.class);
    private final SCRExtractor extractor;
    private final SCRConfiguration configuration;
    private final BOpMetricsGenerator metricGenerator;

    public SCRMetricsGenerator(SCRExtractor extractor, SCRConfiguration configuration) {
        this.extractor = extractor;
        this.configuration = configuration;
        this.metricGenerator = new BOpMetricsGenerator(extractor, configuration.getUpdateDir(), configuration.getRequest());
    }

    public void generateMetrics(BOpSCRData scrData) throws BOpException {
        try {
            logger.info("Starting metrics generation");
            metricGenerator.generateMetrics(scrData, null);
            logger.info("Metrics generation completed successfully");
        } catch (Exception e) {
            logger.error("Error during metrics generation", e);
            throw new BOpException(ExtractionStatus.GENERATE_METRICS, GenericErrorCode.RCE0001, "Metrics generation failed", e);
        }
    }
}