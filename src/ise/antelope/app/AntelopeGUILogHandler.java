// $Id$

package ise.antelope.app;

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
import java.util.logging.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.text.*;

import ise.library.*;
import ise.library.Log;

/**
 * A simple log handler for Antelope that shows the Ant output in a GUI.
 * The output can either be in a separate frame, or applications can get
 * the textarea to position themselves.
 *
 * @author    Dale Anson, danson@germane-software.com
 * @created   July 22, 2002
 */
public class AntelopeGUILogHandler extends Handler {

    /**
     * The output area.
     */
    private JTextPane _text;

    private boolean _tail = true;

    private JPanel _content_pane;
    /**
     * Optional frame
     */
    private JFrame _frame;
    /**
     * Green
     */
    private Color GREEN = new Color( 0, 153, 51 );
    /**
     * Current font
     */
    private Font _font = null;

    /**
     * Constructor for the AntelopeGUILogHandler object
     */
    public AntelopeGUILogHandler() {
        this( false );
    }

    /**
     *Constructor for the AntelopeGUILogHandler object
     *
     * @param use_frame  If true, will show the output in a separate frame.
     */
    public AntelopeGUILogHandler( boolean use_frame ) {
        Log.log("AntelopeGUILogHandler constructor");
        _content_pane = new JPanel(new BorderLayout());

        _text = new JTextPane();
        _text.setCaretPosition(0);
        _content_pane.add(new JScrollPane(_text), BorderLayout.CENTER);
        _content_pane.add(getControlPanel(), BorderLayout.SOUTH);

        if ( use_frame ) {
            _frame = new JFrame( "Antelope Logger" );
            _frame.addWindowListener(
                new WindowAdapter() {
                    public void windowClosing( WindowEvent we ) {
                        _frame.setVisible( false );
                    }
                }
            );
            _frame.getContentPane().add( _content_pane );
            _frame.setSize( 600, 400 );
            GUIUtils.centerOnScreen( _frame );
        }
        setFormatter( new LogFormatter() );
    }

    /**
     * Description of the Class
     */
    public class LogFormatter extends Formatter {
        /**
         * Description of the Method
         *
         * @param record
         * @return        Description of the Returned Value
         */
        public String format( LogRecord record ) {
            return record.getMessage() + System.getProperty( "line.separator" );
        }
    }

    /**
     * Gets the textArea attribute of the AntelopeGUILogHandler object
     *
     * @return   The textArea value
     */
    public JTextComponent getTextComponent() {
        return _text;
    }

    public Document getDocument() {
        return _text.getDocument();
    }

    public JPanel getPanel() {
        return _content_pane;
    }

    /**
     * Sets the font attribute of the AntelopeGUILogHandler object
     *
     * @param font  The new font value
     */
    public void setFont( Font font ) {
        _font = font;
    }

    /**
     * Sets the visible attribute of the optional frame.
     *
     * @param b  The new visible value
     */
    public void setVisible( boolean b ) {
        _frame.setVisible( b );
    }

    /**
     * Disposes of the optional frame.
     */
    public void dispose() {
        _frame.dispose();
    }

    /**
     * Sets the location attribute of the optional frame.
     *
     * @param x  The new location value
     * @param y  The new location value
     */
    public void setLocation( int x, int y ) {
        _frame.setLocation( x, y );
    }

    /**
     * Sets the bounds attribute of the optional frame.
     *
     * @param x  The new bounds value
     * @param y  The new bounds value
     * @param w  The new bounds value
     * @param h  The new bounds value
     */
    public void setBounds( int x, int y, int w, int h ) {
        _frame.setBounds( x, y, w, h );
    }

    /**
     * Gets the size attribute of the optional frame.
     *
     * @return   The size value
     */
    public Dimension getSize() {
        return _frame.getSize();
    }

    /**
     * Finish out the log.
     */
    public void close() {
        if ( getFormatter() != null )
            publish( new LogRecord( Level.INFO, getFormatter().getTail( this ) ) );
    }

    /**
     * Does nothing.
     */
    public void flush() {}

    /**
     * Starts the log.
     */
    public void open() {
        Log.log("gui log handler.open");
        if ( getFormatter() != null ) {
            int index = _text.getDocument().getLength();
            try {
                _text.getDocument().insertString( index, getFormatter().getHead( AntelopeGUILogHandler.this ), null );
            }
            catch ( Exception e ) {
                Log.log(e);
            }
        }
    }

    /**
     * Appends the given record to the GUI.
     *
     * @param lr  the LogRecord to write.
     */
    public void publish( final LogRecord lr ) {
        try {
            SwingUtilities.invokeLater(
                new Runnable() {
                    public void run() {
                        String msg = lr.getMessage();
                        if ( msg == null )
                            return ;
                        if ( getFormatter() != null )
                            msg = getFormatter().format( lr );
                        if ( _text == null ) {
                            return ;
                        }
                        try {
                            int index = _text.getDocument().getLength();
                            int caret_position = _text.getCaretPosition();
                            SimpleAttributeSet set = new SimpleAttributeSet();
                            if ( _font == null ) {
                                StyleConstants.setFontFamily( set, "Monospaced" );
                            }
                            else {
                                StyleConstants.setFontFamily( set, _font.getFamily() );
                                StyleConstants.setBold( set, _font.isBold() );
                                StyleConstants.setItalic( set, _font.isItalic() );
                                StyleConstants.setFontSize( set, _font.getSize() );
                            }
                            if ( lr.getLevel().equals( Level.WARNING ) )
                                StyleConstants.setForeground( set, GREEN );
                            else if ( lr.getLevel().equals( Level.SEVERE ) )
                                StyleConstants.setForeground( set, Color.RED );
                            else if ( lr.getLevel().equals( Level.INFO ) )
                                StyleConstants.setForeground( set, Color.BLUE );
                            _text.getDocument().insertString( index, msg, set );
                            if (_tail)
                                _text.setCaretPosition( index + msg.length() );
                            else 
                                _text.setCaretPosition(caret_position);
                        }
                    catch ( Exception e ) {
                        Log.log(e);
                    }
                    }
                }
            );
        }
        catch ( Exception ignored ) {
            // ignored
            Log.log(ignored);
        }
    }

    private JPanel getControlPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        final JCheckBox tail_cb = new JCheckBox("Tail");
        tail_cb.setSelected(true);
        tail_cb.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent ae) {
                        _tail = tail_cb.isSelected();
                        if (_tail)
                            _text.setCaretPosition(_text.getDocument().getLength());
                    }
                }
                                 );
        panel.add(tail_cb);
        return panel;
    }
}

