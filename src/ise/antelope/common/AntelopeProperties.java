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
package ise.antelope.common;

import ise.library.*;
import ise.library.GUIUtils;

import java.awt.*;
import java.awt.event.*;

import java.io.*;

import java.util.*;
import java.util.prefs.*;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.*;
import javax.swing.table.*;

import org.apache.tools.ant.*;


/**
 * A panel to display Ant properties.
 *
 * @author    Dale Anson
 * @version   $Revision$
 */
public class AntelopeProperties {
   /**
    * Description of the Field
    */
   private AntelopePanel _parent;
   /**
    * Description of the Field
    */
   private HashMap _property_tfs;
   /**
    * Description of the Field
    */
   private File _build_file;

   // tab titles
   /**
    * Description of the Field
    */
   private final String PROPS = "Properties";
   /**
    * Description of the Field
    */
   private final String REFS = "References";
   /**
    * Description of the Field
    */
   private final String USER = "User";

   private final String DESC = "Description";
   
   private final String ABOUT = "About";

   private final String ANTHOME = "Ant Home";

   private String lSep = System.getProperty( "line.separator" );

   private String antelopeVersion = null;

   /**
    * Description of the Field
    */
   private JButton _new_btn = null;

   /**
    * Creates a new AntelopeProperties object.
    *
    * @param parent  an AntelopePanel
    */
   public AntelopeProperties( AntelopePanel parent ) {
      _parent = parent;
   }

   /**
    * Shows a dialog containing the properties for the given project.
    *
    * @param project  An Ant Project.
    */
   public void showProperties( final AntProject project ) {

      // project properties
      TreeMap map = new TreeMap( project.getProperties() );   // TreeMap sorts.
      JTable props_table = new JTable();
      DefaultTableModel model = new DefaultTableModel(
               new String[] {
                  "Name", "Value"
               }, map.size() );
      props_table.setModel( model );
      Iterator it = map.keySet().iterator();
      for ( int i = 0; i < map.size(); i++ ) {
         try {
            String key = ( String ) it.next();
            String value = ( String ) map.get( key );
            model.setValueAt( key, i, 0 );
            model.setValueAt( value, i, 1 );
         }
         catch ( Exception ignored ) {
            // I wouldn't think an exception would ever be thrown, but occasionally
            // one is.
         }
      }
      props_table.addMouseListener( new TableCellViewer( props_table ) );

      // project references
      map = new TreeMap( project.getReferences() );
      JTable ref_table = new JTable();
      model = new DefaultTableModel( new String[] {"Name", "Value"}, map.size() );
      ref_table.setModel( model );
      it = map.keySet().iterator();
      for ( int i = 0; i < map.size(); i++ ) {
         try {
            String key = it.next().toString();
            String value = map.get( key ).toString();
            model.setValueAt( key, i, 0 );
            model.setValueAt( value, i, 1 );
         }
         catch ( Exception ignored ) {
            // bad key/value mapping will throw NPE, ignore
         }
      }
      ref_table.addMouseListener( new TableCellViewer( ref_table ) );

      // user (mutable) properties
      final Hashtable user_props = project.getUserProperties();
      map = new TreeMap( user_props );
      JTable user_table = new JTable();
      final DefaultTableModel user_model = new DefaultTableModel(
               new String[] {"Name", "Value"}, map.size() );
      user_table.setModel( user_model );
      it = map.keySet().iterator();
      for ( int i = 0; i < map.size(); i++ ) {
         try {
            String key = it.next().toString();
            String value = map.get( key ).toString();
            user_model.setValueAt( key, i, 0 );
            user_model.setValueAt( value, i, 1 );
         }
         catch ( Exception ignored ) {
            // bad key/value mapping will throw NPE, ignore
         }
      }
      user_table.addMouseListener( new TableCellViewer( user_table ) );

      // project description
      StringBuffer sb = new StringBuffer();
      sb.append( getProjectDescription( project ) ).append( lSep );
      sb.append( printTargets( project, true ) );
      JEditorPane desc = new JEditorPane( "text/html", "<html><pre>" + sb.toString() );
      desc.setBackground( Color.white );
      desc.setEditable( false );

      // about Antelope
      JEditorPane about = new JEditorPane( );
      try {
         java.net.URL url = getClass().getClassLoader().getResource("about.html" );
         about.setPage(url);  
      }
      catch(IOException ioe) {
         ioe.printStackTrace();
         about.setContentType("text/html");
         about.setText(getAntelopeVersion());
      }
      about.setBackground( Color.white );
      about.setEditable( false );

      
      // show the dialog
      String project_name = project.getName();
      if ( project_name == null )
         project_name = project.getProperty( "ant.project.name" );
      if ( project_name == null )
         project_name = "Ant Properties";
      else
         project_name = "Ant Properties: " + project_name;
      final JDialog dialog = new JDialog( GUIUtils.getRootJFrame( _parent ),
            project_name, false );
      JPanel panel = new JPanel( new BorderLayout() );
      panel.setBorder( new javax.swing.border.EmptyBorder( 6, 6, 6, 6 ) );
      JTabbedPane tabs = new JTabbedPane();
      tabs.addChangeListener(
         new ChangeListener() {
            public void stateChanged( ChangeEvent ce ) {
               JTabbedPane tp = ( JTabbedPane ) ce.getSource();
               int index = tp.getSelectedIndex();
               if ( index < 0 || _new_btn == null )
                  return ;
               _new_btn.setEnabled( USER.equals( tp.getTitleAt( index ) ) );
            }
         }
      );
      panel.add( tabs, BorderLayout.CENTER );
      tabs.add( PROPS, new JScrollPane( props_table ) );
      tabs.add( REFS, new JScrollPane( ref_table ) );
      tabs.add( USER, new JScrollPane( user_table ) );
      tabs.add( DESC, new JScrollPane( desc ) );
      tabs.add( ABOUT, new JScrollPane(about));

      KappaLayout kl = new KappaLayout();
      JPanel btn_panel = new JPanel( kl );
      btn_panel.setBorder( new javax.swing.border.EmptyBorder( 11, 0, 0, 0 ) );
      JButton ok_btn = new JButton( "Ok" );
      ok_btn.addActionListener(
         new ActionListener() {
            public void actionPerformed( ActionEvent ae ) {
               Preferences user_prefs = _parent.getPrefs().node( Constants.ANT_USER_PROPS );
               for ( int row = 0; row < user_model.getRowCount(); row++ ) {
                  String key = ( String ) user_model.getValueAt( row, 0 );
                  String value = ( String ) user_model.getValueAt( row, 1 );
                  project.setUserProperty( key, value );
                  if ( value == null || value.equals( "" ) )
                     user_prefs.remove( key );
                  else
                     user_prefs.put( key, value );
               }
               _parent.reload();
               dialog.hide();
               dialog.dispose();
               return ;
            }
         }
      );

      JButton cancel_btn = new JButton( "Cancel" );
      cancel_btn.addActionListener(
         new ActionListener() {
            public void actionPerformed( ActionEvent ae ) {
               dialog.hide();
               dialog.dispose();
               return ;
            }
         }
      );
      _new_btn = new JButton( "New" );
      _new_btn.setEnabled( false );
      _new_btn.addActionListener(
         new ActionListener() {
            public void actionPerformed( ActionEvent ae ) {
               user_model.addRow( new String[] {"", ""} );
            }
         }
      );

      btn_panel.add( ok_btn, "0, 0, 1, 1, 0, w, 3" );
      btn_panel.add( cancel_btn, "1, 0, 1, 1, 0, w, 3" );
      btn_panel.add( _new_btn, "2, 0, 1, 1, 0, w, 3" );
      kl.makeColumnsSameWidth( new int[] {0, 1, 2} );
      JPanel p = new JPanel();
      p.add( btn_panel );
      panel.add( p, BorderLayout.SOUTH );
      dialog.setContentPane( panel );
      dialog.pack();
      SwingUtilities.getRootPane( ok_btn ).setDefaultButton( ok_btn );
      Dimension d = dialog.getSize();
      dialog.setSize( d.width, Math.min( 400, d.height ) );
      GUIUtils.centerOnScreen( dialog );
      dialog.setVisible( true );
   }

   /**
    * Borrowed from Ant's Main class.   
    */
   public String getAntelopeVersion() throws BuildException {
      if ( antelopeVersion == null ) {
         try {
            Properties props = new Properties();
            InputStream in = getClass().getResourceAsStream( "/ise/antelope/common/version.txt" );
            props.load( in );
            in.close();

            StringBuffer msg = new StringBuffer();
            msg.append( "Antelope Version: " );
            msg.append( props.getProperty( "VERSION" ) );
            msg.append( ", compiled on " );
            msg.append( props.getProperty( "DATE" ) );
            antelopeVersion = msg.toString();
         }
         catch ( Exception ioe ) {
            antelopeVersion = "3.x.x";
         }
      }
      return antelopeVersion;
   }

   private String getProjectDescription( Project p ) {
      StringBuffer sb = new StringBuffer();
      String ant_version = Main.getAntVersion().trim() + lSep;
      sb.append( ant_version == null ? "" : "Ant Version: " + ant_version + lSep );
      String ant_home = Constants.PREFS.get( Constants.ANT_HOME, null );
      if ( ant_home != null )
         sb.append( "Ant Home: " + ant_home + lSep + lSep );
      sb.append( getAntelopeVersion() + lSep + lSep );
      sb.append( p.getDescription() == null ? "" : "Project Description: " + lSep + " " + p.getDescription() + lSep );
      return sb.toString();
   }

   /**
    * Prints a list of all targets in the specified project to
    * <code>System.out</code>, optionally including subtargets. Swiped 
    * from Ant's Main class.
    *
    * @param project The project to display a description of.
    *                Must not be <code>null</code>.
    * @param printSubTargets Whether or not subtarget names should also be
    *                        printed.
    */
   private String printTargets( Project project, boolean printSubTargets ) {

      StringBuffer sb = new StringBuffer();

      // find the target with the longest name
      int maxLength = 0;
      Enumeration ptargets = project.getTargets().elements();
      String targetName;
      String targetDescription;
      Target currentTarget;
      // split the targets in top-level and sub-targets depending
      // on the presence of a description
      Vector topNames = new Vector();
      Vector topDescriptions = new Vector();
      Vector subNames = new Vector();

      while ( ptargets.hasMoreElements() ) {
         currentTarget = ( Target ) ptargets.nextElement();
         targetName = currentTarget.getName();
         targetDescription = currentTarget.getDescription();
         // maintain a sorted list of targets
         if ( targetDescription == null ) {
            int pos = findTargetPosition( subNames, targetName );
            subNames.insertElementAt( targetName, pos );
         }
         else {
            int pos = findTargetPosition( topNames, targetName );
            topNames.insertElementAt( targetName, pos );
            topDescriptions.insertElementAt( targetDescription, pos );
            if ( targetName.length() > maxLength ) {
               maxLength = targetName.length();
            }
         }
      }

      sb.append( printTargets( project, topNames, topDescriptions,
            "Main targets:", maxLength ) ).append( lSep );
      //if there were no main targets, we list all subtargets
      //as it means nothing has a description
      if ( topNames.size() == 0 ) {
         printSubTargets = true;
      }
      if ( printSubTargets ) {
         sb.append( printTargets( project, subNames, null, "Subtargets:", 0 ) ).append( lSep );
      }

      String defaultTarget = project.getDefaultTarget();
      if ( defaultTarget != null && !"".equals( defaultTarget ) ) {
         // shouldn't need to check but...
         sb.append( "Default target: " ).append( defaultTarget ).append( lSep );
      }
      return sb.toString();
   }

   /**
    * Writes a formatted list of target names to <code>System.out</code>
    * with an optional description. Also swiped from Ant's Main class.
    *
    * @param names The names to be printed.
    *              Must not be <code>null</code>.
    * @param descriptions The associated target descriptions.
    *                     May be <code>null</code>, in which case
    *                     no descriptions are displayed.
    *                     If non-<code>null</code>, this should have
    *                     as many elements as <code>names</code>.
    * @param heading The heading to display.
    *                Should not be <code>null</code>.
    * @param maxlen The maximum length of the names of the targets.
    *               If descriptions are given, they are padded to this
    *               position so they line up (so long as the names really
    *               <i>are</i> shorter than this).
    */
   private String printTargets( Project project, Vector names,
         Vector descriptions, String heading,
         int maxlen ) {
      // now, start printing the targets and their descriptions
      // got a bit annoyed that I couldn't find a pad function
      String spaces = "    ";
      while ( spaces.length() < maxlen ) {
         spaces += spaces;
      }
      StringBuffer msg = new StringBuffer();
      msg.append( heading + lSep + lSep );
      for ( int i = 0; i < names.size(); i++ ) {
         msg.append( " " );
         msg.append( names.elementAt( i ) );
         if ( descriptions != null ) {
            msg.append( spaces.substring( 0, maxlen - ( ( String ) names.elementAt( i ) ).length() + 2 ) );
            msg.append( descriptions.elementAt( i ) );
         }
         msg.append( lSep );
      }
      return msg.toString();
   }

   /**
    * Searches for the correct place to insert a name into a list so as
    * to keep the list sorted alphabetically. Also swiped from Ant's Main class.
    *
    * @param names The current list of names. Must not be <code>null</code>.
    * @param name  The name to find a place for.
    *              Must not be <code>null</code>.
    *
    * @return the correct place in the list for the given name
    */
   private static int findTargetPosition( Vector names, String name ) {
      int res = names.size();
      for ( int i = 0; i < names.size() && res == names.size(); i++ ) {
         if ( name.compareTo( ( String ) names.elementAt( i ) ) < 0 ) {
            res = i;
         }
      }
      return res;
   }

   /**
    * Grabs the string value of the contents of a table cell and shows it
    * in a popup.
    */
   public class TableCellViewer extends MouseAdapter {
      private JTable table = null;
      private JTextArea ta;
      private JPopupMenu pm;
      public TableCellViewer( JTable table ) {
         this.table = table;
         ta = new JTextArea( 10, 40 );
         ta.setLineWrap( true );
         ta.setEditable( false );
         pm = new JPopupMenu();
         pm.add( new JScrollPane( ta ) );
      }
      public void mousePressed( MouseEvent me ) {
         doPopup(me);
      }
      public void mouseReleased(MouseEvent me) {
         doPopup(me);
      }
      private void doPopup(MouseEvent me) {
         if ( me.isPopupTrigger() ) {
            Point p = me.getPoint();
            int col = table.columnAtPoint( p );
            int row = table.rowAtPoint( p );
            Object value = table.getModel().getValueAt( row, col );
            if ( value != null ) {
               ta.setText( value.toString() );
               GUIUtils.showPopupMenu( pm, table, me.getX(), me.getY() );
            }
         }
      }
   }

}

