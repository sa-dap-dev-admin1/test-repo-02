package com.blueoptima.connectors.scr;

import com.blueoptima.connectors.common.error.BOpException;
import com.blueoptima.connectors.common.constants.integrations.Extractor;
import com.blueoptima.connectors.common.constants.integrations.Version;
import com.blueoptima.connectors.scr.dvcs.git.GitExtractor;
import com.blueoptima.connectors.scr.dvcs.git.AWSCCGitExtractor;
import com.blueoptima.connectors.scr.dvcs.git.GithubGitExtractor;
import com.blueoptima.connectors.scr.dvcs.git.GerritGitExtractor;
import com.blueoptima.connectors.scr.vcs.svn.SVNExtractor;
import com.blueoptima.connectors.scr.vcs.PerforceExtractor;
import com.blueoptima.connectors.scr.vcs.tfs.TFSExtractor;
import com.blueoptima.connectors.scr.vcs.rtc.RTCExtractor;
import com.blueoptima.connectors.scr.vcs.plasticscm.PlasticSCMExtractor;

public class SCRExtractorFactory {
    public static SCRExtractor create(SCRConfiguration configuration) throws BOpException {
        String infraName = configuration.getRequest().getInfraDetails().getInfraName().toLowerCase();
        Version version = Version.get(configuration.getRequest().getInfraDetails().getInfraVersion());

        if (infraName.startsWith(Extractor.SVN.getType())) {
            return new SVNExtractor(configuration);
        } else if (infraName.startsWith(Extractor.GIT.getType())) {
            if (version == Version.AWS_CC) {
                return new AWSCCGitExtractor(configuration);
            } else if (version == Version.GITHUB_CLOUD || version == Version.GITHUB_ENT) {
                return new GithubGitExtractor(configuration);
            } else if (version == Version.GERRIT3) {
                return new GerritGitExtractor(configuration);
            } else {
                return new GitExtractor(configuration);
            }
        } else if (infraName.startsWith(Extractor.TFVC.getType())) {
            return new TFSExtractor(configuration);
        } else if (infraName.startsWith(Extractor.PERFORCE.getType())) {
            return new PerforceExtractor(configuration);
        } else if (infraName.startsWith(Extractor.RTC_VC.getType())) {
            return new RTCExtractor(configuration);
        } else if (infraName.startsWith(Extractor.PLASTICSCM.getType())) {
            return new PlasticSCMExtractor(configuration);
        } else {
            throw new BOpException(ExtractionStatus.INIT, GenericErrorCode.RCE0001, "Unsupported infrastructure: " + infraName);
        }
    }
}