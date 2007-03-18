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
import java.net.URL;
import java.util.*;
import java.util.logging.*;
import java.util.prefs.*;
import javax.swing.*;
import javax.swing.event.*;

import ise.library.*;
import ise.antelope.tasks.*;

import org.apache.tools.ant.*;

/**
 * A panel to run Ant. Add a java.util.logging.Handler to get info about
 * progress.
 *
 * @author    Dale Anson, danson@germane-software.com
 * @version   $Revision$
 * @created   July 23, 2002
 */
public class AntelopePanel extends JPanel {

    private Preferences _prefs = Constants.PREFS;

    private OptionSettings _settings = null;

    private AntLogger _build_logger = null;
    private AntPerformanceListener _performance_listener = null;

    private Project _project = null;
    private HashMap _property_files = null;

    /**
     * Ant 1.6 has an unnamed target that is used to hold all project-level tasks.
     * This target must be executed before all others.
     */
    private Target _unnamed_target = null;

    private Map _targets = null;   // key is a String, value is a Target
    private ArrayList _buttons = null;
    private ArrayList _execute_targets = null;
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
    private JButton _reload_btn = null;
    private JCheckBox _multi = new JCheckBox( "Multiple targets" );
    private AbstractButton _default_btn = null;

    private JScrollPane _scroller = null;   // for the button panel
    private JPanel _btn_container = null;

    private JTextField _project_name = null;
    private File _last_directory = null;   // for the file chooser
    private File _build_file = null;   // the current Ant build file

    // enable trace mode?
    private boolean _trace = false;   // trace or execute mode

    // enable edit mode?
    private boolean _edit = false;   // edit mode

    // target running threads
    private Thread _runner = null;
    private Thread _target_runner = null;

    private ArrayList _last_ran_targets = null;

    // option settings
    private AntelopeOptions _options = null;   // to adjust the options
    private JMenu _recent = null;

    // should the internal menu be used? Antelope (app) uses it's own menus.
    private boolean _use_internal_menu = true;

    // basic logger settings
    private Logger _logger = null;
    private Handler _console = null;
    private Level _log_level = Level.ALL;

    /** Description of the Field */
    private ArrayList _listeners = null;

    /** Description of the Field */
    private ise.antelope.common.CommonHelper _helper = null;

    private AntProgressListener _progress = null;

    private Color GREEN = new Color( 0, 153, 51 );

    private String IMPLICIT_TARGET_NAME = "&lt;implicit&gt;";

    /** Constructor for the AntelopePanel object */
    public AntelopePanel() {
        this( null, null, true );
    }

    /**
     * Constructor for AntelopePanel
     *
     * @param helper
     */
    public AntelopePanel( CommonHelperWrapper helper ) {
        this( null, helper, true );
    }


    /**
     * Constructor for the AntelopePanel object
     *
     * @param build_file         an Ant build fild
     * @param helper
     * @param use_internal_menu
     */
    public AntelopePanel( File build_file, CommonHelperWrapper helper, boolean use_internal_menu ) {
        this( build_file, helper, use_internal_menu, null );
    }

    public AntelopePanel(java.util.List args) {
        switch(args.size()) {
            case 0:
                init(null, null, true, null);
                break;
            case 1:
                init(null, (CommonHelperWrapper)args.get(0), true, null);
                break;
            case 3:
                init((File)args.get(0), (CommonHelperWrapper)args.get(1), ((Boolean)args.get(2)).booleanValue(), null);
                break;
            case 4:
                File f = (File)args.get(0);
                CommonHelperWrapper chw = (CommonHelperWrapper)args.get(1);
                boolean b = ((Boolean)args.get(2)).booleanValue();
                java.util.List m = (java.util.List)args.get(3);
                init(f, chw, b, m);
                break;
        }
    }

    /**
     * Constructor for the AntelopePanel object
     *
     * @param build_file         an Ant build file
     * @param helper
     * @param use_internal_menu
     * @param menu_items         additional menu items to add, only useful if
     *      use_internal_menu is true
     */
    public AntelopePanel( File build_file, CommonHelperWrapper helper, boolean use_internal_menu,
            java.util.List menu_items ) {
        init(build_file, helper, use_internal_menu, menu_items);
    }

    private void init(File build_file, CommonHelperWrapper helper, boolean use_internal_menu,
            java.util.List menu_items ) {

        setLayout( new BorderLayout() );

        _helper = helper;
        _use_internal_menu = use_internal_menu;
        setPrefs( build_file );

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
        }

        try {
            getClass().getClassLoader().getResource( "ise/antelope/tasks/antlib.xml" );
            getClass().getClassLoader().getResource( "ise/antelope/tasks/antelope.taskdefs" );
        }
        catch ( Exception e ) {
            JOptionPane.showMessageDialog( GUIUtils.getRootJFrame( this ), "<html>Error:<br>" + e.getMessage(),
                    "Ant Error", JOptionPane.ERROR_MESSAGE );
        }

        // set up the control panel
        _control_panel = new JPanel( );
        Insets ins = new Insets( 1, 1, 1, 1 );

        _run_btn = new JToggleButton();
        _run_btn.setToolTipText( "Run target mode" );
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
            if ( _helper.getRunButtonAction() != null )
                _run_btn.addActionListener( _helper.getRunButtonAction() );
        }
        _run_btn.setSelected( true );

        _trace_btn = new JToggleButton();
        _trace_btn.setMargin( ins );
        _trace_btn.setToolTipText( "Trace mode" );
        _trace_btn.setSelected( _trace );
        _trace_btn.addActionListener(
            new ActionListener() {
                public void actionPerformed( ActionEvent ae ) {
                    _trace = true;
                    _edit = false;
                    _center_panel.show( "panel" );
                    _button_panel.setBackground( Color.BLUE );
                }
            }
        );
        if ( _helper != null ) {
            if ( _helper.getTraceButtonAction() != null )
                _trace_btn.addActionListener( _helper.getTraceButtonAction() );
        }

        _edit_btn = new JToggleButton();
        _edit_btn.setMargin( ins );
        _edit_btn.setToolTipText( "Edit mode" );
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

        ButtonGroup bg = new ButtonGroup();
        bg.add( _run_btn );
        bg.add( _trace_btn );
        bg.add( _edit_btn );

        _props_btn = new JButton();
        _props_btn.setMargin( ins );
        _props_btn.setToolTipText( "Show current build properties for Ant" );
        _props_btn.addActionListener(
            new ActionListener() {
                public void actionPerformed( ActionEvent ae ) {
                    showProperties();
                }
            }
        );

        _options_btn = new JButton();
        _options_btn.setMargin( ins );
        _options_btn.setToolTipText( "Show output display options" );
        _options_btn.addActionListener(
            new ActionListener() {
                public void actionPerformed( ActionEvent ae ) {
                    _options = AntelopeOptions.showDialog( AntelopePanel.this, _helper );
                }
            }
        );

        _reload_btn = new JButton();
        _reload_btn.setMargin( ins );
        _reload_btn.setToolTipText( "Reload current build file" );
        _reload_btn.addActionListener( new ActionListener() {
                    public void actionPerformed( ActionEvent ae ) {
                        reload();
                    }
                }
                                     );

        _control_panel.setLayout( new GridLayout( 2, 3, 1, 1 ) );
        _control_panel.add( _run_btn );
        _control_panel.add( _trace_btn );
        _control_panel.add( _edit_btn );
        _control_panel.add( _props_btn );
        _control_panel.add( _options_btn );
        _control_panel.add( _reload_btn );
        showButtonText( _settings.getShowButtonText() );
        showButtonIcon( _settings.getShowButtonIcon() );

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
        _multi.setSelected( _settings.getMultipleTargets() );
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
        openBuildFile( build_file );
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
                            if ( AntUtils.getAntVersion() == 1.6 && _unnamed_target != null )
                                targets.add( _unnamed_target.getName() );
                            Iterator it = _execute_targets.iterator();
                            while ( it.hasNext() ) {
                                JCheckBox cb = ( JCheckBox ) it.next();
                                targets.add( cb.getActionCommand() );
                            }

                            // reload the project if need be
                            if ( _settings.getAutoReload() || shouldReload() ) {
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
                                        if ( !targets.contains( target_name ) ) {
                                            targets.remove( target_name );
                                        }
                                    }
                                }
                                Iterator itr = targets.iterator();
                                while ( itr.hasNext() ) {
                                    String target_name = ( String ) itr.next();
                                    for ( i = 0; i < components.length; i++ ) {
                                        AbstractButton btn = ( AbstractButton ) components[ i ];
                                        if ( target_name.equals( btn.getActionCommand() ) ) {
                                            btn.doClick();
                                            break;
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
                                if ( AntUtils.getAntVersion() == 1.6 && _unnamed_target != null )
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
                if ( _settings.getAutoReload() || shouldReload() ) {
                    reload();
                }

                executeTarget( target_name );
            }
        };


    /**
     * Executes a target in a separate thread.
     *
     * @param target the name of a target to run
     */
    public void executeTarget( String target ) {
        final String target_name = target;
        _runner =
            new Thread() {
                public void run() {
                    // find the button again, the reload replaces the buttons
                    // on the button panel, so the button that caused this action event
                    // is not the same button that needs to change color
                    Component[] components = _button_panel.getComponents();
                    AbstractButton button = null;
                    int i = 0;
                    for ( ; i < components.length; i++ ) {
                        if ( target_name.equals( ( ( AbstractButton ) components[ i ] ).getActionCommand() ) ) {
                            button = ( AbstractButton ) components[ i ];
                            break;
                        }
                    }

                    // set button color
                    Color original_color = null;
                    if ( button != null ) {
                        original_color = button.getForeground();
                        button.setForeground( Color.RED );
                    }

                    // maybe save all files before running the target
                    saveBeforeRun();

                    try {
                        if ( _settings.getShowPerformanceOutput() && _performance_listener != null ) {
                            _performance_listener.reset();
                        }

                        ArrayList targets = new ArrayList();

                        // run the unnamed target if Ant 1.6
                        if ( AntUtils.getAntVersion() == 1.6 && _unnamed_target != null ) {
                            //targets.add( _unnamed_target.getName() );
                            targets.add( "" );
                        }

                        // run the targets
                        if ( !target_name.equals( IMPLICIT_TARGET_NAME ) )
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
                        if ( button != null && original_color != null ) {
                            button.setForeground( original_color );
                            button.setSelected( false );
                        }
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

    /**
     * Executes several targets.
     *
     * @param targets a list of target names to run
     * @param runner the thread that the targets are running in. If Antelope is running
     * as a plugin, this thread will be passed to the Console plugin so the stop
     * button in the Console will stop the build.
     * @exception Exception  Description of Exception
     */
    private void executeTargets( Thread runner, ArrayList targets ) throws Exception {
        _last_ran_targets = targets;

        // maybe prep the error source
        clearErrorSource();

        // set up build logger
        _build_logger.open();
        _build_logger.setMessageOutputLevel( _settings.getMessageOutputLevel() );
        _build_logger.SHOW_BUILD_EVENTS = _settings.getShowBuildEvents();
        _build_logger.SHOW_TARGET_EVENTS = _settings.getShowTargetEvents();
        _build_logger.SHOW_TASK_EVENTS = _settings.getShowTaskEvents();
        _build_logger.SHOW_LOG_MSGS = _settings.getShowLogMessages();

        /* switch to default Ant project helper, need to do this so that the
        implicit target will get executed for <ant> tasks. */
        System.setProperty( "org.apache.tools.ant.ProjectHelper", "org.apache.tools.ant.helper.ProjectHelper2" );


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
            String target = ( String ) it.next();
            if ( target.equals( "" ) && _unnamed_target != null ) {
                Hashtable ptargets = _project.getTargets();
                ( ( Target ) ptargets.get( "" ) ).execute();
            }
            else
                _project.executeTarget( target );
            if ( _target_runner != runner )
                break;
        }
        _project.fireBuildFinished( null );

        // reset project helper
        System.setProperty( "org.apache.tools.ant.ProjectHelper", "ise.antelope.common.AntelopeProjectHelper2" );
    }

    /**
     * Executes the default target.
     *
     * @exception Exception  Description of Exception
     */
    public void executeDefaultTarget() {
        if ( _default_btn != null ) {
            executeTarget( _default_btn.getActionCommand() );
        }
    }

    /**
     * Reruns the last ran target(s).
     */
    public void executeLastRanTargets() {
        if ( _last_ran_targets != null ) {
            if ( _unnamed_target != null )
                _last_ran_targets.remove( _unnamed_target.getName() );
            Iterator it = _last_ran_targets.iterator();
            while ( it.hasNext() ) {
                executeTarget( ( String ) it.next() );
            }
        }
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
     * Finds all property files that the build file loads so they can be checked
     * later for changes. If they've changed, the build file can be automatically
     * reloaded to reflect those changes.
     * @see #shouldReload
     */
    private void loadPropertyFiles() {
        if ( _sax_panel != null ) {
            _property_files = ( ( SAXTreeModel ) _sax_panel.getModel() ).getPropertyFiles();
            if ( _property_files == null )
                return ;
            HashMap filelist = new HashMap();
            ArrayList resolved = new ArrayList();
            Iterator it = _property_files.keySet().iterator();
            while ( it.hasNext() ) {
                Object o = it.next();
                if ( o == null )
                    continue;
                File f = null;
                if ( o instanceof File ) {
                    f = ( File ) o;
                    Long lastModified = ( Long ) _property_files.get( f );
                    filelist.put( f, lastModified );
                }
                else if ( _project != null ) {
                    String value = o.toString();
                    String filename = value;
                    if ( value.startsWith( "${" ) && value.endsWith( "}" ) ) {
                        filename = filename.substring( 2, filename.length() - 1 );
                    }
                    filename = _project.getProperty( filename );
                    if ( filename != null )
                        f = new File( filename );
                    if ( f != null && !f.exists() ) {
                        f = new File( _project.getBaseDir(), filename );
                    }
                    if ( f != null && f.exists() ) {
                        filelist.put( f, new Long( f.lastModified() ) );
                        resolved.add( value );
                    }
                    // see issue #21, Ant standard is to quietly ignore files not found
                    //else
                    //    _logger.warning( "Unable to find property file for " + value );
                }
            }
            it = resolved.iterator();
            while ( it.hasNext() ) {
                filelist.remove( it.next() );
            }
            _property_files = filelist;
        }
    }

    public boolean shouldReload() {
        if ( _property_files == null )
            return false;
        Iterator it = _property_files.keySet().iterator();
        while ( it.hasNext() ) {
            File f = ( File ) it.next();
            Long lastModified = ( Long ) _property_files.get( f );
            if ( lastModified != null && lastModified.longValue() != f.lastModified() ) {
                return true;
            }
        }
        return false;
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
        boolean new_file = !build_file.equals( _build_file );
        _build_file = build_file;
        try {
            // constraints for layout
            KappaLayout.Constraints con = KappaLayout.createConstraint();
            con.s = "w";
            con.p = 1;

            // set up panels
            if ( _center_panel == null ) {
                _center_panel = new DeckPanel();
                AntelopePanel.this.add( _center_panel, BorderLayout.CENTER );
            }
            else {
                _center_panel.removeAll();
            }
            if ( _button_panel == null ) {
                _button_panel = new JPanel( new KappaLayout() );
                _button_panel.setBackground( Color.white );
                _button_panel.setBorder( new javax.swing.border.EmptyBorder( 3, 3, 3, 3 ) );
                _scroller = new JScrollPane( _button_panel );
                _scroller.getVerticalScrollBar().setUnitIncrement(50);
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
                ///_edit_btn.setEnabled( false );
                _props_btn.setEnabled( false );
                _options_btn.setEnabled( false );
            }

            // create a new button panel from the build file
            // make sure the configuration settings are loaded, but first save any
            // previous settings
            try {
                Constants.PREFS.put( Constants.LAST_OPEN_FILE, build_file.getAbsolutePath() );
            }
            catch ( Throwable e ) {
                e.printStackTrace();
            }
            adjustRecentFiles( build_file );
            adjustRecentFilesMenu();
            saveConfigurationSettings();

            // create the project and set up the build logger
            if ( isAntBuildFile ) {
                try {

                    // load the settings for the build file and create an
                    // Ant project
                    _project = createProject( _build_file );
                    loadPropertyFiles();

                    // set up "Execute" button for multiple targets
                    ArrayList last_used_targets = null;
                    if ( new_file ) {
                        _multi.setSelected( _settings.getMultipleTargets() );
                    }
                    if ( _multi.isSelected() ) {
                        JButton execute_btn = new JButton( "Execute" );
                        _button_panel.add( execute_btn, con );
                        execute_btn.addActionListener( _execute_listener );
                        ++con.y;
                    }

                    // set the label with the name of this project
                    String project_name = _project.getProperty( "ant.project.name" );
                    if ( project_name == null || project_name.equals( "" ) )
                        project_name = "<unnamed project>";
                    _project_name.setText( project_name );

                    // load the targets from the build file, only keep the targets
                    // that meet the user's subtarget display settings.
                    Map targets = _project.getTargets();
                    if ( targets == null || targets.size() == 0 ) {
                        //Log.log( "no targets in project" );
                        return ;   /// ??? really ???
                    }
                    ///
                    ///for (Iterator it = targets.keySet().iterator(); it.hasNext(); ) {
                    ///    Log.log("target in targets: " + it.next());
                    ///}
                    ///


                    // Ant 1.6 has an un-named target to hold project-level tasks, so
                    // find it and save it for later.
                    _unnamed_target = null;
                    if ( AntUtils.getAntVersion() >= 1.6 ) {
                        Iterator iter = targets.keySet().iterator();
                        while ( iter.hasNext() ) {
                            if ( iter.next().toString().equals( "" ) ) {
                                _unnamed_target = ( Target ) targets.get( "" );
                            }
                        }
                    }

                    // make buttons by sorting the targets by name or by leaving
                    // them in the order they appear in the build file
                    if ( _settings.getSortTargets() )
                        _targets = new TreeMap( java.text.Collator.getInstance() );
                    else
                        _targets = new LinkedHashMap();
                    Map sax_targets = _sax_panel.getTargets();

                    ///
                    ///for (Iterator ix = sax_targets.keySet().iterator(); ix.hasNext(); ) {
                    ///    String key = (String)ix.next();
                    ///    Object value = sax_targets.get(key);
                    ///    Log.log("sax_targets: " + key + ":" + value);
                    ///}
                    ///

                    Iterator it = sax_targets.keySet().iterator();///targets.keySet().iterator();
                    while ( it.hasNext() ) {
                        // adjust which targets are showing --
                        String target_name = ( String ) it.next();
                        // Ant 1.6 has an un-named target to hold project-level tasks.
                        // It has no name and shouldn't be executed by itself, so
                        // don't make a button for it.
                        if ( target_name == null || target_name.equals("")) {
                            continue;
                        }

                        Target target = ( Target ) targets.get( target_name );

                        if ( target == null ) {
                            SAXTreeNode node = ( SAXTreeNode ) sax_targets.get( target_name );
                            if ( node.isImported() ) {
                                target_name = node.getAttributeValue( "name" );
                                target = ( Target ) targets.get( target_name );
                                ///
                                System.out.println("+++++ " + node.getFile());
                                ///
                            }
                            if ( target == null )
                                continue;
                        }
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

                    if ( _unnamed_target != null ) {
                        _targets.put( IMPLICIT_TARGET_NAME, _unnamed_target );
                    }
                    // make a new button panel and populate it with new buttons
                    // for the targets for this project
                    _buttons = new ArrayList();
                    _execute_targets = new ArrayList();
                    it = _targets.keySet().iterator();
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
                            if ( target_name.equals( IMPLICIT_TARGET_NAME ) )
                                continue;
                            SAXTreeNode node = ( SAXTreeNode ) sax_targets.get( target_name );
                            button = new JCheckBox( );
                            String btn_text = "<html>";
                            if ( node == null ) {
                                btn_text += "<i>";
                            }
                            else {
                                if ( node.isPrivate() )
                                    btn_text += "<i>";
                                if ( node.isDefaultTarget() )
                                    button.setForeground( GREEN );
                            }
                            btn_text += target_name;
                            button.setText( btn_text );
                            button.addActionListener( _cb_listener );
                            button.setBackground( _button_panel.getBackground() );
                            if ( node.isDefaultTarget() )
                                _default_btn = button;
                        }
                        else {
                            String btn_text = "<html>";
                            SAXTreeNode node = ( SAXTreeNode ) sax_targets.get( target_name );
                            button = new JButton();
                            if ( node == null ) {   // this shouldn't happen
                                btn_text += isPrivate( target ) ? "<i>" : "";
                            }
                            else {
                                if ( node.isPrivate() )
                                    btn_text += "<i>";
                                if ( node.isDefaultTarget() )
                                    button.setForeground( GREEN );
                            }
                            btn_text += target_name;
                            button.setText( btn_text );
                            button.addActionListener( _button_listener );
                            if ( node != null && node.isDefaultTarget() )
                                _default_btn = button;
                        }
                        button.setActionCommand( target_name );
                        button.setToolTipText( description );
                        _button_panel.add( button, con );
                        _buttons.add( button );
                        ++con.y;
                    }
                    // if new and was multi, restore selected targets
                    if ( new_file && _multi.isSelected() ) {
                        it = _settings.getMultipleTargetList().iterator();
                        while ( it.hasNext() ) {
                            String name = ( String ) it.next();
                            Iterator itr = _buttons.iterator();
                            while ( itr.hasNext() ) {
                                AbstractButton btn = ( AbstractButton ) itr.next();
                                if ( btn.getActionCommand().equals( name ) )
                                    btn.doClick();
                            }
                        }
                    }
                }
                catch ( Exception e ) {
                    e.printStackTrace();
                }
            }

            /// not sure this is a good idea, we're calling invokeLater
            /// from within an invokeLater
            /* */
            SwingUtilities.invokeLater(
                new Runnable() {
                    public void run() {
                      _button_panel.validate();
                      AntelopePanel.this.validate();
                      AntelopePanel.this.repaint();
                    }
                }
            );
            /* */
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
    public Project createProject( File build_file ) throws Exception {
        if ( build_file == null || !build_file.exists() )
            return null;

        // load the option settings for this build file
        setPrefs( build_file );

        // configure the project
        Project p = new Project();

        // set the project helper -- the AntelopeProjectHelper2 is the same as the Ant
        // ProjectHelper2, but has been slightly modified so it does not automatically
        // run the implicit target
        System.setProperty( "org.apache.tools.ant.ProjectHelper", "ise.antelope.common.AntelopeProjectHelper2" );

        try {
            ClassLoader cl = _helper.getAntClassLoader();
            p.setCoreLoader( cl );

            /*
            try {
                Log.log("loading antlib with _helper classloader");
                cl.getResource( "ise/antelope/tasks/antlib.xml" );
                Log.log("loaded antlib with _helper classloader");
            }
            catch(Exception e) {
                e.printStackTrace();
            }
            */

            // add the antelope build logger now so that any output produced by the
            // ProjectHelper is captured
            p.addBuildListener( _build_logger );

            // add the progress bar build listener
            p.addBuildListener( _progress );

            // optionally add the antelope performance listener
            if ( _settings.getShowPerformanceOutput() ) {
                if ( _performance_listener == null )
                    _performance_listener = new AntPerformanceListener();
                p.addBuildListener( _performance_listener );
            }

            // add the gui input handler
            setInputHandler( p, "ise.antelope.common.AntInputHandler" );

            p.init();   // this takes as much as 9 seconds the first time, less than 1/2 second later

            p.setUserProperty( "ant.file", build_file.getAbsolutePath() );
            p.setProperty( "ant.version", Main.getAntVersion() );
            //String ant_home = System.getProperty("ant.home");
            String ant_home = AntUtils.getAntHome();
            if ( ant_home != null ) {
                p.setProperty( "ant.home", ant_home );
            }
            String ant_lib_dirs = AntUtils.getAntLibDirs();
            if ( ant_lib_dirs != null )
                p.setProperty( "ant.library.dir", ant_lib_dirs );

            // add ant.jar to the classpath
            // for Ant 1.6, does ant-launcher.jar need to be added also? --
            // yes -- add all jars in $ant_home/lib and $user.home/.ant/lib, this
            // is what command-line Ant does. Ant also supports a -lib command-line
            // option where the user can specify additional locations. Should
            // Antelope support this? Need a gui in the properties panel if so.
            // 12/22/2004: added AntelopeLauncher, so -lib option is handled for app,
            // but not for plugin
            /// -- should this be done here or in the helper? --
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
                System.setProperty("java.class.path", classpath);
            }

            // load any saved user properties for this build file. These are properties
            // that the user has set using the properties dialog and in command-line
            // Ant would have been passed on the command line.
            Preferences user_prefs = getPrefs().node( Constants.ANT_USER_PROPS );
            String[] keys = user_prefs.keys();
            for ( int i = 0; i < keys.length; i++ ) {
                p.setUserProperty( keys[ i ], user_prefs.get( keys[ i ], "" ) );
            }

            //ProjectHelper.configureProject( p, build_file );      // deprecated
            ProjectHelper helper = ProjectHelper.getProjectHelper();
            p.addReference("ant.projectHelper", helper);
            helper.parse(p, build_file);

            //for (Iterator it = p.getTargets().keySet().iterator(); it.hasNext(); ) {
            //    System.out.println("target: " + it.next());
            //}

            /*
            // looks like a recent change for antlib has busted loading custom tasks from
            // an antlib declaration. Need to check if this ever worked, I used to use
            // taskdef exclusively, and have only recently switched to using antlib.
            // Using antlib works when Antelope is running as an application, but not as
            // a plugin.  Seems to have something to do with classloading.
            try {
                System.out.println("AntelopePanel classloader = " + getClass().getClassLoader().hashCode());
                Class c = Class.forName("org.apache.tools.ant.Main");
                if (c != null) {
                    System.out.println("classloader for Main = " + c.getClassLoader().hashCode());
                    System.out.println("parent classloader for Main = " + c.getClassLoader().getParent());
                }
                else
                    System.out.println("did not find class for Main");
                c = Class.forName("ise.antelope.tasks.Unset");
                if (c != null){
                    System.out.println("classloader for Unset = " + c.getClassLoader().hashCode());
                    System.out.println("parent classloader for Unset = " + c.getClassLoader().getParent());
                    System.out.println("classloader for Unset is a " + c.getClassLoader().getClass().getName());
                }
                else
                    System.out.println("did not find class for Unset");
                c = Class.forName("org.apache.tools.ant.taskdefs.optional.EchoProperties");
                if (c != null){
                    System.out.println("classloader for EchoProperties = " + c.getClassLoader().hashCode());
                    System.out.println("parent classloader for EchoProperties = " + c.getClassLoader().getParent());
                    System.out.println("classloader for EchoProperties is a " + c.getClassLoader().getClass().getName());
                }
                else
                    System.out.println("did not find class for EchoProperties");
            }
            catch(Exception e) {
                e.printStackTrace();
            }
            */


            return p;
        }
        catch ( Exception e ) {
            Log.log(e);
            e.printStackTrace(System.out);
            JOptionPane.showMessageDialog( GUIUtils.getRootJFrame( this ),
                    "<html>Error:<br>" + e.getMessage(),
                    "Ant Error",
                    JOptionPane.ERROR_MESSAGE );
            throw e;
        }
        catch ( NoClassDefFoundError error ) {
            Log.log(error);
            error.printStackTrace(System.out);
            JOptionPane.showMessageDialog( GUIUtils.getRootJFrame( this ),
                    "<html>Error: No Class Definition Found for<br>" + error.getMessage() +
                    "<br><p>This is most likely caused by a required third-party<br>" +
                    "jar file not being in the class path.",
                    "Ant Error",
                    JOptionPane.ERROR_MESSAGE );
            throw new Exception( error.getMessage() );
        }
    }

    private void setInputHandler( Project p, String inputHandler ) {
        try {
            AntInputHandler ih = new AntInputHandler(AntelopePanel.this);
            PrivilegedAccessor.invokeMethod( p, "setInputHandler", new Object[] {ih} );
        }
        catch ( Exception e ) {
            e.printStackTrace();
        }
    }


    /**
     * @return   the Ant project created in <code>createProject</code>.
     */
    protected Project getAntProject() {
        return _project;
    }


    /** Shows the properties dialog. */
    public void showProperties() {
        if ( _project != null ) {
            AntelopeProperties ap = new AntelopeProperties( this );
            ap.showProperties( _project );
        }
    }

    /**
     * Determines if a target should be considered a "private" target.
     * @return true if the target name contains a "." or starts with "-" or has an
     * empty description.
     */
    private boolean isPrivate( Target target ) {
        if ( target == null )
            return true;
        String target_name = target.getName();
        if ( target_name.indexOf( "." ) > 0 ) {
            return true;
        }
        if ( target_name.startsWith( "-" ) ) {
            return true;
        }
        String description = target.getDescription();
        if ( description == null || description.equals( "" ) ) {
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
        if ( _settings != null )
            _settings.load( build_file );
        else
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
            recent = Constants.PREFS.get( Constants.RECENT_LIST, "" );
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
            recent = Constants.PREFS.get( Constants.RECENT_LIST, "" );
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
            recent = Constants.PREFS.get( Constants.RECENT_LIST, "" );
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
        if ( list.size() > Constants.MAX_RECENT_SIZE ) {
            list = new ArrayList( list.subList( 0, Constants.MAX_RECENT_SIZE ) );
        }

        // save the list
        StringBuffer sb = new StringBuffer();
        Iterator it = list.iterator();
        while ( it.hasNext() ) {
            sb.append( it.next() ).append( File.pathSeparator );
        }

        try {
            Constants.PREFS.put( Constants.RECENT_LIST, sb.toString() );
            Constants.PREFS.flush();
        }
        catch ( Exception e ) {
            e.printStackTrace();
        }

        // notify helpers that the list has changed
        _helper.actionPerformed( new ActionEvent( this, Constants.RECENT_LIST_CHANGED, last_used.getAbsolutePath() ) );
    }

    /** Description of the Method */
    private void saveConfigurationSettings() {
        _settings.setMultipleTargets( _multi.isSelected() );
        if ( _buttons != null && _multi.isSelected() ) {
            TreeSet btns = new TreeSet( new Comparator() {
                        public int compare( Object a, Object b ) {
                            AbstractButton btna = ( AbstractButton ) a;
                            AbstractButton btnb = ( AbstractButton ) b;
                            String aname = btna.getText();
                            String bname = btnb.getText();
                            aname = aname.substring( 0, aname.length() - 1 );
                            aname = aname.substring( aname.lastIndexOf( "(" ) + 1 );
                            bname = bname.substring( 0, bname.length() - 1 );
                            bname = bname.substring( bname.lastIndexOf( "(" ) + 1 );
                            int aint = Integer.parseInt( aname );
                            int bint = Integer.parseInt( bname );
                            if ( aint < bint )
                                return -1;
                            if ( aint == bint )
                                return 0;
                            return 1;
                        }
                    }
                                      );
            Iterator it = _buttons.iterator();
            while ( it.hasNext() ) {
                AbstractButton btn = ( AbstractButton ) it.next();
                if ( btn.isSelected() )
                    btns.add( btn );
            }
            it = btns.iterator();
            ArrayList target_list = new ArrayList();
            while ( it.hasNext() ) {
                String target_name = ( ( AbstractButton ) it.next() ).getActionCommand();
                target_list.add( target_name );
            }
            _settings.setMultipleTargetList( target_list );
        }
        if ( _prefs != null ) {
            try {
                //_settings.save();
                //PREFS.flush();
                _prefs.flush();
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

    public void setOptionsPanel( AntelopeOptions op ) {
        _options = op;
    }

    /**
     * Gets the useErrorParsing attribute of the AntelopePanel object
     *
     * @return   The useErrorParsing value
     */
    public boolean getUseErrorParsing() {
        return _settings.getUseErrorParsing();
    }

    /**
     * Alter the control panel buttons to show text as appropriate. This is a
     * doer, not just a setter and applies to all buttons on the control panel.
     */
    public void showButtonText( boolean b ) {
        if ( b ) {
            _run_btn.setText( "Run" );
            _trace_btn.setText( "Trace" );
            _edit_btn.setText( "Edit" );
            _props_btn.setText( "Properties" );
            _options_btn.setText( "Options" );
            _reload_btn.setText( "Reload" );
        }
        else {
            _run_btn.setText( "" );
            _trace_btn.setText( "" );
            _edit_btn.setText( "" );
            _props_btn.setText( "" );
            _options_btn.setText( "" );
            _reload_btn.setText( "" );
        }
    }

    public boolean getShowButtonText() {
        return _settings.getShowButtonText();
    }

    /**
     * Alter the control panel buttons to show icon as appropriate. This is a
     * doer, not just a setter and applies to all buttons on the control panel.
     */
    public void showButtonIcon( boolean b ) {
        if ( b ) {
            URL url = getClass().getClassLoader().getResource( "images/Play16.gif" );
            Icon icon = null;
            if ( url != null )
                icon = new ImageIcon( url );
            _run_btn.setIcon( icon );

            url = getClass().getClassLoader().getResource( "images/Zoom16.gif" );
            icon = null;
            if ( url != null )
                icon = new ImageIcon( url );
            _trace_btn.setIcon( icon );

            url = getClass().getClassLoader().getResource( "images/Edit16.gif" );
            icon = null;
            if ( url != null )
                icon = new ImageIcon( url );
            _edit_btn.setIcon( icon );

            url = getClass().getClassLoader().getResource( "images/Information16.gif" );
            icon = null;
            if ( url != null )
                icon = new ImageIcon( url );
            _props_btn.setIcon( icon );

            url = getClass().getClassLoader().getResource( "images/Properties16.gif" );
            icon = null;
            if ( url != null )
                icon = new ImageIcon( url );
            _options_btn.setIcon( icon );

            url = getClass().getClassLoader().getResource( "images/Refresh16.gif" );
            icon = null;
            if ( url != null )
                icon = new ImageIcon( url );
            _reload_btn.setIcon( icon );
        }
        else {
            _run_btn.setIcon( null );
            _trace_btn.setIcon( null );
            _edit_btn.setIcon( null );
            _props_btn.setIcon( null );
            _options_btn.setIcon( null );
            _reload_btn.setIcon( null );
        }
    }

    public boolean getShowButtonIcon() {
        return _settings.getShowButtonIcon();
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
                    _logger.removeHandler( handler ); ///
                }
            }
        }
        catch ( Throwable e ) {
            Log.log(this, e);
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

        // make sure it is really added, initLogger may remove it, so
        // explicitly add it again
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
        saveConfigurationSettings();
        removeAllLogHandlers();
        try {
            Constants.PREFS.flush();
        }
        catch ( Exception e ) {
            e.printStackTrace();
        }
    }
}

