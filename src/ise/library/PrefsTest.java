package ise.library;

import java.util.*;
import java.util.prefs.*;

public class PrefsTest {
   public static void main ( String[] args ) {
      try {
         System.setProperty( "java.util.prefs.PreferencesFactory", "ise.library.UserPreferencesFactory" );
         String PREFS_NODE = "/ise/antelope";
         Preferences PREFS = Preferences.userRoot().node( PREFS_NODE );
         System.out.println( "PREFS = " + PREFS );
         PREFS.putLong( "testDate", ( new java.util.Date().getTime() ) );
         Preferences prefs = PREFS.node( "testmore/another_node" );
         System.out.println( "prefs = " + prefs );
         System.out.println( "testLong = " + prefs.getLong( "testLong", -1 ) );
         prefs.putLong( "testLong", ( new java.util.Date().getTime() ) );
         prefs.flush();

         prefs = PREFS;
         System.out.println( "testDate = " + prefs.getLong( "testDate", -1 ) );
         Properties props = System.getProperties();
         Iterator keys = props.keySet().iterator();
         while ( keys.hasNext() ) {
            Object key = keys.next();
            Object value = props.get( key );
            System.out.println( key + " : " + value );
         }
      }
      catch ( Exception e ) {
         e.printStackTrace();
      }
   }
}
