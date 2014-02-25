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
package org.apache.qpid.server.exchange;

import org.apache.log4j.Logger;
import org.apache.qpid.exchange.ExchangeDefaults;
import org.apache.qpid.server.model.UUIDGenerator;
import org.apache.qpid.server.plugin.ExchangeType;
import org.apache.qpid.server.queue.QueueRegistry;
import org.apache.qpid.server.store.DurableConfigurationStore;
import org.apache.qpid.server.store.DurableConfigurationStoreHelper;
import org.apache.qpid.server.util.ServerScopedRuntimeException;
import org.apache.qpid.server.virtualhost.UnknownExchangeException;
import org.apache.qpid.server.virtualhost.VirtualHost;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class DefaultExchangeRegistry implements ExchangeRegistry
{
    private static final Logger LOGGER = Logger.getLogger(DefaultExchangeRegistry.class);
    /**
     * Maps from exchange name to exchange instance
     */
    private ConcurrentMap<String, Exchange> _exchangeMap = new ConcurrentHashMap<String, Exchange>();

    private Exchange _defaultExchange;

    private final VirtualHost _host;
    private final QueueRegistry _queueRegistry;

    private final Collection<RegistryChangeListener> _listeners =
            Collections.synchronizedCollection(new ArrayList<RegistryChangeListener>());

    public DefaultExchangeRegistry(VirtualHost host, QueueRegistry queueRegistry)
    {
        _host = host;
        _queueRegistry = queueRegistry;
    }

    public void initialise(ExchangeFactory exchangeFactory)
    {
        //create 'standard' exchanges:
        initialiseExchanges(exchangeFactory, getDurableConfigurationStore());

        _defaultExchange =
                new DefaultExchange(_host, _queueRegistry,
                                    UUIDGenerator.generateExchangeUUID(ExchangeDefaults.DEFAULT_EXCHANGE_NAME,
                                                                       _host.getName()));


    }

    private void initialiseExchanges(ExchangeFactory factory, DurableConfigurationStore store)
    {
        for (ExchangeType<? extends Exchange> type : factory.getRegisteredTypes())
        {
            defineExchange(factory, type.getDefaultExchangeName(), type.getType(), store);
        }

    }

    private void defineExchange(ExchangeFactory f, String name, String type, DurableConfigurationStore store)
    {
        try
        {
            if(getExchange(name) == null)
            {
                Map<String, Object> attributes = new HashMap<String, Object>();
                attributes.put(org.apache.qpid.server.model.Exchange.ID,
                               UUIDGenerator.generateExchangeUUID(name, _host.getName()));
                attributes.put(org.apache.qpid.server.model.Exchange.NAME, name);
                attributes.put(org.apache.qpid.server.model.Exchange.TYPE, type);
                attributes.put(org.apache.qpid.server.model.Exchange.DURABLE, true);
                Exchange exchange = f.createExchange(attributes);
                registerExchange(exchange);
                if(exchange.isDurable())
                {
                    DurableConfigurationStoreHelper.createExchange(store, exchange);
                }
            }
        }
        catch (AMQUnknownExchangeType e)
        {
            throw new ServerScopedRuntimeException("Unknown exchange type while attempting to initialise exchanges - " +
                                                   "this is because necessary jar files are not on the classpath", e);
        }
        catch (UnknownExchangeException e)
        {
            throw new ServerScopedRuntimeException("Unknown alternate exchange type while attempting to initialise " +
                                                   "a mandatory exchange which should not have an alternate: '" +
                                                   name + "'");
        }
    }

    public DurableConfigurationStore getDurableConfigurationStore()
    {
        return _host.getDurableConfigurationStore();
    }

    public void registerExchange(Exchange exchange)
    {
        _exchangeMap.put(exchange.getName(), exchange);
        synchronized (_listeners)
        {
            for(RegistryChangeListener listener : _listeners)
            {
                listener.exchangeRegistered(exchange);
            }

        }
    }

    public Exchange getDefaultExchange()
    {
        return _defaultExchange;
    }

    public boolean unregisterExchange(String name, boolean inUse)
    {
        final Exchange exchange = _exchangeMap.get(name);
        if (exchange != null)
        {

            _host.getSecurityManager().authoriseDelete(exchange);

            // TODO: check inUse argument

            Exchange e = _exchangeMap.remove(name);
            // if it is null then it was removed by another thread at the same time, we can ignore
            if (e != null)
            {
                e.close();

                synchronized (_listeners)
                {
                    for(RegistryChangeListener listener : _listeners)
                    {
                        listener.exchangeUnregistered(exchange);
                    }
                }

            }
        }
        return exchange != null;

    }

    public Collection<Exchange> getExchanges()
    {
        return new ArrayList<Exchange>(_exchangeMap.values());
    }

    public void addRegistryChangeListener(RegistryChangeListener listener)
    {
        _listeners.add(listener);
    }

    public Exchange getExchange(String name)
    {
        if ((name == null) || name.length() == 0)
        {
            return getDefaultExchange();
        }
        else
        {
            return _exchangeMap.get(name);
        }
    }

    @Override
    public void clearAndUnregisterMbeans()
    {
        for (final Exchange exchange : getExchanges())
        {
            //TODO: this is a bit of a hack, what if the listeners aren't aware
            //that we are just unregistering the MBean because of HA, and aren't
            //actually removing the exchange as such.
            synchronized (_listeners)
            {
                for(RegistryChangeListener listener : _listeners)
                {
                    listener.exchangeUnregistered(exchange);
                }
            }
        }
        _exchangeMap.clear();
    }

    @Override
    public synchronized Exchange getExchange(UUID exchangeId)
    {
        if (exchangeId == null)
        {
            return getDefaultExchange();
        }
        else
        {
            Collection<Exchange> exchanges = _exchangeMap.values();
            for (Exchange exchange : exchanges)
            {
                if (exchange.getId().equals(exchangeId))
                {
                    return exchange;
                }
            }
            return null;
        }
    }

    public boolean isReservedExchangeName(String name)
    {
        if (name == null || ExchangeDefaults.DEFAULT_EXCHANGE_NAME.equals(name)
                || name.startsWith("amq.") || name.startsWith("qpid."))
        {
            return true;
        }
        Collection<ExchangeType<? extends Exchange>> registeredTypes = _host.getExchangeTypes();
        for (ExchangeType<? extends Exchange> type : registeredTypes)
        {
            if (type.getDefaultExchangeName().equals(name))
            {
                return true;
            }
        }
        return false;
    }
}