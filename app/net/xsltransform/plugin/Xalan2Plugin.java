package net.xsltransform.plugin;

import org.apache.xml.utils.ListingErrorHandler;

import javax.xml.transform.*;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.net.URLClassLoader;

public class Xalan2Plugin implements TransformerPlugin{

    public static final String CLASS_NAME = "org.apache.xalan.processor.TransformerFactoryImpl";

    private static final ClassLoader xalan2ClassLoader = new URLClassLoader(new URL[]{
            Xalan2Plugin.class.getClassLoader().getResource("public/external/xalan-2.7.1/xalan.jar"),
            Xalan2Plugin.class.getClassLoader().getResource("public/external/xalan-2.7.1/serializer.jar"),
            Xalan2Plugin.class.getClassLoader().getResource("public/external/xalan-2.7.1/xercesImpl.jar"),
            Xalan2Plugin.class.getClassLoader().getResource("public/external/xalan-2.7.1/xml-apis.jar")
    }, Xalan2Plugin.class.getClassLoader());

    @Override
    public Transformer newTransformer() throws TransformerConfigurationException {
        return TransformerFactory.newInstance(CLASS_NAME, null).newTransformer();
    }

    @Override
    public Transformer newTransformer(Source source, ByteArrayOutputStream errors) throws TransformerConfigurationException {

        TransformerFactory transformerFactory = TransformerFactory.newInstance(CLASS_NAME, null);
        ErrorListener errorListener = new ListingErrorHandler(new PrintWriter(errors));
        transformerFactory.setErrorListener(errorListener);
        return transformerFactory.newTransformer(source);

    }

}
