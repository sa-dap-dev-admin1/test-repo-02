package com.blueoptima.connectors.scr.extractors;

import com.blueoptima.connectors.scr.BOpSCRExtractor;
import com.blueoptima.connectors.scr.vcs.GitExtractor;
import com.blueoptima.connectors.scr.vcs.SVNExtractor;
import com.blueoptima.connectors.scr.vcs.PerforceExtractor;
import com.blueoptima.connectors.common.error.BOpException;

public class ExtractorFactory {
    public BOpSCRExtractor createExtractor(String infraName) throws BOpException {
        switch (infraName.toLowerCase()) {
            case "git":
                return new GitExtractor();
            case "svn":
                return new SVNExtractor();
            case "perforce":
                return new PerforceExtractor();
            default:
                throw new BOpException("Unsupported infrastructure: " + infraName);
        }
    }
}