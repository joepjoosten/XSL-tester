package net.xsltransform.plugin;

import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import java.io.ByteArrayOutputStream;

public interface TransformerPlugin {

    public Transformer newTransformer() throws TransformerConfigurationException;

    public Transformer newTransformer(Source source, ByteArrayOutputStream errors) throws TransformerConfigurationException;

}
