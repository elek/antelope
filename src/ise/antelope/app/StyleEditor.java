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
import ise.antelope.app.jedit.SyntaxStyle;

import ise.library.KappaLayout;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.border.EmptyBorder;

/**
 * Allows the user to select a style for a particular syntax type.
 * @author Dale Anson, Dec 2002
 */
public class StyleEditor extends JDialog {

    private JCheckBox italic_cb;
    private JCheckBox bold_cb;
    private JButton text_color_btn;
    private JButton ok_btn;
    private JButton cancel_btn;
    private SyntaxStyle style = null;

    public StyleEditor( JDialog parent, SyntaxStyle ss ) {
        super( parent );
        setModal( true );
        setResizable( false );
        setTitle( "Set Style" );

        JPanel pane = new JPanel();
        pane.setLayout( new KappaLayout() );
        setContentPane( pane );
        pane.setBorder( new EmptyBorder( 6, 6, 6, 6 ) );

        italic_cb = new JCheckBox( "Italic" );
        italic_cb.setSelected( ss.isItalic() );
        bold_cb = new JCheckBox( "Bold" );
        bold_cb.setSelected( ss.isBold() );
        JLabel label = new JLabel( "Text color:" );
        text_color_btn = new JButton();
        text_color_btn.setBackground( ss.getColor() );
        ok_btn = new JButton( "OK" );
        cancel_btn = new JButton( "Cancel" );

        pane.add( italic_cb, "0, 0, 3, 1, W, , 3" );
        pane.add( bold_cb, "0, 1, 3, 1, W, , 3" );
        pane.add( label, "0, 2, 2, 1, W, , 3" );
        pane.add( text_color_btn, "2, 2, 1, 1, 0, wh, 3" );
        pane.add( KappaLayout.createVerticalStrut( 11 ), "0, 3" );

        KappaLayout layout = new KappaLayout();
        JPanel btn_panel = new JPanel( layout );
        btn_panel.add( ok_btn, "0, 0, 1, 1, 0, wh, 3" );
        btn_panel.add( cancel_btn, "1, 0, 1, 1, 0, wh, 3" );
        layout.makeColumnsSameWidth( 0, 1 );
        pane.add( btn_panel, "0, 4, 3, 1, 0, , 6" );

        text_color_btn.addActionListener( new ActionListener() {
                    public void actionPerformed( ActionEvent ae ) {
                        Color color = JColorChooser.showDialog( StyleEditor.this,
                                "Pick Color", text_color_btn.getBackground() );
                        if (color != null)
                            text_color_btn.setBackground( color );
                    }
                }
                                        );

        ok_btn.addActionListener( new ActionListener() {
                    public void actionPerformed( ActionEvent ae ) {
                        style = new SyntaxStyle( text_color_btn.getBackground(),
                                italic_cb.isSelected(),
                                bold_cb.isSelected() );
                        setVisible( false );
                    }
                }
                                );

        cancel_btn.addActionListener( new ActionListener() {
                    public void actionPerformed( ActionEvent ae ) {
                        style = null;
                        setVisible( false );
                    }
                }
                                    );
        pack();
        Rectangle pb = parent.getBounds();
        setLocation( pb.x + pb.width, pb.y );
        setVisible( true );
    }

    public SyntaxStyle getStyle() {
        return style;
    }
}
