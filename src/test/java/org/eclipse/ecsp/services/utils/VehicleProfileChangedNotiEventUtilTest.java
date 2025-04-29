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

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.eclipse.ecsp.domain.VehicleProfileNotificationEventDataV1_1;
import org.eclipse.ecsp.entities.IgniteEntity;
import org.eclipse.ecsp.entities.IgniteEventImpl;
import org.eclipse.ecsp.nosqldao.IgniteQuery;
import org.eclipse.ecsp.nosqldao.mongodb.IgniteBaseDAOMongoImpl;
import org.eclipse.ecsp.services.ServiceCommonTestConfig;
import org.eclipse.ecsp.services.ServicesTestBase;
import org.eclipse.ecsp.services.constants.Constants;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;

@SpringBootTest
@TestPropertySource("classpath:/application.properties")
@ContextConfiguration(classes = ServiceCommonTestConfig.class)
class VehicleProfileChangedNotiEventUtilTest extends ServicesTestBase {
    final String vin = "vin666666";
    final String uid = "user007";
    @Mock
    IgniteBaseDAOMongoImpl<?, ? extends IgniteEntity> igniteBaseDaoMongo;
    @Autowired
    private VehicleProfileChangedNotificationEventUtil util;
    
    @Test
    void getChangeDescriptionOldUserIdEventIsNull() {
        List<String> list = util.getChangeDescriptionOldUserId(null);
        
        assertNotNull(list);
        assertEquals(0, list.size());
    }
    
    @Test
    void getChangeDescriptionOldUserIdEventDataIsNull() {
        List<String> list = util.getChangeDescriptionOldUserId(new IgniteEventImpl());
        
        assertNotNull(list);
        assertEquals(0, list.size());
    }
    
    @Test
    void getChangeDescriptionOldUserIdChangeDescriptionListIsNull() {
        IgniteEventImpl igniteEvent = new IgniteEventImpl();
        VehicleProfileNotificationEventDataV1_1 data = new VehicleProfileNotificationEventDataV1_1();
        
        // null
        data.setChangeDescriptions(null);
        
        List<String> list = util.getChangeDescriptionOldUserId(igniteEvent);
        
        assertNotNull(list);
        assertEquals(0, list.size());
    }
    
    @Test
    void getChangeDescriptionOldUserIdChangeDescriptionListIsEmpty() {
        IgniteEventImpl igniteEvent = new IgniteEventImpl();
        VehicleProfileNotificationEventDataV1_1 data = new VehicleProfileNotificationEventDataV1_1();
        
        // empty
        data.setChangeDescriptions(Collections.emptyList());
        
        List<String> list = util.getChangeDescriptionOldUserId(igniteEvent);
        
        assertNotNull(list);
        assertEquals(0, list.size());
    }
    
    @Test
    void processVehicleProfileChangedNotiEvent() {
        
        VehicleProfileNotificationEventDataV1_1.ChangeDescription cd =
            new VehicleProfileNotificationEventDataV1_1.ChangeDescription();
        cd.setKey(VehicleProfileChangedNotificationEventUtil.CHANGE_DESCRIPTION_KEY);
        cd.setChanged(null);
        Map<String, String> user = new HashMap<>();
        user.put(VehicleProfileChangedNotificationEventUtil.USER_ID_KEY, uid);
        List<Map<String, String>> oldList = List.of(user);
        cd.setOld(oldList);
        
        // old is null
        VehicleProfileNotificationEventDataV1_1.ChangeDescription cdOldNull =
            new VehicleProfileNotificationEventDataV1_1.ChangeDescription();
        cdOldNull.setKey(VehicleProfileChangedNotificationEventUtil.CHANGE_DESCRIPTION_KEY);
        cdOldNull.setOld(null);
        
        // changed not null
        VehicleProfileNotificationEventDataV1_1.ChangeDescription cdChangedNotNull =
            new VehicleProfileNotificationEventDataV1_1.ChangeDescription();
        cdChangedNotNull.setKey(VehicleProfileChangedNotificationEventUtil.CHANGE_DESCRIPTION_KEY);
        cdChangedNotNull.setChanged(new ArrayList());
        VehicleProfileNotificationEventDataV1_1 data = new VehicleProfileNotificationEventDataV1_1();
        data.setChangeDescriptions(Arrays.asList(cdOldNull, cdChangedNotNull, cd));
        IgniteEventImpl igniteEvent = new IgniteEventImpl();
        igniteEvent.setEventData(data);
        igniteEvent.setVehicleId(vin);
        
        util.processDisassociateEvent(igniteEvent, this::doAssert);
    }
    
    private void doAssert(String vehicleId, String userId) {
        assertEquals(vin, vehicleId);
        assertEquals(uid, userId);
    }
    
    @Test
    void deleteData() {
        Mockito.when(igniteBaseDaoMongo.deleteByQuery(any(IgniteQuery.class))).thenReturn(1);
        
        VehicleProfileNotificationEventDataV1_1.ChangeDescription cd =
            new VehicleProfileNotificationEventDataV1_1.ChangeDescription();
        cd.setKey(VehicleProfileChangedNotificationEventUtil.CHANGE_DESCRIPTION_KEY);
        cd.setChanged(null);
        Map<String, String> user = new HashMap<>();
        user.put(VehicleProfileChangedNotificationEventUtil.USER_ID_KEY, uid);
        List<Map<String, String>> oldList = List.of(user);
        cd.setOld(oldList);
        VehicleProfileNotificationEventDataV1_1 data = new VehicleProfileNotificationEventDataV1_1();
        data.setChangeDescriptions(List.of(cd));
        IgniteEventImpl igniteEvent = new IgniteEventImpl();
        igniteEvent.setEventData(data);
        igniteEvent.setVehicleId(vin);
        
        // ---
        
        Map<String, String> kv = new HashMap<>();
        
        kv.put(Constants.MAP_KEY_VEHICLE_ID_PATH, "event.vehicleId");
        kv.put(Constants.MAP_KEY_USER_ID_PATH, "event.eventData.userId");
        
        ImmutablePair<IgniteBaseDAOMongoImpl<? , ? extends IgniteEntity>, Map<String, String>> pair =
            new ImmutablePair<>(igniteBaseDaoMongo, kv);
        List<ImmutablePair<IgniteBaseDAOMongoImpl<?, ? extends IgniteEntity>, Map<String, String>>> list = List.of(pair);
        
        util.deleteData(igniteEvent, list);
        
        Mockito.verify(igniteBaseDaoMongo, times(1)).deleteByQuery(any(IgniteQuery.class));
    }
}