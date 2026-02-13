/*
 * BlueOptima Limited CONFIDENTIAL
 * Unpublished Copyright (c) 2008-2025 BlueOptima, All Rights Reserved.
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
 * |
 */


// Test Sanity
package com.blueoptima.uix.dao.impl;

import com.blueoptima.uix.common.OverviewChartGroup;
import com.blueoptima.uix.common.OverviewSortingColumn;
import com.blueoptima.uix.common.configs.ConfigurationProperties;
import com.blueoptima.uix.dao.GenAiDao;
import com.blueoptima.uix.dao.GenAiDaoHelper;
import com.blueoptima.uix.dao.impl.hibernate.OverviewDaoHelperV2;
import com.blueoptima.uix.dao.impl.hibernate.OverviewDaoHelperV2.QueryData;
import com.blueoptima.uix.dto.AceContribution;
import com.blueoptima.uix.dto.ProductivityImpact;
import com.blueoptima.uix.dto.aiImpact.AiImpactScatterChartResponse;
import com.blueoptima.uix.dto.aiImpact.GenAIFilterInfo;
import com.blueoptima.uix.dto.aiImpact.Metric;
import com.blueoptima.uix.dto.overview.OverviewFilterInfo;
import com.blueoptima.uix.util.SQLUtil;
import org.hibernate.SQLQuery;
import org.hibernate.SessionFactory;
import org.hibernate.query.NativeQuery;
import org.hibernate.transform.Transformers;
import org.hibernate.type.BooleanType;
import org.hibernate.type.DateType;
import org.hibernate.type.DoubleType;
import org.hibernate.type.IntegerType;
import org.hibernate.type.LongType;
import org.hibernate.type.StringType;
import java.util.Collections;
import org.hibernate.type.Type;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


@Repository
public class GenAiDaoImpl implements GenAiDao {

    private static final String EMP_DETAILS_QUERY = " inner join po_employer emp_employer using (id_employer)\n" +
            "         inner join po_location emp_location using (id_location)\n" +
            "         left outer join po_dev_role_predicted emp_role_predicted using (id_role_predicted)\n" +
            "         inner join po_employee_rank emp_rank using (id_employee_rank)\n" +
            "         inner join po_employee_role emp_role using (id_employee_role)\n" +
            "         inner join po_emp_segment emp_segment using (id_emp_segment)\n" +
            "         inner join po_employee_type emp_type using (id_employee_type)\n" +
            "         inner join po_line_manager emp_line_manager using (id_line_manager)\n" +
            "         inner join po_business_unit emp_business_unit using (id_business_unit) ";

    @Autowired
    private SessionFactory sessionFactory;

    @Autowired
    private ConfigurationProperties configurationProperties;

    @Override
    public Map<String, List<Integer>> getDevCategorisation(OverviewFilterInfo request, Long enterpriseId, Map<String, Object> parameters, StringBuilder filtersWithClause, Double aceReqForLastMonth) {

        String statsTable = DAOPartitionHelper.getPartition("mv_scr_activity_stats", enterpriseId);
        String processedMlcadData = DAOPartitionHelper.getPartition("daily_refresh.po_processed_mlcad_data", enterpriseId);
        Double mlAuthoredThreshold = configurationProperties.getMlauthoredCodeThreshold();

        String query = " With " + filtersWithClause + " , \n" +
                "     monthly_ace AS (SELECT cal.id_employee, \n" +
                "                            dt_month           AS month_start, \n" +
                "                            SUM(cal.nu_ce_model_units * pe.nu_hours_per_unit) AS total_ace \n" +
                "                     FROM daily_refresh.mv_calendar_by_month cal \n" +
                "                              JOIN po_enterprise pe USING (id_enterprise) \n" +
                "                     WHERE cal.id_enterprise = :enterpriseId \n" +
                "                       AND cal.dt_month BETWEEN :startMonth AND :endMonth \n" +
                "                       AND EXISTS (SELECT 1 \n" +
                "                                   FROM collEmpDetails ce \n" +
                "                                   WHERE ce.id_employee = cal.id_employee) \n" +
                "                     GROUP BY cal.id_employee, \n" +
                "                              dt_month), \n" +
                "     active_devs AS (SELECT id_employee \n" +
                "                     FROM monthly_ace \n" +
                "                     GROUP BY id_employee \n" +
                "                     HAVING COUNT(*) = \n" +
                "                            ( \n" +
                "                                (date_part('year', CAST(:endMonth AS date)) - \n" +
                "                                 date_part('year', CAST(:startMonth AS date))) * 12 \n" +
                "                                    + (date_part('month', CAST(:endMonth AS date)) - \n" +
                "                                       date_part('month', CAST(:startMonth AS date))) \n" +
                "                                    + 1 \n" +
                "                                ) \n" +
                "                        AND ( \n" +
                "                         ( \n" +
                "                             CAST(:startMonth AS date) <> date_trunc('month', CAST(:endMonth AS date)) \n" +
                "                                 AND MIN( \n" +
                "                                             CASE \n" +
                "                                                 WHEN month_start < date_trunc('month', CAST(:endMonth AS date)) \n" +
                "                                                     THEN total_ace \n" +
                "                                                 END \n" +
                "                                     ) >= 1 \n" +
                "                             ) \n" +
                "                             OR \n" +
                "                         ( \n" +
                "                             CAST(:startMonth AS date) = date_trunc('month', CAST(:endMonth AS date)) \n" +
                "                             ) \n" +
                "                         ) \n" +
                "                        AND MAX( \n" +
                "                                    CASE \n" +
                "                                        WHEN month_start = date_trunc('month', CAST(:endMonth AS date)) \n" +
                "                                            THEN total_ace \n" +
                "                                        END \n" +
                "                            ) >= :lastMonthRequiredAce" +
             "), \n" +
                "     license_flag AS ( \n" +
                "         SELECT \n" +
                "             ad.id_employee, \n" +
                "             case \n" +
                "                 when nu_ml_license_type is not null \n" +
                "                     and nu_ml_license_type <> 0 \n" +
                "                     and ts_ml_license_rollout_date is not null \n" +
                "                     and ts_ml_license_rollout_date <= :endMonth \n" +
                "                     and ( \n" +
                "                          ts_ml_license_revoke_date is null \n" +
                "                              or ts_ml_license_revoke_date >= :startMonth \n" +
                "                          ) \n" +
                "                     THEN TRUE \n" +
                "                 ELSE FALSE \n" +
                "                 END AS is_licensed \n" +
                "         FROM active_devs ad \n" +
                "                  JOIN po_employee pe USING (id_employee) \n" +
                "         WHERE pe.id_enterprise = :enterpriseId \n" +
                "     ), \n" +
                "     ai_commit_flag AS ( \n" +
                "         SELECT \n" +
                "             ad.id_employee, \n" +
                "             EXISTS ( \n" +
                "                 SELECT 1 \n" +
                "                 FROM po_emp_id s \n" +
                "                          JOIN " + processedMlcadData + " p USING (id_emp_id) \n" +
                "                 WHERE s.id_employee = ad.id_employee \n" +
                "                   AND p.ml_authored_ratio >= :mlRatio \n" +
                "                   AND p.ts_committed_on\\:\\:date BETWEEN :startMonth AND :endMonth and s.id_enterprise = :enterpriseId \n" +
                "                   AND p.id_enterprise = :enterpriseId \n" +
                "             ) AS has_ai_commit \n" +
                "         FROM active_devs ad \n" +
                "     ) \n" +
                "SELECT \n" +
                "    ad.id_employee, \n" +
                "    CASE \n" +
                "        WHEN lf.is_licensed = TRUE \n" +
                "            AND COALESCE(ai.has_ai_commit, FALSE) = TRUE \n" +
                "            THEN 'LicensedAi' \n" +
                "        WHEN lf.is_licensed = TRUE \n" +
                "            AND COALESCE(ai.has_ai_commit, FALSE) = FALSE \n" +
                "            THEN 'LicensedNonAi' \n" +
                "        WHEN lf.is_licensed = FALSE \n" +
                "            AND COALESCE(ai.has_ai_commit, FALSE) = TRUE \n" +
                "            THEN 'UnlicensedAi' \n" +
                "        ELSE 'UnlicensedNonAi' \n" +
                "        END AS developercategory \n" +
                "FROM active_devs ad \n" +
                "          JOIN license_flag lf USING (id_employee) \n" +
                "          JOIN ai_commit_flag ai USING (id_employee)";

        NativeQuery nativeQuery = sessionFactory.getCurrentSession().createNativeQuery(query);

        SQLUtil.setParameter(nativeQuery, parameters);
        nativeQuery.addScalar("id_employee", LongType.INSTANCE);
        nativeQuery.addScalar("developercategory", StringType.INSTANCE);

        nativeQuery.setParameter("enterpriseId", enterpriseId);
        nativeQuery.setParameter("startMonth", LocalDate.parse(request.getStartMonth()));
        nativeQuery.setParameter("endMonth", LocalDate.parse(request.getEndMonth()));
        nativeQuery.setParameter("mlRatio", mlAuthoredThreshold);
        nativeQuery.setParameter("lastMonthRequiredAce", aceReqForLastMonth);

        List<Object[]> rows = nativeQuery.getResultList();
        return buildLicenseStatsResponse(rows);

    }

    private Map<String, List<Integer>> buildLicenseStatsResponse(List<Object[]> rows) {

        Map<String, List<Integer>> result = new HashMap<>();

        result.put("licensedAi", new ArrayList<>());
        result.put("licensedNonAi", new ArrayList<>());
        result.put("unlicensedAi", new ArrayList<>());
        result.put("unlicensedNonAi", new ArrayList<>());

        for (Object[] row : rows) {
            Long employeeId = ((Number) row[0]).longValue();
            String category = (String) row[1];

            switch (category) {
                case "LicensedAi":
                    result.get("licensedAi").add(employeeId.intValue());
                    break;

                case "LicensedNonAi":
                    result.get("licensedNonAi").add(employeeId.intValue());
                    break;

                case "UnlicensedAi":
                    result.get("unlicensedAi").add(employeeId.intValue());
                    break;

                case "UnlicensedNonAi":
                    result.get("unlicensedNonAi").add(employeeId.intValue());
                    break;
            }
        }

        return result;
    }


    @Override
    public List<Integer> getDevContributionStats(OverviewFilterInfo request, Long enterpriseId, StringBuilder filtersWithClause, Map<String, Object> parameters, Double ceThreshold, Boolean isCountReq) {

        String statsTable = DAOPartitionHelper.getPartition("mv_scr_activity_stats", enterpriseId);
        String processedMlcadData = DAOPartitionHelper.getPartition("daily_refresh.po_processed_mlcad_data", enterpriseId);

        Double mlAuthoredThreshold = configurationProperties.getMlauthoredCodeThreshold();

        String query = "With " + filtersWithClause +
                " SELECT distinct id_employee \n" +
            " FROM (SELECT stats.id_employee \n" +
            " FROM " + statsTable + " stats \n" +
            " JOIN po_enterprise pe USING (id_enterprise) \n" +
            " JOIN " + processedMlcadData + " USING (id_screntry) \n" +
            " JOIN collEmpDetails USING (id_project_sub, id_employee) \n" +
            " WHERE stats.id_tseries\\:\\:date BETWEEN :startMonth AND :endMonth \n" +
            " AND stats.id_enterprise = :enterpriseId \n" +
            " AND ml_authored_ratio >= " + mlAuthoredThreshold + " \n" +
            " group by stats.id_employee \n" +
            "         HAVING SUM(stats.nu_ce_model_units * pe.nu_hours_per_unit) >= :ceThreshold \n" +
            "     ) AS aiContributors" ;

        NativeQuery<Integer> nativeQuery = sessionFactory.getCurrentSession().createNativeQuery(query);

        nativeQuery.setParameter("startMonth", LocalDate.parse(request.getStartMonth()));
        nativeQuery.setParameter("endMonth", LocalDate.parse(request.getEndMonth()));
        nativeQuery.setParameter("enterpriseId", enterpriseId);
        nativeQuery.setParameter("ceThreshold", ceThreshold);
        SQLUtil.setParameter(nativeQuery, parameters);

        return nativeQuery.list();
    }

    @Override
    public Boolean checkDataExists(String startMonth, StringBuilder getFiltersWithClauseQuery, Map<String, Object> parameters, Long enterpriseId){
        String query = "With " + getFiltersWithClauseQuery.toString() + " \n" +
                " SELECT EXISTS ( \n" +
                "    SELECT 1 \n" +
                "    FROM po_employee \n" +
                "    join collEmpDetails using (id_employee)" +
                "    WHERE id_enterprise = :enterpriseId\n" +
                "      AND nu_ml_license_type IS NOT NULL \n" +
                " ) AS is_data_updated ";

        NativeQuery nativeQuery = sessionFactory.getCurrentSession().createNativeQuery(query);

        nativeQuery.addScalar("is_data_updated", BooleanType.INSTANCE);
        nativeQuery.setParameter("enterpriseId", enterpriseId);
        nativeQuery.setParameter("startMonth", LocalDate.parse(startMonth));
        SQLUtil.setParameter(nativeQuery, parameters);

        return (Boolean) nativeQuery.getSingleResult();
    }

    @Override
    public ProductivityImpact getProductivityImpact(OverviewFilterInfo request, List<Integer> devIds, Long enterpriseId){
        String query = "Select COALESCE(SUM(nu_bce) / NULLIF(COUNT(DISTINCT (id_employee, id_tseries)), 0), 0) AS avgBce, \n" +
                "       CASE \n" +
                "           WHEN sum(nu_bce) > 0 \n" +
                "               THEN sum(nu_aberrant_ce) / sum(nu_bce) * 100 \n" +
                "           ELSE 0 \n" +
                "           END                                                 AS pctAberrantBce \n" +
                " from daily_refresh.mv_calendar \n" +
                " where id_enterprise = :enterpriseId \n" +
                " and id_tseries\\:\\:date between :startDate AND :endDate ";

        if(devIds != null && !devIds.isEmpty()){
            query += " and id_employee in (:employeeId) ";
        }
        NativeQuery<ProductivityImpact> nativeQuery = sessionFactory.getCurrentSession().createNativeQuery(query);

        nativeQuery.addScalar("avgBce", DoubleType.INSTANCE);
        nativeQuery.addScalar("pctAberrantBce", DoubleType.INSTANCE);

        nativeQuery.setParameter("startDate", LocalDate.parse(request.getStartMonth()));
        nativeQuery.setParameter("endDate", LocalDate.parse(request.getEndMonth()));
        nativeQuery.setParameter("enterpriseId", enterpriseId);

        if(devIds != null && !devIds.isEmpty()){
            nativeQuery.setParameterList("employeeId", devIds);
        }
        nativeQuery.setResultTransformer(Transformers.aliasToBean(ProductivityImpact.class));

        return nativeQuery.getSingleResult();
    }

    @Override
    public List<AceContribution> getAceContribution(OverviewFilterInfo request, StringBuilder filtersWithClause, Map<String, Object> parameters, Long enterpriseId, OverviewChartGroup group, OverviewSortingColumn sortingColumn, Boolean isTrendChart, Boolean isClickthrough){
        String statsTable = DAOPartitionHelper.getPartition("mv_scr_activity_stats", enterpriseId);
        String processedMlcadData = DAOPartitionHelper.getPartition("daily_refresh.po_processed_mlcad_data", enterpriseId);

        Double mlAuthoredThreshold = configurationProperties.getMlauthoredCodeThreshold();
        String grouping = null;
        String selectAlias = null;
        String sortingScalarAlias = null;
        String sortingAlias = null;
        if(group != null && !group.equals(OverviewChartGroup.EVERYTHING)){
            grouping = group.getSqlField();
            selectAlias = group.getRequestfield();
        }

        if(sortingColumn != null && !sortingColumn.equals(OverviewSortingColumn.DEVELOPERCOUNT) && !sortingColumn.equals(OverviewSortingColumn.AIACE) && !sortingColumn.equals(OverviewSortingColumn.HUMANACE) && !sortingColumn.equals(OverviewSortingColumn.LICENSEASSIGNMENTDATE)){
            sortingAlias = getSortingProjection(sortingColumn);
            sortingScalarAlias = sortingColumn.getQueryAlias();
        }

        String query = " With " + filtersWithClause + ", \n";

        query += "     ai_ace as (select sum(nu_ce_model_units * pe.nu_hours_per_unit) as aiAce \n";

                if(isTrendChart)
                    query += ", cast(date_trunc('month', id_tseries) AS DATE) AS dt_month \n";

                if(isTrendChart && grouping != null)
                    query += " , collEmpDetails." + grouping + " \n";

                if(isClickthrough && sortingColumn != null && sortingAlias != null && sortingScalarAlias != null && !sortingColumn.equals(OverviewSortingColumn.COUNTRIES))
                    query += sortingAlias + " as " + sortingScalarAlias + "\n";

                query += "                from " + statsTable + " \n" +
                "                         join po_enterprise pe using (id_enterprise) \n" +
                "                         join " + processedMlcadData + " using (id_screntry) \n" +
                "                         join collEmpDetails using (id_project_sub, id_employee) \n" +
                "                where pe.id_enterprise = :enterpriseId \n" +
                "                  and ml_authored_ratio >= " + mlAuthoredThreshold + " \n" +
                "                  and id_tseries\\:\\:date between :startMonth and :endMonth ";

                if(isTrendChart){
                    query += " group by ";
                    if(grouping != null)
                        query += " collEmpDetails." + grouping + ", \n ";

                    query += " cast(date_trunc('month', id_tseries) AS DATE) \n";

                    if(isClickthrough && sortingColumn != null && sortingScalarAlias != null && !sortingColumn.equals(OverviewSortingColumn.COUNTRIES))
                        query += "," + sortingScalarAlias + "\n";
                }
                query += "), \n" +
                "     total_ace as (Select sum(nu_ce_model_units * nu_hours_per_unit) as totalAce \n";

                if(isTrendChart){
                    if(grouping != null)
                        query += " , collEmpDetails." + grouping + " \n";
                    query += " , dt_month \n";
                    if(isClickthrough && !group.equals(OverviewChartGroup.DEVELOPERS))
                        query += " , count(distinct id_employee) as devCount \n";

                    if(isClickthrough && sortingColumn != null && sortingAlias != null && sortingScalarAlias != null && !sortingColumn.equals(OverviewSortingColumn.COUNTRIES))
                        query += sortingAlias + " as " + sortingScalarAlias + "\n";
                }
                query += "                   from daily_refresh.mv_calendar_by_month cal \n" +
                "                            join po_enterprise using (id_enterprise) \n" +
                "                            join collEmpDetails using (id_project_sub, id_employee) \n" +
                "                   where cal.id_enterprise = :enterpriseId \n" +
                "                     and dt_month between :startMonth and :endMonth \n" +
                "                   and nu_ce_model_units * nu_hours_per_unit > 0 \n";
                if(isTrendChart) {
                    query += " group by ";
                    if (grouping != null)
                        query += " collEmpDetails." + grouping + ", \n ";

                    query += " dt_month \n";

                    if(isClickthrough && sortingColumn != null && sortingScalarAlias != null && !sortingColumn.equals(OverviewSortingColumn.COUNTRIES))
                        query += "," + sortingScalarAlias + "\n";
                }

                query += " ) SELECT \n";

                if(isTrendChart){
                    if (grouping != null)
                        query += "COALESCE(a." + grouping + ", t." + grouping + ") AS " + selectAlias + ", \n";

                    query += " COALESCE(a.dt_month, t.dt_month)               AS dtMonth, \n" +
                            " COALESCE(t.totalAce, 0)                        AS enterpriseAce, \n";
                }

                query += " COALESCE(a.aiAce, 0) AS aiAce, \n" +
                "    COALESCE(t.totalAce, 0) - COALESCE(a.aiAce, 0) AS humanAce \n";

                if(isClickthrough && group.equals(OverviewChartGroup.DEVELOPERS)) {
                    query += ", ts_ml_license_rollout_date AS licenseAssignmentDate \n";
                }

                if(isClickthrough && !group.equals(OverviewChartGroup.DEVELOPERS)) {
                    query += ", COALESCE(devCount, 0)                  AS developerCount \n";
                }

                if(isClickthrough && sortingColumn != null && sortingScalarAlias != null && !sortingColumn.equals(OverviewSortingColumn.COUNTRIES))
                    query += ",t." + sortingScalarAlias + "\n";

                query += "FROM ai_ace a \n";

                if(isTrendChart){
                    query += "FULL OUTER JOIN total_ace t\n" +
                            "  ON a.dt_month = t.dt_month \n";

                    if(grouping != null){
                        query += " AND a." + grouping + " = t." + grouping + "\n";

                        if (isClickthrough && group.equals(OverviewChartGroup.DEVELOPERS))
                            query += " JOIN po_employee pe on pe.id_employee = t.id_employee ";
                    }

                }else{
                    query += " ,total_ace t \n";
                }

                if(isTrendChart){
                    if(isClickthrough)
                        query += " where t." + grouping + " is not null and t.totalAce > 0 \n";
                    query += " ORDER BY ";

                    if(grouping != null)
                        query += selectAlias + ", \n";

                    query +=  " dtMonth";
                }


        NativeQuery<AceContribution> nativeQuery = sessionFactory.getCurrentSession().createNativeQuery(query);

        if(isTrendChart){
            nativeQuery.addScalar("dtMonth", StringType.INSTANCE);
            nativeQuery.addScalar("enterpriseAce", DoubleType.INSTANCE);
            if(grouping != null){
                if(grouping.equals("id_organization"))
                    nativeQuery.addScalar(selectAlias, LongType.INSTANCE);

                if(grouping.equals("id_project"))
                    nativeQuery.addScalar(selectAlias, LongType.INSTANCE);

                if(grouping.equals("id_project_sub"))
                    nativeQuery.addScalar( selectAlias, LongType.INSTANCE);

                if(grouping.equals("id_employee"))
                    nativeQuery.addScalar(selectAlias, LongType.INSTANCE);

                if(grouping.equals("id_employer"))
                    nativeQuery.addScalar(selectAlias, LongType.INSTANCE);

                if(grouping.equals("id_line_manager"))
                    nativeQuery.addScalar(selectAlias, LongType.INSTANCE);

                if(grouping.equals("tx_country"))
                    nativeQuery.addScalar(selectAlias, StringType.INSTANCE);

                if(grouping.equals("id_location"))
                    nativeQuery.addScalar(selectAlias, LongType.INSTANCE);

                if(grouping.equals("id_employee_rank"))
                    nativeQuery.addScalar(selectAlias, LongType.INSTANCE);

                if(grouping.equals("id_employee_type"))
                    nativeQuery.addScalar(selectAlias, LongType.INSTANCE);

                if(grouping.equals("id_employee_role"))
                    nativeQuery.addScalar(selectAlias, LongType.INSTANCE);

                if(grouping.equals("id_role_predicted"))
                    nativeQuery.addScalar(selectAlias, LongType.INSTANCE);

                if(grouping.equals("id_emp_segment"))
                    nativeQuery.addScalar(selectAlias, LongType.INSTANCE);

                if(grouping.equals("id_business_unit"))
                    nativeQuery.addScalar(selectAlias, LongType.INSTANCE);

                if(isClickthrough && !group.equals(OverviewChartGroup.DEVELOPERS))
                    nativeQuery.addScalar("developerCount", IntegerType.INSTANCE);

                if(isClickthrough && group.equals(OverviewChartGroup.DEVELOPERS))
                    nativeQuery.addScalar("licenseAssignmentDate", DateType.INSTANCE);
            }
            if(sortingColumn != null && sortingScalarAlias != null)
                nativeQuery.addScalar(sortingScalarAlias, StringType.INSTANCE);
        }

        nativeQuery.addScalar("aiAce", DoubleType.INSTANCE);
        nativeQuery.addScalar("humanAce", DoubleType.INSTANCE);

        nativeQuery.setParameter("startMonth", LocalDate.parse(request.getStartMonth()));
        nativeQuery.setParameter("endMonth", LocalDate.parse(request.getEndMonth()));
        nativeQuery.setParameter("enterpriseId", enterpriseId);
        SQLUtil.setParameter(nativeQuery, parameters);

        nativeQuery.setResultTransformer(Transformers.aliasToBean(AceContribution.class));

        return nativeQuery.list();

    }

    public String getSortingProjection(OverviewSortingColumn sortingColumn) {
        if (sortingColumn == null) {
            return "";
        }

        switch (sortingColumn) {
            case ORGANIZATIONS:
                return ", collEmpDetails.tx_org_name";
            case PROJECTS:
                return ", collEmpDetails.tx_project_name";
            case COLLECTIONS:
                return ", collEmpDetails.tx_subproject_name";
            case DEVELOPERS:
                return ", collEmpDetails.developer";
            case EMPLOYERS:
                return ", collEmpDetails.tx_employer_name";
            case LINEMANAGERS:
                return ", collEmpDetails.tx_line_manager";
            case JOBROLES:
                return ", collEmpDetails.tx_employee_role";
            case LOCATIONS:
                return ", collEmpDetails.tx_city";
            case RANKS:
                return ", collEmpDetails.tx_employee_rank";
            case BUSINESSUNITS:
                return ", collEmpDetails.tx_business_unit";
            default:
                return "";
        }
    }

    @Override
    public Map<String, AiImpactScatterChartResponse.Category> getBceMetricsForAllCategory(Long enterpriseId, Integer userId, Boolean isSuperUser, GenAIFilterInfo request,
        Map<String, List<Integer>> devLicenseStats, boolean includeEnterpriseAvg, String startMonth, String endMonth, String sortingColumn, String sortingMode, List<OverviewChartGroup> group
    ) {

        List<Integer> licensedAi = devLicenseStats.getOrDefault("licensedAi", Collections.emptyList());
        List<Integer> licensedNonAi = devLicenseStats.getOrDefault("licensedNonAi", Collections.emptyList());
        List<Integer> unlicensedAi = devLicenseStats.getOrDefault("unlicensedAi", Collections.emptyList());
        List<Integer> unlicensedNonAi = devLicenseStats.getOrDefault("unlicensedNonAi", Collections.emptyList());

        QueryData queryData =  GenAiDaoHelper.getBceMetricsQuery(enterpriseId, userId, isSuperUser,  request, includeEnterpriseAvg, startMonth, endMonth, group, licensedAi, licensedNonAi, unlicensedAi, unlicensedNonAi, false, false, null, null, false);

        StringBuilder query = OverviewDaoHelperV2.generateQuery(queryData);
        if (includeEnterpriseAvg) {
            query.append(" UNION ALL SELECT * FROM enterprise_avg");
        }

        NativeQuery sqlQuery = sessionFactory.getCurrentSession().createNativeQuery(query.toString());
        Map<String, Object> parameters = queryData.getParameters();
        Map<String, Type> scalars = queryData.getScalars();
        SQLUtil.setParameter(sqlQuery, parameters);
        SQLUtil.setScalars(sqlQuery,scalars);

        Map<String, AiImpactScatterChartResponse.Category> result = GenAiDaoHelper.getResultList(sqlQuery,sessionFactory.getCurrentSession());

        return result;

    }

    @Override
    public List<Metric> getBceMetricsforLicenseCategory(Long enterpriseId, Integer userId, Boolean isSuperUser, GenAIFilterInfo request,
        Map<String, List<Integer>> devLicenseStats, boolean includeEnterpriseAvg, String startMonth, String endMonth, String sortingColumn, String sortingMode,List<OverviewChartGroup> group
    ) {

        Integer licenseLevelId = request.getLicenseLevelId();

        List<Integer> licensedAi = Collections.emptyList();
        List<Integer> licensedNonAi = Collections.emptyList();
        List<Integer> unlicensedAi = Collections.emptyList();
        List<Integer> unlicensedNonAi = Collections.emptyList();

        // Fill only the list corresponding to licenseLevelId
        if (licenseLevelId != null) {
            switch (licenseLevelId) {
                case 1:
                    licensedAi = devLicenseStats.getOrDefault("licensedAi", Collections.emptyList());
                    break;
                case 2:
                    licensedNonAi = devLicenseStats.getOrDefault("licensedNonAi", Collections.emptyList());
                    break;
                case 3:
                    unlicensedAi = devLicenseStats.getOrDefault("unlicensedAi", Collections.emptyList());
                    break;
                case 4:
                    unlicensedNonAi = devLicenseStats.getOrDefault("unlicensedNonAi", Collections.emptyList());
                    break;
            }
        }

        QueryData queryData =  GenAiDaoHelper.getBceMetricsQuery(enterpriseId, userId, isSuperUser,  request, includeEnterpriseAvg, startMonth, endMonth, group, licensedAi, licensedNonAi, unlicensedAi, unlicensedNonAi, true, false, sortingColumn, sortingMode, false);

        StringBuilder query = OverviewDaoHelperV2.generateQuery(queryData);

        NativeQuery sqlQuery = sessionFactory.getCurrentSession().createNativeQuery(query.toString());
        Map<String, Object> parameters = queryData.getParameters();
        Map<String, Type> scalars = queryData.getScalars();
        SQLUtil.setParameter(sqlQuery, parameters);
        SQLUtil.setScalars(sqlQuery,scalars);

        sqlQuery.setResultTransformer(Transformers.aliasToBean(Metric.class));

        List<Metric> metrics = sqlQuery.list();

        return metrics;
    }

    @Override
    public List<Map<String, Object>> getRollouts(String startMonth, String endMonth, Long enterpriseId){
        String employeeHierarchy = DAOPartitionHelper.getPartition("mv_employee_hierarchy", enterpriseId);
        Map<String, Object> params = new HashMap<>();
        StringBuilder query = new StringBuilder("SELECT ")
                .append("to_char(m, 'YYYY-MM') AS month, \n")
                .append("COALESCE(rc.rollouts, 0) AS rollouts \n")
                .append("FROM generate_series( \n")
                .append("CAST(:startMonth AS DATE), \n")
                .append("CAST(:endMonth AS DATE), \n")
                .append("INTERVAL '1 month' \n")
                .append(") AS m \n")
                .append("LEFT JOIN ( \n")
                .append("SELECT \n")
                .append("date_trunc('month', ts_ml_license_rollout_date) AS rollout_month, \n")
                .append("COUNT(*) AS rollouts \n")
                .append("FROM po_employee \n").append("JOIN ").append(employeeHierarchy).append(" USING (id_employee)")
                .append(" WHERE id_enterprise = :enterpriseId \n")
                .append("AND dt_last_activity >= CAST(:startMonth AS DATE) \n")
                .append("AND ts_ml_license_rollout_date BETWEEN CAST(:startMonth AS DATE) AND CAST(:endMonth AS DATE) \n")
                .append("GROUP BY rollout_month \n")
                .append(") rc ON m = rc.rollout_month \n")
                .append("ORDER BY m ");

        params.put("enterpriseId", enterpriseId);
        params.put("startMonth", startMonth);
        params.put("endMonth", endMonth);

        SQLQuery sqlQuery = sessionFactory.getCurrentSession().createSQLQuery(query.toString());
        SQLUtil.setParameter(sqlQuery, params);
        List<Object[]> results = sqlQuery.list();

        List<Map<String, Object>> monthlyRollouts = new ArrayList<>();
        for (Object[] row : results) {
            Map<String, Object> entry = new HashMap<>();
            entry.put("month", row[0]);
            entry.put("rollouts", row[1]);
            monthlyRollouts.add(entry);
        }

        return monthlyRollouts;
    }
 
    public List<Metric> getBceMetricsTrend(Long enterpriseId, Integer userId, Boolean isSuperUser, GenAIFilterInfo request,
        Map<String, List<Integer>> devLicenseStats, boolean includeEnterpriseAvg, String startMonth, String endMonth, List<OverviewChartGroup> group, boolean isMonthlyTrend, boolean isGroupingCountries){

        List<Integer> licensedAi = devLicenseStats.getOrDefault("licensedAi", Collections.emptyList());
        List<Integer> licensedNonAi = devLicenseStats.getOrDefault("licensedNonAi", Collections.emptyList());
        List<Integer> unlicensedAi = devLicenseStats.getOrDefault("unlicensedAi", Collections.emptyList());
        List<Integer> unlicensedNonAi = devLicenseStats.getOrDefault("unlicensedNonAi", Collections.emptyList());

        QueryData queryData =  GenAiDaoHelper.getBceMetricsQuery(enterpriseId, userId, isSuperUser,  request, includeEnterpriseAvg, startMonth, endMonth, group, licensedAi, licensedNonAi, unlicensedAi, unlicensedNonAi, false, true, null, null, isGroupingCountries);

        StringBuilder query = OverviewDaoHelperV2.generateQuery(queryData);

        if (includeEnterpriseAvg) {
            query.append(" UNION ALL SELECT * FROM enterprise_avg");
        }


        NativeQuery sqlQuery = sessionFactory.getCurrentSession().createNativeQuery(query.toString());
        Map<String, Object> parameters = queryData.getParameters();
        Map<String, Type> scalars = queryData.getScalars();
        SQLUtil.setParameter(sqlQuery, parameters);
        SQLUtil.setScalars(sqlQuery,scalars);

        sqlQuery.setResultTransformer(Transformers.aliasToBean(Metric.class));

        List<Metric> metrics = sqlQuery.list();

        return metrics;
    }

}
