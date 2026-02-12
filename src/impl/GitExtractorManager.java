package com.blueoptima.connectors.scr.managers;

import com.blueoptima.connectors.scr.dvcs.git.GitExtractor;
import com.blueoptima.connectors.common.message.InfrastructureDetails;
import com.blueoptima.connectors.common.error.BOpException;
import com.blueoptima.connectors.scr.dvcs.git.GitDownloader;
import com.blueoptima.connectors.scr.dvcs.git.GitDownloaderPool;
import org.apache.logging.log4j.Logger;

import java.nio.file.Path;
import java.nio.file.Paths;

public class GitExtractorManager {
    private final ConfigurationManager configManager;
    private final AuthenticationManager authManager;
    private final GitExtractor gitExtractor;
    private final Logger logger;
    private Path accessPath;

    public GitExtractorManager(ConfigurationManager configManager, AuthenticationManager authManager) {
        this.configManager = configManager;
        this.authManager = authManager;
        this.logger = configManager.getLogger();
        this.gitExtractor = new GitExtractor(configManager.getRequest(), configManager.getCurrentUpdateDir());
    }

    public void initialize() throws Exception {
        gitExtractor.init(configManager.getCurrentDataDir() + java.io.File.separator + "..");
        String fileRevisionsIdentifier = com.blueoptima.connectors.common.BOpFileUtils.checkFileExistence(com.blueoptima.connectors.ConnectorConstants.GIT_FILE_REVISION_SCRIPT);
        if (GitExtractor.fileRevisionsIdentifier == null) {
            GitExtractor.fileRevisionsIdentifier = fileRevisionsIdentifier;
        }

        String repoRevisionLister = com.blueoptima.connectors.common.BOpFileUtils.checkFileExistence(com.blueoptima.connectors.ConnectorConstants.GIT_REPO_REVISION_SCRIPT);
        if (GitExtractor.repoRevisionLister == null) {
            GitExtractor.repoRevisionLister = repoRevisionLister;
        }
    }

    public void connect() throws Exception {
        boolean connected = gitExtractor.connect(configManager.getInfraDetails().getInstanUrl(), "ext");
        if (!connected) {
            throw new BOpException("Could not connect to the Repository");
        }
    }

    public void downloadRepository() throws Exception {
        InfrastructureDetails infraDetails = configManager.getInfraDetails();
        GitDownloader downloader = GitDownloaderPool.getInstance().getDownloader(infraDetails, logger, gitExtractor);
        String downloadPath = downloader.download(configManager.getRequest().getRequestID(), infraDetails, configManager.getRequest().getEndDate(), logger);
        accessPath = Paths.get(downloadPath);
        gitExtractor.setGitRepositoryDir(com.blueoptima.connectors.ConnectorConstants.BLUEOPTIMA_HOME + downloadPath);
    }

    public void disconnect() {
        if (accessPath != null) {
            InfrastructureDetails infraDetails = configManager.getInfraDetails();
            GitDownloader downloader = GitDownloaderPool.getInstance().getDownloader(infraDetails, logger, gitExtractor);
            downloader.delete(accessPath, configManager.getRequest().getRequestID(), logger);
        }
        gitExtractor.disconnect();
    }

    public GitExtractor getGitExtractor() {
        return gitExtractor;
    }

    public Path getAccessPath() {
        return accessPath;
    }
}