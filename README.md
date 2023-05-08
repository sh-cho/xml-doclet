A doclet to output javadoc as XML
=================================

This library provides a doclet to output the javadoc comments from Java source code to a XML document.
JavaDoc 11 up to JavaDoc 13 is supported.

The source code has been salvaged from https://github.com/MarkusBernhardt/xml-doclet, which has been derived from the [xml-doclet](http://code.google.com/p/xml-doclet) library by Seth Call.

Gradle
------

```gradle
repositories {
    // Sonatype OSSRH
    maven {
        url = uri('https://s01.oss.sonatype.org/content/repositories/snapshots/')
    }
}
configurations {
    xmlDoclet
}

dependencies {
    xmlDoclet 'com.manticore-projects.tools:xml-doclet:+'
}

tasks.register('xmldoc', Javadoc) {
    source = sourceSets.main.allJava
    destinationDir = reporting.file("xmlDoclet")
    options.docletpath = configurations.xmlDoclet.files.asType(List)
    options.doclet = "com.github.markusbernhardt.xmldoclet.XmlDoclet"

    // @see https://github.com/gradle/gradle/issues/11898#issue-549900869
    title = null
    options.noTimestamp(false)
}
```

Usage
-----

If you are using maven you can use this library by adding the following report to your pom.xml:

```xml
<repositories>
    <repository>
        <id>jsqlparser-snapshots</id>
        <snapshots>
            <enabled>true</enabled>
        </snapshots>
        <url>https://oss.sonatype.org/content/groups/public/</url>
    </repository>
</repositories>
<plugin>
    <groupId>org.apache.maven.plugins</groupId>
    <artifactId>maven-javadoc-plugin</artifactId>
    <executions>
        <execution>
            <id>xml-doclet</id>
        <phase>prepare-package</phase>
            <goals>
                <goal>javadoc</goal>
            </goals>
            <configuration>
                <doclet>com.github.markusbernhardt.xmldoclet.XmlDoclet</doclet>
                <additionalparam>-d ${project.build.directory} -filename ${project.artifactId}-${project.version}-javadoc.xml</additionalparam>
                <useStandardDocletOptions>false</useStandardDocletOptions>
                <docletArtifact>
                    <groupId>com.manticore-projects.tools</groupId>
                    <artifactId>xml-doclet</artifactId>
                    <version>1.1.3</version>
                </docletArtifact>
            </configuration>
        </execution>
    </executions>
</plugin>
```

Options
-------

    -d <directory>            Destination directory for output file.
                              Default: .

    -docencoding <encoding>   Encoding of the output file.
                              Default: UTF8

    -dryrun                   Parse javadoc, but don't write output file.
                              Default: false

    -filename <filename>      Name of the output file.
                              Default: javadoc.xml
