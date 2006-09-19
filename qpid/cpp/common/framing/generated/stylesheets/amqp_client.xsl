<?xml version='1.0'?> 
<xsl:stylesheet version="2.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:amqp="http://amqp.org"> 

  <xsl:import href="code_utils.xsl"/>

  <!--
  ==================
  Template: client_h
  ==================
  Client header file.
  -->
  <xsl:template match="amqp" mode="client_h">
    <xsl:param name="domain-cpp-table"/>
    <xsl:result-document href="AMQP_Client.h" format="textFormat">
      <xsl:value-of select="amqp:copyright()"/>
      <xsl:text>
#ifndef _AMQP_Client_
#define _AMQP_Client_

#include "AMQP_ServerOperations.h"
#include "FieldTable.h"
#include "OutputHandler.h"

namespace qpid {
namespace framing {

class AMQP_Client : virtual public AMQP_ServerOperations
{
        OutputHandler* out;

    public:
        AMQP_Client(OutputHandler* _out);
        virtual ~AMQP_Client() {}&#xA;&#xA;</xsl:text>
      <xsl:for-each select="class">
        <xsl:variable name="class" select="amqp:cpp-class-name(@name)"/>
        <xsl:if test="doc">
          <xsl:text>&#xA;/**&#xA;===== Class: </xsl:text><xsl:value-of select="$class"/><xsl:text> =====&#xA;</xsl:text>
          <xsl:value-of select="amqp:process-docs(doc)"/>
          <xsl:text>&#xA;*/&#xA;</xsl:text>
        </xsl:if>
        <xsl:text>        class </xsl:text><xsl:value-of select="$class"/><xsl:text> :  virtual public AMQP_ServerOperations::</xsl:text><xsl:value-of select="$class"/><xsl:text>Handler
        {
                OutputHandler* out;

            public:
                /* Constructors and destructors */
                </xsl:text><xsl:value-of select="$class"/><xsl:text>(OutputHandler* _out);
                virtual ~</xsl:text><xsl:value-of select="$class"/><xsl:text>();
                
                /* Protocol methods */&#xA;</xsl:text>
        <xsl:for-each select="method">
          <xsl:if test="chassis[@name='server']">
            <xsl:variable name="method" select="amqp:cpp-name(@name)"/>
            <xsl:if test="doc">
              <xsl:text>&#xA;/**&#xA;----- Method: </xsl:text><xsl:value-of select="$class"/><xsl:text>.</xsl:text><xsl:value-of select="$method"/><xsl:text> -----&#xA;</xsl:text>
              <xsl:value-of select="amqp:process-docs(doc)"/>
              <xsl:text>&#xA;*/&#xA;</xsl:text>
            </xsl:if>
            <xsl:for-each select="rule">
              <xsl:text>&#xA;/**&#xA;</xsl:text>
              <xsl:text>Rule "</xsl:text><xsl:value-of select="@name"/><xsl:text>":&#xA;</xsl:text><xsl:value-of select="amqp:process-docs(doc)"/>
              <xsl:text>&#xA;*/&#xA;</xsl:text>
            </xsl:for-each>
            <xsl:text>                virtual void </xsl:text><xsl:value-of select="$method"/>
            <xsl:text>( u_int16_t channel</xsl:text><xsl:if test="field"><xsl:text>,&#xA;                        </xsl:text>
            <xsl:for-each select="field">
              <xsl:variable name="domain-cpp-type" select="amqp:cpp-lookup(@domain, $domain-cpp-table)"/>
              <xsl:value-of select="concat($domain-cpp-type, amqp:cpp-arg-ref($domain-cpp-type), ' ', amqp:cpp-name(@name))"/>
              <xsl:if test="position()!=last()">
                <xsl:text>,&#xA;                        </xsl:text>
              </xsl:if>
            </xsl:for-each>
          </xsl:if>
            <xsl:text> );&#xA;</xsl:text>
          </xsl:if>
        </xsl:for-each>
        <xsl:text>        }; /* class </xsl:text><xsl:value-of select="$class"/><xsl:text> */&#xA;</xsl:text>
      </xsl:for-each>
      <xsl:text>}; /* class AMQP_Client */

} /* namespace framing */
} /* namespace qpid */

#endif&#xA;</xsl:text>
    </xsl:result-document>
  </xsl:template>


  <!--
  ====================
  Template: client_cpp
  ====================
  Client body.
  -->
  <xsl:template match="amqp" mode="client_cpp">
    <xsl:param name="domain-cpp-table"/>
    <xsl:result-document href="AMQP_Client.cpp" format="textFormat">
      <xsl:value-of select="amqp:copyright()"/>
      <xsl:text>

#include "AMQP_Client.h"

namespace qpid {
namespace framing {

AMQP_Client::AMQP_Client(OutputHandler* _out) :
    out(_out)
{
}&#xA;&#xA;</xsl:text>
      <xsl:for-each select="class">
        <xsl:variable name="class" select="amqp:cpp-class-name(@name)"/>
        <xsl:text>&#xA;/* ++++++++++ Class: </xsl:text><xsl:value-of select="$class"/><xsl:text> ++++++++++ */

AMQP_Client::</xsl:text><xsl:value-of select="$class"/><xsl:text>::</xsl:text><xsl:value-of select="$class"/><xsl:text>(OutputHandler* _out) :
    out(_out)
{
}

AMQP_Client::</xsl:text><xsl:value-of select="$class"/><xsl:text>::~</xsl:text><xsl:value-of select="$class"/><xsl:text>() {}&#xA;&#xA;</xsl:text>
        <xsl:for-each select="method">
          <xsl:if test="chassis[@name='server']">
            <xsl:text>void AMQP_Client::</xsl:text><xsl:value-of select="$class"/><xsl:text>::</xsl:text>
            <xsl:value-of select="amqp:cpp-name(@name)"/><xsl:text>( u_int16_t channel</xsl:text><xsl:if test="field">
            <xsl:text>,&#xA;                        </xsl:text>
            <xsl:for-each select="field">
              <xsl:variable name="domain-cpp-type" select="amqp:cpp-lookup(@domain, $domain-cpp-table)"/>
              <xsl:value-of select="concat($domain-cpp-type, amqp:cpp-arg-ref($domain-cpp-type), ' ', amqp:cpp-name(@name))"/>
              <xsl:if test="position()!=last()">
                <xsl:text>,&#xA;                        </xsl:text>
              </xsl:if>
            </xsl:for-each>
          </xsl:if>
          <xsl:text> )
{
    out->send( new AMQFrame( channel,
        new </xsl:text><xsl:value-of select="concat($class, amqp:field-name(@name), 'Body')"/><xsl:text>( </xsl:text>
          <xsl:for-each select="field">
            <xsl:value-of select="amqp:cpp-name(@name)"/>
            <xsl:if test="position()!=last()">
              <xsl:text>,&#xA;            </xsl:text>
            </xsl:if>
            </xsl:for-each>
            <xsl:text> ) ) );
}&#xA;&#xA;</xsl:text>
          </xsl:if>
        </xsl:for-each>
      </xsl:for-each>
      <xsl:text>

} /* namespace framing */
} /* namespace qpid */&#xA;</xsl:text>
    </xsl:result-document>
  </xsl:template>
  
</xsl:stylesheet> 
