package controllers;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import jakarta.persistence.EntityManager;
import models.Fiddle;
import models.FiddleRevision;
import org.apache.fop.apps.FOPException;
import org.apache.fop.apps.Fop;
import org.apache.fop.apps.FopFactory;
import org.apache.fop.apps.MimeConstants;
import org.xml.sax.InputSource;
import play.data.DynamicForm;
import play.data.FormFactory;
import play.db.jpa.JPAApi;
import play.Environment;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Http;
import play.mvc.Result;
import play.routing.JavaScriptReverseRouter;
import plugins.Saxon12HEPlugin;
import plugins.TransformerPlugin;
import plugins.Xalan2Plugin;

import javax.inject.Inject;
import javax.inject.Singleton;
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

@Singleton
public class HomeController extends Controller {

    private final JPAApi jpaApi;
    private final FormFactory formFactory;
    private final FopFactory fopFactory;
    private final Map<String, TransformerPlugin> plugins;
    private static final String DEFAULT_ENGINE = "Saxon12";

    @Inject
    public HomeController(JPAApi jpaApi, FormFactory formFactory, Environment environment) {
        this.jpaApi = jpaApi;
        this.formFactory = formFactory;
        this.fopFactory = FopFactory.newInstance(new java.io.File(".").toURI());

        this.plugins = new HashMap<>();
        this.plugins.put("Saxon12", new Saxon12HEPlugin());
        this.plugins.put("Xalan2", new Xalan2Plugin(environment));
    }

    public Result index(Http.Request request) {
        return ok(views.html.index.render("", 0, DEFAULT_ENGINE, request));
    }

    public Result fiddle(Http.Request request, String shortId, int revisionId) {
        return jpaApi.withTransaction(em -> {
            long id = Fiddle.decodeShortenedID(shortId);
            Fiddle fiddle = em.find(Fiddle.class, id);
            if (fiddle == null) {
                return notFound("Fiddle not found");
            }
            FiddleRevision revision = fiddle.getRevision(revisionId);
            if (revision == null) {
                revision = fiddle.getLatestRevision();
            }
            String engine = revision != null ? revision.getEngine() : DEFAULT_ENGINE;
            return ok(views.html.index.render(shortId, revisionId, engine, request));
        });
    }

    public Result fiddleWithRevision(Http.Request request, String shortId, int revisionId) {
        return fiddle(request, shortId, revisionId);
    }

    public Result transform(Http.Request request) {
        DynamicForm form = formFactory.form().bindFromRequest(request);
        String xml = form.get("xml");
        String xsl = form.get("xsl");
        String systemId = form.get("systemId");
        String engine = form.get("engine");

        if (engine == null || !plugins.containsKey(engine)) {
            engine = DEFAULT_ENGINE;
        }

        SAXSource xslSource = new SAXSource(new InputSource(new StringReader(xsl)));
        if (systemId != null && !systemId.isEmpty()) {
            xslSource.setSystemId(systemId);
        }

        ByteArrayOutputStream errors = new ByteArrayOutputStream();
        StringWriter resultWriter = new StringWriter();

        try {
            Transformer transformer = plugins.get(engine).newTransformer(xslSource, errors);
            transformer.transform(
                    new SAXSource(new InputSource(new StringReader(xml))),
                    new StreamResult(resultWriter)
            );
        } catch (TransformerException e) {
            String errorOutput = errors.toString();
            if (errorOutput.isEmpty()) {
                errorOutput = e.getMessage();
            }
            resultWriter.write(errorOutput);
        }

        return ok(resultWriter.toString());
    }

    public Result pdf(Http.Request request) {
        DynamicForm form = formFactory.form().bindFromRequest(request);
        String result = form.get("result");

        try {
            ByteArrayOutputStream pdf = new ByteArrayOutputStream();
            Fop fop = fopFactory.newFop(MimeConstants.MIME_PDF, pdf);
            Transformer transformer = plugins.get("Saxon12").newTransformer();
            transformer.transform(
                    new StreamSource(new StringReader(result)),
                    new SAXResult(fop.getDefaultHandler())
            );
            // Allow PDF to be displayed in iframe (same origin)
            return ok(pdf.toByteArray())
                    .as("application/pdf")
                    .withHeader("X-Frame-Options", "SAMEORIGIN")
                    .withHeader("Content-Security-Policy", "frame-ancestors 'self'");
        } catch (TransformerException | FOPException e) {
            return badRequest("Error rendering PDF: " + e.getMessage());
        }
    }

    public Result save(Http.Request request) {
        DynamicForm form = formFactory.form().bindFromRequest(request);
        String idSlug = form.get("id_slug");
        String xml = form.get("xml");
        String xsl = form.get("xsl");
        String engine = form.get("engine");

        return jpaApi.withTransaction(em -> {
            Fiddle fiddle;
            if (idSlug == null || idSlug.isEmpty()) {
                fiddle = new Fiddle();
                em.persist(fiddle);
                em.flush(); // Get the ID
            } else {
                long id = Fiddle.decodeShortenedID(idSlug);
                fiddle = em.find(Fiddle.class, id);
                if (fiddle == null) {
                    fiddle = new Fiddle();
                    em.persist(fiddle);
                    em.flush();
                }
            }

            fiddle.addRevision(xml, xsl, engine);
            em.merge(fiddle);

            ArrayNode response = JsonNodeFactory.instance.arrayNode();
            response.add(fiddle.getShortId());
            response.add(String.valueOf(fiddle.getRevisions().size()));

            return ok(response);
        });
    }

    public Result xml(Http.Request request, String id, int revision) {
        return jpaApi.withTransaction(em -> {
            long fiddleId = Fiddle.decodeShortenedID(id);
            Fiddle fiddle = em.find(Fiddle.class, fiddleId);
            if (fiddle == null) {
                return notFound("Fiddle not found");
            }
            FiddleRevision rev = fiddle.getRevision(revision);
            if (rev == null) {
                return notFound("Revision not found");
            }
            return ok(rev.getXml()).as("application/xml");
        });
    }

    public Result xsl(Http.Request request, String id, int revision) {
        return jpaApi.withTransaction(em -> {
            long fiddleId = Fiddle.decodeShortenedID(id);
            Fiddle fiddle = em.find(Fiddle.class, fiddleId);
            if (fiddle == null) {
                return notFound("Fiddle not found");
            }
            FiddleRevision rev = fiddle.getRevision(revision);
            if (rev == null) {
                return notFound("Revision not found");
            }
            return ok(rev.getXsl()).as("application/xml");
        });
    }

    public Result list(Http.Request request) {
        return jpaApi.withTransaction(em -> {
            List<Fiddle> fiddles = em.createQuery("SELECT f FROM Fiddle f", Fiddle.class)
                    .getResultList();
            return ok(views.html.list.render(fiddles, request));
        });
    }

    public Result jsRoutes(Http.Request request) {
        return ok(
                JavaScriptReverseRouter.create(
                        "jsRoutes",
                        "jQuery.ajax",
                        request.host(),
                        routes.javascript.HomeController.transform(),
                        routes.javascript.HomeController.pdf(),
                        routes.javascript.HomeController.save(),
                        routes.javascript.HomeController.xml(),
                        routes.javascript.HomeController.xsl()
                )
        ).as("text/javascript");
    }
}
