
package com.blueoptima.connectors.scr;
//just a comment 12
import static com.blueoptima.connectors.ConnectorConstants.METRICS_ENABLED;
import static com.blueoptima.connectors.scr.constants.BopScrDataMiscKey.DEFAULT_BRANCH;
import static com.blueoptima.connectors.scr.constants.BopScrDataMiscKey.SD_FCE_FAILED_FILES;
import static com.blueoptima.connectors.scr.constants.BopScrDataMiscKey.SD_FCE_SKIPPED_LARGE_FILES;
import static com.blueoptima.connectors.scr.constants.BopScrDataMiscKey.SD_TOTAL_SCANNED_FILES_COUNT;
import static com.blueoptima.connectors.scr.constants.BopScrDataMiscKey.CRED_SCANNER_VERSION;
import static com.blueoptima.connectors.scr.constants.BopScrDataMiscKey.SD_FCE_VERSION;
import static com.blueoptima.connectors.util.ConfigUtil.getBooleanConfig;
import static com.blueoptima.cs.common.constants.IntegratorConfigConstants.BUILD_FILE_REVISION_BRANCH_ASSOCIATION;
import static com.blueoptima.cs.common.constants.IntegratorConfigConstants.PROCESS_PR_DELETE;
import static com.blueoptima.cs.common.constants.IntegratorConfigConstants.SHARED_DIRECTORY_ENABLED;
import static com.blueoptima.sca.SCAUtil.isSCAEnabled;
import static com.blueoptima.sca.SCAUtil.isSCAEnabledForExtractionRequest;
import static org.apache.commons.collections.CollectionUtils.isEmpty;

import com.blueoptima.clients.github.InstallationAccessTokenClient;
import com.blueoptima.commons.dto.rms.PublisherStatus;
import com.blueoptima.connectors.BOpInfraExtractor;
import com.blueoptima.connectors.ConnectorConstants;
import com.blueoptima.connectors.ConnectorConstants.EXTRACTOR;
import com.blueoptima.connectors.DataObliterator;
import com.blueoptima.connectors.IntegratorConfigTracker;
import com.blueoptima.connectors.common.BOpFileUtils;
import com.blueoptima.connectors.common.Duration;
import com.blueoptima.connectors.common.Housekeeper;
import com.blueoptima.connectors.common.constants.ExtractionStatus;
import com.blueoptima.connectors.common.constants.integrations.Extractor;
import com.blueoptima.connectors.common.constants.integrations.Version;
import com.blueoptima.connectors.common.error.BOpException;
import com.blueoptima.connectors.common.error.DataException;
import com.blueoptima.connectors.common.error.ExtractionException;
import com.blueoptima.connectors.common.error.ExtractorInitializationException;;
import com.blueoptima.connectors.common.error.GitCLIException;
import com.blueoptima.connectors.common.error.InstallDependentsException;
import com.blueoptima.connectors.common.message.BOpExtractionData;
import com.blueoptima.connectors.common.message.InfrastructureDetails;
import com.blueoptima.connectors.common.message.RequestDetails;
import com.blueoptima.connectors.common.workflow.ExtractionState;
import com.blueoptima.connectors.error.codes.GenericErrorCode;
import com.blueoptima.connectors.error.codes.SCRErrorCode;
import com.blueoptima.connectors.extraction.status.tracker.ScrExtractionTracker;
import com.blueoptima.connectors.main.VersionReader;
import com.blueoptima.connectors.mlcodedetection.CodeAuthorDetectionUtil;
import com.blueoptima.connectors.pullrequest.PRData;
import com.blueoptima.connectors.pullrequest.dto.Commit;
import com.blueoptima.connectors.pullrequest.dto.PullRequestData;
import com.blueoptima.connectors.scr.MetricEngine.BOpMetricsGenerator;
import com.blueoptima.connectors.scr.MetricEngine.Metrics;
import com.blueoptima.connectors.scr.common.BOpRelease;
import com.blueoptima.connectors.scr.common.BOpSCRData;
import com.blueoptima.connectors.scr.common.BuildRevisionData;
import com.blueoptima.connectors.scr.common.FileInfo;
import com.blueoptima.connectors.scr.common.RevisionFileMapping;
import com.blueoptima.connectors.scr.common.RevisionInfo;
import com.blueoptima.connectors.scr.dvcs.git.AWSCCGitExtractor;
import com.blueoptima.connectors.scr.dvcs.git.CloneRequestHandler;
import com.blueoptima.connectors.scr.dvcs.git.GerritGitExtractor;
import com.blueoptima.connectors.scr.dvcs.git.GitDownloader;
import com.blueoptima.connectors.scr.dvcs.git.GitDownloaderPool;
import com.blueoptima.connectors.scr.dvcs.git.GitExtractor;
import com.blueoptima.connectors.scr.dvcs.git.GithubGitExtractor;
import com.blueoptima.connectors.scr.dvcs.git.saasdownloader.PRDataSaasCommunicationService;
import com.blueoptima.connectors.scr.processing.utilities.HelperDataUtil;
import com.blueoptima.connectors.scr.strategies.buildrevision.BuildRevisionSetStrategy;
import com.blueoptima.connectors.scr.strategies.buildrevision.RevisionBuildEnum;
import com.blueoptima.connectors.scr.strategies.extractionmode.ExtractionModeDetails;
import com.blueoptima.connectors.scr.strategies.prdata.FetchPRModeEnum;
import com.blueoptima.connectors.scr.vcs.PerforceExtractor;
import com.blueoptima.connectors.scr.vcs.plasticscm.PlasticSCMExtractor;
import com.blueoptima.connectors.scr.vcs.rtc.RTCExtractor;
import com.blueoptima.connectors.scr.vcs.svn.SVNExtractor;
import com.blueoptima.connectors.scr.vcs.tfs.TFSExtractor;
import com.blueoptima.connectors.util.IntegratorConfigUtil;
import com.blueoptima.connectors.vulhunter.SourceVulDetectionUtil;
import com.blueoptima.cs.common.constants.IntegratorConfigConstants;
import com.blueoptima.cs.common.dto.exceptions.ConnectorShutdownSignalException;
import com.blueoptima.cs.common.dto.exceptions.ConnectorSleepSignalException_Exception;
import com.blueoptima.cs.common.dto.exceptions.IntegratorSuspendedException_Exception;
import com.blueoptima.cs.common.dto.exceptions.UnknownConnectorException;
import com.blueoptima.cs.common.dto.message.PRStatus;
import com.blueoptima.cs.common.dto.message.RequestType;
import com.blueoptima.discovery.cs.common.AuthenticationType;
import com.blueoptima.nro.maintainability.PluginMQWrapper;
import com.blueoptima.nro.maintainability.PluginPRDetailsProcessor;
import com.blueoptima.nro.maintainability.PluginPayloadHandler;
import com.blueoptima.nro.maintainability.dto.PrProcessingContext;
import com.blueoptima.nro.maintainability.dto.PublisherPayload;
import com.blueoptima.nro.maintainability.error.FatalPluginProcessingException;
import com.blueoptima.nro.maintainability.util.PluginMetricsAggregator;
import com.blueoptima.secretsdetection.fullcheckout.SDFCService;
import com.blueoptima.nro.maintainability.dto.PluginFileMetadata;
import com.blueoptima.services.ml.htf.util.LLMService;
import com.blueoptima.telemetry.TelemetryNames;
import com.blueoptima.telemetry.annotation.ActiveCounter;
import com.blueoptima.telemetry.annotation.MarkTimer;
import com.blueoptima.telemetry.annotation.TransactionSource;
import com.ibm.team.workitem.api.common.connectors.ConnectorException;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.owasp.encoder.Encode;


public class SCR extends BOpInfraExtractor {
	private static Logger logger = LogManager.getLogger(SCR.class);

	public static String SCR_SCRIPTS_DIR;

	private final BOpSCRExtractor extractor;
	private final BOpMetricsGenerator metricGenerator;
	private final RevisionRouter revisionRouter;
	private final SDFCService sdfcService;

	private BOpSCRData scrData = new BOpSCRData();
	private String publisherResponse;

	public String getPublisherResponse() {
		return publisherResponse;
	}

	public void setPublisherResponse(String publisherResponse) {
		this.publisherResponse = publisherResponse;
	}

	public SCR(RequestDetails request, Housekeeper housekeeper, List<ExtractionState> workflow) throws Exception {
		super(request, housekeeper, workflow);

		try {
			// Create appropriate extractor based on the infrastructure name
			String infraName = request.getInfraDetails().getInfraName();
			status = ExtractionStatus.QUEUED;
			try {
				if (infraName.toLowerCase().startsWith(Extractor.SVN.getType())) {
					init(Extractor.SVN.getShortName());
					extractor = new SVNExtractor(this, request);

				} else if (infraName.toLowerCase().startsWith(Extractor.GIT.getType())) {
					Version ver = Version.get(request.getInfraDetails().getInfraVersion());
					init(Extractor.GIT.getShortName());
					if(ver != null && ver.equals(Version.AWS_CC)) {
						extractor = new AWSCCGitExtractor(this, request);
					} else if (ver != null && (ver.equals(Version.GITHUB_CLOUD) || ver.equals(
							Version.GITHUB_ENT))) {
						extractor = new GithubGitExtractor(this, request);
					} else if (ver != null && ver.equals(Version.GERRIT3)) {
						extractor = new GerritGitExtractor(this, request);
					} else {
						extractor = new GitExtractor(this, request);
					}

				} else if (infraName.toLowerCase().startsWith(Extractor.TFVC.getType())) {
					init(Extractor.TFVC.getShortName());
					stopTFVCExtractionIfRequired(request);
					extractor = new TFSExtractor(this, request);

				} else if (infraName.toLowerCase().startsWith(Extractor.PERFORCE.getType())) {
					init(Extractor.PERFORCE.getShortName());
					extractor = new PerforceExtractor(this, request);

				} else if (infraName.toLowerCase().startsWith(Extractor.RTC_VC.getType())) {
					init(Extractor.RTC_VC.getShortName());
					extractor = new RTCExtractor(this, request);

				} else if(infraName.toLowerCase().startsWith(Extractor.PLASTICSCM.getType())){
					init(Extractor.PLASTICSCM.getShortName());
					extractor = new PlasticSCMExtractor(this,request);
				}
				else {
					throw new InstallDependentsException(GenericErrorCode.RCE0001);
				}
			} catch (NoClassDefFoundError error) {
				throw new ExtractorInitializationException(infraName, error);
			}

			extractor.setExtractionLog(extractionLog);
			extractor.setExtractionDetails(request.getRequestConfig(),request.getRequestType());

			metricGenerator = new BOpMetricsGenerator(extractor, currentUpdateDir, extractionLog, request);
			revisionRouter = new RevisionRouter(extractor, currentUpdateDir, request);

		} catch (BOpException e) {
			throw e;
		} catch (Exception e) {
			throw new BOpException(ExtractionStatus.QUEUED, GenericErrorCode.UNCLASSIFIED, e);
		}

		this.statusTracker = new ScrExtractionTracker(this);

    sdfcService = new SDFCService(extractor, revisionRouter, request, extractionLog);
  }

	/*
	 * Whether to stop TFVC extraction or not.
	 * This parameter is added in 5.15.0 and to be removed in future versions when this
	 * is not a concern anymore
	 * */
	private void stopTFVCExtractionIfRequired(RequestDetails request) throws BOpException {

		boolean connectorLevelSkip = false;
		if(IntegratorConfigUtil.shouldStopTFVCExtraction(extractionLog)) {
			connectorLevelSkip = true;
			extractionLog.info("Found TFVC skip parameter at the connector level");
		}

		boolean requestLevelSkip = false;
		if (request != null && request.getRequestConfig() != null &&
				request.getRequestConfig().containsKey(IntegratorConfigConstants.STOP_TFVC_EXTRACTION)) {
			requestLevelSkip = Boolean.parseBoolean((String)request.getRequestConfig().get(IntegratorConfigConstants.STOP_TFVC_EXTRACTION));
			if(requestLevelSkip) {
				extractionLog.info("Found TFVC skip parameter at the request level");
			}
		}

		boolean infraLevelSkip = false;

		if(request != null && request.getInfraDetails() != null &&
				request.getInfraDetails().getConfigParamsMap() != null && request.getInfraDetails().getConfigParamsMap().containsKey(IntegratorConfigConstants.STOP_TFVC_EXTRACTION)) {
			infraLevelSkip = Boolean.parseBoolean((String)request.getInfraDetails().getConfigParamsMap().get(IntegratorConfigConstants.STOP_TFVC_EXTRACTION));
			if(infraLevelSkip) {
				extractionLog.info("Found TFVC skip parameter at the infra level");
			}

		}

		if(connectorLevelSkip || requestLevelSkip || infraLevelSkip) {
			extractionLog.info("Stopping TFVC extraction");
			throw new BOpException(SCRErrorCode.IR0304);
		}


	}

	private void initRevisionActivityThreshold(RequestDetails request) {
		Map<String, Object> requestConfig = request.getRequestConfig();

		if(requestConfig != null && requestConfig.containsKey(ConnectorConstants.REVISION_ACTIVITY_THRESHOLD)) {
			String maxFiles = (String) requestConfig.get(ConnectorConstants.REVISION_ACTIVITY_THRESHOLD);
			revisionActivityThreshold = Integer.valueOf(maxFiles);
			logger.info("Setting revisionActivityThreshold from request config: "+ revisionActivityThreshold);
			return;
		}

		Map<String, String> infraConfigMap = request.getInfraDetails().getConfigParamsMap();
		if(infraConfigMap != null && infraConfigMap.containsKey(ConnectorConstants.REVISION_ACTIVITY_THRESHOLD)) {
			String maxFiles = infraConfigMap.get(ConnectorConstants.REVISION_ACTIVITY_THRESHOLD);
			revisionActivityThreshold = Integer.valueOf(maxFiles);
			logger.info("Setting revisionActivityThreshold from infra config: "+ revisionActivityThreshold);
			return;
		}
		revisionActivityThreshold =
				IntegratorConfigTracker.instance().getValueFromConfig(ConnectorConstants.REVISION_ACTIVITY_THRESHOLD,
						revisionActivityThreshold,logger);
	}


	@Override
	public void init(String dir) throws IllegalAccessException, BOpException {
		repositoryDataDir = dir;

		currentLogsDir = ConnectorConstants.EXTRACTION_LOGS_DIR;
		currentDataDir = ConnectorConstants.EXTRACTION_DATA_DIR + File.separator + dir + File.separator
				+ request.getRequestID();
		currentUpdateDir = ConnectorConstants.EXTRACTION_DATA_DIR + File.separator + dir + File.separator
				+ request.getRequestID() + File.separator + ConnectorConstants.DEF_UPDATE_DIR;

		//authorizing all file path
		File currentUpdateDirFile = BOpFileUtils.authorizeFilePath(currentUpdateDir);
		File currentDataDirFile = BOpFileUtils.authorizeFilePath(currentDataDir);
		File currentLogsDirFile = BOpFileUtils.authorizeFilePath(currentLogsDir);

		// Create the required directories
		currentLogsDirFile.mkdirs();
		currentDataDirFile.mkdirs();
		currentUpdateDirFile.mkdirs();

		currentExtractionDataFile = currentDataDir + File.separator + "Data_" + request.getRequestID()
				+ ConnectorConstants.FILE_EXTENSION_SEARIALIZED;

		Boolean debugOn = false;
		Map<String, Object> requestConfig = request.getRequestConfig();
		if(requestConfig != null && requestConfig.containsKey("debug")) {
			debugOn = Boolean.parseBoolean((String)request.getRequestConfig().get("debug"));
		}

		setExtractionLog(currentLogsDir + File.separator + "extraction_" + request.getRequestID() + ".log",
				(ConnectorConstants.isDebugEnabled() || debugOn ) ? Level.DEBUG : Level.INFO); // DEBUG
		extractionLog.info("Integrator Version : " + VersionReader.getInstance().getVersion());
		extractionLog.info("LogDir : " + currentLogsDir);
		extractionLog.info("Update Dir : " + currentUpdateDir);

		initRevisionActivityThreshold(request);

	}

	// Method to print the extraction data in log file
	private void printExtractionData(BOpSCRData scrData) {
		if (!extractionLog.isDebugEnabled()) {
			return;
		}
		SimpleDateFormat sdf = new SimpleDateFormat("yy/MM/dd'@'HH:mm:ss");

		extractionLog.debug(String.format("%-8s %-8s %-8s %-8s %-8s %-8s %-8s %-8s %-8s %-8s %-8s %-8s "
				+ "%-8s %-8s %-8s %-8s %-8s %-8s %-20s %-20s %-5s", "REVn", "LoC", "LoC_A", "LoC_R", "CLOC", "CLOC_F",
				"HALS", "HALS_F", "CYCLO", "CYCLO_F", "SIZE", "SIZE_F", "FanOut", "FanOut_F", "DAC", "DAC_F", "NoM",
				"NoM_F", "Time", "AUTHOR", "Copy"));

		Map<String, RevisionInfo> revisionInfosMap = new HashMap<>();
		if(scrData.getRevisionsInfo() != null) {
			revisionInfosMap = scrData.getRevisionsInfo().stream()
					.collect(Collectors.toMap(RevisionInfo::getRevision, revisionInfo -> revisionInfo));
		}

		Map<String, FileInfo> fileInfoMap = new HashMap<>();
		if(scrData.getFilesInfo() != null) {
			fileInfoMap = scrData.getFilesInfo().stream()
					.collect(Collectors.toMap(FileInfo::getFileNameDigest, fileInfo -> fileInfo));
		}


		if (scrData.getRevisionFileMappings() != null) {
			for (RevisionFileMapping revisionFileMapping : scrData.getRevisionFileMappings()) {
				checkInterrupt();
				FileInfo fileInfo = fileInfoMap.get(revisionFileMapping.getFileNameDigest());
				RevisionInfo revisionInfo = revisionInfosMap.get(revisionFileMapping.getRevision());

				extractionLog.debug(fileInfo.getTxWorkingFile());
				if(revisionFileMapping == null || revisionFileMapping.getMetricsVersionMap()==null){
					continue;
				}
				for(Integer version :revisionFileMapping.getMetricsVersionMap().keySet()){
					Map<Metrics, Long> revFileMap = revisionFileMapping.getMetricsVersionMap().get(version);
					extractionLog.debug(String.format("%-8s %-8d %-8d %-8d %-8d %-8d %-8d %-8d %-8d %-8d %-8d %-8d "
									+ "%-8d %-8d %-8d %-8d %-8d %-8d %-20s %-20s %-5b", revisionFileMapping.getRevision(),
							revFileMap.getOrDefault(Metrics.LoC,0L), revFileMap.getOrDefault(Metrics.LoC_Added,0L),
							revFileMap.getOrDefault(Metrics.LoC_Removed,0L), revFileMap.getOrDefault(Metrics.CommentLoC,0L),
							revFileMap.getOrDefault(Metrics.CommentLoC_Flux,0L), revFileMap.getOrDefault(Metrics.Halstead,0L),
							revFileMap.getOrDefault(Metrics.Halstead_Flux,0L), revFileMap.getOrDefault(Metrics.Cyclomatic,0L),
							revFileMap.getOrDefault(Metrics.Cyclomatic_Flux,0L), revFileMap.getOrDefault(Metrics.FileSize,0L),
							revFileMap.getOrDefault(Metrics.FileSize_Flux,0L), revFileMap.getOrDefault(Metrics.FanOut,0L),
							revFileMap.getOrDefault(Metrics.FanOut_Flux,0L), revFileMap.getOrDefault(Metrics.DAC,0L),
							revFileMap.getOrDefault(Metrics.DAC_Flux,0L), revFileMap.getOrDefault(Metrics.NoM,0L),
							revFileMap.getOrDefault(Metrics.NoM_Flux,0L),
							revisionInfo != null && revisionInfo.getCommitTime() == null ? "NULL" : sdf.format(new Date(revisionInfo.getCommitTime())),
							((revisionInfo != null)?revisionInfo.getUAuthor().getUserName() : "NULL"),
							revisionFileMapping.isPossibleCopy()));
				}
			}
		}

		// Print Tag data to logs
    extractionLog.debug(String.format("%-60s %20s", "Tag Name", "Approx. Create Date"));
		if (scrData.getReleases() != null && scrData.getReleases().size() > 0) {
			for (BOpRelease branch : scrData.getReleases()) {
        extractionLog.debug(String.format("%-60s %-20s", branch.getName(), sdf.format(new Date(branch.getCreateTime()))));
			}
		} else {
      extractionLog.debug("No tags found..");
		}
	}



	public boolean validateRequest() throws BOpException {
		InfrastructureDetails infraDetails = request.getInfraDetails();
		if (infraDetails.getInstanUrl() == null || infraDetails.getInstanUrl().equals("")) {// A URL is a must
      extractionLog.error("URL was not defined in the request");
			throw new ExtractionException(GenericErrorCode.RCE0001);
		}

		if (infraDetails.getInfraName() == null
        || infraDetails.getInfraName().equals("")
        || infraDetails.getInstanName() == null
        || infraDetails.getInstanName().equals("")) {
      extractionLog.error("Infrastructure Name was not defined in the request");
			throw new ExtractionException(GenericErrorCode.RCE0001);
		}

		if (request.getStartDate().getTime() >= request.getEndDate().getTime()) {
      extractionLog.error("Invalid Date Argument - FromDate should be less than ToDate");
			throw new ExtractionException(GenericErrorCode.RCE0001);
		}

		request.getInfraDetails().setPriorityBasedAuthenticator();
		return true;
	}

	@ActiveCounter(key = TelemetryNames.EXTRACTION_PR_KEY, fetchTransaction = TransactionSource.GLOBAL)
	@MarkTimer(key = TelemetryNames.EXTRACTION_PR_KEY)
	private void fetchPullRequestData() throws Exception {

		boolean processPRDelete = false;
		Map<String, Object> requestConfigMap = request.getRequestConfig();
		if(requestConfigMap != null && requestConfigMap.get(PROCESS_PR_DELETE)!=null && requestConfigMap.get(PROCESS_PR_DELETE).equals("true")) {
			processPRDelete = true;
		}

		PRData prData = extractor.getPullRequestData(request.getInfraDetails(), request, processPRDelete);
		if (prData == null) {
			logger.warn("Received null PRData from extractor. Skipping pull request processing.");
			// Ensure scrData is handled gracefully, e.g., by setting an empty list.
			scrData.setPullRequestList(Collections.emptyList());
			return;
		}else
			scrData.setPullRequestList(prData.getPullRequestData());

		if(processPRDelete) {
			fetchAndUpdateDeletedPRIdList(prData.getPrIdList(), scrData);
		}
	}

	/**
	 * This method handles the extraction If any error occurred during exception, this function throws Exception If
	 * extraction Cancel calls, This function return null If data successfully extracted, This function return
	 * BOpExtractionData object
	 *
	 * @return BOpExtractionData if successful null if the extraction was Canceled
	 * @throws ExtractionException
	 *             in case of any errors during extraction, after configured retries are over
	 */
	@MarkTimer(key = TelemetryNames.EXTRACTOR_KEY, type = TelemetryNames.EXTRACT_TYPE, tag = TelemetryNames.SCR)
	public BOpExtractionData extract() throws Exception {

		HelperDataUtil  helperDataUtil= revisionRouter.getHelperDataUtil();
		HelperDataUtil.setHelperDataUtilThreadLocal(helperDataUtil);

		Path accessPath =null;
		String fullCheckoutPath = null; //for full checkout extraction only currently
		InfrastructureDetails infraDetails =null;
		boolean isSharedDirectoryEnabled = IntegratorConfigTracker.instance().getBooleanConfig(SHARED_DIRECTORY_ENABLED,false);
		try {
			Duration d = new Duration("", extractionLog);
			setSCAEnabledInSCRData();
			infraDetails = request.getInfraDetails();
			List<String> fileTypes = null;
			FullCheckoutConfig fullCheckoutConfig = null; //sca full checkout
			Map requestConfig = request.getRequestConfig();
			BOpExtractionData bOpExtractionData = new BOpExtractionData();

			if (requestConfig != null) {
				String fileTypesStr = (String) requestConfig.get("fileTypes");
				if (StringUtils.isNotBlank(fileTypesStr)) {
					fileTypes = Arrays.asList(fileTypesStr.split(","));
					extractionLog.info("Setting requestConfig file types" + fileTypes);
				} else {
					extractionLog.warn("fileTypes string is null");
				}
			} else {
				extractionLog.info("requestConfig is null");
			}


			int index = 0;
      for (ExtractionState state : workflow) {
				status = state.getValue();
				ExtractionStatus currentStatus = getStatus();
				ExtractionStatus nextStatus = getNextStatus(index);
				index++;

				checkInterrupt();
				switch (currentStatus) {

					case QUEUED:
						break;

					case VALIDATE_DETAIL:
						if (!validateRequest()) {
							return null;
						}
						break;
					case INIT:
						extractor.init(currentDataDir + File.separator + "..");
						break;

					case INIT_PR_DATA:
						extractor.initPRData(infraDetails,request);
						break;

					case INIT_FULL_CHECKOUT:
						if (extractor instanceof GithubGitExtractor) {
							extractor.initPRData(infraDetails,request);
						}

						fullCheckoutConfig = new FullCheckoutConfig();
						fullCheckoutConfig.setExtractionLog(extractionLog);

						if(request.getRequestType().getId() != RequestType.EXTRACTION_ML_CODE_DETECTION.getId()){
							request.setEndDate(new Date());
						}

						fullCheckoutConfig.setInfraConfigs(request.getInfraDetails().getConfigParamsMap());
						fullCheckoutConfig.setRequestConfigs(request.getRequestConfig());
						fullCheckoutConfig.setRequestID(requestID);
						fullCheckoutConfig.setScaEnabled(isSCAEnabled(request));

						break;

					case CONNECT:
						// TODO handle this generically
						String protocol = infraDetails.getInfraName().equalsIgnoreCase("CVS_pserver") ? "pserver" : "ext";
						boolean connected = extractor.connect(infraDetails.getInstanUrl(), protocol);


						if (!connected) {
							extractionLog.error("Could not connect to the Repository");
							return null;
						}
						break;

					case AUTHENTICATE:

						extractionLog.info(
								"Authenticating for user: " + request.getInfraDetails().getInstanLogin() + " using "
										+ request.getInfraDetails().getAuthenticationType());

						String credential = request.getInfraDetails().getDecryptedCredential();
						boolean authenticated = extractor.authenticate(infraDetails.getInstanLogin(), credential);

						if (!authenticated) {
							extractionLog.error("Could logon to the server");
							return null;
						}
						break;

					case AUTHENTICATE_PR_DATA:
						if(extractor.isExtractPRData() || extractor.isExtractFaultyCommitData()) {
							authenticated = extractor.authenticatePRRequest(infraDetails);
							if (!authenticated) {
								extractionLog.error("Pull request authentication failed");
								return null;
							}
						}
						break;

					case DOWNLOAD_REPOSITORY:
						// TODO consider defining DistributedScrExtractor extends BOpScrExtractor and add download method in
						// that then call download method on that extractor rather than calling on downloader directly
						if (infraDetails.getInfraName().toLowerCase().startsWith(Extractor.GIT.getType())) {

							initOutputDirectory();

							String downloadPath ;
							if(request.getRequestType() == RequestType.HOOK_EXTRACTION || request.getRequestType() == RequestType.MAINTAINABILITY_APP_EVENTS ){
								List<String> commits = getRequestCommitList();
								downloadPath = getDownloadPathForHook(infraDetails,commits);
							}else{
								downloadPath = getDownloadPath(isSharedDirectoryEnabled, infraDetails);
							}
							accessPath = Paths.get(downloadPath);

              // TODO - calling init here might not be a good, think something else
                          extractor.init(ConnectorConstants.BLUEOPTIMA_HOME+downloadPath);
                          ((GitExtractor) extractor).setGitRepositoryDir(ConnectorConstants.BLUEOPTIMA_HOME+downloadPath);

                          final String fileRevisionsIdentifier = BOpFileUtils.checkFileExistence(ConnectorConstants.GIT_FILE_REVISION_SCRIPT);
							if (GitExtractor.fileRevisionsIdentifier == null) {
								GitExtractor.fileRevisionsIdentifier = fileRevisionsIdentifier;
							}

							final String repoRevisionLister = BOpFileUtils.checkFileExistence(ConnectorConstants.GIT_REPO_REVISION_SCRIPT);
							if (GitExtractor.repoRevisionLister == null) {
								GitExtractor.repoRevisionLister = repoRevisionLister;
							}
						}



						if (infraDetails.getInfraName().toLowerCase().startsWith(Extractor.PLASTICSCM.getType())) {
							((PlasticSCMExtractor)extractor).cloneRepoToLocal(request);
						}
						break;

					case GET_REVISION_STATE:

						fullCheckoutPath = getRevisionState(accessPath,infraDetails,fullCheckoutConfig);

						break;

					case SCAN_REVISION:

						extractor.performFullCheckoutScan(fullCheckoutPath,scrData,fullCheckoutConfig);
						break;

					case VALIDATE_INFRASTRUCTURE:
						extractor.validate(infraDetails.getInstanName(), infraDetails.getComponentName(),
								infraDetails.getConfigParamsMap());
						// Create BOpExtractionData
						final BOpExtractionData validationData = new BOpExtractionData();
						validationData.setRequestID(requestID);
						validationData.setFileName(currentExtractionLogFile);
						return validationData;

					case EXTRACT_METADATA:
						List<RevisionInfo> revisions = extractor
								.getRevisionData(request.getStartDate().getTime(), request.getEndDate().getTime());
							scrData.setRevisionsInfo(revisions);
						break;

					case EXTRACT_HOOK_PR_DATA:
						fetchPullRequestData();
						break;

					case EXTRACT_HOOK_REVISION_DATA:
						buildRevisionSet(infraDetails, fileTypes, d);
						break;

					case EXTRACT_PR_DATA:
						if (extractor.isExtractPRData()) {
							fetchPullRequestData();
						}
						break;

					case BUILD_REVISION_SET:
						buildRevisionSet(infraDetails, fileTypes, d);
						break;

					case BUILD_REVISION_SET_FULL_CHECKOUT:
						BuildRevisionData fullCheckoutBuildRevision = extractor.getFullCheckoutBuildRevisionSet(fullCheckoutPath,scrData,fullCheckoutConfig);
						scrData.setRevisionsInfo(fullCheckoutBuildRevision.getRevisionsInfo());
						scrData.setRevisionFileMappings(fullCheckoutBuildRevision.getRevisionFileMappings());
						scrData.setFilesInfo(fullCheckoutBuildRevision.getFilesInfo());
						extractionLog.info("File count: "+scrData.getFilesInfo().size()+", Revision File Mapping count: "+scrData.getRevisionFileMappings().size());

						break;

					case GET_RELEASE_DETAILS:
						List<BOpRelease> tags = extractor
								.getReleaseData(infraDetails.getInstanName(), request.getStartDate()
										.getTime(), request.getEndDate().getTime());
						scrData.setReleases(tags);
						break;

					case GENERATE_METRICS:
						metricGenerator.generateMetrics(scrData,fullCheckoutConfig);
						break;

					case REVISION_PROCESSING:
						boolean onlyDA = requestConfig != null ? Boolean.parseBoolean(
								(String) requestConfig.getOrDefault("onlyDA", "false")) : false;
						if(onlyDA)
							metricGenerator.generateMetrics(scrData,fullCheckoutConfig);
						else
							revisionRouter.processSCRData(scrData, fullCheckoutConfig);

						break;

					case OBLITERATE:
						d.update("SCR::: Running Obliteration");
						// integratorConfiguration could be null in case of isolated execution of integrator
						if (ConnectorConstants.integratorConfiguration != null
								&& ConnectorConstants.integratorConfiguration.getEnableObliteration()) {

							DataObliterator<BOpSCRData> obliterator = new ScrDataObliterator(
									infraDetails.getInfraVersion());
							obliterator.obliterate(scrData);
						}

						break;

					case EXTRACT_SECRET_DATA_ON_FULL_CHECKOUT:
						if (infraDetails.getInfraName().toLowerCase().startsWith(Extractor.GIT.getType())) {
							fullCheckoutConfig.setGenerateMetrics(false);
							fullCheckoutConfig.setScaEnabled(false);
							// we never want metric parsing for SD FCE Service
							if (request.getRequestConfig() != null) {
								request.getRequestConfig().put(METRICS_ENABLED, "false");
							} else {
								Map<String, Object> configMap = new HashMap<>();
								configMap.put(METRICS_ENABLED, "false");
								request.setRequestConfig(configMap);
							}
							BuildRevisionData sdFCEBuildRevisionData = sdfcService.processSDForFCE(
									fullCheckoutPath, fileTypes, fullCheckoutConfig);
							scrData.setRevisionsInfo(sdFCEBuildRevisionData.getRevisionsInfo());
							scrData.setRevisionFileMappings(sdFCEBuildRevisionData.getRevisionFileMappings());
							scrData.setFilesInfo(sdFCEBuildRevisionData.getFilesInfo());

							if (scrData.getMiscellaneous() == null){
								scrData.setMiscellaneous(new HashMap<>());
							}
							scrData.getMiscellaneous().put(DEFAULT_BRANCH, fullCheckoutConfig.getDefaultBranch());
							scrData.getMiscellaneous().put(SD_FCE_VERSION, "2.0");

							if (!isEmpty(fullCheckoutConfig.getSkippedLargeFiles())) {
								String json = gson.toJson(fullCheckoutConfig.getSkippedLargeFiles());
								extractionLog.info("skippedLargeFiles : " + json);
								scrData.getMiscellaneous().put(SD_FCE_SKIPPED_LARGE_FILES, json);
							}
							if (!isEmpty(fullCheckoutConfig.getFailedFiles())) {
								String json = gson.toJson(fullCheckoutConfig.getFailedFiles());
								extractionLog.info("failedFiles : " + json);
								scrData.getMiscellaneous().put(SD_FCE_FAILED_FILES, json);
							}
							extractionLog.info("totalScannedFiles : " + fullCheckoutConfig.getTotalScannedFiles());
							scrData.getMiscellaneous().put(SD_TOTAL_SCANNED_FILES_COUNT,
									String.valueOf(fullCheckoutConfig.getTotalScannedFiles()));
							scrData.getMiscellaneous().put(CRED_SCANNER_VERSION, fullCheckoutConfig.getCredScannerVersion());
						}
						break;

					case EXTRACT_MLCD_ON_FULL_CHECKOUT:

						if (infraDetails.getInfraName().toLowerCase().startsWith(Extractor.GIT.getType())) {
							currentExtractionDataFile = extractor.detectMachineGeneratedCode(scrData,
									fullCheckoutPath, request);
						}
						break;

					case EXTRACT_SECRETS_DATA:
						currentExtractionDataFile = extractor.getSecretsDetectionDataFileDiff(
								scrData, requestID, request);
						break;

					case EXTRACT_SVD_ON_FULL_CHECKOUT:
						if (infraDetails.getInfraName().toLowerCase().startsWith(Extractor.GIT.getType())) {
							currentExtractionDataFile = extractor.processSourceCodeVulnerabilityDetection(scrData,
									fullCheckoutPath, request);
						}
						break;

					case EXTRACT_HTF_DATA:
						if (request.getRequestConfig() != null && !request.getRequestConfig().isEmpty()) {
							LLMService llmServiceUtil = new LLMService();
							currentExtractionDataFile = llmServiceUtil.generateHowToFixSuggestionsForFile(
									request, accessPath, extractor, extractionLog);
						} else {
							logger.info("No config received to extract HTF for requestId {},skipping...",
									requestID);
						}
						break;

          			case BUILD_FILE_REVISION_BRANCH_ASSOCIATION:
						if (infraDetails.getInfraName().toLowerCase().startsWith(Extractor.GIT.getType())) {
              				boolean isEnabled = getBooleanConfig(request,
								BUILD_FILE_REVISION_BRANCH_ASSOCIATION, false, extractionLog);
							if (isEnabled) {
								extractor.processBuildRevisionBranchMapping(scrData);
							}
						}
						break;

					case MAINTAINABILITY_APP_EVENTS:
						PublisherPayload pubPayload = getPublisherPayload(accessPath, request.getRequestID());
						PublisherStatus publisherResponse = PluginMQWrapper.publish(pubPayload, request, extractionLog);
						currentExtractionDataFile = PluginPayloadHandler.createResponseFile(publisherResponse,request.getRequestID(), logger);
						extractionLog.info("Publisher Response received.. Proceeding with File Upload for reqID: {}",requestID);
						break;

					case SAVE_DATA_TO_FILE:
						// Write metrics into the extraction file
						d.update("SCR::: Write Data to File");
						BOpFileUtils.writeObjectToFile(currentExtractionDataFile, scrData);

						d.done().update("SCR::: Generate XML");
						writeExtractionDataAsJson(scrData);

						if (extractionLog.isDebugEnabled()) {
							d.done().update("SCR::: Print Debug data");
							printExtractionData(scrData);
						}
						break;

					case DISCONNECT:
						if (infraDetails.getAuthenticationType() == AuthenticationType.GITHUB_APPS) {
							InstallationAccessTokenClient.instance().cleanUpTokens(false, requestID);
						}
						break;

					case EXTRACTION_DONE:
						extractionLog.info("Extraction was successful :-)");
						bOpExtractionData.setRequestID(requestID);
						bOpExtractionData.setFileName(currentExtractionDataFile);
						if(request.getRequestType().equals(RequestType.EXTRACTION)){
							bOpExtractionData.computeIfEmpty(scrData);
						}

						return bOpExtractionData;

					default:
				} // End of switch
		  d.done();
		  if (nextStatus != null) {
			  d.update("SCR:::" + nextStatus.getMessage());
		  }
	  }//end of for loop
		} catch(BOpException e) {
			if(this.shouldRetry && !isExtractionCancelled()) {
				extractionLog.error("Error Occurred in extraction. Retrying again ", e);
				BOpExtractionData extractionData = retryExtraction();

				if(extractionData == null) {
					throw e;
				}

				return extractionData;
      } else {
			  throw e;
      }
    }finally {

			if (!isSharedDirectoryEnabled) {
				if (infraDetails.getInfraName().toLowerCase().startsWith(Extractor.GIT.getType()) && accessPath != null) {
					GitDownloader downloader = GitDownloaderPool.getInstance()
							.getDownloader(infraDetails, extractionLog, extractor.infraExtractor);
					downloader.delete(accessPath, requestID, extractionLog);
				}

				//Delete full checkout dir, if present
				Path fullCheckoutDirToDelete = Paths
						.get(ConnectorConstants.EXTRACTION_DATA_DIR, ConnectorConstants.GIT_PROJECTS_CLONE_DIR,
								String.valueOf(infraDetails.getInfraInstanId()), ConnectorConstants.FULL_CHECKOUT_DIR, requestID);
				if (Files.exists(fullCheckoutDirToDelete)) {
					FileUtils.deleteQuietly(fullCheckoutDirToDelete.toFile());
				}

			}
		}

		if (isExtractionCancelled()) {
			return null;
		}
		return null;
		//retry
	}

	private PublisherPayload getPublisherPayload(Path accessPath, String requestId) throws BOpException {
		try {
			PrProcessingContext filesForAF =
					PluginPRDetailsProcessor.processMaintainabilityPluginRequest( request, scrData);
			PluginPRDetailsProcessor.filterPRByFileThreshold(request,
					filesForAF.getFilesForProcessing());
			List<PluginFileMetadata> filesForAFWithMetrics =
					PluginMetricsAggregator.aggregateMetrics(filesForAF, request, extractor,
							accessPath.toString(), requestID);
			return PluginPayloadHandler.createPublisherPayload(filesForAFWithMetrics, accessPath,
							request, extractor, scrData, logger);
		} catch (FatalPluginProcessingException e){
			logger.error("Plugin Processing failed for reqId {}", requestId);
			// Create Publisher Failure Payload
			return PluginPayloadHandler.getFailurePayload(request,scrData,logger,true,e.getErrorCode().toString(),0,null);
		}
	}

	private List<String> getRequestCommitList() {
		ExtractionModeDetails details = extractor.getExtractionDetails();
		if (details.getFetchPRModeEnum() == FetchPRModeEnum.BY_PULL_REQUEST_ID)
			return getCommitsForPRList();
		else
			return getCommitsList(details.getCommitId());
	}

	private List<String> getCommitsForPRList() {
		if (scrData.getPullRequestList() == null || scrData.getPullRequestList().isEmpty()) {
			logger.error("Extraction by pull request ID was requested, but " +
					"no pull request data is available.");
			throw new IllegalArgumentException("For HOOK_EXTRACTION, " +
					"pullRequestNumber or commitIds must be provided.");
		}
		return scrData.getPullRequestList().stream()
							.map(PullRequestData::getCommits)
							.flatMap(Collection::stream)
							.distinct()
							.map(Commit::getCommitId)
							.collect(Collectors.toList());
	}

	private List<String> getCommitsList(List<String> commitHashes) {
		if (commitHashes == null || commitHashes.isEmpty()) {
			logger.error("Extraction by commitHashes List was requested, but " +
					"no data is available.");
			throw new IllegalArgumentException("For HOOK_EXTRACTION, " +
					"pullRequestNumber or commitIds must be provided.");
		}
		return commitHashes;
	}

	private String getDownloadPath(boolean isSharedDirectoryEnabled,
								   InfrastructureDetails infraDetails)
			throws IllegalAccessException, BOpException {

		String downloadPath;
		if (!isSharedDirectoryEnabled) {
			GitDownloader downloader = GitDownloaderPool
					.getInstance()
					.getDownloader(infraDetails, extractionLog, extractor.infraExtractor);
			downloadPath = downloader
					.download(request.getRequestID(), infraDetails, request.getEndDate(), extractionLog);
		} else {
			downloadPath = CloneRequestHandler
					.getInstance()
					.download(request.getRequestID(), infraDetails, request.getEndDate(), extractionLog);
		}
		return downloadPath;
	}

	private String getDownloadPathForHook(InfrastructureDetails infraDetails,List<String> commits)
			throws IllegalAccessException, BOpException {

		String downloadPath;
		GitDownloader downloader = GitDownloaderPool
					.getInstance()
					.getDownloader(infraDetails, extractionLog, extractor.infraExtractor);
		downloadPath = downloader.downloadForHook(request.getRequestID(), infraDetails,commits, extractionLog);
		return downloadPath;
	}

	private void initOutputDirectory() throws IllegalAccessException {
		final String revisionsOutputDir =
		  ConnectorConstants.EXTRACTION_DATA_DIR + File.separator
			+ EXTRACTOR.GIT.getShortName() + File.separator + request
			.getRequestID()
			+ File.separator
			+ ConnectorConstants.GIT_FILE_REVISION_SCRIPT_OUTPUT_DIR;
		new File(revisionsOutputDir).mkdirs();
		BOpFileUtils.authorizeFilePath(revisionsOutputDir);
		((GitExtractor) extractor).setRevisionsOutputDir(revisionsOutputDir);
	}

	private void buildRevisionSet(InfrastructureDetails infraDetails, List<String> fileTypes, Duration d)
			throws Exception {
		BuildRevisionData buildRevisionData = getBuildRevisionData(extractor.getExtractionDetails(),
				infraDetails, fileTypes);
		if(buildRevisionData == null)//Can happen in case of ML_CODE_DETECTION where [isDelta] is not given
			return;
		buildRevisionSet(d, buildRevisionData, infraDetails);
	}

	private BuildRevisionData getBuildRevisionData(ExtractionModeDetails details, InfrastructureDetails infraDetails, List<String> fileTypes) throws Exception {
		if (housekeeper != null) {
			housekeeper.getEntry(requestID).setLastUpdateTime(new Date());
		}
		RevisionBuildEnum mode = details.getRevisionBuildMode();
		BuildRevisionSetStrategy strategy = details.getBuildRevisionStrategy(mode);

		// This decouples the Context and Strategies from the internal structure of SCR, RequestDetails, and BOpSCRData.
		BuildRevisionSetStrategy.Context context = BuildRevisionSetStrategy.Context.builder()
				.extractor(this.extractor)
				.logger(this.extractionLog)
				.fileTypes(fileTypes)
				.instanceName(request.getInfraDetails().getInstanName())
				.extractionDetails(details)
				.pullRequestDataList(scrData.getPullRequestList() != null ? scrData.getPullRequestList() : Collections.emptyList())
				.startDate(request.getStartDate())
				.endDate(request.getEndDate())
				.analysisMode(details.getRevisionBuildMode())
				.build();

		return strategy.build(context);
	}

	private void buildRevisionSet(Duration d, BuildRevisionData buildRevisionData, InfrastructureDetails infraDetails) throws Exception {
		if (!CodeAuthorDetectionUtil.isMLCDRequest(request) && !SourceVulDetectionUtil.isSVDRequest(request)) {
			if (extractor.isExtractPRData() && extractor.isFetchDeletedRevision()) {
				buildRevisionData = extractor
						.fetchDeletedRevisionsForPullRequests(request, scrData.getPullRequestList());
			}

			if (extractor.isExtractFaultyCommitData()) {
				buildRevisionData = extractor.fetchFaultyCommits(request, extractor.faultyCommits);
			}
		}

		scrData.setRevisionFileMappings(buildRevisionData.getRevisionFileMappings());
		scrData.setRevisionsInfo(buildRevisionData.getRevisionsInfo());
		scrData.setFilesInfo(buildRevisionData.getFilesInfo());

      //  collecting patch ids
      if (request.getRequestType().equals(RequestType.EXTRACTION)
          && infraDetails.getInfraName().toLowerCase().startsWith(Extractor.GIT.getType())) {
        GitExtractor gitExtractor = (GitExtractor) extractor;
        gitExtractor.collectCommitPatchHash(scrData.getRevisionsInfo());
      }

		if (extractionLog.isDebugEnabled()) {
			d.done().update("SCR::: Print revisions");

			Map<String, RevisionInfo> revisionInfosMap = scrData.getRevisionsInfo().stream()
					.collect(Collectors.toMap(RevisionInfo::getRevision, revisionInfo -> revisionInfo));
			Map<String, FileInfo> fileInfoMap = scrData.getFilesInfo().stream()
					.collect(Collectors.toMap(FileInfo::getFileNameDigest, fileInfo -> fileInfo));

			for (RevisionFileMapping revisionFileMapping : scrData.getRevisionFileMappings()) {
				checkInterrupt();
				FileInfo fileInfo = fileInfoMap.get(revisionFileMapping.getFileNameDigest());

				extractionLog.debug("Revision: " + Encode.forJava(revisionFileMapping.getRevision())
						+ ", File : " + ((fileInfo != null) ? fileInfo.getTxWorkingFile() : ""));

				RevisionInfo revisionInfo = revisionInfosMap.get(revisionFileMapping.getRevision());
				extractionLog.debug(" Current Revision: "
						+ Encode.forJava(revisionInfo.getRevision())
						+ "  &  Prev Revision: "
						+ ((revisionFileMapping.getPreviousRevisionFileMapping() == null) ? ""
						: Encode.forJava(
								revisionFileMapping.getPreviousRevisionFileMapping().getRevision())));
			}
		}
	}


	private void setSCAEnabledInSCRData() {
		scrData.setScaEnabledDuringExtraction(isSCAEnabledForExtractionRequest(request));
	}


	private String getRevisionState(Path accessPath, InfrastructureDetails infraDetails, FullCheckoutConfig fullCheckoutConfig)
      throws IllegalAccessException, InterruptedException, GitCLIException, DataException, BOpException, IOException, ConnectorSleepSignalException_Exception, UnknownConnectorException, ConnectorException, IntegratorSuspendedException_Exception, ConnectorShutdownSignalException {
		/*
			in future the variable "fullCheckoutPath" can be named to something more generic as other infrastructures
			may not necessarily return a path to a repository. Keeping it "fullCheckoutPath" currently to avoid confusion
			since implementation is only for git
		*/

		if(accessPath == null || accessPath.toAbsolutePath() == null) {
			throw new IllegalAccessException("path to mirror clone dir is null");
		}

		fullCheckoutConfig.setPathToMirrorClone(ConnectorConstants.BLUEOPTIMA_HOME+accessPath.toAbsolutePath().toString());
		return extractor.getCommitState(infraDetails,fullCheckoutConfig,scrData);

	}

	private void fetchAndUpdateDeletedPRIdList(List<String> prIdListFromClient, BOpSCRData scrData) {
		PRStatus prByStatusRequest = new PRStatus();
		prByStatusRequest.setInfraInstanceId(request.getInfraDetails().getInfraInstanId());
		prByStatusRequest.setPrState(1); // nu_state is 1 for Open PRs
		List<String> openPRIds = PRDataSaasCommunicationService.getPRIdListByStatus(prByStatusRequest);
		List<String> deletedPRIds = openPRIds.stream()
				.filter(prId -> !prIdListFromClient.contains(prId))
				.collect(Collectors.toList());
		scrData.setDeletedPRIdList(deletedPRIds);
	}

	@Override
	public void disconnect() {
		if(extractor!=null) {
			extractor.cleanup();
			extractionLog.info("Disconnecting.....");
			extractor.disconnect();
		}
	}

	public BOpSCRExtractor getScrExtractor() {
		return extractor;
	}

	public BOpMetricsGenerator getMetricsEngine() {
		return metricGenerator;
	}

	public RevisionRouter getRevisionRouter() {
		return revisionRouter;
	}



}
