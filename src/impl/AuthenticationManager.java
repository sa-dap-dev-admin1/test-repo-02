package com.blueoptima.connectors.scr.managers;

import com.blueoptima.connectors.common.message.InfrastructureDetails;
import com.blueoptima.discovery.cs.common.AuthenticationType;
import com.blueoptima.clients.github.InstallationAccessTokenClient;
import org.apache.logging.log4j.Logger;

public class AuthenticationManager {
    private final ConfigurationManager configManager;
    private final Logger logger;

    public AuthenticationManager(ConfigurationManager configManager) {
        this.configManager = configManager;
        this.logger = configManager.getLogger();
    }

    public void authenticate() throws Exception {
        InfrastructureDetails infraDetails = configManager.getInfraDetails();
        logger.info("Authenticating for user: {} using {}", 
                    infraDetails.getInstanLogin(), 
                    infraDetails.getAuthenticationType());

        String credential = infraDetails.getDecryptedCredential();
        boolean authenticated = configManager.getRequest().getInfraExtractor().authenticate(infraDetails.getInstanLogin(), credential);

        if (!authenticated) {
            throw new Exception("Could not log on to the server");
        }
    }

    public void authenticatePRRequest() throws Exception {
        InfrastructureDetails infraDetails = configManager.getInfraDetails();
        boolean authenticated = configManager.getRequest().getInfraExtractor().authenticatePRRequest(infraDetails);
        
        if (!authenticated) {
            throw new Exception("Pull request authentication failed");
        }
    }

    public void cleanupAuthentication() {
        InfrastructureDetails infraDetails = configManager.getInfraDetails();
        if (infraDetails.getAuthenticationType() == AuthenticationType.GITHUB_APPS) {
            InstallationAccessTokenClient.instance().cleanUpTokens(false, configManager.getRequest().getRequestID());
        }
    }
}