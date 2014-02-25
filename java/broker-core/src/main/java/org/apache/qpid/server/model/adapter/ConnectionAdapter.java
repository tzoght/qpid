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
package org.apache.qpid.server.model.adapter;

import java.security.AccessControlException;
import java.security.Principal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.qpid.protocol.AMQConstant;
import org.apache.qpid.server.model.*;
import org.apache.qpid.server.configuration.updater.TaskExecutor;
import org.apache.qpid.server.protocol.AMQConnectionModel;
import org.apache.qpid.server.protocol.AMQSessionModel;
import org.apache.qpid.server.stats.StatisticsGatherer;

final class ConnectionAdapter extends AbstractConfiguredObject<ConnectionAdapter> implements Connection<ConnectionAdapter>
{
    private AMQConnectionModel _connection;

    private final Map<AMQSessionModel, SessionAdapter> _sessionAdapters =
            new HashMap<AMQSessionModel, SessionAdapter>();

    public ConnectionAdapter(final AMQConnectionModel conn, TaskExecutor taskExecutor)
    {
        super(UUIDGenerator.generateRandomUUID(), taskExecutor);
        _connection = conn;
    }

    @Override
    public String getClientId()
    {
        return (String) getAttribute(CLIENT_ID);
    }

    @Override
    public String getClientVersion()
    {
        return (String) getAttribute(CLIENT_VERSION);
    }

    @Override
    public boolean isIncoming()
    {
        return true;
    }

    @Override
    public String getLocalAddress()
    {
        return (String)getAttribute(LOCAL_ADDRESS);
    }

    @Override
    public String getPrincipal()
    {
        final Principal authorizedPrincipal = _connection.getAuthorizedPrincipal();
        return authorizedPrincipal == null ? null : authorizedPrincipal.getName();
    }

    @Override
    public String getRemoteAddress()
    {
        return _connection.getRemoteAddressString();
    }

    @Override
    public String getRemoteProcessName()
    {
        return null;
    }

    @Override
    public String getRemoteProcessPid()
    {
        return null;
    }

    @Override
    public long getSessionCountLimit()
    {
        return _connection.getSessionCountLimit();
    }

    @Override
    public Transport getTransport()
    {
        return _connection.getTransport();
    }

    @Override
    public Port getPort()
    {
        return _connection.getPort();
    }

    public Collection<Session> getSessions()
    {
        List<AMQSessionModel> actualSessions = _connection.getSessionModels();

        synchronized (_sessionAdapters)
        {
            Iterator<AMQSessionModel> iterator = _sessionAdapters.keySet().iterator();
            while(iterator.hasNext())
            {
                AMQSessionModel session = iterator.next();
                if(!actualSessions.contains(session))
                {
                    SessionAdapter adapter = _sessionAdapters.get(session);
                    iterator.remove();

                    childRemoved(adapter); // Trigger corresponding ConfigurationChangeListener childRemoved() callback.
                }
            }

            for(AMQSessionModel session : actualSessions)
            {
                if(!_sessionAdapters.containsKey(session))
                {
                    SessionAdapter adapter = new SessionAdapter(session, getTaskExecutor());
                    _sessionAdapters.put(session, adapter);
                    childAdded(adapter); // Trigger corresponding ConfigurationChangeListener childAdded() callback.
                }
            }

            return new ArrayList<Session>(_sessionAdapters.values());
        }
    }

    /**
     * Retrieve the SessionAdapter instance keyed by the AMQSessionModel from this Connection.
     * @param session the AMQSessionModel used to index the SessionAdapter.
     * @return the requested SessionAdapter.
     */
    SessionAdapter getSessionAdapter(AMQSessionModel session)
    {
        synchronized (_sessionAdapters)
        {
            getSessions(); // Call getSessions() first to ensure _sessionAdapters state is up to date with actualSessions.
            return _sessionAdapters.get(session);
        }
    }

    public void delete()
    {
        _connection.close(AMQConstant.CONNECTION_FORCED, "Connection closed by external action");
    }

    public String getName()
    {
        final String remoteAddressString = _connection.getRemoteAddressString();
        return remoteAddressString.replaceAll("/","");
    }

    public String setName(final String currentName, final String desiredName)
            throws IllegalStateException, AccessControlException
    {
        return null;  //TODO
    }

    public State getState()
    {
        return null;  //TODO
    }

    public boolean isDurable()
    {
        return false;  //TODO
    }

    public void setDurable(final boolean durable)
            throws IllegalStateException, AccessControlException, IllegalArgumentException
    {
        //TODO
    }

    public LifetimePolicy getLifetimePolicy()
    {
        return null;  //TODO
    }

    public LifetimePolicy setLifetimePolicy(final LifetimePolicy expected, final LifetimePolicy desired)
            throws IllegalStateException, AccessControlException, IllegalArgumentException
    {
        return null;  //TODO
    }

    public long getTimeToLive()
    {
        return 0;  //TODO
    }

    public long setTimeToLive(final long expected, final long desired)
            throws IllegalStateException, AccessControlException, IllegalArgumentException
    {
        return 0;  //TODO
    }

    @Override
    public Object getAttribute(String name)
    {

        if(name.equals(ID))
        {
            return getId();
        }
        else if (name.equals(NAME))
        {
            return getName();
        }
        else if(name.equals(CLIENT_ID))
        {
            return _connection.getClientId();
        }
        else if(name.equals(CLIENT_VERSION))
        {
            return _connection.getClientVersion();
        }
        else if(name.equals(INCOMING))
        {
            return true;
        }
        else if(name.equals(LOCAL_ADDRESS))
        {

        }
        else if(name.equals(PRINCIPAL))
        {
            final Principal authorizedPrincipal = _connection.getAuthorizedPrincipal();
            return authorizedPrincipal == null ? null : authorizedPrincipal.getName();
        }
        else if(name.equals(PROPERTIES))
        {

        }
        else if(name.equals(REMOTE_ADDRESS))
        {
            return _connection.getRemoteAddressString();
        }
        else if(name.equals(REMOTE_PROCESS_NAME))
        {

        }
        else if(name.equals(REMOTE_PROCESS_PID))
        {

        }
        else if(name.equals(SESSION_COUNT_LIMIT))
        {
            return _connection.getSessionCountLimit();
        }
        else if(name.equals(TRANSPORT))
        {
            return String.valueOf(_connection.getTransport());
        }
        else if(name.equals(PORT))
        {
            Port port = _connection.getPort();
            return String.valueOf(port == null ? null : port.getName());
        }
        return super.getAttribute(name);
    }

    @Override
    public Collection<String> getAttributeNames()
    {
        final HashSet<String> attrNames = new HashSet<String>(getAttributeNames(Connection.class));

        return Collections.unmodifiableCollection(attrNames);
    }


    @Override
    public <C extends ConfiguredObject> Collection<C> getChildren(Class<C> clazz)
    {
        if(clazz == Session.class)
        {
            return (Collection<C>) getSessions();
        }
        else
        {
            return Collections.emptySet();
        }
    }

    @Override
    public <C extends ConfiguredObject> C addChild(Class<C> childClass, Map<String, Object> attributes, ConfiguredObject... otherParents)
    {
        if(childClass == Session.class)
        {
            throw new IllegalStateException();
        }
        else
        {
            throw new IllegalArgumentException("Cannot create a child of class " + childClass.getSimpleName());
        }

    }

    @Override
    protected boolean setState(State currentState, State desiredState)
    {
        // TODO: add state management
        return false;
    }

    @Override
    public Object setAttribute(final String name, final Object expected, final Object desired) throws IllegalStateException,
            AccessControlException, IllegalArgumentException
    {
        throw new UnsupportedOperationException("Changing attributes on connection is not supported.");
    }

    @Override
    public void setAttributes(final Map<String, Object> attributes) throws IllegalStateException, AccessControlException,
            IllegalArgumentException
    {
        throw new UnsupportedOperationException("Changing attributes on connection is not supported.");
    }

    @Override
    public long getBytesIn()
    {
        return _connection.getDataReceiptStatistics().getTotal();
    }

    @Override
    public long getBytesOut()
    {
        return _connection.getDataDeliveryStatistics().getTotal();
    }

    @Override
    public long getMessagesIn()
    {
        return _connection.getMessageReceiptStatistics().getTotal();
    }

    @Override
    public long getMessagesOut()
    {
        return _connection.getMessageDeliveryStatistics().getTotal();
    }

    @Override
    public long getLastIoTime()
    {
        return _connection.getLastIoTime();
    }

    @Override
    public int getSessionCount()
    {
        return _connection.getSessionModels().size();
    }
}