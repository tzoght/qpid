/*
 *
 * Copyright (c) 2006 The Apache Software Foundation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
package org.apache.qpid.jms;

import java.util.List;

/**
 Connection URL format
 amqp://[user:pass@][clientid]/virtualhost?brokerlist='tcp://host:port?option=\'value\'&option=\'value\';vm://:3/virtualpath?option=\'value\''&failover='method?option=\'value\'&option='value''"
 Options are of course optional except for requiring a single broker in the broker list.
 The option seperator is defined to be either '&' or ','
  */
public interface ConnectionURL
{
    public static final String AMQ_PROTOCOL = "amqp";
    public static final String OPTIONS_BROKERLIST = "brokerlist";
    public static final String OPTIONS_FAILOVER = "failover";
    public static final String OPTIONS_FAILOVER_CYCLE = "cyclecount";
    public static final String OPTIONS_SSL = "ssl";

    String getURL();

    String getFailoverMethod();

    String getFailoverOption(String key);

    int getBrokerCount();

    BrokerDetails getBrokerDetails(int index);

    void addBrokerDetails(BrokerDetails broker);

    List<BrokerDetails> getAllBrokerDetails();

    String getClientName();

    void setClientName(String clientName);

    String getUsername();

    void setUsername(String username);

    String getPassword();

    void setPassword(String password);

    String getVirtualHost();

    void setVirtualHost(String virtualHost);

    String getOption(String key);

    void setOption(String key, String value);
}
