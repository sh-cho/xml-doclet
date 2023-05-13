<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="2.0"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:my="http://manticore-projects.com/my" >

    <xsl:output
            method="xml"
            encoding="utf8"
            omit-xml-declaration="yes"
            indent="no" />

    <xsl:function name="my:replaceTags">
        <xsl:param name="input"/>
        <xsl:variable name="linkTag" select='replace($input, "\{@link (.*)\}", "`$1`_")' />
        <xsl:variable name="preTag" select='replace($linkTag, "&lt;pre&gt;(.*)&lt;/pre&gt;", "``$1``")' />
        <xsl:variable name="codeTag" select='replace($preTag, "\{@code (.*)\}", "`$1`")' />
        <xsl:variable name="codeTag1" select='replace($codeTag, "&lt;code&gt;(.*)&lt;/code&gt;", "``$1``")' />

        <xsl:value-of select="normalize-space($codeTag1)" disable-output-escaping="yes"/>
    </xsl:function>

    <xsl:function name="my:className">
        <xsl:param name="input"/>
        <xsl:choose>
            <xsl:when test="contains($input, '.')">
                <xsl:variable name="name" select="tokenize($input,'\.')[last()]" />
                <xsl:value-of select="concat(':ref:`', $name, '&lt;', $input, '&gt;`')" disable-output-escaping="yes"/>
            </xsl:when>
            <xsl:otherwise>
                <xsl:value-of select="$input" />
            </xsl:otherwise>
        </xsl:choose>
    </xsl:function>

    <xsl:variable name="basePackageName" select="concat('', 'net.sf.jsqlparser')"/>

    <!-- Match the root element -->
    <xsl:template match="/root">
        <xsl:text  disable-output-escaping="yes">
#######################################################################
JAVA API </xsl:text><xsl:value-of select="$basePackageName"/><xsl:text>
#######################################################################

</xsl:text>
        <xsl:for-each select="package">
            <xsl:sort select="@name"/>
            <xsl:apply-templates select="."/>
        </xsl:for-each>
    </xsl:template>

    <!-- Match package elements -->
    <xsl:template match="package">
        <xsl:variable name="packageName" select="@name"/>
        <xsl:text  disable-output-escaping="yes">
..  _</xsl:text><xsl:value-of select="@name"/><xsl:text  disable-output-escaping="yes">:
***********************************************************************
</xsl:text><xsl:choose>
            <xsl:when test="substring(@name, string-length(concat($basePackageName, '.')))=''">Base</xsl:when>
            <xsl:otherwise><xsl:value-of select="substring(@name, string-length($basePackageName)+2)"/></xsl:otherwise>
        </xsl:choose><xsl:text  disable-output-escaping="yes">
***********************************************************************
</xsl:text>

        <!-- Process classes in the package -->
        <xsl:for-each select="class">
            <xsl:sort select="@qualified"/>
            <xsl:apply-templates select="."/>
        </xsl:for-each>

        <!-- Process classes in the interface -->
        <xsl:for-each select="interface">
            <xsl:sort select="@qualified"/>
            <xsl:apply-templates select="."/>
        </xsl:for-each>

    </xsl:template>

    <!-- Match interface elements -->
    <xsl:template match="interface">
        <xsl:variable name="interfaceName" select="@name"/>
        <xsl:variable name="qualifiedInterfaceName" select="@qualified"/>

        <!-- Generate reStructuredText heading for interface -->
<xsl:text  disable-output-escaping="yes">
..  _</xsl:text><xsl:value-of select="@qualified"/><xsl:text  disable-output-escaping="yes">:
=======================================================================
</xsl:text>
<xsl:value-of select="$interfaceName"/>
        <xsl:text  disable-output-escaping="yes">
=======================================================================

</xsl:text>

        <xsl:choose>
            <xsl:when test="interface">
                <xsl:text>*implements:* </xsl:text>
                <xsl:for-each select="interface">
                    <xsl:value-of select="my:className(@qualified)" disable-output-escaping="yes"/>
                    <xsl:if test="position() != last()">
                        <xsl:text>, </xsl:text>
                    </xsl:if>
                </xsl:for-each>
                <xsl:text> </xsl:text>
            </xsl:when>
        </xsl:choose>

        <xsl:choose>
            <xsl:when test="//*[interface[@qualified=$qualifiedInterfaceName]]">
                <xsl:text>*provides:* </xsl:text>
                <xsl:for-each select="//*[interface[@qualified=$qualifiedInterfaceName]]">
                    <xsl:value-of select="my:className(@qualified)" disable-output-escaping="yes"/>
                    <xsl:if test="position() != last()">
                        <xsl:text>, </xsl:text>
                    </xsl:if>
                </xsl:for-each>
                <xsl:text> </xsl:text>
            </xsl:when>
        </xsl:choose>
        <xsl:text>

</xsl:text>

        <!-- Process methods in the interface -->
        <xsl:apply-templates select="method"/>
    </xsl:template>


    <!-- Match class elements -->
    <xsl:template match="class">
        <xsl:variable name="className" select="@name"/>
        <xsl:variable name="qualifiedClassName" select="@qualified"/>

        <!-- Generate reStructuredText heading for class -->
        <xsl:text  disable-output-escaping="yes">
..  _</xsl:text><xsl:value-of select="@qualified"/><xsl:text  disable-output-escaping="yes">:

=======================================================================
</xsl:text>
        <xsl:text></xsl:text>
<xsl:value-of select="$className"/>
        <xsl:text  disable-output-escaping="yes">
=======================================================================

</xsl:text>

        <xsl:choose>
            <xsl:when test="comment">
                <xsl:value-of select='my:replaceTags(comment)' disable-output-escaping="yes"/>
                <xsl:text>

</xsl:text>
            </xsl:when>
        </xsl:choose>

        <xsl:choose>
            <xsl:when test="class">
<xsl:text>*extends:* </xsl:text>
                    <xsl:value-of select="my:className(class/@qualified)" disable-output-escaping="yes"/>
                <xsl:text> </xsl:text>
            </xsl:when>
        </xsl:choose>

        <xsl:choose>
            <xsl:when test="interface">
<xsl:text>*implements:* </xsl:text>
                <xsl:for-each select="interface">
                    <xsl:value-of select="my:className(@qualified)" disable-output-escaping="yes"/>
                    <xsl:if test="position() != last()">
                        <xsl:text>, </xsl:text>
                    </xsl:if>
                </xsl:for-each>
                <xsl:text> </xsl:text>
            </xsl:when>
        </xsl:choose>

        <xsl:choose>
            <xsl:when test="//class[class[@qualified=$qualifiedClassName]]">
                <xsl:text>*provides:* </xsl:text>
                <xsl:for-each select="//class[class[@qualified=$qualifiedClassName]]">
                    <xsl:value-of select="my:className(@qualified)" disable-output-escaping="yes"/>
                    <xsl:if test="position() != last()">
                        <xsl:text>, </xsl:text>
                    </xsl:if>
                </xsl:for-each>
                <xsl:text> </xsl:text>
            </xsl:when>
        </xsl:choose>
    <xsl:text>

</xsl:text>

        <!-- Process constructors -->
        <xsl:apply-templates select="constructor"/>

        <!-- Process methods -->
        <xsl:apply-templates select="method"/>
    </xsl:template>

    <!-- Match constructor elements -->
    <xsl:template match="constructor[@scope='public']">
        <xsl:variable name="constructorName" select="@name"/>

        <!-- Generate reStructuredText heading for constructor -->
        <xsl:text  disable-output-escaping="yes">
-----------------------------------------------------------------------
</xsl:text>
<xsl:value-of select="$constructorName"/><xsl:text>(</xsl:text>
        <xsl:choose>
            <xsl:when test="parameter">
                <xsl:for-each select="parameter">
                    <xsl:value-of select="@name"/>
                    <xsl:if test="position() != last()">
                        <xsl:text>, </xsl:text>
                    </xsl:if>
                </xsl:for-each>
            </xsl:when>
        </xsl:choose>
        <xsl:text>)</xsl:text>
        <xsl:text  disable-output-escaping="yes">
-----------------------------------------------------------------------

</xsl:text>

        <!-- Process constructor parameters -->
        <xsl:apply-templates select="parameter"/>
    </xsl:template>

    <!-- Match method elements -->
    <xsl:template match="method[@scope='public']">
        <xsl:variable name="methodName" select="@name"/>
        <!-- Generate reStructuredText heading for method -->
        <xsl:text  disable-output-escaping="yes">
-----------------------------------------------------------------------
</xsl:text>
<xsl:value-of select="$methodName"/>
        <xsl:text>(</xsl:text>
        <xsl:choose>
            <xsl:when test="parameter">
                <xsl:for-each select="parameter">
                    <xsl:value-of select="@name"/>
                    <xsl:if test="position() != last()">
                        <xsl:text>, </xsl:text>
                    </xsl:if>
                </xsl:for-each>
            </xsl:when>
        </xsl:choose>
        <xsl:text>)</xsl:text>
        <xsl:choose>
            <xsl:when test="./return">
                <xsl:text> → </xsl:text>
                <xsl:value-of select="my:className(./return/@qualified)" disable-output-escaping="yes"/>
            </xsl:when>
        </xsl:choose>
        <xsl:choose>
            <xsl:when test="annotation">
                <xsl:for-each select="annotation">
                    <xsl:text> *@</xsl:text>
                    <xsl:value-of select="@name"/>
                    <xsl:text>*</xsl:text>
                    <xsl:if test="position() != last()">
                        <xsl:text>,</xsl:text>
                    </xsl:if>
                </xsl:for-each>
            </xsl:when>
        </xsl:choose>
        <xsl:text disable-output-escaping="yes">
-----------------------------------------------------------------------

</xsl:text>

        <xsl:choose>
            <xsl:when test="comment">
                <xsl:value-of select='my:replaceTags(comment)' disable-output-escaping="yes"/>
                <xsl:text>

</xsl:text>
            </xsl:when>
        </xsl:choose>

        <!-- Process method parameters -->
        <xsl:apply-templates select="parameter"/>

        <!-- Process method return -->
        <xsl:apply-templates select="return"/>
    </xsl:template>

    <!-- Match param elements -->
    <xsl:template match="parameter">
        <xsl:variable name="paramName" select="@name"/>

        <!-- Generate reStructuredText bullet point for parameter -->
        <xsl:text>* </xsl:text>


        <xsl:value-of select="my:className(type/@qualified)" disable-output-escaping="yes"/>
        <xsl:text> **</xsl:text>
        <xsl:value-of select="$paramName"/>
        <xsl:text>**</xsl:text>
        <xsl:choose>
            <xsl:when test="./return">
                <xsl:text> → </xsl:text>
                <xsl:value-of select="my:className(./return/@qualified)" disable-output-escaping="yes"/>
            </xsl:when>
        </xsl:choose>
        <xsl:choose>
            <xsl:when test="../tag[@name]='@param' and starts-with(../tag[@text], $paramName) ">
                <xsl:text> *</xsl:text>
                <xsl:value-of select="@text"/>
                <xsl:text>*</xsl:text>
            </xsl:when>
        </xsl:choose>
        <xsl:choose>
            <xsl:when test="../tag[@name='@param' and starts-with(@text, concat($paramName, ' '))]">
                <xsl:text>  ← </xsl:text>
                <xsl:value-of select='my:replaceTags(../tag[@name="@param" and starts-with(@text, $paramName)]/@text)'/>
                <xsl:text></xsl:text>
            </xsl:when>
        </xsl:choose>
        <xsl:text>
</xsl:text>
    </xsl:template>

    <!-- Match return elements -->
    <xsl:template match="return">
        <!-- Generate reStructuredText bullet point for return -->
        <xsl:text>* returns </xsl:text>
        <xsl:value-of select="my:className(@qualified)" disable-output-escaping="yes"/>
        <xsl:text></xsl:text>
        <xsl:choose>
            <xsl:when test="../tag[@name]='@param'">
                <xsl:text> ― *</xsl:text>
                <xsl:value-of select="desc"/>
                <xsl:text>*</xsl:text>
            </xsl:when>
        </xsl:choose>
        <xsl:choose>
            <xsl:when test="../tag[@name='@return']">
                <xsl:text>  ← </xsl:text>
                <xsl:value-of select='my:replaceTags(../tag[@name="@return"]/@text)' />
                <xsl:text>
</xsl:text>
            </xsl:when>
        </xsl:choose>
        <xsl:text>
</xsl:text>
    </xsl:template>

</xsl:stylesheet>
