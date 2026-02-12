package com.blueoptima.uix.dao.impl;

import com.blueoptima.uix.common.tld.PRState;
import com.blueoptima.uix.controller.TLDMaintainabilityController;
import com.blueoptima.uix.dao.CodeDeliveryDao;
import com.blueoptima.uix.dao.impl.hibernate.OverviewDaoHelperV2;
import com.blueoptima.uix.dao.impl.hibernate.OverviewDaoHelperV2.QueryData;
import com.blueoptima.uix.dao.impl.hibernate.OverviewTaskDaoHelper;
import com.blueoptima.uix.dao.impl.hibernate.TeamLeadDashboardDaoHelper;
import com.blueoptima.uix.dto.BlueoptimaMetrics;
import com.blueoptima.uix.dto.tld.codeDelivery.CodeDeliveryCommitBean;
import com.blueoptima.uix.dto.tld.codeDelivery.CodeDeliveryCommitWithPRBean;
import com.blueoptima.uix.dto.tld.codeDelivery.CodeDeliveryGraphDAORequest;
import com.blueoptima.uix.dto.tld.compass.DeliverySummaryBean;
import com.blueoptima.uix.util.CollectionUtil;
import com.blueoptima.uix.util.SQLUtil;
import com.blueoptima.uix.dto.tld.compass.TLDTrendViewLevel;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.hibernate.SessionFactory;
import org.hibernate.query.NativeQuery;
import org.hibernate.transform.AliasToBeanResultTransformer;
import org.hibernate.type.BooleanType;
import org.hibernate.type.DateType;
import org.hibernate.type.DoubleType;
import org.hibernate.type.IntegerType;
import org.hibernate.type.LongType;
import org.hibernate.type.StringType;
import org.hibernate.type.Type;
import org.hibernate.transform.Transformers;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.Date;

import static com.blueoptima.uix.dao.impl.TLDCompassDaoImpl.getQueryForCycleTime;

@Repository
public class CodeDeliveryDaoImpl implements CodeDeliveryDao {


  private static final Logger logger = LoggerFactory.getLogger(CodeDeliveryDaoImpl.class);


  @Autowired
  private SessionFactory sessionFactory;

  @Override
  public BlueoptimaMetrics getCycleTime(Long enterpriseId, Integer userId, Boolean superUser, Date startDate, Date endDate,
                                          Integer teamId, List<Integer> developerIds, List<Integer> datasourceIds) {

      StringBuilder plainquery = getQueryForCycleTime(enterpriseId, superUser, null, developerIds, datasourceIds);
      NativeQuery<BlueoptimaMetrics> query = sessionFactory.getCurrentSession().createNativeQuery(plainquery.toString());
      query.addScalar("cycleTime", DoubleType.INSTANCE);
      query.setParameter("userId", userId);
      query.setParameter("teamId", teamId);
      query.setParameter("enterpriseId", enterpriseId);
      query.setParameter("startDate", startDate);
      query.setParameter("endDate", endDate);
        if (CollectionUtil.notNullAndEmpty(developerIds)) {
            query.setParameter("developerIds", developerIds);
        }
        if (CollectionUtil.notNullAndEmpty(datasourceIds)) {
            query.setParameter("datasourceIds", datasourceIds);
        }
      query.setResultTransformer(Transformers.aliasToBean(BlueoptimaMetrics.class));
      return query.uniqueResult();

  }

  @Override
    public Long getCodingTime(Long enterpriseId, Integer userId, Boolean superUser, Date startDate, Date endDate,
                                Integer teamId, List<Integer> developerIds, List<Integer> datasourceIds) {

        StringBuilder plainquery = getQueryForCodingTime(enterpriseId, superUser, null, developerIds, datasourceIds);
        NativeQuery query = sessionFactory.getCurrentSession().createNativeQuery(plainquery.toString());
        query.addScalar("codingTime", LongType.INSTANCE);
        query.setParameter("userId", userId);
        query.setParameter("teamId", teamId);
        query.setParameter("enterpriseId", enterpriseId);
        query.setParameter("startDate", startDate);
        query.setParameter("endDate", endDate);
        if (CollectionUtil.notNullAndEmpty(developerIds)) {
            query.setParameterList("developerIds", developerIds, IntegerType.INSTANCE);
        }
        if (CollectionUtil.notNullAndEmpty(datasourceIds)) {
            query.setParameterList("datasourceIds", datasourceIds, IntegerType.INSTANCE);
        }
        return (Long) query.getSingleResult();
    }

    public static StringBuilder getQueryForCodingTime(Long enterpriseId, Boolean superUser, TLDTrendViewLevel trendViewLevel,
                                                      List<Integer> developerIds, List<Integer> datasourceIds){
      StringBuilder query = new StringBuilder("WITH "+ TeamLeadDashboardDaoHelper.withClauseForTeamChanges());
      query.append(" select\n");

      List<String> projections = new ArrayList<>(List.of(" avg(nu_coding_time) as codingTime\n"));
      query.append(String.join(", ", projections));
        query.append(" from tld.gdop_pr_metrics_info_" + enterpriseId + " gpm\n")
                .append(TeamLeadDashboardDaoHelper.joinClauseForTeamChanges(null, "gpm"))
                .append(!superUser ? " join po_proj_sub_infra ppsi on ppsi.id_infra_instan = gpm.id_infra_instan " + OverviewTaskDaoHelper.ACCESS_JOIN : "")
      .append(trendViewLevel != null ? " where ts_creation_date >= :startDate\n" : " where ts_creation_date between :startDate and :endDate\n")
                .append(" and nu_coding_time >= 0\n")
            .append(" and gpm.nu_state = " + PRState.MERGED.code + "\n")
                .append(" and gpm.id_enterprise = :enterpriseId\n" +
          " and " +
                                (TeamLeadDashboardDaoHelper.whereClauseForTeamChanges(null, "ts_creation_date")) +
                                (!superUser ? OverviewTaskDaoHelper.WC_NON_SU : ""));
          if(CollectionUtil.notNullAndEmpty(developerIds)) {
              query.append(" and gpm.id_employee in (:developerIds)\n");
        }
        if (CollectionUtil.notNullAndEmpty(datasourceIds)) {
            query.append(" and gpm.id_infra_instan in (:datasourceIds)\n");
        }
        return query;
    }

    @Override
    public Integer getFollowUpCommits(Long enterpriseId, Integer userId, Boolean superUser, Date startDate, Date endDate,
                                      Integer teamId, List<Integer> developerIds, List<Integer> datasourceIds) {
        StringBuilder plainquery = getQueryForFollowUpCommits(enterpriseId, superUser, developerIds, datasourceIds);
        NativeQuery query = sessionFactory.getCurrentSession().createNativeQuery(plainquery.toString());
        query.addScalar("followUpCommits", IntegerType.INSTANCE);
        query.setParameter("userId", userId);
        query.setParameter("teamId", teamId);
        query.setParameter("enterpriseId", enterpriseId);
        query.setParameter("startDate", startDate);
        query.setParameter("endDate", endDate);
        if (CollectionUtil.notNullAndEmpty(developerIds)) {
            query.setParameterList("developerIds", developerIds, IntegerType.INSTANCE);
        }
        if (CollectionUtil.notNullAndEmpty(datasourceIds)) {
            query.setParameterList("datasourceIds", datasourceIds, IntegerType.INSTANCE);
        }
        return (Integer) query.getSingleResult();
    }

    public static StringBuilder getQueryForFollowUpCommits(Long enterpriseId, Boolean superUser, List<Integer> developerIds, List<Integer> datasourceIds) {
        StringBuilder query = new StringBuilder("WITH "+TeamLeadDashboardDaoHelper.withClauseForTeamChanges());
        String filters = "";
        if (CollectionUtil.notNullAndEmpty(datasourceIds)) {
            filters += " and gpm.id_infra_instan in (:datasourceIds)\n";
        }
        if (CollectionUtil.notNullAndEmpty(developerIds)) {
            filters += " and gpm.id_employee in (:developerIds)\n";
        }

        query.append(" select avg(nu_follow_up_commits) filter ( where nu_follow_up_commits > 0 ) as followUpCommits\n");
        query.append(" from tld.cycle_time_influencing_metrics_info_"+ enterpriseId +" gpm\n")
                .append(TeamLeadDashboardDaoHelper.joinClauseForTeamChanges(null, "gpm"))
                .append(!superUser ? " join po_proj_sub_infra ppsi on ppsi.id_infra_instan = gpm.id_infra_instan " + OverviewTaskDaoHelper.ACCESS_JOIN : "")
                .append(" where ts_creation_date\\:\\:date between :startDate and :endDate\n")
                .append(" and gpm.nu_state = " + PRState.MERGED.code + "\n")
                .append(" and gpm.id_enterprise = :enterpriseId\n" +
                        filters +
                        " and " +
                        (TeamLeadDashboardDaoHelper.whereClauseForTeamChanges(null, "ts_creation_date"))+
                        (!superUser ? OverviewTaskDaoHelper.WC_NON_SU : ""));

        return query;
    }

    @Override
    public Long getAuthorResTime(Long enterpriseId, Integer userId, Boolean superUser, Date startDate, Date endDate,
                                   Integer teamId, List<Integer> developerIds, List<Integer> datasourceIds) {
        StringBuilder plainquery = getQueryForAuthorResTime(enterpriseId, superUser, developerIds, datasourceIds);
        NativeQuery query = sessionFactory.getCurrentSession().createNativeQuery(plainquery.toString());
        query.addScalar("authorResponseTime", LongType.INSTANCE);
        query.setParameter("userId", userId);
        query.setParameter("teamId", teamId);
        query.setParameter("enterpriseId", enterpriseId);
        query.setParameter("startDate", startDate);
        query.setParameter("endDate", endDate);
        if (CollectionUtil.notNullAndEmpty(developerIds)) {
            query.setParameterList("developerIds", developerIds, IntegerType.INSTANCE);
        }
        if (CollectionUtil.notNullAndEmpty(datasourceIds)) {
            query.setParameterList("datasourceIds", datasourceIds, IntegerType.INSTANCE);
        }
        return (Long) query.getSingleResult();
    }

    public static StringBuilder getQueryForAuthorResTime(Long enterpriseId, Boolean superUser, List<Integer> developerIds, List<Integer> datasourceIds) {
        StringBuilder query = new StringBuilder("WITH "+TeamLeadDashboardDaoHelper.withClauseForTeamChanges());
        String filters = "";
        if (CollectionUtil.notNullAndEmpty(datasourceIds)) {
            filters += " and gpm.id_infra_instan in (:datasourceIds)\n";
        }
        if (CollectionUtil.notNullAndEmpty(developerIds)) {
            filters += " and gpm.id_employee in (:developerIds)\n";
        }

        query.append(" select avg(nu_author_response_time) filter ( where nu_author_response_time > 0 ) as authorResponseTime\n");
        query.append(" from tld.cycle_time_influencing_metrics_info_"+ enterpriseId +" gpm\n")
                .append(TeamLeadDashboardDaoHelper.joinClauseForTeamChanges(null, "gpm"))
                .append(!superUser ? " join po_proj_sub_infra ppsi on ppsi.id_infra_instan = gpm.id_infra_instan " + OverviewTaskDaoHelper.ACCESS_JOIN : "")
                .append(" where ts_creation_date\\:\\:date between :startDate and :endDate\n")
                .append(" and gpm.nu_state = " + PRState.MERGED.code + "\n")
                .append(" and gpm.id_enterprise = :enterpriseId\n" +
                        filters +
                        " and " +
                        (TeamLeadDashboardDaoHelper.whereClauseForTeamChanges(null, "ts_creation_date"))+
                        (!superUser ? OverviewTaskDaoHelper.WC_NON_SU : ""));

        return query;
    }

    @Override
    public Integer getPrParticipant(Long enterpriseId, Integer userId, Boolean superUser, Date startDate, Date endDate,
                                    Integer teamId, List<Integer> developerIds, List<Integer> datasourceIds) {
        StringBuilder plainquery = getQueryForPrParticipant(enterpriseId, superUser, developerIds, datasourceIds);
        NativeQuery query = sessionFactory.getCurrentSession().createNativeQuery(plainquery.toString());
        query.addScalar("prParticipant", IntegerType.INSTANCE);
        query.setParameter("userId", userId);
        query.setParameter("teamId", teamId);
        query.setParameter("startDate", startDate);
        query.setParameter("endDate", endDate);
        if (CollectionUtil.notNullAndEmpty(developerIds)) {
            query.setParameterList("developerIds", developerIds, IntegerType.INSTANCE);
        }
        if (CollectionUtil.notNullAndEmpty(datasourceIds)) {
            query.setParameterList("datasourceIds", datasourceIds, IntegerType.INSTANCE);
        }
        return (Integer) query.getSingleResult();
    }

    public static StringBuilder getQueryForPrParticipant(Long enterpriseId, Boolean superUser, List<Integer> developerIds, List<Integer> datasourceIds) {
        StringBuilder query = new StringBuilder("WITH " + TeamLeadDashboardDaoHelper.withClauseForTeamChanges());
        query.append("SELECT ROUND(AVG(participant_count)) as prParticipant\n" +
                "         FROM (\n" +
                "           SELECT ppa.id_pullrequest,\n" +
                "           COUNT(DISTINCT id_performed_by) AS participant_count\n" +
                "         from daily_refresh.mv_ce_by_pull_request_" + enterpriseId + " pp\n" +
                "         join po_pr_activity_" + enterpriseId + " ppa on id_pullrequest = id_pull_request\n" +
                "         JOIN po_emp_id creator\n" +
                "              ON (pp.id_creator = creator.id_emp_id)\n" +
                TeamLeadDashboardDaoHelper.joinClauseForTeamChanges(null, "creator"));
        query.append(!superUser ? " join po_proj_sub_infra ppsi on ppsi.id_infra_instan = pp.id_infra_instan " + OverviewTaskDaoHelper.ACCESS_JOIN : "")
                .append(" where pp.ts_creation_date\\:\\:date between :startDate and :endDate\n" +
                        "  and ppa.ts_date_performed\\:\\:date between :startDate and :endDate\n" +
                        "  and id_performed_by != pp.id_creator and upper(tx_activity_type) in ('REVIEWED', 'APPROVED', 'UNAPPROVED', 'COMMENT', 'COMMIT', 'COMMENTED', 'COMMITTED')" +
                        "  and nu_state = 3\n" +
                        " and " +
                        (TeamLeadDashboardDaoHelper.whereClauseForTeamChanges(null, "ts_creation_date")) +
                        (!superUser ? OverviewTaskDaoHelper.WC_NON_SU : ""));

        if (CollectionUtil.notNullAndEmpty(developerIds)) {
            query.append(" and team_developers.id_employee in (:developerIds)\n");
                      }
        if (CollectionUtil.notNullAndEmpty(datasourceIds)) {
            query.append(" and pp.id_infra_instan in (:datasourceIds)\n");
        }
        query.append(" group by ppa.id_pullrequest\n");
        query.append(") as participats_per_pr");

        return query;
    }

    @Override
    public BlueoptimaMetrics getPickUpAndResponseMetrics(Long enterpriseId, Integer userId, Boolean superUser, Date startDate, Date endDate,
                                                         Integer teamId, List<Integer> developerIds, List<Integer> datasourceIds) {
        StringBuilder plainquery = getQueryForPickUpAndAuthorResponseTime(enterpriseId, superUser, developerIds, datasourceIds);
        NativeQuery<BlueoptimaMetrics> query = sessionFactory.getCurrentSession().createNativeQuery(plainquery.toString());
        query.addScalar("prPickupTime", DoubleType.INSTANCE);
        query.addScalar("prApprovalToMerge", DoubleType.INSTANCE);
        query.setParameter("userId", userId);
        query.setParameter("teamId", teamId);
        query.setParameter("startDate", startDate);
        query.setParameter("endDate", endDate);
        if (CollectionUtil.notNullAndEmpty(developerIds)) {
            query.setParameterList("developerIds", developerIds, IntegerType.INSTANCE);
        }
        if (CollectionUtil.notNullAndEmpty(datasourceIds)) {
            query.setParameterList("datasourceIds", datasourceIds, IntegerType.INSTANCE);
        }
        query.setResultTransformer(Transformers.aliasToBean(BlueoptimaMetrics.class));
        return query.uniqueResult();
    }

    public static StringBuilder getQueryForPickUpAndAuthorResponseTime(Long enterpriseId, Boolean superUser, List<Integer> developerIds, List<Integer> datasourceIds) {
        StringBuilder query = new StringBuilder("WITH " + TeamLeadDashboardDaoHelper.withClauseForTeamChanges());
        query.append(" select avg(nu_pick_up_time) as prPickupTime,\n" +
                "avg(extract('epoch' from pr.ts_end_date - ts_last_pr_approval_date)) as prApprovalToMerge\n" +
                "from daily_refresh.mv_ce_by_pull_request_" + enterpriseId + " pr\n" +
                "join po_emp_id pe on pe.id_emp_id = pr.id_creator\n");
        query.append(TeamLeadDashboardDaoHelper.joinClauseForTeamChanges(null, "pe"))
                .append(!superUser ? " join po_proj_sub_infra ppsi on ppsi.id_infra_instan = pr.id_infra_instan " + OverviewTaskDaoHelper.ACCESS_JOIN : "")
                .append(" where ts_creation_date\\:\\:date between :startDate and :endDate and nu_state =3\n" +
                              " and " +
                              (TeamLeadDashboardDaoHelper.whereClauseForTeamChanges(null, "ts_creation_date"))+
                              (!superUser ? OverviewTaskDaoHelper.WC_NON_SU : ""));
      if (CollectionUtil.notNullAndEmpty(developerIds)) {
          query.append(" and team_developers.id_employee in (:developerIds)\n");
        }
        if (CollectionUtil.notNullAndEmpty(datasourceIds)) {
          query.append(" and pr.id_infra_instan in (:datasourceIds)\n");
      }

      return query;
  }

  @Override
  public List<CodeDeliveryCommitBean> getCommitsWithinPeriod(CodeDeliveryGraphDAORequest daoRequest, Long enterpriseId, Integer userId, Boolean isSuper) {
    String withTeamClause = "WITH " + TeamLeadDashboardDaoHelper.withClauseForTeamChanges();
    OverviewDaoHelperV2.QueryData queryData = OverviewDaoHelperV2.QueryData.getNewQueryData();

    Map<String, Object> parameters = new HashMap<>();
    Map<String, Type> scalars = this.setProjectionsAndGetScalarsForCommits(queryData);

    String statsTable = DAOPartitionHelper.getPartition("tld.mv_scr_activity_stats_view", enterpriseId.intValue());

    queryData.from.append("from " + statsTable + " rev\n");
    queryData.from.append("join team_developers on team_developers.id_employee = rev.id_employee\n");

    filterCommitsWithinPeriod(queryData, parameters, daoRequest, userId);
    filterCommitsOnUserAccess(queryData, isSuper);
    filterCommitsByInfra(queryData, parameters, daoRequest.getInfraInstanIds());
    filterCommitsByDev(queryData, parameters, daoRequest.getDeveloperIds());

    String rawQuery = withTeamClause + OverviewDaoHelperV2.generateQuery(queryData);
    NativeQuery<CodeDeliveryCommitBean> query = sessionFactory.getCurrentSession().createNativeQuery(rawQuery);

    SQLUtil.setScalars(query, scalars);
    SQLUtil.setParameter(query, parameters);

    query.setResultTransformer(new AliasToBeanResultTransformer(CodeDeliveryCommitBean.class));

    return query.getResultList();
  }

  @Override
  public List<CodeDeliveryCommitWithPRBean> getCommitsWithPRsIncludingOffPeriodCommits(CodeDeliveryGraphDAORequest daoRequest, Long enterpriseId, Integer userId, Boolean isSuper) {
    OverviewDaoHelperV2.QueryData commitsPRs = this.buildCommitPRsQuery(enterpriseId);

    Map<String, Object> parameters = new HashMap<>();
    Map<String, Type> scalars = this.getScalarsForPullRequests();

    filterCommitsWithPrs(commitsPRs, parameters, daoRequest, enterpriseId, userId);
    filterCommitPRsOnUserAccess(commitsPRs, isSuper);
    filterCommitsByInfra(commitsPRs, parameters, daoRequest.getInfraInstanIds());
    filterCommitsByDev(commitsPRs, parameters, daoRequest.getDeveloperIds());

    String withTeamClause = "WITH " + TeamLeadDashboardDaoHelper.withClauseForTeamChanges();
    String commitPRsClause = ",commits_prs as (" + OverviewDaoHelperV2.generateQuery(commitsPRs) + "\n)\n";
    String productionBranchesClause = ",production_branches as (" + getProductionBranchesQuery() + "\n)\n";
    String selectClause = buildSelectForCommitsWithPRs();

    String rawQuery = withTeamClause + commitPRsClause + productionBranchesClause + selectClause;

    NativeQuery<CodeDeliveryCommitWithPRBean> query = sessionFactory.getCurrentSession().createNativeQuery(rawQuery);

    SQLUtil.setScalars(query, scalars);
    SQLUtil.setParameter(query, parameters);
    query.setResultTransformer(new AliasToBeanResultTransformer(CodeDeliveryCommitWithPRBean.class));

    return query.getResultList();
  }

  @Override
  public DeliverySummaryBean getDeliverySummary(CodeDeliveryGraphDAORequest daoRequest, Long enterpriseId, Integer userId, Boolean isSuperUser) {
    Map<String, Object> parameters = new HashMap<>();

    QueryData enterpriseSummary = queryForEnterpriseSummary(enterpriseId);
    QueryData teamSummary = queryForTeamSummary(enterpriseId);

    filterSummaryByUserAccess(enterpriseSummary, isSuperUser);
    filterSummaryByInfraInstanIds(enterpriseSummary, parameters, daoRequest.getInfraInstanIds());

    filterSummaryByUserAccess(teamSummary, isSuperUser);
    filterSummaryByEmployeeIds(teamSummary, parameters, daoRequest.getDeveloperIds());
    filterSummaryByInfraInstanIds(teamSummary, parameters, daoRequest.getInfraInstanIds());

    String withTeamClause = "WITH " + TeamLeadDashboardDaoHelper.withClauseForTeamChanges();
    String withDeliveryBranches = ",deliveryBranches as (" + getProductionBranchesQuery() + "\n)\n";
    String withEnterpriseSummary = ",enterpriseAverage as (" + OverviewDaoHelperV2.generateQuery(enterpriseSummary) + "\n)\n";
    String withTeamSummary = ",teamAverage as (" + OverviewDaoHelperV2.generateQuery(teamSummary) + "\n)\n";

    StringBuilder queryBuilder = new StringBuilder(withTeamClause)
        .append(withDeliveryBranches)
        .append(withEnterpriseSummary)
        .append(withTeamSummary)
        .append(getSelectForTeamDeliverySummary());

    logger.debug("Query : " + queryBuilder);

    NativeQuery<DeliverySummaryBean> query = sessionFactory.getCurrentSession().createNativeQuery(queryBuilder.toString());

    query.addScalar("enterpriseCodingTime", LongType.INSTANCE);
    query.addScalar("enterpriseReviewTime", LongType.INSTANCE);
    query.addScalar("enterpriseMergeLeadTime", LongType.INSTANCE);
    query.addScalar("enterpriseApprovalToMergeTime", LongType.INSTANCE);
    query.addScalar("enterpriseDeliveryTime", LongType.INSTANCE);
    query.addScalar("enterprisePickUpTime", LongType.INSTANCE);
    query.addScalar("teamCodingTime", LongType.INSTANCE);
    query.addScalar("teamReviewTime", LongType.INSTANCE);
    query.addScalar("teamMergeLeadTime", LongType.INSTANCE);
    query.addScalar("teamApprovalToMergeTime", LongType.INSTANCE);
    query.addScalar("teamDeliveryTime", LongType.INSTANCE);
    query.addScalar("teamPickUpTime", LongType.INSTANCE);

    parameters.put("userId", userId);
    parameters.put("teamId", daoRequest.getTeamId());
    parameters.put("enterpriseId", enterpriseId);
    parameters.put("startDate", daoRequest.getStartDate());
    parameters.put("endDate", daoRequest.getEndDate());

    SQLUtil.setParameter(query, parameters);

    query.setResultTransformer(new AliasToBeanResultTransformer(DeliverySummaryBean.class));

    return query.getSingleResult();
  }

  private Map<String, Type> setProjectionsAndGetScalarsForCommits(OverviewDaoHelperV2.QueryData queryData) {
    queryData.projectionList.add("rev.id_employee as employeeId");
    queryData.projectionList.add("rev.tx_revision as commitHash");
    queryData.projectionList.add("rev.nu_actual_ce as commitACEHours");
    queryData.projectionList.add("rev.nu_actual_aberrant_ce as commitAberrantACEHours");
    queryData.projectionList.add("rev.id_tseries as commitDate");
    queryData.projectionList.add("rev.id_infra_instan as infraInstanId");
    queryData.projectionList.add("bl_excluded as isExcluded");

    Map<String, Type> scalars = new HashMap<>();

    scalars.put("employeeId", IntegerType.INSTANCE);
    scalars.put("commitHash", StringType.INSTANCE);
    scalars.put("commitACEHours", DoubleType.INSTANCE);
    scalars.put("commitAberrantACEHours", DoubleType.INSTANCE);
    scalars.put("commitDate", DateType.INSTANCE);
    scalars.put("infraInstanId", LongType.INSTANCE);
    scalars.put("isExcluded", BooleanType.INSTANCE);

    return scalars;
  }

  private Map<String, Type> getScalarsForPullRequests() {
    Map<String, Type> scalars = new HashMap<>();

    scalars.put("employeeId", IntegerType.INSTANCE);
    scalars.put("commitHash", StringType.INSTANCE);
    scalars.put("commitACEHours", DoubleType.INSTANCE);
    scalars.put("commitAberrantACEHours", DoubleType.INSTANCE);
    scalars.put("commitDate", DateType.INSTANCE);
    scalars.put("infraInstanId", LongType.INSTANCE);
    scalars.put("pullRequestId", IntegerType.INSTANCE);
    scalars.put("prState", IntegerType.INSTANCE);
    scalars.put("prCreatedAt", DateType.INSTANCE);
    scalars.put("lastApprovedAt", DateType.INSTANCE);
    scalars.put("prPickupTime", LongType.INSTANCE);
    scalars.put("prReviewTime", LongType.INSTANCE);
    scalars.put("prCodingTime", LongType.INSTANCE);
    scalars.put("prMergeTime", LongType.INSTANCE);
    scalars.put("prApprovalToMergeTime", LongType.INSTANCE);
    scalars.put("prDestinationBranch", StringType.INSTANCE);
    scalars.put("prFollowUpCommits", IntegerType.INSTANCE);
    scalars.put("prAuthorResponseTime", DoubleType.INSTANCE);
    scalars.put("prSize", DoubleType.INSTANCE);
    scalars.put("prParticipant", IntegerType.INSTANCE);
    scalars.put("dataSourceProductionBranch", StringType.INSTANCE);
    scalars.put("isExcluded", BooleanType.INSTANCE);
    scalars.put("prSourceBranch", StringType.INSTANCE);

    return scalars;
  }

  private void filterCommitsOnUserAccess(OverviewDaoHelperV2.QueryData queryData, Boolean isSuper) {
    if (!isSuper) {
      queryData.from.append(OverviewTaskDaoHelper.SUB_INFRA_JOIN);
      queryData.from.append(OverviewTaskDaoHelper.ACCESS_JOIN);
      queryData.whereClause.append(OverviewTaskDaoHelper.WC_NON_SU);
    }
  }

  private void filterCommitPRsOnUserAccess(OverviewDaoHelperV2.QueryData queryData, Boolean isSuper) {
    if (!isSuper) {
      queryData.from.append(" join po_proj_sub_infra sub on sub.id_infra_instan = pr.id_infra_instan");
      queryData.from.append(OverviewTaskDaoHelper.ACCESS_JOIN);
      queryData.whereClause.append(OverviewTaskDaoHelper.WC_NON_SU);
    }
  }

  private void filterCommitsWithinPeriod(OverviewDaoHelperV2.QueryData queryData, Map<String, Object> parameters, CodeDeliveryGraphDAORequest daoRequest, Integer userId) {
    queryData.whereClause.append("where rev.id_tseries >= :startDate\n");
    queryData.whereClause.append("and rev.id_tseries < (:endDate\\:\\:date + INTERVAL '1 day')\n");
    queryData.whereClause.append("and rev.id_tseries >= team_developers.ts_start_date\n");
    queryData.whereClause.append("and rev.id_tseries <= team_developers.ts_end_date\n");

    parameters.put("userId", userId);
    parameters.put("startDate", daoRequest.getStartDate());
    parameters.put("endDate", daoRequest.getEndDate());
    parameters.put("teamId", daoRequest.getTeamId());
  }

  private void filterCommitsWithPrs(OverviewDaoHelperV2.QueryData queryData, Map<String, Object> parameters, CodeDeliveryGraphDAORequest daoRequest, Long enterpriseId, Integer userId) {
    queryData.whereClause.append("where pr.nu_state in (3, 2, 1)\n");
    queryData.whereClause.append("and pr.ts_creation_date >= :startDate\n");
    queryData.whereClause.append("and pr.ts_creation_date < (:endDate\\:\\:date + INTERVAL '1 day')\n");
    queryData.whereClause.append("and pr.ts_creation_date >= team_developers.ts_start_date\n");
    queryData.whereClause.append("and pr.ts_creation_date <= team_developers.ts_end_date\n");

    parameters.put("userId", userId);
    parameters.put("enterpriseId", enterpriseId);
    parameters.put("startDate", daoRequest.getStartDate());
    parameters.put("endDate", daoRequest.getEndDate());
    parameters.put("teamId", daoRequest.getTeamId());
  }

  private void filterCommitsByInfra(OverviewDaoHelperV2.QueryData queryData, Map<String, Object> parameters, List<Integer> infraInstanIds) {
    if (CollectionUtil.notNullAndEmpty(infraInstanIds)) {
      queryData.whereClause.append("and rev.id_infra_instan in (:infraInstanIds)");
      parameters.put("infraInstanIds", infraInstanIds);
    }
  }

  private void filterCommitsByDev(OverviewDaoHelperV2.QueryData queryData, Map<String, Object> parameters, List<Integer> developerIds) {
    if (CollectionUtil.notNullAndEmpty(developerIds)) {
      queryData.whereClause.append("and rev.id_employee in (:developerIds)");
      parameters.put("developerIds", developerIds);
    }
  }

  private void setProjectionsForCommitPRsQuery(OverviewDaoHelperV2.QueryData queryData) {
    queryData.projectionList.add("pr.id_pull_request");
    queryData.projectionList.add("pr.nu_state");
    queryData.projectionList.add("pr.ts_creation_date");
    queryData.projectionList.add("pr.ts_end_date");
    queryData.projectionList.add("pr.nu_pick_up_time");
    queryData.projectionList.add("pr.ts_last_pr_approval_date");
    queryData.projectionList.add("pr.nu_review_time");
    queryData.projectionList.add("pr.tx_destination_branch");
    queryData.projectionList.add("pr.nu_actual_development_ce");
    queryData.projectionList.add("pr.nu_actual_reworked_ce");
    queryData.projectionList.add("rev.id_employee");
    queryData.projectionList.add("rev.tx_revision");
    queryData.projectionList.add("rev.nu_actual_ce");
    queryData.projectionList.add("rev.nu_actual_aberrant_ce");
    queryData.projectionList.add("rev.id_infra_instan");
    queryData.projectionList.add("rev.id_tseries");
    queryData.projectionList.add("bl_excluded");
    queryData.projectionList.add("pr.tx_source_branch");
    queryData.projectionList.add("gpr.nu_coding_time");
    queryData.projectionList.add("gpr.nu_merge_time");
    queryData.projectionList.add("cycle.nu_author_response_time");
    queryData.projectionList.add("cycle.nu_follow_up_commits");
    queryData.projectionList.add("part.participantIds");
  }

  private OverviewDaoHelperV2.QueryData buildCommitPRsQuery(Long enterpriseId) {
    OverviewDaoHelperV2.QueryData commitPRsQuery = OverviewDaoHelperV2.QueryData.getNewQueryData();

    this.setProjectionsForCommitPRsQuery(commitPRsQuery);

    String pullRequestTable = DAOPartitionHelper.getPartition("daily_refresh.mv_ce_by_pull_request", enterpriseId);
    String prCommitTable = DAOPartitionHelper.getPartition("po_pr_commit", enterpriseId);
    String revisionsTable = DAOPartitionHelper.getPartition("tld.mv_scr_activity_stats_view", enterpriseId);
    String gdopInfoTable = DAOPartitionHelper.getPartition("tld.gdop_pr_metrics_info", enterpriseId);
    String cycleTimeMetricsTable = DAOPartitionHelper.getPartition("tld.cycle_time_influencing_metrics_info", enterpriseId);
    String prActivityTable = DAOPartitionHelper.getPartition("po_pr_activity", enterpriseId);
    String ceByPullRequestTable = DAOPartitionHelper.getPartition("daily_refresh.mv_ce_by_pull_request", enterpriseId);


    commitPRsQuery.withClause.append(
        "\n participants as (\n"
            + "select id_pullrequest, array_agg(distinct id_performed_by) as participantIds from " + prActivityTable + " prr\n"
            + "join " + ceByPullRequestTable + " mvpr on mvpr.id_pull_request = prr.id_pullrequest\n"
            + "where id_performed_by != mvpr.id_creator and upper(tx_activity_type) in ('REVIEWED', 'APPROVED', 'UNAPPROVED', 'COMMENT', 'COMMIT', 'COMMENTED', 'COMMITTED')\n"
            + "group by id_pullrequest\n"
            + ")"
    );

    commitPRsQuery.from.append("from " + pullRequestTable + " pr\n");
    commitPRsQuery.from.append("join " + prCommitTable + " prc on prc.id_pullrequest = pr.id_pull_request\n");
    commitPRsQuery.from.append("join " + revisionsTable + " rev on cast(md5(rev.tx_revision) as UUID) = cast(md5(prc.tx_revision) as UUID)\n");
    commitPRsQuery.from.append("and rev.id_infra_instan = pr.id_infra_instan\n");
    commitPRsQuery.from.append("left join " + gdopInfoTable + " gpr on gpr.id_pull_request = pr.id_pull_request\n");
    commitPRsQuery.from.append("left join " + cycleTimeMetricsTable +" cycle on cycle.id_pull_request = pr.id_pull_request\n");
    commitPRsQuery.from.append("left join participants part on part.id_pullrequest = pr.id_pull_request\n");
    commitPRsQuery.from.append("join team_developers on team_developers.id_employee = rev.id_employee\n");

    return commitPRsQuery;
  }

  public String getProductionBranchesQuery() {
    OverviewDaoHelperV2.QueryData teamBranches = OverviewDaoHelperV2.QueryData.getNewQueryData();
    teamBranches.projectionList.add("id_infra_instan");
    teamBranches.projectionList.add("tx_delivery_branch as delivery_branch");
    teamBranches.from.append("from po_tld_delivery_branch_configuration\n");
    teamBranches.whereClause.append("where id_enterprise = :enterpriseId and id_team = :teamId\n");

    OverviewDaoHelperV2.QueryData defaultBranches = OverviewDaoHelperV2.QueryData.getNewQueryData();
    defaultBranches.projectionList.add("id_infra_instan");
    defaultBranches.projectionList.add("tx_delivery_branch as delivery_branch");
    defaultBranches.from.append("from po_tld_delivery_branches\n");
    defaultBranches.whereClause.append("where id_enterprise = :enterpriseId\n");
    defaultBranches.whereClause.append("and id_infra_instan not in (select id_infra_instan from po_tld_delivery_branch_configuration where id_enterprise = :enterpriseId and id_team = :teamId)\n");

    StringBuilder teamQuery = OverviewDaoHelperV2.generateQuery(teamBranches);
    StringBuilder defaultsQuery = OverviewDaoHelperV2.generateQuery(defaultBranches);

    return teamQuery
        .append("\n union all \n")
        .append(defaultsQuery)
        .toString();
  }

  private String buildSelectForCommitsWithPRs() {
    OverviewDaoHelperV2.QueryData selectQueryData = OverviewDaoHelperV2.QueryData.getNewQueryData();

    selectQueryData.projectionList.add("id_employee as employeeId");
    selectQueryData.projectionList.add("tx_revision as commitHash");
    selectQueryData.projectionList.add("nu_actual_ce as commitACEHours");
    selectQueryData.projectionList.add("nu_actual_aberrant_ce as commitAberrantACEHours");
    selectQueryData.projectionList.add("id_tseries as commitDate");
    selectQueryData.projectionList.add("id_infra_instan as infraInstanId");
    selectQueryData.projectionList.add("id_pull_request as pullRequestId");
    selectQueryData.projectionList.add("nu_state as prState");
    selectQueryData.projectionList.add("ts_creation_date as prCreatedAt");
    selectQueryData.projectionList.add("case when nu_pick_up_time > 0 then nu_pick_up_time else null end as prPickupTime");
    selectQueryData.projectionList.add("ts_last_pr_approval_date as lastApprovedAt");
    selectQueryData.projectionList.add("case when nu_review_time > 0 then nu_review_time else null end as prReviewTime");
    selectQueryData.projectionList.add("case when nu_coding_time > 0 then nu_coding_time else null end as prCodingTime");
    selectQueryData.projectionList.add("case when nu_merge_time > 0 then nu_merge_time else null end as prMergeTime");
    selectQueryData.projectionList.add("case when ts_last_pr_approval_date is not null then extract (epoch from ts_end_date - ts_last_pr_approval_date) end as prApprovalToMergeTime");
    selectQueryData.projectionList.add("tx_destination_branch as prDestinationBranch");
    selectQueryData.projectionList.add("delivery_branch as dataSourceProductionBranch");
    selectQueryData.projectionList.add("bl_excluded as isExcluded");
    selectQueryData.projectionList.add("tx_source_branch as prSourceBranch");
    selectQueryData.projectionList.add("nu_follow_up_commits as prFollowUpCommits");
    selectQueryData.projectionList.add("nu_author_response_time as prAuthorResponseTime");
    selectQueryData.projectionList.add("array_length(participantIds,1) as prParticipant");
    selectQueryData.projectionList.add("CASE WHEN nu_actual_development_ce IS NULL AND nu_actual_reworked_ce IS NULL THEN NULL ELSE COALESCE(nu_actual_development_ce, 0) + COALESCE(nu_actual_reworked_ce, 0) END AS prSize");

    selectQueryData.from.append("from commits_prs left join production_branches using (id_infra_instan)");

    return OverviewDaoHelperV2.generateQuery(selectQueryData).toString();
  }

  private QueryData queryForTeamSummary(Long enterpriseId) {
    QueryData query = QueryData.getNewQueryData();

    query.projectionList.add("avg(nu_coding_time) filter (where nu_coding_time > 0) as codingTime");
    query.projectionList.add("avg(nu_pick_up_time) filter (where nu_pick_up_time > 0) as pickUpTime");
    query.projectionList.add("avg(nu_review_time) filter (where nu_review_time > 0) as reviewTime");
    query.projectionList.add("avg(nu_merge_time) filter (where nu_merge_time > 0 and gpr.ts_end_date >= :startDate and gpr.ts_end_date <= :endDate) as mergeLeadTime");
    query.projectionList.add("avg(nu_approval_merge_time) filter (where nu_approval_merge_time > 0 and gpr.ts_last_pr_approval_date >= :startDate and gpr.ts_last_pr_approval_date <= :endDate) as approvalToMergeTime");
    query.projectionList.add("avg(case when deliveryBranches.delivery_branch is not null then nu_delivery_time end) filter (where nu_delivery_time > 0) as deliveryTime");

    String gdopPRTable = DAOPartitionHelper.getPartition("tld.gdop_pr_metrics_info", enterpriseId);
    String mvPRTable = DAOPartitionHelper.getPartition("daily_refresh.mv_ce_by_pull_request", enterpriseId);

    query.from.append("from " + gdopPRTable + " gpr\n");
    query.from.append("join team_developers using (id_employee)\n");
    query.from.append("join " + mvPRTable + " mvpr using (id_pull_request)\n");
    query.from.append(
        "left join deliveryBranches\n"
        + "            on mvpr.id_infra_instan = deliveryBranches.id_infra_instan\n"
        + "            and mvpr.tx_destination_branch = deliveryBranches.delivery_branch\n"
    );

    query.whereClause.append("where mvpr.nu_state = " + PRState.MERGED.code + " \n");
    query.whereClause.append("and gpr.id_employee in (select id_employee from team_developers)\n");
    query.whereClause.append("and gpr.ts_creation_date >= :startDate\n");
    query.whereClause.append("and gpr.ts_creation_date <= :endDate\n");
    query.whereClause.append("and gpr.ts_creation_date >= team_developers.ts_start_date\n");
    query.whereClause.append("and gpr.ts_creation_date < team_developers.ts_end_date\n");

    return query;
  }

  private QueryData queryForEnterpriseSummary(Long enterpriseId) {
    QueryData query = QueryData.getNewQueryData();

    query.projectionList.add("avg(nu_coding_time) filter (where nu_coding_time > 0) as codingTime");
    query.projectionList.add("avg(nu_pick_up_time) filter (where nu_pick_up_time > 0) as pickUpTime");
    query.projectionList.add("avg(nu_review_time) filter (where nu_review_time > 0)  as reviewTime");
    query.projectionList.add("avg(nu_merge_time) filter (where nu_merge_time > 0 and gpr.ts_end_date >= :startDate and gpr.ts_end_date <= :endDate) as mergeLeadTime");
    query.projectionList.add("avg(nu_approval_merge_time) filter (where nu_approval_merge_time > 0 and gpr.ts_last_pr_approval_date >= :startDate and gpr.ts_last_pr_approval_date <= :endDate) as approvalToMergeTime");
    query.projectionList.add("avg(case when deliveryBranches.delivery_branch is not null then nu_delivery_time end) as deliveryTime");

    String gdopPRTable = DAOPartitionHelper.getPartition("tld.gdop_pr_metrics_info", enterpriseId);
    String mvPRTable = DAOPartitionHelper.getPartition("daily_refresh.mv_ce_by_pull_request", enterpriseId);

    query.from.append("from " + gdopPRTable + " gpr\n");
    query.from.append("join " + mvPRTable + " mvpr using (id_pull_request)\n");
    query.from.append(
        "left join deliveryBranches\n"
            + "            on mvpr.id_infra_instan = deliveryBranches.id_infra_instan\n"
            + "            and mvpr.tx_destination_branch = deliveryBranches.delivery_branch\n"
    );

    query.whereClause.append("where mvpr.nu_state = " + PRState.MERGED.code + "\n");
    query.whereClause.append("and gpr.ts_creation_date >= :startDate\n");
    query.whereClause.append("and gpr.ts_creation_date >= :endDate\n");

    return query;
  }

  public void filterSummaryByUserAccess(QueryData query, Boolean isSuper) {
    if (!isSuper) {
      query.from.append(" join po_proj_sub_infra sub on sub.id_infra_instan = gpr.id_infra_instan");
      query.from.append(OverviewTaskDaoHelper.ACCESS_JOIN);
      query.whereClause.append(OverviewTaskDaoHelper.WC_NON_SU);
    }
  }

  public void filterSummaryByInfraInstanIds(QueryData query, Map<String, Object> parameters, List<Integer> infraInstanIds) {
    if (CollectionUtil.notNullAndEmpty(infraInstanIds)) {
      query.whereClause.append("and gpr.id_infra_instan in (:infraInstanIds)");
      parameters.put("infraInstanIds", infraInstanIds);
    }
  }

  public void filterSummaryByEmployeeIds(QueryData query, Map<String, Object> parameters, List<Integer> employeeIds) {
    if (CollectionUtil.notNullAndEmpty(employeeIds)) {
      query.whereClause.append("and gpr.id_employee in (:employeeIds)");
      parameters.put("employeeIds", employeeIds);
    }
  }

  private String getSelectForTeamDeliverySummary() {
    return
        "select\n"
            + "    enterpriseAverage.codingTime as enterpriseCodingTime,\n"
            + "    enterpriseAverage.pickUpTime as enterprisePickUpTime,\n"
            + "    enterpriseAverage.reviewTime as enterpriseReviewTime,\n"
            + "    enterpriseAverage.mergeLeadTime as enterpriseMergeLeadTime,\n"
            + "    enterpriseAverage.approvalToMergeTime as enterpriseApprovalToMergeTime,\n"
            + "    enterpriseAverage.deliveryTime as enterpriseDeliveryTime,"
            + "    teamAverage.codingTime as teamCodingTime,\n"
            + "    teamAverage.pickUpTime as teamPickUpTime,\n"
            + "    teamAverage.reviewTime as teamReviewTime,\n"
            + "    teamAverage.mergeLeadTime as teamMergeLeadTime,\n"
            + "    teamAverage.approvalToMergeTime as teamApprovalToMergeTime,\n"
            + "    teamAverage.deliveryTime as teamDeliveryTime\n"
            + "from enterpriseAverage cross join teamAverage";
  }
}