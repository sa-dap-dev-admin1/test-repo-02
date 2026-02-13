package com.blueoptima.uix.controller;
//test Generated PRId
import com.blueoptima.iam.dto.PermissionsCode;
import com.blueoptima.uix.SkipValidationCheck;
import com.blueoptima.uix.annotations.CSVConverter;
import com.blueoptima.uix.csv.FileSeparator;
import com.blueoptima.uix.dto.Message;
import com.blueoptima.uix.security.UserToken;
import com.blueoptima.uix.security.auth.AccessCode;
import com.blueoptima.uix.service.JiraService;
import com.blueoptima.uix.util.MultipartUtil;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;

@RestController
public class JiraController {

  @Autowired
  private JiraService jiraService;

  private static final Logger logger = LoggerFactory.getLogger(JiraController.class);

    public static final String UIX_DIR = "uix_invalid_csv_files";

  @RequestMapping(name = "Request to raise a ticket", value = "/v1/admin/jira/issue", method = RequestMethod.POST, consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
  @AccessCode(PermissionsCode.DEVELOPER_READ + PermissionsCode.DEVELOPER_WRITE)
  @SkipValidationCheck
  @CSVConverter
  public Message raiseJiraTicket(@RequestBody MultipartFile data) throws IOException {

      UserToken userToken = (UserToken) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
      String csvContents = MultipartUtil.getData(data,null);
      File file = null;
      Message message;


      //convert a multipart file to File. Test 10
  
      try {
          String tmpDir = System.getProperty("java.io.tmpdir");
          File dir = new File(tmpDir, UIX_DIR);
          // empty check here.
          if(!dir.exists()){
              dir.mkdir();
          }
          // doing null check 
          if(csvContents != null) {
              file = new File(dir, FileSeparator.CSV_SEPARATOR.getName() + "_" + System.currentTimeMillis() + "X" + userToken.getUserId());
              FileUtils.writeStringToFile(file, csvContents);
          }

          message = jiraService.raiseTSUP(file);

      } catch (IOException e) {
          logger.error("Error in file reading: ",e);
          throw e;
      }


      return message;


      }


  public class FileMetrics {

    private Double maintainability_score;
    private Double score_change;
    private Integer effort_saved;
    private Double expected_impact;
    private String fixes_applied;

    public FileMetrics(Double maintainability_score, Double score_change, Integer effort_saved, Double expected_impact, String fixes_applied) {
        this.maintainability_score = maintainability_score;
        this.score_change = score_change;
        this.effort_saved = effort_saved;
        this.fixes_applied = fixes_applied;
        this.expected_impact = expected_impact;
    }

    public static FileMetrics getFileAnalysis(FlartScoreResponse flartScoreResponse, String fixApplied,
                    FileAnalyticsResponse analyticsResponse, Logger LOGGER, String fileName, String requestID)
        throws PluginFileProcessingException {

        Double preScore = 0.0;
        Double postScore = 0.0;
        Integer rawEffort = 0;
        if (analyticsResponse != null) {
            LOGGER.info("Reading new analytics response for file {} in reqID {}", fileName, requestID);
            if(analyticsResponse.getOriginalFiles() == null || analyticsResponse.getOriginalFiles().size() == 0) {
                LOGGER.error("No original files found for file {} in reqID {}", fileName, requestID);
                throw new PluginFileProcessingException(PluginProcessingErrorCode.METRICS_DATA_PROCESSING_FAILED);
            }
            ResponseOriginalFileDataDto flartMetrics = analyticsResponse.getOriginalFiles()
                .stream()
                .filter(Objects::nonNull)
                .filter(ResponseOriginalFileDto::isPrimary)
                .map(ResponseOriginalFileDto::getResponseOriginalFileData)
                .findFirst()
                .orElse(null);
            if (flartMetrics != null && flartMetrics.getFlart()!=null) {
                FlartDataDto flartMetricsForFile = flartMetrics.getFlart();
                postScore = flartMetricsForFile.getNew();
                preScore = flartMetricsForFile.getOld();
                if(flartScoreResponse != null){
                    rawEffort = (flartScoreResponse.getEffortToFix()!=null)?flartScoreResponse.getEffortToFix():0;
                }
            } else{
                LOGGER.warn("New analytics response is null for file {} in reqID {}", fileName, requestID);
            }
        } else{
            if (flartScoreResponse == null) {
                LOGGER.warn("FileAnalysis/FlartScoreResponse is null for file {} in reqID {}", fileName,
                    requestID);
                return new FileMetrics(0.0, 0.0, 0, 0.0, fixApplied);
            } else {
                preScore = flartScoreResponse.getFilePreFlartScore();
                postScore = flartScoreResponse.getFilePostFlartScore();
                rawEffort = flartScoreResponse.getEffortToFix();
            }
        }
        return getFileMetricsHelper(flartScoreResponse, fixApplied, LOGGER, fileName,preScore,postScore,rawEffort, requestID);
    }

    private static void setFlartScoreResponses(String requestID, List<FlartScoreRequest> flartScoreRequests,
                                List<PluginFileMetadata> output) throws FatalPluginProcessingException {
    List<FlartScoreResponse> scores = getFlartScoreResponses(requestID, flartScoreRequests);
    Map<String, FlartScoreResponse> fileToScoreMap = FlartResponseMapper.mapResponsesToPath(flartScoreRequests, scores);
    String txWorkingFile;
    PluginFileMetadata currFileMetadata;
    for (int i = 0; i < scores.size(); i++) {
      currFileMetadata = output.get(i);
      txWorkingFile = currFileMetadata.getFileInfos().getTxWorkingFile();
      currFileMetadata.setFlartScoreResponse(fileToScoreMap.getOrDefault(txWorkingFile, null));
    }
  }

  private static List<FlartScoreResponse> getFlartScoreResponses(String requestID,
               List<FlartScoreRequest> flartScoreRequests) throws FatalPluginProcessingException {

    List<FlartScoreResponse> scores = Collections.emptyList();
    if (flartScoreRequests.isEmpty()) {
      logger.info("No files to process after metric aggregation for requestID {}", requestID);
      return scores;
    }
    try {
      scores = CommonsUtil.calculateFlartScoreForList(flartScoreRequests, logger);
      if(scores==null || scores.isEmpty()) {
        logger.error("Could not fetch Scores for requestID {} from CEQ ", requestID);
        throw new FatalPluginProcessingException(PluginProcessingErrorCode.FLART_SCORE_API_FAILED);
      }
    } catch (Exception e) {
      logger.error("Could not fetch Scores for requestID {} ", requestID);
      throw new FatalPluginProcessingException(PluginProcessingErrorCode.FLART_SCORE_API_FAILED);
    }
    return scores;
  }

    private static FileMetrics getFileMetricsHelper(FlartScoreResponse flartScoreResponse,
                  String fixApplied, Logger LOGGER, String fileName, Double preScore,
                  Double postScore, Integer rawEffort, String requestID) {
        // We default to 0.0 or 0 to prevent NPEs
        double postmaintScore = 0.0;
        double premaintScore = 0.0;
        Double maintPctChange = null ;
        double expectedImpact = 0.0;
        int effortSaved = 0;
        String safeFixApplied = (fixApplied != null) ? fixApplied : "";
        if (flartScoreResponse == null) {
            LOGGER.warn("FileAnalysis/FlartScoreResponse is null for file {} in reqID {}", fileName,
                requestID);
            return new FileMetrics(postmaintScore, maintPctChange, effortSaved, expectedImpact,
                safeFixApplied);
        }
        if ( rawEffort != null )
            effortSaved = rawEffort;

        if (postScore != null) {
            postmaintScore = (1.0 - postScore) * 100.0;
            expectedImpact = 100 - postmaintScore;
        }
        if (preScore != null) {
            premaintScore = (1.0 - preScore) * 100.0;
            maintPctChange = postmaintScore - premaintScore;
        }

        if(maintPctChange !=null && maintPctChange < 0.0) {
            LOGGER.warn("maintPctChange is negative for file {} in reqID {}", fileName,
                    requestID);
        }
        return new FileMetrics(postmaintScore, maintPctChange, effortSaved, expectedImpact,
            safeFixApplied);
    }

}
