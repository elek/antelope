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
import java.util.logging.*;
import java.util.prefs.*;
import javax.swing.*;
import javax.swing.event.*;

import ise.library.*;

import ise.library.*;

import org.apache.tools.ant.*;

/**
 * A panel to run Ant. Add a java.util.logging.Handler to get info about
 * progress.
 *
 * @author    Dale Anson, danson@germane-software.com
 * @version   $Revision$
 * @created   July 23, 2002
 */
public class AntelopePanel extends JPanel implements Constants {

   private Preferences _prefs = PREFS;

   private OptionSettings _settings = null;

   /** Description of the Field */
   private AntLogger _build_logger = null;
   private AntPerformanceListener _performance_listener = null;

   /** Description of the Field */
   private AntProject _project = null;

   /**
    * Ant 1.6 has an unnamed target that is used to hold all project-level tasks.
    * This target must be executed before all others.
    */
   private Target _unnamed_target = null;

   /** Description of the Field */
   private TreeMap _targets = null;   // key is a String, value is a Target
   private ArrayList _buttons = null;
   private ArrayList _execute_targets = null;
   /** Description of the Field */
   private DeckPanel _center_panel = null;
   private JPanel _button_panel = null;
   private JTabbedPane _tabs = null;
   private SAXPanel _sax_panel = null;

   private JPanel _control_panel = null;
   private JToggleButton _run_btn = null;
   private JToggleButton _trace_btn = null;
   private JToggleButton _edit_btn = null;
   private JButton _props_btn = null;
   private JButton _options_btn = null;
   private JCheckBox _multi = new JCheckBox( "Multiple targets" );

   /** Description of the Field */
   private JScrollPane _scroller = null;   // for the button panel
   private JPanel _btn_container = null;

   /** Description of the Field */
   private JTextField _project_name = null;
   /** Description of the Field */
   private File _last_directory = null;   // for the file chooser
   /** Description of the Field */
   private File _build_file = null;   // the current Ant build file
   /** Description of the Field */
   private boolean _trace = false;   // trace or execute mode

   /** Description of the Field */
   private boolean _edit = false;   // edit mode

   /** Description of the Field */
   private Thread _runner = null;
   private Thread _target_runner = null;

   // option settings
   /** Description of the Field */
   private AntelopeOptions _options = null;   // to adjust the options
   /** Description of the Field */
   private JMenu _recent = null;

   /** Description of the Field */
   private boolean _use_internal_menu = true;

   // basic logger settings
   /** Description of the Field */
   private Logger _logger = null;
   /** Description of the Field */
   private Handler _console = null;
   /** Description of the Field */
   private Level _log_level = Level.ALL;

   /** Description of the Field */
   private ArrayList _listeners = null;

   /** Description of the Field */
   private CommonHelper _helper = null;

   private AntProgressListener _progress = null;

   private Color GREEN = new Color( 0, 153, 51 );

   /** Constructor for the AntelopePanel object */
   public AntelopePanel() {
      this( null, null, true );
   }

   /**
    * Constructor for AntelopePanel
    *
    * @param helper
    */
   public AntelopePanel( CommonHelper helper ) {
      this( null, helper, true );
   }


   /**
    * Constructor for the AntelopePanel object
    *
    * @param build_file         an Ant build fild
    * @param helper
    * @param use_internal_menu
    */
   public AntelopePanel( File build_file, CommonHelper helper, boolean use_internal_menu ) {
      this( build_file, helper, use_internal_menu, null );
   }

   /**
    * Constructor for the AntelopePanel object
    *
    * @param build_file         an Ant build fild
    * @param helper
    * @param use_internal_menu
    * @param menu_items         additional menu items to add, only useful if
    *      use_internal_menu is true
    */
   public AntelopePanel( File build_file, CommonHelper helper, boolean use_internal_menu,
         ArrayList menu_items ) {



      setLayout( new BorderLayout() );

      _build_file = build_file;
      _helper = helper;
      _use_internal_menu = use_internal_menu;

      if ( _build_file != null ) {
         setPrefs( _build_file );
      }

      try {
         // for some reason, the GUIUtils aren't always loaded in jEdit,
         // so explicity load it into the classloader now.
         Class.forName( "ise.library.GUIUtils" );
      }
      catch ( ClassNotFoundException e ) {
         throw new RuntimeException( e );
      }

      // make sure the xml parser is loaded
      try {
         Class.forName( "javax.xml.parsers.SAXParserFactory" );
      }
      catch ( Exception e ) {
         JOptionPane.showMessageDialog( GUIUtils.getRootJFrame( this ), "<html>Error:<br>" + e.getMessage(),
               "Ant Error", JOptionPane.ERROR_MESSAGE );
         //throw e;
      }

      // set up the control panel





      LambdaLayout lal = new LambdaLayout();
      _control_panel = new JPanel( lal );
      Insets ins = new Insets( 1, 1, 1, 1 );

      _run_btn = new JToggleButton( "Run" );
      _run_btn.setMargin( ins );
      _run_btn.addActionListener(
         new ActionListener() {
            public void actionPerformed( ActionEvent ae ) {
               _edit = false;
               _trace = false;
               _center_panel.show( "panel" );
               _button_panel.setBackground( Color.WHITE );
            }
         }
      );
      if ( _helper != null ) {
         ActionListener al = _helper.getRunButtonAction();
         if ( al != null )
            _run_btn.addActionListener( al );
      }
      _run_btn.setSelected( true );

      _trace_btn = new JToggleButton( "Trace" );
      _trace_btn.setMargin( ins );
      _trace_btn.setToolTipText( "Show target trace" );
      _trace_btn.setSelected( _trace );
      _trace_btn.addActionListener(
         new ActionListener() {
            public void actionPerformed( ActionEvent ae ) {
               _trace = true;
               _edit = false;
               _center_panel.show( "panel" );
               //_helper.actionPerformed( new ActionEvent( AntelopePanel.this, CommonHelper.TRACE_EVENT, "trace" ) );
               _button_panel.setBackground( Color.BLUE );
            }
         }
      );

      _edit_btn = new JToggleButton( "Edit" );
      _edit_btn.setMargin( ins );
      _edit_btn.setToolTipText( "Edit the build file." );
      _edit_btn.setSelected( _edit );
      if ( _helper != null ) {
         ActionListener al = _helper.getEditButtonAction();
         if ( al != null )
            _edit_btn.addActionListener( al );
      }
      _edit_btn.addActionListener(
         new ActionListener() {
            public void actionPerformed( ActionEvent ae ) {
               _edit = true;
               _trace = false;
               _center_panel.show( "tree" );
            }
         }
      );

      _props_btn = new JButton( "Properties" );
      _props_btn.setMargin( ins );
      _props_btn.setToolTipText( "Show current build properties for Ant" );
      _props_btn.addActionListener(
         new ActionListener() {
            public void actionPerformed( ActionEvent ae ) {
               showProperties();
            }
         }
      );

      ButtonGroup bg = new ButtonGroup();
      bg.add( _run_btn );
      bg.add( _trace_btn );
      bg.add( _edit_btn );

      _options_btn = new JButton( "Options" );
      _options_btn.setMargin( ins );
      _options_btn.setToolTipText( "Show output display options" );
      _options_btn.addActionListener(
         new ActionListener() {
            public void actionPerformed( ActionEvent ae ) {
               _options = AntelopeOptions.showDialog( AntelopePanel.this, _helper );
            }
         }
      );
      _control_panel.add( _run_btn, "0, 0, 2, 1, 0, w, 1" );
      _control_panel.add( _trace_btn, "2, 0, 2, 1, 0, w, 1" );
      _control_panel.add( _edit_btn, "4, 0, 2, 1, 0, w, 1" );
      _control_panel.add( _props_btn, "0, 1, 3, 1, 0, w, 1" );
      _control_panel.add( _options_btn, "3, 1, 3, 1, 0, w, 1" );
      lal.makeColumnsSameWidth();

      _project_name =
         new JTextField() {
            public void setText( String text ) {
               if ( text != null && text.length() > 0 ) {
                  super.setText( "Ant Project: " + text );
               }
               else {
                  super.setText( "" );
               }
            }
         };
      _project_name.setEditable( false );
      _project_name.setText( "None" );
      _progress = new AntProgressListener();
      JPanel bottom_panel = new JPanel( new BorderLayout() );
      bottom_panel.add( _control_panel, BorderLayout.NORTH );
      bottom_panel.add( _progress, BorderLayout.CENTER );
      bottom_panel.add( _project_name, BorderLayout.SOUTH );
      add( bottom_panel, BorderLayout.SOUTH );

      _recent = getRecentFilesMenu();

      if ( _use_internal_menu ) {
         JMenuBar bar = new JMenuBar();
         JMenu filemenu = new JMenu( "File" );
         JMenuItem open_mi = new JMenuItem( "Open" );
         if ( menu_items != null ) {
            Iterator it = menu_items.iterator();
            while ( it.hasNext() ) {
               filemenu.add( ( JMenuItem ) it.next() );
            }
         }
         open_mi.addActionListener( getOpenActionListener() );
         filemenu.add( open_mi );
         JMenuItem reload_mi = new JMenuItem( "Reload" );
         reload_mi.addActionListener(
            new ActionListener() {
               public void actionPerformed( ActionEvent ae ) {
                  reload();
               }
            }
         );
         filemenu.add( reload_mi );
         filemenu.add( _recent );
         bar.add( filemenu );
         add( bar, BorderLayout.NORTH );
      }

      _multi.setToolTipText( "Execute multiple targets sequentially" );
      _multi.addActionListener(
         new ActionListener() {
            public void actionPerformed( ActionEvent ae ) {
               openBuildFile( _build_file );
            }
         }
      );

      // initialize the logger
      initLogger();

      // initialize from the build file
      openBuildFile( _build_file );
   }

   private ActionListener _cb_listener =
      new ActionListener() {
         public void actionPerformed( ActionEvent ae ) {
            JCheckBox cb = ( JCheckBox ) ae.getSource();
            if ( _execute_targets == null )
               _execute_targets = new ArrayList();
            if ( cb.isSelected() ) {
               _execute_targets.add( cb );
               cb.setText( cb.getActionCommand() + " (" + _execute_targets.size() + ")" );
            }
            else {
               _execute_targets.remove( cb );
               cb.setText( cb.getActionCommand() );
               for ( int i = 0; i < _execute_targets.size(); i++ ) {
                  cb = ( JCheckBox ) _execute_targets.get( i );
                  cb.setText( cb.getActionCommand() + " (" + ( i + 1 ) + ")" );
               }
            }
         }
      };

   /**
    * Runs several build targets.
    */
   private ActionListener _execute_listener =
      new ActionListener() {
         public void actionPerformed( ActionEvent ae ) {
            final ActionEvent event = ae;
            final AbstractButton button = ( AbstractButton ) ae.getSource();
            String target_name = event.getActionCommand();

            // maybe stop any currently running targets
            if ( _runner != null && _runner.isAlive() ) {
               _target_runner = null;
               _runner.interrupt();
               return ;
            }

            // maybe trace a target
            if ( _trace ) {
               log( "" );
               Hashtable targets = _project.getTargets();
               TraceTarget tt = new TraceTarget();
               Target target = ( Target ) targets.get( target_name );
               log( tt.traceTarget( target ) );
               return ;
            }

            // maybe edit mode, jump to target in source
            if ( _edit ) {
               _helper.actionPerformed( new ActionEvent( this, CommonHelper.EDIT_EVENT, target_name ) );
               return ;
            }

            // execute multiple targets
            _runner =
               new Thread() {
                  public void run() {

                     // make a list of targets to run
                     ArrayList targets = new ArrayList();
                     if ( getAntVersion() == 16 && _unnamed_target != null )
                        targets.add( _unnamed_target.getName() );
                     Iterator it = _execute_targets.iterator();
                     while ( it.hasNext() ) {
                        JCheckBox cb = ( JCheckBox ) it.next();
                        targets.add( cb.getActionCommand() );
                     }

                     // reload the project if need be
                     if ( _settings.getAutoReload() ) {
                        try {
                           reload();
                        }
                        catch ( Exception e ) {
                           e.printStackTrace();
                        }
                        // re-check the appropriate checkboxes, the reload replaces the buttons
                        // on the button panel, so the button that caused this action event
                        // is not the same button that were checked
                        Component[] components = _button_panel.getComponents();
                        int i = 0;
                        for ( ; i < components.length; i++ ) {
                           AbstractButton btn = ( AbstractButton ) components[ i ];
                           if ( btn instanceof JCheckBox ) {
                              String target_name = btn.getActionCommand();
                              if ( targets.contains( target_name ) ) {
                                 btn.setSelected( true );
                              }
                              else {
                                 targets.remove( target_name );
                              }
                           }
                        }
                     }

                     // set button color
                     Color original_color = button.getForeground();
                     button.setForeground( Color.RED );

                     // maybe save all files before running the target
                     saveBeforeRun();

                     // execute the targets
                     try {
                        if ( _settings.getShowPerformanceOutput() && _performance_listener != null ) {
                           _performance_listener.reset();
                        }

                        // run the unnamed target if Ant 1.6
                        if ( getAntVersion() == 16 && _unnamed_target != null )
                           targets.add( _unnamed_target.getName() );

                        // run the targets
                        AntelopePanel.this.executeTargets( this, targets );

                        // old code, before adding reload. This would change the
                        // color of the currently running target to blue to give
                        // a visual indicator of the progress of the build. It would
                        // be nice to get this working again.
                        /*
                        Iterator it = _execute_targets.iterator();
                        while ( it.hasNext() ) {
                           if ( _target_runner != this )
                              break;
                           JCheckBox cb = ( JCheckBox ) it.next();
                           Color cb_color = cb.getForeground();
                           cb.setForeground( Color.blue );
                           String target = cb.getActionCommand();
                           AntelopePanel.this.executeTarget( this, target );
                           cb.setForeground( cb_color );
                           JCheckBox cb = ( JCheckBox ) it.next();
                           targets.add( cb.getActionCommand() );
                     }
                        */
                     }
                     catch ( Exception e ) {
                        _project.fireBuildFinished( e );
                     }
                     finally {
                        if ( _settings.getShowPerformanceOutput() && _performance_listener != null ) {
                           log( _performance_listener.getPerformanceStatistics() );
                           _performance_listener.reset();
                        }
                        _build_logger.close();
                        button.setForeground( original_color );
                        button.setSelected( false );
                     }
                  }

                  public void interrupt() {
                     if ( !isAlive() ) {
                        return ;
                     }
                     super.interrupt();
                     log( Level.SEVERE, "=====> BUILD INTERRUPTED <=====" );
                  }
               };
            _target_runner = _runner;
            _runner.start();
         }
      };

   /**
    * Runs a build target. This action listener is added to all buttons on this
    * panel. This is where all the real work happens to make Ant run.
    */
   private ActionListener _button_listener =
      new ActionListener() {
         public void actionPerformed( ActionEvent ae ) {
            final ActionEvent event = ae;
            final AbstractButton button;
            final String target_name = event.getActionCommand();

            // maybe stop any running targets
            if ( _runner != null && _runner.isAlive() ) {
               _runner.interrupt();
               return ;
            }

            // maybe trace a target
            if ( _trace ) {
               log( "" );
               Hashtable targets = _project.getTargets();
               TraceTarget tt = new TraceTarget();
               Target target = ( Target ) targets.get( target_name );
               log( tt.traceTarget( target ) );
               return ;
            }

            // maybe edit mode, jump to target in source
            if ( _edit ) {
               _helper.actionPerformed( new ActionEvent( this, CommonHelper.EDIT_EVENT, target_name ) );
               return ;
            }

            // execute a target, but first reload the project if need be
            if ( _settings.getAutoReload() ) {
               reload();
               // find the button again, the reload replaces the buttons
               // on the button panel, so the button that caused this action event
               // is not the same button that needs to change color
               Component[] components = _button_panel.getComponents();
               int i = 0;
               for ( ; i < components.length; i++ ) {
                  if ( target_name.equals( ( ( AbstractButton ) components[ i ] ).getActionCommand() ) ) {
                     break;
                  }
               }
               if ( i == components.length )
                  button = ( AbstractButton ) ae.getSource();
               else
                  button = ( AbstractButton ) components[ i ];

            }
            else
               button = ( AbstractButton ) ae.getSource();

            _runner =
               new Thread() {
                  public void run() {
                     // set button color
                     Color original_color = button.getForeground();
                     button.setForeground( Color.RED );

                     // maybe save all files before running the target
                     saveBeforeRun();

                     try {
                        if ( _settings.getShowPerformanceOutput() && _performance_listener != null ) {
                           _performance_listener.reset();
                        }

                        ArrayList targets = new ArrayList();

                        // run the unnamed target if Ant 1.6
                        if ( getAntVersion() == 16 && _unnamed_target != null )
                           targets.add( _unnamed_target.getName() );

                        // run the targets
                        targets.add( target_name );
                        AntelopePanel.this.executeTargets( this, targets );
                     }
                     catch ( Exception e ) {
                        _project.fireBuildFinished( e );
                     }
                     finally {
                        if ( _settings.getShowPerformanceOutput() && _performance_listener != null ) {
                           log( _performance_listener.getPerformanceStatistics() );
                           _performance_listener.reset();
                        }
                        _build_logger.close();
                        button.setForeground( original_color );
                        button.setSelected( false );
                     }
                  }

                  public void interrupt() {
                     if ( !isAlive() ) {
                        return ;
                     }
                     super.interrupt();
                     log( Level.SEVERE, "=====> BUILD INTERRUPTED <=====" );
                  }
               };
            _target_runner = _runner;
            _runner.start();
         }
      };


   /**
    * Executes a target.
    *
    * @param target the name of a target to run
    * @param runner the thread that the target is running in. If Antelope is running
    * as a plugin, this thread will be passed to the Console plugin so the stop
    * button in the Console will stop the build.
    * @exception Exception  Description of Exception
    */
   public void executeTarget( Thread runner, String target ) throws Exception {
      ArrayList targets = new ArrayList();
      targets.add( target );
      executeTargets( runner, targets );
   }

   /**
    * Executes several targets.
    *
    * @param targets a list of target names to run
    * @param runner the thread that the targets are running in. If Antelope is running
    * as a plugin, this thread will be passed to the Console plugin so the stop
    * button in the Console will stop the build.
    * @exception Exception  Description of Exception
    */
   public void executeTargets( Thread runner, ArrayList targets ) throws Exception {
      // maybe prep the error source
      clearErrorSource();

      // set up build logger
      _build_logger.open();
      _build_logger.setMessageOutputLevel( _settings.getMessageOutputLevel() );
      _build_logger.SHOW_BUILD_EVENTS = _settings.getShowBuildEvents();
      _build_logger.SHOW_TARGET_EVENTS = _settings.getShowTargetEvents();
      _build_logger.SHOW_TASK_EVENTS = _settings.getShowTaskEvents();
      _build_logger.SHOW_LOG_MSGS = _settings.getShowLogMessages();

      // set up the progress listener
      _progress.setExecutingTarget( _project, targets );

      // maybe set up shell
      if ( _helper != null && runner != null )
         _helper.setTargetExecutionThread( runner );

      // execute targets
      log( " " );
      _project.fireBuildStarted();
      Iterator it = targets.iterator();
      while ( it.hasNext() ) {
         if ( _target_runner != runner )
            break;
         String target = ( String ) it.next();
         _project.executeTarget( target );
      }
      _project.fireBuildFinished( null );
   }

   /** Saves jEdit buffers before running target. */
   private void saveBeforeRun() {
      if ( _helper != null && _settings.getSaveBeforeRun() )
         _helper.saveBeforeRun();
   }


   /** Clears the jEdit error source. */
   private void clearErrorSource() {
      if ( _helper != null )
         _helper.clearErrorSource();
   }


   /**
    * ActionListener to allow the user to pick a build file for the current
    * ProjectViewer project.
    *
    * @return   The openActionListener value
    */
   public ActionListener getOpenActionListener() {
      ActionListener al =
         new ActionListener() {
            public void actionPerformed( ActionEvent ae ) {
               JFileChooser chooser;
               if ( _last_directory == null && _build_file != null ) {
                  _last_directory = _build_file.getParentFile();
               }
               if ( _last_directory == null ) {
                  chooser = new JFileChooser();
               }
               else {
                  chooser = new JFileChooser( _last_directory );
               }
               int rtn = chooser.showOpenDialog( AntelopePanel.this );
               if ( rtn == JFileChooser.CANCEL_OPTION ) {
                  return ;
               }
               _build_file = chooser.getSelectedFile();
               if ( _build_file != null ) {
                  _last_directory = _build_file.getParentFile();
                  openBuildFile( _build_file );
               }
            }
         };
      return al;
   }


   /**
    * Adds an action listener interested in knowing when the build file has
    * changed or been reloaded.
    *
    * @param listener  The listener
    */
   public void addActionListener( ActionListener listener ) {
      if ( _listeners == null )
         _listeners = new ArrayList();
      _listeners.add( listener );
   }

   /**
    * Notifies registered listeners that the build file has changed or been
    * reloaded.
    *
    * @param build_file  The build file.
    */
   private void fireEvent( File build_file ) {
      if ( _listeners == null )
         return ;
      synchronized ( _listeners ) {
         ActionEvent ae = new ActionEvent( AntelopePanel.this, ActionEvent.ACTION_PERFORMED, build_file.getAbsolutePath() );
         Iterator it = _listeners.iterator();
         while ( it.hasNext() ) {
            ActionListener listener = ( ActionListener ) it.next();
            listener.actionPerformed( ae );
         }
      }
   }

   /**
    * Gets the buildFile attribute of the AntelopePanel object
    *
    * @return   The buildFile value
    */
   public File getBuildFile() {
      return _build_file;
   }


   /**
    * Reloads the current Ant build file from disk and re-initializes this
    * panel.
    */
   public void reload() {
      if ( _build_file != null ) {
         openBuildFile( _build_file );
      }
   }

   /**
    * Sets up an Ant project and creates a button panel for the given build
    * file. Each target gets a button.
    *
    * @param filename  an Ant build file.
    */
   public void openBuildFile( String filename ) {
      if ( filename == null )
         return ;
      File f = new File( filename );
      openBuildFile( f );
   }

   /**
    * Sets up an Ant project and creates a button panel for the given build
    * file. Each target gets a button.
    *
    * @param build_file  an Ant build file.
    */
   public void openBuildFile( final File build_file ) {
      if ( build_file == null || !build_file.exists() )
         return ;
      _build_file = build_file;
      try {
         // constraints for layout
         KappaLayout.Constraints con = KappaLayout.createConstraint();
         con.s = "w";
         con.p = 1;

         // set up panels
         if ( _center_panel == null ) {
            _center_panel = new DeckPanel();
         }
         else {
            _center_panel.removeAll();
         }
         AntelopePanel.this.add( _center_panel, BorderLayout.CENTER );

         if ( _button_panel == null ) {
            _button_panel = new JPanel( new KappaLayout() );
            _button_panel.setBackground( Color.white );
            _button_panel.setBorder( new javax.swing.border.EmptyBorder( 3, 3, 3, 3 ) );
            _scroller = new JScrollPane( _button_panel );
            _btn_container = new JPanel( new BorderLayout() );
            _btn_container.add( _scroller, BorderLayout.CENTER );
            JPanel multi_panel = new JPanel();
            _btn_container.add( _multi, BorderLayout.SOUTH );
         }
         else {
            _button_panel.removeAll();
            _button_panel.setLayout( new KappaLayout() );
         }
         _center_panel.add( "panel", _btn_container );

         if ( _sax_panel == null ) {
            _sax_panel = new SAXPanel( _helper );
         }

         // load the 'edit' panel
         boolean isAntBuildFile = _sax_panel.openBuildFile( _build_file );
         _center_panel.add( "tree", _sax_panel );
         if ( isAntBuildFile ) {
            if ( _edit_btn.isSelected() )
               _center_panel.last();
            else
               _center_panel.first();
            _run_btn.setEnabled( true );
            _trace_btn.setEnabled( true );
            _edit_btn.setEnabled( true );
            _props_btn.setEnabled( true );
            _options_btn.setEnabled( true );
         }
         else {
            _center_panel.last();
            _run_btn.setEnabled( false );
            _trace_btn.setEnabled( false );
            _edit_btn.setEnabled( false );
            _props_btn.setEnabled( false );
            _options_btn.setEnabled( false );
         }

         // create a new button panel from the build file
         // make sure the configuration settings are loaded, but first save any
         // previous settings
         try {
            PREFS.put( LAST_OPEN_FILE, build_file.getAbsolutePath() );
         }
         catch ( Throwable e ) {
            e.printStackTrace();
         }
         adjustRecentFiles( build_file );
         adjustRecentFilesMenu();
         saveConfigurationSettings();

         // create the project and set up the build logger
         if ( isAntBuildFile ) {
            // set up "Execute" button for multiple targets
            try {
               if ( _multi.isSelected() ) {
                  JButton execute_btn = new JButton( "Execute" );
                  _button_panel.add( execute_btn, con );
                  execute_btn.addActionListener( _execute_listener );
                  ++con.y;
               }

               // load the settings for the build file and create an
               // Ant project
               _project = createProject( _build_file );

               // set the label with the name of this project
               String project_name = _project.getProperty( "ant.project.name" );
               if ( project_name == null || project_name.equals( "" ) )
                  project_name = "<unnamed project>";
               _project_name.setText( project_name );

               // load the targets from the build file, only keep the targets
               // that meet the user's subtarget display settings.
               Hashtable targets = _project.getTargets();
               if ( targets == null || targets.size() == 0 ) {
                  System.out.println( "no targets in project" );
                  return ;   /// ??? really ???
               }

               // Ant 1.6 has an un-named target to hold project-level tasks, so
               // find it and save it for later.
               _unnamed_target = null;
               if ( getAntVersion() == 16 ) {
                  Iterator iter = targets.keySet().iterator();
                  while ( iter.hasNext() ) {
                     if ( iter.next().toString().equals( "" ) ) {
                        _unnamed_target = ( Target ) targets.get( "" );
                     }
                  }
               }

               // make buttons by sorting the targets by name
               _targets = new TreeMap( java.text.Collator.getInstance() );
               Enumeration enum = targets.keys();
               while ( enum.hasMoreElements() ) {
                  // adjust which targets are showing --
                  String target_name = ( String ) enum.nextElement();

                  // Ant 1.6 has an un-named target to hold project-level tasks.
                  // It has no name and shouldn't be executed by itself, so
                  // don't make a button for it.
                  if ( target_name == null || target_name.equals( "" ) ) {
                     continue;
                  }


                  Target target = ( Target ) targets.get( target_name );
                  String description = target.getDescription();
                  if ( _settings.getShowAllTargets() ) {
                     _targets.put( target_name, target );
                  }
                  else {
                     if ( target_name.indexOf( "." ) > 0 && _settings.getShowTargetsWDot() ) {
                        // got dots and that's okay, show the target
                        _targets.put( target_name, target );
                        continue;
                     }
                     if ( target_name.indexOf( "." ) > 0 && !_settings.getShowTargetsWDot() ) {
                        // got dots and that's not okay
                        continue;
                     }
                     if ( target_name.startsWith( "-" ) && _settings.getShowTargetsWDash() ) {
                        // got dash and that's okay, show the target
                        _targets.put( target_name, target );
                        continue;
                     }
                     if ( target_name.startsWith( "-" ) && !_settings.getShowTargetsWDash() ) {
                        // got dash and that's not okay
                        continue;
                     }
                     if ( ( description == null || description.equals( "" ) ) && _settings.getShowTargetsWODesc() ) {
                        // got no desc and that's okay, show the target
                        _targets.put( target_name, target );
                        continue;
                     }
                     if ( ( description == null || description.equals( "" ) ) && !_settings.getShowTargetsWODesc() ) {
                        // got no desc and that's not okay
                        continue;
                     }
                     if ( target_name.indexOf( "." ) == -1 && description != null ) {
                        // got no dots and got desc, show the target
                        _targets.put( target_name, target );
                     }
                  }
               }

               // make a new button panel and populate it with new buttons
               // for the targets for this project
               _buttons = new ArrayList();
               _execute_targets = new ArrayList();
               Iterator it = _targets.keySet().iterator();
               while ( it.hasNext() ) {
                  String target_name = ( String ) it.next();
                  Target target = ( Target ) _targets.get( target_name );
                  if ( target == null ) {
                     continue;
                  }
                  String description = target.getDescription();
                  if ( description == null ) {
                     description = target_name;
                  }
                  AbstractButton button;
                  if ( _multi.isSelected() ) {
                     button = new JCheckBox( (isPrivate(target) ? "<html><i>" : "") + target_name );
                     button.addActionListener( _cb_listener );
                     button.setBackground( _button_panel.getBackground() );
                  }
                  else {
                     button = new JButton( (isPrivate(target) ? "<html><i>" : "") + target_name );
                     button.addActionListener( _button_listener );
                  }
                  button.setActionCommand( target_name );
                  button.setToolTipText( description );
                  _button_panel.add( button, con );
                  _buttons.add( button );
                  ++con.y;
               }
            }
            catch ( Exception e ) {
               e.printStackTrace();
            }
         }

         /// not sure this is a good idea, we're calling invokeLater
         /// from within an invokeLater
         SwingUtilities.invokeLater(
            new Runnable() {
               public void run() {
                  _button_panel.validate();
                  AntelopePanel.this.validate();
                  AntelopePanel.this.repaint();
               }
            }
         );
      }
      catch ( Exception e ) {
         e.printStackTrace();
      }
      fireEvent( _build_file );
   }

   /**
    * Sets up an Ant project for the given build file.
    *
    * @param build_file     Description of Parameter
    * @return               an Ant Project initialized and configured with the
    *      given build file
    * @exception Exception  Description of Exception
    */
   public AntProject createProject( File build_file ) throws Exception {
      if ( build_file == null || !build_file.exists() )
         return null;

      // load the option settings for this build file
      setPrefs( build_file );

      // configure the project
      AntProject p = new AntProject();
      try {
         ClassLoader cl = _helper.getAntClassLoader();
         p.init( cl );   // this takes as much as 9 seconds the first time, less than 1/2 second later

         // add the antelope build logger now so that any output produced by the 
         // ProjectHelper is captured
         p.addBuildListener( _build_logger );

         ProjectHelper.configureProject( p, build_file );
         p.setProperty( "ant.file", build_file.getAbsolutePath() );
         p.setProperty( "ant.version", Main.getAntVersion() );

         // add ant.jar to the classpath
         // for Ant 1.6, does ant-launcher.jar need to be added also? --
         // yes -- add all jars in $ant_home/lib and $user.home/.ant/lib, this
         // is what command-line Ant does. Ant also supports a -lib command-line
         // option where the user can specify additional locations. Should
         // Antelope support this? Need a gui in the properties panel if so.
         java.util.List ant_jars = _helper.getAntJarList();
         if ( ant_jars != null ) {
            java.util.List cp_list = new ArrayList();
            String classpath = p.getProperty( "java.class.path" );
            StringTokenizer st = new StringTokenizer( classpath, File.pathSeparator );
            while ( st.hasMoreTokens() ) {
               cp_list.add( new File( st.nextToken() ) );
            }
            Iterator it = ant_jars.iterator();
            while ( it.hasNext() ) {
               File f = new File( ( String ) it.next() );
               if ( !cp_list.contains( f ) ) {
                  cp_list.add( f );
               }
            }
            StringBuffer sb = new StringBuffer();
            it = cp_list.iterator();
            while ( it.hasNext() ) {
               sb.append( ( ( File ) it.next() ).getAbsolutePath() ).append( File.pathSeparator );
            }
            classpath = sb.toString();
            p.setProperty( "java.class.path", classpath );
         }

         // load any saved user properties for this build file. These are properties
         // that the user has set using the properties dialog and in command-line
         // Ant would have been passed on the command line.
         Preferences user_prefs = getPrefs().node( ANT_USER_PROPS );
         String[] keys = user_prefs.keys();
         for ( int i = 0; i < keys.length; i++ ) {
            p.setUserProperty( keys[ i ], user_prefs.get( keys[ i ], "" ) );
         }

         // add the progress bar build listener
         p.addBuildListener( _progress );

         // add the gui input handler
         p.setInputHandler( new AntInputHandler( this ) );

         // optionally add the antelope performance listener
         if ( _settings.getShowPerformanceOutput() ) {
            if ( _performance_listener == null )
               _performance_listener = new AntPerformanceListener();
            p.addBuildListener( _performance_listener );
         }
         return p;
      }
      catch ( Exception e ) {
         //e.printStackTrace();
         JOptionPane.showMessageDialog( GUIUtils.getRootJFrame( this ),
               "<html>Error:<br>" + e.getMessage(),
               "Ant Error",
               JOptionPane.ERROR_MESSAGE );
         throw e;
      }
      catch ( NoClassDefFoundError error ) {
         JOptionPane.showMessageDialog( GUIUtils.getRootJFrame( this ),
               "<html>Error: No Class Definition Found for<br>" + error.getMessage() +
               "<br><p>This is most likely caused by a required third-party<br>" +
               "jar file not being in the class path.",
               "Ant Error",
               JOptionPane.ERROR_MESSAGE );
         throw new Exception( error.getMessage() );
      }
   }


   /**
    * @return   the Ant project created in <code>createProject</code>.
    */
   protected AntProject getAntProject() {
      return _project;
   }


   /** Shows the properties dialog. */
   public void showProperties() {
      if ( _project != null ) {
         AntelopeProperties ap = new AntelopeProperties( this );
         ap.showProperties( _project );
      }
   }

   private boolean isPrivate( Target target ) {
      if (target == null)
         return true;
      String target_name = target.getName();
      if ( target_name.indexOf( "." ) > 0 ) {
         return true;
      }
      if ( target_name.startsWith( "-" ) ) {
         return true;
      }
      String description = target.getDescription();
      if (  description == null || description.equals( "" ) ) {
         return true;
      }
      return false;
   }


   //=============================================================
   // handle configuration settings
   //=============================================================

   /**
    * Loads and sets the preferences for the given build file.
    *
    * @param build_file  The build file to get the preferences for.
    */
   private void setPrefs( File build_file ) {
      if ( build_file == null )
         return ;
      _settings = new OptionSettings( build_file );

      _prefs = _settings.getPrefs();

   }

   /**
    * Gets the preferences for the current build file.
    *
    * @return   the preferences for the current build file
    */
   public Preferences getPrefs() {
      return _prefs;
   }

   /**
    * @return the Ant version if possible. Ant 1.5.x will return 15, Ant 1.6.x will
    * return 16. Anything else returns 15.
    */
   public int getAntVersion() {
      String ant_version = org.apache.tools.ant.Main.getAntVersion();
      return ant_version.indexOf( "1.6" ) > -1 ? 16 : 15;
   }

   /**
    * Sets the useInternalMenu attribute of the AntelopePanel object
    *
    * @param b  The new useInternalMenu value
    */
   public void setUseInternalMenu( boolean b ) {
      _use_internal_menu = b;
   }

   /**
    * Description of the Method
    *
    * @return   Description of the Returned Value
    */
   public boolean useInternalMenu() {
      return _use_internal_menu;
   }

   /**
    * Gets a menu containing recently used files.
    *
    * @return   The recent files menu
    */
   public JMenu getRecentFilesMenu() {
      JMenu menu = new JMenu( "Recent Files" );
      String recent = "";
      try {
         recent = PREFS.get( RECENT_LIST, "" );
      }
      catch ( Exception e ) {}
      ActionListener al =
         new ActionListener() {
            public void actionPerformed( ActionEvent ae ) {
               JMenuItem item = ( JMenuItem ) ae.getSource();
               String filename = item.getText();
               File file = new File( filename );
               openBuildFile( file );
            }
         };
      StringTokenizer st = new StringTokenizer( recent, File.pathSeparator );
      while ( st.hasMoreTokens() ) {
         JMenuItem item = new JMenuItem( st.nextToken() );
         item.addActionListener( al );
         menu.add( item );
      }
      return menu;
   }

   /**
    * Gets a list of recently used files as an ArrayList of JMenuItems
    *
    * @return   a list of JMenuItems
    */
   public ArrayList getRecentFilesList() {
      ArrayList list = new ArrayList();
      String recent = "";
      try {
         recent = PREFS.get( RECENT_LIST, "" );
      }
      catch ( Exception e ) {}
      StringTokenizer st = new StringTokenizer( recent, File.pathSeparator );
      while ( st.hasMoreTokens() ) {
         String filename = st.nextToken();
         JMenuItem item = new JMenuItem( filename );
         item.addActionListener( new MenuItemListener( AntelopePanel.this, filename ) );
         list.add( item );
      }
      return list;
   }

   /** Adjusts the recent files on the Recent Files menu. */
   private void adjustRecentFilesMenu() {
      _recent.removeAll();
      ArrayList list = getRecentFilesList();
      Iterator it = list.iterator();
      while ( it.hasNext() ) {
         _recent.add( ( JMenuItem ) it.next() );
      }
   }

   /**
    * Adjusts the recent file list based on the last file used.
    *
    * @param last_used  The last file used
    */
   private void adjustRecentFiles( File last_used ) {
      // get the current list
      String recent = "";
      try {
         recent = PREFS.get( RECENT_LIST, "" );
      }
      catch ( Exception e ) {}
      if ( recent.startsWith( last_used.getAbsolutePath() ) )
         return ;
      ArrayList list = new ArrayList();
      StringTokenizer st = new StringTokenizer( recent, File.pathSeparator );
      while ( st.hasMoreTokens() ) {
         list.add( st.nextToken() );
      }

      // check if the last used file is already in the list, remove
      // it if it is
      String last = last_used.getAbsolutePath();
      if ( list.contains( last ) ) {
         list.remove( last );
      }

      // add the last used at the top of the list
      list.add( 0, last );

      // trim the list to size
      if ( list.size() > MAX_RECENT_SIZE ) {
         list = new ArrayList( list.subList( 0, MAX_RECENT_SIZE ) );
      }

      // save the list
      StringBuffer sb = new StringBuffer();
      Iterator it = list.iterator();
      while ( it.hasNext() ) {
         sb.append( it.next() ).append( File.pathSeparator );
      }

      try {
         PREFS.put( RECENT_LIST, sb.toString() );
         PREFS.flush();
      }
      catch ( Exception e ) {
         e.printStackTrace();
      }

      // notify helpers that the list has changed
      _helper.actionPerformed( new ActionEvent( this, Constants.RECENT_LIST_CHANGED, last_used.getAbsolutePath() ) );
   }

   /** Description of the Method */
   private void saveConfigurationSettings() {
      if ( _prefs != null ) {
         try {

            PREFS.flush();

         }
         catch ( Exception e ) {
            e.printStackTrace();
         }
      }
   }

   /**
    * Gets the optionsPanel attribute of the AntelopePanel object
    *
    * @return   The optionsPanel value
    */
   protected Component getOptionsPanel() {
      return _options;
   }

   /**
    * Gets the useErrorParsing attribute of the AntelopePanel object
    *
    * @return   The useErrorParsing value
    */
   public boolean getUseErrorParsing() {
      return _settings.getUseErrorParsing();
   }

   //=============================================================
   // handle loggers, action listeners, and events
   //=============================================================
   /** Initializes the logger. */
   private void initLogger() {
      _logger = Logger.getLogger( "ise.antelope.Antelope" );
      _logger.setUseParentHandlers( false );
      _build_logger = new AntLogger();

      // do this after the AntLogger is created as the AntLogger
      // installs a ConsoleHandler by default, so remove all ConsoleHandlers
      // to be on the safe side.
      Handler[] handlers = _logger.getHandlers();
      try {
         for ( int i = 0; i < handlers.length; i++ ) {
            Handler handler = handlers[ i ];
            if ( handler instanceof ConsoleHandler ) {
               //_logger.removeHandler( handler );
            }
         }
      }
      catch ( Throwable e ) {
         e.printStackTrace();
      }
      _logger.setLevel( Level.ALL );
   }


   /**
    * Sets the log level for the default log handler. The default level is
    * Level.ALL, which is the most useful.
    *
    * @param level  The new log level value
    */
   public void setLogLevel( Level level ) {
      _log_level = level;
   }


   /**
    * Adds a log Handler. Antelope uses log handlers to display output, the
    * default handler sends all output to System.out. This method can be used to
    * add additional handlers. The log level for the handler will be set to the
    * current log level for the logger.
    *
    * @param h  The new Handler
    */
   public void addLogHandler( Handler h ) {
      if ( h == null ) {
         return ;
      }
      if ( _logger == null ) {
         initLogger();
      }
      h.setLevel( _log_level );
      _logger.removeHandler( h );
      _logger.addHandler( h );
   }


   /**
    * Removes the given handler from the logger for Antelope.
    *
    * @param h  the Handler to remove
    */
   public void removeLogHandler( Handler h ) {
      if ( _logger == null ) {
         return ;
      }
      _logger.removeHandler( h );
   }


   /** Removes all log handlers from the logger for Antelope. */
   public void removeAllLogHandlers() {
      if ( _logger == null ) {
         return ;
      }
      synchronized ( _logger ) {
         Handler[] handlers = _logger.getHandlers();
         for ( int i = 0; i < handlers.length; i++ ) {
            _logger.removeHandler( handlers[ i ] );
         }
      }
   }


   /**
    * Logs a message.
    *
    * @param msg  the message to log
    */
   private void log( String msg ) {
      log( Level.INFO, msg );
   }

   /**
    * Logs a message.
    *
    * @param level  Description of Parameter
    * @param msg    Description of Parameter
    */
   private void log( Level level, String msg ) {
      _logger.log( level, msg );
   }


   /**
    * Cleans up system resources -- cleans up loggers and flushes preferences to
    * the backing store. Always call this on exit.
    */
   public void close() {
      removeAllLogHandlers();
      try {
         PREFS.flush();
      }
      catch ( Exception e ) {
         e.printStackTrace();
      }
   }
}

