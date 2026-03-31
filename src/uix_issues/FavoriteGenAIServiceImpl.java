/*
 * BlueOptima Limited CONFIDENTIAL
 * Unpublished Copyright (c) 2008-2016 BlueOptima, All Rights Reserved.
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
 */

package com.blueoptima.uix.service.impl;

import com.blueoptima.uix.auth.FavoriteGenAIAuthDao;
import com.blueoptima.uix.common.OverviewChartDateOption;
import com.blueoptima.uix.common.OverviewChartDateRange;
import com.blueoptima.uix.common.UserContext;
import com.blueoptima.uix.controller.exception.UnauthorizedAccessException;
import com.blueoptima.uix.dao.model.hibernate.FavGenAIConfDeveloperModel;
import com.blueoptima.uix.dao.model.hibernate.FavGenAIConfEmployerModel;
import com.blueoptima.uix.dao.model.hibernate.FavGenAIConfLocationModel;
import com.blueoptima.uix.dao.model.hibernate.FavGenAIConfOrgModel;
import com.blueoptima.uix.dao.model.hibernate.FavGenAIConfConfigurationsModel;
import com.blueoptima.uix.dao.model.hibernate.FavoriteGenAIModel;
import com.blueoptima.uix.dto.favorites.Configuration;
import com.blueoptima.uix.dto.favorites.Favorites;
import com.blueoptima.uix.dto.favorites.genai.FavoriteGenAISchema;
import com.blueoptima.uix.dto.favorites.genai.GenAIDateRange;
import com.blueoptima.uix.security.UserToken;
import com.blueoptima.uix.service.FavoriteGenAIService;
import com.blueoptima.uix.service.FavoritesService;
import com.blueoptima.uix.service.exceptions.DuplicateEntryException;
import com.blueoptima.uix.util.CollectionUtil;
import com.blueoptima.uix.util.DateUtil;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service implementation for GenAI favorites
 */
@Service
public class FavoriteGenAIServiceImpl implements FavoriteGenAIService {

    @Autowired
    private FavoriteGenAIAuthDao favoriteGenAIAuthDao;

    @Autowired
    private FavoritesService favoritesService;

    private static final Logger logger = LoggerFactory.getLogger(FavoriteGenAIServiceImpl.class);

    public static final String INPUT_DATE_FORMAT = "yyyy-MM";

    @Override
    @Transactional(timeout = 120)
    public Map<Long, FavoriteGenAISchema> getAllFavoriteGenAIs() {
        List<FavoriteGenAIModel> favoriteGenAIList = favoriteGenAIAuthDao.getAllFavoriteGenAIs();
        Map<Long, FavoriteGenAISchema> favoritesResponseMap = new HashMap<>(favoriteGenAIList.size());
        for (FavoriteGenAIModel model : favoriteGenAIList) {
            FavoriteGenAISchema favoriteGenAISchema = modelToSchemaConverter(model);
            favoritesResponseMap.put(model.getFavoriteId(), favoriteGenAISchema);
        }
        return favoritesResponseMap;
    }

    @Override
    @Transactional(timeout = 120)
    public FavoriteGenAISchema getFavoriteGenAI(UserToken userToken, Long favoriteId) {
        FavoriteGenAIModel model = favoriteGenAIAuthDao.getFavoriteGenAI(favoriteId);
        if (!userToken.getUserId().equals(model.getUser().getUserId())) {
            throw new UnauthorizedAccessException("Unauthorized Access to Favorite", CollectionUtil
                .createMap(com.blueoptima.uix.common.ExceptionCodes.FAVORITE_IDS, Collections.singletonList(favoriteId)));
        }
        return modelToSchemaConverter(model);
    }

    private FavoriteGenAISchema modelToSchemaConverter(FavoriteGenAIModel model) {
        FavoriteGenAISchema favoriteGenAISchema = new FavoriteGenAISchema();
        favoriteGenAISchema.setId(model.getFavoriteId());
        favoriteGenAISchema.setName(model.getName());
        favoriteGenAISchema.setType("genai");

        // Set if the user accessing owns the fav
        if (model.getUser() != null &&
            model.getUser().getUserId().equals(UserContext.getUserToken().getUserId())) {
            favoriteGenAISchema.setOwned(true);
        }

        // Setting the order of filters
        favoriteGenAISchema.setFilterOrder(model.getFilterOrder());

        // Set groupLevel
        favoriteGenAISchema.setGroupLevel(model.getGroupLevel());

        // Set pageView (UI sub-page/view identifier)
        favoriteGenAISchema.setPageView(model.getPageView());

        if (model.getCustomDateSelect() != null) {
            GenAIDateRange range = new GenAIDateRange();
            range.setOption(model.getCustomDateSelect() ? OverviewChartDateOption.CUSTOM.getValue() : OverviewChartDateOption.STANDARD.getValue());
            if (model.getCustomDateSelect()) {
                Map<String, String> custom = new HashMap<>();
                custom.put("from", DateUtil.toDbFormat(model.getStartDate(), INPUT_DATE_FORMAT));
                custom.put("to", DateUtil.toDbFormat(model.getEndDate(), INPUT_DATE_FORMAT));
                range.setCustom(custom);
            } else {
                String dateRange = OverviewChartDateRange.getValue(model.getDateRange()).toString();
                range.setStandard(dateRange);
                favoriteGenAISchema.setDateRange(dateRange);
            }
            favoriteGenAISchema.setRange(range);
        }

        // Set configuration
        Configuration configuration = configurationConverter(model);
        favoriteGenAISchema.setConfiguration(configuration);

        return favoriteGenAISchema;
    }

    private Configuration configurationConverter(FavoriteGenAIModel model) {
        if (model == null) {
            return null;
        }
        Configuration conf = new Configuration();

        if (CollectionUtil.notNullAndEmpty(model.getConfOrgs())) {
            List<Long> organizationIds = new ArrayList<>(model.getConfOrgs().size());
            conf.setOrganizationIds(organizationIds);
            for (FavGenAIConfOrgModel orgModel : model.getConfOrgs()) {
                organizationIds.add(orgModel.getOrgId());
            }
        }

        if (CollectionUtil.notNullAndEmpty(model.getConfDevelopers())) {
            List<Long> developerIds = new ArrayList<>(model.getConfDevelopers().size());
            conf.setDeveloperIds(developerIds);
            for (FavGenAIConfDeveloperModel confDeveloperModel : model.getConfDevelopers()) {
                developerIds.add(confDeveloperModel.getDeveloperId());
            }
        }

        if (CollectionUtil.notNullAndEmpty(model.getConfEmployers())) {
            List<Long> employerIds = new ArrayList<>(model.getConfEmployers().size());
            conf.setEmployerIds(employerIds);
            for (FavGenAIConfEmployerModel employerModel : model.getConfEmployers()) {
                employerIds.add(employerModel.getEmployerId());
            }
        }

        if (CollectionUtil.notNullAndEmpty(model.getConfLocations())) {
            List<Long> locationIds = new ArrayList<>(model.getConfLocations().size());
            conf.setLocationIds(locationIds);
            for (FavGenAIConfLocationModel confLocationModel : model.getConfLocations()) {
                locationIds.add(confLocationModel.getLocationId());
            }
        }

        if (model.getConfigurations() != null) {
            conf.setLineManagerIds(model.getConfigurations().getLineManagerIds());
            conf.setCountries(model.getConfigurations().getCountries());
        }

        return conf;
    }

    @Override
    @Transactional
    public Long postFavoriteGenAI(FavoriteGenAISchema favoriteSchema) {
        validateDuplicateFavoriteFromSameUser(favoriteSchema.getName());

        FavoriteGenAIModel model = new FavoriteGenAIModel();
        model.setCreatedDate(new Date(System.currentTimeMillis()));
        FavoriteGenAIModel favoriteGenAIModel = copyToModel(favoriteSchema, model, true);
        favoriteGenAIAuthDao.saveOrUpdate(favoriteGenAIModel);

        return favoriteGenAIModel.getFavoriteId();
    }

    private void validateDuplicateFavoriteFromSameUser(String favoriteName) {
        UserToken userToken = UserContext.getUserToken();
        Favorites favorites = favoritesService.getFavoriteByUserIdAndFavoriteName(userToken.getUserId(), favoriteName);
        if (favorites != null) {
            throw new DuplicateEntryException("Duplicate favorites found with name : " + favorites.getName(), favorites.getId());
        }
    }

    private void populateStandardDateRange(FavoriteGenAIModel model, String dateRange) {
        model.setCustomDateSelect(false);
        model.setDateRange(OverviewChartDateRange.getValue(dateRange).getIndex());
        model.setStartDate(null);
        model.setEndDate(null);
    }

    private FavoriteGenAIModel copyToModel(FavoriteGenAISchema favoriteSchema, FavoriteGenAIModel model, boolean isNewModel) {
        model.setName(favoriteSchema.getName());
        if (favoriteSchema.getRange() != null) {
            OverviewChartDateOption option = OverviewChartDateOption.getValue(favoriteSchema.getRange().getOption());
            if (option == OverviewChartDateOption.STANDARD) {
                populateStandardDateRange(model, favoriteSchema.getRange().getStandard());
            } else if (option == OverviewChartDateOption.CUSTOM) {
                model.setCustomDateSelect(true);
                model.setStartDate(DateUtil.toDbFormat(favoriteSchema.getRange().getCustom().get("from"), INPUT_DATE_FORMAT));
                model.setEndDate(DateUtil.toDbFormat(favoriteSchema.getRange().getCustom().get("to"), INPUT_DATE_FORMAT));
                model.setDateRange(null);
            }
        }

        if (favoriteSchema.getDateRange() != null) {
            populateStandardDateRange(model, favoriteSchema.getDateRange());
        }

        // Set order -1 only when new favorite is getting added
        if (isNewModel) {
            model.setOrder(-1);
        }

        model.setMinimized(false);

        // Set filter's order - default to ["developers"] if not provided
        if (favoriteSchema.getFilterOrder() != null && !favoriteSchema.getFilterOrder().isEmpty()) {
            model.setFilterOrder(favoriteSchema.getFilterOrder());
        } else {
            List<String> defaultFilterOrder = new ArrayList<>();
            defaultFilterOrder.add("developers");
            model.setFilterOrder(defaultFilterOrder);
        }

        // Set groupLevel
        model.setGroupLevel(favoriteSchema.getGroupLevel());

        // Set pageView (UI sub-page/view identifier)
        model.setPageView(favoriteSchema.getPageView());

        // Set configurations
        setModelConfigurations(favoriteSchema, model);

        return model;
    }

    private void setModelConfigurations(FavoriteGenAISchema favoriteSchema, FavoriteGenAIModel model) {
        if (favoriteSchema.getConfiguration() == null) {
            return;
        }

        Configuration configuration = favoriteSchema.getConfiguration();

        model.setConfOrgs(new ArrayList<>());
        if (configuration.getOrganizationIds() != null) {
            List<FavGenAIConfOrgModel> orgList = new ArrayList<>();
            for (Long orgId : new HashSet<>(configuration.getOrganizationIds())) {
                FavGenAIConfOrgModel orgModel = new FavGenAIConfOrgModel();
                orgModel.setFavoriteGenAI(model);
                orgModel.setOrgId(orgId);
                orgList.add(orgModel);
            }
            model.setConfOrgs(orgList);
        }

        model.setConfDevelopers(new ArrayList<>());
        if (configuration.getDeveloperIds() != null) {
            List<FavGenAIConfDeveloperModel> devList = new ArrayList<>();
            for (Long developerId : new HashSet<>(configuration.getDeveloperIds())) {
                FavGenAIConfDeveloperModel confDeveloperModel = new FavGenAIConfDeveloperModel();
                confDeveloperModel.setFavoriteGenAI(model);
                confDeveloperModel.setDeveloperId(developerId);
                devList.add(confDeveloperModel);
            }
            model.setConfDevelopers(devList);
        }

        model.setConfEmployers(new ArrayList<>());
        if (configuration.getEmployerIds() != null) {
            List<FavGenAIConfEmployerModel> employerList = new ArrayList<>();
            for (Long employerId : new HashSet<>(configuration.getEmployerIds())) {
                FavGenAIConfEmployerModel employerModel = new FavGenAIConfEmployerModel();
                employerModel.setFavoriteGenAI(model);
                employerModel.setEmployerId(employerId);
                employerList.add(employerModel);
            }
            model.setConfEmployers(employerList);
        }

        model.setConfLocations(new ArrayList<>());
        if (configuration.getLocationIds() != null) {
            List<FavGenAIConfLocationModel> locList = new ArrayList<>();
            for (Long locationId : new HashSet<>(configuration.getLocationIds())) {
                FavGenAIConfLocationModel confLocationModel = new FavGenAIConfLocationModel();
                confLocationModel.setFavoriteGenAI(model);
                confLocationModel.setLocationId(locationId);
                locList.add(confLocationModel);
            }
            model.setConfLocations(locList);
        }

        // Set lineManagerIds and countries in configurations table
        if ((configuration.getLineManagerIds() != null && !configuration.getLineManagerIds().isEmpty()) ||
            (configuration.getCountries() != null && !configuration.getCountries().isEmpty())) {
            FavGenAIConfConfigurationsModel configurationsModel = new FavGenAIConfConfigurationsModel();
            configurationsModel.setFavoriteGenAI(model);
            if (configuration.getLineManagerIds() != null && !configuration.getLineManagerIds().isEmpty()) {
                configurationsModel.setLineManagerIds(configuration.getLineManagerIds());
            }
            if (configuration.getCountries() != null && !configuration.getCountries().isEmpty()) {
                configurationsModel.setCountries(configuration.getCountries());
            }
            model.setConfigurations(configurationsModel);
        }
    }

    @Override
    @Transactional
    public void deleteFavoriteGenAI(Long favoriteId) {
        favoriteGenAIAuthDao.deleteGenAIFav(favoriteId);
    }

    @Override
    @Transactional
    public void updateFavoriteGenAI(Long favoriteId, FavoriteGenAISchema favoriteSchema) {
        FavoriteGenAIModel model = favoriteGenAIAuthDao.getFavoriteGenAI(favoriteId);
        UserToken userToken = UserContext.getUserToken();
        if (!userToken.getUserId().equals(model.getUser().getUserId())) {
            throw new UnauthorizedAccessException("Unauthorized Access to Favorite", CollectionUtil
                .createMap(com.blueoptima.uix.common.ExceptionCodes.FAVORITE_IDS, Collections.singletonList(favoriteId)));
        }
        // Delete existing configurations before updating
        favoriteGenAIAuthDao.deleteAllConfig(favoriteId);
        model.setConfOrgs(null);
        model.setConfDevelopers(null);
        model.setConfEmployers(null);
        model.setConfLocations(null);
        model.setConfigurations(null);
        copyToModel(favoriteSchema, model, false);
        favoriteGenAIAuthDao.saveOrUpdate(model);
    }
}
