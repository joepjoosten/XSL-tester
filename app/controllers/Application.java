package controllers;

import models.Fiddle;
import net.sf.saxon.Configuration;
import net.sf.saxon.lib.FeatureKeys;
import net.sf.saxon.lib.StandardErrorListener;
import org.apache.fop.apps.FOPException;
import org.apache.fop.apps.Fop;
import org.apache.fop.apps.FopFactory;
import org.apache.fop.apps.MimeConstants;
import org.xml.sax.InputSource;
import play.Routes;
import play.data.Form;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Result;
import views.html.index;
import views.html.list;
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
import java.util.List;

public class Application extends Controller {

    private static final FopFactory fopFactory = FopFactory.newInstance();
    private static final TransformerFactory transformerFactory = TransformerFactory.newInstance();

    public static class Files {
        public String id_slug;
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

    public static Result defaultXML() {
        return ok(defaultXML.render());
    }

    public static Result defaultXSL() {
        return ok(defaultXSL.render());
    }

    public static Result index() {
        return ok(index.render("",0));
    }

    public static Result fiddle(String shortid, int revisionId) {


        return ok(index.render(shortid, revisionId));
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
            return ok(pdf.toByteArray()).as("application/pdf");
        } catch (TransformerException e) {
            return badRequest("An error occurred rendering the PDF: " + e.getMessage());
        } catch (FOPException e) {
            return badRequest("An error occurred rendering the PDF: " + e.getMessage());
        }
    }

    public static Result save() {
        Form<Files> filesForm = Form.form(Files.class).bindFromRequest();
        Files files = filesForm.get();
        Fiddle f;
        if("".equals(files.id_slug)){
            f = new Fiddle();
        } else {
            f = Fiddle.getByShortId(files.id_slug);
        }

        f.addRevision(files.xml, files.xsl);

        f.save();
        String[] res = new String[2];
        res[0] = String.valueOf(f.getShortId());
        res[1] = String.valueOf(f.getFiddleRevisionList().size()-1);

        return ok(Json.toJson(res));
    }

    public static Result xml(String id, int revision) {

        String shortid = String.valueOf(Fiddle.decodeShortenedID(id));

        Fiddle res = Fiddle.find.where().eq("id", shortid).findUnique();

        return ok(res.getFiddleRevision(revision).getXml());
    }

    public static Result xsl(String id, int revision) {
        String shortid = String.valueOf(Fiddle.decodeShortenedID(id));
        Fiddle res = Fiddle.find.where().eq("id", shortid).findUnique();
        return ok(res.getFiddleRevision(revision).getXsl());
    }

    public static Result list() {
        List<Fiddle> fiddleList = Fiddle.find.all();
        return ok(list.render(fiddleList));
    }


}
