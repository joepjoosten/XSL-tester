package controllers;

import models.Fiddle;
import net.sf.saxon.Configuration;
import net.sf.saxon.lib.FeatureKeys;
import net.sf.saxon.lib.StandardErrorListener;
import org.apache.commons.codec.binary.Base64;
import org.apache.fop.apps.FOPException;
import org.apache.fop.apps.Fop;
import org.apache.fop.apps.FopFactory;
import org.apache.fop.apps.MimeConstants;
import org.xml.sax.InputSource;
import play.Routes;
import play.data.Form;
import play.mvc.Controller;
import play.mvc.Result;
import views.html.index;
import views.xml.defaultXML;
import views.xml.defaultXSL;

import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.sax.SAXResult;
import javax.xml.transform.sax.SAXSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.io.StringReader;
import java.io.StringWriter;

import static org.apache.commons.codec.binary.Base64.*;

public class Application extends Controller {

    private static final FopFactory fopFactory = FopFactory.newInstance();
    private static final TransformerFactory transformerFactory = TransformerFactory.newInstance();
    public static class Files {

        public String xml;
        public String xsl;
        public String systemId;
        public String result;

    }
    public static Result jsRoutes() {
        response().setContentType("text/javascript");
        return ok(
                Routes.javascriptRouter("jsRoutes",
                    controllers.routes.javascript.Application.transform(),
                    controllers.routes.javascript.Application.pdf(),
                    controllers.routes.javascript.Application.save(),
                    controllers.routes.javascript.Application.defaultXML(),
                    controllers.routes.javascript.Application.defaultXSL(),
                    controllers.routes.javascript.Application.xml(),
                    controllers.routes.javascript.Application.xsl()
                )
        );
    }

    public static Result defaultXML(){
        return ok(defaultXML.render());
    }
    public static Result defaultXSL(){
        return ok(defaultXSL.render());
    }
    public static Result index() {
        return ok(index.render(""));
    }
    public static Result fiddle(String id) {
        return ok(index.render(id));
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

        return ok(resultWriter.toString());
    }

    public static Result pdf() {
        Form<Files> filesForm = Form.form(Files.class).bindFromRequest();
        Files files = filesForm.get();
        Transformer transformer;
        try {
            ByteArrayOutputStream pdf = new ByteArrayOutputStream();
            Fop fop = fopFactory.newFop(MimeConstants.MIME_PDF, pdf);
            transformer = transformerFactory.newTransformer();
            transformer.transform(new StreamSource(new StringReader(files.result)), new SAXResult(fop.getDefaultHandler()));
            return ok(pdf.toByteArray());
        } catch (TransformerException e) {
            return badRequest("An error occurred rendering the PDF: " + e.getMessage());
        } catch (FOPException e) {
            return badRequest("An error occurred rendering the PDF: " + e.getMessage());
        }
    }

    public static Result save() {
        Form<Files> filesForm = Form.form(Files.class).bindFromRequest();
        Files files = filesForm.get();

        Fiddle f = new Fiddle();
        f.setXml(files.xml);
        f.setXsl(files.xsl);
        f.save();

        return ok(f.getId().toString());
    }

    public static Result xml(String id){
        return ok(Fiddle.find.byId(id).getXml());
    }
    public static Result xsl(String id){
        return ok(Fiddle.find.byId(id).getXsl());
    }



}
