/*
Copyright 2019 GEOSIRIS

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
*/
package com.geosiris.webstudio.etp;

import Energistics.Etp.v12.Datatypes.MessageHeader;
import Energistics.Etp.v12.Protocol.GrowingObjectNotification.*;
import com.geosiris.etp.communication.ClientInfo;
import com.geosiris.etp.communication.Message;
import com.geosiris.etp.protocols.handlers.GrowingObjectNotificationHandler;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.Collection;



public class GrowingObjectNotificationHandler_WebStudio extends GrowingObjectNotificationHandler{
    public static Logger logger = LogManager.getLogger(GrowingObjectNotificationHandler_WebStudio.class);

    @Override
    public Collection<Message> on_PartSubscriptionEnded(PartSubscriptionEnded msg, MessageHeader msgHeader, ClientInfo clientInfo) {
        logger.info("[GrowingObjectNotificationHandler_WebStudio] received message" + msg);
        return new ArrayList<>();
    }

    @Override
    public Collection<Message> on_PartsChanged(PartsChanged msg, MessageHeader msgHeader, ClientInfo clientInfo) {
        logger.info("[GrowingObjectNotificationHandler_WebStudio] received message" + msg);
        return new ArrayList<>();
    }

    @Override
    public Collection<Message> on_PartsDeleted(PartsDeleted msg, MessageHeader msgHeader, ClientInfo clientInfo) {
        logger.info("[GrowingObjectNotificationHandler_WebStudio] received message" + msg);
        return new ArrayList<>();
    }

    @Override
    public Collection<Message> on_PartsReplacedByRange(PartsReplacedByRange msg, MessageHeader msgHeader, ClientInfo clientInfo) {
        logger.info("[GrowingObjectNotificationHandler_WebStudio] received message" + msg);
        return new ArrayList<>();
    }

    @Override
    public Collection<Message> on_SubscribePartNotifications(SubscribePartNotifications msg, MessageHeader msgHeader, ClientInfo clientInfo) {
        logger.info("[GrowingObjectNotificationHandler_WebStudio] received message" + msg);
        return new ArrayList<>();
    }

    @Override
    public Collection<Message> on_SubscribePartNotificationsResponse(SubscribePartNotificationsResponse msg, MessageHeader msgHeader, ClientInfo clientInfo) {
        logger.info("[GrowingObjectNotificationHandler_WebStudio] received message" + msg);
        return new ArrayList<>();
    }

    @Override
    public Collection<Message> on_UnsolicitedPartNotifications(UnsolicitedPartNotifications msg, MessageHeader msgHeader, ClientInfo clientInfo) {
        logger.info("[GrowingObjectNotificationHandler_WebStudio] received message" + msg);
        return new ArrayList<>();
    }

    @Override
    public Collection<Message> on_UnsubscribePartNotification(UnsubscribePartNotification msg, MessageHeader msgHeader, ClientInfo clientInfo) {
        logger.info("[GrowingObjectNotificationHandler_WebStudio] received message" + msg);
        return new ArrayList<>();
    }


}