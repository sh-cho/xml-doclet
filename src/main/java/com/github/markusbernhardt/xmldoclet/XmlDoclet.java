package com.github.markusbernhardt.xmldoclet;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.transform.Source;
import javax.xml.transform.URIResolver;
import javax.xml.transform.stream.StreamSource;

import org.apache.commons.cli.BasicParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

import com.github.markusbernhardt.xmldoclet.xjc.Root;
import com.sun.javadoc.DocErrorReporter;
import com.sun.javadoc.LanguageVersion;
import com.sun.javadoc.RootDoc;

import net.sf.saxon.s9api.*;

import java.io.OutputStream;
import java.util.Map;
import java.util.logging.Level;

/**
 * Doclet class.
 * 
 * @author markus
 */
public class XmlDoclet {
    private final static java.util.logging.Logger LOGGER =
            java.util.logging.Logger.getLogger(XmlDoclet.class.getName());

    public static final String RESTRUCTURED_XSL = "/com/manticore/xsl/restructured.xsl";
    public static final String MARKDOWN_XSL = "/com/manticore/xsl/markdown.xsl";

    /**
     * The parsed object model. Used in unit tests.
     */
    static Root root;

    /**
     * The Options instance to parse command line strings.
     */
    public final static Options OPTIONS;

    static {
        OPTIONS = new Options();

        OptionBuilder.withArgName("directory");
        OptionBuilder.isRequired(false);
        OptionBuilder.hasArg();
        OptionBuilder.withDescription("Destination directory for output file.\nDefault: .");
        OPTIONS.addOption(OptionBuilder.create("d"));

        OptionBuilder.withArgName("encoding");
        OptionBuilder.isRequired(false);
        OptionBuilder.hasArg();
        OptionBuilder.withDescription("Encoding of the output file.\nDefault: UTF8");
        OPTIONS.addOption(OptionBuilder.create("docencoding"));

        OptionBuilder.withArgName("dryrun");
        OptionBuilder.isRequired(false);
        OptionBuilder.hasArgs(0);
        OptionBuilder
                .withDescription("Parse javadoc, but don't write output file.\nDefault: false");
        OPTIONS.addOption(OptionBuilder.create("dryrun"));

        OptionBuilder.withArgName("rst");
        OptionBuilder.isRequired(false);
        OptionBuilder.hasArgs(0);
        OptionBuilder
                .withDescription(
                        "Transform the XML into a Restructured Text file (*.rst).\nDefault: false");
        OPTIONS.addOption(OptionBuilder.create("rst"));

        OptionBuilder.withArgName("md");
        OptionBuilder.isRequired(false);
        OptionBuilder.hasArgs(0);
        OptionBuilder
                .withDescription("Transform the XML into a Markdown file (*.md).\nDefault: false");
        OPTIONS.addOption(OptionBuilder.create("md"));

        OptionBuilder.withArgName("docbook");
        OptionBuilder.isRequired(false);
        OptionBuilder.hasArgs(0);
        OptionBuilder
                .withDescription(
                        "Transform the XML into a DocBook file (*.db.xml).\nDefault: false");
        OPTIONS.addOption(OptionBuilder.create("docbook"));

        OptionBuilder.withArgName("adoc");
        OptionBuilder.isRequired(false);
        OptionBuilder.hasArgs(0);
        OptionBuilder
                .withDescription(
                        "Transform the XML into an Ascii Doctor file (*.adoc).\nDefault: false");
        OPTIONS.addOption(OptionBuilder.create("adoc"));

        OptionBuilder.withArgName("filename");
        OptionBuilder.isRequired(false);
        OptionBuilder.hasArgs(1);
        OptionBuilder.withDescription("Name of the output file.\nDefault: javadoc.xml");
        OPTIONS.addOption(OptionBuilder.create("filename"));

        OptionBuilder.withArgName("basePackage");
        OptionBuilder.isRequired(false);
        OptionBuilder.hasArgs(1);
        OptionBuilder.withDescription("Name of the base package.\n");
        OPTIONS.addOption(OptionBuilder.create("basePackage"));

        OptionBuilder.withArgName("doctitle");
        OptionBuilder.isRequired(false);
        OptionBuilder.hasArgs(1);
        OptionBuilder.withDescription("Document Title\n");
        OPTIONS.addOption(OptionBuilder.create("doctitle"));

        OptionBuilder.withArgName("windowtitle");
        OptionBuilder.isRequired(false);
        OptionBuilder.hasArgs(1);
        OptionBuilder.withDescription("Window Title\n");
        OPTIONS.addOption(OptionBuilder.create("windowtitle"));

        OptionBuilder.withArgName("noTimestamp");
        OptionBuilder.isRequired(false);
        OptionBuilder.hasArgs(0);
        OptionBuilder.withDescription("No Timestamp.\n");
        OPTIONS.addOption(OptionBuilder.create("notimestamp"));
    }

    /**
     * Check for doclet-added options. Returns the number of arguments you must specify on the
     * command line for the given option. For example, "-d docs" would return 2.
     * <P>
     * This method is required if the doclet contains any options. If this method is missing,
     * Javadoc will print an invalid flag error for every option.
     * 
     * @see com.sun.javadoc.Doclet#optionLength(String)
     * 
     * @param optionName The name of the option.
     * @return number of arguments on the command line for an option including the option name
     *         itself. Zero return means option not known. Negative value means error occurred.
     */
    public static int optionLength(String optionName) {
        Option option = OPTIONS.getOption(optionName);
        if (option == null) {
            return 0;
        }
        return option.getArgs() + 1;
    }

    /**
     * Check that options have the correct arguments.
     * <P>
     * This method is not required, but is recommended, as every option will be considered valid if
     * this method is not present. It will default gracefully (to true) if absent.
     * <P>
     * Printing option related error messages (using the provided DocErrorReporter) is the
     * responsibility of this method.
     * 
     * @see com.sun.javadoc.Doclet#validOptions(String[][], DocErrorReporter)
     * 
     * @param optionsArrayArray The two-dimensional array of options.
     * @param reporter The error reporter.
     * 
     * @return <code>true</code> if the options are valid.
     */
    public static boolean validOptions(String optionsArrayArray[][], DocErrorReporter reporter) {
        return null != parseCommandLine(optionsArrayArray);
    }

    /**
     * Processes the JavaDoc documentation.
     * <p>
     * This method is required for all doclets.
     * 
     * @see com.sun.javadoc.Doclet#start(RootDoc)
     * 
     * @param rootDoc The root of the documentation tree.
     * 
     * @return <code>true</code> if processing was successful.
     */
    public static boolean start(RootDoc rootDoc) {
        CommandLine commandLine = parseCommandLine(rootDoc.options());
        Parser parser = new Parser();
        root = parser.parseRootDoc(rootDoc);
        save(commandLine, root);
        return true;
    }

    public static void transform(InputStream xsltInputStream, File xmlFile, File outFile,
            Map<String, String> parameters)
            throws IOException, SaxonApiException {

        try (InputStream xmlInputStream = new FileInputStream(xmlFile);
                OutputStream output = new FileOutputStream(outFile);) {
            // Create a Saxon Processor
            Processor processor = new Processor(false);

            // Create a DocumentBuilder
            DocumentBuilder docBuilder = processor.newDocumentBuilder();

            // Parse the XML input
            XdmNode xmlDoc = docBuilder.build(new StreamSource(xmlInputStream));

            // Create a XsltCompiler
            XsltCompiler compiler = processor.newXsltCompiler();

            // Set the ClassLoader for the compiler to load resources from the classpath
            compiler.setURIResolver(new ClasspathResourceURIResolver());

            // Create a XsltExecutable from the XSLT input stream
            XsltExecutable xsltExecutable = compiler.compile(new StreamSource(xsltInputStream));
            XsltTransformer transformer = xsltExecutable.load();

            // Set the source document
            transformer.setInitialContextNode(xmlDoc);

            // Set the result destination
            Serializer serializer = processor.newSerializer(output);
            transformer.setDestination(serializer);

            for (Map.Entry<String, String> parameter : parameters.entrySet()) {
                transformer.setParameter(new QName(parameter.getKey()),
                        new XdmAtomicValue(parameter.getValue()));
            }

            // Transform the XML
            transformer.transform();
        }
    }

    // ClasspathResourceURIResolver class for resolving resources from the classpath
    private static class ClasspathResourceURIResolver implements URIResolver {
        public Source resolve(String href, String base) {
            InputStream inputStream = getClass().getClassLoader().getResourceAsStream(href);
            if (inputStream != null) {
                return new StreamSource(inputStream);
            }
            return null;
        }
    }

    /**
     * Save XML object model to a file via JAXB.
     * 
     * @param commandLine the parsed command line arguments
     * @param root the document root
     */
    public static void save(CommandLine commandLine, Root root) {
        if (commandLine.hasOption("dryrun")) {
            return;
        }

        String filename = commandLine.hasOption("filename")
                ? commandLine.getOptionValue("filename")
                : "javadoc.xml";

        String basename = filename.toLowerCase().endsWith(".xml")
                ? filename.substring(0, filename.length() - ".xml".length())
                : filename;

        File xmlFile = commandLine.hasOption("d")
                ? new File(commandLine.getOptionValue("d"), filename)
                : new File(filename);

        try (
                FileOutputStream fileOutputStream = new FileOutputStream(xmlFile);
                BufferedOutputStream bufferedOutputStream =
                        new BufferedOutputStream(fileOutputStream);) {
            JAXBContext contextObj = JAXBContext.newInstance(Root.class);

            Marshaller marshaller = contextObj.createMarshaller();
            marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
            if (commandLine.hasOption("docencoding")) {
                marshaller.setProperty(Marshaller.JAXB_ENCODING,
                        commandLine.getOptionValue("docencoding"));
            }


            marshaller.marshal(root, bufferedOutputStream);
            bufferedOutputStream.flush();
            fileOutputStream.flush();

            LOGGER.info("Wrote XML to: " + xmlFile.getAbsolutePath());

            HashMap<String, String> parameters = new HashMap<>();
            for (Option o : commandLine.getOptions()) {
                if (o.getValue() != null) {
                    parameters.put(o.getArgName(), o.getValue());
                }
            }

            if (commandLine.hasOption("rst")) {
                File outFile = new File(xmlFile.getParent(), basename + ".rst");
                try (InputStream inputStream =
                        XmlDoclet.class.getResourceAsStream(RESTRUCTURED_XSL);) {
                    transform(inputStream, xmlFile, outFile, parameters);
                } catch (Exception ex) {
                    LOGGER.log(Level.SEVERE, "Failed to write Restructured Text", ex);
                }
                LOGGER.info("Wrote Restructured Text to: " + outFile.getAbsolutePath());
            }

            if (commandLine.hasOption("md")) {
                File outFile = new File(xmlFile.getParent(), basename + ".md");
                try (InputStream inputStream =
                        XmlDoclet.class.getResourceAsStream(MARKDOWN_XSL);) {
                    transform(inputStream, xmlFile, outFile, parameters);
                } catch (Exception ex) {
                    LOGGER.log(Level.SEVERE, "Failed to write Markdown", ex);
                }
                LOGGER.info("Wrote Markdown to: " + outFile.getAbsolutePath());
            }

            if (commandLine.hasOption("docbook")) {
                LOGGER.info("Docbook transformation is not supported yet.");
            }

            if (commandLine.hasOption("adoc")) {
                LOGGER.info("ASCII Doctor transformation is not supported yet.");
            }
        } catch (RuntimeException | IOException | JAXBException e) {
            LOGGER.log(Level.SEVERE, "Failed to write the XML File", e);
        }
    }

    /**
     * Return the version of the Java Programming Language supported by this doclet.
     * <p>
     * This method is required by any doclet supporting a language version newer than 1.1.
     * <p>
     * This Doclet supports Java 5.
     * 
     * @see com.sun.javadoc.Doclet#languageVersion()
     * 
     * @return LanguageVersion#JAVA_1_5
     */
    public static LanguageVersion languageVersion() {
        return LanguageVersion.JAVA_1_5;
    }

    /**
     * Parse the given options.
     * 
     * @param optionsArrayArray The two dimensional array of options.
     * @return the parsed command line arguments.
     */
    public static CommandLine parseCommandLine(String[][] optionsArrayArray) {
        try {
            List<String> argumentList = new ArrayList<String>();
            for (String[] optionsArray : optionsArrayArray) {
                argumentList.addAll(Arrays.asList(optionsArray));
            }

            CommandLineParser commandLineParser = new BasicParser() {
                @Override
                protected void processOption(final String arg,
                        @SuppressWarnings("rawtypes") final ListIterator iter)
                        throws ParseException {
                    boolean hasOption = getOptions().hasOption(arg);
                    if (hasOption) {
                        super.processOption(arg, iter);
                    }
                }
            };
            CommandLine commandLine =
                    commandLineParser.parse(OPTIONS, argumentList.toArray(new String[] {}));
            return commandLine;
        } catch (ParseException e) {
            PrintWriter printWriter = new PrintWriter(System.out, true, Charset.defaultCharset());
            HelpFormatter helpFormatter = new HelpFormatter();
            helpFormatter.printHelp(printWriter, 74,
                    "javadoc -doclet " + XmlDoclet.class.getName() + " [options]",
                    null, OPTIONS, 1, 3, null, false);
            return null;
        }
    }
}
