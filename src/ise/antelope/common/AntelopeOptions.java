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

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.*;
import javax.swing.*;
import javax.swing.event.*;

import ise.library.*;
import org.apache.tools.ant.*;

/**
 * Panel to set options for Ant.
 *
 * @author    Dale Anson, danson@germane-software.com
 * @version   $Revision$
 */
public class AntelopeOptions extends JPanel implements Constants {

   /**
    * Description of the Field
    */
   private CommonHelper _helper;

   private static OptionSettings settings;

   // components
   /**
    * Description of the Field
    */
   private JLabel build_file;
   /**
    * Description of the Field
    */
   private JCheckBox cb_auto_reload, cba, cba2, cba3, cb00, cb0, cb1, cb1a, cb1b, cb2, cb3, cb4, cb5, cb6, cb7;
   /**
    * Description of the Field
    */
   private LevelRadioButton lrb0, lrb1, lrb2, lrb3, lrb4;

   private static HashMap _dialogs = new HashMap();
   private static HashMap _options = new HashMap();

   public static AntelopeOptions showDialog( AntelopePanel p, CommonHelper helper ) {
      JDialog d = (JDialog)_dialogs.get(p);
      if ( d == null ) {
         final AntelopePanel parent = p;
         AntelopeOptions options = new AntelopeOptions( parent, helper );

         JFrame f = GUIUtils.getRootJFrame( parent );
         final JDialog dialog = new JDialog( ( JFrame ) f, "Antelope Options", false );
         _dialogs.put(p, dialog);
         _options.put(dialog, options);
         d = dialog;
         JPanel pane = new JPanel( new KappaLayout() );
         dialog.setContentPane( pane );

         pane.add( options, "0,0,1,1" );

         JButton ok_btn = new JButton( "OK" );
         ok_btn.addActionListener( new ActionListener() {
                  public void actionPerformed( ActionEvent ae ) {
                     dialog.setVisible( false );
                     dialog.dispose();
                     settings.save();
                     parent.reload();
                  }
               }
                                 );
         JButton apply_btn = new JButton( "Apply" );
         apply_btn.addActionListener( new ActionListener() {
                  public void actionPerformed( ActionEvent ae ) {
                     settings.save();
                     parent.reload();
                  }
               }
                                    );
         JButton cancel_btn = new JButton( "Cancel" );
         cancel_btn.addActionListener( new ActionListener() {
                  public void actionPerformed( ActionEvent ae ) {
                     dialog.setVisible( false );
                     dialog.dispose();
                  }
               }
                                     );
         KappaLayout kl = new KappaLayout();
         JPanel btn_panel = new JPanel( kl );
         btn_panel.add( ok_btn, "0, 0, 1, 1, 0, w, 3" );
         btn_panel.add( apply_btn, "1, 0, 1, 1, 0, w, 3" );
         btn_panel.add( cancel_btn, "2, 0, 1, 1, 0, w, 3" );
         kl.makeColumnsSameWidth();
         pane.add( btn_panel, "0, 1, 1, 1, 1, 0, 0, 5" );
         dialog.pack();
         if ( f != null ) {
            GUIUtils.center( f, dialog );
         }
         else {
            GUIUtils.centerOnScreen( dialog );
         }
      }
      d.setVisible( true );

      return (AntelopeOptions)_options.get(d);
   }

   /**
    *Constructor for the AntelopeOptions object
    *
    * @param parent  Description of Parameter
    * @param helper
    */
   private AntelopeOptions( AntelopePanel p, CommonHelper helper ) {
      final AntelopePanel parent = p;
      _helper = helper;

      settings = new OptionSettings( parent.getBuildFile() );

      setLayout(new BorderLayout());      
      setBorder( new javax.swing.border.EmptyBorder( 6, 6, 6, 6 ) );
      JTabbedPane tabs = new JTabbedPane();
      add(tabs, BorderLayout.CENTER);

      KappaLayout.Constraints con = KappaLayout.createConstraint();
      con.a = KappaLayout.W;
      con.s = "w";

      // General tab
      JPanel general_panel = new JPanel(new KappaLayout());
      general_panel.setPreferredSize(new Dimension(400, 250));
      general_panel.setBorder( new javax.swing.border.EmptyBorder( 6, 6, 6, 6 ) );
      
      // only if being used as a plugin
      /// I'm overusing canSaveBeforeRun, need a better way of telling if
      /// this is a plugin or not
      if ( _helper != null && _helper.canSaveBeforeRun() ) {
         cba = new JCheckBox( "Save all files before running targets" );
         cba.setSelected( settings.getSaveBeforeRun() );
         cba.setToolTipText( "<html>If checked, all files will be saved<br>before running any target." );
         cba.addActionListener(
            new ActionListener() {
               public void actionPerformed( ActionEvent ae ) {
                  JCheckBox b = ( JCheckBox ) ae.getSource();
                  settings.setSaveBeforeRun( b.isSelected() );
               }
            }
         );
         ++con.y;
         general_panel.add( cba, con );
         cba2 = new JCheckBox( "Use error parsing" );
         cba2.setSelected( settings.getUseErrorParsing() );
         cba2.setToolTipText( "<html>If checked, errors generated during target execution\nwill be displayed in the Error List." );
         cba2.addActionListener(
            new ActionListener() {
               public void actionPerformed( ActionEvent ae ) {
                  JCheckBox b = ( JCheckBox ) ae.getSource();
                  settings.setUseErrorParsing( b.isSelected() );
               }
            }
         );
         ++con.y;
         general_panel.add( cba2, con );
      }

      cba3 = new JCheckBox( "Show performance statistics" );
      cba3.setSelected( settings.getShowPerformanceOutput() );
      cba3.setToolTipText( "<html>If checked, execution time for each task and\ntarget will be displayed following the build output." );
      cba3.addActionListener(
         new ActionListener() {
            public void actionPerformed( ActionEvent ae ) {
               JCheckBox b = ( JCheckBox ) ae.getSource();
               settings.setShowPerformanceOutput( b.isSelected() );
            }
         }
      );
      ++con.y;
      general_panel.add( cba3, con );

      cb_auto_reload = new JCheckBox( "Automatically reload build file" );
      cb_auto_reload.setSelected( settings.getAutoReload() );
      cb_auto_reload.setToolTipText( "<html>If checked, the build file will be reloaded\nprior to running any targets." );
      cb_auto_reload.addActionListener(
         new ActionListener() {
            public void actionPerformed( ActionEvent ae ) {
               JCheckBox b = ( JCheckBox ) ae.getSource();
               settings.setAutoReload( b.isSelected() );
            }
         }
      );
      ++con.y;
      general_panel.add( cb_auto_reload, con );

      // Target panel
      con.y = 0;
      JPanel target_panel = new JPanel(new KappaLayout());
      target_panel.setBorder( new javax.swing.border.EmptyBorder( 6, 6, 6, 6 ) );

      JLabel label0 = new JLabel( "Show subtargets:" );
      cb00 = new JCheckBox( "Show all targets" );
      cb00.setSelected( settings.getShowAllTargets() );
      cb00.setToolTipText( "Show all targets" );
      cb00.addActionListener(
         new ActionListener() {
            public void actionPerformed( ActionEvent ae ) {
               JCheckBox cb = ( JCheckBox ) ae.getSource();
               settings.setShowAllTargets( cb.isSelected() );
               cb0.setEnabled( !cb.isSelected() );
               cb1.setEnabled( !cb.isSelected() );
               cb1a.setEnabled( !cb.isSelected() );
            }
         }
      );
      cb0 = new JCheckBox( "Show targets without descriptions" );
      cb0.setSelected( settings.getShowTargetsWODesc() );
      cb0.setToolTipText( "<html>If checked, targets without descriptions will be shown.<br>" +
            "Typically, only top-level targets have descriptions." );
      cb0.addActionListener(
         new ActionListener() {
            public void actionPerformed( ActionEvent ae ) {
               JCheckBox cb = ( JCheckBox ) ae.getSource();
               settings.setShowTargetsWODesc( cb.isSelected() );
            }
         }
      );
      cb1 = new JCheckBox( "Show targets with dots" );
      cb1.setSelected( settings.getShowTargetsWDot() );
      cb1.setToolTipText( "<html>If checked, targets with dots in their names will be shown.<br>" +
            "A fairly common practice is to name subtargets using 'dot'<br>notation, e.g. 'clean.apidocs'." );
      cb1.addActionListener(
         new ActionListener() {
            public void actionPerformed( ActionEvent ae ) {
               JCheckBox cb = ( JCheckBox ) ae.getSource();
               settings.setShowTargetsWDot( cb.isSelected() );
            }
         }
      );
      cb1a = new JCheckBox( "Show targets with dash" );
      cb1a.setSelected( settings.getShowTargetsWDash() );
      cb1a.setToolTipText( "<html>If checked, targets with names starting with a dash will be shown.<br>" +
            "A common practice is to name subtargets using a dash at the start<br>e.g. '-clean.apidocs'." );
      cb1a.addActionListener(
         new ActionListener() {
            public void actionPerformed( ActionEvent ae ) {
               JCheckBox cb = ( JCheckBox ) ae.getSource();
               settings.setShowTargetsWDash( cb.isSelected() );
            }
         }
      );
      cb1b = new JCheckBox( "Sort target buttons" );
      cb1b.setSelected( settings.getSortTargets() );
      cb1b.setToolTipText( "<html>If checked, the target buttons will be sorted alphabetically by target name." );
      cb1b.addActionListener(
         new ActionListener() {
            public void actionPerformed( ActionEvent ae ) {
               JCheckBox cb = ( JCheckBox ) ae.getSource();
               settings.setSortTargets( cb.isSelected() );
            }
         }
      );

      ++con.y;
      target_panel.add( label0, con );
      ++con.y;
      target_panel.add( cb00, con );
      ++con.y;
      target_panel.add( cb0, con );
      ++con.y;
      target_panel.add( cb1, con );
      ++con.y;
      target_panel.add( cb1a, con );
      ++con.y;
      target_panel.add( cb1b, con);
      
      // Message Level panel
      JPanel msg_panel = new JPanel(new KappaLayout());
      msg_panel.setBorder( new javax.swing.border.EmptyBorder( 6, 6, 6, 6 ) );
      
      lrb0 = new LevelRadioButton( "Errors", Project.MSG_ERR );
      lrb1 = new LevelRadioButton( "Warnings", Project.MSG_WARN );
      lrb2 = new LevelRadioButton( "Information", Project.MSG_INFO );
      lrb3 = new LevelRadioButton( "Verbose", Project.MSG_VERBOSE );
      lrb4 = new LevelRadioButton( "Debug", Project.MSG_DEBUG );

      switch ( settings.getMessageOutputLevel() ) {
         case Project.MSG_ERR:
            lrb0.setSelected( true );
            break;
         case Project.MSG_WARN:
            lrb1.setSelected( true );
            break;
         default:
            lrb2.setSelected( true );
            break;
         case Project.MSG_VERBOSE:
            lrb3.setSelected( true );
            break;
         case Project.MSG_DEBUG:
            lrb4.setSelected( true );
            break;
      }

      lrb0.addActionListener( lrb_al );
      lrb1.addActionListener( lrb_al );
      lrb2.addActionListener( lrb_al );
      lrb3.addActionListener( lrb_al );
      lrb4.addActionListener( lrb_al );

      ButtonGroup bg = new ButtonGroup();
      bg.add( lrb0 );
      bg.add( lrb1 );
      bg.add( lrb2 );
      bg.add( lrb3 );
      bg.add( lrb4 );

      JLabel label1 = new JLabel( "Set message level:" );

      con.y = 0;
      msg_panel.add( label1, con );
      ++con.y;
      msg_panel.add( lrb0, con );
      ++con.y;
      msg_panel.add( lrb1, con );
      ++con.y;
      msg_panel.add( lrb2, con );
      ++con.y;
      msg_panel.add( lrb3, con );
      ++con.y;
      msg_panel.add( lrb4, con );

      // Event panel
      JPanel event_panel = new JPanel(new KappaLayout());
      event_panel.setBorder( new javax.swing.border.EmptyBorder( 6, 6, 6, 6 ) );
      
      JLabel label2 = new JLabel( "Show message events:" );
      cb2 = new JCheckBox( "Build events" );
      cb3 = new JCheckBox( "Target events" );
      cb4 = new JCheckBox( "Task events" );
      cb5 = new JCheckBox( "Log events" );

      cb2.setSelected( settings.getShowBuildEvents() );
      cb3.setSelected( settings.getShowTargetEvents() );
      cb4.setSelected( settings.getShowTaskEvents() );
      cb5.setSelected( settings.getShowLogMessages() );

      cb2.addActionListener(
         new ActionListener() {
            public void actionPerformed( ActionEvent ae ) {
               JCheckBox cb = ( JCheckBox ) ae.getSource();
               settings.setShowBuildEvents( cb.isSelected() );
            }
         }
      );
      cb3.addActionListener(
         new ActionListener() {
            public void actionPerformed( ActionEvent ae ) {
               JCheckBox cb = ( JCheckBox ) ae.getSource();
               settings.setShowTargetEvents( cb.isSelected() );
            }
         }
      );
      cb4.addActionListener(
         new ActionListener() {
            public void actionPerformed( ActionEvent ae ) {
               JCheckBox cb = ( JCheckBox ) ae.getSource();
               settings.setShowTaskEvents( cb.isSelected() );
            }
         }
      );
      cb5.addActionListener(
         new ActionListener() {
            public void actionPerformed( ActionEvent ae ) {
               JCheckBox cb = ( JCheckBox ) ae.getSource();
               settings.setShowLogMessages( cb.isSelected() );
            }
         }
      );

      con.y = 0;
      event_panel.add( label2, con );
      ++con.y;
      event_panel.add( cb2, con );
      ++con.y;
      event_panel.add( cb3, con );
      ++con.y;
      event_panel.add( cb4, con );
      ++con.y;
      event_panel.add( cb5, con );
      
      // Appearance panel
      JPanel appearance_panel = new JPanel(new KappaLayout());
      appearance_panel.setBorder( new javax.swing.border.EmptyBorder( 6, 6, 6, 6 ) );
      
      JLabel label3 = new JLabel( "Antelope Appearance:" );
      cb6 = new JCheckBox( "Show button text" );
      cb7 = new JCheckBox( "Show button icon" );

      cb6.setSelected( settings.getShowButtonText() );
      cb7.setSelected( settings.getShowButtonIcon() );

      cb6.addActionListener(
         new ActionListener() {
            public void actionPerformed( ActionEvent ae ) {
               JCheckBox cb = ( JCheckBox ) ae.getSource();
               settings.setShowButtonText( cb.isSelected() );
               parent.showButtonText(cb.isSelected());
            }
         }
      );
      cb7.addActionListener(
         new ActionListener() {
            public void actionPerformed( ActionEvent ae ) {
               JCheckBox cb = ( JCheckBox ) ae.getSource();
               settings.setShowButtonIcon( cb.isSelected() );
               parent.showButtonIcon(cb.isSelected());
            }
         }
      );

      con.y = 0;
      appearance_panel.add( label3, con );
      ++con.y;
      appearance_panel.add( cb6, con );
      ++con.y;
      appearance_panel.add( cb7, con );
      
      
      
      tabs.add("General", general_panel);
      tabs.add("Targets", target_panel);
      tabs.add("Messages", msg_panel);
      tabs.add("Events", event_panel);
      tabs.add("Appearance", appearance_panel);

   }

   /**
    * Resets all controls to reflect the stored settings in the
    * AntelopePanel.
    */
   protected void reset( String bf ) {
      String build_filename = "<none>";
      if ( bf != null )
         build_filename = bf;
      build_file.setText( build_filename );
      cba.setSelected( settings.getSaveBeforeRun() );
      cba2.setSelected( settings.getUseErrorParsing() );
      cba3.setSelected( settings.getShowPerformanceOutput() );
      cb1.setSelected( settings.getShowTargetsWDot() );
      cb0.setSelected( settings.getShowTargetsWODesc() );
      cb2.setSelected( settings.getShowBuildEvents() );
      cb3.setSelected( settings.getShowTargetEvents() );
      cb4.setSelected( settings.getShowTaskEvents() );
      cb5.setSelected( settings.getShowLogMessages() );
      switch ( settings.getMessageOutputLevel() ) {
         case Project.MSG_ERR:
            lrb0.setSelected( true );
            break;
         case Project.MSG_WARN:
            lrb1.setSelected( true );
            break;
         default:
            lrb2.setSelected( true );
            break;
         case Project.MSG_VERBOSE:
            lrb3.setSelected( true );
            break;
         case Project.MSG_DEBUG:
            lrb4.setSelected( true );
            break;
      }
   }

   /**
    * Description of the Class
    *
    * @version   $Revision$
    */
   class LevelRadioButton extends JRadioButton {
      /**
       * Description of the Field
       */
      private int _level = Project.MSG_ERR;

      /**
       *Constructor for the LevelRadioButton object
       *
       * @param text   Description of Parameter
       * @param level  Description of Parameter
       */
      public LevelRadioButton( String text, int level ) {
         super( text );
         _level = level;
      }

      /**
       * Gets the level attribute of the LevelRadioButton object
       *
       * @return   The level value
       */
      public int getLevel() {
         return _level;
      }
   }

   /**
    * Description of the Field
    */
   ActionListener lrb_al =
      new ActionListener() {
         public void actionPerformed( ActionEvent ae ) {
            LevelRadioButton lrb = ( LevelRadioButton ) ae.getSource();
            settings.setMessageOutputLevel( lrb.getLevel() );
         }
      };
}

