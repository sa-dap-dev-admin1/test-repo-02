package com.blueoptima.connectors.scr.services;

import com.blueoptima.connectors.scr.MetricEngine.BOpMetricsGenerator;
import com.blueoptima.connectors.scr.common.BOpSCRData;

public class MetricsGenerationService {
    private final BOpMetricsGenerator metricsGenerator;

    public MetricsGenerationService() {
        this.metricsGenerator = new BOpMetricsGenerator();
    }

    public void generateMetrics(BOpSCRData scrData) {
        metricsGenerator.generateMetrics(scrData);
    }

    public BOpMetricsGenerator getMetricsGenerator() {
        return metricsGenerator;
    }
}