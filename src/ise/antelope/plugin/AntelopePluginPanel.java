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


import java.awt.Dimension;
import java.awt.Point;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.io.*;
import java.util.*;
import java.util.prefs.Preferences;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.*;
import org.gjt.sp.util.Log;
import org.gjt.sp.jedit.jEdit;
import org.gjt.sp.jedit.View;
import org.gjt.sp.jedit.EditPlugin;
import org.gjt.sp.jedit.PluginJAR;
import org.gjt.sp.jedit.gui.DockableWindowManager;
import org.gjt.sp.jedit.io.VFSManager;

import console.*;
import errorlist.*;

import ise.antelope.common.*;

/**
 * This is the panel displayed and manipulated by jEdit. It wraps the
 * AntelopePanel, which actually takes care of displaying something on the
 * screen, and acts as  the interface between jEdit and AntelopePanel.
 *
 * @version   $Revision$
 */
public class AntelopePluginPanel extends JPanel implements Constants, CommonHelper {

   private View _view = null;
   private AntelopePanel antelopePanel = null;
   private ConsolePluginHandler _console_handler = null;
   private AntelopeShell _shell = null;
   private DefaultErrorSource _error_source = null;

   /**
    * Constructor for the AntelopePluginPanel. Based on the position parameter,
    * the preferred size may or may not be set.
    *
    * Initially, this shows a "Loading Ant..." message. The actual loading of
    * Ant happens in the Plugin class and in the <code>init()</code> method. Once 
    * Ant is loaded, the AntelopePanel is shown.
    *
    * @param position  this is passed in by the DockableWindowManager, it will
    * be one of DockableWindowManager.FLOATING, TOP, LEFT, RIGHT, or BOTTOM.
    * @param view      this is passed in by the DockableWindowManager.
    */
   public AntelopePluginPanel( View view, String position ) {
      super( new java.awt.BorderLayout() );
      setBackground( java.awt.Color.WHITE );
      _view = view;
      JLabel label = new JLabel( "<html><center><b><i>Please wait,<p>Loading Ant...</i></b></center></html>", SwingConstants.CENTER );
      add( label );

      if ( position.equals( DockableWindowManager.FLOATING ) ) {
         setPreferredSize( new Dimension( 150, 300 ) );
      }
      init();
   }

   public void init() {
      SwingUtilities.invokeLater( new Runnable() {
               public void run() {
                  try {
                     // load ant
                     jEdit.resetProperty( "plugin.ise.antelope.plugin.AntelopePlugin.jars" );
                     String ant_jars = AntelopePlugin.getAntJars( true );
                     if ( ant_jars != null ) {
                        jEdit.setProperty( "plugin.ise.antelope.plugin.AntelopePlugin.jars", ant_jars );
                        StringTokenizer st = new StringTokenizer( AntelopePlugin.getAntJars( false ) );
                        while ( st.hasMoreTokens() ) {
                           String token = st.nextToken();
                           jEdit.addPluginJAR( token );
                        }
                        AntelopePlugin.getAntelopePluginJAR().checkDependencies();
                        _view.getStatus().setMessageAndClear( "Antelope finished loading Ant." );
                     }
                  }
                  catch ( Exception e ) {
                     // start Antelope anyway
                     /// really? what good is antelope without ant???
                     jEdit.resetProperty("plugin.ise.antelope.plugin.AntelopePlugin.jars");
                     int rtn = JOptionPane.showConfirmDialog(AntelopePluginPanel.this, "<html>Error loading Ant:<p>" + e.getMessage() + "<p>Do you want to try again?", "Error Loading Ant", JOptionPane.YES_NO_OPTION);
                     if (rtn == JOptionPane.YES_OPTION) {
                        run();
                     }
                  }

                  // set up Antelope's menu
                  System.out.println( "load antelope" );
                  _view.getStatus().setMessageAndClear( "Loading Antelope..." );
                  JMenuItem mi = new JMenuItem( "Open Current Buffer" );
                  mi.addActionListener( new ActionListener() {
                           public void actionPerformed( ActionEvent ae ) {
                              File f = getView().getBuffer().getFile();
                              antelopePanel.openBuildFile( f );
                           }
                        }
                                      );
                  ArrayList menu_items = new ArrayList();
                  menu_items.add( mi );
                  System.out.println( "added menu items" );

                  // get the last open file
                  String name = Constants.PREFS.get( LAST_OPEN_FILE, null );
                  File file = null;
                  if ( name != null ) {
                     file = new File( name );
                  }

                  // create and add Antelope
                  try {
                     antelopePanel = new AntelopePanel( file, AntelopePluginPanel.this, true, menu_items );
                  }
                  catch ( Throwable t ) {
                     t.printStackTrace();
                     JOptionPane.showMessageDialog(null, "<html>Error starting Antelope:<p>" +
                        t.getMessage() + "<p>Usually this can be fixed by restarting jEdit.",
                        "Error", JOptionPane.ERROR_MESSAGE);
                     return;
                  }
                  removeAll();
                  setBackground( java.awt.Color.WHITE );
                  add( antelopePanel );
                  AntelopePlugin.addPanel( _view, AntelopePluginPanel.this );

                  // set up the error source
                  _error_source = new DefaultErrorSource( AntelopePlugin.NAME );
                  ErrorSource.registerErrorSource( _error_source );

                  // set up the logger
                  _console_handler = new ConsolePluginHandler( AntelopePluginPanel.this );
                  antelopePanel.addLogHandler( _console_handler );
                  _view.getStatus().setMessageAndClear( "Antelope loaded." );
               }
            }
                                );
   }

   /**
    * Gets the view attribute of the AntelopePluginPanel class
    *
    * @return   The view value
    */
   public View getView() {
      return _view;
   }

   public DefaultErrorSource getErrorSource() {
      return _error_source;
   }

   /**
    * Is Antelope being used as a jEdit plugin?
    *
    * @return   true if so.
    */
   public boolean isPlugin() {
      return getView() != null;
   }

   /**
    * Gets the buildFile attribute of the AntelopePluginPanel class
    *
    * @return   The buildFile value
    */
   public File getBuildFile() {
      return antelopePanel.getBuildFile();
   }

   public AntelopePanel getAntelopePanel() {
      return antelopePanel;
   }

   /**
    * Forces a reload of the current build file.
    */
   public void reload() {
      antelopePanel.reload();
   }

   /**
    * Opens the given file in jEdit.
    *
    * @param f  Description of Parameter
    */
   public void openFile( File f ) {
      if ( f == null )
         return ;
      if ( f.isDirectory() )
         return ;
      if ( getView().getBuffer().getPath().equals( f.getAbsolutePath() ) )
         return ;
      jEdit.openFile( getView(), f.getAbsolutePath() );
   }

   /**
    * Save all jEdit buffers.
    */
   public void saveBuffers( ) {
      if ( getView() == null )
         return ;
      jEdit.saveAllBuffers( getView(), false );
      VFSManager.waitForRequests();
   }

   /**
    * Cleans up system resources for the AntelopePanel -- removes all loggers,
    * flushes preferences. Only call this at system exit.
    */
   public void close() {
      antelopePanel.close();
   }

   /**
    * Description of the Method
    *
    * @return   Description of the Returned Value
    */
   public boolean useErrorParsing() {
      return antelopePanel.getUseErrorParsing();
   }

   /**
    * Sets the targetExecutionThread attribute of the AntelopePluginPanel object
    *
    * @param thread  The new targetExecutionThread value
    */
   public void setTargetExecutionThread( Thread thread ) {
      _console_handler.getShell().setRunner( thread );
   }

   public void updateGUI() {}

   /**
    * Description of the Method
    *
    * @return   Description of the Returned Value
    */
   public boolean canSaveBeforeRun() {
      return true;
   }

   /**
    * Description of the Method
    */
   public void saveBeforeRun() {
      saveBuffers();
   }

   /**
    * Description of the Method
    */
   public void clearErrorSource() {
      _error_source.clear();
   }

   /**
    * Description of the Method
    *
    * @return   Description of the Returned Value
    */
   public boolean canShowEditButton() {
      return true;
   }

   /**
    * Gets the editButtonAction attribute of the AntelopePluginPanel object
    *
    * @return   The editButtonAction value
    */
   public ActionListener getEditButtonAction() {
      return
         new ActionListener() {
            public void actionPerformed( ActionEvent ae ) {
               File build_file = getBuildFile();
               if ( build_file == null ) {
                  JOptionPane.showMessageDialog( antelopePanel, "No build file is selected.", "Error", JOptionPane.ERROR_MESSAGE );
                  return ;
               }
               if ( !build_file.exists() ) {
                  JOptionPane.showMessageDialog( antelopePanel, "<html>Build file<p>" + build_file.toString() + "<p>doesn't exist.", "Error", JOptionPane.ERROR_MESSAGE );
                  return ;
               }
               openFile( build_file );
            }
         };
   }

   public ActionListener getRunButtonAction() {
      return null;
   }

   /**
    * Find the classloader that loaded Ant and return it.
    * XXX "ant.jar" is hard-coded, maybe that's okay, maybe not
    *
    * @return   The antClassLoader value, may be null
    */
   public ClassLoader getAntClassLoader() {
      return ise.antelope.plugin.AntelopePlugin.getAntClassLoader();
   }

   public String getAntJarLocation() {
      return ise.antelope.plugin.AntelopePlugin.getAntJarLocation();
   }

   public java.util.List getAntJarList() {
      return ise.antelope.plugin.AntelopePlugin.getAntJarList();
   }

   /**
    * On an EDIT_EVENT, scrolls the view of the build file to the target passed
    * as the action command if it actually exists in the file.
    *
    * @param ae  an action event
    */
   public void actionPerformed( ActionEvent ae ) {
      switch ( ae.getID() ) {
         case EDIT_EVENT:
            if ( ae.getSource() instanceof Point ) {
               try {
                  Point p = ( Point ) ae.getSource();
                  final int offset = getView().getTextArea().getLineStartOffset( p.x - 1 );
                  SwingUtilities.invokeLater(
                     new Runnable() {
                        public void run() {
                           getView().getTextArea().requestFocus();
                           getView().getTextArea().setCaretPosition( offset, true );
                           getView().getTextArea().scrollToCaret( true );
                        }
                     }
                  );
               }
               catch ( Exception e ) {
                  // ignore this
               }
            }
            else {
               try {
                  String target = ae.getActionCommand();
                  String doc = getView().getTextArea().getText();
                  Pattern pattern = Pattern.compile( "(<target)(.+?)(>)", Pattern.DOTALL );
                  Matcher matcher = pattern.matcher( doc );
                  while ( matcher.find() ) {
                     final int start = matcher.start();
                     int end = matcher.end();
                     String target_line = doc.substring( start, end );
                     if ( target_line.indexOf( "name=\"" + target + "\"" ) > 0 ) {
                        SwingUtilities.invokeLater(
                           new Runnable() {
                              public void run() {
                                 getView().getTextArea().requestFocus();
                                 getView().getTextArea().setCaretPosition( start, true );
                                 getView().getTextArea().scrollToCaret( true );
                              }
                           }
                        );
                        break;
                     }
                  }
               }
               catch ( Exception e ) {
                  e.printStackTrace();
               }
            }
            break;
         default:
      }
   }
}

