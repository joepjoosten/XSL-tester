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

public class Saxon9EEPlugin implements TransformerPlugin{

    private static final String TRANSFORMER_FACTORY_CLASS_NAME = "net.sf.saxon.TransformerFactoryImpl";


    private static final ClassLoader saxon9EEClassLoader = new JarClassLoader(new InputStream[]{
            Play.application().resourceAsStream("public/plugins/saxon-9.5.1.3-ee/saxon-9.5.1.3-ee.jar")
    }, Saxon9EEPlugin.class.getClassLoader());

    @Override
    public Transformer newTransformer() throws TransformerConfigurationException {
        return TransformerFactory.newInstance(TRANSFORMER_FACTORY_CLASS_NAME, saxon9EEClassLoader).newTransformer();
    }

    @Override
    public Transformer newTransformer(Source source, ByteArrayOutputStream errors) throws TransformerConfigurationException {

        TransformerFactory transformerFactory = TransformerFactory.newInstance(TRANSFORMER_FACTORY_CLASS_NAME, saxon9EEClassLoader);

        try {
            Class FeatureKeysClass = saxon9EEClassLoader.loadClass("net.sf.saxon.lib.FeatureKeys");
            Class ConfigurationClass = saxon9EEClassLoader.loadClass("com.saxonica.config.EnterpriseConfiguration");
            Class StandardErrorListenerClass = saxon9EEClassLoader.loadClass("net.sf.saxon.lib.StandardErrorListener");

            Object conf = transformerFactory.getAttribute((String)FeatureKeysClass.getField("CONFIGURATION").get(null));
            ConfigurationClass.getMethod("setConfigurationProperty", String.class, Object.class).invoke(conf, "http://saxon.sf.net/feature/allow-external-functions", false);
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
