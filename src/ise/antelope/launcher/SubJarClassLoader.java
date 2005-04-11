package ise.antelope.launcher;

import java.io.*;
import java.net.*;
import java.lang.reflect.*;
import java.util.*;
import java.util.jar.*;

/**
 * A class loader for loading jar files.  By allowing "jar" urls, that is, urls
 * of the form jar:&lt;url&gt;!/{entry}, jars nested inside other jars can be
 * loaded.
 * <p>
 * Suppose you have a jar named mainjar.jar like this:<p>
 * <pre>
 *     META-INF/
 *     META-INF/MANIFEST.MF
 *     test/
 *     test/Main.class
 *     subjar.jar
 * </pre><p>    
 * Assume that test.Main is set in the manifest file so that executing<br>
 * <code>java -jar mainjar.jar</code><br>
 * will execute test.Main.  When test.Main is invoked, nothing in subjar.jar is
 * loaded into a classloader as class files.  Now suppose you want to invoke a
 * class in subjar.jar.  Code similar to this in test.Main would do it:
 * <code><pre>
 * // if subjar.jar has a class specified in the manifest as the main class, then
 * // this code would invoke that class
 * URL url = getClass().getResource("/subjar.jar");
 * SubJarClassLoader cl = new SubJarClassLoader( url );
 * String name = SubJarClassLoader.getMainClassName(url);
 * cl.invokeClass( name, args);    // args defined elsewhere
 *
 * // if you want to get an arbitrary object out of subjar.jar, then this code
 * // would do it
 * String MAIN_CLASS = "test.MyClass";
 * Class mainClass = cl.loadClass(MAIN_CLASS);
 * Object object = mainClass.newInstance();
 * </pre><p>
 * This class loader is not restricted to loading nested jars, jars from any
 * location can be loaded as long as they are specified with a jar url or file
 * url format.  If a file url is given, then it is assumed that the file is a 
 * jar file and will be treated as such.  While the comments throughout this
 * class say "jar", "zip" is also supported.
 * <p>
 * The motivation for this class loader is I want to be able to use the xml
 * parser bundled with Ant from within Antelope from within jEdit when the XML
 * plugin is also loaded. The XML plugin bundles its own parser which may not be
 * as current as that bundled with Ant.  Furthermore, I can now bundle as many
 * jar files together into a single jar file for distribution and not have to
 * worry about making sure all the jar files are in the classpath, etc.
 *
 * @author Dale Anson, Jan 2005 
 */

public class SubJarClassLoader extends URLClassLoader {
    
    public static final String HANDLER_PKG = "ise.antelope.launcher";
    
    static {
        String handler_pkgs = System.getProperty("java.protocol.handler.pkgs");
        handler_pkgs = handler_pkgs == null ? "" : handler_pkgs;
        if (handler_pkgs.indexOf(HANDLER_PKG) == -1) {
            System.setProperty("java.protocol.handler.pkgs", HANDLER_PKG + "|" + handler_pkgs);   
        }
        //System.out.println("java.protocol.handler.pkgs = " + System.getProperty("java.protocol.handler.pkgs"));
    }

    // a list of URLs that this classloader will handle
    private ArrayList urls;

    // look up for jar entries, the key is a String of the name of an entry in a
    // jar file, the value is a URL to a jar file that contains the entry.
    private HashMap myClassNames = new HashMap();

    /**
     * Creates a new SubJarClassLoader for the specified jar url.
     *
     * @param url the url of a jar
     */
    public SubJarClassLoader( URL url ) {
        this( new URL[] { url } );
    }

    /**
     * Creates a new SubJarClassLoader for the specified jar urls.
     *
     * @param urls the urls of some jars
     */
    public SubJarClassLoader( URL[] urls ) {
        super( urls );
        this.urls = new ArrayList();
        this.urls.addAll( Arrays.asList( urls ) );
        loadMyClassNames();
    }

    /**
     * Creates a new SubJarClassLoader for the specified url and sets the parent
     * classloader.
     * @param url the url of a jar
     * @param parent the parent classloader
     */
    public SubJarClassLoader( URL url, ClassLoader parent ) {
        this( new URL[] {url}, parent );
    }

    /**
     * Creates a new SubJarClassLoader for the specified urls and sets the parent
     * classloader.
     * @param urls the urls of some jars
     * @param parent the parent classloader
     */
    public SubJarClassLoader( URL[] urls, ClassLoader parent ) {
        super( urls, parent );
        this.urls = new ArrayList();
        this.urls.addAll( Arrays.asList( urls ) );
        loadMyClassNames();
    }

    /**
     * Add a URL to be handled by this classloader.  The given URL must be to a
     * jar.
     * @param url the url of a jar 
     */
    public void addURL( URL url ) {
        if ( urls == null )
            urls = new ArrayList();
        urls.add( url );
        //if ( url.getProtocol().equals( "jar" ) )
        loadClassNamesForUrl( url );
    }

    /**
     * Populates the jar entry lookup by connecting to each url that this
     * classloader knows about and reading the jar entries.
     */
    private void loadMyClassNames() {
        for ( Iterator it = urls.iterator(); it.hasNext(); ) {
            URL url = ( URL ) it.next();
            loadClassNamesForUrl( url );
        }
    }

    /**
     * Populates the jar entry lookup by connecting to the given url and 
     * reading the jar entries.
     * @param url the url to connect to
     */
    private void loadClassNamesForUrl( URL url ) {
        try {
            if ( url.getProtocol().equals( "jar" ) ) {
                JarInputStream jis = new JarInputStream( url.openStream() );
                try {
                    while ( true ) {
                        JarEntry je = jis.getNextJarEntry();
                        if ( je == null )
                            break;
                        if ( je.isDirectory() )
                            continue;
                        String je_name = je.getName();
                        if ( je_name.endsWith( ".class" ) )
                            je_name = je_name.substring( 0, je_name.length() - 6 );
                        je_name = je_name.replace( '/', '.' );
                        myClassNames.put( je_name, url );
                    }
                }
                catch ( Exception e ) {
                    jis.close();
                    e.printStackTrace();
                }
            }
            else if ( url.getProtocol().equals( "file" ) ) {
                String urlstring = url.toString();
                urlstring = urlstring.replaceAll("[ ]", "%20");
                JarFile jf = new JarFile( new File( new URI( urlstring ) ) );
                Enumeration en = jf.entries();
                while ( en.hasMoreElements() ) {
                    JarEntry je = ( JarEntry ) en.nextElement();
                    if ( je.isDirectory() )
                        continue;
                    String je_name = je.getName();
                    if ( je_name.endsWith( ".class" ) )
                        je_name = je_name.substring( 0, je_name.length() - 6 );
                    je_name = je_name.replace( '/', '.' );
                    myClassNames.put( je_name, url );
                }
            }
        }
        catch ( Exception e ) {
            e.printStackTrace();
        }
    }

    /**
     * Convenience method to get the name of the jar file main class.
     * @param url the URL of a jar file, both "file" and "jar" urls are supported.
     * @return the name of the main class as specified in the jar manifest, null
     * if the manifest does not contain a Main-Class attribute.
     */
    public static String getMainClassName( URL url ) throws IOException {
        if ( url.getProtocol().equals( "jar" ) ) {
            JarInputStream jis = new JarInputStream( url.openStream() );
            Manifest manifest = jis.getManifest();
            Attributes attr = manifest.getMainAttributes();
            String classname = attr != null ? attr.getValue( Attributes.Name.MAIN_CLASS ) : null;
            jis.close();
            return classname;
        }
        else if ( url.getProtocol().equals( "file" ) ) {
            String filename = url.getFile();
            File f = new File( filename );
            if ( !f.exists() )
                throw new FileNotFoundException( "File " + filename + " referenced by URL " + url + " does not exist." );
            JarFile jarfile = new JarFile( f );
            Manifest manifest = jarfile.getManifest();
            Attributes attr = manifest.getMainAttributes();
            String classname = attr != null ? attr.getValue( Attributes.Name.MAIN_CLASS ) : null;
            return classname;
        }
        else
            return null;
    }

    /**
     * Invokes the application in this jar file given the name of the
     * main class and an array of arguments. The class must define a
     * static method "main" which takes an array of String arguemtns
     * and is of return type "void".
     *
     * @param name the name of the main class
     * @param args the arguments for the application
     * @exception ClassNotFoundException if the specified class could not
     *            be found
     * @exception NoSuchMethodException if the specified class does not
     *            contain a "main" method
     * @exception InvocationTargetException if the application raised an
     *            exception
     */
    public void invokeMainClass( String name, String[] args )
    throws ClassNotFoundException, NoSuchMethodException, InvocationTargetException {
        Class c = loadClass( name );
        Method m = c.getMethod( "main", new Class[] { args.getClass() } );
        m.setAccessible( true );
        int mods = m.getModifiers();
        if ( m.getReturnType() != void.class || !Modifier.isStatic( mods ) ||
                !Modifier.isPublic( mods ) ) {
            throw new NoSuchMethodException( "main" );
        }
        try {
            m.invoke( null, new Object[] { args } );
        }
        catch ( IllegalAccessException e ) {
            // this should not happen as setAccessible has been set to true
        }
    }

    /**
     * Same as calling <code>loadClass(classname, false)</code>.    
     */
    public Class loadClass( String classname ) throws ClassNotFoundException {
        return loadClass( classname, false );
    }

    /**
     * Loads the class with the specified name. Unlike the default implementation of 
     * this method in ClassLoader, this method searches for classes in the following 
     * order:
     * <ol>
     * <li>Invoke findLoadedClass(String) to check if the class has already been 
     * loaded.
     * <li>Invoke the findClass(String) method to find the class in our jar file(s).
     * <li>Invoke the loadClass method on the parent class loader. If the parent 
     * is null the class loader built-in to the virtual machine is used, instead.
     * </ol>
     * If the class was found using the above steps, and the resolve flag is true, 
     * this method will then invoke the resolveClass(Class) method on the resulting 
     * Class object. 
     * <p>
     * The reason for looking for a class in our jar file(s) first is so that I
     * can override any class loaded by a parent class loader with a special class.
     * In particular, I want to load the xml parser distributed with Ant rather
     * than any parser that jEdit may have loaded.
     *
     * @param classname the name of the class to load
     * @param resolve if true, then resolve the class
     */
    public Class loadClass( String classname, boolean resolve ) throws ClassNotFoundException {
        Class c = findLoadedClass( classname );
        if ( c != null )
            return c;
        try {
            c = findClass( classname );
        }
        catch ( ClassNotFoundException e ) {
            ClassLoader parent = getParent();
            if ( parent == null )
                parent = getSystemClassLoader();
            c = parent.loadClass( classname );
        }
        if ( c != null && resolve )
            resolveClass( c );
        return c;
    }

    /**
     * Finds the specified class in one of the jar urls.
     * @param classname the name of the class to find.
     * @return the class, look in this class loader first, then the parent
     * class loader.
     */
    public Class findClass( String classname ) throws ClassNotFoundException {
        byte[] b = loadClassData( classname );
        if ( b.length == 0 )
            return super.findClass( classname );
        try {
            return defineClass( classname, b, 0, b.length );
        }
        catch ( Exception e ) {
            throw new ClassNotFoundException( "Not found: " + classname );
        }
    }

    private byte[] loadClassData( String classname ) {
        try {
            InputStream is = getDataInputStream( classname );
            if ( is == null ) {
                return new byte[ 0 ];
            }
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            while ( true ) {
                int b = is.read();
                if ( b == -1 )
                    break;
                baos.write( ( byte ) b );
            }
            is.close();
            baos.flush();
            return baos.toByteArray();
        }
        catch ( Exception e ) {
            e.printStackTrace();
            return new byte[ 0 ];
        }
    }

    /**
     * @param name the name of an entry in the jar file
     * @return a JarInputStream positioned at the start of the data for the
     * given entry or null if an entry could not be found.
     */
    private InputStream getDataInputStream( String name ) {
        try {
            URL url = ( URL ) myClassNames.get( name );
            if ( url == null )
                return null;

            if ( url.getProtocol().equals( "jar" ) ) {
                JarInputStream jis = new JarInputStream( url.openStream() );
                while ( true ) {
                    JarEntry je = jis.getNextJarEntry();
                    if ( je == null )
                        return null;
                    if ( je.isDirectory() )
                        continue;
                    String je_name = je.getName();
                    if ( je_name.endsWith( ".class" ) )
                        je_name = je_name.substring( 0, je_name.length() - 6 );
                    je_name = je_name.replace( '/', '.' );
                    if ( je_name.equals( name ) ) {
                        return jis;
                    }
                }
            }
            else if ( url.getProtocol().equals( "file" ) ) {
                String filename = url.getFile();
                File f = new File( filename );
                if ( !f.exists() )
                    return null;
                JarFile jarfile = new JarFile( f );
                JarEntry je = jarfile.getJarEntry( name );
                if ( je == null )
                    return null;
                return jarfile.getInputStream( je );
            }
            return null;
        }
        catch ( Exception e ) {
            e.printStackTrace();
            return null;
        }
    }

    public URL getResource(String name) {
        URL url = findResource(name);
        if (url == null)
            url = super.getResource(name);
        return url;
    }
    
    public URL findResource(String name) {
        try {
            String lookup = name.replace('/', '.');
            URL url = ( URL ) myClassNames.get( lookup );
            if ( url == null )
                return super.findResource(name);

            if ( url.getProtocol().equals( "jar" ) ) {
                JarInputStream jis = new JarInputStream( url.openStream() );
                while ( true ) {
                    JarEntry je = jis.getNextJarEntry();
                    if ( je == null )
                        return null;
                    if ( je.isDirectory() )
                        continue;
                    String je_name = je.getName();
                    if ( je_name.endsWith( ".class" ) ){
                        je_name = je_name.substring( 0, je_name.length() - 6 );
                        je_name = je_name.replace( '/', '.' );
                    }
                    if ( je_name.equals( name ) ) {
                        String urlstring = "sub" + url.toString() + "!/" + name;
                        URL resource_url = new URL(urlstring);
                        return resource_url;
                    }
                }
            }
            else if ( url.getProtocol().equals( "file" ) ) {
                String filename = url.getFile();
                File f = new File( filename );
                if ( !f.exists() )
                    return null;
                JarFile jarfile = new JarFile( f );
                JarEntry je = jarfile.getJarEntry( name );
                if ( je == null )
                    return null;
                String urlstring = "jar:" + url.toString() + "!/" + name;
                URL resource_url = new URL(urlstring);
                return resource_url;
            }
            return null;
        }
        catch ( Exception e ) {
            e.printStackTrace();
            return null;
        }
    }
}

