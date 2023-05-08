A doclet to output javadoc as XML
=================================

This library provides a doclet to output the javadoc comments from Java source code to a XML document.
JavaDoc 11 up to JavaDoc 13 is supported.

The source code has been salvaged from https://github.com/MarkusBernhardt/xml-doclet, which has been derived from the [xml-doclet](http://code.google.com/p/xml-doclet) library by Seth Call.

Gradle
------

```gradle
configurations {
    xmlDoclet
}

dependencies {
    xmlDoclet 'com.github.manticore-projects:xml-doclet:+'
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
                    <groupId>com.github.markusbernhardt</groupId>
                    <artifactId>xml-doclet</artifactId>
                    <version>1.0.5</version>
                </docletArtifact>
            </configuration>
        </execution>
    </executions>
</plugin>
```

Use 'mvn package' with maven.
If you are not using maven, you can use the [jar-with-dependencies](http://search.maven.org/remotecontent?filepath=com/github/markusbernhardt/xml-doclet/1.0.5/xml-doclet-1.0.5-jar-with-dependencies.jar), which contains all required libraries.

    javadoc -doclet com.github.markusbernhardt.xmldoclet.XmlDoclet \
    -docletpath xml-doclet-1.0.5-jar-with-dependencies.jar \
    [Javadoc- and XmlDoclet-Options]

A Makefile target to generate xml from both the production and test code:


    javadoc:
    mkdir -p target/production target/test
    CLASSPATH=$$(echo $$(find ~/.m2/repository/ -name '*.jar'|grep -v jdk14 )|sed 's/ /:/g')\
     javadoc -doclet com.github.markusbernhardt.xmldoclet.XmlDoclet -sourcepath src/main/java -d target/production org.rulez.demokracia.PDEngine
    CLASSPATH=$$(echo $$(find ~/.m2/repository/ -name '*.jar'|grep -v jdk14 )|sed 's/ /:/g')\
     javadoc -doclet com.github.markusbernhardt.xmldoclet.XmlDoclet -sourcepath src/test/java -d target/test org.rulez.demokracia.PDEngine

If you want more control and feel adventurous you could you use this [jar](http://search.maven.org/remotecontent?filepath=com/github/markusbernhardt/xml-doclet/1.0.5/xml-doclet-1.0.5.jar) and provide all required libraries from this [list](DEPENDENCIES.md) on your own.

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
