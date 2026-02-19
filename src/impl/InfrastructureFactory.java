package com.blueoptima.connectors.scr;

import com.blueoptima.connectors.common.message.RequestDetails;
import com.blueoptima.connectors.common.error.BOpException;
import com.blueoptima.connectors.scr.dvcs.git.GitExtractor;
import com.blueoptima.connectors.scr.vcs.PerforceExtractor;
import com.blueoptima.connectors.scr.vcs.svn.SVNExtractor;
import com.blueoptima.connectors.scr.vcs.tfs.TFSExtractor;

public class InfrastructureFactory {
    private BOpSCRExtractor extractor;

    public BOpSCRExtractor createExtractor(RequestDetails request) throws BOpException {
        String infraName = request.getInfraDetails().getInfraName().toLowerCase();
        
        if (infraName.startsWith("svn")) {
            extractor = new SVNExtractor(request);
        } else if (infraName.startsWith("git")) {
            extractor = new GitExtractor(request);
        } else if (infraName.startsWith("tfvc")) {
            extractor = new TFSExtractor(request);
        } else if (infraName.startsWith("perforce")) {
            extractor = new PerforceExtractor(request);
        } else {
            throw new BOpException("Unsupported infrastructure: " + infraName);
        }
        
        return extractor;
    }

    public BOpSCRExtractor getExtractor() {
        return extractor;
    }
}