/*
 *
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *
 */
package org.apache.qpid.server.handler;

import org.apache.qpid.AMQException;
import org.apache.qpid.framing.AMQMethodBody;
import org.apache.qpid.framing.AMQShortString;
import org.apache.qpid.framing.ConnectionOpenBody;
import org.apache.qpid.framing.ConnectionOpenOkBody;
import org.apache.qpid.protocol.AMQConstant;
import org.apache.qpid.protocol.AMQMethodEvent;
import org.apache.qpid.server.protocol.AMQProtocolSession;
import org.apache.qpid.server.state.AMQState;
import org.apache.qpid.server.state.AMQStateManager;
import org.apache.qpid.server.state.StateAwareMethodListener;
import org.apache.qpid.server.virtualhost.VirtualHost;

//import org.apache.log4j.Logger;

public class ConnectionOpenMethodHandler implements StateAwareMethodListener<ConnectionOpenBody>
{
    //private static final Logger _logger = Logger.getLogger(ConnectionOpenMethodHandler.class);
    
    private static ConnectionOpenMethodHandler _instance = new ConnectionOpenMethodHandler();

    public static ConnectionOpenMethodHandler getInstance()
    {
        return _instance;
    }

    private ConnectionOpenMethodHandler() {}

    private static AMQShortString generateClientID()
    {
        return new AMQShortString(Long.toString(System.currentTimeMillis()));
    }

    public void methodReceived(AMQStateManager stateManager, AMQMethodEvent<ConnectionOpenBody> evt) throws AMQException
    {
        AMQProtocolSession session = stateManager.getProtocolSession();
        final ConnectionOpenBody body = evt.getMethod();

        //ignore leading '/'
        String virtualHostName;
        if((body.virtualHost != null) && body.virtualHost.charAt(0) == '/')
        {
            virtualHostName = new StringBuilder(body.virtualHost.subSequence(1,body.virtualHost.length())).toString();
        }
        else
        {
            virtualHostName = body.virtualHost == null ? null : String.valueOf(body.virtualHost);
        }

        VirtualHost virtualHost = stateManager.getVirtualHostRegistry().getVirtualHost(virtualHostName);

        if(virtualHost == null)
        {
            throw body.getConnectionException(AMQConstant.NOT_FOUND.getCode(), "Unknown virtual host: " + virtualHostName);
        }
        
        session.setVirtualHost( virtualHost );

        // See Spec (0.8.2). Section  3.1.2 Virtual Hosts
        if (session.getContextKey() == null)
        {
            session.setContextKey(generateClientID());
        }

        // Be aware of possible changes to parameter order as versions change.
        AMQMethodBody response = ConnectionOpenOkBody.createMethodBody(
            session.getProtocolMajorVersion(), // AMQP major version
            session.getProtocolMinorVersion(), // AMQP minor version
            body.virtualHost);	// knownHosts
        session.getStateManager().changeState(AMQState.CONNECTION_OPEN);
        session.writeResponse(evt, response);
    }
}
