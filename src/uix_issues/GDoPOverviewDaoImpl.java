package com.blueoptima.uix.dao.impl;

import com.blueoptima.uix.common.UserContext;
import com.blueoptima.uix.common.tld.PRState;
import com.blueoptima.uix.common.tld.TLDExecutiveOverviewCategory;
import com.blueoptima.uix.common.tld.TLDExecutiveOverviewGrouping;
import com.blueoptima.uix.common.tld.TLDExecutiveOverviewTrendType;
import com.blueoptima.uix.dao.GDoPOverviewDao;
import com.blueoptima.uix.dao.impl.hibernate.OverviewDaoHelperV2;
import com.blueoptima.uix.dao.impl.hibernate.OverviewDaoHelperV2.QueryData;
import com.blueoptima.uix.dto.tld.compass.GDOPGroupDaoRequest;
import com.blueoptima.uix.dto.tld.compass.GDOPGroupQueryBCEResult;
import com.blueoptima.uix.dto.tld.compass.GDOPGroupQueryResult;
import com.blueoptima.uix.util.CollectionUtil;
import com.blueoptima.uix.util.SQLUtil;
import lombok.RequiredArgsConstructor;
import org.hibernate.SessionFactory;
import org.hibernate.query.NativeQuery;
import org.hibernate.transform.AliasToBeanResultTransformer;
import org.hibernate.type.DateType;
import org.hibernate.type.DoubleType;
import org.hibernate.type.LongType;
import org.hibernate.type.StringType;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Repository
@RequiredArgsConstructor
public class GDoPOverviewDaoImpl implements GDoPOverviewDao {

    private final SessionFactory sessionFactory;

    @Override
    public List<GDOPGroupQueryResult> getCycleTimeTrend(GDOPGroupDaoRequest daoRequest, Long enterpriseId, Integer userId, Boolean isSuper) {
        TLDExecutiveOverviewTrendType trendType = daoRequest.getTrendType();
        TLDExecutiveOverviewGrouping grouping = daoRequest.getGrouping();
        TLDExecutiveOverviewCategory category = grouping.category;

        Map<String, Object> parameters = new HashMap<>();
        QueryData query = QueryData.getNewQueryData();

        String filteringBy = TLDExecutiveOverviewCategory.ORG_HIERARCHY.equals(category) ?
            filtersByHierarchy(daoRequest, enterpriseId, parameters)
            : filtersByTeamStructure(daoRequest, enterpriseId, parameters);

        query.withClause.append(filteringBy);

        addWeeklyTrendScalars(query.projectionList, trendType, "ts_end_date");
        query.projectionList.add(grouping.idColumn + " as groupId");
        query.projectionList.add(grouping.nameColumn + " as groupName");

        String gdopPrMetrics = DAOPartitionHelper.getPartition("tld.gdop_pr_metrics_info", enterpriseId);
        query.from.append("from ").append(gdopPrMetrics).append(" mainTable\n");
        query.whereClause.append("where ts_creation_date >= :startDate and ts_end_date <= :endDate and mainTable.nu_state = " + PRState.MERGED.code + "\n");

        if (TLDExecutiveOverviewCategory.TEAM_STRUCTURE.equals(category)) {
            query.projectionList.add("avg(nu_cycle_time) filter (where ts_creation_date >= struct.joiningDate and (struct.leavingDate is null or ts_creation_date <= struct.leavingDate)) as value");
            query.projectionList.add("count(nu_cycle_time) filter (where ts_creation_date >= struct.joiningDate and (struct.leavingDate is null or ts_creation_date <= struct.leavingDate)) as totalEntries");
            query.from.append("join filtering_by_team_struct struct using (id_employee)\n");
        } else {
            query.projectionList.add("avg(nu_cycle_time) as value");
            query.projectionList.add("count(nu_cycle_time) as totalEntries");
            query.from.append("join filtering_by_hierarchy struct using (id_infra_instan)\n");

            List<Long> developerIds = daoRequest.getDeveloperIds();
            if (CollectionUtil.notNullAndEmpty(developerIds)) {
                query.whereClause.append("and id_employee in (:developerIds)\n");
                parameters.put("developerIds", developerIds);
            }
        }

        checkUserAccess("mainTable",query,parameters);

        query.groupList.add("startDate, groupId, groupName");
        query.limitsAndOrder.append("order by startDate");

        String rawQuery = OverviewDaoHelperV2.generateQuery(query).toString();
        NativeQuery<GDOPGroupQueryResult> nativeQuery = sessionFactory.getCurrentSession().createNativeQuery(rawQuery);

        addScalars(nativeQuery);

        if(grouping.equals(TLDExecutiveOverviewGrouping.EVERYTHING)){
            parameters.put("enterpriseId", enterpriseId);
        }
        parameters.put("startDate", daoRequest.getStartDate());
        parameters.put("endDate", daoRequest.getEndDate());
        SQLUtil.setParameter(nativeQuery, parameters);

        nativeQuery.setResultTransformer(new AliasToBeanResultTransformer(GDOPGroupQueryResult.class));
        return nativeQuery.getResultList();
    }

    @Override
    public List<GDOPGroupQueryBCEResult> getBillableCETrend(GDOPGroupDaoRequest daoRequest, Long enterpriseId, Integer userId, Boolean isSuper) {
        TLDExecutiveOverviewTrendType trendType = daoRequest.getTrendType();
        TLDExecutiveOverviewGrouping grouping = daoRequest.getGrouping();
        TLDExecutiveOverviewCategory category = grouping.category;

        Map<String, Object> parameters = new HashMap<>();
        QueryData query = QueryData.getNewQueryData();

        String filteringBy = TLDExecutiveOverviewCategory.ORG_HIERARCHY.equals(category) ?
                filtersByHierarchy(daoRequest, enterpriseId, parameters)
                : filtersByTeamStructure(daoRequest, enterpriseId, parameters);

        query.withClause.append(filteringBy);

        addWeeklyTrendScalars(query.projectionList, trendType, "id_tseries");
        query.projectionList.add(grouping.idColumn + " as groupId");
        query.projectionList.add(grouping.nameColumn + " as groupName");

        query.from.append("from daily_refresh.mv_calendar mainTable\n");
        query.whereClause.append("where id_tseries >= :startDate and id_tseries <= :endDate\n");

        if (TLDExecutiveOverviewCategory.TEAM_STRUCTURE.equals(category)) {
            query.projectionList.add("sum(nu_bce) filter (where id_tseries >= struct.joiningDate and (struct.leavingDate is null or id_tseries <= struct.leavingDate)) as billableCE");
            query.projectionList.add("sum(nu_aberrant_ce) filter (where id_tseries >= struct.joiningDate and (struct.leavingDate is null or id_tseries <= struct.leavingDate)) as aberrantBCE");
            query.projectionList.add("count(distinct (mainTable.id_employee, mainTable.id_tseries)) filter (where id_tseries >= struct.joiningDate and (struct.leavingDate is null or id_tseries <= struct.leavingDate)) as totalEntries");
            query.from.append("join filtering_by_team_struct struct using (id_employee)\n");
        } else {
            query.projectionList.add("sum(nu_bce) as billableCE");
            query.projectionList.add("sum(nu_aberrant_ce) as aberrantBCE");
            query.projectionList.add("count(distinct (mainTable.id_employee, mainTable.id_tseries)) as totalEntries");
            query.from.append("join filtering_by_hierarchy struct using (id_infra_instan)\n");

            List<Long> developerIds = daoRequest.getDeveloperIds();
            if (CollectionUtil.notNullAndEmpty(developerIds)) {
                query.whereClause.append("and id_employee in (:developerIds)\n");
                parameters.put("developerIds", developerIds);
            }
        }

        checkUserAccess("mainTable",query,parameters);

        query.groupList.add("startDate, groupId, groupName");
        query.limitsAndOrder.append("order by startDate");

        String rawQuery = OverviewDaoHelperV2.generateQuery(query).toString();
        NativeQuery<GDOPGroupQueryBCEResult> nativeQuery = sessionFactory.getCurrentSession().createNativeQuery(rawQuery);

        nativeQuery.addScalar("startDate", DateType.INSTANCE);
        nativeQuery.addScalar("endDate", DateType.INSTANCE);
        nativeQuery.addScalar("groupId", LongType.INSTANCE);
        nativeQuery.addScalar("groupName", StringType.INSTANCE);
        nativeQuery.addScalar("billableCE", DoubleType.INSTANCE);
        nativeQuery.addScalar("aberrantBCE", DoubleType.INSTANCE);
        nativeQuery.addScalar("totalEntries", LongType.INSTANCE);

        if(grouping.equals(TLDExecutiveOverviewGrouping.EVERYTHING)){
            parameters.put("enterpriseId", enterpriseId);
        }
        parameters.put("startDate", daoRequest.getStartDate());
        parameters.put("endDate", daoRequest.getEndDate());
        SQLUtil.setParameter(nativeQuery, parameters);

        nativeQuery.setResultTransformer(new AliasToBeanResultTransformer(GDOPGroupQueryBCEResult.class));
        return nativeQuery.getResultList();
    }

    @Override
    public List<GDOPGroupQueryResult> getInterPrTrend(GDOPGroupDaoRequest daoRequest, Long enterpriseId, Integer userId, Boolean isSuper) {
        TLDExecutiveOverviewTrendType trendType = daoRequest.getTrendType();
        TLDExecutiveOverviewGrouping grouping = daoRequest.getGrouping();
        TLDExecutiveOverviewCategory category = grouping.category;

        Map<String, Object> parameters = new HashMap<>();
        QueryData query = QueryData.getNewQueryData();

        String filteringBy = TLDExecutiveOverviewCategory.ORG_HIERARCHY.equals(category) ?
            filtersByHierarchy(daoRequest, enterpriseId, parameters)
            : filtersByTeamStructure(daoRequest, enterpriseId, parameters);

        String filterName = TLDExecutiveOverviewCategory.ORG_HIERARCHY.equals(category) ?
            "filtering_by_hierarchy"
            : "filtering_by_team_struct";

        query.withClause
            .append(filteringBy).append(",\n")
            .append(buildDayWisePRCTE(grouping,filterName, enterpriseId, category, parameters)).append(",\n")
            .append(buildNextDayWisePRCTE(grouping)).append(",\n")
            .append(buildEmployeeHolidayCTE()).append(",\n")
            .append(buildEmployeeHolidaysFilter()).append(",\n")
            .append(buildDayDifferenceCTE(grouping)).append("\n");

        addProjectionsForTrendType(trendType, query);
        query.projectionList.add(grouping.idColumn + " as groupId");
        query.projectionList.add(grouping.nameColumn + " as groupName");
        query.projectionList.add("SUM(dayDifference) / SUM(dist_day) as value");
        query.projectionList.add("COUNT(*) as totalEntries");

        query.from.append("FROM dayDifferenceCTE");
        if(TLDExecutiveOverviewCategory.ORG_HIERARCHY.equals(category)){
            query.from.append("\n").append("JOIN ").append(filterName).append(" f ").append("USING (id_infra_instan)\n");
        }
        List<Long> developerIds = daoRequest.getDeveloperIds();
        if (CollectionUtil.notNullAndEmpty(developerIds)) {
            query.whereClause.append(" WHERE id_employee IN ( :developerIds )\n");
            parameters.put("developerIds", developerIds);
        }
        query.groupList.add("startDate, groupId, groupName");
        query.limitsAndOrder.append("ORDER BY startDate");

        parameters.put("enterpriseId", enterpriseId);
        parameters.put("startDate", daoRequest.getStartDate());
        parameters.put("endDate", daoRequest.getEndDate());

        String rawQuery = OverviewDaoHelperV2.generateQuery(query).toString();
        NativeQuery<GDOPGroupQueryResult> nativeQuery = sessionFactory.getCurrentSession().createNativeQuery(rawQuery);

        addScalars(nativeQuery);
        SQLUtil.setParameter(nativeQuery, parameters);

        nativeQuery.setResultTransformer(new AliasToBeanResultTransformer(GDOPGroupQueryResult.class));
        return nativeQuery.getResultList();
    }

    @Override
    public List<GDOPGroupQueryResult> getIntraPrTrend(GDOPGroupDaoRequest daoRequest, Long enterpriseId, Integer userId, Boolean isSuper) {
        TLDExecutiveOverviewTrendType trendType = daoRequest.getTrendType();
        TLDExecutiveOverviewGrouping grouping = daoRequest.getGrouping();
        TLDExecutiveOverviewCategory category = grouping.category;

        Map<String, Object> parameters = new HashMap<>();
        QueryData query = QueryData.getNewQueryData();

        String filteringBy = TLDExecutiveOverviewCategory.ORG_HIERARCHY.equals(category) ?
            filtersByHierarchy(daoRequest, enterpriseId, parameters)
            : filtersByTeamStructure(daoRequest, enterpriseId, parameters);

        String filterName = TLDExecutiveOverviewCategory.ORG_HIERARCHY.equals(category) ?
            "filtering_by_hierarchy"
            : "filtering_by_team_struct";

        query.withClause
            .append(filteringBy).append(",\n")
            .append(buildIntraPrCTE(grouping,enterpriseId,filterName,parameters)).append(",\n")
            .append(buildEmployeeHolidayCTE()).append(",\n")
            .append(buildEmployeeHolidaysFilter()).append(",\n")
            .append(buildIntraPrCountCTE(grouping)).append("\n");

        query.from.append("FROM intraPrCountCTE\n");
        if(TLDExecutiveOverviewCategory.ORG_HIERARCHY.equals(category)){
            query.from.append("\n").append("JOIN ")
                .append(filterName).append(" f ")
                .append("USING (id_infra_instan)\n");
        }
        addProjectionsForTrendType(trendType, query);
        query.projectionList.add(grouping.idColumn + " as groupId");
        query.projectionList.add(grouping.nameColumn + " as groupName");
        query.projectionList.add("COUNT(*) as totalEntries");
        query.projectionList.add("AVG(minutesDifference) / 60 AS value");

        List<Long> developerIds = daoRequest.getDeveloperIds();
        if (CollectionUtil.notNullAndEmpty(developerIds)) {
            query.whereClause.append(" WHERE id_employee IN ( :developerIds )\n");
            parameters.put("developerIds", developerIds);
        }
        query.groupList.add("startDate, groupId, groupName");
        query.limitsAndOrder.append("ORDER BY startDate");

        parameters.put("enterpriseId", enterpriseId);
        parameters.put("startDate", daoRequest.getStartDate());
        parameters.put("endDate", daoRequest.getEndDate());

        String rawQuery = OverviewDaoHelperV2.generateQuery(query).toString();
        NativeQuery<GDOPGroupQueryResult> nativeQuery = sessionFactory.getCurrentSession().createNativeQuery(rawQuery);

        addScalars(nativeQuery);
        SQLUtil.setParameter(nativeQuery, parameters);

        nativeQuery.setResultTransformer(new AliasToBeanResultTransformer(GDOPGroupQueryResult.class));
        return nativeQuery.getResultList();
    }

    @Override
    public List<GDOPGroupQueryResult> getCommitFrequencyTrend(GDOPGroupDaoRequest daoRequest, Long enterpriseId, Integer userId, Boolean isSuper) {
        TLDExecutiveOverviewTrendType trendType = daoRequest.getTrendType();
        TLDExecutiveOverviewGrouping grouping = daoRequest.getGrouping();
        TLDExecutiveOverviewCategory category = grouping.category;

        Map<String, Object> parameters = new HashMap<>();
        QueryData query = QueryData.getNewQueryData();

        String filteringBy = TLDExecutiveOverviewCategory.ORG_HIERARCHY.equals(category) ?
            filtersByHierarchy(daoRequest, enterpriseId, parameters)
            : filtersByTeamStructure(daoRequest, enterpriseId, parameters);

        String filterName = TLDExecutiveOverviewCategory.ORG_HIERARCHY.equals(category) ?
            "filtering_by_hierarchy"
            : "filtering_by_team_struct";

        query.withClause.append(filteringBy).append(",\n")
                .append(buildDayWiseDataCTE(category, grouping, enterpriseId, parameters, filterName)).append(",\n")
                .append(buildNextDayWiseDataCTE(grouping)).append(",\n")
                .append(buildLastDayOfMonthRemovalFilterCTE()).append(",\n")
                .append(buildEmployeeHolidayCTE()).append(",\n")
                .append(buildEmployeeHolidaysFilter()).append(",\n")
                .append(buildDayDifferenceCTECommit(grouping));

        addProjectionsForTrendType(trendType, query);
        query.projectionList.add(grouping.idColumn + " as groupId");
        query.projectionList.add(grouping.nameColumn + " as groupName");
        query.projectionList.add("SUM(dayDifference) / SUM(dist_day) AS value");
        query.projectionList.add("COUNT(*) as totalEntries");

        query.from.append("FROM dayDifferenceCTE");
        if(TLDExecutiveOverviewCategory.ORG_HIERARCHY.equals(category)){
            query.from.append("\n").append("JOIN ")
                .append(filterName).append(" f ")
                .append("USING (id_infra_instan)\n");
        }
        List<Long> developerIds = daoRequest.getDeveloperIds();
        if (CollectionUtil.notNullAndEmpty(developerIds)) {
            query.whereClause.append(" WHERE id_employee IN ( :developerIds )\n");
            parameters.put("developerIds", developerIds);
        }
        query.groupList.add("startDate, groupId, groupName");
        query.limitsAndOrder.append("ORDER BY startDate");

        String rawQuery = OverviewDaoHelperV2.generateQuery(query).toString();
        NativeQuery<GDOPGroupQueryResult> nativeQuery = sessionFactory.getCurrentSession().createNativeQuery(rawQuery);

        addScalars(nativeQuery);

        parameters.put("enterpriseId", enterpriseId);
        parameters.put("startDate", daoRequest.getStartDate());
        parameters.put("endDate", daoRequest.getEndDate());

        SQLUtil.setParameter(nativeQuery, parameters);
        nativeQuery.setResultTransformer(new AliasToBeanResultTransformer(GDOPGroupQueryResult.class));

        return nativeQuery.getResultList();
    }

    private String buildDayWiseDataCTE(TLDExecutiveOverviewCategory category, TLDExecutiveOverviewGrouping grouping, Long enterpriseId, Map<String, Object> parameters, String filterName) {
        QueryData query = QueryData.getNewQueryData();

        query.projectionList.add("DISTINCT mainTable.id_tseries");
        query.projectionList.add("mainTable.id_employee");
        query.from.append("FROM tld.mv_scr_activity_stats_view_").append(enterpriseId).append(" mainTable\n");

        if (TLDExecutiveOverviewCategory.TEAM_STRUCTURE.equals(category)) {
            query.projectionList.add(filterName+"."+grouping.idColumn);
            query.projectionList.add(filterName+"."+grouping.nameColumn);
            query.from.append("JOIN ").append(filterName).append(" using (id_employee)");
        } else {
            query.projectionList.add("mainTable.id_infra_instan");
        }

        query.whereClause.append("WHERE (nu_actual_ce > 0.0 OR bl_excluded = false)\n" +
                "          AND id_tseries >= :startDate AND id_tseries <= :endDate \n");

        if (TLDExecutiveOverviewCategory.TEAM_STRUCTURE.equals(category)) {
            query.whereClause.append("AND id_tseries >= ")
                .append(filterName+".joiningDate").append(" and id_tseries < ").append(filterName+".leavingDate");
        }
        checkUserAccess("mainTable", query, parameters);

        StringBuilder builder = OverviewDaoHelperV2.generateQuery(query);

        builder.insert(0, "dayWiseData as (\n");
        builder.append(")\n");

        return builder.toString();
    }

    private String buildNextDayWiseDataCTE(TLDExecutiveOverviewGrouping grouping) {
        QueryData query = QueryData.getNewQueryData();

        query.projectionList.add("id_tseries AS day");
        query.projectionList.add("id_employee");
        query.projectionList.add("LEAD(id_tseries, 1) OVER (PARTITION BY id_employee ORDER BY id_tseries) AS latest_day");
        if (grouping.category.equals(TLDExecutiveOverviewCategory.ORG_HIERARCHY)) {
            query.projectionList.add("id_infra_instan");
        } else {
            query.projectionList.add(grouping.idColumn);
            query.projectionList.add(grouping.nameColumn);
        }

        query.from.append("FROM dayWiseData");

        StringBuilder builder = OverviewDaoHelperV2.generateQuery(query);

        builder.insert(0, "nextDayWiseData AS (\n");
        builder.append(")\n");

        return builder.toString();
    }

    private String buildLastDayOfMonthRemovalFilterCTE() {
        return " lastDayOfMonthRemovalFilter AS (\n" +
                "        SELECT *\n" +
                "        FROM nextDayWiseData\n" +
                "        WHERE extract(month FROM day) = extract(month FROM latest_day)\n" +
                "    )";
    }

    private String buildDayDifferenceCTECommit(TLDExecutiveOverviewGrouping grouping) {
        QueryData query = QueryData.getNewQueryData();

        query.projectionList.add("lastDayOfMonthRemovalFilter.id_employee");
        query.projectionList.add("day");
        query.projectionList.add("COUNT(DISTINCT day) AS dist_day");
        query.projectionList.add("(latest_day\\:\\:date - day\\:\\:date - COUNT(DISTINCT dt_holiday)) AS dayDifference\n");
        if (grouping.category.equals(TLDExecutiveOverviewCategory.ORG_HIERARCHY)) {
            query.projectionList.add("id_infra_instan");
        } else {
            query.projectionList.add(grouping.idColumn);
            query.projectionList.add(grouping.nameColumn);
        }

        query.from.append("FROM lastDayOfMonthRemovalFilter\n" +
                "                LEFT JOIN\n" +
                "            employeeHolidaysFilter ON lastDayOfMonthRemovalFilter.id_employee = employeeHolidaysFilter.id_employee\n" +
                "                AND employeeHolidaysFilter.dt_holiday > lastDayOfMonthRemovalFilter.day\n" +
                "                AND employeeHolidaysFilter.dt_holiday < latest_day");

        if (grouping.category.equals(TLDExecutiveOverviewCategory.ORG_HIERARCHY)) {
            query.groupList.add("lastDayOfMonthRemovalFilter.id_employee,\n" +
                "            latest_day,\n" +
                "            day,\n" +
                "            id_infra_instan\n");
        }   else {
            query.groupList.add("lastDayOfMonthRemovalFilter.id_employee,\n" +
                "            latest_day,\n" +
                "            day,\n" +
                "            " + grouping.idColumn + ",\n" +
                "            " + grouping.nameColumn);
        }

        StringBuilder builder = OverviewDaoHelperV2.generateQuery(query);

        builder.insert(0, "dayDifferenceCTE as (\n");
        builder.append(")\n");

        return builder.toString();
    }

    private String filtersByTeamStructure(GDOPGroupDaoRequest daoRequest, Long enterpriseId, Map<String, Object> parameters) {
        StringBuilder builder = new StringBuilder();
        List<Long> teamIds = daoRequest.getTeamIds();

        builder.append("filtering_by_team_struct as (\n");
        builder.append("select conf.id_team, id_employee, ts_start_date as joiningDate, COALESCE(ts_end_date, NOW()\\:\\:date) as leavingDate, tx_team_name\n");
        builder.append("from po_tld_team_configuration conf join po_tld_team using (id_team)\n");
        builder.append("where conf.id_enterprise = :enterpriseId\n");

        if (CollectionUtil.notNullAndEmpty(teamIds)) {
            builder.append("and id_team in (:teamIds)\n");
            parameters.put("teamIds", teamIds);
        }

        parameters.put("enterpriseId", enterpriseId);

        builder.append(")\n");

        return builder.toString();
    }

    private String filtersByHierarchy(GDOPGroupDaoRequest daoRequest, Long enterpriseId, Map<String, Object> parameters) {
        QueryData query = QueryData.getNewQueryData();

        List<Long> organizationIds = daoRequest.getOrganizationIds();
        List<Long> projectIds = daoRequest.getProjectIds();
        List<Long> collectionIds = daoRequest.getCollectionIds();
        List<Long> datasourceIds = daoRequest.getDatasourceIds();

        query.projectionList.add("id_organization");
        query.projectionList.add("id_project");
        query.projectionList.add("id_project_sub");
        query.projectionList.add("id_infra_instan");

        if (!TLDExecutiveOverviewGrouping.DEVELOPERS.equals(daoRequest.getGrouping()) &&
                !TLDExecutiveOverviewGrouping.DATASOURCES.equals(daoRequest.getGrouping())) {
            query.projectionList.add(daoRequest.getGrouping().nameColumn);
        }

        query.from.append("from ").append("po_organization join po_project using (id_organization) " +
                "join po_project_sub using (id_project) join po_proj_sub_infra using (id_project_sub)").append("\n");

        query.whereClause.append("WHERE po_organization.id_enterprise = ").append(enterpriseId).append("\n");

        addFilterIfIdsProvided(query, parameters, organizationIds, TLDExecutiveOverviewGrouping.ORGANIZATIONS);
        addFilterIfIdsProvided(query, parameters, projectIds, TLDExecutiveOverviewGrouping.PROJECTS);
        addFilterIfIdsProvided(query, parameters, collectionIds, TLDExecutiveOverviewGrouping.COLLECTIONS);
        addFilterIfIdsProvided(query, parameters, datasourceIds, TLDExecutiveOverviewGrouping.DATASOURCES);

        StringBuilder builder = OverviewDaoHelperV2.generateQuery(query);

        builder.insert(0, "filtering_by_hierarchy as (\n");
        builder.append(")\n");

        return builder.toString();
    }

    private void addWeeklyTrendScalars(List<String> projections, TLDExecutiveOverviewTrendType trendType, String timeColumn) {
        if (TLDExecutiveOverviewTrendType.WEEKLY.equals(trendType)) {
            projections.add("date_trunc(" + TLDExecutiveOverviewTrendType.WEEKLY.getValueForSQL() + ", mainTable." + timeColumn + ") as startDate");
            projections.add("(date_trunc(" + TLDExecutiveOverviewTrendType.WEEKLY.getValueForSQL() + ", mainTable." + timeColumn + ") + interval '6 days') as endDate");
        } else {
            projections.add("date_trunc(" + TLDExecutiveOverviewTrendType.MONTHLY.getValueForSQL() + ", mainTable." + timeColumn + ") as startDate");
            projections.add("(date_trunc(" + TLDExecutiveOverviewTrendType.MONTHLY.getValueForSQL() + ", mainTable." + timeColumn + ") + interval '1 month - 1 day') as endDate");
        }
    }

    private void addProjectionsForTrendType(TLDExecutiveOverviewTrendType trendType, QueryData query) {
        query.projectionList.add("date_trunc(" + trendType.getValueForSQL() + ", day) as startDate");
        if (TLDExecutiveOverviewTrendType.WEEKLY.equals(trendType)) {
            query.projectionList.add("date_trunc('week', day) + INTERVAL '6 days' as endDate");
        } else {
            query.projectionList.add("date_trunc('month', day) + INTERVAL '1 month' - INTERVAL '1 day' as endDate");
        }
    }

    private void addScalars(NativeQuery<GDOPGroupQueryResult> nativeQuery){
        nativeQuery.addScalar("startDate", DateType.INSTANCE);
        nativeQuery.addScalar("endDate", DateType.INSTANCE);
        nativeQuery.addScalar("groupId", LongType.INSTANCE);
        nativeQuery.addScalar("groupName", StringType.INSTANCE);
        nativeQuery.addScalar("value", DoubleType.INSTANCE);
        nativeQuery.addScalar("totalEntries", LongType.INSTANCE);
    }

    private String buildDayWisePRCTE(TLDExecutiveOverviewGrouping grouping, String filterName, Long enterpriseId, TLDExecutiveOverviewCategory category,Map<String, Object> parameters) {
        String dailyRefreshTable = DAOPartitionHelper.getPartition("daily_refresh.mv_ce_by_pull_request", enterpriseId);
        QueryData qd = QueryData.getNewQueryData();

        qd.projectionList.add("DISTINCT po_emp_id.id_employee");
        if (category.equals(TLDExecutiveOverviewCategory.TEAM_STRUCTURE)) {
            qd.projectionList.add(filterName+"."+grouping.idColumn);
            qd.projectionList.add(filterName+"."+grouping.nameColumn);
        } else {
            qd.projectionList.add("pop.id_infra_instan");
        }

        qd.projectionList.add("ts_creation_date\\:\\:date AS ts_creation_date");
        qd.from.append("FROM ").append(dailyRefreshTable).append(" pop\n")
            .append("JOIN po_emp_id ON pop.id_creator = po_emp_id.id_emp_id\n");

        if (category.equals(TLDExecutiveOverviewCategory.TEAM_STRUCTURE)) {
            qd.from
                .append("JOIN ").append(filterName)
                .append(" ON ")
                .append(filterName).append(".id_employee = po_emp_id.id_employee ");
        }
        qd.whereClause
            .append("WHERE ts_creation_date >= :startDate\n")
            .append("  AND ts_creation_date <= :endDate\n");

        if (category.equals(TLDExecutiveOverviewCategory.TEAM_STRUCTURE)) {
            qd.whereClause
                .append("  AND (ts_creation_date >= ")
                .append(filterName).append(".joiningDate")
                .append(" AND ts_creation_date < ")
                .append(filterName).append(".leavingDate)\n");
        }
        qd.limitsAndOrder.append("ORDER BY ts_creation_date");

        checkUserAccess("pop",qd,parameters);

        String sql = OverviewDaoHelperV2.generateQuery(qd).toString();
        return "dayWisePR as (\n" + sql + "\n)";
    }


    private String buildNextDayWisePRCTE(TLDExecutiveOverviewGrouping grouping) {
        QueryData qd = QueryData.getNewQueryData();

        qd.projectionList.add("id_employee");
        if (grouping.category.equals(TLDExecutiveOverviewCategory.TEAM_STRUCTURE)) {
            qd.projectionList.add(grouping.idColumn);
            qd.projectionList.add(grouping.nameColumn);
        } else {
            qd.projectionList.add("id_infra_instan");
        }
        qd.projectionList.add("ts_creation_date AS day");
        qd.projectionList.add("LEAD(ts_creation_date, 1) OVER (PARTITION BY id_employee ORDER BY ts_creation_date)\\:\\:timestamp AS latest_day");
        qd.from.append("FROM dayWisePR");
        String sql = OverviewDaoHelperV2.generateQuery(qd).toString();
        return "nextDayWisePR as (\n" + sql + "\n)";
    }

    private String buildEmployeeHolidayCTE() {
        QueryData qd = QueryData.getNewQueryData();
        qd.projectionList.add("id_employee");
        qd.projectionList.add("UNNEST(arr_holidays) AS dt_holiday");
        qd.from.append("FROM employee_holidays");
        qd.whereClause.append(" WHERE employee_holidays.id_enterprise = :enterpriseId");
        String sql = OverviewDaoHelperV2.generateQuery(qd).toString();
        return "employeeHoliday as (\n" + sql + "\n)";
    }

    private String buildEmployeeHolidaysFilter() {
        QueryData qd = QueryData.getNewQueryData();
        qd.projectionList.add("id_employee");
        qd.projectionList.add("dt_holiday");
        qd.from.append("FROM employeeHoliday");
        qd.whereClause.append(" WHERE employeeHoliday.dt_holiday BETWEEN :startDate and :endDate");
        String sql = OverviewDaoHelperV2.generateQuery(qd).toString();
        return "employeeHolidaysFilter as (\n" + sql + "\n)";
    }

    private String buildDayDifferenceCTE(TLDExecutiveOverviewGrouping grouping) {
        QueryData qd = QueryData.getNewQueryData();

        qd.projectionList.add("ndwp.id_employee");
        if (grouping.category.equals(TLDExecutiveOverviewCategory.TEAM_STRUCTURE)) {
            qd.projectionList.add("ndwp."+grouping.idColumn);
            qd.projectionList.add("ndwp."+grouping.nameColumn);
        } else {
            qd.projectionList.add("ndwp.id_infra_instan");
        }
        qd.projectionList.add("ndwp.day");
        qd.projectionList.add("COUNT(DISTINCT ndwp.day) AS dist_day");
        qd.projectionList.add("(ndwp.latest_day\\:\\:date - ndwp.day\\:\\:date - COUNT(DISTINCT eh.dt_holiday)) AS dayDifference");

        qd.from.append("FROM nextDayWisePR ndwp\n")
            .append("LEFT JOIN employeeHolidaysFilter eh ON ndwp.id_employee = eh.id_employee\n")
            .append("     AND eh.dt_holiday > ndwp.day\n")
            .append("     AND eh.dt_holiday < ndwp.latest_day\n");
        if (grouping.category.equals(TLDExecutiveOverviewCategory.TEAM_STRUCTURE)) {
            qd.groupList.add("ndwp.id_employee, ndwp.latest_day, ndwp.day, "+"ndwp."+grouping.idColumn+", ndwp."+grouping.nameColumn);
        } else{
            qd.groupList.add("ndwp.id_employee, ndwp.latest_day, ndwp.day, ndwp.id_infra_instan");
        }
        String sql = OverviewDaoHelperV2.generateQuery(qd).toString();
        return "dayDifferenceCTE as (\n" + sql + "\n)";
    }

    public String buildIntraPrCTE(TLDExecutiveOverviewGrouping grouping, Long enterpriseId, String filterName, Map<String, Object> parameters){
        QueryData qd = QueryData.getNewQueryData();

        if (grouping.category.equals(TLDExecutiveOverviewCategory.ORG_HIERARCHY)) {
            qd.projectionList.add("pp.id_infra_instan");
        } else {
            qd.projectionList.add(filterName+"."+grouping.idColumn);
            qd.projectionList.add(filterName+"."+grouping.nameColumn);
        }
        qd.projectionList.add("pei.id_employee");
        qd.projectionList.add("ppr.id_pullrequest");
        qd.projectionList.add("ts_creation_date\\:\\:date AS ts_creation_date");
        qd.projectionList.add("ts_date_performed");
        qd.projectionList.add("LAG(ts_date_performed, 1) OVER (\n" +
            "             PARTITION BY id_pullrequest\n" +
            "             ORDER BY ts_date_performed\n" +
            "             ) AS prev_activity");

        String dailyRefreshMCPullRequestTable = DAOPartitionHelper.getPartition("daily_refresh.mv_ce_by_pull_request", enterpriseId);
        String prActivityTable = DAOPartitionHelper.getPartition("po_pr_activity", enterpriseId);
        String poEmpTable = "po_emp_id";

        qd.from.append("FROM ").append(dailyRefreshMCPullRequestTable).append(" pp\n")
            .append("JOIN ").append(prActivityTable).append(" ppr ").append("ON id_pullrequest = id_pull_request\n")
            .append("JOIN ").append(poEmpTable).append(" pei ").append("ON ppr.id_performed_by = pei.id_emp_id\n");

        if (grouping.category.equals(TLDExecutiveOverviewCategory.TEAM_STRUCTURE)) {
            qd.from.append("JOIN ").append(filterName).append(" ON ").append(filterName).append(".id_employee = pei.id_employee\n");
        }
        qd.whereClause.append("WHERE ppr.id_enterprise = :enterpriseId\n")
            .append("AND ts_creation_date >= :startDate\n")
            .append("AND ts_creation_date <= :endDate\n");
        if (grouping.category.equals(TLDExecutiveOverviewCategory.TEAM_STRUCTURE)) {
            qd.whereClause.append("AND ts_creation_date >= ").append(filterName).append(".joiningDate\n")
                .append("AND ts_creation_date < ").append(filterName).append(".leavingDate\n");
        }

        checkUserAccess("pp",qd,parameters);

        String sql = OverviewDaoHelperV2.generateQuery(qd).toString();
        return "intraPrCTE as (\n" + sql + "\n)";
    }

    public String buildIntraPrCountCTE(TLDExecutiveOverviewGrouping grouping){
        QueryData qd = QueryData.getNewQueryData();

        if (grouping.category.equals(TLDExecutiveOverviewCategory.ORG_HIERARCHY)) {
            qd.projectionList.add("id_infra_instan");
        } else {
            qd.projectionList.add(grouping.idColumn);
            qd.projectionList.add(grouping.nameColumn);
        }
        qd.projectionList.add("pr.id_employee");
        qd.projectionList.add("id_pullrequest");
        qd.projectionList.add("ts_creation_date AS day");
        qd.projectionList.add("((EXTRACT(EPOCH FROM ts_date_performed - prev_activity) / 60) - (count(distinct dt_holiday) * 24 * 60))  AS minutesDifference\n");

        qd.from.append("FROM intraPrCTE pr \n")
            .append("LEFT JOIN employeeHolidaysFilter eh ON pr.id_employee = eh.id_employee\n")
            .append("AND eh.dt_holiday > pr.prev_activity\n")
            .append("AND eh.dt_holiday < pr.ts_date_performed\n");

        qd.whereClause.append(" WHERE pr.prev_activity IS NOT NULL");

        if (grouping.category.equals(TLDExecutiveOverviewCategory.TEAM_STRUCTURE)) {
            qd.groupList.add("id_pullrequest, day, pr.id_employee, ts_date_performed, prev_activity, "+grouping.idColumn+", "+grouping.nameColumn);
        } else{
            qd.groupList.add("id_pullrequest, id_infra_instan, day, pr.id_employee, ts_date_performed, prev_activity");
        }

        String sql = OverviewDaoHelperV2.generateQuery(qd).toString();
        return "intraPrCountCTE as (\n" + sql + "\n)";
    }

    private void addFilterIfIdsProvided(QueryData queryData, Map<String, Object> parameters, List<Long> filterByIds, TLDExecutiveOverviewGrouping grouping) {
        if (CollectionUtil.notNullAndEmpty(filterByIds)) {
            if (queryData.whereClause.toString().isEmpty()) {
                queryData.whereClause.append("WHERE ").append(grouping.idColumn).append(" IN (:").append(grouping.commonName).append(")\n");
            } else {
                queryData.whereClause.append("AND ").append(grouping.idColumn).append(" IN (:").append(grouping.commonName).append(")\n");
            }

            parameters.put(grouping.commonName, filterByIds);
        }
    }

    private void checkUserAccess(String tableAliasName, QueryData query, Map<String, Object> parameters){
        Boolean isSuperUser = UserContext.getUserToken().getDataAccess().isSuperUser();
        if (!isSuperUser) {
            query.from.append(" JOIN po_proj_sub_infra sub ON sub.id_infra_instan = ")
                .append(tableAliasName)
                .append(".id_infra_instan");
            query.from.append("\n JOIN mv_user_access mvua ON mvua.id_project_sub = sub.id_project_sub\n");
            query.whereClause.append("\n AND mvua.id_user = :userId");
            parameters.put("userId", UserContext.getUserToken().getUserId());
        }
    }
}