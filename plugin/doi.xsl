<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:goobi="http://www.goobi.io/goobi">
<xsl:template match="/">
<xml>

  <h2>My CD Collection</h2>
  <xsl:value-of select="//goobi:SPRACHE"/>
<br/>
<xsl:value-of select="//goobi:TITEL"/>


</xml>
</xsl:template>
</xsl:stylesheet>
