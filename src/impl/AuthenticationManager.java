package com.blueoptima.connectors.scr.managers;

import com.blueoptima.connectors.common.message.RequestDetails;
import com.blueoptima.connectors.common.error.BOpException;
import com.blueoptima.connectors.scr.BOpSCRExtractor;

public class AuthenticationManager {
    private BOpSCRExtractor extractor;

    public void setExtractor(BOpSCRExtractor extractor) {
        this.extractor = extractor;
    }

    public void connect(RequestDetails request) throws BOpException {
        if (extractor == null) {
            throw new BOpException("Extractor not set");
        }
        boolean connected = extractor.connect(request.getInfraDetails().getInstanUrl(), "");
        if (!connected) {
            throw new BOpException("Failed to connect to the repository");
        }
    }

    public void authenticate(RequestDetails request) throws BOpException {
        if (extractor == null) {
            throw new BOpException("Extractor not set");
        }
        String login = request.getInfraDetails().getInstanLogin();
        String credential = request.getInfraDetails().getDecryptedCredential();
        boolean authenticated = extractor.authenticate(login, credential);
        if (!authenticated) {
            throw new BOpException("Authentication failed");
        }
    }

    public void disconnect() {
        if (extractor != null) {
            extractor.disconnect();
        }
    }
}