package ise.antelope.common;

import java.io.*;
import java.util.regex.*;
import ise.library.Os;

public class AntUtils {
    private static double ant_version = 0;

    /**
     * Ant has a version number standard of majorversion.minorversion.patchlevel.
     * This method combines the minorversion and patchlevel into a single number,
     * and returns a float. For example, the float returned for Ant 1.5.4 would
     * be 1.54, for 1.6.2, 1.62.
     * @return the Ant version if possible. The number returned is formatted as
     * majorversion.minorversionpatchlevel.
     */
    public static double getAntVersion() {
        if ( ant_version > 0 )
            return ant_version;
        String av = org.apache.tools.ant.Main.getAntVersion();
        Pattern p = Pattern.compile( "\\d+[.]\\d+[.]\\d+" );
        Matcher m = p.matcher( av );
        if ( m.matches() ) {
            int start = m.start();
            int end = m.end();
            av = av.substring( start, end - start );
            String[] split = av.split( "[.]" );
            av = split[ 0 ] + "." + split[ 1 ] + split[ 2 ];
            ant_version = Float.parseFloat( av );
            return ant_version;
        }

        if ( av.indexOf( "1.9" ) > -1 )
            return 1.90;
        if ( av.indexOf( "1.8" ) > -1 )
            return 1.80;
        if ( av.indexOf( "1.7" ) > -1 )
            return 1.70;
        if ( av.indexOf( "1.6" ) > -1 )
            return 1.60;
        if ( av.indexOf( "1.5" ) > -1 )
            return 1.50;
        if ( av.indexOf( "1.4" ) > -1 )
            return 1.40;
        return 1.50;
    }

    /**
     * Returns ANT_HOME as defined by an OS environment variable or System
     * property. System property is checked first, so it takes precedence, that is,
     * it can be added on the command line to override an environment setting.
     * <p>
     * Changed to for ANT_HOME first in Antelope's preferences, then System,
     * then environment.
     *
     * @return ANT_HOME or null if not found in preferences, System, or environment.
     */
    public static String getAntHome() {
        String ant_home = null;
        try {
            // first, check stored settings
            ant_home = Constants.PREFS.get( Constants.ANT_HOME, null );
            if ( ant_home != null ) {
                File ant_dir = new File( ant_home );
                if ( ant_dir.exists() ) {
                    return ant_home;
                }
            }
            // second, check System properties
            ant_home = System.getProperty( "ANT_HOME" );
            if ( ant_home != null ) {
                File ant_dir = new File( ant_home );
                if ( ant_dir.exists() ) {
                    Constants.PREFS.put( Constants.ANT_HOME, ant_home );
                    return ant_home;
                }
            }

            // third, check environment
            ant_home = Os.getEnvironmentValue( "ANT_HOME" );
            if ( ant_home != null ) {
                File ant_dir = new File( ant_home );
                if ( ant_dir.exists() ) {
                    Constants.PREFS.put( Constants.ANT_HOME, ant_home );
                    return ant_home;
                }
            }
        }
        catch ( Exception e ) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * @return a path containing directories that ant jars are loaded from
     */
    public static String getAntLibDirs() {
        StringBuffer sb = new StringBuffer();
        File ant_lib = new File( getAntHome(), "lib" );
        if ( ant_lib.exists() && ant_lib.isDirectory() ) {
            String[] jars = ant_lib.list( new FilenameFilter() {
                        public boolean accept( File dir, String name ) {
                            return name.endsWith( ".jar" );
                        }
                    }
                                        );
            if ( jars.length > 0 )
                sb.append( ant_lib.getAbsolutePath() );
        }

        ant_lib = new File( System.getProperty( "user.home" ) + ".ant", "lib" );
        if ( ant_lib.exists() && ant_lib.isDirectory() ) {
            String[] jars = ant_lib.list( new FilenameFilter() {
                        public boolean accept( File dir, String name ) {
                            return name.endsWith( ".jar" );
                        }
                    }
                                        );
            if ( jars.length > 0 )
                sb.append( File.pathSeparator ).append( ant_lib.getAbsolutePath() );
        }
        return sb.length() > 0 ? sb.toString() : null;
    }
}
