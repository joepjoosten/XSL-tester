package net.xsltransform.plugin;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.SecureClassLoader;
import java.util.Hashtable;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;

public class JarClassLoader extends SecureClassLoader{

    private Hashtable<String, byte[]> classes = new Hashtable<>();
    private Hashtable<String, Class> loadedClasses = new Hashtable<>();

    JarClassLoader(InputStream[] jarFiles, ClassLoader parent) {
        super(parent);
        try {
            for (InputStream jarFile : jarFiles) {
                JarInputStream jis = new JarInputStream(jarFile);
                JarEntry entry = jis.getNextJarEntry();
                while(entry != null) {
                    if (entry.getName().endsWith(".class")) {
                        BufferedInputStream bis = new BufferedInputStream(jis);
                        ByteArrayOutputStream baos = new ByteArrayOutputStream();
                        int bytes = bis.read();
                        while (bytes != -1) {
                            baos.write(bytes);
                            bytes = bis.read();
                        }
                        classes.put(entry.getName().substring(0, entry.getName().length() - 6).replace("/", "."), baos.toByteArray());
                    }
                    entry = jis.getNextJarEntry();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            classes = null;
        }
    }

    @Override
    public Class loadClass(String name) throws ClassNotFoundException {
        if (loadedClasses.containsKey(name)) {
            return loadedClasses.get(name);
        } else if (classes.containsKey(name)) {
            byte[] classBytes = classes.get(name);
            Class clazz = defineClass(name, classBytes, 0, classBytes.length);
            loadedClasses.put(name, clazz);
            return clazz;
        } else {
            return super.loadClass(name);
        }
    }

}
