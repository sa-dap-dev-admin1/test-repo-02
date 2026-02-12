package com.blueoptima.connectors.scr;

import com.blueoptima.connectors.common.error.BOpException;
import com.blueoptima.connectors.scr.common.RevisionInfo;
import com.blueoptima.connectors.scr.common.BOpRelease;

import java.util.List;

public interface SCRExtractor {
    void init() throws BOpException;
    void connect() throws BOpException;
    boolean authenticate(String username, String credential) throws BOpException;
    List<RevisionInfo> getRevisionData(long startTime, long endTime) throws BOpException;
    List<BOpRelease> getReleaseData(String instanceName, long startTime, long endTime) throws BOpException;
    void disconnect();
    void cleanup();
}