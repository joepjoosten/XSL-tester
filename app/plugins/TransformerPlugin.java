package plugins;

import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import java.io.ByteArrayOutputStream;

public interface TransformerPlugin {

    Transformer newTransformer() throws TransformerConfigurationException;

    Transformer newTransformer(Source source, ByteArrayOutputStream errors) throws TransformerConfigurationException;

}
