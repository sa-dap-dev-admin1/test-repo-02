package com.blueoptima.connectors.scr.services;

import com.blueoptima.connectors.common.message.RequestDetails;
import com.blueoptima.connectors.common.error.BOpException;
import com.blueoptima.connectors.scr.BOpSCRExtractor;
import com.blueoptima.discovery.cs.common.AuthenticationType;
import com.blueoptima.clients.github.InstallationAccessTokenClient;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class AuthenticationService {
    private static final Logger logger = LogManager.getLogger(AuthenticationService.class);

    public void authenticate(BOpSCRExtractor extractor, RequestDetails request) throws BOpException {
        logger.info("Authenticating user: {} using {}", 
                    request.getInfraDetails().getInstanLogin(),
                    request.getInfraDetails().getAuthenticationType());

        String username = request.getInfraDetails().getInstanLogin();
        String credential = request.getInfraDetails().getDecryptedCredential();
        AuthenticationType authType = request.getInfraDetails().getAuthenticationType();

        boolean authenticated;

        switch (authType) {
            case PASSWORD:
                authenticated = extractor.authenticate(username, credential);
                break;
            case TOKEN:
                authenticated = extractor.authenticateWithToken(credential);
                break;
            case SSH_KEY:
                authenticated = extractor.authenticateWithSSH(username, credential);
                break;
            case GITHUB_APPS:
                authenticated = authenticateGithubApps(extractor, request);
                break;
            default:
                throw new BOpException("Unsupported authentication type: " + authType);
        }

        if (!authenticated) {
            logger.error("Authentication failed for user: {}", username);
            throw new BOpException("Authentication failed");
        }

        logger.info("Authentication successful for user: {}", username);
    }

    private boolean authenticateGithubApps(BOpSCRExtractor extractor, RequestDetails request) throws BOpException {
        try {
            String token = InstallationAccessTokenClient.instance().getToken(request.getRequestID());
            return extractor.authenticateWithToken(token);
        } catch (Exception e) {
            logger.error("Failed to authenticate with GitHub Apps", e);
            throw new BOpException("GitHub Apps authentication failed", e);
        }
    }
}