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
package org.apache.qpid.server.queue;

import org.apache.qpid.server.management.MBeanDescription;
import org.apache.qpid.server.management.AMQManagedObject;
import org.apache.qpid.server.management.MBeanConstructor;
import org.apache.qpid.AMQException;
import org.apache.qpid.framing.ContentBody;
import org.apache.qpid.framing.BasicContentHeaderProperties;
import org.apache.qpid.framing.ContentHeaderBody;
import org.apache.mina.common.ByteBuffer;
import org.apache.log4j.Logger;

import javax.management.openmbean.*;
import javax.management.JMException;
import javax.management.Notification;
import javax.management.MBeanException;
import javax.management.MBeanNotificationInfo;
import javax.management.OperationsException;
import javax.management.monitor.MonitorNotification;
import java.util.List;
import java.util.ArrayList;

/**
 * MBean class for AMQQueue. It implements all the management features exposed
 * for an AMQQueue.
 */
@MBeanDescription("Management Interface for AMQQueue")
public class AMQQueueMBean extends AMQManagedObject implements ManagedQueue, QueueNotificationListener
{

    private static final Logger _logger = Logger.getLogger(AMQQueueMBean.class);

    private AMQQueue _queue = null;
    private String _queueName = null;
    // OpenMBean data types for viewMessages method
    private final static String[] _msgAttributeNames = {"AMQ MessageId", "Header", "Size(bytes)", "Redelivered"};
    private static String[] _msgAttributeIndex = {_msgAttributeNames[0]};
    private static OpenType[] _msgAttributeTypes = new OpenType[4]; // AMQ message attribute types.
    private static CompositeType _messageDataType = null;           // Composite type for representing AMQ Message data.
    private static TabularType _messagelistDataType = null;         // Datatype for representing AMQ messages list.

    // OpenMBean data types for viewMessageContent method
    private static CompositeType _msgContentType = null;
    private final static String[] _msgContentAttributes = {"AMQ MessageId", "MimeType", "Encoding", "Content"};
    private static OpenType[] _msgContentAttributeTypes = new OpenType[4];

    private final long[] _lastNotificationTimes = new long[NotificationCheck.values().length];

    @MBeanConstructor("Creates an MBean exposing an AMQQueue")
    public AMQQueueMBean(AMQQueue queue) throws JMException
    {
        super(ManagedQueue.class, ManagedQueue.TYPE);
        _queue = queue;
        _queueName = jmxEncode(new StringBuffer(queue.getName()), 0).toString();
    }

    static
    {
        try
        {
            init();
        }
        catch (JMException ex)
        {
            // It should never occur
            System.out.println(ex.getMessage());
        }
    }

    /**
     * initialises the openmbean data types
     */
    private static void init() throws OpenDataException
    {
        _msgContentAttributeTypes[0] = SimpleType.LONG;                    // For message id
        _msgContentAttributeTypes[1] = SimpleType.STRING;                  // For MimeType
        _msgContentAttributeTypes[2] = SimpleType.STRING;                  // For Encoding
        _msgContentAttributeTypes[3] = new ArrayType(1, SimpleType.BYTE);  // For message content
        _msgContentType = new CompositeType("Message Content", "AMQ Message Content", _msgContentAttributes,
                                            _msgContentAttributes, _msgContentAttributeTypes);

        _msgAttributeTypes[0] = SimpleType.LONG;                      // For message id
        _msgAttributeTypes[1] = new ArrayType(1, SimpleType.STRING);  // For header attributes
        _msgAttributeTypes[2] = SimpleType.LONG;                      // For size
        _msgAttributeTypes[3] = SimpleType.BOOLEAN;                   // For redelivered

        _messageDataType = new CompositeType("Message", "AMQ Message", _msgAttributeNames, _msgAttributeNames, _msgAttributeTypes);
        _messagelistDataType = new TabularType("Messages", "List of messages", _messageDataType, _msgAttributeIndex);
    }

    public String getObjectInstanceName()
    {
        return _queueName;
    }

    public String getName()
    {
        return _queueName;
    }

    public boolean isDurable()
    {
        return _queue.isDurable();
    }

    public String getOwner()
    {
        return _queue.getOwner();
    }

    public boolean isAutoDelete()
    {
        return _queue.isAutoDelete();
    }

    public Integer getMessageCount()
    {
        return _queue.getMessageCount();
    }

    public Long getMaximumMessageSize()
    {
        return _queue.getMaximumMessageSize();
    }

    public void setMaximumMessageSize(Long value)
    {
        _queue.setMaximumMessageSize(value);
    }

    public Integer getConsumerCount()
    {
        return _queue.getConsumerCount();
    }

    public Integer getActiveConsumerCount()
    {
        return _queue.getActiveConsumerCount();
    }

    public Long getReceivedMessageCount()
    {
        return _queue.getReceivedMessageCount();
    }

    public Integer getMaximumMessageCount()
    {
        return _queue.getMaximumMessageCount();
    }

    public void setMaximumMessageCount(Integer value)
    {
        _queue.setMaximumMessageCount(value);
    }

    public Long getMaximumQueueDepth()
    {
        return _queue.getMaximumQueueDepth();
    }

    public void setMaximumQueueDepth(Long value)
    {
        _queue.setMaximumQueueDepth(value);
    }

    /**
     * returns the size of messages(KB) in the queue.
     */
    public Long getQueueDepth()
    {
//        List<AMQMessage> list = _queue.getMessagesOnTheQueue();
//        if (list.size() == 0)
//        {
//            return 0l;
//        }
//
//        long queueDepth = 0;
//        for (AMQMessage message : list)
//        {
//            queueDepth = queueDepth + getMessageSize(message);
//        }
//        return (long) Math.round(queueDepth / 1000);

        //fixme delegate to DeliveryManger
        //return _queue.getTotalSize();
        return 0L;
    }

    /**
     * returns size of message in bytes
     */
    private long getMessageSize(AMQMessage msg)
    {
        if (msg == null)
        {
            return 0l;
        }

        return msg.getContentHeaderBody().bodySize;
    }

    /**
     * Checks if there is any notification to be send to the listeners
     */
    public void checkForNotification(AMQMessage msg)
    {
        final long currentTime = System.currentTimeMillis();
        final long thresholdTime = currentTime - _queue.getMinimumAlertRepeatGap();

        for (NotificationCheck check : NotificationCheck.values())
        {
            if (check.isMessageSpecific() || _lastNotificationTimes[check.ordinal()] < thresholdTime)
            {
                if (check.notifyIfNecessary(msg, _queue, this))
                {
                    _lastNotificationTimes[check.ordinal()] = currentTime;
                }
            }
        }
    }

    /**
     * Sends the notification to the listeners
     */
    public void notifyClients(NotificationCheck notification, AMQQueue queue, String notificationMsg)
    {
        // important : add log to the log file - monitoring tools may be looking for this
        _logger.info(notification.name() + " On Queue " + queue.getName() + " - " + notificationMsg);

        Notification n = new Notification(MonitorNotification.THRESHOLD_VALUE_EXCEEDED, this,
                                          ++_notificationSequenceNumber, System.currentTimeMillis(), notificationMsg);

        _broadcaster.sendNotification(n);
    }

    /**
     * @see org.apache.qpid.server.queue.AMQQueue#deleteMessageFromTop()
     */
    public void deleteMessageFromTop() throws JMException
    {
        try
        {
            _queue.deleteMessageFromTop();
        }
        catch (AMQException ex)
        {
            throw new MBeanException(ex, ex.toString());
        }
    }

    /**
     * @see org.apache.qpid.server.queue.AMQQueue#clearQueue()
     */
    public void clearQueue() throws JMException
    {
        try
        {
            _queue.clearQueue();
        }
        catch (AMQException ex)
        {
            throw new MBeanException(ex, ex.toString());
        }
    }

    /**
     * returns message content as byte array and related attributes for the given message id.
     */
    public CompositeData viewMessageContent(long msgId) throws JMException
    {
        AMQMessage msg = _queue.getMessageOnTheQueue(msgId);
        if (msg == null)
        {
            throw new OperationsException("AMQMessage with message id = " + msgId + " is not in the " + _queueName);
        }
        // get message content
        List<ContentBody> cBodies = msg.getContentBodies();
        List<Byte> msgContent = new ArrayList<Byte>();
        if (cBodies != null)
        {
            for (ContentBody body : cBodies)
            {
                if (body.getSize() != 0)
                {
                    ByteBuffer slice = body.payload.slice();
                    for (int j = 0; j < slice.limit(); j++)
                    {
                        msgContent.add(slice.get());
                    }
                }
            }
        }

        // Create header attributes list
        BasicContentHeaderProperties headerProperties = (BasicContentHeaderProperties) msg.getContentHeaderBody().properties;
        String mimeType = null, encoding = null;
        if (headerProperties != null)
        {
            mimeType = headerProperties.getContentType();
            encoding = headerProperties.getEncoding() == null ? "" : headerProperties.getEncoding();
        }
        Object[] itemValues = {msgId, mimeType, encoding, msgContent.toArray(new Byte[0])};

        return new CompositeDataSupport(_msgContentType, _msgContentAttributes, itemValues);
    }

    /**
     * Returns the header contents of the messages stored in this queue in tabular form.
     */
    public TabularData viewMessages(int beginIndex, int endIndex) throws JMException
    {
        if ((beginIndex > endIndex) || (beginIndex < 1))
        {
            throw new OperationsException("From Index = " + beginIndex + ", To Index = " + endIndex +
                                          "\n\"From Index\" should be greater than 0 and less than \"To Index\"");
        }

        List<AMQMessage> list = _queue.getMessagesOnTheQueue();
        TabularDataSupport _messageList = new TabularDataSupport(_messagelistDataType);

        // Create the tabular list of message header contents
        for (int i = beginIndex; i <= endIndex && i <= list.size(); i++)
        {
            AMQMessage msg = list.get(i - 1);
            ContentHeaderBody headerBody = msg.getContentHeaderBody();
            // Create header attributes list
            BasicContentHeaderProperties headerProperties = (BasicContentHeaderProperties) headerBody.properties;
            String[] headerAttributes = headerProperties.toString().split(",");
            Object[] itemValues = {msg.getMessageId(), headerAttributes, headerBody.bodySize, msg.isRedelivered()};
            CompositeData messageData = new CompositeDataSupport(_messageDataType, _msgAttributeNames, itemValues);
            _messageList.put(messageData);
        }

        return _messageList;
    }

    /**
     * returns Notifications sent by this MBean.
     */
    @Override
    public MBeanNotificationInfo[] getNotificationInfo()
    {
        String[] notificationTypes = new String[]{MonitorNotification.THRESHOLD_VALUE_EXCEEDED};
        String name = MonitorNotification.class.getName();
        String description = "Either Message count or Queue depth or Message size has reached threshold high value";
        MBeanNotificationInfo info1 = new MBeanNotificationInfo(notificationTypes, name, description);

        return new MBeanNotificationInfo[]{info1};
    }

} // End of AMQQueueMBean class
