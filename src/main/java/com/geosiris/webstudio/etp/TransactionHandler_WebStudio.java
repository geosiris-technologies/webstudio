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
import Energistics.Etp.v12.Protocol.Transaction.*;
import com.geosiris.etp.communication.ClientInfo;
import com.geosiris.etp.communication.Message;
import com.geosiris.etp.protocols.handlers.TransactionHandler;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.Collection;



public class TransactionHandler_WebStudio extends TransactionHandler{
    public static Logger logger = LogManager.getLogger(TransactionHandler_WebStudio.class);

    @Override
    public Collection<Message> on_CommitTransaction(CommitTransaction msg, MessageHeader msgHeader, ClientInfo clientInfo) {
        logger.info("[TransactionHandler_WebStudio] received message" + msg);
        return new ArrayList<>();
    }

    @Override
    public Collection<Message> on_CommitTransactionResponse(CommitTransactionResponse msg, MessageHeader msgHeader, ClientInfo clientInfo) {
        logger.info("[TransactionHandler_WebStudio] received message" + msg);
        return new ArrayList<>();
    }

    @Override
    public Collection<Message> on_RollbackTransaction(RollbackTransaction msg, MessageHeader msgHeader, ClientInfo clientInfo) {
        logger.info("[TransactionHandler_WebStudio] received message" + msg);
        return new ArrayList<>();
    }

    @Override
    public Collection<Message> on_RollbackTransactionResponse(RollbackTransactionResponse msg, MessageHeader msgHeader, ClientInfo clientInfo) {
        logger.info("[TransactionHandler_WebStudio] received message" + msg);
        return new ArrayList<>();
    }

    @Override
    public Collection<Message> on_StartTransaction(StartTransaction msg, MessageHeader msgHeader, ClientInfo clientInfo) {
        logger.info("[TransactionHandler_WebStudio] received message" + msg);
        return new ArrayList<>();
    }

    @Override
    public Collection<Message> on_StartTransactionResponse(StartTransactionResponse msg, MessageHeader msgHeader, ClientInfo clientInfo) {
        logger.info("[TransactionHandler_WebStudio] received message" + msg);
        return new ArrayList<>();
    }


}