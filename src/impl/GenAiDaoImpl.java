package com.blueoptima.uix.dao.impl;

import com.blueoptima.uix.common.OverviewChartGroup;
import com.blueoptima.uix.common.OverviewSortingColumn;
import com.blueoptima.uix.common.configs.ConfigurationProperties;
import com.blueoptima.uix.dao.GenAiDao;
import com.blueoptima.uix.dto.AceContribution;
import com.blueoptima.uix.dto.ProductivityImpact;
import com.blueoptima.uix.dto.aiImpact.AiImpactScatterChartResponse;
import com.blueoptima.uix.dto.aiImpact.GenAIFilterInfo;
import com.blueoptima.uix.dto.aiImpact.Metric;
import com.blueoptima.uix.dto.overview.OverviewFilterInfo;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Repository
public class GenAiDaoImpl implements GenAiDao {

    @Autowired
    private SessionFactory sessionFactory;

    @Autowired
    private ConfigurationProperties configurationProperties;

    @Autowired
    private GenAiQueryBuilder queryBuilder;

    @Autowired
    private GenAiMetricsCalculator metricsCalculator;

    @Autowired
    private GenAiLicenseHandler licenseHandler;

    @Autowired
    private GenAiTrendAnalyzer trendAnalyzer;

    @Override
    public Map<String, List<Integer>> getDevCategorisation(OverviewFilterInfo request, Long enterpriseId, Map<String, Object> parameters, StringBuilder filtersWithClause, Double aceReqForLastMonth) {
        String query = queryBuilder.getDevCategorisationQuery(request, enterpriseId, parameters, filtersWithClause, aceReqForLastMonth);
        List<Object[]> rows = sessionFactory.getCurrentSession().createNativeQuery(query).getResultList();
        return licenseHandler.buildLicenseStatsResponse(rows);
    }

    @Override
    public List<Integer> getDevContributionStats(OverviewFilterInfo request, Long enterpriseId, StringBuilder filtersWithClause, Map<String, Object> parameters, Double ceThreshold, Boolean isCountReq) {
        String query = queryBuilder.getDevContributionStatsQuery(request, enterpriseId, filtersWithClause, parameters, ceThreshold, isCountReq);
        return sessionFactory.getCurrentSession().createNativeQuery(query).list();
    }

    @Override
    public Boolean checkDataExists(String startMonth, StringBuilder getFiltersWithClauseQuery, Map<String, Object> parameters, Long enterpriseId) {
        return licenseHandler.checkDataExists(startMonth, getFiltersWithClauseQuery, parameters, enterpriseId, sessionFactory);
    }

    @Override
    public ProductivityImpact getProductivityImpact(OverviewFilterInfo request, List<Integer> devIds, Long enterpriseId) {
        String query = queryBuilder.getProductivityImpactQuery(request, devIds, enterpriseId);
        return metricsCalculator.calculateProductivityImpact(query, request, devIds, enterpriseId, sessionFactory);
    }

    @Override
    public List<AceContribution> getAceContribution(OverviewFilterInfo request, StringBuilder filtersWithClause, Map<String, Object> parameters, Long enterpriseId, OverviewChartGroup group, OverviewSortingColumn sortingColumn, Boolean isTrendChart, Boolean isClickthrough) {
        String query = queryBuilder.getAceContributionQuery(request, filtersWithClause, parameters, enterpriseId, group, sortingColumn, isTrendChart, isClickthrough);
        return metricsCalculator.calculateAceContribution(query, request, parameters, enterpriseId, group, sortingColumn, isTrendChart, isClickthrough, sessionFactory);
    }

    @Override
    public Map<String, AiImpactScatterChartResponse.Category> getBceMetricsForAllCategory(Long enterpriseId, Integer userId, Boolean isSuperUser, GenAIFilterInfo request,
                                                                                          Map<String, List<Integer>> devLicenseStats, boolean includeEnterpriseAvg, String startMonth, String endMonth, String sortingColumn, String sortingMode, List<OverviewChartGroup> group) {
        String query = queryBuilder.getBceMetricsQuery(enterpriseId, userId, isSuperUser, request, devLicenseStats, includeEnterpriseAvg, startMonth, endMonth, group, false, false, sortingColumn, sortingMode, false);
        return metricsCalculator.calculateBceMetrics(query, enterpriseId, userId, isSuperUser, request, devLicenseStats, includeEnterpriseAvg, startMonth, endMonth, sortingColumn, sortingMode, group, sessionFactory);
    }

    @Override
    public List<Metric> getBceMetricsforLicenseCategory(Long enterpriseId, Integer userId, Boolean isSuperUser, GenAIFilterInfo request,
                                                        Map<String, List<Integer>> devLicenseStats, boolean includeEnterpriseAvg, String startMonth, String endMonth, String sortingColumn, String sortingMode, List<OverviewChartGroup> group) {
        String query = queryBuilder.getBceMetricsQuery(enterpriseId, userId, isSuperUser, request, devLicenseStats, includeEnterpriseAvg, startMonth, endMonth, group, true, false, sortingColumn, sortingMode, false);
        return metricsCalculator.calculateBceMetricsForLicenseCategory(query, enterpriseId, userId, isSuperUser, request, devLicenseStats, includeEnterpriseAvg, startMonth, endMonth, sortingColumn, sortingMode, group, sessionFactory);
    }

    @Override
    public List<Map<String, Object>> getRollouts(String startMonth, String endMonth, Long enterpriseId) {
        String query = queryBuilder.getRolloutsQuery(startMonth, endMonth, enterpriseId);
        return sessionFactory.getCurrentSession().createSQLQuery(query).list();
    }

    @Override
    public List<Metric> getBceMetricsTrend(Long enterpriseId, Integer userId, Boolean isSuperUser, GenAIFilterInfo request,
                                           Map<String, List<Integer>> devLicenseStats, boolean includeEnterpriseAvg, String startMonth, String endMonth, List<OverviewChartGroup> group, boolean isMonthlyTrend, boolean isGroupingCountries) {
        String query = queryBuilder.getBceMetricsQuery(enterpriseId, userId, isSuperUser, request, devLicenseStats, includeEnterpriseAvg, startMonth, endMonth, group, false, true, null, null, isGroupingCountries);
        return trendAnalyzer.analyzeBceMetricsTrend(query, enterpriseId, userId, isSuperUser, request, devLicenseStats, includeEnterpriseAvg, startMonth, endMonth, group, isMonthlyTrend, isGroupingCountries, sessionFactory);
    }
}