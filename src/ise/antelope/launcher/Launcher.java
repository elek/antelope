/*
 * Copyright  2003-2004 The Apache Software Foundation
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */
 
package ise.antelope.launcher;

import java.net.URL;
import java.net.URLClassLoader;
import java.net.MalformedURLException;
import java.io.File;
import java.lang.reflect.Constructor;
import java.util.StringTokenizer;
import java.util.List;
import java.util.ArrayList;
import java.util.Iterator;


/**
 *  This is a launcher for Ant.
 * danson: modified to launch Antelope
 *
 * @since Ant 1.6
 */
public class Launcher {
    /** The Ant Home property */
    public static final String ANTHOME_PROPERTY = "ant.home";

    /** The Ant Library Directory property */
    public static final String ANTLIBDIR_PROPERTY = "ant.library.dir";

    /** The location of a per-user library directory */
    public static final String USER_LIBDIR = ".ant/lib";

    /** The startup class that is to be run */
    public static final String MAIN_CLASS = "ise.antelope.app.Antelope";
    
    private static URLClassLoader loader = null;

    /**
     *  Entry point for starting command line Antelope
     *
     * @param  args commandline arguments
     */
    public static void main(String[] args) {
        try {
            Launcher launcher = new Launcher();
            launcher.run(args);
        } catch (LaunchException e) {
            System.err.println(e.getMessage());
            System.exit(1);
        } catch (Throwable t) {
            t.printStackTrace();
            System.exit(1);
        }
    }


    /**
     * Run the launcher to launch Antelope
     *
     * @param args the command line arguments
     *
     * @exception MalformedURLException if the URLs required for the classloader
     *            cannot be created.
     */
    public static void run(String[] args) throws LaunchException, MalformedURLException {
        String antHomeProperty = AntUtils.getAntHome();
        File antHome = null;

        if (antHomeProperty != null) {
            antHome = new File(antHomeProperty);
            System.setProperty(ANTHOME_PROPERTY, antHomeProperty);
        }
        System.out.println("ANT_HOME: " + antHome);
        if (antHome == null || !antHome.exists()) {
            /// need to ask user for Ant location
            WhereIsAntDialog d = new WhereIsAntDialog();
            d.setVisible(true);
            String ah = d.getAntHome();
            if (ah != null)
                antHome = new File(ah);
            if (ah == null || !antHome.exists())
                throw new LaunchException("Ant home is set incorrectly or "
                    + "ant could not be located");
            System.setProperty(ANTHOME_PROPERTY, antHome.getAbsolutePath());
        }

        // app jars
        File sourceJar = Locator.getClassSource(ise.launcher.Launcher.class);
        File jarDir = sourceJar.getParentFile();
        URL[] appJars = Locator.getLocationURLs(jarDir);

        // library jars specified on command line        
        List libPaths = new ArrayList();
        List argList = new ArrayList();
        String[] newArgs;

        for (int i = 0; i < args.length; ++i) {
            if (args[i].equals("-lib")) {
                if (i == args.length - 1) {
                    throw new LaunchException("The -lib argument must "
                        + "be followed by a library location");
                }
                libPaths.add(args[++i]);
            } else {
                argList.add(args[i]);
            }
        }

        if (libPaths.size() == 0) {
            newArgs = args;
        } else {
            newArgs = (String[]) argList.toArray(new String[0]);
        }
        
        List libPathURLs = new ArrayList();
        for (Iterator i = libPaths.iterator(); i.hasNext();) {
            String libPath = (String) i.next();
            StringTokenizer myTokenizer = new StringTokenizer(libPath, System.getProperty("path.separator"));
            while (myTokenizer.hasMoreElements()) {
                String elementName = myTokenizer.nextToken();
                File element = new File(elementName);
                if (elementName.indexOf("%") != -1 && !element.exists()) {
                    continue;
                }
                if (element.isDirectory()) {
                    // add any jars in the directory
                    URL[] dirURLs = Locator.getLocationURLs(element);
                    for (int j = 0; j < dirURLs.length; ++j) {
                        libPathURLs.add(dirURLs[j]);
                    }
                }

                libPathURLs.add(element.toURL());
            }
        }

        URL[] libJars = (URL[]) libPathURLs.toArray(new URL[0]);

        // Now try to find JAVA_HOME to load tools.jar
        File toolsJar = Locator.getToolsJar();

        // determine ant library directory for system jars: use property
        // or default using location of ant-launcher.jar
        File antLibDir = null;
        String antLibDirProperty = System.getProperty(ANTLIBDIR_PROPERTY);
        if (antLibDirProperty != null) {
            antLibDir = new File(antLibDirProperty);
        }
        if ((antLibDir == null) || !antLibDir.exists()) {
            antLibDir = new File(antHome, "lib");
            System.setProperty(ANTLIBDIR_PROPERTY, antLibDir.getAbsolutePath());
        }
        URL[] systemJars = Locator.getLocationURLs(antLibDir);

        // user library jars
        File userLibDir = new File(System.getProperty("user.home"), USER_LIBDIR);
        URL[] userJars = Locator.getLocationURLs(userLibDir);


        List jarsList = new ArrayList();
        for (int i = 0; i < appJars.length; i++)
            jarsList.add(appJars[i]);
        for (int i = 0; i < libJars.length; i++)
            jarsList.add(libJars[i]);
        for (int i = 0; i < userJars.length; i++)
            jarsList.add(userJars[i]);
        for (int i = 0; i < systemJars.length; i++)
            jarsList.add(systemJars[i]);
        if (toolsJar != null)
            jarsList.add(toolsJar.toURL());
        
        URL[] jars = (URL[])jarsList.toArray(new URL[0]);


        // now update the class.path property
        StringBuffer baseClassPath = new StringBuffer(System.getProperty("java.class.path"));
        if (baseClassPath.charAt(baseClassPath.length() - 1) == File.pathSeparatorChar) {
            baseClassPath.setLength(baseClassPath.length() - 1);
        }

        for (int i = 0; i < jars.length; ++i) {
            baseClassPath.append(File.pathSeparatorChar);
            baseClassPath.append(Locator.fromURI(jars[i].toString()));
        }

        System.setProperty("java.class.path", baseClassPath.toString());
        
        // set up a class loader for the application and start the app
        loader = new URLClassLoader(jars);
        Thread.currentThread().setContextClassLoader(loader);
        try {
            Class mainClass = loader.loadClass(MAIN_CLASS);
            Constructor constructor = mainClass.getConstructor(new Class[]{newArgs.getClass()});
            constructor.newInstance(new Object[]{newArgs});
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }
}

