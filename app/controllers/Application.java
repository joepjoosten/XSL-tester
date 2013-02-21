package controllers;

import net.sf.saxon.Configuration;
import net.sf.saxon.lib.FeatureKeys;
import net.sf.saxon.lib.StandardErrorListener;

import org.apache.fop.apps.*;

import org.xml.sax.InputSource;
import play.data.Form;
import play.mvc.*;

import views.html.*;

import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.sax.SAXResult;
import javax.xml.transform.sax.SAXSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import java.io.*;

public class Application extends Controller {

    private static final FopFactory fopFactory = FopFactory.newInstance();
    private static final TransformerFactory transformerFactory = TransformerFactory.newInstance();

    public static final String DEFAULT_XML =
            "<?xml version=\"1.0\" encoding=\"UTF-8\" ?>\n" +
            "<root></root>";
    public static final String DEFAULT_XSL =
            "<?xml version=\"1.0\" encoding=\"UTF-8\" ?>\n" +
            "<xsl:transform xmlns:xsl=\"http://www.w3.org/1999/XSL/Transform\" version=\"2.0\">\n" +
            "    <xsl:template match=\"@*|node()\">\n" +
            "        <xsl:copy>\n" +
            "            <xsl:apply-templates select=\"@*|node()\"/>\n" +
            "        </xsl:copy>\n" +
            "    </xsl:template>\n" +
            "</xsl:transform>";
    public static final String DEFAULT_RESULT =
            "<?xml version=\"1.0\" encoding=\"UTF-8\" ?>";

    public static class Files {

        public String xml;
        public String xsl;
        public String systemId;
        public String result;

    }

    public static Result index() {
        return ok(index.render(DEFAULT_XML, DEFAULT_XSL, DEFAULT_RESULT));
    }

    public static Result transform() {
        Form<Files> filesForm = Form.form(Files.class).bindFromRequest();
        Files files = filesForm.get();

        // Redirect error listener to errors byte stream to give user feedback on input
        Configuration conf = (Configuration) transformerFactory.getAttribute(FeatureKeys.CONFIGURATION);
        StandardErrorListener errorListener = (StandardErrorListener) conf.getErrorListener();
        ByteArrayOutputStream errors = new ByteArrayOutputStream();
        errorListener.setErrorOutput(new PrintStream(errors));

        // Create XSL source and set system id if this was used
        SAXSource xsl = new SAXSource(new InputSource(new StringReader(files.xsl)));
        if (!"".equals(files.systemId)) {
            xsl.setSystemId(files.systemId);
        }

        // Create a String writer for the transformation result
        StringWriter resultWriter = new StringWriter();
        try {
            // Create a transformer
            Transformer transformer = transformerFactory.newTransformer(xsl);

            // Create output source
            transformer.transform(new SAXSource(new InputSource(new StringReader(files.xml))), new StreamResult(resultWriter));

        } catch (TransformerException e) {
            resultWriter.write(errors.toString());
        }

        return ok(index.render(files.xml, files.xsl, resultWriter.toString()));
    }

    public static Result pdf() {
        Form<Files> filesForm = Form.form(Files.class).bindFromRequest();
        Files files = filesForm.get();

        try {
            ByteArrayOutputStream pdf = new ByteArrayOutputStream();
            Fop fop = fopFactory.newFop(MimeConstants.MIME_PDF, pdf);
            Transformer transformer = transformerFactory.newTransformer();
            transformer.transform(new StreamSource(new StringReader(files.result)), new SAXResult(fop.getDefaultHandler()));
            return ok(pdf.toByteArray()).as("application/pdf");
        } catch (TransformerException e) {
            return ok(index.render(files.xml, files.xsl, "An error occurred rendering the PDF."));
        } catch (FOPException e) {
            return ok(index.render(files.xml, files.xsl, "An error occurred rendering the PDF."));
        }
    }
  
}
