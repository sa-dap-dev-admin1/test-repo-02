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

package com.blueoptima.uix.dao.impl.hibernate;

import com.blueoptima.uix.common.InfrastructureType;
import com.blueoptima.uix.common.SprintState;
import com.blueoptima.uix.common.UserContext;
import com.blueoptima.uix.common.tld.PRState;
import com.blueoptima.uix.dao.TeamLeadDashboardDao;
import com.blueoptima.uix.dao.beans.tld.*;
import com.blueoptima.uix.dao.model.hibernate.TLDOnboardingModel;
import com.blueoptima.uix.dao.model.hibernate.TLDTeamConfigModel;
import com.blueoptima.uix.dao.type.hibernate.ArrayUserType;
import com.blueoptima.uix.dto.BlueoptimaMetrics;
import com.blueoptima.uix.dto.MissingActivitySources;
import com.blueoptima.uix.dto.SprintDto;
import com.blueoptima.uix.dto.tld.CombinedTeamAndDevelopers;
import com.blueoptima.uix.dto.tld.TLDOverviewResponse;
import com.blueoptima.uix.dto.tld.TLDSprintBean;
import com.blueoptima.uix.util.CollectionUtil;
import java.util.List;
import java.util.Objects;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;

import com.blueoptima.uix.util.SQLUtil;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.query.NativeQuery;
import org.hibernate.query.Query;
import org.hibernate.transform.AliasToBeanResultTransformer;
import org.hibernate.transform.Transformers;
import org.hibernate.type.BooleanType;
import org.hibernate.type.CustomType;
import org.hibernate.type.DateType;
import org.hibernate.type.DoubleType;
import org.hibernate.type.IntegerType;
import org.hibernate.type.LongType;
import org.hibernate.type.StringType;
import org.hibernate.type.TimestampType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import javax.persistence.criteria.Predicate;
import java.util.*;
import java.util.stream.Stream;

import static com.blueoptima.uix.dao.impl.hibernate.OverviewTaskDaoHelper.TASK_JOIN;

/*
 * Author: Seervide on: 23/07/2021
 * */

@Repository
public class TeamLeadDashboardDaoImpl implements TeamLeadDashboardDao {

  @Autowired
  private SessionFactory sessionFactory;

  @Override
  @SuppressWarnings(value = "unchecked")
  public TLDOverviewResponse getCEMetricsForTLD(List<Integer> taskProjectIds,
                                                List<Integer> sprintIds, Long entId, Integer userId,
                                                Boolean superUser, List<Integer> developers, Long teamId, List<Integer> dataSourceIds, Date startDate, Date endDate) {

    Map<String, Object> params = new HashMap<>();
    StringBuilder withClause = getWithClauseForHistory();
    params.put("userId",userId);
    params.put("teamId",teamId);
    StringBuilder whereClause = new StringBuilder(
            "where ce_sprint.id_enterprise = :enterpriseId\n");
    params.put("enterpriseId",entId);
    TeamLeadDashboardDaoHelper.applyFilters(whereClause,params,sprintIds,developers,taskProjectIds, dataSourceIds, startDate, endDate);
    StringBuilder outerMetricQuery = new StringBuilder("select \n" +
            "       sum(nu_bce) / count(distinct (ce_sprint.id_employee, ce_sprint.id_tseries)) as avgBce,\n" +
            "       sum(nu_actual_ce) as ceHours,\n"+
            "       count(distinct ce_sprint.id_employee) as developerCount,\n"+
            "       count(distinct (ce_sprint.id_employee, ce_sprint.id_tseries)) as activeDays,\n" +
            "       CASE\n" +
            "           when sum(nu_actual_ce) > 0 then\n" +
            "               sum(nu_actual_aberrant_ce) / sum(nu_actual_ce) * 100\n" +
            "           ELSE\n" +
            "               0.0 END             as pctAbCeHours\n, " +
            "       CASE when sum(nu_bce) > 0 then sum(nu_aberrant_ce)/sum(nu_bce) " +
            "           ELSE null END            as pctAbBce\n");
    outerMetricQuery.append(" from daily_refresh.mv_task_by_day ce_sprint\n" );
    if (!superUser) {
      outerMetricQuery.append(" JOIN po_proj_sub_infra using (id_infra_instan)\n"+
      " JOIN mv_user_access using (id_project_sub)\n");
      whereClause.append(" and mv_user_access.id_user = :userId\n");
    }
    outerMetricQuery.append(
            "JOIN task.mv_task_details USING (id_task)\n" +
                    "JOIN task.sprint_details sd USING (id_sprint)\n" +
            "JOIN team_developers on team_developers.id_employee = ce_sprint.id_employee\n");
    outerMetricQuery.append(whereClause);
    withClause.append(outerMetricQuery);

    // gets only ce metrics
    NativeQuery nativeQuery = sessionFactory.getCurrentSession().createNativeQuery(withClause.toString());
    SQLUtil.setParameter(nativeQuery, params);
    nativeQuery.addScalar("pctAbCeHours", DoubleType.INSTANCE);
    nativeQuery.addScalar("pctAbBce", DoubleType.INSTANCE);
    nativeQuery.addScalar("avgBce", DoubleType.INSTANCE);
    nativeQuery.addScalar("ceHours", DoubleType.INSTANCE);
    nativeQuery.addScalar("developerCount", IntegerType.INSTANCE);
    nativeQuery.addScalar("activeDays", IntegerType.INSTANCE);
    nativeQuery.setResultTransformer(new AliasToBeanResultTransformer(TLDOverviewResponse.class));
    return (TLDOverviewResponse) nativeQuery.getSingleResult();

  }

  @Override
  @SuppressWarnings(value = "unchecked")
  public List<TLDSprintBean> getSprintInfo(Long enterpriseId,Integer userId,Boolean superUser, List<Integer> taskProjectIds,
      Integer lastSelectedSprints,
      List<Integer> developerIds, Long teamId) {

    String activitySprintTable = getSprintCETable(enterpriseId);

    StringBuilder with = getWithClauseForHistory();

    StringBuilder select = new StringBuilder(
            "select                         ce_sprint.id_sprint      as sprintId,\n" +
            "                               sd.tx_name               as name,\n" +
            "                               sd.ts_start_date\\:\\:date   as startDate,\n" +
            "                               sd.ts_end_date\\:\\:date     as endDate,\n" +
            "                               ts_completion_date\\:\\:date as completionDate,\n" +
            "                               ts_activated_date        as activationDate,\n" +
            "                               sum(nu_actual_ce)        as totalCEHours" +
            "  from " + activitySprintTable + " ce_sprint\n");

    StringBuilder whereClause = new StringBuilder(" where ce_sprint.id_enterprise = :enterpriseId\n" +
            "  and sd.nu_state = :sprintState\n" +
            "  and nu_actual_ce > 0\n" +
            "  and (sd.ts_start_date < team_developers.ts_end_date and sd.ts_end_date >= team_developers.ts_start_date)\n");

    if(!superUser) {
      select.append(" JOIN po_proj_sub_infra using (id_infra_instan)\n");
      select.append(" JOIN mv_user_access using (id_project_sub)\n");
      whereClause.append(" and mv_user_access.id_user = :userId\n");
    }
    select.append("JOIN task.sprint_details sd USING (id_sprint)\n" +
            "  JOIN task.mv_task_details_" + enterpriseId + " task_details USING (id_task)\n");
    select.append(" JOIN team_developers on team_developers.id_employee = ce_sprint.id_employee\n");

    if(CollectionUtil.notNullAndEmpty(taskProjectIds)){
      whereClause.append(" and task_details.id_project in (:taskProjectIds)\n");
    }
    if(CollectionUtil.notNullAndEmpty(developerIds)){
      whereClause.append(" and ce_sprint.id_employee in (:developerIds)\n");
    }

    with.append(select).append(whereClause).append("GROUP BY sprintId, name, startDate, endDate, completionDate, activationDate, id_sprint\n"+
            " ORDER BY completionDate DESC, sum(nu_actual_ce) DESC");

    NativeQuery nativeQuery = sessionFactory.getCurrentSession()
        .createNativeQuery(with.toString());
    nativeQuery.setParameter("enterpriseId", enterpriseId);
    nativeQuery.setParameter("sprintState", SprintState.CLOSED.getState());
    if(CollectionUtil.notNullAndEmpty(taskProjectIds)){
      nativeQuery.setParameterList("taskProjectIds", taskProjectIds);
    }
    if(CollectionUtil.notNullAndEmpty(developerIds)){
      nativeQuery.setParameterList("developerIds", developerIds);
    }

    nativeQuery.setParameter("userId", userId);
    nativeQuery.setParameter("teamId", teamId);

    nativeQuery.addScalar("sprintId", IntegerType.INSTANCE);
    nativeQuery.addScalar("name", StringType.INSTANCE);
    nativeQuery.addScalar("startDate", DateType.INSTANCE);
    nativeQuery.addScalar("endDate", DateType.INSTANCE);
    nativeQuery.addScalar("completionDate", DateType.INSTANCE);
    nativeQuery.addScalar("activationDate", DateType.INSTANCE);
    nativeQuery.addScalar("totalCEHours", DoubleType.INSTANCE);
    nativeQuery.setResultTransformer(new AliasToBeanResultTransformer(TLDSprintBean.class));
    nativeQuery.setMaxResults(Objects.requireNonNullElse(lastSelectedSprints, 6));

    return nativeQuery.getResultList();
  }

  private static void addNonSuperUserClause(StringBuilder from, StringBuilder where, Integer userId,
                                            Map<String, Object> params) {
    from.append(" JOIN po_proj_sub_infra USING (id_infra_instan)\n" +
            "           JOIN mv_user_access USING (id_project_sub)\n ");
    where.append(" and id_user = :userId\n");
    params.put("userId", userId);
  }

  @Override
  public List<Integer> getTLDDevelopers(Integer userId) {
    TLDTeamConfigModel tldTeamConfigModel = getTLDTeamConfigModel(userId);
    if(tldTeamConfigModel != null)
      return tldTeamConfigModel.getDevelopers();
    return Collections.emptyList();
  }

  @Override
  @SuppressWarnings(value = "unchecked")
  public List<TLDOverviewResponse> getSprintMetrics(List<Integer> taskProjectIds,
                                                    List<Integer> sprintIds, Long entId, Integer userId,
                                                    Boolean superUser,List<Integer> developers,Long teamId) {

    Map<String, Object> params = new HashMap<>();
    StringBuilder with = getWithClauseForHistory();
    params.put("teamId",teamId);
    StringBuilder query = new StringBuilder();
    query.append(with).append("select ce_sprint.id_sprint   as sprintId,\n" +
            "  count(distinct id_task) filter ( where dt_closed is not null and dt_closed <=\n" +
            "  sd.ts_end_date) as\n" +
            "  completedTasks,\n" +
            "  count(distinct id_task)  as allTasks,\n" +
            "  (extract(days from sd.ts_end_date -\n" +
            "  sd.ts_start_date)) +\n" +
            "  1  as sprintDuration\n" +
            "  from daily_refresh.mv_ce_by_sprint_").append(entId).append(" ce_sprint\n");
    if(!superUser){
      query.append("JOIN po_proj_sub_infra using (id_infra_instan)\n");
      query.append(" JOIN mv_user_access using (id_project_sub)\n");
    }
    query.append(" JOIN task.sprint_details sd USING (id_sprint)\n" +
            " JOIN task.mv_task_details_").append(entId).append(" USING (id_task)\n"+
            " join team_developers using (id_employee) \n"+
            " WHERE ce_sprint.id_enterprise = :enterpriseId and nu_actual_ce > 0\n");
    params.put("enterpriseId", entId);
    if(!superUser){
      query.append(" and mv_user_access.id_user = :userId\n");
    }
    params.put("userId", userId);
    if(CollectionUtil.notNullAndEmpty(sprintIds)) {
      query.append(" and ce_sprint.id_sprint in (:sprintIds)\n");
      params.put("sprintIds", sprintIds);
    }
    query.append(" and (sd.ts_start_date < team_developers.ts_end_date and sd.ts_end_date >= team_developers.ts_start_date)\n");
    if(CollectionUtil.notNullAndEmpty(taskProjectIds)) {
      query.append(" and id_project in (:taskProjectIds)\n");
      params.put("taskProjectIds", taskProjectIds);
    }
    if(CollectionUtil.notNullAndEmpty(developers)) {
      query.append(" and id_employee in (:developers)\n");
      params.put("developers", developers);
    }
    query.append(" group by ce_sprint.id_sprint, sd.ts_start_date, sd.ts_end_date\n");

    NativeQuery durationQuery = sessionFactory.getCurrentSession().createNativeQuery(query.toString());
    SQLUtil.setParameter(durationQuery, params);
    durationQuery.addScalar("sprintId", IntegerType.INSTANCE);
    durationQuery.addScalar("completedTasks", DoubleType.INSTANCE);
    durationQuery.addScalar("allTasks", DoubleType.INSTANCE);
    durationQuery.addScalar("sprintDuration", IntegerType.INSTANCE);
    durationQuery.setResultTransformer(new AliasToBeanResultTransformer(TLDOverviewResponse.class));

    return durationQuery.getResultList();
  }

  @Override
  public TLDTeamConfigModel getTLDTeamConfigModel(Integer userId) {
    Session session = sessionFactory.getCurrentSession();
    CriteriaBuilder builder = session.getCriteriaBuilder();
    CriteriaQuery<TLDTeamConfigModel> query = builder.createQuery(TLDTeamConfigModel.class);
    Root<TLDTeamConfigModel> root = query.from(TLDTeamConfigModel.class);
    List<Predicate> predicates = new ArrayList<>();
    predicates.add(builder.equal(root.get("userId"), userId));
    predicates.add(root.get("endDate").isNull());
    query.select(root).where(predicates.toArray(new Predicate[]{}));
    return session.createQuery(query).uniqueResult();
  }

  @Override
  public TLDOnboardingModel getTLDOnboardingModel(Integer userId) {
    Session session = sessionFactory.getCurrentSession();
    CriteriaBuilder builder = session.getCriteriaBuilder();
    CriteriaQuery<TLDOnboardingModel> query = builder.createQuery(TLDOnboardingModel.class);
    Root<TLDOnboardingModel> root = query.from(TLDOnboardingModel.class);
    query.select(root).where(builder.equal(root.get("userId"), userId));
    return session.createQuery(query).uniqueResult();
  }

  public static StringBuilder getWithClauseForHistory(){
    StringBuilder with = new StringBuilder("with team_developers as (\n" +
            "    select id_employee as id_employee,\n" +
            "           ts_start_date\\:\\:date               as ts_start_date,\n" +
            "           coalesce(ts_end_date, now())\\:\\:date as ts_end_date\n" +
            "    from po_tld_team_configuration\n" +
            "    join po_tld_team_access using(id_team)\n" +
            "    where id_user = :userId\n" +
            "    and id_team = :teamId\n"+
            ")\n");
    return with;
  }

  @Override
  @SuppressWarnings(value = "unchecked")
  public Map<Integer, TLDSprintCodingMetricBean> getSprintCodingMetrics(Long enterpriseId, Integer userId,Boolean superUser, List<Integer> sprintIds,
                                                                        List<Integer> taskProjectIds, List<Integer> developers,Long teamId) {

    Map<String, Object> params = new HashMap<>();

    StringBuilder with = getWithClauseForHistory();
    params.put("teamId",teamId);

    StringBuilder sprintCEInnerQuery = TeamLeadDashboardDaoHelper.getSprintCEInnerQuery(enterpriseId, sprintIds, taskProjectIds, developers, params, true,false,superUser,userId);
    StringBuilder outerMetricQuery = new StringBuilder("select id_sprint                   as sprintId,\n" +
            "       sum(totalCE)                as totalCeHours,\n" +
            "       sum(totalBce) / sum(days)   as avgBce,\n" +
            "       count(distinct id_employee) as activeDevelopers,\n" +
            "       CASE\n" +
            "           when sum(totalCE) > 0 then\n" +
            "               sum(totalAbBce) / sum(totalCE) * 100\n" +
            "           ELSE\n" +
            "               0.0 END             as pctAbCeHours,\n" +
            "       sum(totalTasks) as totalTasks,\n" +
            "       sum(carryOvers) as carryOvers\n");
    outerMetricQuery.append(" from ( ").append(sprintCEInnerQuery).append(" ) sprint_data\n").append(" group by " +
            "id_sprint");

    NativeQuery query = sessionFactory.getCurrentSession().createNativeQuery((with.append(outerMetricQuery)).toString());
    query.addScalar("sprintId", IntegerType.INSTANCE);
    query.addScalar("avgBce", DoubleType.INSTANCE);
    query.addScalar("totalCeHours", DoubleType.INSTANCE);
    query.addScalar("pctAbCeHours", DoubleType.INSTANCE);
    query.addScalar("activeDevelopers", IntegerType.INSTANCE);
    query.addScalar("carryOvers", IntegerType.INSTANCE);
    query.addScalar("totalTasks", IntegerType.INSTANCE);
    SQLUtil.setParameter(query, params);
    query.setResultTransformer(new AliasToBeanResultTransformer(TLDSprintCodingMetricBean.class));

    List<TLDSprintCodingMetricBean> resultList = query.getResultList();
    Map<Integer, TLDSprintCodingMetricBean> sprintMetricMap = new HashMap<>(resultList.size());
    resultList.forEach(result -> sprintMetricMap.put(result.getSprintId(), result));

    return sprintMetricMap;
  }

  @Override
  @SuppressWarnings(value = "unchecked")
  public Map<Integer, List<TLDSprintTaskTypeBean>> getSprintTaskTypeMetrics(Long enterpriseId, Integer userId,Boolean superUser, List<Integer> sprintIds,
                                                                            List<Integer> taskProjectIds, List<Integer> developers,Long teamId) {

    Map<String, Object> params = new HashMap<>();

    StringBuilder with = getWithClauseForHistory();

    params.put("teamId",teamId);

    StringBuilder sprintTaskTypeInnerQuery = TeamLeadDashboardDaoHelper.getSprintCEInnerQuery(enterpriseId, sprintIds, taskProjectIds, developers,
            params, false,false,superUser,userId);
    StringBuilder outerMetricQuery = new StringBuilder("select id_sprint                   as sprintId,\n" +
            "       tx_issue_type as taskType,\n" +
            "       sum(totalCE)                as totalCeHours,\n" +
            "       CASE\n" +
            "           when sum(days) > 0 then\n" +
            "               sum(totalBce) / sum(days)\n" +
            "           ELSE\n" +
            "               0.0 END             as avgBce,\n"+
            "       count(distinct id_employee) as activeDevelopers,\n" +
            "       CASE\n" +
            "           when sum(totalCE) > 0 then\n" +
            "               sum(totalAbBce) / sum(totalCE) * 100\n" +
            "           ELSE\n" +
            "               0.0 END             as pctAbCeHours\n");
    outerMetricQuery.append(" from ( ").append(sprintTaskTypeInnerQuery).append(" ) sprint_data\n").append(" group by " +
            "id_sprint, tx_issue_type");

    NativeQuery nativeQuery = sessionFactory.getCurrentSession().createNativeQuery((with.append(outerMetricQuery)).toString());
    SQLUtil.setParameter(nativeQuery, params);
    nativeQuery.addScalar("sprintId", IntegerType.INSTANCE);
    nativeQuery.addScalar("taskType", StringType.INSTANCE);
    nativeQuery.addScalar("avgBce", DoubleType.INSTANCE);
    nativeQuery.addScalar("totalCeHours", DoubleType.INSTANCE);
    nativeQuery.addScalar("pctAbCeHours", DoubleType.INSTANCE);
    nativeQuery.setResultTransformer(new AliasToBeanResultTransformer(TLDSprintTaskTypeBean.class));

    List<TLDSprintTaskTypeBean> resultList = nativeQuery.getResultList();
    Map<Integer, List<TLDSprintTaskTypeBean>> resultMap = new HashMap<>();
    resultList.forEach(result -> {
      Integer sprintId = result.getSprintId();
      List<TLDSprintTaskTypeBean> tldSprintTaskTypeBeans = resultMap.get(sprintId);
      if(CollectionUtil.nullOrEmpty(tldSprintTaskTypeBeans)) {
        tldSprintTaskTypeBeans = new ArrayList<>();
        resultMap.put(sprintId, tldSprintTaskTypeBeans);
      }
      tldSprintTaskTypeBeans.add(result);
    });
    return resultMap;
  }

  public Map<Integer, TLDSprintTaskTypeBean> getSprintUnattributedCE(Long enterpriseId, Integer userId,
                                                                     Boolean superUser,List<Integer> filteredSprints,
                                                                     List<Integer> developers,Long teamId) {
    Map<String, Object> param = new HashMap<>();

    StringBuilder with = getWithClauseForHistory();

    param.put("teamId",teamId);

    StringBuilder innerQuery = new StringBuilder("select id_sprint,\n" +
            "                ce_sprint.id_employee,\n" +
            "                sum(nu_bce)                as totalBce,\n" +
            "                sum(nu_actual_ce)          as totalCE,\n" +
            "                sum(nu_actual_aberrant_ce) as totalAbBce,\n" +
            " char_length(replace(bit_or(lpad(nu_active_days, 415, '0')\\:\\:bit(415))\\:\\:text,\n" +
            "           '0', '')) as days\n" +
            "         from daily_refresh.mv_ce_by_sprint_" + enterpriseId + " ce_sprint\n " +
            "              join task.sprint_details sd USING (id_sprint)\n" +
            "              join team_developers using (id_employee)\n" );
    StringBuilder whereClause = new StringBuilder(" where id_enterprise = :enterpriseId\n" +
            "           and bl_unattributed = true\n");
    whereClause.append(" and (sd.ts_start_date < team_developers.ts_end_date and sd.ts_end_date >= team_developers.ts_start_date)\n");
    param.put("enterpriseId", enterpriseId);
    if(!superUser) {
      innerQuery.append(" JOIN po_proj_sub_infra using (id_infra_instan)\n");
      innerQuery.append(" JOIN mv_user_access using (id_project_sub)\n");
      whereClause.append(" and mv_user_access.id_user = :userId\n");
    }
    param.put("userId", userId);
    if(CollectionUtil.notNullAndEmpty(filteredSprints)) {
      whereClause.append(" and id_sprint in (:sprintIds)\n");
      param.put("sprintIds", filteredSprints);
    }
    if(CollectionUtil.notNullAndEmpty(developers)) {
      whereClause.append(" and id_employee in (:developers)\n");
      param.put("developers", developers);
    }
    innerQuery.append(whereClause).append(" group by id_sprint, id_employee\n");
    String outerMetricQuery = with + "select id_sprint                   as sprintId,\n" +
            "       sum(totalCE)                as totalCeHours,\n" +
            "       sum(totalBce) / sum(days)   as avgBce,\n" +
            "       count(distinct id_employee) as activeDevelopers,\n" +
            "       CASE\n" +
            "           when sum(totalCE) > 0 then\n" +
            "               sum(totalAbBce) / sum(totalCE) * 100\n" +
            "           ELSE\n" +
            "               0.0 END             as pctAbCeHours\n" + " from ( " + innerQuery + " ) sprint_data\n" + " group by " +
            "id_sprint";
    NativeQuery nativeQuery = sessionFactory.getCurrentSession().createNativeQuery(outerMetricQuery);
    SQLUtil.setParameter(nativeQuery, param);
    nativeQuery.addScalar("sprintId", IntegerType.INSTANCE);
    nativeQuery.addScalar("totalCeHours", DoubleType.INSTANCE);
    nativeQuery.addScalar("avgBce", DoubleType.INSTANCE);
    nativeQuery.addScalar("pctAbCeHours", DoubleType.INSTANCE);
    nativeQuery.setResultTransformer(new AliasToBeanResultTransformer(TLDSprintTaskTypeBean.class));

    List<TLDSprintTaskTypeBean> resultList = nativeQuery.getResultList();
    Map<Integer, TLDSprintTaskTypeBean> resultMap = new HashMap<>();
    resultList.forEach(result -> resultMap.put(result.getSprintId(), result));

    return resultMap;
  }

  @Override
  @SuppressWarnings(value = "unchecked")
  public List<SprintDto> getSprintDetails(Long enterpriseId, List<Integer> sprintIds) {
    if(CollectionUtil.nullOrEmpty(sprintIds))
      return Collections.emptyList();

    NativeQuery query = sessionFactory.getCurrentSession().createNativeQuery("select id_sprint           as sprintId,\n" +
            "       tx_name             as name,\n" +
            "       ts_start_date\\:\\:date as startDate,\n" +
            "       ts_end_date\\:\\:date   as endDate\n" +
            "from task.sprint_details\n" +
            "         JOIN task.task_sprint_assoc USING (id_sprint)\n" +
            "         JOIN task.mv_task_details_" + enterpriseId + " USING (id_task)\n" +
            "where id_sprint in (:sprintIds)");
    query.setParameterList("sprintIds", sprintIds);
    query.addScalar("sprintId", IntegerType.INSTANCE);
    query.addScalar("name", StringType.INSTANCE);
    query.addScalar("startDate", DateType.INSTANCE);
    query.addScalar("endDate", DateType.INSTANCE);
    query.setResultTransformer(new AliasToBeanResultTransformer(SprintDto.class));
    return query.getResultList();
  }

  @Override
  @SuppressWarnings(value = "unchecked")
  public Map<Date, TLDMetricBean> getDayMetricData(Long enterpriseId,Integer userId,Boolean superUser, Date minSprintStartDate, Date maxSprintEndDate,List<Integer> developerIds) {
    StringBuilder select = new StringBuilder("select date_trunc('day', id_tseries)\\:\\:date  as activityDay,\n"+
            "       sum(nu_bce)                                             as totalBce,\n" +
            "       sum(nu_actual_ce)                                       as totalCeHours,\n" +
            "       sum(nu_actual_aberrant_ce)                              as totalAbCeHours,\n" +
            "       count(distinct (id_employee, id_tseries))               as totalDays\n" +
            "from daily_refresh.mv_calendar\n");
    StringBuilder whereClause = new StringBuilder("where id_enterprise = :enterpriseId\n" +
            "  and id_tseries >= :minSprintStartDate\n" +
            "  and id_tseries <= :maxSprintEndDate\n");
    if(!superUser){
      select.append(" JOIN po_proj_sub_infra using (id_infra_instan)\n");
      select.append(" JOIN mv_user_access using (id_project_sub)\n");
      whereClause.append(" and mv_user_access.id_user = :userId\n");
    }

    if(CollectionUtil.notNullAndEmpty(developerIds)){
      whereClause.append(" and id_employee in (:developerIds)\n");
    }
    select.append(whereClause).append("GROUP BY activityDay");
    NativeQuery query = sessionFactory.getCurrentSession().createNativeQuery(select.toString());

    query.setParameter("minSprintStartDate", minSprintStartDate);
    query.setParameter("maxSprintEndDate", maxSprintEndDate);
    query.setParameter("enterpriseId", enterpriseId);

    if(!superUser){
      query.setParameter("userId", userId);
    }

    if(CollectionUtil.notNullAndEmpty(developerIds)){
      query.setParameter("developerIds", developerIds);
    }

    query.addScalar("activityDay", DateType.INSTANCE);
    query.addScalar("totalBce", DoubleType.INSTANCE);
    query.addScalar("totalDays", IntegerType.INSTANCE);
    query.addScalar("totalAbCeHours", DoubleType.INSTANCE);
    query.addScalar("totalCeHours", DoubleType.INSTANCE);
    query.setResultTransformer(new AliasToBeanResultTransformer(TLDDayMetricBean.class));

    List<TLDDayMetricBean> result = query.getResultList();
    Map<Date, TLDMetricBean> resultMap = new HashMap<>(result.size());
    result.forEach(data -> resultMap.put(data.getActivityDay(), data));
    return resultMap;
  }


  private static String getSprintCETable(Long enterpriseId) {
    return "daily_refresh.mv_ce_by_sprint_" + enterpriseId;
  }

  @Override
  public List<TLDDeveloperBean> getTLDDeveloperFilters(Integer limit, Integer offset,
      List<Integer> taskProjectIds, List<Long> teamIds, Integer userId, Boolean superUser, Long enterpriseId,
      List<Long> datasourceIds) {
    boolean checkTaskFilter = CollectionUtil.notNullAndEmpty(taskProjectIds);

    StringBuilder select = new StringBuilder();
    StringBuilder where = new StringBuilder(" where po_employee.id_enterprise = :enterpriseId\n");

    StringBuilder with = new StringBuilder()
            .append("with emp_infra_ids as ( \n " +
                    "  SELECT id_employee, " +
                    "  max(dt_last_commit) as dt_last_commit, \n" +
                    "  max(dt_last_pr) as dt_last_pr, \n" +
                    "  max(dt_last_task) as dt_last_task \n" +
                    "  FROM po_emp_infra_").append(enterpriseId.longValue())
            .append(" join po_emp_id using (id_emp_id) group by id_employee), \n");

    with.append("team_members as (\n"
        + "                               select distinct on(id_employee, po_tld_team_access.id_team)id_employee,\n"
        + "                                      case when ts_end_date is null then true else false end as active,\n"
        + "                                      po_tld_team_access.id_team                                    as teamId,\n"
        + "                                      tx_team_name                                           as teamName\n"
        + "    from po_tld_team_configuration "
        + "    join po_tld_team_access using(id_team)\n"
        + "    join po_tld_team using(id_team)"
        + "    where id_user = :userId\n"
        + (CollectionUtil.notNullAndEmpty(teamIds) ?
          "           and id_team in (:teamIds) \n" : "")
        + "    order by id_employee, po_tld_team_access.id_team,ts_start_date desc)\n");
    select.append(
            "select po_employee.id_employee       as developerId,\n" +
            "       po_employee.tx_email_address  as email,\n" +
            "       po_employee.tx_first_name     as firstName,\n" +
            "       po_employee.tx_second_name    as lastName,\n" +
            "       tx_role_predicted as predictedRole,\n" +
            "       po_employee.nu_tenure         as tenure ,\n" +
            "       tx_employer_id    as uid,\n" +
            "       team_members.active,\n" +
            "       teamId,\n" +
            "       teamName,\n" +
            "       po_employee.bl_anonymise_data as isAnonymised,\n" +
            "       array_agg(distinct secondary.tx_email_address) as consolidatedLogins," +
            "       array_remove(array[ \n" +
            "                 case when count(emp_infra_ids.dt_last_commit) != 0 then 'Commit' end, \n" +
            "                 case when count(emp_infra_ids.dt_last_pr) != 0 then 'PR' end, \n" +
            "                 case when count(emp_infra_ids.dt_last_task) != 0 then 'Task Tracker' end \n" +
            "             ], null) as activitySources \n" +
            "    from po_employee\n" +
            "         join po_emp_id on po_employee.id_employee = po_emp_id.id_employee\n" +
            "         join po_employee secondary on secondary.id_employee = po_emp_id.id_orig_employee\n" +
            "         join team_members on po_employee.id_employee = team_members.id_employee\n" +
            "         join po_emp_profile on po_employee.id_employee = po_emp_profile.id_employee\n" +
            "         join emp_infra_ids on po_employee.id_employee =  emp_infra_ids.id_employee\n" +
            "         left join po_dev_role_predicted using (id_role_predicted)\n");

    if (!superUser || CollectionUtil.notNullAndEmpty(datasourceIds)) {
      select.append(" JOIN po_emp_infra on po_emp_id.id_emp_id = po_emp_infra.id_emp_id\n" +
              " JOIN po_proj_sub_infra using (id_infra_instan)\n");
    }
    if(!superUser){
      select.append(" JOIN mv_user_access using (id_project_sub)\n");
      where.append(" and mv_user_access.id_user = :userId\n");
    }
    if(CollectionUtil.notNullAndEmpty(datasourceIds)){
      select.append(" join po_proj_sub_infra subInfra using (id_project_sub)\n");
      where.append(" and subInfra.id_infra_instan in (:datasourceIds) ");
    }

    if(checkTaskFilter) {
      select.append(" join daily_refresh.mv_task_by_month on po_employee.id_employee = mv_task_by_month.id_employee\n");
      select.append(
          TASK_JOIN
              .replace("{entId}", String.valueOf(UserContext.getUserToken().getEnterpriseId())))
          .append("        join task.project_details using (id_project) ");
      where.append(" and id_project in (:taskProjectIds) ");
    }

    where.append(" group by 1,2,3,4,5,6,7,8,9,10,11 ");
    NativeQuery sqlQuery = sessionFactory.getCurrentSession()
        .createNativeQuery(with.append(select.append(where)).toString());
    if (limit != null) {
      sqlQuery.setMaxResults(limit);
    }
    if (offset != null) {
      sqlQuery.setFirstResult(offset);
    }
    sqlQuery.setParameter("enterpriseId", enterpriseId);
    sqlQuery.setParameter("userId", userId);
    if (CollectionUtil.notNullAndEmpty(taskProjectIds)) {
      sqlQuery.setParameter("taskProjectIds", taskProjectIds);
    }
    if (CollectionUtil.notNullAndEmpty(teamIds)) {
      sqlQuery.setParameter("teamIds", teamIds);
    }
    if (CollectionUtil.notNullAndEmpty(datasourceIds)) {
      sqlQuery.setParameter("datasourceIds", datasourceIds);
    }
    sqlQuery.addScalar("developerId", IntegerType.INSTANCE);
    sqlQuery.addScalar("email", StringType.INSTANCE);
    sqlQuery.addScalar("firstName", StringType.INSTANCE);
    sqlQuery.addScalar("lastName", StringType.INSTANCE);
    sqlQuery.addScalar("predictedRole", StringType.INSTANCE);
    sqlQuery.addScalar("teamName", StringType.INSTANCE);
    sqlQuery.addScalar("tenure", IntegerType.INSTANCE);
    sqlQuery.addScalar("uid", StringType.INSTANCE);
    sqlQuery.addScalar("teamId", LongType.INSTANCE);
    sqlQuery.addScalar("active", BooleanType.INSTANCE);
    sqlQuery.addScalar("consolidatedLogins", new CustomType(new ArrayUserType("string")));
    sqlQuery.addScalar("activitySources", new CustomType(new ArrayUserType("string")));
    sqlQuery.addScalar("isAnonymised", BooleanType.INSTANCE);

    sqlQuery.setResultTransformer(new AliasToBeanResultTransformer(TLDDeveloperBean.class));

    return (List<TLDDeveloperBean>) sqlQuery.list();

  }

  @Override
  public MissingActivitySources getMissingActivitySourcesByEnterprise(Long enterpriseId) {
    StringBuilder query = new StringBuilder()
            .append("SELECT EXISTS ( SELECT dt_last_pr FROM po_emp_infra_").append(enterpriseId.longValue())
            .append(" WHERE dt_last_pr is not null) as hasPr,\n"+
                    " EXISTS ( SELECT dt_last_task FROM po_emp_infra_").append(enterpriseId.longValue())
            .append(" WHERE dt_last_task is not null) as hasTaskTracker");
    NativeQuery sqlQuery = sessionFactory.getCurrentSession()
            .createNativeQuery(query.toString());
    sqlQuery.addScalar("hasPr", BooleanType.INSTANCE);
    sqlQuery.addScalar("hasTaskTracker", BooleanType.INSTANCE);
    sqlQuery.setResultTransformer(Transformers.aliasToBean(MissingActivitySources.class));
    return (MissingActivitySources) sqlQuery.uniqueResult();
  }

  @Override
  public void suggestChanges(Integer action, List<Integer> developerIds, Integer userId, Integer enterpriseId) {
    String query = new StringBuilder()
        .append("insert into po_tld_team_changes\n")
        .append("(id_user,id_enterprise,nu_action_type,arr_employees,ts_action_date, nu_status)\n")
        .append("values(:userId, :enterpriseId, :action, :developerIds,now(),1)").toString();

    NativeQuery sqlQuery = sessionFactory.getCurrentSession().createNativeQuery(query);
    sqlQuery.setParameter("developerIds",developerIds,new CustomType(new ArrayUserType("int")));
    sqlQuery.setParameter("userId", userId);
    sqlQuery.setParameter("enterpriseId",enterpriseId);
    sqlQuery.setParameter("action", action);
    sqlQuery.executeUpdate();
  }


  @Override
  public List<String> getSysAdmins(Integer enterpriseId, boolean isEmpty) {
    String query="select distinct \n"
        + "    po_user.tx_email_address\n"
        + "FROM\n"
        + "    po_user\n"
        + "        join po_enterprise using (id_enterprise)\n"
        + "        LEFT JOIN po_access_user_group USING (id_user)\n"
        + "        LEFT JOIN po_access_group USING (id_access_group)\n"
        + "        LEFT JOIN po_access_group_role USING (id_access_group)\n"
        + "        LEFT JOIN po_access_role USING (id_access_role)\n"
        + "        join po_user_profile using (id_user)\n"
        + "--\n"
        + "WHERE  (po_access_role.arr_pages @> ARRAY['manageDevelopers'] or bl_super)\n"
        + "  AND po_user.id_enterprise=:enterpriseId\n";
        if(!isEmpty)
          query+= "  AND  po_user_profile.dt_current_login >= now() - interval '3 months'\n";
        query+= "  AND bl_enabled\n"
        + "  AND bl_refresh_bi\n"
        + "  AND nu_products <> 0;";
    NativeQuery nativeQuery = sessionFactory.getCurrentSession().createNativeQuery(query);
    nativeQuery.setParameter("enterpriseId", enterpriseId);
    List<String> result=nativeQuery.getResultList();
    return  result;
  }

  @Override
  public boolean checkIfTeamWithSameNameExists(String teamName, Integer userId, Long enterpriseId) {
    NativeQuery query = sessionFactory.getCurrentSession().createNativeQuery(
        "select distinct 1 from po_tld_team where id_enterprise = :enterpriseId and lower(tx_team_name) = lower(:teamName) and id_user = :userId");
    query.setParameter("userId", userId);
    query.setParameter("teamName", teamName);
    query.setParameter("enterpriseId", enterpriseId);
    return query.list().size() > 0;
  }

  @Override
  public void removeDevsFromPreferencesForTeamLeadsRetro(ArrayList<String> removedTls,
      long enterpriseId) {
    String sqlQuery = "delete\n"
        + "from po_tld_preferences\n"
        + "where id_user in (select id_user\n"
        + "                  from po_user\n"
        + "                  where tx_email_address in (:teamLeadEmails) and id_enterprise = :enterpriseId)";
    NativeQuery nativeQuery = sessionFactory.getCurrentSession().createNativeQuery(sqlQuery);
    nativeQuery.setParameter("teamLeadEmails", removedTls).setParameter("enterpriseId", enterpriseId);
    nativeQuery.executeUpdate();
  }

  @Override
  public void removePreferencesForMnt(int userId) {
    String sqlQuery = "delete\n"
        + "from po_tld_preferences_mnt\n"
        + "where id_user = :userId";
    NativeQuery nativeQuery = sessionFactory.getCurrentSession().createNativeQuery(sqlQuery);
    nativeQuery.setParameter("userId", userId);
    nativeQuery.executeUpdate();
  }

  @Override
  public void removePreferencesRetro(int userId) {
    String sqlQuery = "delete\n"
        + "from po_tld_preferences\n"
        + "where id_user = :userId";
    NativeQuery nativeQuery = sessionFactory.getCurrentSession().createNativeQuery(sqlQuery);
    nativeQuery.setParameter("userId", userId);
    nativeQuery.executeUpdate();
  }

  @Override
  public void removeDevsFromPreferencesForTeamLeadsMnt(ArrayList<String> removedTls,
      long enterpriseId) {
    String sqlQuery = "delete\n"
        + "from po_tld_preferences_mnt\n"
        + "where id_user in (select id_user\n"
        + "                  from po_user\n"
        + "                  where tx_email_address in (:teamLeadEmails) and id_enterprise = :enterpriseId)";
    NativeQuery nativeQuery = sessionFactory.getCurrentSession().createNativeQuery(sqlQuery);
    nativeQuery.setParameter("teamLeadEmails", removedTls).setParameter("enterpriseId", enterpriseId);
    nativeQuery.executeUpdate();
  }


  @Override
  public void addTeamName(String teamName, Integer userId, Long enterpriseId) {
    StringBuilder sql = new StringBuilder("update po_tld_team_configuration \n"
        + "set tx_team_name =:teamName \n"
        + "where id_user= :userId and\n"
        + "      id_enterprise = :enterpriseId");
    Query query = sessionFactory.getCurrentSession().createNativeQuery(sql.toString());
    query.setParameter("teamName", teamName);
    query.setParameter("userId", userId);
    query.setParameter("enterpriseId", enterpriseId);

    query.executeUpdate();

  }

  @Override
  public String getTeamName(Integer userId,Integer enterpriseId){
    String sql="SELECT  tx_team_name\n"
        + "FROM po_tld_team_configuration\n"
        + "where id_user = :userId\n"
        + "  AND id_enterprise = :enterpriseId order by ts_start_date  desc limit 1";

    Query query = sessionFactory.getCurrentSession().createNativeQuery(sql);
    query.setParameter("userId", userId);
    query.setParameter("enterpriseId", enterpriseId);
    return (String) query.uniqueResult();

  }

  @Override
  public boolean checkTeamExistence(Integer userId, Integer enterpriseId) {
    String sql = "select id_user from po_tld_team_configuration \n"
        + "where id_user = :userId and \n"
        + "      ts_end_date is null and \n"
        + "      id_enterprise = :enterpriseId";
    Query query = sessionFactory.getCurrentSession().createNativeQuery(sql);
    query.setParameter("userId", userId);
    query.setParameter("enterpriseId", enterpriseId);

    return !query.list().isEmpty();
  }

  @Override
  public boolean isTaskDataAvailable(Long enterpriseId,List<Integer> currentDeveloperIds){

    return (boolean) sessionFactory.getCurrentSession().createNativeQuery(
            "select count(*) > 0 as count \n"
                    + " from daily_refresh.mv_ce_by_task_type_" + enterpriseId
                    + " where id_infrastructure_task in (:infrastructureIds)\n"
                    + " and id_employee in (:employeeIds)\n"
                    + " and id_task is not null ")
        .setParameterList("employeeIds", currentDeveloperIds)
        .setParameterList("infrastructureIds", InfrastructureType.getAvailableTaskTracker())
        .addScalar("count", BooleanType.INSTANCE)
        .getSingleResult();
  }

  @Override
  public boolean isSprintDataAvailable(Long enterpriseId, List<Integer> developers) {
    return (boolean) sessionFactory.getCurrentSession()
        .createNativeQuery("select count(*) > 0 as sprintExist\n"
            + "from daily_refresh.mv_ce_by_sprint_"+enterpriseId+" mcbpr\n"
            + "     join task.sprint_details using(id_sprint)"
            + "         where id_employee in (:employeeIds)"
            + "             and nu_actual_ce > 0"
            + "             and nu_state = 1"
            + "             and ts_start_date > now() - interval '1 year'")
        .setParameterList("employeeIds", developers)
        .addScalar("sprintExist", BooleanType.INSTANCE)
        .getSingleResult();
  }

  @Override
  public boolean isPullRequestDataAvailable(Long enterpriseId, List<Integer> developers) {
    return (boolean) sessionFactory.getCurrentSession()
        .createNativeQuery("select count(*) > 0  as closedPRs\n"
            + "from daily_refresh.mv_ce_by_pull_request_"+enterpriseId+" mcbpr\n"
            + "         join po_emp_id pei on mcbpr.id_creator = pei.id_emp_id"
            + "         where id_employee in (:employeeIds)"
            + "           and nu_state <> 1"
            + "           and ts_creation_date > now() - interval '1 year'")
        .setParameterList("employeeIds", developers)
        .addScalar("closedPRs", BooleanType.INSTANCE)
        .getSingleResult();
  }

  @Override
  public boolean isCommitFrequencyDataAvailable(Date startDate, Date endDate, Long enterpriseId, Long teamId, Integer userId, Boolean isSuperUser) {
    String query = " WITH " + TeamLeadDashboardDaoHelper.withClauseForTeamChanges() +
            " select count(*) > 0 as commitFrequency \n" +
            " from tld.mv_scr_activity_stats_view_" + enterpriseId + " msasv\n" +
            TeamLeadDashboardDaoHelper.joinClauseForTeamChanges(null, "msasv") +
            (!isSuperUser ?
            (" inner join po_proj_sub_infra psi ON msasv.id_infra_instan = psi.id_infra_instan" +
            " inner join mv_user_access mua ON psi.id_project_sub = mua.id_project_sub" +
            " where mua.id_user = :userId and ") : " where ") +
            " id_tseries between :startDate and :endDate and " +
            TeamLeadDashboardDaoHelper.whereClauseForTeamChanges();
    return (boolean) sessionFactory.getCurrentSession().createNativeQuery(query)
            .setParameter("teamId", teamId)
            .setParameter("userId", userId)
            .setParameter("startDate", startDate)
            .setParameter("endDate", endDate)
            .addScalar("commitFrequency", BooleanType.INSTANCE)
            .getSingleResult();
  }

  @Override
  public boolean isInterOrIntraPrDataAvailable(Date startDate, Date endDate, Long enterpriseId, Long teamId, Integer userId, Boolean isSuperUser) {
    String query = " WITH " + TeamLeadDashboardDaoHelper.withClauseForTeamChanges() +
            " select count(*) > 0 as prData \n" +
            " from daily_refresh.mv_ce_by_pull_request_" + enterpriseId + " mcbpr\n" +
            " join po_emp_id pe on pe.id_emp_id = mcbpr.id_creator " +
            TeamLeadDashboardDaoHelper.joinClauseForTeamChanges(null, "pe") +
            (!isSuperUser ?
            (" inner join po_proj_sub_infra psi ON mcbpr.id_infra_instan = psi.id_infra_instan" +
            " inner join mv_user_access mua ON psi.id_project_sub = mua.id_project_sub" +
            " where mua.id_user = :userId and ") : " where ") +
            " ts_creation_date between :startDate and :endDate and " +
            TeamLeadDashboardDaoHelper.whereClauseForTeamChanges(null, "ts_creation_date");
    return (boolean) sessionFactory.getCurrentSession().createNativeQuery(query)
            .setParameter("teamId", teamId)
            .setParameter("userId", userId)
            .setParameter("startDate", startDate)
            .setParameter("endDate", endDate)
            .addScalar("prData", BooleanType.INSTANCE)
            .getSingleResult();
  }

  @Override
  public boolean isCycleTimeDataAvailable(Date startDate, Date endDate, Long enterpriseId, Long teamId, Integer userId, Boolean isSuperUser) {
    String query = " WITH " + TeamLeadDashboardDaoHelper.withClauseForTeamChanges() +
            " select count(*) > 0 as cycleTime \n" +
            " from tld.gdop_pr_metrics_info_" + enterpriseId + " gpmi\n" +
            TeamLeadDashboardDaoHelper.joinClauseForTeamChanges(null, "gpmi") +
            (!isSuperUser ?
            (" inner join po_proj_sub_infra psi ON gpmi.id_infra_instan = psi.id_infra_instan" +
            " inner join mv_user_access mua ON psi.id_project_sub = mua.id_project_sub" +
            " where gpmi.nu_state = " + PRState.MERGED.code + " and mua.id_user = :userId and ") : " where ") +
            " gpmi.nu_state = PRState.MERGED.code and ts_creation_date between :startDate and :endDate and " +
            TeamLeadDashboardDaoHelper.whereClauseForTeamChanges(null, "ts_creation_date");
    return (boolean) sessionFactory.getCurrentSession().createNativeQuery(query)
            .setParameter("teamId", teamId)
            .setParameter("userId", userId)
            .setParameter("startDate", startDate)
            .setParameter("endDate", endDate)
            .addScalar("cycleTime", BooleanType.INSTANCE)
            .getSingleResult();
  }

  @Override
  public boolean isPrInfluencingMetricsDataAvailable(Date startDate, Date endDate, Long enterpriseId, Long teamId, Integer userId, Boolean isSuperUser) {
    String query = " WITH " + TeamLeadDashboardDaoHelper.withClauseForTeamChanges() +
            " select count(*) > 0 as prInfluencingMetrics \n" +
            " from tld.cycle_time_influencing_metrics_info_" + enterpriseId + " ctimi\n" +
            TeamLeadDashboardDaoHelper.joinClauseForTeamChanges(null, "ctimi") +
            (!isSuperUser ?
                    (" inner join po_proj_sub_infra psi ON ctimi.id_infra_instan = psi.id_infra_instan" +
                            " inner join mv_user_access mua ON psi.id_project_sub = mua.id_project_sub" +
                            " where ctimi.nu_state = " + PRState.MERGED.code + " and mua.id_user = :userId and ") : " where ") +
            " ts_creation_date between :startDate and :endDate and ctimi.nu_state = " + PRState.MERGED.code + " and " +
            TeamLeadDashboardDaoHelper.whereClauseForTeamChanges(null, "ts_creation_date");
    return (boolean) sessionFactory.getCurrentSession().createNativeQuery(query)
            .setParameter("teamId", teamId)
            .setParameter("userId", userId)
            .setParameter("startDate", startDate)
            .setParameter("endDate", endDate)
            .addScalar("prInfluencingMetrics", BooleanType.INSTANCE)
            .getSingleResult();
  }

    @Override
  public boolean isActivityMappedDataAvailable(Long enterpriseId, List<Integer> developers) {
    return (boolean) sessionFactory.getCurrentSession()
        .createNativeQuery(
            "select count(*) > 0 as validActivityExist from daily_refresh.mv_task_by_day where id_enterprise = " + enterpriseId
                + " and nu_bce > 0 and id_employee in (:employeeIds) "
                + " and id_tseries > now() - interval '1 year' limit 1")
        .setParameterList("employeeIds", developers)
        .addScalar("validActivityExist", BooleanType.INSTANCE)
        .getSingleResult();
  }

  @Override
  public Stream<CombinedTeamAndDevelopers> getTeamsForUser(Long enterpriseId, Integer userId, Boolean isAdmin, Integer limit, Integer offset) {
    String rawQuery = getTeamInfoClause(isAdmin) + getOwnerAndLeadClauses() + getMembersInfoClause(enterpriseId) + getTeamsQueryForUser();

    NativeQuery query = sessionFactory.getCurrentSession().createNativeQuery(rawQuery);

    query.setParameter("enterpriseId", enterpriseId);
    if(!isAdmin){
      query.setParameter("userId", userId);
    }
    if(isAdmin){
      query.setParameter("limit", limit);
      query.setParameter("offset", offset);
    }

    query.addScalar("teamId", LongType.INSTANCE);
    query.addScalar("teamName", StringType.INSTANCE);
    query.addScalar("teamCreatedAt", TimestampType.INSTANCE);
    query.addScalar("ownerId", IntegerType.INSTANCE);
    query.addScalar("ownerName", StringType.INSTANCE);
    query.addScalar("ownerEmail", StringType.INSTANCE);
    query.addScalar("leadId", IntegerType.INSTANCE);
    query.addScalar("leadName", StringType.INSTANCE);
    query.addScalar("leadEmail", StringType.INSTANCE);
    query.addScalar("leadPermissionCode", IntegerType.INSTANCE);
    query.addScalar("showTeam", BooleanType.INSTANCE);
    query.addScalar("permissionCode", IntegerType.INSTANCE);
    query.addScalar("developerId", IntegerType.INSTANCE);
    query.addScalar("joiningDate", DateType.INSTANCE);
    query.addScalar("leavingDate", DateType.INSTANCE);
    query.addScalar("activitySources", new CustomType(new ArrayUserType("string")));

    query.setResultTransformer(new AliasToBeanResultTransformer(CombinedTeamAndDevelopers.class));
    return query.getResultStream();
  }

  @Override
  public BlueoptimaMetrics getCeDataWithoutTaskAssociation(Long enterpriseId, Integer userId, Boolean superUser, Date startDate, Date endDate, Integer teamId) {
    String plainQuery = " WITH " + TeamLeadDashboardDaoHelper.withClauseForTeamChanges() +
            "select sum(nu_bce) / count(distinct (mainTable.id_employee, id_tseries)) as avgBce,\n" +
            "       CASE\n" +
            "           when sum(nu_bce) > 0 then\n" +
            "               sum(nu_aberrant_ce) / sum(nu_bce) * 100\n" +
            "           ELSE\n" +
            "               null END                                         as pctAbBce\n" +
            " from daily_refresh.mv_calendar mainTable\n" +
            // using calendar because employee might leave within a week
            TeamLeadDashboardDaoHelper.joinClauseForTeamChanges(null, "mainTable") +
            ((!superUser) ? " JOIN po_proj_sub_infra using (id_infra_instan)\n" +
                    " JOIN mv_user_access using (id_project_sub)\n" : "") +
            "where id_enterprise = :enterpriseId\n" +
            (!superUser ? " and id_user = :userId\n" : "") + " and " +
            TeamLeadDashboardDaoHelper.whereClauseForTeamChanges() +
            "  and mainTable.id_tseries between :startDate and :endDate\n";
    NativeQuery<BlueoptimaMetrics> query = sessionFactory.getCurrentSession().createNativeQuery(plainQuery);
    query.addScalar("avgBce", DoubleType.INSTANCE);
    query.addScalar("pctAbBce", DoubleType.INSTANCE);
    query.setParameter("userId", userId);
    query.setParameter("teamId", teamId);
    query.setParameter("enterpriseId", enterpriseId);
    query.setParameter("startDate", startDate);
    query.setParameter("endDate", endDate);
    query.setResultTransformer(Transformers.aliasToBean(BlueoptimaMetrics.class));
    return query.uniqueResult();

  }

  private String getTeamInfoClause(Boolean isAdmin) {
    String teamInfoClause =  "with team_info as (\n"
            + "    select id_team as teamId,\n"
            + "           tx_team_name as teamName,\n"
            + "           ts_team_created as teamCreatedAt,\n"
            + "           id_owner as ownerId,\n"
            + "           id_lead as leadId,\n"
            + "           bl_show_team as showTeam,\n"
            + "           nu_access as permissionCode\n"
            + "    from po_tld_team_access access\n"
            + "    join po_tld_team using (id_team)\n"
            + "    where access.id_enterprise = :enterpriseId\n";
            if(!isAdmin){
              teamInfoClause += "    and access.id_user = :userId\n";
            }
            if(isAdmin){
              teamInfoClause += " LIMIT :limit OFFSET :offset";
            }
            teamInfoClause += "),\n";
            return teamInfoClause;
  }

  private String getOwnerAndLeadClauses() {
    return "ownerInfo as (\n" +
            "    select\n" +
            "        id_user as owner_id,\n" +
            "        tx_first_name || coalesce(' ' || tx_second_name, '') as ownerName,\n" +
            "        tx_email_address as ownerEmail \n" +
            "    from po_user where po_user.id_user in (select ownerId from team_info)\n" +
            "), leadInfo as (\n" +
            "    select\n" +
            "        id_team,\n" +
            "        id_user as lead_id,\n" +
            "        tx_first_name || coalesce(' ' || tx_second_name, '') as leadName,\n" +
            "        tx_email_address as leadEmail,\n" +
            "        nu_access as leadPermissionCode\n" +
            "    from po_user join po_tld_team_access using (id_user)\n" +
            "    where po_user.id_user in (select leadId from team_info)\n" +
            "    and po_tld_team_access.id_user in (select leadId from team_info)\n" +
            "),\n";
  }

  private String getMembersInfoClause(Long enterpriseId) {
    return "membersInfo as (\n" +
            "         select\n" +
            "             id_team as team_id,\n" +
            "             id_employee as developerId,\n" +
            "             ts_start_date as joiningDate,\n" +
            "             ts_end_date as leavingDate,\n" +
            "             max(dt_last_commit) as dt_last_commit,\n" +
            "             max(dt_last_pr)     as dt_last_pr,\n" +
            "             max(dt_last_task)   as dt_last_task\n" +
            "         from po_tld_team_configuration config\n" +
            "         join po_emp_id using (id_employee)\n"+
            "         join po_emp_infra_"+enterpriseId+" using (id_emp_id)\n"+
            "         where config.id_team in (select teamId from team_info) group by id_team, id_employee, ts_start_date, ts_end_date\n" +
            "     )\n";
  }

  private String getTeamsQueryForUser() {
    return "select\n" +
            "      teamId,\n" +
            "      teamName,\n" +
            "      teamCreatedAt,\n" +
            "      ownerId,\n" +
            "      ownerName,\n" +
            "      ownerEmail,\n" +
            "      leadId,\n" +
            "      leadName,\n" +
            "      leadEmail,\n" +
            "      leadPermissionCode,\n" +
            "      showTeam,\n" +
            "      permissionCode,\n" +
            "      developerId,\n" +
            "      joiningDate,\n" +
            "      leavingDate,\n" +
            "      array_remove(array[ \n" +
            "                 case when count(membersInfo.dt_last_commit) != 0 then 'Commit' end, \n" +
            "                 case when count(membersInfo.dt_last_pr) != 0 then 'PR' end, \n" +
            "                 case when count(membersInfo.dt_last_task) != 0 then 'Task Tracker' end \n" +
            "             ], null) as activitySources \n" +
            "from team_info\n" +
            "join ownerInfo on team_info.ownerId = ownerInfo.owner_id\n" +
            "join leadInfo on team_info.leadId = leadInfo.lead_id and team_info.teamId = leadInfo.id_team\n" +
            "join membersInfo on team_info.teamId = membersInfo.team_id group by teamId, teamName, teamCreatedAt, ownerId, ownerName, ownerEmail, leadId, leadName, leadEmail, leadPermissionCode, showTeam, permissionCode, developerId, joiningDate, leavingDate;";
  }

}

