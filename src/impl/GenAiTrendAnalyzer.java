package com.blueoptima.uix.dao.impl;

import com.blueoptima.uix.common.OverviewChartGroup;
import com.blueoptima.uix.dto.aiImpact.GenAIFilterInfo;
import com.blueoptima.uix.dto.aiImpact.Metric;
import com.blueoptima.uix.util.SQLUtil;
import org.hibernate.SessionFactory;
import org.hibernate.query.NativeQuery;
import org.hibernate.transform.Transformers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
public class GenAiTrendAnalyzer {

    @Autowired
    private SessionFactory sessionFactory;

    public List<Metric> analyzeBceMetricsTrend(String query, Long enterpriseId, Integer userId, Boolean isSuperUser, GenAIFilterInfo request,
                                               Map<String, List<Integer>> devLicenseStats, boolean includeEnterpriseAvg, String startMonth, String endMonth, List<OverviewChartGroup> group, boolean isMonthlyTrend, boolean isGroupingCountries, SessionFactory sessionFactory) {
        NativeQuery<Metric> nativeQuery = sessionFactory.getCurrentSession().createNativeQuery(query);

        // Set query parameters
        nativeQuery.setParameter("enterpriseId", enterpriseId);
        nativeQuery.setParameter("userId", userId);
        nativeQuery.setParameter("isSuperUser", isSuperUser);
        nativeQuery.setParameter("startMonth", startMonth);
        nativeQuery.setParameter("endMonth", endMonth);

        // Set dev license stats parameters
        for (Map.Entry<String, List<Integer>> entry : devLicenseStats.entrySet()) {
            nativeQuery.setParameterList(entry.getKey(), entry.getValue());
        }

        // Set other parameters from the request
        SQLUtil.setParameter(nativeQuery, request.getParameters());

        // Set result transformer
        nativeQuery.setResultTransformer(Transformers.aliasToBean(Metric.class));

        // Execute query and get results
        List<Metric> metrics = nativeQuery.list();

        // Post-processing of metrics if needed
        if (isMonthlyTrend) {
            metrics = processMonthlyTrend(metrics);
        }

        if (isGroupingCountries) {
            metrics = groupByCountries(metrics);
        }

        return metrics;
    }

    private List<Metric> processMonthlyTrend(List<Metric> metrics) {
        // Implement logic to process monthly trend
        // This might involve grouping metrics by month, calculating averages, etc.
        return metrics;
    }

    private List<Metric> groupByCountries(List<Metric> metrics) {
        // Implement logic to group metrics by countries
        // This might involve aggregating metrics for each country
        return metrics;
    }
}