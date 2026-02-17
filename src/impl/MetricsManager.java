package com.blueoptima.connectors.scr.managers;

import com.blueoptima.connectors.common.message.RequestDetails;
import com.blueoptima.connectors.scr.MetricEngine.BOpMetricsGenerator;
import com.blueoptima.connectors.scr.common.BOpSCRData;
import com.blueoptima.connectors.common.error.BOpException;

public class MetricsManager {
    private BOpMetricsGenerator metricsGenerator;
    private BOpSCRData scrData;

    public MetricsManager() {
        this.metricsGenerator = new BOpMetricsGenerator();
    }

    public void setScrData(BOpSCRData scrData) {
        this.scrData = scrData;
    }

    public void generateMetrics(RequestDetails request) throws BOpException {
        if (scrData == null) {
            throw new BOpException("SCR data not set");
        }

        try {
            metricsGenerator.generateMetrics(scrData, null); // Assuming null for FullCheckoutConfig
        } catch (Exception e) {
            throw new BOpException("Failed to generate metrics", e);
        }
    }

    public BOpSCRData getScrData() {
        return scrData;
    }
}