<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

    <!-- Match the root element -->
    <xsl:template match="/">
        <xsl:apply-templates select="javadoc"/>
    </xsl:template>

    <!-- Match the javadoc element -->
    <xsl:template match="javadoc">
        <!-- Apply templates for classes -->
        <xsl:apply-templates select="class"/>
    </xsl:template>

    <!-- Match class elements -->
    <xsl:template match="class">
        <xsl:variable name="className" select="@name"/>

        <!-- Generate Markdown heading for class -->
        <xsl:text># Class: </xsl:text>
        <xsl:value-of select="$className"/>
        <xsl:text>
</xsl:text>

        <!-- Process class description -->
        <xsl:apply-templates select="desc"/>

        <!-- Process class tags -->
        <xsl:apply-templates select="tag"/>

        <!-- Process constructors -->
        <xsl:apply-templates select="constructor"/>

        <!-- Process methods -->
        <xsl:apply-templates select="method"/>
    </xsl:template>

    <!-- Match constructor elements -->
    <xsl:template match="constructor">
        <xsl:variable name="constructorName" select="@name"/>

        <!-- Generate Markdown heading for constructor -->
        <xsl:text>## Constructor: </xsl:text>
        <xsl:value-of select="$constructorName"/>
        <xsl:text>
</xsl:text>

        <!-- Process constructor description -->
        <xsl:apply-templates select="desc"/>

        <!-- Process constructor tags -->
        <xsl:apply-templates select="tag"/>

        <!-- Process constructor parameters -->
        <xsl:apply-templates select="param"/>
    </xsl:template>

    <!-- Match method elements -->
    <xsl:template match="method">
        <xsl:variable name="methodName" select="@name"/>

        <!-- Generate Markdown heading for method -->
        <xsl:text>## Method: </xsl:text>
        <xsl:value-of select="$methodName"/>
        <xsl:text>
</xsl:text>

        <!-- Process method description -->
        <xsl:apply-templates select="desc"/>

        <!-- Process method tags -->
        <xsl:apply-templates select="tag"/>

        <!-- Process method parameters -->
        <xsl:apply-templates select="param"/>

        <!-- Process method return -->
        <xsl:apply-templates select="return"/>
    </xsl:template>

    <!-- Match param elements -->
    <xsl:template match="param">
        <xsl:variable name="paramName" select="@name"/>

        <!-- Generate Markdown bullet point for parameter -->
        <xsl:text>* Parameter: </xsl:text>
        <xsl:value-of select="$paramName"/>
        <xsl:text>
</xsl:text>
        <xsl:text>    </xsl:text>
        <xsl:value-of select="desc"/>
        <xsl:text>
</xsl:text>
    </xsl:template>

    <!-- Match return elements -->
    <xsl:template match="return">
        <!-- Generate Markdown bullet point for return -->
        <xsl:text>* Return: </xsl:text>
        <xsl:value-of select="desc"/>
    </xsl:template>
    <!-- Match desc elements -->
    <xsl:template match="desc">
        <xsl:value-of select="."/>
        <xsl:text>
</xsl:text>
    </xsl:template>

    <!-- Match tag elements -->
    <xsl:template match="tag">
        <xsl:variable name="tagName" select="@name"/>

        <!-- Generate Markdown bullet point for tag -->
        <xsl:text>* Tag: </xsl:text>
        <xsl:value-of select="$tagName"/>
        <xsl:text>
</xsl:text>
        <xsl:text>    </xsl:text>
        <xsl:value-of select="desc"/>
        <xsl:text>
</xsl:text>
    </xsl:template>

</xsl:stylesheet>
