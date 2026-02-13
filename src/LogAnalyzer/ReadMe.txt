How execution works:
    - Start (L1): When the script sees Starting AutoFix..., it grabs the requestID and the
       ThreadName (e.g., pool-1-thread-5). It creates an entry in memory linked to that ID.

    - Processing (Interleaved logs): The script ignores noise but watches for errors.

    - Error (L2): If it sees an error log with the matching requestID, it updates the
      memory entry with the ErrorReason.

    - End (L3): When it sees Final response created..., it checks which thread logged it.
      It looks up the requestID associated with that thread, calculates the time difference
      (L3 timestamp - L1 timestamp), prints the row, and clears the memory.



This is the exact line L1 from logs(request start):
    - 2026-02-13 04:52:24.562 GMT INFO  pool-69-thread-1 BBAgentAutofixOrchestrator - ParameterizedMessage[messagePattern=Starting AutoFix data generation for requestID: {}, stringArgs=[scan_mlkevg4t_46niibah8oh], throwable=null]

lines for L2 (to get potential errorReason, might not exist):
    - 2026-02-13 05:00:48.002 GMT ERROR pool-69-thread-2 MASingleFileInOutService - ParameterizedMessage[messagePattern=Error in getting Single file autofix generation from ML service {} for reqId: {} , stringArgs=[[GENERIC_EXCEPTION] Server error. Unexpected error executing your command. Retry or contact BlueOptima Support., scan_mlkevg56_fckgx4ljby], throwable=null]
        Now there are 2 cases for finding the ending line for a request (L3), if error Reason found for request ID look for:
        logger.info("RequestID: {}. How-To-Fix payload created.", requestID);

else if no error detected, look for line:
    - 2026-02-08 16:21:42.667 GMT INFO  pool-69-thread-1 MASingleFileInOutService - ParameterizedMessage[messagePattern=Autofix process completed successfully for file: {}, stringArgs=[/home/blueoptima/BlueOptima-Integrator/Data/maintainability_agent/scan_mldy3lwk_vk5irgywb4/multifile_ma_request/FlartChartsJDBCDaoImpl.java], throwable=null]
