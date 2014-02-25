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
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;

import java.util.Map;

import org.apache.qpid.server.model.*;
import org.apache.qpid.server.consumer.Consumer;
import org.apache.qpid.server.configuration.updater.TaskExecutor;
import org.apache.qpid.server.protocol.AMQSessionModel;

final class SessionAdapter extends AbstractConfiguredObject<SessionAdapter> implements Session<SessionAdapter>
{
    // Attributes


    private AMQSessionModel _session;
    private Map<Consumer, ConsumerAdapter> _consumerAdapters = new HashMap<Consumer, ConsumerAdapter>();

    public SessionAdapter(final AMQSessionModel session, TaskExecutor taskExecutor)
    {
        super(UUIDGenerator.generateRandomUUID(), taskExecutor);
        _session = session;
    }

    @Override
    public int getChannelId()
    {
        return _session.getChannelId();
    }

    @Override
    public boolean isProducerFlowBlocked()
    {
        return _session.getBlocking();
    }

    public Collection<org.apache.qpid.server.model.Consumer> getConsumers()
    {
        synchronized (_consumerAdapters)
        {
            return new ArrayList<org.apache.qpid.server.model.Consumer>(_consumerAdapters.values());
        }
    }

    public Collection<Publisher> getPublishers()
    {
        return null;  //TODO
    }

    public String getName()
    {
        return String.valueOf(_session.getChannelId());
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

    /**
     * Register a ConsumerAdapter with this Session keyed by the Consumer.
     * @param consumer the org.apache.qpid.server.consumer.Consumer used to key the ConsumerAdapter.
     * @param adapter the registered ConsumerAdapter.
     */
    void consumerRegistered(Consumer consumer, ConsumerAdapter adapter)
    {
        synchronized (_consumerAdapters)
        {
            _consumerAdapters.put(consumer, adapter);
        }
        childAdded(adapter);
    }

    /**
     * Unregister a ConsumerAdapter  with this Session keyed by the Consumer.
     * @param consumer the org.apache.qpid.server.consumer.Consumer used to key the ConsumerAdapter.
     */
    void consumerUnregistered(Consumer consumer)
    {
        ConsumerAdapter adapter = null;
        synchronized (_consumerAdapters)
        {
            adapter = _consumerAdapters.remove(consumer);
        }
        if (adapter != null)
        {
            childRemoved(adapter);
        }
    }

    @Override
    public Collection<String> getAttributeNames()
    {
        return getAttributeNames(Session.class);
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
        else if(name.equals(CHANNEL_ID))
        {
            return _session.getChannelId();
        }
        else if(name.equals(PRODUCER_FLOW_BLOCKED))
        {
            return _session.getBlocking();
        }
        return super.getAttribute(name);    //TODO - Implement
    }

    @Override
    public <C extends ConfiguredObject> Collection<C> getChildren(Class<C> clazz)
    {
        if(clazz == org.apache.qpid.server.model.Consumer.class)
        {
            return (Collection<C>) getConsumers();
        }
        else if(clazz == Publisher.class)
        {
            return (Collection<C>) getPublishers();
        }
        else
        {
            return Collections.emptySet();
        }
    }

    @Override
    public <C extends ConfiguredObject> C createChild(Class<C> childClass, Map<String, Object> attributes, ConfiguredObject... otherParents)
    {
        throw new  UnsupportedOperationException();
    }

    @Override
    public long getConsumerCount()
    {
        return _session.getConsumerCount();
    }

    @Override
    public long getLocalTransactionBegins()
    {
        return _session.getTxnStart();
    }

    @Override
    public int getLocalTransactionOpen()
    {
        long open = _session.getTxnStart() - (_session.getTxnCommits() + _session.getTxnRejects());
        return (open > 0l) ? 1 : 0;
    }

    @Override
    public long getLocalTransactionRollbacks()
    {
        return _session.getTxnRejects();
    }

    @Override
    public long getUnacknowledgedMessages()
    {
        return _session.getUnacknowledgedMessageCount();
    }


    @Override
    protected boolean setState(State currentState, State desiredState)
    {
        // TODO : add state management
        return false;
    }

    @Override
    public Object setAttribute(final String name, final Object expected, final Object desired) throws IllegalStateException,
            AccessControlException, IllegalArgumentException
    {
        throw new UnsupportedOperationException("Changing attributes on session is not supported.");
    }

    @Override
    public void setAttributes(final Map<String, Object> attributes) throws IllegalStateException, AccessControlException,
            IllegalArgumentException
    {
        throw new UnsupportedOperationException("Changing attributes on session is not supported.");
    }
}