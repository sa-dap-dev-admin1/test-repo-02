package com.blueoptima.connectors.scr;

import com.blueoptima.connectors.common.message.InfrastructureDetails;
import com.blueoptima.connectors.common.error.BOpException;
import com.blueoptima.discovery.cs.common.AuthenticationType;
import com.blueoptima.clients.github.InstallationAccessTokenClient;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

public class AuthenticationHandler {
    private static final Logger logger = LogManager.getLogger(AuthenticationHandler.class);

    private final InfrastructureDetails infraDetails;

    public AuthenticationHandler(InfrastructureDetails infraDetails) {
        this.infraDetails = infraDetails;
    }

    public boolean authenticate(BOpSCRExtractor extractor) throws BOpException {
        logger.info("Authenticating for user: {} using {}", 
            infraDetails.getInstanLogin(), infraDetails.getAuthenticationType());

        String credential = infraDetails.getDecryptedCredential();
        return extractor.authenticate(infraDetails.getInstanLogin(), credential);
    }

    public boolean authenticatePRRequest(BOpSCRExtractor extractor) throws BOpException {
        logger.info("Authenticating PR request for user: {}", infraDetails.getInstanLogin());
        return extractor.authenticatePRRequest(infraDetails);
    }

    public void cleanupAuthentication(String requestID) {
        if (infraDetails.getAuthenticationType() == AuthenticationType.GITHUB_APPS) {
            InstallationAccessTokenClient.instance().cleanUpTokens(false, requestID);
        }
    }

    public boolean isGithubAppsAuthentication() {
        return infraDetails.getAuthenticationType() == AuthenticationType.GITHUB_APPS;
    }

    public String getAuthenticationType() {
        return infraDetails.getAuthenticationType().toString();
    }

    public String getInstanceLogin() {
        return infraDetails.getInstanLogin();
    }
}