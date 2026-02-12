package com.blueoptima.connectors.scr;

import com.blueoptima.connectors.common.message.RequestDetails;
import com.blueoptima.connectors.common.constants.integrations.Extractor;
import com.blueoptima.connectors.common.constants.integrations.Version;
import com.blueoptima.connectors.scr.dvcs.git.*;
import com.blueoptima.connectors.scr.vcs.*;
import com.blueoptima.connectors.common.error.ExtractorInitializationException;
import com.blueoptima.connectors.error.codes.GenericErrorCode;

public class ExtractorFactory {

    public static BOpSCRExtractor createExtractor(RequestDetails request, SCR scr) throws Exception {
        String infraName = request.getInfraDetails().getInfraName().toLowerCase();
        Version version = Version.get(request.getInfraDetails().getInfraVersion());

        try {
            if (infraName.startsWith(Extractor.SVN.getType())) {
                return new SVNExtractor(scr, request);
            } else if (infraName.startsWith(Extractor.GIT.getType())) {
                return createGitExtractor(scr, request, version);
            } else if (infraName.startsWith(Extractor.TFVC.getType())) {
                return new TFSExtractor(scr, request);
            } else if (infraName.startsWith(Extractor.PERFORCE.getType())) {
                return new PerforceExtractor(scr, request);
            } else if (infraName.startsWith(Extractor.RTC_VC.getType())) {
                return new RTCExtractor(scr, request);
            } else if (infraName.startsWith(Extractor.PLASTICSCM.getType())) {
                return new PlasticSCMExtractor(scr, request);
            } else {
                throw new ExtractorInitializationException(GenericErrorCode.RCE0001);
            }
        } catch (NoClassDefFoundError error) {
            throw new ExtractorInitializationException(infraName, error);
        }
    }

    private static BOpSCRExtractor createGitExtractor(SCR scr, RequestDetails request, Version version) {
        if (version != null) {
            switch (version) {
                case AWS_CC:
                    return new AWSCCGitExtractor(scr, request);
                case GITHUB_CLOUD:
                case GITHUB_ENT:
                    return new GithubGitExtractor(scr, request);
                case GERRIT3:
                    return new GerritGitExtractor(scr, request);
                default:
                    return new GitExtractor(scr, request);
            }
        }
        return new GitExtractor(scr, request);
    }
}