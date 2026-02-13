package com.blueoptima.uix.dao.impl;

import com.blueoptima.uix.common.OverviewChartGroup;
import com.blueoptima.uix.common.OverviewSortingColumn;
import com.blueoptima.uix.dto.overview.OverviewFilterInfo;
import com.blueoptima.uix.dto.aiImpact.GenAIFilterInfo;
import com.blueoptima.uix.util.DAOPartitionHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class GenAiQueryBuilder {

    @Autowired
    private DAOPartitionHelper daoPartitionHelper;

    public String getDevCategorisationQuery(OverviewFilterInfo request, Long enterpriseId, Map<String, Object> parameters, StringBuilder filtersWithClause, Double aceReqForLastMonth) {
        String statsTable = daoPartitionHelper.getPartition("mv_scr_activity_stats", enterpriseId);
        String processedMlcadData = daoPartitionHelper.getPartition("daily_refresh.po_processed_mlcad_data", enterpriseId);

        return "WITH " + filtersWithClause + ", \n" +
                "monthly_ace AS (...), \n" +
                "active_devs AS (...), \n" +
                "license_flag AS (...), \n" +
                "ai_commit_flag AS (...) \n" +
                "SELECT \n" +
                "    ad.id_employee, \n" +
                "    CASE \n" +
                "        WHEN lf.is_licensed = TRUE AND COALESCE(ai.has_ai_commit, FALSE) = TRUE THEN 'LicensedAi' \n" +
                "        WHEN lf.is_licensed = TRUE AND COALESCE(ai.has_ai_commit, FALSE) = FALSE THEN 'LicensedNonAi' \n" +
                "        WHEN lf.is_licensed = FALSE AND COALESCE(ai.has_ai_commit, FALSE) = TRUE THEN 'UnlicensedAi' \n" +
                "        ELSE 'UnlicensedNonAi' \n" +
                "    END AS developercategory \n" +
                "FROM active_devs ad \n" +
                "JOIN license_flag lf USING (id_employee) \n" +
                "JOIN ai_commit_flag ai USING (id_employee)";
    }

    public String getDevContributionStatsQuery(OverviewFilterInfo request, Long enterpriseId, StringBuilder filtersWithClause, Map<String, Object> parameters, Double ceThreshold, Boolean isCountReq) {
        String statsTable = daoPartitionHelper.getPartition("mv_scr_activity_stats", enterpriseId);
        String processedMlcadData = daoPartitionHelper.getPartition("daily_refresh.po_processed_mlcad_data", enterpriseId);

        return "WITH " + filtersWithClause + " \n" +
                "SELECT distinct id_employee \n" +
                "FROM (SELECT stats.id_employee \n" +
                "      FROM " + statsTable + " stats \n" +
                "      JOIN po_enterprise pe USING (id_enterprise) \n" +
                "      JOIN " + processedMlcadData + " USING (id_screntry) \n" +
                "      JOIN collEmpDetails USING (id_project_sub, id_employee) \n" +
                "      WHERE stats.id_tseries::date BETWEEN :startMonth AND :endMonth \n" +
                "      AND stats.id_enterprise = :enterpriseId \n" +
                "      AND ml_authored_ratio >= :mlAuthoredThreshold \n" +
                "      GROUP BY stats.id_employee \n" +
                "      HAVING SUM(stats.nu_ce_model_units * pe.nu_hours_per_unit) >= :ceThreshold \n" +
                "     ) AS aiContributors";
    }

    public String getProductivityImpactQuery(OverviewFilterInfo request, List<Integer> devIds, Long enterpriseId) {
        String query = "SELECT COALESCE(SUM(nu_bce) / NULLIF(COUNT(DISTINCT (id_employee, id_tseries)), 0), 0) AS avgBce, \n" +
                "       CASE \n" +
                "           WHEN sum(nu_bce) > 0 THEN sum(nu_aberrant_ce) / sum(nu_bce) * 100 \n" +
                "           ELSE 0 \n" +
                "       END AS pctAberrantBce \n" +
                "FROM daily_refresh.mv_calendar \n" +
                "WHERE id_enterprise = :enterpriseId \n" +
                "AND id_tseries::date BETWEEN :startDate AND :endDate ";

        if (devIds != null && !devIds.isEmpty()) {
            query += "AND id_employee IN (:employeeIds) ";
        }

        return query;
    }

    public String getAceContributionQuery(OverviewFilterInfo request, StringBuilder filtersWithClause, Map<String, Object> parameters, Long enterpriseId, OverviewChartGroup group, OverviewSortingColumn sortingColumn, Boolean isTrendChart, Boolean isClickthrough) {
        String statsTable = daoPartitionHelper.getPartition("mv_scr_activity_stats", enterpriseId);
        String processedMlcadData = daoPartitionHelper.getPartition("daily_refresh.po_processed_mlcad_data", enterpriseId);

        String query = "WITH " + filtersWithClause + ", \n" +
                "ai_ace AS (...), \n" +
                "total_ace AS (...) \n" +
                "SELECT \n";

        if (isTrendChart) {
            if (group != null && !group.equals(OverviewChartGroup.EVERYTHING)) {
                query += "COALESCE(a." + group.getSqlField() + ", t." + group.getSqlField() + ") AS " + group.getRequestfield() + ", \n";
            }
            query += "COALESCE(a.dt_month, t.dt_month) AS dtMonth, \n" +
                    "COALESCE(t.totalAce, 0) AS enterpriseAce, \n";
        }

        query += "COALESCE(a.aiAce, 0) AS aiAce, \n" +
                "COALESCE(t.totalAce, 0) - COALESCE(a.aiAce, 0) AS humanAce \n";

        if (isClickthrough && group.equals(OverviewChartGroup.DEVELOPERS)) {
            query += ", ts_ml_license_rollout_date AS licenseAssignmentDate \n";
        }

        if (isClickthrough && !group.equals(OverviewChartGroup.DEVELOPERS)) {
            query += ", COALESCE(devCount, 0) AS developerCount \n";
        }

        query += "FROM ai_ace a \n";

        if (isTrendChart) {
            query += "FULL OUTER JOIN total_ace t ON a.dt_month = t.dt_month \n";
            if (group != null && !group.equals(OverviewChartGroup.EVERYTHING)) {
                query += "AND a." + group.getSqlField() + " = t." + group.getSqlField() + " \n";
            }
        } else {
            query += ", total_ace t \n";
        }

        if (isTrendChart) {
            if (isClickthrough) {
                query += "WHERE t." + group.getSqlField() + " IS NOT NULL AND t.totalAce > 0 \n";
            }
            query += "ORDER BY ";
            if (group != null && !group.equals(OverviewChartGroup.EVERYTHING)) {
                query += group.getRequestfield() + ", \n";
            }
            query += "dtMonth";
        }

        return query;
    }

    public String getBceMetricsQuery(Long enterpriseId, Integer userId, Boolean isSuperUser, GenAIFilterInfo request,
                                     Map<String, List<Integer>> devLicenseStats, boolean includeEnterpriseAvg, String startMonth, String endMonth, List<OverviewChartGroup> group, boolean isLicenseCategory, boolean isTrend, String sortingColumn, String sortingMode, boolean isGroupingCountries) {
        String query = "WITH filtered_employees AS (...), \n" +
                "bce_metrics AS (...), \n" +
                "grouped_metrics AS (...) \n";

        if (includeEnterpriseAvg) {
            query += ", enterprise_avg AS (...) \n";
        }

        query += "SELECT * FROM grouped_metrics \n";

        if (includeEnterpriseAvg) {
            query += "UNION ALL SELECT * FROM enterprise_avg \n";
        }

        if (sortingColumn != null && sortingMode != null) {
            query += "ORDER BY " + sortingColumn + " " + sortingMode;
        }

        return query;
    }

    public String getRolloutsQuery(String startMonth, String endMonth, Long enterpriseId) {
        String employeeHierarchy = daoPartitionHelper.getPartition("mv_employee_hierarchy", enterpriseId);

        return "SELECT \n" +
                "to_char(m, 'YYYY-MM') AS month, \n" +
                "COALESCE(rc.rollouts, 0) AS rollouts \n" +
                "FROM generate_series( \n" +
                "CAST(:startMonth AS DATE), \n" +
                "CAST(:endMonth AS DATE), \n" +
                "INTERVAL '1 month' \n" +
                ") AS m \n" +
                "LEFT JOIN ( \n" +
                "SELECT \n" +
                "date_trunc('month', ts_ml_license_rollout_date) AS rollout_month, \n" +
                "COUNT(*) AS rollouts \n" +
                "FROM po_employee \n" +
                "JOIN " + employeeHierarchy + " USING (id_employee) \n" +
                "WHERE id_enterprise = :enterpriseId \n" +
                "AND dt_last_activity >= CAST(:startMonth AS DATE) \n" +
                "AND ts_ml_license_rollout_date BETWEEN CAST(:startMonth AS DATE) AND CAST(:endMonth AS DATE) \n" +
                "GROUP BY rollout_month \n" +
                ") rc ON m = rc.rollout_month \n" +
                "ORDER BY m";
    }
}