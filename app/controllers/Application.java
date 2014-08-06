package controllers;

import models.Fiddle;
import net.xsltransform.plugin.*;
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
import javax.xml.transform.sax.SAXResult;
import javax.xml.transform.sax.SAXSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import java.io.ByteArrayOutputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Application extends Controller {

    private static final FopFactory FOP_FACTORY = FopFactory.newInstance();

    private static final Map<String, TransformerPlugin> PLUGINS;
    static {
        PLUGINS = new HashMap<>();
        PLUGINS.put("Saxon9", new Saxon9HEPlugin());
        PLUGINS.put("Saxon9EE", new Saxon9EEPlugin());
        PLUGINS.put("Saxon6", new Saxon6Plugin());
        PLUGINS.put("Xalan2", new Xalan2Plugin());
    }
    private static final String DEFAULT_ENGINE = "Saxon9";

    public static class Files {
        public String id_slug;
        public String xml;
        public String xsl;
        public String systemId;
        public String result;
        public String engine;
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
        return ok(index.render("", 0, DEFAULT_ENGINE));
    }

    public static Result fiddle(String shortId, int revisionId) {
        return ok(index.render(shortId, revisionId, Fiddle.getByShortId(shortId).getFiddleRevision(revisionId).getEngine()));
    }

    public static Result transform() {
        Form<Files> filesForm = Form.form(Files.class).bindFromRequest();
        Files files = filesForm.get();

        // Create XSL source and set system id if this was used
        SAXSource xsl = new SAXSource(new InputSource(new StringReader(files.xsl)));
        if (!"".equals(files.systemId)) {
            xsl.setSystemId(files.systemId);
        }

        // Output for errors
        ByteArrayOutputStream errors = new ByteArrayOutputStream();

        // Create a String writer for the transformation result
        StringWriter resultWriter = new StringWriter();
        try {
            // Create a plugin
            Transformer transformer = PLUGINS.get(files.engine).newTransformer(xsl, errors);

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
            Fop fop = FOP_FACTORY.newFop(MimeConstants.MIME_PDF, pdf);
            transformer = PLUGINS.get("Saxon9").newTransformer();
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

        f.addRevision(files.xml, files.xsl, files.engine);

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