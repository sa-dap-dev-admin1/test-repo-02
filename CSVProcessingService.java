package com.blueoptima.uix.service;

import com.blueoptima.uix.util.MultipartUtil;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Service
public class CSVProcessingService {

    public String extractCSVContents(MultipartFile data) throws IOException {
        return MultipartUtil.getData(data, null);
    }
}