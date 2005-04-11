package ise.antelope.launcher.subjar;
import java.io.*;

import java.net.*;
import java.util.jar.*;

/**
 * A URLConnection used to open a stream to a jar contained within a jar.  This
 * does not recurse, that is, it won't work for a jar in a jar in a jar.  What
 * this does allow is a single jar to be packaged containing other jars and 
 * access those internal jars as if they are on the classpath.
 *
 * @author Dale Anson, Jan 2005
 * @version   $Revision$
 */
public class SubJarURLConnection extends URLConnection {

    /**
     * @param url  a url with "subjar" as the protocol. The subjar URL is
     *      identical to the standard jar URL, except that jars within jars may
     *      be specified. For example, suppose myapp.jar contains b.jar and
     *      b.jar contains myimage.jpg. Then a URL for myimage.jpg would be:<br>
     *      <code>
     * subjar:file://home/username/apps/myapp.jar!/b.jar!/myimage.jpg
     * </code>
     */
    public SubJarURLConnection(URL url) {
        super(url);
    }

    /**
     * Description of the Method
     *
     * @exception IOException  Description of Exception
     */
    public void connect() throws IOException {
        // no-op
    }

    /**
     * Gets the inputStream attribute of the SubJarURLConnection object
     *
     * @return                 The inputStream value
     * @exception IOException  Description of Exception
     */
    public InputStream getInputStream() throws IOException {
        // open a stream to the entry in the jar given by the url
        try {
            URL url = getURL();
            if (url == null)
                return super.getInputStream();

            String urlstring = url.toString().substring(3);
            String name = urlstring.substring(urlstring.lastIndexOf("!") + 1);
            if (name.startsWith("/"))
                name = name.substring(1);
            urlstring = urlstring.substring(0, urlstring.lastIndexOf("!"));
            URL jar_url = new URL(urlstring);
            JarInputStream jis = new JarInputStream(jar_url.openStream());
            while (true) {
                JarEntry je = jis.getNextJarEntry();
                if (je == null)
                    return null;
                if (je.isDirectory())
                    continue;
                String je_name = je.getName();
                if (je_name.endsWith(".class")) {
                    je_name = je_name.substring(0, je_name.length() - 6);
                    je_name = je_name.replace('/', '.');
                }
                if (je_name.equals(name)) {
                    return jis;
                }
            }
        }
        catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Gets the headerField attribute of the SubJarURLConnection object
     *
     * @param name
     * @return      The headerField value
     */
    public String getHeaderField(String name) {
        if (name.equals("content-type")) {
            String url = getURL().toString().toLowerCase();
            if (url.endsWith(".html"))
                return "text/html";
            else if (url.endsWith(".txt"))
                return "text/plain";
            else if (url.endsWith(".rtf"))
                return "text/rtf";
            else if (url.endsWith(".gif"))
                return "image/gif";
            else if (url.endsWith(".jpg") || url.endsWith(".jpeg"))
                return "image/jpeg";
            else
                return super.getHeaderField(name);
        }
        else
            return super.getHeaderField(name);
    }
}

