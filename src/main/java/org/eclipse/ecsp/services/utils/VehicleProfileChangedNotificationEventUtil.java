/********************************************************************************
 * Copyright (c) 2023-24 Harman International
 * 
 * <p>Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * <p>http://www.apache.org/licenses/LICENSE-2.0
 * 
 * <p>Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and\
 * limitations under the License.
 * 
 * <p>SPDX-License-Identifier: Apache-2.0
 ********************************************************************************/

package org.eclipse.ecsp.services.utils;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.eclipse.ecsp.domain.Constants;
import org.eclipse.ecsp.domain.VehicleProfileNotificationEventDataV1_1;
import org.eclipse.ecsp.entities.IgniteEntity;
import org.eclipse.ecsp.entities.IgniteEvent;
import org.eclipse.ecsp.nosqldao.IgniteCriteria;
import org.eclipse.ecsp.nosqldao.IgniteCriteriaGroup;
import org.eclipse.ecsp.nosqldao.IgniteQuery;
import org.eclipse.ecsp.nosqldao.Operator;
import org.eclipse.ecsp.nosqldao.mongodb.IgniteBaseDAOMongoImpl;
import org.eclipse.ecsp.utils.logger.IgniteLogger;
import org.eclipse.ecsp.utils.logger.IgniteLoggerFactory;
import org.springframework.stereotype.Component;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiConsumer;

/**
 * utility to process vehicle profile changed notification.<br/>
 * change notification for user association/disassociation
 */
@Component
public class VehicleProfileChangedNotificationEventUtil {

    /**
     * authorizedUsers.
     */
    public static final String CHANGE_DESCRIPTION_KEY = "authorizedUsers";
    /**
     * userId.
     */
    public static final String USER_ID_KEY = "userId";
    private static final IgniteLogger LOGGER =
        IgniteLoggerFactory.getLogger(VehicleProfileChangedNotificationEventUtil.class);
    
    /**
     * Fetch list old userId from VEHICLE_PROFILE_CHANGED_NOTIFICATION_EVENT.
     * user disassociation to the vehicle.
     *
     * @param igniteEvent VEHICLE_PROFILE_CHANGED_NOTIFICATION_EVENT
     * @return list if user which are disassociated from vehicle, <br/>
     *     return null if not a user disassociation change.
     */
    public List<String> getChangeDescriptionOldUserId(IgniteEvent igniteEvent) {
        if (igniteEvent == null || igniteEvent.getEventData() == null) {
            LOGGER.debug("IgniteEvent or EventData is null");
            return Collections.emptyList();
        }

        VehicleProfileNotificationEventDataV1_1 data =
            (VehicleProfileNotificationEventDataV1_1) igniteEvent.getEventData();
        
        List<VehicleProfileNotificationEventDataV1_1.ChangeDescription> changeDescriptionList =
            data.getChangeDescriptions();
        
        if (changeDescriptionList == null || changeDescriptionList.isEmpty()) {
            LOGGER.debug("EventData.ChangeDescriptions is null or empty");
            return Collections.emptyList();
        }
        
        return changeDescriptionList.stream()
            .filter(changeDescription -> CHANGE_DESCRIPTION_KEY.equals(changeDescription.getKey()))
            .filter(changeDescription -> changeDescription.getChanged() == null)
            .filter(changeDescription -> changeDescription.getOld() != null)
            .map(changeDescription -> (List<Map<String, String>>) changeDescription.getOld())
            .flatMap(userList -> userList.stream().map(u -> u.get(USER_ID_KEY)))
            .toList();
    }
    
    /**
     * Checks for user disassociate event in VEHICLE_PROFILE_CHANGED_NOTIFICATION_EVENT.<br/>
     * execute biConsumer if the user is disassociated from the vehicle.
     *
     * @param igniteEvent VEHICLE_PROFILE_CHANGED_NOTIFICATION_EVENT
     * @param biConsumer  execute if the user is disassociated from the vehicle
     */
    public void processDisassociateEvent(IgniteEvent igniteEvent,
                                         BiConsumer<String, String> biConsumer) {
        String vehicleId = igniteEvent.getVehicleId();
        
        getChangeDescriptionOldUserId(igniteEvent).stream()
            .forEach(userId -> biConsumer.accept(vehicleId, userId));
    }
    
    /**
     * Checks for user disassociate event in VEHICLE_PROFILE_CHANGED_NOTIFICATION_EVENT.<br/>
     * delete data from mongo DB based on the yserId and vehicleId on provided mongo collection.
     *
     * @param igniteEvent VEHICLE_PROFILE_CHANGED_NOTIFICATION_EVENT
     * @param list        list of mongo collection from which the record to be deleted.
     */
    public void deleteData(IgniteEvent igniteEvent,
                           List<ImmutablePair<IgniteBaseDAOMongoImpl<?, ? extends IgniteEntity>,
                               Map<String, String>>> list) {
        processDisassociateEvent(igniteEvent,
            (vehicleId, userId) -> deleteDataByVehicleIdAndUserId(list, vehicleId, userId)
        );
    }
    
    /**
     * delete data from mongo DB based on the yserId and vehicleId on provided mongodb collection.
     *
     * @param daoAndPathMapList list of mongodb collection from which the record to be deleted.
     * @param vehicleId         delete the records for the particular vehicle
     * @param userId            delete the records for the particular user
     */
    private void deleteDataByVehicleIdAndUserId(
        List<ImmutablePair<IgniteBaseDAOMongoImpl<?, ? extends IgniteEntity>,
            Map<String, String>>> daoAndPathMapList,
        String vehicleId,
        String userId) {
        LOGGER.info("Prepare delete vehicleId: {}, userId: {}", vehicleId, userId);
        
        for (ImmutablePair<IgniteBaseDAOMongoImpl<?, ? extends IgniteEntity>,
            Map<String, String>> daoAndPathMapPair : daoAndPathMapList) {
            IgniteBaseDAOMongoImpl<?, ? extends IgniteEntity> igniteBaseDao = daoAndPathMapPair.getLeft();
            
            Map<String, String> pathMap = daoAndPathMapPair.getRight();
            
            String vehicleIdPath = pathMap.get(Constants.MAP_KEY_VEHICLE_ID_PATH);
            String userIdPath = pathMap.get(Constants.MAP_KEY_USER_ID_PATH);
            
            deleteDataByVehicleIdAndUserId(igniteBaseDao, vehicleIdPath, vehicleId, userIdPath, userId);
        }
    }
    
    /**
     * Delete record for userId and vehicle in provided collection.
     *
     * @param igniteBaseDaoMongo collection from which record should be deleted
     * @param vehicleIdPath      vehicle id path in the record
     * @param vehicleId          vehicle id which record should be deleted
     * @param userIdPath         user id path in the record
     * @param userId             user id which record should be deleted
     */
    public void deleteDataByVehicleIdAndUserId(IgniteBaseDAOMongoImpl<?, ? extends IgniteEntity> igniteBaseDaoMongo,
                                               String vehicleIdPath,
                                               String vehicleId,
                                               String userIdPath,
                                               String userId) {
        Optional<IgniteQuery> optIgniteQuery =
            createIgniteQuery(vehicleIdPath, vehicleId, userIdPath, userId);
        
        if (optIgniteQuery.isPresent()) {
            LOGGER.info("deleting data for vehicleId: {}, userId: {} from {}", vehicleId, userId,
                igniteBaseDaoMongo.getClass().getCanonicalName());
            igniteBaseDaoMongo.deleteByQuery(optIgniteQuery.get());
        } else {
            LOGGER.debug("optIgniteQuery is null");
        }
    }
    
    private Optional<IgniteQuery> createIgniteQuery(String vehicleIdPath, String vehicleId,
                                                    String userIdPath, String userId) {
        if (StringUtils.isEmpty(vehicleIdPath) && StringUtils.isEmpty(userIdPath)) {
            LOGGER.debug("vehicleId path and userId path not found");
            return Optional.empty();
        }
        
        IgniteCriteriaGroup criteriaGroup = new IgniteCriteriaGroup();
        
        if (vehicleIdPath != null) {
            IgniteCriteria icEqVehicleId = new IgniteCriteria(vehicleIdPath, Operator.EQ, vehicleId);
            criteriaGroup.and(icEqVehicleId);
        }
        
        if (userIdPath != null) {
            IgniteCriteria icEqUserId = new IgniteCriteria(userIdPath, Operator.IN,
                Arrays.asList(userId, Constants.USER_ID_UNKNOWN));
            criteriaGroup.and(icEqUserId);
        }
        
        return Optional.of(new IgniteQuery(criteriaGroup));
    }
}
