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
package ise.antelope.app;

import java.awt.*;
import java.awt.event.*;

import java.io.*;

import java.util.*;
import java.util.prefs.*;
import java.util.regex.*;

import javax.swing.*;
import javax.swing.event.*;
import javax.swing.text.*;

import ise.antelope.app.jedit.*;
import ise.antelope.common.*;
import ise.library.*;

/**
 * Main entry point for Antelope application.
 *
 * @author Dale Anson
 * @version   $Revision$
 */
public class Antelope extends JFrame implements Constants, CommonHelper {

    /** Usage  */
    private static String usage = "java ise.antelope.Antelope build_file";

    /** Reference to the AntelopePanel  */
    private AntelopePanel _antelope_panel = null;

    /** Reference to the build file editor  */
    private JEditTextArea _editor;

    /** Description of the Field  */
    private JScrollPane _scroller;

    /** Reference to the tabbed pane.  */
    private JTabbedPane _tabs;

    /** Description of the Field  */
    private Font _font = null;

    /** Description of the Field  */
    private JMenu _file_menu = null;

    /** Description of the Field  */
    private JMenu _recent_menu = null;

    private File _build_file = null;

    private StatusBar status = null;

    /** Constructor for the Antelope object  */
    public Antelope() {
        this( null );
    }

    /**
     * Constructor for the Antelope object
     *
     * @param build_file  the build file to use
     */
    public Antelope( File build_file ) {
        super( "Antelope" );
        _tabs = new JTabbedPane();

        final AntelopePanel panel;

        if ( build_file == null ) {
            String name = Constants.PREFS.get( LAST_OPEN_FILE, null );
            if ( name != null )
                build_file = new File( name );
        }
        _antelope_panel = new AntelopePanel( build_file, this, false );

        _antelope_panel.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent ae) {
                        String cmd = ae.getActionCommand();
                        if (cmd == null || cmd.length() == 0)
                            return ;
                        status.setStatus(cmd);

                    }
                }
                                         );


        JPanel contents = new JPanel(new BorderLayout());
        JSplitPane split_pane = new JSplitPane( JSplitPane.HORIZONTAL_SPLIT, _antelope_panel, _tabs );
        contents.add(split_pane, BorderLayout.CENTER);
        status = new StatusBar();
        contents.add(status, BorderLayout.SOUTH);
        setContentPane( contents );

        String font_family = Constants.PREFS.get( FONT_FAMILY, "Monospaced" );
        int font_style = Constants.PREFS.getInt( FONT_STYLE, Font.PLAIN );
        int font_size = Constants.PREFS.getInt( FONT_SIZE, 12 );
        _font = new Font( font_family, font_style, font_size );

        JMenuBar bar = new JMenuBar();
        setJMenuBar( bar );
        _file_menu = new JMenu( "File" );
        _file_menu.setMnemonic( KeyEvent.VK_F );

        JMenu edit_menu = new JMenu( "Edit" );
        edit_menu.setMnemonic( KeyEvent.VK_E );

        JMenu output_menu = new JMenu( "Output" );
        output_menu.setMnemonic( KeyEvent.VK_O );

        JMenu format_menu = new JMenu( "Options" );
        format_menu.setMnemonic( KeyEvent.VK_M );

        JMenu help_menu = new JMenu( "Help" );
        help_menu.setMnemonic( KeyEvent.VK_H );

        JMenuItem new_mi = new JMenuItem( "New Build File",
                new ImageIcon( ClassLoader.getSystemResource( "images/New16.gif" ) ) );
        JMenuItem open_mi = new JMenuItem( "Open Build File",
                new ImageIcon( ClassLoader.getSystemResource( "images/Open16.gif" ) ) );
        JMenuItem save_mi = new JMenuItem( "Save Build File",
                new ImageIcon( ClassLoader.getSystemResource( "images/Save16.gif" ) ) );
        JMenuItem exit_mi = new JMenuItem( "Exit",
                new ImageIcon( ClassLoader.getSystemResource( "images/Stop16.gif" ) ) );
        final JMenuItem undo_mi = new JMenuItem( "Undo",
                new ImageIcon( ClassLoader.getSystemResource( "images/Undo16.gif" ) ) );
        final JMenuItem redo_mi = new JMenuItem( "Redo",
                new ImageIcon( ClassLoader.getSystemResource( "images/Redo16.gif" ) ) );
        final JMenuItem cut_mi = new JMenuItem( "Cut",
                new ImageIcon( ClassLoader.getSystemResource( "images/Cut16.gif" ) ) );
        final JMenuItem copy_mi = new JMenuItem( "Copy",
                new ImageIcon( ClassLoader.getSystemResource( "images/Copy16.gif" ) ) );
        final JMenuItem paste_mi = new JMenuItem( "Paste",
                new ImageIcon( ClassLoader.getSystemResource( "images/Paste16.gif" ) ) );
        final JMenuItem find_mi = new JMenuItem( "Find",
                new ImageIcon( ClassLoader.getSystemResource( "images/Find16.gif" ) ) );
        final JMenuItem replace_mi = new JMenuItem( "Replace",
                new ImageIcon( ClassLoader.getSystemResource( "images/Replace16.gif" ) ) );
        JMenuItem output_mi = new JMenuItem( "Save Output",
                new ImageIcon( ClassLoader.getSystemResource( "images/Save16.gif" ) ) );
        JMenuItem clear_mi = new JMenuItem( "Clear Output",
                new ImageIcon( ClassLoader.getSystemResource( "images/New16.gif" ) ) );
        JMenuItem font_mi = new JMenuItem( "Font..." );
        JMenuItem options_mi = new JMenuItem( "Editor..." );
        JMenuItem syntax_mi = new JMenuItem( "Syntax..." );
        JMenuItem build_options_mi = new JMenuItem("Build File Options...");
        JMenuItem help_mi = new JMenuItem( "Help",
                new ImageIcon( ClassLoader.getSystemResource( "images/Help16.gif" ) ) );
        JMenuItem about_mi = new JMenuItem( "About",
                new ImageIcon( ClassLoader.getSystemResource( "images/About16.gif" ) ) );

        new_mi.setMnemonic( KeyEvent.VK_N );
        open_mi.setMnemonic( KeyEvent.VK_O );
        save_mi.setMnemonic( KeyEvent.VK_S );
        clear_mi.setMnemonic( KeyEvent.VK_N );
        font_mi.setMnemonic( KeyEvent.VK_F );
        help_mi.setMnemonic( KeyEvent.VK_H );
        about_mi.setMnemonic( KeyEvent.VK_A );
        undo_mi.setMnemonic( KeyEvent.VK_Z );
        redo_mi.setMnemonic( KeyEvent.VK_Y );
        cut_mi.setMnemonic( KeyEvent.VK_X );
        copy_mi.setMnemonic( KeyEvent.VK_C );
        paste_mi.setMnemonic( KeyEvent.VK_V );
        find_mi.setMnemonic( KeyEvent.VK_F );
        replace_mi.setMnemonic( KeyEvent.VK_R );
        open_mi.setAccelerator( KeyStroke.getKeyStroke( KeyEvent.VK_O, ActionEvent.CTRL_MASK ) );
        save_mi.setAccelerator( KeyStroke.getKeyStroke( KeyEvent.VK_S, ActionEvent.CTRL_MASK ) );
        clear_mi.setAccelerator( KeyStroke.getKeyStroke( KeyEvent.VK_N, ActionEvent.CTRL_MASK ) );
        help_mi.setAccelerator( KeyStroke.getKeyStroke( KeyEvent.VK_H, ActionEvent.CTRL_MASK ) );
        about_mi.setAccelerator( KeyStroke.getKeyStroke( KeyEvent.VK_A, ActionEvent.CTRL_MASK ) );
        undo_mi.setAccelerator( KeyStroke.getKeyStroke( KeyEvent.VK_Z, ActionEvent.CTRL_MASK ) );
        redo_mi.setAccelerator( KeyStroke.getKeyStroke( KeyEvent.VK_Y, ActionEvent.CTRL_MASK ) );
        cut_mi.setAccelerator( KeyStroke.getKeyStroke( KeyEvent.VK_X, ActionEvent.CTRL_MASK ) );
        copy_mi.setAccelerator( KeyStroke.getKeyStroke( KeyEvent.VK_C, ActionEvent.CTRL_MASK ) );
        paste_mi.setAccelerator( KeyStroke.getKeyStroke( KeyEvent.VK_V, ActionEvent.CTRL_MASK ) );
        find_mi.setAccelerator( KeyStroke.getKeyStroke( KeyEvent.VK_F, ActionEvent.CTRL_MASK ) );
        replace_mi.setAccelerator( KeyStroke.getKeyStroke( KeyEvent.VK_R, ActionEvent.CTRL_MASK ) );

        _file_menu.add( new_mi );
        _file_menu.add( open_mi );
        _file_menu.add( save_mi );
        _file_menu.addSeparator();
        _file_menu.add( exit_mi );
        _file_menu.addSeparator();
        adjustRecentFilesMenu();
        edit_menu.add( undo_mi );
        edit_menu.add( redo_mi );
        edit_menu.addSeparator();
        edit_menu.add( cut_mi );
        edit_menu.add( copy_mi );
        edit_menu.add( paste_mi );
        edit_menu.addSeparator();
        edit_menu.add( find_mi );
        edit_menu.add( replace_mi );
        replace_mi.setVisible( false );
        output_menu.add( output_mi );
        output_menu.add( clear_mi );
        format_menu.add( font_mi );
        format_menu.add( syntax_mi );
        format_menu.add( options_mi );
        format_menu.add( build_options_mi);
        help_menu.add( help_mi );
        help_menu.add( about_mi );
        bar.add( _file_menu );
        bar.add( edit_menu );
        bar.add( output_menu );
        bar.add( format_menu );
        bar.add( help_menu );

        new_mi.addActionListener(
            new ActionListener() {
                public void actionPerformed( ActionEvent ae ) {
                    File build_file = createProject();
                    if ( build_file != null ) {
                        _antelope_panel.openBuildFile( build_file );
                    }
                }
            }
        );
        new_mi.addActionListener( getEditButtonAction() );
        open_mi.addActionListener( _antelope_panel.getOpenActionListener() );
        save_mi.addActionListener(
            new ActionListener() {
                public void actionPerformed( ActionEvent ae ) {
                    saveBuildFile();
                }
            }
        );
        exit_mi.addActionListener(
            new ActionListener() {
                public void actionPerformed( ActionEvent ae ) {
                    _antelope_panel.close();
                    System.exit( 0 );
                }
            }
        );
        help_mi.addActionListener(
            new ActionListener() {
                public void actionPerformed( ActionEvent ae ) {
                    try {
                        java.net.URL url = ClassLoader.getSystemResource(
                                    "manual/index.html" );
                        AboutDialog help_dialog = new AboutDialog( Antelope.this,
                                "Help", url, true );
                        GUIUtils.fillScreen( help_dialog );
                        help_dialog.setVisible( true );
                    }
                    catch ( Exception e ) {
                        e.printStackTrace();
                    }
                }
            }
        );
        about_mi.addActionListener(
            new ActionListener() {
                public void actionPerformed( ActionEvent ae ) {
                    try {
                        java.net.URL url = ClassLoader.getSystemResource(
                                    "about.html" );
                        AboutDialog about_dialog = new AboutDialog( Antelope.this,
                                "About", url, true );
                        GUIUtils.center( Antelope.this, about_dialog );
                        about_dialog.setVisible( true );
                    }
                    catch ( Exception e ) {
                        e.printStackTrace();
                    }
                }
            }
        );

        final AntelopeGUILogHandler logger = new AntelopeGUILogHandler( false );
        logger.setFont( _font );
        _antelope_panel.addLogHandler( logger );

        final JTextComponent ta = logger.getTextComponent();
        find_mi.addActionListener(
            new ActionListener() {
                public void actionPerformed( ActionEvent ae ) {
                    int index = _tabs.getSelectedIndex();
                    if ( index == 0 ) {
                        FindDialog find = new FindDialog( Antelope.this, ta );
                        GUIUtils.center( Antelope.this, find );
                        find.setVisible( true );
                    }
                    else if ( index == 1 ) {
                        FindAndReplace find = new FindAndReplace( Antelope.this, FindAndReplace.FIND, _editor );
                        GUIUtils.center( Antelope.this, find );
                        find.setVisible( true );
                    }
                }
            }
        );

        replace_mi.addActionListener(
            new ActionListener() {
                public void actionPerformed( ActionEvent ae ) {
                    int index = _tabs.getSelectedIndex();
                    if ( index == 1 ) {
                        FindAndReplace find = new FindAndReplace( Antelope.this, FindAndReplace.REPLACE, _editor );
                        GUIUtils.center( Antelope.this, find );
                        find.setVisible( true );
                    }
                }
            }
        );

        font_mi.addActionListener(
            new ActionListener() {
                public void actionPerformed( ActionEvent ae ) {
                    Font font = FontDialog.showFontDialog( Antelope.this, _font );
                    if ( font != null ) {
                        _font = font;
                        Constants.PREFS.put( FONT_FAMILY, _font.getFamily() );
                        Constants.PREFS.putInt( FONT_STYLE, _font.getStyle() );
                        Constants.PREFS.putInt( FONT_SIZE, _font.getSize() );
                        _editor.getPainter().setFont( font );
                        logger.setFont(font);
                    }
                }
            }
        );
        syntax_mi.addActionListener(
            new ActionListener() {
                public void actionPerformed( ActionEvent ae ) {
                    SyntaxChooser sc = SyntaxChooser.showDialog( Antelope.this );
                    if ( sc.getStyles() != null )
                        _editor.getPainter().setStyles( sc.getStyles() );
                }
            }
        );
        options_mi.addActionListener(
            new ActionListener() {
                public void actionPerformed( ActionEvent ae ) {
                    OptionChooser oc = OptionChooser.showDialog( Antelope.this, _editor );
                }
            }
        );
        build_options_mi.addActionListener(
            new ActionListener() {
                public void actionPerformed( ActionEvent ae ) {
                    _antelope_panel.setOptionsPanel( AntelopeOptions.showDialog( _antelope_panel, Antelope.this ));
                }
            }
        );

        _tabs.add( "Output", logger.getPanel() );
        output_mi.addActionListener(
            new ActionListener() {
                public void actionPerformed( ActionEvent ae ) {
                    try {
                        Document doc = logger.getTextComponent().getDocument();
                        String text = doc.getText( 0, doc.getLength() );
                        JFileChooser chooser = new JFileChooser();
                        chooser.setDialogTitle( "Save Output" );

                        int rtn = chooser.showSaveDialog( Antelope.this );

                        if ( rtn == JFileChooser.CANCEL_OPTION ) {

                            return ;
                        }

                        File output_file = chooser.getSelectedFile();
                        StringReader reader = new StringReader( text );
                        FileWriter writer = new FileWriter( output_file );
                        FileUtilities.copyToWriter( reader, writer );
                    }
                    catch ( Exception e ) {}
                }
            }
        );
        clear_mi.addActionListener(
            new ActionListener() {
                public void actionPerformed( ActionEvent ae ) {
                    try {
                        Document doc = logger.getTextComponent().getDocument();
                        doc.remove( 0, doc.getLength() );
                    }
                    catch ( Exception e ) {}
                }
            }
        );
        _editor = new JEditTextArea();
        _editor.addCaretListener(new CaretListener() {
                    public void caretUpdate(CaretEvent ce) {
                        int dot = ce.getDot();
                        status.setLine(_editor.getLineOfOffset(dot) + 1, _editor.getLineCount());
                    }
                }
                                );
        OptionSettings settings = new OptionSettings();
        settings.load();
        settings.apply( _editor );
        _editor.setTokenMarker( new XMLTokenMarker() );
        _editor.getPainter().setStyles( AntelopeSyntaxUtilities.getStoredStyles() );
        _font = new Font(
                    Constants.PREFS.get( FONT_FAMILY, "dialog" ),
                    Constants.PREFS.getInt( FONT_STYLE, Font.PLAIN ),
                    Constants.PREFS.getInt( FONT_SIZE, 12 ) );
        _editor.getPainter().setFont( _font );
        _editor.setCaretPosition( 0 );
        _editor.scrollToCaret();
        //_scroller = new JScrollPane( _editor );
        _tabs.add( "Edit", _editor );

        _tabs.addChangeListener(
            new ChangeListener() {
                public void stateChanged( ChangeEvent ce ) {
                    int index = _tabs.getSelectedIndex();
                    if ( index == 0 ) {
                        logger.getTextComponent().requestFocus();
                        replace_mi.setVisible( false );
                    }
                    else
                        replace_mi.setVisible( true );
                }
            }
        );

        undo_mi.addActionListener(
            new ActionListener() {
                public void actionPerformed( ActionEvent ae ) {
                    _editor.undo();
                    undo_mi.setEnabled( _editor.canUndo() );
                    redo_mi.setEnabled( _editor.canRedo() );
                }
            }
        );
        redo_mi.addActionListener(
            new ActionListener() {
                public void actionPerformed( ActionEvent ae ) {
                    _editor.redo();
                    undo_mi.setEnabled( _editor.canUndo() );
                    redo_mi.setEnabled( _editor.canRedo() );
                }
            }
        );
        cut_mi.addActionListener(
            new ActionListener() {
                public void actionPerformed( ActionEvent ae ) {
                    _editor.cut();
                }
            }
        );
        copy_mi.addActionListener(
            new ActionListener() {
                public void actionPerformed( ActionEvent ae ) {
                    _editor.copy();
                }
            }
        );
        paste_mi.addActionListener(
            new ActionListener() {
                public void actionPerformed( ActionEvent ae ) {
                    _editor.paste();
                }
            }
        );
        open_mi.addActionListener(
            new ActionListener() {
                public void actionPerformed( ActionEvent ae ) {
                    loadBuildFile();
                }
            }
        );

        loadBuildFile();

        _antelope_panel.addActionListener(
            new ActionListener() {
                public void actionPerformed( ActionEvent ae ) {
                    loadBuildFile();
                }
            }
        );
        addWindowListener(
            new WindowAdapter() {
                public void windowClosing( WindowEvent we ) {
                    _antelope_panel.close();
                    System.exit( 0 );
                }
            }
        );

        undo_mi.setEnabled( _editor.canUndo() );
        redo_mi.setEnabled( _editor.canRedo() );

        // look and feel
        /*
        try {
            if (Constants.PREFS.getBoolean( USE_NATIVE_LF, true )) {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            }
            else {
                UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
            }
            SwingUtilities.updateComponentTreeUI(this);
            validate();
        }
        catch (Throwable e) {}
        */
        pack();
        ta.requestFocus();
        GUIUtils.fillScreen( this );
        split_pane.setDividerLocation( 0.25 );
        setVisible( true );
        status.setStatus("Antelope ready.");
    }


    /**
     * Creates an xml file for a bare-bones Ant build file.
     *
     * @return   an xml file for a bare-bones Ant build file
     */
    private File createProject() {
        try {
            JFileChooser chooser = new JFileChooser();
            chooser.setDialogTitle( "Create Build File" );
            int rtn = chooser.showOpenDialog( Antelope.this );
            if ( rtn == JFileChooser.CANCEL_OPTION )
                return null;

            String NL = System.getProperty( "line.separator" );
            File build_file = chooser.getSelectedFile();
            StringBuffer sb = new StringBuffer();
            sb.append( "<project name=\"\" default=\"default\" basedir=\".\">" );
            sb.append( NL );
            sb.append( "   <target name=\"default\"/>" );
            sb.append( NL );
            sb.append( "</project>" );
            StringReader sr = new StringReader( sb.toString() );
            FileUtilities.copyToWriter( sr, new FileWriter( build_file ) );
            return build_file;
        }
        catch ( Exception e ) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Loads the editor with the contents of the current build file from the
     * AntelopePanel.
     */
    private void loadBuildFile() {
        File build_file = _antelope_panel.getBuildFile();
        if ( build_file == null )
            return ;
        openFile( build_file );
    }

    /**
     * Loads the editor with the contents of the given build file.   
     */
    public void openFile( File file ) {
        final File build_file = file;
        if (build_file == null)
            return ;
        if (build_file.isDirectory())
            return ;
        if (build_file.equals(_build_file))
            return ;
        try {
            FileReader reader = new FileReader( build_file );
            StringWriter writer = new StringWriter();
            FileUtilities.copyToWriter( reader, writer );
            TextAreaDefaults defaults = TextAreaDefaults.getDefaults();
            SyntaxDocument doc = defaults.document;
            doc.putProperty(SyntaxDocument.FILE, build_file);
            doc.remove( 0, doc.getLength() );
            doc.insertString( 0, writer.toString(), null );
            _editor.setDocument( doc );
            _editor.setCaretPosition( 0 );
            _editor.scrollToCaret();
            setTitle( "Antelope: " + build_file.getAbsolutePath() );
            _editor.getDocument().addDocumentListener(
                new DocumentListener() {
                    public void changedUpdate( DocumentEvent de ) {
                        setTitle( "Antelope: " + build_file.getAbsolutePath() + "*" );
                    }

                    public void insertUpdate( DocumentEvent de ) {
                        setTitle( "Antelope: " + build_file.getAbsolutePath() + "*" );
                    }

                    public void removeUpdate( DocumentEvent de ) {
                        setTitle( "Antelope: " + build_file.getAbsolutePath() + "*" );
                    }
                }
            );
        }
        catch ( Exception e ) {}
    }

    /**
     * Saves the contents of the editor to the current build file from the
     * AntelopePanel. Whoa, Nelly! It is possible that the build file in the
     * editor is NOT the build file open in the AntelopePanel!
     */
    private void saveBuildFile() {
        // don't do this:
        //File build_file = _antelope_panel.getBuildFile();
        // instead, do this:
        SyntaxDocument doc = (SyntaxDocument)_editor.getDocument();
        File build_file = (File)doc.getProperty(SyntaxDocument.FILE);
        if ( build_file == null )
            return ;

        try {
            StringReader reader = new StringReader( _editor.getText() );
            FileWriter writer = new FileWriter( build_file );
            FileUtilities.copyToWriter( reader, writer );
            _antelope_panel.reload();
            setTitle( "Antelope: " + build_file.getAbsolutePath() );
        }
        catch ( Exception e ) {
            e.printStackTrace();
        }
    }

    /**
     * Gets a list of recently used files as an ArrayList of JMenuItems
     *
     * @return   a list of JMenuItems
     */
    public ArrayList getRecentFilesList() {

        ArrayList list = new ArrayList();
        String recent = PREFS.get( RECENT_LIST, "" );
        StringTokenizer st = new StringTokenizer( recent, File.pathSeparator );

        StringBuffer new_recent = new StringBuffer();
        MenuItemListener mil = new MenuItemListener();
        while ( st.hasMoreTokens() ) {
            String filename = st.nextToken();
            File file = new File( filename );
            if ( !file.exists() ) {
                // delete preferences node for non-existant build file. If the file has
                // been deleted, there is no reason to keep the stored preferences for
                // that file.
                try {
                    Preferences node = PREFS.node( String.valueOf( file.hashCode() ) );
                    node.removeNode();
                }
                catch ( Exception e ) {}
                continue;
            }
            JMenuItem item = new JMenuItem( filename );
            item.addActionListener( mil );
            list.add( item );
            new_recent.append( filename ).append( File.pathSeparator );
        }

        PREFS.put( RECENT_LIST, new_recent.toString() );

        return list;
    }

    public class MenuItemListener implements ActionListener {
        public void actionPerformed( ActionEvent ae ) {
            JMenuItem mi = ( JMenuItem ) ae.getSource();
            String filename = mi.getText();
            _antelope_panel.openBuildFile( new File( filename ) );
        }
    }

    /** Adjusts the recent files on the File menu.  */
    private void adjustRecentFilesMenu() {

        /// TODO: fix the magic number 5.
        for ( int i = _file_menu.getItemCount() - 1; i > 5; i-- ) {
            _file_menu.remove( i );
        }

        ArrayList list = getRecentFilesList();
        Iterator it = list.iterator();

        while ( it.hasNext() ) {
            _file_menu.add( ( JMenuItem ) it.next() );
        }
    }

    /**
     * Sets the targetExecutionThread attribute of the Antelope object
     *
     * @param thread  The new targetExecutionThread value
     */
    public void setTargetExecutionThread( Thread thread ) {}

    /** Description of the Method */
    public void updateGUI() {
        int index = _tabs.getSelectedIndex();
        if ( index == 1 ) {
            _tabs.setSelectedIndex( 0 );
        }
        _tabs.setSelectedIndex( 1 );
        setVisible( true );
    }

    /**
     * Description of the Method
     *
     * @return   Description of the Returned Value
     */
    public boolean canSaveBeforeRun() {

        return false;
    }

    /** Description of the Method  */
    public void saveBeforeRun() {}

    /** Description of the Method  */
    public void clearErrorSource() {}

    /**
     * Description of the Method
     *
     * @return   Description of the Returned Value
     */
    public boolean canShowEditButton() {

        return true;
    }

    /**
     * Gets the editButtonAction attribute of the Antelope object
     *
     * @return   The editButtonAction value
     */
    public ActionListener getEditButtonAction() {

        return
            new ActionListener() {
                public void actionPerformed( ActionEvent ae ) {
                    _tabs.setSelectedIndex( 1 );
                }
            }
            ;
    }

    /**
     * Gets the runButtonAction attribute of the Antelope object
     *
     * @return   The runButtonAction value
     */
    public ActionListener getRunButtonAction() {

        return
            new ActionListener() {
                public void actionPerformed( ActionEvent ae ) {
                    _tabs.setSelectedIndex( 0 );
                }
            }
            ;
    }

    /**
     * Gets the antClassLoader attribute of the Antelope object
     *
     * @return   The antClassLoader value
     */
    public ClassLoader getAntClassLoader() {
        return null;
    }

    public java.util.List getAntJarList() {
        return null;
    }

    public void reloadAnt() {}

    /**
     * Expects the name of a target as the action command. Moves the cursor to
     * that target if found in the build file.
     *
     * @param ae an ActionEvent
     */
    public void actionPerformed( ActionEvent ae ) {
        switch ( ae.getID() ) {
            case CommonHelper.EDIT_EVENT:
                if ( ae.getSource() instanceof Point ) {
                    Point p = ( Point ) ae.getSource();
                    int offset = _editor.getLineStartOffset( p.x - 1 );
                    _editor.setCaretPosition( offset );
                    _editor.scrollToCaret();
                }
                else {
                    try {

                        String target = ae.getActionCommand();
                        String doc = _editor.getText();
                        Pattern pattern = Pattern.compile( "(<target)(.+?)(>)",
                                Pattern.DOTALL );
                        Matcher matcher = pattern.matcher( doc );

                        while ( matcher.find() ) {

                            int start = matcher.start();
                            int end = matcher.end();
                            if ( start < 0 || end < 0 )
                                continue;
                            String target_line = doc.substring( start, end );
                            if ( target_line.indexOf( "name=\"" + target + "\"" ) > 0 ) {
                                _editor.setCaretPosition( start );
                                _editor.scrollToCaret();
                                break;
                            }
                        }
                    }
                    catch ( Exception e ) {
                        e.printStackTrace();
                    }
                }
                break;
            case CommonHelper.TRACE_EVENT:
                //_tabs.setSelectedIndex( 0 );
                break;
            case Constants.RECENT_LIST_CHANGED:
                adjustRecentFilesMenu();
                break;
        }
    }

    /**
     * The main program for the Antelope class
     *
     * @param args  The command line arguments
     */
    public static void main( String[] args ) {
        // load our preferences handler -- this one doesn't give any problems on
        /// Linux like the default preferences handler does. ??? could this possibly
        /// cause problems with a system preferences factory? Shouldn't there be
        /// delegates?
        try {
            Class.forName("ise.antelope.common.Constants");
        }
        catch (Exception e) {
            e.printStackTrace();
        }

        try {
            if ( args.length == 1 ) {

                File f = new File( args[ 0 ] );

                if ( !f.exists() )
                    throw new IllegalArgumentException( usage );

                new Antelope( f );
            }
            else {
                new Antelope();
            }
        }
        catch ( Exception e ) {
            e.printStackTrace();
        }
    }
}

