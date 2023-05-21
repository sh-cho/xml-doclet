******************************
How to use it
******************************

Compile from Source Code
==============================

You will need to have ``JDK 11`` or ``JDK 17`` installed.


.. tab:: Maven

.. code-block:: shell

        git clone https://github.com/manticore-projects/xml-doclet.git
        cd xml-doclet
        mvn install

.. tab:: Gradle

  .. code-block:: shell

        git clone https://github.com/manticore-projects/xml-doclet.git
        cd JDBCParquetWriter
        gradle publishToMavenLocal



Build Dependencies
==============================


.. tab:: Maven Release

    .. code-block:: xml
        :substitutions:

        <dependency>
            <groupId>com.manticore-projects.tools</groupId>
            <artifactId>xml-doclet</artifactId>
            <version>|XMLDOCLET_VERSION|</version>
        </dependency>

.. tab:: Maven Snapshot

    .. code-block:: xml
        :substitutions:

        <repositories>
            <repository>
                <id>sonatype-snapshots</id>
                <snapshots>
                    <enabled>true</enabled>
                </snapshots>
                <url>https://oss.sonatype.org/content/groups/public/</url>
            </repository>
        </repositories>
        <dependency>
            <groupId>com.manticore-projects.tools</groupId>
            <artifactId>xml-doclet+</artifactId>
            <version>|XMLDOCLET_SNAPSHOT_VERSION|</version>
        </dependency>

.. tab:: Gradle Stable

    .. code-block:: groovy
        :substitutions:

        repositories {
            mavenCentral()
        }

        dependencies {
            implementation 'com.manticore-projects.tools:xml-doclet:|XMLDOCLET_VERSION|'
        }

.. tab:: Gradle Snapshot

    .. code-block:: groovy
        :substitutions:

        repositories {
            maven {
                url = uri('https://oss.sonatype.org/content/groups/public/')
            }
        }

        dependencies {
            implementation 'com.manticore-projects.tools:xml-doclet:|XMLDOCLET_SNAPSHOT_VERSION|'
        }

Sphinx Integration
==============================

.. tab:: Gradle

    .. code-block:: groovy
        :caption: build.gradle

        configurations {
            xmlDoclet
        }

        repositories {
            mavenCentral()
            // use Snapshots
            maven {
                url = uri('https://oss.sonatype.org/content/repositories/snapshots')
            }
        }

        dependencies {
            xmlDoclet 'com.manticore-projects.tools:xml-doclet:+'
        }

        tasks.register('xmldoc', Javadoc) {
            source = sourceSets.main.allJava

            // beware: Gradle deletes this folder automatically and there is no switch-off
            destinationDir = reporting.file("xmlDoclet")
            options.docletpath = configurations.xmlDoclet.files.asType(List)
            options.doclet = "com.github.markusbernhardt.xmldoclet.XmlDoclet"

            // optional: transform into Restructured Text for Sphinx
            options.addBooleanOption("rst", true)
            options.addBooleanOption("withFloatingToc", true)
            options.addStringOption("basePackage", "com.github.markusbernhardt.xmldoclet")

            // optional: copy the generated RST file into the Sphinx Folder
            doLast {
                copy {
                    from reporting.file("xmlDoclet/javadoc.rst")
                    into "${projectDir}/src/site/sphinx"
                }
            }
        }

.. tab:: Maven

    .. code-block:: xml
        :caption: pom.xml
        :substitutions:

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
                            <version>|XMLDOCLET_VERSION|</version>
                        </docletArtifact>
                    </configuration>
                </execution>
            </executions>
        </plugin>

Floating Table of Content
==============================

Download the CSS file |FLOATING_TOC_CSS_LINK| and JavaScript file |FLOATING_TOC_JS_LINK| and add those to your Sphinx resource folder ``_static``:

.. list-table:: Static Binaries Direct Download Links
   :widths: 75 25
   :header-rows: 1

   * - File
     - Size
   * - |FLOATING_TOC_CSS_LINK|
     - (2 kB)
   * - |FLOATING_TOC_JS_LINK|
     - (4 kB)


.. code-block:: python
    :caption: config.py

    html_static_path = ['_static']
    html_css_files = ['floating_toc.css']
    html_js_files = ['floating_toc.js',]


Then you can provide the `Floating TOC` Option together with the `Restructured Text` Option in your build file:

.. code-block:: groovy
    :caption: build.gradle
    :emphasize-lines: 4

    tasks.register('xmldoc', Javadoc) {
        // optional: transform into Restructured Text for Sphinx
        options.addBooleanOption("rst", true)
        options.addBooleanOption("withFloatingToc", true)
        options.addStringOption("basePackage", "com.github.markusbernhardt.xmldoclet")

    }


