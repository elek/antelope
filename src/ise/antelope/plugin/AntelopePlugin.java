// $Id$
/*
* Based on the Apache Software License, Version 1.1
*
* Copyright (c) 2002 Dale Anson.  All rights reserved.
*
* Redistribution and use in source and binary forms, with or without
* modification, are permitted provided that the following conditions
* are met:
*
* 1. Redistributions of source code must retain the above copyright
*    notice, this list of conditions and the following disclaimer.
*
* 2. Redistributions in binary form must reproduce the above copyright
*    notice, this list of conditions and the following disclaimer in
*    the documentation and/or other materials provided with the
*    distribution.
*
* 3. The end-user documentation included with the redistribution, if
*    any, must include the following acknowlegement:
*       "This product includes software developed by Dale Anson,
*        danson@users.sourceforge.net."
*    Alternately, this acknowlegement may appear in the software itself,
*    if and wherever such third-party acknowlegements normally appear.
*
* 4. The name "Antelope" must not be used to endorse or promote products derived
*    from this software without prior written permission. For written
*    permission, please contact danson@users.sourceforge.net.
*
* 5. Products derived from this software may not be called "Antelope"
*    nor may "Antelope" appear in their names without prior written
*    permission of Dale Anson.
*
* THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
* WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
* OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
* DISCLAIMED.  IN NO EVENT SHALL DALE ANSON OR ANY PROJECT
* CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
* SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
* LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
* USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
* ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
* OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
* OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
* SUCH DAMAGE.
* ====================================================================
*/
package ise.antelope.plugin;


import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.net.*;
import java.util.*;
import javax.swing.*;
import console.*;
import errorlist.*;
import ise.antelope.common.AntelopePanel;
import ise.antelope.common.Constants;
import ise.library.Os;
import org.gjt.sp.jedit.*;
import org.gjt.sp.jedit.gui.DockableWindowManager;
import org.gjt.sp.jedit.gui.OptionsDialog;
import org.gjt.sp.util.Log;
import org.gjt.sp.jedit.msg.BufferUpdate;
import org.gjt.sp.jedit.msg.EditorExiting;
import org.gjt.sp.jedit.msg.ViewUpdate;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.ProjectHelper;

/**
 * This is the AntelopePlugin.
 *
 * @version   $Revision$
 */
public class AntelopePlugin extends EBPlugin implements Constants {
   /**
    * This name will be used to reference the properties for the plugin.
    */
   public final static String NAME = "Antelope";
   /**
    * This name will be used to reference the menu items on the jEdit plugin
    * menu for the plugin.
    */
   public final static String MENU = "Antelope.menu";

   // a shell instance for output
   public static AntelopeShell SHELL = new AntelopeShell();

   // one panel per view
   private static HashMap panelList = new HashMap();

   // list of Ant's jars that have been loaded by this plugin
   private static String antJars = null;

   private static ArrayList _listeners = new ArrayList();

   static {
      // load our preferences handler -- this one doesn't give any problems on
      /// Linux like the default preferences handler does. ??? could this possibly
      /// cause problems with a system preferences factory? Shouldn't there be
      /// delegates?
      try {
         Class.forName("ise.antelope.common.Constants");
      }
      catch(Exception e) {
         e.printStackTrace();
      }
      
      // also reset the ant jars property
      jEdit.resetProperty( "plugin.ise.antelope.plugin.AntelopePlugin.jars" );
   }

   /**
    * This method is called every time a view is created to set up the Plugins
    * menu. Menus and menu items should be loaded using the methods in the
    * GUIUtilities class, and added to given the vector.
    *
    * @param menuItems  add menu items to this Vector
    */
   public void createMenuItems( Vector menuItems ) {
      menuItems.addElement( GUIUtilities.loadMenu( MENU ) );
   }

   /**
    * Method called by jEdit to initialize the plugin. Actions and edit modes
    * should be registered here, along with any EditBus requirements.
    */
   public void start() {
      panelList = new HashMap();
      // jEdit keeps a list in a properties, reset it now as we'll want to
      // change the value when the AntelopePluginPanel starts up.
      jEdit.resetProperty( "plugin.ise.antelope.plugin.AntelopePlugin.jars" );

      // add this plugin to the EditBus so this plugin can recieve
      // edit bus messages
      EditBus.addToBus( this );

      // load the jdk tools.jar if available. Technically, this isn't necessary --
      // some Ant tasks use the tools.jar, loading it here is a convenience for the
      // user, but not a requirement.
      MiscUtilities.isToolsJarAvailable();

      Shell.registerShell( SHELL );
      //Log.log( Log.DEBUG, AntelopePlugin.class, ">>>>>>>>>> AntelopePlugin.start()" );

   }

   /**
    * Method called by jEdit before exiting, and when this plugin is unloaded by
    * the PluginManager. This plugin takes the opportunity to unload all of Ant's
    * jars when it stops so reloading the plugin works correctly.
    */
   public void stop() {
      //Log.log( Log.DEBUG, AntelopePlugin.class, ">>>>>>>>>> AntelopePlugin.stop()" );
      // jEdit keeps a list in a properties, reset it as we'll want to reload it
      // when/if the plugin restarts
      jEdit.resetProperty( "plugin.ise.antelope.plugin.AntelopePlugin.jars" );
      if ( antJars == null )
         antJars = getAntJars( false );

      // unload Ant's jars also
      StringTokenizer st = new StringTokenizer( antJars );
      while ( st.hasMoreTokens() ) {
         String token = st.nextToken();
         PluginJAR pj = jEdit.getPluginJAR( token );
         jEdit.removePluginJAR( pj, false );
      }

      if ( panelList != null ) {
         Iterator it = panelList.values().iterator();
         while ( it.hasNext() ) {
            AntelopePluginPanel panel = ( AntelopePluginPanel ) it.next();
            panel.close();
         }
      }

      antJars = null;
      panelList = null;
      Shell.unregisterShell( SHELL );
      SHELL = null;
      _listeners = null;
   }

   protected void finalize() {
      jEdit.resetProperty( "plugin.ise.antelope.plugin.AntelopePlugin.jars" );
   }

   public static void reload() {
      EditPlugin plugin = jEdit.getPlugin( "ise.antelope.plugin.AntelopePlugin" );
      PluginJAR jar = plugin.getPluginJAR();
      jEdit.removePluginJAR( jar, false );
      jEdit.addPluginJAR( jar.getPath() );
   }

   /**
    * Handles a message sent on the EditBus. The default implementation ignores
    * the message. This plugin registers itself to receive messages from the
    * edit bus in the <code>start</code> method. The only message handles is a
    * BufferUpdate message, and then only if the buffer is for the Ant build
    * file. If the build file has been edited and saved, then this panel is
    * reloaded to reflect the changes.
    *
    * @param message  Description of Parameter
    */
   public void handleMessage( EBMessage message ) {
      //org.gjt.sp.util.Log.log(org.gjt.sp.util.Log.DEBUG, this, message);
      if ( message instanceof BufferUpdate ) {
         if ( panelList == null )
            return ;
         // check if the updated buffer is our build file, if it is and it's being
         // saved, reload this panel
         BufferUpdate msg = ( BufferUpdate ) message;
         if ( msg.getWhat().equals( BufferUpdate.SAVED ) ) {
            String filename = msg.getBuffer().getPath();
            if ( filename != null ) {
               File f = new File( filename );
               Iterator it = panelList.keySet().iterator();
               while ( it.hasNext() ) {
                  View view = ( View ) it.next();
                  AntelopePluginPanel panel = ( AntelopePluginPanel ) panelList.get( view );
                  if ( f.equals( panel.getBuildFile() ) ) {
                     panel.reload();
                  }
                  ActionEvent ae = new ActionEvent( view, 0, f.getAbsolutePath() );
                  fireActionEvent( ae );
               }
            }
         }
      }
      else if ( message instanceof EditorExiting ) {
         if ( panelList == null )
            return ;
         Iterator it = panelList.values().iterator();
         while ( it.hasNext() ) {
            AntelopePluginPanel panel = ( AntelopePluginPanel ) it.next();
            panel.close();
         }
      }
      else if ( message instanceof ViewUpdate ) {
         ViewUpdate msg = ( ViewUpdate ) message;
         Object what = msg.getWhat();
         if ( what.equals( ViewUpdate.CLOSED ) ) {
            AntelopePluginPanel panel = ( AntelopePluginPanel ) panelList.remove( msg.getView() );
            if ( panel != null )
               panel.close();
         }
      }
   }


   // the following methods are not part of the standard EditPlugin API
   /**
    * Adds a feature to the Panel attribute of the AntelopePlugin class
    *
    * @param view   The feature to be added to the Panel attribute
    * @param panel  The feature to be added to the Panel attribute
    */
   public static void addPanel( View view, AntelopePluginPanel panel ) {
      if ( view == null )
         return ;
      if ( panelList == null )
         panelList = new HashMap();
      panelList.put( view, panel );
      File build_file = getBuildFile( view );
      if ( build_file != null ) {
         ActionEvent ae = new ActionEvent( view, 0, build_file.getAbsolutePath() );
         fireActionEvent( ae );
      }
      else
         System.out.println( "AntelopePanel.addPanel, build_file is null" );
   }

   /**
    * ActionListeners added via this method will receive an ActionEvent when a 
    * build file changes for a View (there is a 1-1 relationship between a View
    * and an Antelope instance). The ActionEvent will contain the name of the
    * build file (full path included) as the action command (retrieve with 
    * ActionEvent.getActionCommand()) and the View as the source object (retrieve
    * with ActionEvent.getSource()).
    *
    * /// need to define 'changed' -- when the build file has been edited or
    * when a new build file has been opened? Both?
    *
    * @param al an action listener interested in receiving notification of when
    * the build file has been changed.
    */
   public static void addActionListener( ActionListener al ) {
      _listeners.add( al );
   }

   protected static void fireActionEvent( ActionEvent ae ) {
      if ( ae == null )
         return ;
      if ( ae.getSource() == null )
         return ;
      if ( ae.getActionCommand() == null || ae.getActionCommand().equals( "" ) )
         return ;
      Iterator it = _listeners.iterator();
      while ( it.hasNext() ) {
         ActionListener al = ( ActionListener ) it.next();
         al.actionPerformed( ae );
      }
   }

   /**
    * Exposed API for other plugins to run targets.
    *
    * @param buildFile the build file
    * @param target the target to run
    * @param view the view containing Antelope
    */
   public static void executeTarget( View view, File buildFile, String target ) {
      try {
         AntelopePluginPanel app = ( AntelopePluginPanel ) panelList.get( view );
         if ( app != null ) {
            AntelopePanel panel = app.getAntelopePanel();
            panel.createProject( buildFile );
            panel.executeTarget( target );
            return ;
         }

         DefaultErrorSource es = new DefaultErrorSource( AntelopePlugin.NAME );
         ErrorSource.registerErrorSource( es );
         AntelopePanel panel = new AntelopePanel( buildFile, null, false );
         panel.addLogHandler( new ConsolePluginHandler( view, es ) );
         panel.executeTarget( target );
      }
      catch ( Exception e ) {
         Log.log( Log.DEBUG, AntelopePlugin.class, "EXCEPTION: " + e.getMessage() );
         e.printStackTrace();
      }

   }


   public static void executeDefaultTarget( View view ) {
      try {
         AntelopePluginPanel app = ( AntelopePluginPanel ) panelList.get( view );
         if ( app != null ) {
            AntelopePanel panel = app.getAntelopePanel();
            panel.executeDefaultTarget();
            return ;
         }
      }
      catch ( Exception e ) {
         Log.log( Log.DEBUG, AntelopePlugin.class, "EXCEPTION: " + e.getMessage() );
         e.printStackTrace();
      }

   }



   /**
    * @return a list of targets found in the given build file.   
    */
   public static String[] getTargetList( File buildFile ) {
      Project project = new Project();
      project.init();
      ProjectHelper.configureProject( project, buildFile );
      Hashtable h = project.getTargets();
      String[] targets = new String[ h.size() ];
      Enumeration enum = h.keys();
      for ( int i = 0; i < targets.length; i++ ) {
         targets[ i ] = ( String ) enum.nextElement();
      }
      return targets;
   }

   /**
    * Cause Antelope to use the given build file.   
    */
   public static void setBuildFile( View view, File buildFile ) {
      try {
         AntelopePluginPanel app = ( AntelopePluginPanel ) panelList.get( view );
         if ( app != null ) {
            AntelopePanel panel = app.getAntelopePanel();
            panel.openBuildFile( buildFile );
            return ;
         }
         else {
            Log.log( Log.DEBUG, AntelopePlugin.class, "app is null, can't change build file" );
         }
      }
      catch ( Exception e ) {
         Log.log( Log.DEBUG, AntelopePlugin.class, "EXCEPTION: " + e.getMessage() );
         //e.printStackTrace();
      }
   }

   /**
    * @return the current build file   
    */
   public static File getBuildFile( View view ) {
      try {
         AntelopePluginPanel app = ( AntelopePluginPanel ) panelList.get( view );
         if ( app != null ) {
            return app.getBuildFile();
         }
         else {
            Log.log( Log.DEBUG, AntelopePlugin.class, "app is null, can't get build file" );
            return null;
         }
      }
      catch ( Exception e ) {
         Log.log( Log.DEBUG, AntelopePlugin.class, "EXCEPTION: " + e.getMessage() );
         //e.printStackTrace();
         return null;
      }
   }

   /**
    * Check for ant.jar and ANT_HOME in several places:<p>
    * 1. look for ant.jar in the classpath. If found, assume any other jars needed
    * for Ant are already in the classpath.<p>
    * 2. check Antelope's preferences for ANT_HOME<p>
    * 3. check for a System property named ANT_HOME<p>
    * 4. check for an environment variable named ANT_HOME<p>
    * 5. check if jEdit loaded ant.jar. This would happen if ant.jar is in the 
    * $user.home/.jedit/jars directory.<p>
    * 6. ask the user, then store their choice in Antelope's preferences<p>
    * Once found, return a String containing a list of the jars found in $ANT_HOME/lib,
    * one jar per line. Ant also includes all jars found in $user_home/.ant/lib,
    * so any jars found there are included also.
    * @return a path-separated list of jars from $ANT_HOME/lib and $user_home/.ant/lib.   
    */
   public static String getAntJars( boolean relative ) {
      // first check classpath, if in classpath, assume all jars necessary
      // for ant are in the classpath
      String classpath = System.getProperty( "java.class.path" );
      if ( classpath.toLowerCase().indexOf( "ant.jar" ) > -1 ) {
         // the initial check shows ant.jar is in the classpath, but need to make
         // sure it's really ant.jar and not elephant.jar
         StringTokenizer st = new StringTokenizer( classpath, File.pathSeparator );
         while ( st.hasMoreTokens() ) {
            String path = st.nextToken();
            File f = new File( path );
            if ( f.getName().toLowerCase().equals( "ant.jar" ) && f.exists() )
               return path;
         }
      }

      // next check stored settings, System and environment
      String ant_home = getAntHome();
      if ( ant_home == null ) {
         // check if ant.jar was loaded by jEdit
         String ant_jar = getAntJarLocation();
         if ( ant_jar != null ) {
            return ant_jar;
         }
         // we got nothing, so tell the user to pick a directory --
         // show a directory chooser and store the setting in preferences
         JFileChooser chooser = new JFileChooser();
         chooser.setFileSelectionMode( JFileChooser.DIRECTORIES_ONLY );
         chooser.setDialogTitle( "Select Ant home directory" );
         chooser.setAccessory( new JLabel( "<html>Antelope needs to know where Ant is installed.<p>Please choose the Ant installation directory." ) );
         int rtn = chooser.showOpenDialog( null );
         if ( rtn == JFileChooser.APPROVE_OPTION ) {
            File f = chooser.getSelectedFile();
            ant_home = f.getAbsolutePath();
            PREFS.put( ANT_HOME, ant_home );
         }
         else {
            JOptionPane.showMessageDialog( null, "No Ant, no Antelope. Sorry.", "Error", JOptionPane.ERROR_MESSAGE );
            return null;
         }
      }
      // got Ant home

      // put all jars from ANT_HOME into line-separated string
      File lib_dir = new File( ant_home, "lib" );
      File[] ant_jars = lib_dir.listFiles( new FileFilter() {
               public boolean accept( File name ) {
                  return name.getName().toLowerCase().endsWith( ".jar" );
               }
            }
                                         );
      if ( ant_jars != null ) {
         PluginJAR antelope_jar = getAntelopePluginJAR();
         String dir = MiscUtilities.getParentOfPath( antelope_jar.getPath() );
         StringBuffer sb = new StringBuffer();
         for ( int i = 0; i < ant_jars.length ; i++ ) {
            String path = convertPath( dir, ant_jars[ i ].getAbsolutePath() );
            if ( relative ) {
               sb.append( path ).append( "\n" );
            }
            else {
               // use MiscUtilities to avoid problems with C:\ vs. c:\, they mean
               // the same thing, but jEdit stores the names as strings in a Hashtable,
               // so case matters
               sb.append( MiscUtilities.constructPath( dir, path ) ).append( "\n" );
            }
         }

         // also check for jars in USER_HOME/.ant/lib
         lib_dir = new File( System.getProperty( "user.home" ) + ".ant", "lib" );
         ant_jars = lib_dir.listFiles( new FileFilter() {
                  public boolean accept( File name ) {
                     return name.getName().toLowerCase().endsWith( ".jar" );
                  }
               }
                                     );
         if ( ant_jars != null ) {
            for ( int i = 0; i < ant_jars.length ; i++ ) {
               dir = MiscUtilities.getParentOfPath( ant_jars[ i ].getPath() );
               String path = convertPath( dir, ant_jars[ i ].getAbsolutePath() );
               if ( relative ) {
                  sb.append( path ).append( "\n" );
               }
               else {
                  // use MiscUtilities to avoid problems with C:\ vs. c:\, they mean
                  // the same thing, but jEdit stores the names as strings in a Hashtable,
                  // so case matters
                  sb.append( MiscUtilities.constructPath( dir, path ) ).append( "\n" );
               }
            }
         }

         antJars = sb.toString();
         return antJars;
      }
      else {
         // this shouldn't happen
         JOptionPane.showMessageDialog( null, "No Ant, no Antelope. Sorry.", "Error", JOptionPane.ERROR_MESSAGE );
         return null;
      }
   }

   public static List getAntJarList() {
      return getAntJarList( false );
   }

   public static List getAntJarList( boolean relative ) {
      String jars = getAntJars( relative );
      List list = new ArrayList();
      StringTokenizer st = new StringTokenizer( jars, "\n" );
      while ( st.hasMoreTokens() ) {
         list.add( st.nextToken() );
      }
      return list;
   }

   /**
    * Find ant.jar if it has been loaded by jEdit.
    * XXX "ant.jar" is hard-coded, maybe that's okay, maybe not
    *
    * @return   The absolute path to ant.jar, may be null
    */
   public static String getAntJarLocation() {
      //EditPlugin.JAR[] jars = jEdit.getPluginJARs(); // use this line for jEdit 4.1
      PluginJAR[] jars = jEdit.getPluginJARs();       // use this line for jEdit 4.2

      for ( int i = 0; i < jars.length; i++ ) {
         if ( jars[ i ].getPath() != null ) {
            File f = new File( jars[ i ].getPath() );
            if ( f.getName().toLowerCase().equals( "ant.jar" ) ) {
               return f.getAbsolutePath();
            }
         }
      }
      return null;
   }
   public static File getAntelopeJar() {
      //EditPlugin.JAR[] jars = jEdit.getPluginJARs(); // use this line for jEdit 4.1
      PluginJAR[] jars = jEdit.getPluginJARs();       // use this line for jEdit 4.2

      for ( int i = 0; i < jars.length; i++ ) {
         if ( jars[ i ].getPath() != null ) {
            File f = new File( jars[ i ].getPath() );
            if ( f.getName().toLowerCase().equals( "antelope.jar" ) ) {
               return f;
            }
         }
      }
      return null;
   }

   public static PluginJAR getAntelopePluginJAR() {
      //EditPlugin.JAR[] jars = jEdit.getPluginJARs(); // use this line for jEdit 4.1
      PluginJAR[] jars = jEdit.getPluginJARs();       // use this line for jEdit 4.2

      for ( int i = 0; i < jars.length; i++ ) {
         if ( jars[ i ].getPath() != null ) {
            File f = new File( jars[ i ].getPath() );
            if ( f.getName().toLowerCase().equals( "antelope.jar" ) ) {
               return jars[ i ];
            }
         }
      }
      return null;
   }

   /**
    * Find the classloader that loaded Ant and return it.
    * XXX "ant.jar" is hard-coded, maybe that's okay, maybe not
    *
    * @return   The antClassLoader value, may be null
    */
   public static ClassLoader getAntClassLoader() {
      //EditPlugin.JAR[] jars = jEdit.getPluginJARs(); // use this line for jEdit 4.1
      PluginJAR[] jars = jEdit.getPluginJARs();       // use this line for jEdit 4.2

      ClassLoader cl = null;
      for ( int i = 0; i < jars.length; i++ ) {
         if ( jars[ i ].getPath() != null ) {
            File f = new File( jars[ i ].getPath() );
            if ( f.getName().equals( "ant.jar" ) ) {
               return jars[ i ].getClassLoader();
            }
         }
      }
      return cl;
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
   protected static String getAntHome() {
      String ant_home = null;
      try {
         // third, check stored settings
         ant_home = PREFS.get( ANT_HOME, null );
         if ( ant_home != null ) {
            File ant_dir = new File( ant_home );
            if ( ant_dir.exists() ) {
               return ant_home;
            }
         }
         // first, check System properties
         ant_home = System.getProperty( "ANT_HOME" );
         if ( ant_home != null ) {
            File ant_dir = new File( ant_home );
            if ( ant_dir.exists() ) {
               PREFS.put( ANT_HOME, ant_home );
               return ant_home;
            }
         }

         // second, check environment
         ant_home = Os.getEnvironmentValue( "ANT_HOME" );
         if ( ant_home != null ) {
            File ant_dir = new File( ant_home );
            if ( ant_dir.exists() ) {
               PREFS.put( ANT_HOME, ant_home );
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
    * Copied from org.apache.tools.ant.taskdefs.Execute. It's a private method in
    * that class, so I can't access it directly.
    */
   private static String[] getProcEnvCommand() {
      if ( Os.isFamily( "os/2" ) ) {
         // OS/2 - use same mechanism as Windows 2000
         String[] cmd = {"cmd", "/c", "set" };
         return cmd;
      }
      else if ( Os.isFamily( "windows" ) ) {
         // Determine if we're running under XP/2000/NT or 98/95
         if ( !Os.isFamily( "win9x" ) ) {
            // Windows XP/2000/NT
            String[] cmd = {"cmd", "/c", "set" };
            return cmd;
         }
         else {
            // Windows 98/95
            String[] cmd = {"command.com", "/c", "set" };
            return cmd;
         }
      }
      else if ( Os.isFamily( "z/os" ) || Os.isFamily( "unix" ) ) {
         // On most systems one could use: /bin/sh -c env

         // Some systems have /bin/env, others /usr/bin/env, just try
         String[] cmd = new String[ 1 ];
         if ( new File( "/bin/env" ).canRead() ) {
            cmd[ 0 ] = "/bin/env";
         }
         else if ( new File( "/usr/bin/env" ).canRead() ) {
            cmd[ 0 ] = "/usr/bin/env";
         }
         else {
            // rely on PATH
            cmd[ 0 ] = "env";
         }
         return cmd;
      }
      else if ( Os.isFamily( "netware" ) || Os.isFamily( "os/400" ) ) {
         // rely on PATH
         String[] cmd = {"env"};
         return cmd;
      }
      else if ( Os.isFamily( "openvms" ) ) {
         String[] cmd = {"show", "logical"};
         return cmd;
      }
      else {
         // MAC OS 9 and previous
         //TODO: I have no idea how to get it, someone must fix it
         String[] cmd = null;
         return cmd;
      }
   }

   private static String convertPath( File base_dir, File destination ) {
      return convertPath( base_dir.getAbsolutePath(), destination.getAbsolutePath() );
   }

   /**
    * Returns a relative path from base_dir to destination. base_dir may represent
    * a file rather than a directory.
    * @param base_dir where to start, may be a file or directory
    * @param destination where to find a relative path to
    * @return a relative path from base_dir to destination
    */
   private static String convertPath( String base_dir, String destination ) {
      StringBuffer sb = new StringBuffer();
      File bd = new File( base_dir );
      if ( !bd.isDirectory() )
         base_dir = bd.getParentFile().getAbsolutePath();
      StringTokenizer st1 = new StringTokenizer( base_dir, File.separator );
      StringTokenizer st2 = new StringTokenizer( destination, File.separator );
      String token1, token2 = "";
      while ( st1.hasMoreTokens() ) {
         token1 = st1.nextToken();
         token2 = st2.nextToken();
         if ( token1.equalsIgnoreCase( token2 ) ) {
            continue;
         }
         else {
            sb.append( ".." ).append( File.separator );
            break;
         }
      }
      while ( st1.hasMoreTokens() ) {
         sb.append( ".." ).append( File.separator );
         st1.nextToken();
      }
      sb.append( token2 );
      if ( st2.hasMoreTokens() )
         sb.append( File.separator );
      while ( st2.hasMoreTokens() ) {
         sb.append( st2.nextToken() );
         if ( st2.hasMoreTokens() )
            sb.append( File.separator );
      }
      return sb.toString();
   }


}

