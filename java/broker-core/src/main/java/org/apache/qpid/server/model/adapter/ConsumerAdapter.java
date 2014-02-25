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

import java.util.Map;

import org.apache.qpid.server.model.ConfiguredObject;
import org.apache.qpid.server.model.LifetimePolicy;
import org.apache.qpid.server.model.State;
import org.apache.qpid.server.model.UUIDGenerator;
import org.apache.qpid.server.consumer.Consumer;

import java.security.AccessControlException;
import java.util.Collection;
import java.util.Collections;

public class ConsumerAdapter extends AbstractConfiguredObject<ConsumerAdapter> implements org.apache.qpid.server.model.Consumer<ConsumerAdapter>
{
    private final Consumer _consumer;
    private final QueueAdapter _queue;
    private final SessionAdapter _session;

    public ConsumerAdapter(final QueueAdapter queueAdapter, final SessionAdapter sessionAdapter,
                           final Consumer consumer)
    {
        super(UUIDGenerator.generateConsumerUUID(queueAdapter.getVirtualHost().getName(),
                                               queueAdapter.getName(),
                                               consumer.getSessionModel().getConnectionModel().getRemoteAddressString(),
                                               String.valueOf(consumer.getSessionModel().getChannelId()),
                                               consumer.getName()), queueAdapter.getTaskExecutor());
        _consumer = consumer;
        _queue = queueAdapter;
        _session = sessionAdapter;
        //TODO
    }

    public String getName()
    {
        return _consumer.getName();
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
    public Collection<String> getAttributeNames()
    {
        return getAttributeNames(org.apache.qpid.server.model.Consumer.class);
    }

    @Override
    public Object getAttribute(final String name)
    {
        if(ID.equals(name))
        {
            return getId();
        }
        else if(NAME.equals(name))
        {
            return getName();
        }
        else if(STATE.equals(name))
        {

        }
        else if(DURABLE.equals(name))
        {
            return false;
        }
        else if(LIFETIME_POLICY.equals(name))
        {
            return LifetimePolicy.DELETE_ON_SESSION_END;
        }
        else if(TIME_TO_LIVE.equals(name))
        {

        }
        else if(DISTRIBUTION_MODE.equals(name))
        {
            return _consumer.acquires() ? "MOVE" : "COPY";
        }
        else if(SETTLEMENT_MODE.equals(name))
        {

        }
        else if(EXCLUSIVE.equals(name))
        {

        }
        else if(NO_LOCAL.equals(name))
        {

        }
        else if(SELECTOR.equals(name))
        {

        }
        return super.getAttribute(name);    //TODO
    }

    @Override
    public <C extends ConfiguredObject> Collection<C> getChildren(Class<C> clazz)
    {
        return Collections.emptySet();
    }

    @Override
    public <C extends ConfiguredObject> C createChild(Class<C> childClass, Map<String, Object> attributes, ConfiguredObject... otherParents)
    {
        throw new IllegalArgumentException();
    }

    @Override
    public String getDistributionMode()
    {
        return _consumer.acquires() ? "MOVE" : "COPY";
    }

    @Override
    public String getSettlementMode()
    {
        return null;
    }

    @Override
    public boolean isExclusive()
    {
        return false;
    }

    @Override
    public boolean isNoLocal()
    {
        return false;
    }

    @Override
    public String getSelector()
    {
        return null;
    }

    @Override
    public long getBytesOut()
    {
        return _consumer.getBytesOut();
    }

    @Override
    public long getMessagesOut()
    {
        return _consumer.getMessagesOut();
    }

    @Override
    public long getUnacknowledgedBytes()
    {
        return _consumer.getUnacknowledgedBytes();
    }

    @Override
    public long getUnacknowledgedMessages()
    {
        return _consumer.getUnacknowledgedMessages();
    }

    @Override
    protected boolean setState(State currentState, State desiredState)
    {
        // TODO : Add state management
        return false;
    }

    @Override
    public Object setAttribute(final String name, final Object expected, final Object desired) throws IllegalStateException,
            AccessControlException, IllegalArgumentException
    {
        throw new UnsupportedOperationException("Changing attributes on consumer is not supported.");
    }

    @Override
    public void setAttributes(final Map<String, Object> attributes) throws IllegalStateException, AccessControlException,
            IllegalArgumentException
    {
        throw new UnsupportedOperationException("Changing attributes on consumer is not supported.");
    }
}