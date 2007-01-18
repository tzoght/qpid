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
#include <ConnectionRedirectBody.h>
#include <ProtocolVersion.h>
#include <amqp_framing.h>
#include <iostream>
#include <qpid_test_plugin.h>
#include <sstream>
#include <typeinfo>
#include <AMQP_HighestVersion.h>
#include "AMQRequestBody.h"
#include "AMQResponseBody.h"
#include "Requester.h"
#include "Responder.h"

using namespace qpid::framing;

template <class T>
std::string tostring(const T& x) 
{
    std::ostringstream out;
    out << x;
    return out.str();
}

class FramingTest : public CppUnit::TestCase  
{
    CPPUNIT_TEST_SUITE(FramingTest);
    CPPUNIT_TEST(testBasicQosBody); 
    CPPUNIT_TEST(testConnectionSecureBody); 
    CPPUNIT_TEST(testConnectionRedirectBody);
    CPPUNIT_TEST(testAccessRequestBody);
    CPPUNIT_TEST(testBasicConsumeBody);
    CPPUNIT_TEST(testConnectionRedirectBodyFrame);
    CPPUNIT_TEST(testBasicConsumeOkBodyFrame);
    CPPUNIT_TEST(testRequestBodyFrame);
    CPPUNIT_TEST(testResponseBodyFrame);
    CPPUNIT_TEST(testRequester);
    CPPUNIT_TEST(testResponder);
    CPPUNIT_TEST(testInlineContent);
    CPPUNIT_TEST(testContentReference);
    CPPUNIT_TEST_SUITE_END();

  private:
    Buffer buffer;
    ProtocolVersion version;
    AMQP_MethodVersionMap versionMap;
    
  public:

    FramingTest() : buffer(1024), version(highestProtocolVersion) {}

    void testBasicQosBody() 
    {
        BasicQosBody in(version, 0xCAFEBABE, 0xABBA, true);
        in.encodeContent(buffer);
        buffer.flip(); 
        BasicQosBody out(version);
        out.decodeContent(buffer);
        CPPUNIT_ASSERT_EQUAL(tostring(in), tostring(out));
    }
    
    void testConnectionSecureBody() 
    {
        std::string s = "security credential";
        ConnectionSecureBody in(version, s);
        in.encodeContent(buffer);
        buffer.flip(); 
        ConnectionSecureBody out(version);
        out.decodeContent(buffer);
        CPPUNIT_ASSERT_EQUAL(tostring(in), tostring(out));
    }

    void testConnectionRedirectBody()
    {
        std::string a = "hostA";
        std::string b = "hostB";
        ConnectionRedirectBody in(version, a, b);
        in.encodeContent(buffer);
        buffer.flip(); 
        ConnectionRedirectBody out(version);
        out.decodeContent(buffer);
        CPPUNIT_ASSERT_EQUAL(tostring(in), tostring(out));
    }

    void testAccessRequestBody()
    {
        std::string s = "text";
        AccessRequestBody in(version, s, true, false, true, false, true);
        in.encodeContent(buffer);
        buffer.flip(); 
        AccessRequestBody out(version);
        out.decodeContent(buffer);
        CPPUNIT_ASSERT_EQUAL(tostring(in), tostring(out));
    }

    void testBasicConsumeBody()
    {
        std::string q = "queue";
        std::string t = "tag";
        BasicConsumeBody in(version, 0, q, t, false, true, false, false,
                            FieldTable());
        in.encodeContent(buffer);
        buffer.flip(); 
        BasicConsumeBody out(version);
        out.decodeContent(buffer);
        CPPUNIT_ASSERT_EQUAL(tostring(in), tostring(out));
    }
    

    void testConnectionRedirectBodyFrame()
    {
        std::string a = "hostA";
        std::string b = "hostB";
        AMQFrame in(version, 999, new ConnectionRedirectBody(version, a, b));
        in.encode(buffer);
        buffer.flip(); 
        AMQFrame out;
        out.decode(buffer);
        CPPUNIT_ASSERT_EQUAL(tostring(in), tostring(out));
    }

    void testBasicConsumeOkBodyFrame()
    {
        std::string s = "hostA";
        AMQFrame in(version, 999, new BasicConsumeOkBody(version, s));
        in.encode(buffer);
        buffer.flip(); 
        AMQFrame out;
        for(int i = 0; i < 5; i++){
            out.decode(buffer);
            CPPUNIT_ASSERT_EQUAL(tostring(in), tostring(out));
        }
    }

    void testRequestBodyFrame() {
        std::string testing("testing");
        AMQBody::shared_ptr request(new ChannelOpenBody(version, testing));
        AMQFrame in(version, 999, request);
        in.encode(buffer);
        buffer.flip();
        AMQFrame out;
        out.decode(buffer);
        ChannelOpenBody* decoded =
            dynamic_cast<ChannelOpenBody*>(out.getBody().get());
        CPPUNIT_ASSERT(decoded);
        CPPUNIT_ASSERT_EQUAL(testing, decoded->getOutOfBand());
    }
    
    void testResponseBodyFrame() {
        AMQBody::shared_ptr response(new ChannelOkBody(version));
        AMQFrame in(version, 999, response);
        in.encode(buffer);
        buffer.flip();
        AMQFrame out;
        out.decode(buffer);
        ChannelOkBody* decoded =
            dynamic_cast<ChannelOkBody*>(out.getBody().get());
        CPPUNIT_ASSERT(decoded);
    }

    void testInlineContent() {        
        Content content(INLINE, "MyData");
        CPPUNIT_ASSERT(content.isInline());
        content.encode(buffer);
        buffer.flip();
        Content recovered;
        recovered.decode(buffer);
        CPPUNIT_ASSERT(recovered.isInline());
        CPPUNIT_ASSERT_EQUAL(content.getValue(), recovered.getValue());
    }

    void testContentReference() {        
        Content content(REFERENCE, "MyRef");
        CPPUNIT_ASSERT(content.isReference());
        content.encode(buffer);
        buffer.flip();
        Content recovered;
        recovered.decode(buffer);
        CPPUNIT_ASSERT(recovered.isReference());
        CPPUNIT_ASSERT_EQUAL(content.getValue(), recovered.getValue());
    }

    void testRequester() {
        Requester r;
        AMQRequestBody::Data q;
        AMQResponseBody::Data p;

        r.sending(q);
        CPPUNIT_ASSERT_EQUAL(1ULL, q.requestId);
        CPPUNIT_ASSERT_EQUAL(0ULL, q.responseMark);

        r.sending(q);
        CPPUNIT_ASSERT_EQUAL(2ULL, q.requestId);
        CPPUNIT_ASSERT_EQUAL(0ULL, q.responseMark);

        // Now process a response
        p.responseId = 1;
        p.requestId = 2;
        r.processed(AMQResponseBody::Data(1, 2));

        r.sending(q);
        CPPUNIT_ASSERT_EQUAL(3ULL, q.requestId);
        CPPUNIT_ASSERT_EQUAL(1ULL, q.responseMark);
        
        try {
            r.processed(p);     // Already processed this response.
            CPPUNIT_FAIL("Expected exception");
        } catch (...) {}

        try {
            p.requestId = 50;
            r.processed(p);     // No such request
            CPPUNIT_FAIL("Expected exception");
        } catch (...) {}

        r.sending(q);           // reqId=4
        r.sending(q);           // reqId=5
        r.sending(q);           // reqId=6
        p.responseId++;
        p.requestId = 4;
        p.batchOffset = 2;
        r.processed(p);
        r.sending(q);
        CPPUNIT_ASSERT_EQUAL(7ULL, q.requestId);
        CPPUNIT_ASSERT_EQUAL(2ULL, q.responseMark);

        p.responseId++;
        p.requestId = 1;        // Out of order
        p.batchOffset = 0;
        r.processed(p);
        r.sending(q);
        CPPUNIT_ASSERT_EQUAL(8ULL, q.requestId);
        CPPUNIT_ASSERT_EQUAL(3ULL, q.responseMark);
    }

    void testResponder() {
        Responder r;
        AMQRequestBody::Data q;
        AMQResponseBody::Data p;

        q.requestId = 1;
        q.responseMark = 0;
        r.received(q);
        p.requestId = q.requestId;
        r.sending(p);
        CPPUNIT_ASSERT_EQUAL(1ULL, p.responseId);
        CPPUNIT_ASSERT_EQUAL(1ULL, p.requestId);
        CPPUNIT_ASSERT_EQUAL(0U,   p.batchOffset);
        CPPUNIT_ASSERT_EQUAL(0ULL, r.getResponseMark());

        q.requestId++;
        q.responseMark = 1;
        r.received(q);
        r.sending(p);
        CPPUNIT_ASSERT_EQUAL(2ULL, p.responseId);
        CPPUNIT_ASSERT_EQUAL(0U,   p.batchOffset);
        CPPUNIT_ASSERT_EQUAL(1ULL, r.getResponseMark());

        try {
            // Response mark higher any request ID sent.
            q.responseMark = 3;
            r.received(q);
        } catch(...) {}

        try {
            // Response mark lower than previous response mark.
            q.responseMark = 0;
            r.received(q);
        } catch(...) {}

        // TODO aconway 2007-01-14: Test for batching when supported.
        
    }
};


// Make this test suite a plugin.
CPPUNIT_PLUGIN_IMPLEMENT();
CPPUNIT_TEST_SUITE_REGISTRATION(FramingTest);



