package com.blueoptima.connectors.scr;

import com.blueoptima.connectors.common.message.RequestDetails;
import com.blueoptima.connectors.scr.MetricEngine.BOpMetricsGenerator;
import com.blueoptima.connectors.scr.common.BOpSCRData;
import org.apache.logging.log4j.Logger;

public class MetricsGenerationHandler {
    private final BOpMetricsGenerator metricGenerator;
    private final Logger extractionLog;

    public MetricsGenerationHandler(BOpSCRExtractor extractor, String currentUpdateDir, Logger extractionLog, RequestDetails request) {
        this.metricGenerator = new BOpMetricsGenerator(extractor, currentUpdateDir, extractionLog, request);
        this.extractionLog = extractionLog;
    }

    public void generateMetrics(BOpSCRData scrData, FullCheckoutConfig fullCheckoutConfig) throws Exception {
        metricGenerator.generateMetrics(scrData, fullCheckoutConfig);
    }
}