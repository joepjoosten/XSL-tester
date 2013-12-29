package net.xsltransform.plugin;

import play.Play;

import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerFactory;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.PrintStream;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.net.URLClassLoader;

public class Saxon9Plugin implements TransformerPlugin{

    private static final String TRANSFORMER_FACTORY_CLASS_NAME = "net.sf.saxon.TransformerFactoryImpl";


    private static final ClassLoader saxon9ClassLoader = new JarClassLoader(new InputStream[]{
            Play.application().resourceAsStream("public/external/saxon-9.5.1.3-he/saxon-9.5.1.3-he.jar")
    }, Saxon9Plugin.class.getClassLoader());

    @Override
    public Transformer newTransformer() throws TransformerConfigurationException {
        return TransformerFactory.newInstance(TRANSFORMER_FACTORY_CLASS_NAME, saxon9ClassLoader).newTransformer();
    }

    @Override
    public Transformer newTransformer(Source source, ByteArrayOutputStream errors) throws TransformerConfigurationException {

        TransformerFactory transformerFactory = TransformerFactory.newInstance(TRANSFORMER_FACTORY_CLASS_NAME, saxon9ClassLoader);

        try {
            Class FeatureKeysClass = saxon9ClassLoader.loadClass("net.sf.saxon.lib.FeatureKeys");
            Class ConfigurationClass = saxon9ClassLoader.loadClass("net.sf.saxon.Configuration");
            Class StandardErrorListenerClass = saxon9ClassLoader.loadClass("net.sf.saxon.lib.StandardErrorListener");

            Object conf = transformerFactory.getAttribute((String)FeatureKeysClass.getField("CONFIGURATION").get(null));
            Object errorListener = ConfigurationClass.getMethod("getErrorListener").invoke(conf);
            StandardErrorListenerClass.getMethod("setErrorOutput", PrintStream.class).invoke(errorListener, new PrintStream(errors));

            return transformerFactory.newTransformer(source);

        } catch (ClassNotFoundException e) {
            throw new TransformerConfigurationException("net.sf.saxon.TransformerFactoryImpl not found");
        } catch (NoSuchMethodException e) {
            throw new TransformerConfigurationException("net.sf.saxon.TransformerFactoryImpl no such method for error listener");
        } catch (IllegalAccessException e) {
            throw new TransformerConfigurationException("net.sf.saxon.TransformerFactoryImpl not accessible");
        } catch (InvocationTargetException e) {
            throw new TransformerConfigurationException("net.sf.saxon.TransformerFactoryImpl could invoke method for error listener");
        } catch (NoSuchFieldException e) {
            throw new TransformerConfigurationException("net.sf.saxon.TransformerFactoryImpl couldn't get the 'Configuration' field");
        }
    }
}
