package ise.antelope.common;

import java.util.regex.*;

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

        if ( av.indexOf( "1.6" ) > -1 )
            return 1.60;
        if ( av.indexOf( "1.5" ) > -1 )
            return 1.50;
        if ( av.indexOf( "1.4" ) > -1 )
            return 1.40;
        return 1.50;
    }

}
