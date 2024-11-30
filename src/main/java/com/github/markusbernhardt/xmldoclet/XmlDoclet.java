package com.github.markusbernhardt.xmldoclet;

import java.io.FileWriter;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import javax.lang.model.SourceVersion;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.util.ElementFilter;
import javax.tools.Diagnostic;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamWriter;

import com.sun.source.doctree.DocCommentTree;
import com.sun.source.util.DocTrees;

import jdk.javadoc.doclet.Doclet;
import jdk.javadoc.doclet.DocletEnvironment;
import jdk.javadoc.doclet.Reporter;

/**
 * @see <a href="https://openjdk.org/groups/compiler/using-new-doclet.html">OpenJDK - Using the new Doclet API</a>
 */
public class XmlDoclet implements Doclet {

    private DocTrees docTrees;
    private Reporter reporter;

    abstract class Option implements Doclet.Option {

        private final String name;
        private final boolean hasArg;
        private final String description;
        private final String parameters;

        Option(String name, boolean hasArg, String description, String parameters) {
            this.name = name;
            this.hasArg = hasArg;
            this.description = description;
            this.parameters = parameters;
        }

        @Override
        public int getArgumentCount() {
            return hasArg ? 1 : 0;
        }

        @Override
        public String getDescription() {
            return description;
        }

        @Override
        public Kind getKind() {
            return Kind.STANDARD;
        }

        @Override
        public List<String> getNames() {
            return List.of(name);
        }

        @Override
        public String getParameters() {
            return hasArg ? parameters : "";
        }
    }

    private final Set<Option> options = Set.of(
            new Option("-d", true, "Destination directory for output file.", null) {
                @Override
                public boolean process(String option, List<String> arguments) {
                    // do nothing just for now

                    return true;
                }
            }
    );

    @Override
    public void init(Locale locale, Reporter reporter) {
        this.reporter = reporter;
    }

    @Override
    public String getName() {
        return getClass().getSimpleName();
    }

    @Override
    public Set<? extends Option> getSupportedOptions() {
        return options;
    }

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latest();
    }

    @Override
    public boolean run(DocletEnvironment docEnv) {
        this.docTrees = docEnv.getDocTrees();

        try (FileWriter fileWriter = new FileWriter("output.xml")) {
            final XMLStreamWriter writer = XMLOutputFactory.newInstance().createXMLStreamWriter(fileWriter);
            writer.writeStartDocument("UTF-8", "1.0");
            writer.writeStartElement("root");

            for (PackageElement packageElement : ElementFilter.packagesIn(docEnv.getIncludedElements())) {
                processPackage(writer, packageElement);
            }

            writer.writeEndElement(); // root
            writer.writeEndDocument();
            writer.close();
        } catch (Exception e) {
            reporter.print(Diagnostic.Kind.ERROR, "Error generating XML: " + e.getMessage());
        }

        return true;
    }

    private void processPackage(XMLStreamWriter writer, PackageElement packageElement) throws Exception {
        writer.writeStartElement("package");
        writer.writeAttribute("name", packageElement.getQualifiedName().toString());

        for (TypeElement typeElement : ElementFilter.typesIn(packageElement.getEnclosedElements())) {
            processType(writer, typeElement);
        }

        writer.writeEndElement(); // package
    }

    private void processType(XMLStreamWriter writer, TypeElement typeElement) throws Exception {
        final String elementName = typeElement.getKind() == ElementKind.CLASS ? "class" : "interface";
        writer.writeStartElement(elementName);
        writer.writeAttribute("name", typeElement.getSimpleName().toString());
        writer.writeAttribute("qualified", typeElement.getQualifiedName().toString());

        // Javadoc comments
        final DocCommentTree docCommentTree = docTrees.getDocCommentTree(typeElement);
        if (docCommentTree != null) {
            writer.writeStartElement("comment");
            writer.writeCharacters(docCommentTree.toString());
            writer.writeEndElement(); // comment
        }

        // Process fields
        for (VariableElement field : ElementFilter.fieldsIn(typeElement.getEnclosedElements())) {
            processField(writer, field);
        }

        // Nested elements like methods, fields, etc., can be processed similarly.
        writer.writeEndElement(); // class or interface
    }

    private void processField(XMLStreamWriter writer, VariableElement field) throws Exception {
        writer.writeStartElement("field");
        writer.writeAttribute("name", field.getSimpleName().toString());

        // Javadoc comments
        final DocCommentTree docCommentTree = docTrees.getDocCommentTree(field);
        if (docCommentTree != null) {
            writer.writeStartElement("comment");
            writer.writeCharacters(docCommentTree.toString());
            writer.writeEndElement(); // comment
        }

        writer.writeEndElement(); // field
    }
}
