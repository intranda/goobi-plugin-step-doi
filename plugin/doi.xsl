<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="2.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
<xsl:output indent="yes"/>
<xsl:template match="/">
<resource xmlns="http://datacite.org/schema/kernel-4" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:schemaLocation="http://datacite.org/schema/kernel-4 http://schema.datacite.org/meta/kernel-4.2/metadata.xsd">

  <identifier identifierType="DOI"><xsl:value-of select="//GOOBI-DOI"/></identifier>
  <titles>
      <title><xsl:value-of select="//TITLE"/></title>
  </titles>
  <publisher><xsl:value-of select="//PUBLISHER"/></publisher>
  <publicationYear><xsl:value-of select="//PUBLICATIONYEAR"/></publicationYear>
  <subjects>
      <xsl:for-each select="//SUBJECT">
          <subject xml:lang="de-DE"><xsl:value-of select="."/></subject>
      </xsl:for-each>
  </subjects>
  <resourceType resourceTypeGeneral="Text"><xsl:value-of select="//GOOBI-DOCTYPE"/></resourceType>
  <language><xsl:value-of select="//LANGUAGE"/></language>
  <creators>
      <xsl:for-each select="//CREATOR">
          <creator>
            <creatorName><xsl:value-of select="."/></creatorName>
            <givenName><xsl:value-of select="substring-before(., ', ')"/></givenName>
            <familyName><xsl:value-of select="substring-after(., ', ')"/></familyName>
          </creator>
      </xsl:for-each>
  </creators>
  <sizes>
      <size><xsl:value-of select="//FORMAT"/></size>
  </sizes>
  <alternateIdentifiers>
      <alternateIdentifier alternateIdentifierType="Goobi identifier"><xsl:value-of select="//IDENTIFIER"/></alternateIdentifier>
  </alternateIdentifiers>
  <contributors>
      <contributor contributorType="HostingInstitution">
          <contributorName>Wirtschaftsuniversität Wien</contributorName>
      </contributor>
  </contributors>

<!-- 
  <xsl:if test="//NUMBER != ''">
	  <relatedItem relatedItemType="Collection" relationType="IsPartOf">
      <title><xsl:value-of select="//ANCHORTITLE"/></title>
      <title titleType="Subtitle"><xsl:value-of select="//ANCHORSUBTITLE"/></title>
      <volume><xsl:value-of select="//SERIES"/></volume>
      <number><xsl:value-of select="//NUMBER"/></number>
    </relatedItem>	
  </xsl:if>
-->


</resource>
</xsl:template>
</xsl:stylesheet>


<!--
========================== Available internal elements ==========================

- Publication type of anchor document (e.g. Periodical)
<xsl:value-of select="//GOOBI-ANCHOR-DOCTYPE"/>

- Publication type of document (e.g. Monograph or Volume)
<xsl:value-of select="//GOOBI-DOCTYPE"/>

- Generated DOI
<xsl:value-of select="//GOOBI-DOI"/>

========================== // Available internal elements ==========================
-->
