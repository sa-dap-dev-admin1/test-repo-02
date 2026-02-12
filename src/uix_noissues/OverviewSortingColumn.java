/*
 * BlueOptima Limited CONFIDENTIAL
 * Unpublished Copyright (c) 2008-2021 BlueOptima, All Rights Reserved.
 *
 * NOTICE:  All information contained herein is, and remains the property of BlueOptima. The intellectual and technical concepts contained
 * herein are proprietary to BlueOptima and may be covered by U.K. and Foreign Patents, patents in process, and are protected by trade secret and copyright law.
 * Dissemination of this information or reproduction of this material is strictly forbidden unless prior written permission is obtained
 * from BlueOptima.  Access to the source code contained herein is hereby forbidden to anyone except current BlueOptima employees, managers or contractors who have executed
 * Confidentiality and Non-disclosure agreements explicitly covering such access.
 *
 * The copyright notice above does not evidence any actual or intended publication or disclosure  of  this source code, which includes
 * information that is confidential and/or proprietary, and is a trade secret, of  BlueOptima. ANY REPRODUCTION, MODIFICATION, DISTRIBUTION, PUBLIC  PERFORMANCE,
 * OR PUBLIC DISPLAY OF OR THROUGH USE  OF THIS  SOURCE CODE  WITHOUT  THE EXPRESS WRITTEN CONSENT OF BlueOptima IS STRICTLY PROHIBITED, AND IN VIOLATION OF APPLICABLE
 * LAWS AND INTERNATIONAL TREATIES.  THE RECEIPT OR POSSESSION OF  THIS SOURCE CODE AND/OR RELATED INFORMATION DOES NOT CONVEY OR IMPLY ANY RIGHTS
 * TO REPRODUCE, DISCLOSE OR DISTRIBUTE ITS CONTENTS, OR TO MANUFACTURE, USE, OR SELL ANYTHING THAT IT  MAY DESCRIBE, IN WHOLE OR IN PART.
 *
 */

package com.blueoptima.uix.common;

import lombok.Getter;

public enum OverviewSortingColumn {

	DEVELOPERS( "id_employee", "developer"),
	COLLECTIONS("id_project_sub", "collection"),
	PROJECTS( "id_project", "project"),
	EMPLOYERS( "id_employer", "employer"),
	ORGANIZATIONS( "id_organization", "organization"),
	COUNTRIES(  "tx_country", "country"),
	RANKS( "id_employee_rank", "rankName"),
	LOCATIONS("id_location", "city"),
	TYPES( "id_employee_type", "typeName"),
	SEGMENTS( "id_emp_segment", "segmentName"),
	JOBROLES("id_employee_role", "jobRoleName"),
	EVERYTHING("id_enterprise", "enterpriseId"),
	BCE("bch", "metric_bch"),
	AVG_BCE( "bch_per_day", "metric_bch_per_day"),
	CE_HOURS("ce_hours", OverviewMetric.CE_HOURS.getColumnName()),
	CE_UNITS("ce_units", OverviewMetric.CE_UNITS.getColumnName()),
    DEVELOPERCOUNT("developerCount", "developerCount"),
    AIACE("ai_ace", "aiAce"),
    HUMANACE("human_ace", "humanAce"),
    LICENSEASSIGNMENTDATE("licenseAssignmentDate", "licenseAssignmentDate"),
	AB_BCE( "aberrant_ce", "metric_aberrant_ce"),
	TENURE("tenure", "metric_tenure"),
	AVG_TENURE("avg_tenure", ""),
	DEV_COUNT("dev_count", "metric_dev_count"),
	BOTTOM_UP_COST("cost", ""),
	PCT_AB_BCE("pct_aberrant_bce", "metric_pct_aberrant_bce"),
	COST_PER_AVG_BCE("cost_per_avg_bce", ""),
	PCT_AB_CE_HOURS("pct_aberrant_ce_hours", ""),
	UID("uid", "uid"),
	PREDICTEDROLES("id_role_predicted", "predictedRoleName"),
	LINEMANAGERS("id_line_manager", "lineManagerName"),
	BUSINESSUNITS("id_business_unit", "businessUnitName"),
	TASKIDS("tx_orig_id", "taskId"),
	TASKPROJECTS("id_project", "taskProject"),
	SPRINTS("id_sprint", "sprint"),
	TASKSTATUS("tx_status_name", OverviewChartGroup.TASKSTATUS.getField()),
	TASKRESOLUTION("tx_resolution_name", OverviewChartGroup.TASKRESOLUTION.getField()),
	TASKPRIORITY("tx_priority_name", OverviewChartGroup.TASKPRIORITY.getField()),
	TASKTYPES("tx_task_type", OverviewChartGroup.TASKTYPES.getField()),
	TASK_COUNT("id_task", "metric_task_count"),
	SPRINT_COUNT("id_sprint", "metric_sprint_count"),
	SPRINT_HEALTH_PCT("dt_closed", "sprintHealthPct"),
	SPRINT_LENGTH("ts_end_date , ts_start_date", "sprint"),
	USERNAME(null, "userName");

	private final String field;
	@Getter
	private final String queryAlias;


	OverviewSortingColumn(String field, String queryAlias) {
		this.field = field;
		this.queryAlias = queryAlias;
	}

	public static OverviewSortingColumn getValue(String analysisLevel) {
		if (analysisLevel == null || analysisLevel.isEmpty()) {
			throw new IllegalArgumentException("Sorting column Cannot be null or empty");
		} else {
			for (OverviewSortingColumn value : values()) {
				if (value.name().equalsIgnoreCase(analysisLevel)) {
					return value;
				}
			}
		}
		throw new IllegalArgumentException("Invalid Sorting column :" + analysisLevel);
	}


	public String getField() {
		return field;
	}

	@Override
	public String toString() {
		return this.name().toLowerCase();
	}

}