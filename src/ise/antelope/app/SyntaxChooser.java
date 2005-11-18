/*
*  The Apache Software License, Version 1.1
*
*  Copyright (c) 2000-2002 The Apache Software Foundation.  All rights
*  reserved.
*
*  Redistribution and use in source and binary forms, with or without
*  modification, are permitted provided that the following conditions
*  are met:
*
*  1. Redistributions of source code must retain the above copyright
*  notice, this list of conditions and the following disclaimer.
*
*  2. Redistributions in binary form must reproduce the above copyright
*  notice, this list of conditions and the following disclaimer in
*  the documentation and/or other materials provided with the
*  distribution.
*
*  3. The end-user documentation included with the redistribution, if
*  any, must include the following acknowlegement:
*  "This product includes software developed by the
*  Apache Software Foundation (http://www.apache.org/)."
*  Alternately, this acknowlegement may appear in the software itself,
*  if and wherever such third-party acknowlegements normally appear.
*
*  4. The names "The Jakarta Project", "Ant", and "Apache Software
*  Foundation" must not be used to endorse or promote products derived
*  from this software without prior written permission. For written
*  permission, please contact apache@apache.org.
*
*  5. Products derived from this software may not be called "Apache"
*  nor may "Apache" appear in their names without prior written
*  permission of the Apache Group.
*
*  THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
*  WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
*  OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
*  DISCLAIMED.  IN NO EVENT SHALL THE APACHE SOFTWARE FOUNDATION OR
*  ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
*  SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
*  LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
*  USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
*  ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
*  OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
*  OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
*  SUCH DAMAGE.
*  ====================================================================
*
*  This software consists of voluntary contributions made by many
*  individuals on behalf of the Apache Software Foundation.  For more
*  information on the Apache Software Foundation, please see
*  <http://www.apache.org/>.
*/
package ise.antelope.app;

import ise.antelope.common.Constants;
import ise.antelope.app.jedit.*;
import ise.library.GUIUtils;
import ise.library.KappaLayout;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.border.EmptyBorder;

/**
 * Allows the user to select a style for a particular syntax type.
 * @author Dale Anson, Dec 2002
 */
public class SyntaxChooser extends JDialog {

    private JButton ok_btn;
    private StyleButton comment1_disp = null;
    private StyleButton comment2_disp = null;
    private StyleButton keyword1_disp = null;
    private StyleButton keyword2_disp = null;
    private StyleButton keyword3_disp = null;
    private StyleButton literal1_disp = null;
    private StyleButton literal2_disp = null;
    private StyleButton label_disp = null;
    private StyleButton operator_disp = null;
    private StyleButton invalid_disp = null;
    private SyntaxStyle[] styles = null;

    private SyntaxChooser( JFrame parent ) {
        super( parent );
        setModal( true );
        setResizable( false );
        setTitle( "Syntax Highlighting" );

        JLabel comment1_lbl = new JLabel( "Comment 1" );
        JLabel comment2_lbl = new JLabel( "Comment 2" );
        JLabel keyword1_lbl = new JLabel( "Tag name" );
        JLabel keyword2_lbl = new JLabel( "Attribute" );
        JLabel keyword3_lbl = new JLabel( "Inside PI" );
        JLabel literal1_lbl = new JLabel( "String" );
        JLabel literal2_lbl = new JLabel( "String" );
        JLabel label_lbl = new JLabel( "Entity" );
        JLabel operator_lbl = new JLabel( "Operator" );
        JLabel invalid_lbl = new JLabel( "Invalid" );

        SyntaxStyle[] ss = AntelopeSyntaxUtilities.getStoredStyles();
        comment1_disp = new StyleButton( "comment", ss[ Token.COMMENT1 ] );
        comment2_disp = new StyleButton( "comment", ss[ Token.COMMENT2 ] );
        keyword1_disp = new StyleButton( "target ", ss[ Token.KEYWORD1 ] );
        keyword2_disp = new StyleButton( "name", ss[ Token.KEYWORD2 ] );
        keyword3_disp = new StyleButton( "some text", ss[ Token.KEYWORD3 ] );
        literal1_disp = new StyleButton( "\"some text\"", ss[ Token.LITERAL1 ] );
        literal2_disp = new StyleButton( "'some text'", ss[ Token.LITERAL2 ] );
        label_disp = new StyleButton( "&amp;", ss[ Token.LABEL ] );
        operator_disp = new StyleButton( "=", ss[ Token.OPERATOR ] );
        invalid_disp = new StyleButton( "invalid", ss[ Token.INVALID ] );

        ActionListener al = new ActionListener() {
                    public void actionPerformed( ActionEvent ae ) {
                        StyleButton btn = ( StyleButton ) ae.getSource();
                        StyleEditor se = new StyleEditor( SyntaxChooser.this, btn.getStyle() );
                        SyntaxStyle ss = se.getStyle();
                        if ( ss != null ){
                            btn.setStyle( ss );
                        }
                    }
                };

        comment1_disp.addActionListener(al);
        comment2_disp.addActionListener(al);
        keyword1_disp.addActionListener(al);
        keyword2_disp.addActionListener(al);
        keyword3_disp.addActionListener(al);
        literal1_disp.addActionListener(al);
        literal2_disp.addActionListener(al);
        label_disp.addActionListener(al);   
        operator_disp.addActionListener(al);
        invalid_disp.addActionListener(al); 

        KappaLayout layout = new KappaLayout();
        JPanel pane = new JPanel( layout );
        setContentPane( pane );
        pane.setBorder( new EmptyBorder( 6, 6, 6, 6 ) );

        pane.add( comment1_lbl, "0, 0, 1, 1, W, wh, 0" );
        pane.add( comment2_lbl, "0, 1, 1, 1, W, wh, 0" );
        pane.add( keyword1_lbl, "0, 2, 1, 1, W, wh, 0" );
        pane.add( keyword2_lbl, "0, 3, 1, 1, W, wh, 0" );
        pane.add( keyword3_lbl, "0, 4, 1, 1, W, wh, 0" );
        pane.add( literal1_lbl, "0, 5, 1, 1, W, wh, 0" );
        pane.add( literal2_lbl, "0, 6, 1, 1, W, wh, 0" );
        pane.add( label_lbl, "0, 7, 1, 1, W, wh, 0" );
        pane.add( operator_lbl, "0, 8, 1, 1, W, wh, 0" );
        pane.add( invalid_lbl, "0, 9, 1, 1, W, wh, 0" );
        pane.add( comment1_disp, "1, 0, 1, 1, W, wh, 0" );
        pane.add( comment2_disp, "1, 1, 1, 1, W, wh, 0" );
        pane.add( keyword1_disp, "1, 2, 1, 1, W, wh, 0" );
        pane.add( keyword2_disp, "1, 3, 1, 1, W, wh, 0" );
        pane.add( keyword3_disp, "1, 4, 1, 1, W, wh, 0" );
        pane.add( literal1_disp, "1, 5, 1, 1, W, wh, 0" );
        pane.add( literal2_disp, "1, 6, 1, 1, W, wh, 0" );
        pane.add( label_disp, "1, 7, 1, 1, W, wh, 0" );
        pane.add( operator_disp, "1, 8, 1, 1, W, wh, 0" );
        pane.add( invalid_disp, "1, 9, 1, 1, W, wh, 0" );

        layout.makeColumnsSameWidth( 0, 1 );

        layout = new KappaLayout();
        JPanel btn_panel = new JPanel( layout );
        ok_btn = new JButton( "OK" );
        JButton cancel_btn = new JButton( "Cancel" );
        btn_panel.add( ok_btn, "0, 0, 1, 1, 0, wh, 3" );
        btn_panel.add( cancel_btn, "1, 0, 1, 1, 0, wh, 3" );
        layout.makeColumnsSameWidth( 0, 1 );
        pane.add( btn_panel, "0, 10, 2, 1, 0, , 6" );

        ok_btn.addActionListener( new ActionListener() {
                    public void actionPerformed( ActionEvent ae ) {
                        styles = AntelopeSyntaxUtilities.getStoredStyles();
                        styles[ Token.COMMENT1 ] = comment1_disp.getStyle();
                        styles[ Token.COMMENT2 ] = comment2_disp.getStyle();
                        styles[ Token.KEYWORD1 ] = keyword1_disp.getStyle();
                        styles[ Token.KEYWORD2 ] = keyword2_disp.getStyle();
                        styles[ Token.KEYWORD3 ] = keyword3_disp.getStyle();
                        styles[ Token.LITERAL1 ] = literal1_disp.getStyle();
                        styles[ Token.LITERAL2 ] = literal2_disp.getStyle();
                        styles[ Token.LABEL ] = label_disp .getStyle();
                        styles[ Token.OPERATOR ] = operator_disp.getStyle();
                        styles[ Token.INVALID ] = invalid_disp .getStyle();
                        AntelopeSyntaxUtilities.storeStyles( styles );
                        setVisible( false );
                        dispose();
                    }
                }
                                );
        cancel_btn.addActionListener( new ActionListener() {
                    public void actionPerformed( ActionEvent ae ) {
                        styles = null;
                        setVisible( false );
                        dispose();
                    }
                }
                                    );

        pack();
        GUIUtils.center( parent, this );
        setVisible( true );
    }

    public SyntaxStyle[] getStyles() {
        return styles;
    }

    public static SyntaxChooser showDialog( JFrame parent ) {
        SyntaxChooser c = new SyntaxChooser( parent );
        return c;
    }

    public class StyleButton extends JButton {
        private SyntaxStyle style = null;
        public StyleButton( String title, SyntaxStyle ss ) {
            super( title );
            setStyle( ss );
        }
        public void setStyle( SyntaxStyle ss ) {
            style = ss;
            this.setForeground(ss.getColor());
            this.setBackground(Color.white);
            this.setFont( ss.getStyledFont( this.getFont() ) );
        }
        public SyntaxStyle getStyle() {
            return style;
        }
    }
}
