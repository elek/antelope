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

import ise.library.LambdaLayout;

import ise.library.*;

import java.awt.*;
import java.awt.event.*;

import java.net.URL;

import javax.swing.*;
import javax.swing.event.*;
import javax.swing.text.html.*;


/**
 * An About dialog that uses a JEditorPane to display content.  JEditorPane
 * can render html and even make the links work, so this About dialog can
 * actually connect the user to my home page.
 * 
 * @version $Revision$
 * @author Dale Anson
 */
public class AboutDialog
            extends JDialog
    implements Navable {

    /** Description of the Field */
    private JEditorPane editor;

    /** Description of the Field */
    private JScrollPane scrollpane;

    /** Description of the Field */
    private Nav nav;
    private URL initialURL = null;

    /**
     * Constructor
     * 
     * @param owner      parent frame hosting this dialog
     * @param title      title to display on dialog, probably just "About"
     * @param mime_type  JEditorPane allows "text/html", "text/plain", and
     *        "text/rtf"
     * @param contents   the stuff to show, coded in the correct mime type
     * @param nav   should "back" and "forward" buttons be displayed?
     */
    public AboutDialog( Frame owner, String title, String mime_type,
            String contents, boolean nav ) {

        // initialize
        super( owner, title, true );

        // set up JEditorPane
        editor = new JEditorPane( mime_type, contents );
        init( nav );
    }

    /**
     * Constructor for AboutDialog
     * 
     * @param owner
     * @param title
     * @param contents
     * @param nav
     * @exception Exception  Description of Exception
     */
    public AboutDialog( Frame owner, String title, URL contents, boolean nav )
    throws Exception {

        // initialize
        super( owner, title, true );

        // set up JEditorPane
        Log.log("AboutDialog, contents = " + contents);
        Log.log("AboutDialog, content type = " + contents.openConnection().getContentType());
        editor = new JEditorPane( contents );
        initialURL = contents;
        init( nav );
    }

    /**
     * Description of the Method
     * 
     * @param use_nav
     */
    private void init( boolean use_nav ) {
        editor.setEditable( false );
        editor.addHyperlinkListener( new LinkListener() );

        // set up main panel
        BorderLayout layout = new BorderLayout();
        JPanel panel = new JPanel( layout );

        scrollpane = new JScrollPane( editor );
        panel.add( scrollpane, BorderLayout.CENTER );

        JButton ok_btn = new JButton( "Ok" );
        ok_btn.addActionListener( new ActionListener() {
                    public void actionPerformed( ActionEvent ae ) {
                        setVisible( false );
                        dispose();
                    }
                }
                                );

        JPanel btn_panel = new JPanel();
        if ( use_nav ) {
            btn_panel.setLayout( new LambdaLayout() );
            nav = new Nav( this );
            btn_panel.add( nav, "0, 0, 1, 1, W, , 5" );
            btn_panel.add( ok_btn, "1, 0, 1, 1, E, , 5" );

            if ( initialURL != null ) {
                nav.update( initialURL );
            }
        }
        else {
            btn_panel.add( ok_btn );
        }
        panel.add( btn_panel, BorderLayout.SOUTH );
        setContentPane( panel );
        pack();
        setSize( new Dimension( 500, 400 ) );
    }

    /**
     * Overridden to make sure that the top of the document is visible.
     * 
     * @param visible  The new visible value
     */
    public void setVisible( boolean visible ) {
        editor.getCaret().setDot( 0 );
        scrollpane.getViewport().scrollRectToVisible( new Rectangle( 0, 0, 1, 1 ) );
        super.setVisible( visible );
    }

    /**
     * Sets the position attribute of the AboutDialog object
     * 
     * @param ref  The new position value
     */
    public void setPosition( Object ref ) {

        if ( ref instanceof URL ) {

            try {
                editor.setPage( ( URL ) ref );
            }
            catch ( Exception e ) {}
        }
        else if ( ref instanceof String ) {
            editor.scrollToReference( ( String ) ref );
        }
    }

    /**
     * Makes the hyperlinks work, swiped from Swing Connection.
     */
    class LinkListener
        implements HyperlinkListener {

        /** Description of the Field */
        private Cursor hand_cursor = Cursor.getPredefinedCursor(
                    Cursor.HAND_CURSOR );

        /** Description of the Field */
        private Cursor default_cursor = Cursor.getPredefinedCursor(
                    Cursor.DEFAULT_CURSOR );

        /**
         * Description of the Method
         * 
         * @param e
         */
        public void hyperlinkUpdate( HyperlinkEvent e ) {

            JEditorPane pane = ( JEditorPane ) e.getSource();

            if ( e.getEventType() == HyperlinkEvent.EventType.ACTIVATED ) {

                if ( e instanceof HTMLFrameHyperlinkEvent ) {

                    HTMLFrameHyperlinkEvent evt = ( HTMLFrameHyperlinkEvent ) e;
                    HTMLDocument doc = ( HTMLDocument ) pane.getDocument();
                    doc.processHTMLFrameHyperlinkEvent( evt );
                }
                else {

                    try {

                        java.net.URL url = e.getURL();

                        if ( url != null ) {
                            pane.setPage( url );

                            if ( nav != null )
                                nav.update( url );
                        }
                        else {

                            String desc = e.getDescription();
                            desc = desc.substring( 1 );
                            pane.scrollToReference( desc );

                            if ( nav != null )
                                nav.update( desc );
                        }
                    }
                    catch ( Throwable t ) {
                        JOptionPane.showMessageDialog( AboutDialog.this,
                                "Unable to open URL.",
                                "Hyperlink Error",
                                JOptionPane.ERROR_MESSAGE );
                        ;
                    }
                }
            }
            else if ( e.getEventType() == HyperlinkEvent.EventType.ENTERED ) {
                pane.setCursor( hand_cursor );
            }
            else if ( e.getEventType() == HyperlinkEvent.EventType.EXITED ) {
                pane.setCursor( default_cursor );
            }
        }
    }

    // for testing

    /**
     * The main program for the AboutDialog class
     * 
     * @param args  The command line arguments
     */
    public static void main( String[] args ) {

        JFrame frame = new JFrame();
        AboutDialog about = new AboutDialog( frame, "About", "text/html",
                "<html>Here is an About Dialog.</html>",
                false );
        about.setVisible( true );
    }
}
