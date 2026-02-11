package com.blueoptima.uix.dto.aiImpact;

import com.fasterxml.jackson.annotation.JsonCreator;
import lombok.Getter;

@Getter
public enum GenAISortingColumn {

  DEVELOPER_ID("developerId", "developerId"),
  AVG_BCE("avgBce", "avgBce"),
  PCT_AVG_BCE("pctAberrantBce", "pctAberrantBce"),
  LICENSE_ASSIGNMENT_DATE("licenseAssignmentDate", "licenseAssignmentDate"),
  COLLECTIONS("collections", "id_project_sub"),
  PROJECTS("projects", "id_project"),
  EMPLOYERS("employers", "id_employer"),
  ORGANIZATIONS("organizations", "id_organization"),
  COUNTRIES("countries", "tx_country"),
  RANKS("ranks", "id_employee_rank"),
  LOCATIONS("locations", "id_location"),
  TYPES("types", "id_employee_type"),
  SEGMENTS("segments", "id_emp_segment"),
  JOBROLES("jobRoles", "id_employee_role"),
  EVERYTHING("everything", "id_enterprise"),
  TOTAL_DEVELOPERS("devCount", "devCount"),
  LINE_MANAGER("lineManagers", "id_line_manager"),
  BUSINESS_UNIT("businessUnits", "id_business_unit");

  private final String displayName;  // frontend-friendly name
  private final String sqlColumn;    // actual DB column

  GenAISortingColumn(String displayName, String sqlColumn) {
    this.displayName = displayName;
    this.sqlColumn = sqlColumn;
  }

  @JsonCreator
  public static GenAISortingColumn getValue(String column) {
    for (GenAISortingColumn c : values()) {
      if (c.displayName.equalsIgnoreCase(column)) {
        return c;
      }
    }
    throw new IllegalArgumentException("Invalid sorting column: " + column);
  }
}package com.blueoptima.uix.dto.aiImpact;


import lombok.Data;

@Data
public class DeveloperMetrics {
  private Long developerId;
  private double avgBce;
  private double pctAberrantBce;
  private String licenseAssignmentDate;
}