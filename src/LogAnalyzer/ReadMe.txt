How execution works:
    - Start (L1): When the script sees Starting AutoFix..., it grabs the requestID and the
       ThreadName (e.g., pool-1-thread-5). It creates an entry in memory linked to that ID.

    - Processing (Interleaved logs): The script ignores noise but watches for errors.

    - Error (L2): If it sees an error log with the matching requestID, it updates the
      memory entry with the ErrorReason.

    - End (L3): When it sees Final response created..., it checks which thread logged it.
      It looks up the requestID associated with that thread, calculates the time difference
      (L3 timestamp - L1 timestamp), prints the row, and clears the memory.



How to use it
    - Save the code as LogAnalyzer.java.

    - Update the logDirectoryPath variable (line 42) to point to your folder,
      or pass the path as a command-line argument.

    - Important: Check line 17 (BASE_LOG_PATTERN). Log files vary wildly.
      Ensure the regex matches your file's timestamp and structure. The
      current regex assumes standard Log4j format: 2023-10-27 10:15:30.000 [main] INFO ClassName - Message