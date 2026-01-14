package plugins;

import net.sf.saxon.TransformerFactoryImpl;

import javax.xml.transform.ErrorListener;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

/**
 * Saxon 12 HE Plugin
 * Uses Saxon from Maven dependency
 */
public class Saxon12HEPlugin implements TransformerPlugin {

    public Saxon12HEPlugin() {
        // Saxon HE is loaded from Maven dependency
    }

    @Override
    public Transformer newTransformer() throws TransformerConfigurationException {
        TransformerFactoryImpl factory = new TransformerFactoryImpl();
        return factory.newTransformer();
    }

    @Override
    public Transformer newTransformer(Source source, ByteArrayOutputStream errors) throws TransformerConfigurationException {
        TransformerFactoryImpl factory = new TransformerFactoryImpl();

        // Set up error listener to capture errors
        PrintStream errorStream = new PrintStream(errors);
        factory.setErrorListener(new ErrorListener() {
            @Override
            public void warning(TransformerException exception) {
                errorStream.println("Warning: " + exception.getMessage());
            }

            @Override
            public void error(TransformerException exception) throws TransformerException {
                errorStream.println("Error: " + exception.getMessage());
                throw exception;
            }

            @Override
            public void fatalError(TransformerException exception) throws TransformerException {
                errorStream.println("Fatal: " + exception.getMessage());
                throw exception;
            }
        });

        return factory.newTransformer(source);
    }
}
