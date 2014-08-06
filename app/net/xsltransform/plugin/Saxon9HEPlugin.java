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

public class Saxon9HEPlugin implements TransformerPlugin{

    private static final String VERSION = "saxon-9.5.1.6-he";

    private static final String TRANSFORMER_FACTORY_CLASS_NAME = "net.sf.saxon.TransformerFactoryImpl";

    private static final ClassLoader saxon9HEClassLoader = new JarClassLoader(new InputStream[]{
            Play.application().resourceAsStream("public/plugins/"+VERSION+"/"+VERSION+".jar")
    }, Saxon9HEPlugin.class.getClassLoader());

    @Override
    public Transformer newTransformer() throws TransformerConfigurationException {
        return TransformerFactory.newInstance(TRANSFORMER_FACTORY_CLASS_NAME, saxon9HEClassLoader).newTransformer();
    }

    @Override
    public Transformer newTransformer(Source source, ByteArrayOutputStream errors) throws TransformerConfigurationException {

        TransformerFactory transformerFactory = TransformerFactory.newInstance(TRANSFORMER_FACTORY_CLASS_NAME, saxon9HEClassLoader);

        try {
            Class FeatureKeysClass = saxon9HEClassLoader.loadClass("net.sf.saxon.lib.FeatureKeys");
            Class ConfigurationClass = saxon9HEClassLoader.loadClass("net.sf.saxon.Configuration");
            Class StandardErrorListenerClass = saxon9HEClassLoader.loadClass("net.sf.saxon.lib.StandardErrorListener");

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
