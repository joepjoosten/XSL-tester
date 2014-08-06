package net.xsltransform.plugin;

import play.Play;

import javax.xml.transform.*;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.PrintStream;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.net.URLClassLoader;

public class Saxon6Plugin implements TransformerPlugin{

    private static final String VERSION = "saxon-6.5.5";

    private static final String TRANSFORMER_FACTORY_CLASS_NAME = "com.icl.saxon.TransformerFactoryImpl";
    private static final String STANDARD_ERROR_LISTENER_CLASS_NAME = "com.icl.saxon.StandardErrorListener";
    private static final String SET_ERROR_OUTPUT_METHOD = "setErrorOutput";

    private static final ClassLoader saxon6ClassLoader = new JarClassLoader(new InputStream[]{
            Play.application().resourceAsStream("public/plugins/"+VERSION+"/"+VERSION+".jar")
    }, Saxon6Plugin.class.getClassLoader());


    @Override
    public Transformer newTransformer() throws TransformerConfigurationException {
        return TransformerFactory.newInstance(TRANSFORMER_FACTORY_CLASS_NAME, saxon6ClassLoader).newTransformer();
    }

    @Override
    public Transformer newTransformer(Source source, ByteArrayOutputStream errors) throws TransformerConfigurationException {

        TransformerFactory transformerFactory = TransformerFactory.newInstance(TRANSFORMER_FACTORY_CLASS_NAME, saxon6ClassLoader);

        try {
            ErrorListener errorListener = transformerFactory.getErrorListener();
            Class StandardErrorListenerClass = saxon6ClassLoader.loadClass(STANDARD_ERROR_LISTENER_CLASS_NAME);
            StandardErrorListenerClass.getMethod(SET_ERROR_OUTPUT_METHOD, PrintStream.class).invoke(errorListener, new PrintStream(errors));

            return transformerFactory.newTransformer(source);

        } catch (ClassNotFoundException e) {
            throw new TransformerConfigurationException(TRANSFORMER_FACTORY_CLASS_NAME + " not found");
        } catch (NoSuchMethodException e) {
            throw new TransformerConfigurationException(TRANSFORMER_FACTORY_CLASS_NAME + ": no such method for error listener");
        } catch (IllegalAccessException e) {
            throw new TransformerConfigurationException(TRANSFORMER_FACTORY_CLASS_NAME + " not accessible");
        } catch (InvocationTargetException e) {
            throw new TransformerConfigurationException(TRANSFORMER_FACTORY_CLASS_NAME + ": could invoke method for error listener");
        }
    }
}
