<?xml version='1.0'?>

<!-- You should use this XSL stylesheet to create plugin documentation.

     If you want all output in a single HTML file, specify the path to
     your DocBook-XSL "html/docbook.xsl" file in the <xsl:import>
     statement below. If you want each chapter to have its own file,
     specify the path to your "html/xtchunk.xsl".
-->

<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                version='1.0'
                xmlns="http://www.w3.org/TR/xhtml1/transitional"
                exclude-result-prefixes="#default">
<xsl:import href="docbook-wrapper.xsl"/>

<!-- Swing HTML control doesn't support &ldquo; and &rdquo; -->
<xsl:template match="quote">&quot;<xsl:apply-templates/>&quot;</xsl:template>

<xsl:template match="guibutton">
  <xsl:call-template name="inline.boldseq"/>
</xsl:template>

<xsl:template match="guiicon">
  <xsl:call-template name="inline.boldseq"/>
</xsl:template>

<xsl:template match="guilabel">
  <xsl:call-template name="inline.boldseq"/>
</xsl:template>

<xsl:template match="guimenu">
  <xsl:call-template name="inline.boldseq"/>
</xsl:template>

<xsl:template match="guimenuitem">
  <xsl:call-template name="inline.boldseq"/>
</xsl:template>

<xsl:template match="guisubmenu">
  <xsl:call-template name="inline.boldseq"/>
</xsl:template>

<xsl:variable name="toc.list.type">ul</xsl:variable>

<xsl:variable name="shade.verbatim">1</xsl:variable>

<xsl:variable name="funcsynopsis.style">ansi</xsl:variable>
<xsl:template match="void"><xsl:apply-templates/></xsl:template>

</xsl:stylesheet>
