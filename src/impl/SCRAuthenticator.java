package com.blueoptima.connectors.scr;

import com.blueoptima.connectors.common.error.BOpException;
import com.blueoptima.connectors.common.message.InfrastructureDetails;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class SCRAuthenticator {
    private static final Logger logger = LogManager.getLogger(SCRAuthenticator.class);
    private final SCRConfiguration configuration;
    private final SCRExtractor extractor;

    public SCRAuthenticator(SCRConfiguration configuration, SCRExtractor extractor) {
        this.configuration = configuration;
        this.extractor = extractor;
    }

    public void authenticate() throws BOpException {
        InfrastructureDetails infraDetails = configuration.getRequest().getInfraDetails();
        logger.info("Authenticating for user: {} using {}", infraDetails.getInstanLogin(), infraDetails.getAuthenticationType());

        String credential = infraDetails.getDecryptedCredential();
        boolean authenticated = extractor.authenticate(infraDetails.getInstanLogin(), credential);

        if (!authenticated) {
            logger.error("Could not log on to the server");
            throw new BOpException(ExtractionStatus.AUTHENTICATE, GenericErrorCode.RCE0001, "Authentication failed");
        }
    }
}