package com.blueoptima.uix.dto.aiImpact;

import com.blueoptima.uix.common.PaginationMetaData;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class DeveloperClickThroughResponse {
  private PaginationMetaData metaData;
  private List<Map<Long, DeveloperMetrics>> results;

}