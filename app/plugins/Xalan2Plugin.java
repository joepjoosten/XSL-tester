package plugins;

import org.apache.xml.utils.ListingErrorHandler;

import javax.xml.transform.*;
import java.io.ByteArrayOutputStream;
import java.io.PrintWriter;

/**
 * Xalan 2.7.3 Plugin
 * Uses Xalan from Maven dependency
 */
public class Xalan2Plugin implements TransformerPlugin {

    private static final String CLASS_NAME = "org.apache.xalan.processor.TransformerFactoryImpl";

    public Xalan2Plugin() {
        // Xalan is loaded from Maven dependency
    }

    // Constructor with Environment parameter for backwards compatibility
    public Xalan2Plugin(play.Environment environment) {
        this();
    }

    @Override
    public Transformer newTransformer() throws TransformerConfigurationException {
        TransformerFactory factory = TransformerFactory.newInstance(CLASS_NAME, null);
        return factory.newTransformer();
    }

    @Override
    public Transformer newTransformer(Source source, ByteArrayOutputStream errors) throws TransformerConfigurationException {
        TransformerFactory factory = TransformerFactory.newInstance(CLASS_NAME, null);
        ErrorListener errorListener = new ListingErrorHandler(new PrintWriter(errors));
        factory.setErrorListener(errorListener);
        return factory.newTransformer(source);
    }
}
