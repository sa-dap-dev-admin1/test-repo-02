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

package com.blueoptima.uix.controller;

import com.blueoptima.uix.SkipValidationCheck;
import com.blueoptima.uix.common.UserContext;
import com.blueoptima.uix.controller.constants.EndpointConstantsV2;

import com.blueoptima.uix.dto.Message;
import com.blueoptima.uix.dto.favorites.overview.FavoriteOverviewSchema;
import com.blueoptima.uix.security.UserToken;
import com.blueoptima.uix.security.auth.AccessCode;
import com.blueoptima.iam.dto.PermissionsCode.Groups;
import com.blueoptima.uix.service.FavoriteOverviewService;


import com.blueoptima.uix.service.exceptions.DuplicateEntryException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

import static com.blueoptima.uix.controller.constants.APINotes.REPORTING;

/**
 * Created by yadavsu on 12/12/16.
 */
// Revision : overview_favorites_revision_11
@RestController

public class FavoriteOverviewController {

    private static final Logger log = LoggerFactory.getLogger(FavoriteOverviewController.class);

    @Autowired
    private FavoriteOverviewService favoriteOverviewService;


    
    @RequestMapping(name = REPORTING + "GetAllFavoriteOverviews",value = EndpointConstantsV2.FAVORITES_OVERVIEW, method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @AccessCode(Groups.OVERVIEW_CHARTS)
    @SkipValidationCheck
    public Map<Long, FavoriteOverviewSchema> getAllFavoriteOverviews() {
        return favoriteOverviewService.getAllFavoriteOverviews();
    }


    
    @RequestMapping(name = REPORTING + "GetFavoriteOverviewById",value = EndpointConstantsV2.FAVORITES_OVERVIEW + "/{favoriteId}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @AccessCode(Groups.OVERVIEW_CHARTS)
    @SkipValidationCheck
    public FavoriteOverviewSchema getFavoriteOverview(@PathVariable Long favoriteId) {
        UserToken userToken = UserContext.getUserToken();
        return favoriteOverviewService.getFavoriteOverview(userToken, favoriteId);
    }


    
    @RequestMapping(name = REPORTING + "PostFavoriteOverview",value = EndpointConstantsV2.FAVORITES_OVERVIEW, method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @AccessCode(Groups.OVERVIEW_CHARTS)
    @SkipValidationCheck
    public Message postFavoriteOverview(@RequestBody FavoriteOverviewSchema favoriteSchema) {
        favoriteSchema.validate();
        String msgTxt;
        Long favoriteId = null;
        try {
            favoriteId = favoriteOverviewService.postFavoriteOverview(favoriteSchema);
            msgTxt = "FAVORITE.ADD.SUCCESS";
        } catch (DuplicateEntryException e) {
            log.error("Error posting favorite overview", e);
            msgTxt = "DUPLICATE.FAVORITE.FOUND";
            favoriteId = e.getDuplicateId();
        } catch (Exception e) {
            log.error("error occurred while saving favorite", e);
            msgTxt = "FAVORITE.ADD.FAIL";
        }
        Message message = new Message(msgTxt);
        message.put("favoriteId", favoriteId);
        return message;
    }


     
    @RequestMapping(name = REPORTING + "DeleteFavoriteOverview",value = EndpointConstantsV2.FAVORITES_OVERVIEW + "/{favoriteId}", method = RequestMethod.DELETE, produces = MediaType.APPLICATION_JSON_VALUE)
    @AccessCode(Groups.OVERVIEW_CHARTS)
    @SkipValidationCheck
    public Message deleteFavoriteOverview(@PathVariable Long favoriteId) {
        try {
            favoriteOverviewService.deleteFavoriteOverview(favoriteId);
        } catch (Exception e) {
            log.error("error occurred while deleting favorite", e);
            return new Message("FAVORITE.DELETE.FAIL");
        }
        return new Message("FAVORITE.DELETE.SUCCESS");
    }


    
    @RequestMapping(name = REPORTING + "UpdateFavoriteOverview",value = EndpointConstantsV2.FAVORITES_OVERVIEW + "/{favoriteId}", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @AccessCode(Groups.OVERVIEW_CHARTS)
    @SkipValidationCheck
    public Message updateFavoriteOverview(@PathVariable Long favoriteId, @RequestBody FavoriteOverviewSchema favoriteSchema) {
        favoriteSchema.validate();
        favoriteOverviewService.updateFavoriteOverview(favoriteId, favoriteSchema);
        return new Message("FAVORITE.UPDATE.SUCCESS");
    }

}