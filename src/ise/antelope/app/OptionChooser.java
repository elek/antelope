package ise.antelope.app;

import ise.antelope.common.Constants;
import ise.antelope.app.jedit.JEditTextArea;
import ise.antelope.app.jedit.TextAreaPainter;
import ise.antelope.app.jedit.InputHandler;
import ise.library.KappaLayout;
import ise.library.GUIUtils;

import java.awt.Color;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import javax.swing.*;
import javax.swing.border.EmptyBorder;


/**
 * Support these option settings, using the values shown here as the defaults:
 * boolean caretBlinks = true;
 * Color caretColor = Color.black;
 * int electricScroll = 3;
 * boolean useSmartHome = false;
 * Color selectionColor = new Color( 0xccccff ) ;
 * Color lineHighlightColor = new Color( 0xe0e0e0 ) ;
 * boolean showLineHighlight = true;
 * Color bracketHighlightColor = Color.black;
 * boolean showBracketHighlight = false;
 * Color eolMarkerColor = new Color( 0x009999 ) ;
 * boolean showEolMarker = false;
 * textArea.setClientProperty(InputHandler.SMART_HOME_END_PROPERTY, true)
 */
public class OptionChooser extends JDialog implements Constants {

    private JEditTextArea editor;

    private OptionChooser( JFrame parent, JEditTextArea t ) {
        super( parent );
        editor = t;
        setModal( true );
        setResizable( false );
        setTitle( "Editor Options" );

        KappaLayout layout = new KappaLayout();
        JPanel pane = new JPanel( layout );
        pane.setBorder( new EmptyBorder( 11, 11, 11, 11 ) );
        setContentPane( pane );

        final OptionSettings settings = new OptionSettings();
        settings.load();

        final JCheckBox caretBlinks_cb = new JCheckBox( "Caret blinks" );
        final JButton caretColor_btn = new JButton();
        final JSpinner tabSize_sp = new JSpinner( new SpinnerNumberModel( 4, 0, 12, 1 ) );
        final JSpinner electricScroll_sp = new JSpinner( new SpinnerNumberModel( 3, 0, 10, 1 ) );
        final JCheckBox useSmartHome_cb = new JCheckBox( "Use BRIEF-style home/end" );
        final JButton selectionColor_btn = new JButton();
        final JButton lineHighlightColor_btn = new JButton();
        final JCheckBox showLineHighlight_cb = new JCheckBox( "Show line highlight" );
        final JButton bracketHighlightColor_btn = new JButton();
        final JCheckBox showBracketHighlight_cb = new JCheckBox( "Highlight matching brackets" );
        final JButton eolMarkerColor_btn = new JButton();
        final JCheckBox showEolMarker_cb = new JCheckBox( "Show end of line marker" );

        caretBlinks_cb.setSelected( settings.getCaretBlinks() );
        caretColor_btn.setBackground( settings.getCaretColor() );
        useSmartHome_cb.setSelected( settings.useSmartHome() );
        selectionColor_btn.setBackground( settings.getSelectionColor() );
        lineHighlightColor_btn.setBackground( settings.getLineHighlightColor() );
        showLineHighlight_cb.setSelected( settings.showLineHighlight() );
        lineHighlightColor_btn.setEnabled( showLineHighlight_cb.isSelected() );
        bracketHighlightColor_btn.setBackground( settings.getBracketHighlightColor() );
        showBracketHighlight_cb.setSelected( settings.showBracketHighlight() );
        bracketHighlightColor_btn.setEnabled( showBracketHighlight_cb.isSelected() );
        showEolMarker_cb.setSelected( settings.showEolMarker() );
        eolMarkerColor_btn.setBackground( settings.getEolMarkerColor() );
        eolMarkerColor_btn.setEnabled( showEolMarker_cb.isSelected() );

        pane.add( caretBlinks_cb, "0, 0, 1, 1, W, 0,  3" );
        pane.add( new JLabel( "Caret color" ), "0, 1, 1, 1, W, 0,  3" );
        pane.add( caretColor_btn, "1, 1, 1, 1, 0, wh, 3" );
        pane.add( new JLabel( "Tab size" ), "0, 2, 1, 1, W, 0,  3" );
        pane.add( tabSize_sp, "1, 2, 1, 1, 0, wh, 3" );
        pane.add( new JLabel( "Electric scroll height" ), "0, 3, 1, 1, W, 0,  3" );
        pane.add( electricScroll_sp, "1, 3, 1, 1, 0, wh, 3" );
        pane.add( useSmartHome_cb, "0, 4, 1, 1, W, 0,  3" );
        pane.add( new JLabel( "Selection color" ), "0, 5, 1, 1, W, 0,  3" );
        pane.add( selectionColor_btn, "1, 5, 1, 1, 0, wh, 3" );
        pane.add( showLineHighlight_cb, "0, 6, 1, 1, W, 0,  3" );
        pane.add( lineHighlightColor_btn, "1, 6, 1, 1, 0, wh, 3" );
        pane.add( showBracketHighlight_cb, "0, 7, 1, 1, W, 0,  3" );
        pane.add( bracketHighlightColor_btn, "1, 7, 1, 1, 0, wh, 3" );
        pane.add( showEolMarker_cb, "0, 8, 1, 1, W, 0,  3" );
        pane.add( eolMarkerColor_btn, "1, 8, 1, 1, 0, wh, 3" );
        layout.makeRowsSameHeight();

        KappaLayout kl = new KappaLayout();
        JPanel btn_panel = new JPanel( kl );
        JButton ok_btn = new JButton( "OK" );
        JButton apply_btn = new JButton( "Apply" );
        JButton cancel_btn = new JButton( "Cancel" );
        btn_panel.add( ok_btn, "0, 0, 1, 1, 0, wh, 3" );
        btn_panel.add( apply_btn, "1, 0, 1, 1, 0, wh, 3" );
        btn_panel.add( cancel_btn, "2, 0, 1, 1, 0, wh, 3" );
        kl.makeColumnsSameWidth();
        pane.add( KappaLayout.createVerticalStrut( 17 ), "0, 9" );
        pane.add( btn_panel, "0, 10, 2, 1, 0, 0, 6" );

        ActionListener al = new ActionListener() {
                    public void actionPerformed( ActionEvent ae ) {
                        JButton btn = ( JButton ) ae.getSource();
                        Color color = JColorChooser.showDialog( OptionChooser.this,
                                "Pick Color", btn.getBackground() );
                        if ( color != null ) {
                            btn.setBackground( color );
                        }
                    }
                };
        caretColor_btn.addActionListener( al );
        selectionColor_btn.addActionListener( al );
        lineHighlightColor_btn.addActionListener( al );
        bracketHighlightColor_btn.addActionListener( al );
        eolMarkerColor_btn.addActionListener( al );

        showLineHighlight_cb.addActionListener( new ActionListener() {
                    public void actionPerformed( ActionEvent ae ) {
                        lineHighlightColor_btn.setEnabled( showLineHighlight_cb.isSelected() );
                    }
                }
                                              );
        showBracketHighlight_cb.addActionListener( new ActionListener() {
                    public void actionPerformed( ActionEvent ae ) {
                        bracketHighlightColor_btn.setEnabled( showBracketHighlight_cb.isSelected() );
                    }
                }
                                                 );
        showEolMarker_cb.addActionListener( new ActionListener() {
                    public void actionPerformed( ActionEvent ae ) {
                        eolMarkerColor_btn.setEnabled( showEolMarker_cb.isSelected() );
                    }
                }
                                          );

        final ActionListener apply_action = new ActionListener() {
                    public void actionPerformed( ActionEvent ae ) {
                        TextAreaPainter tap = editor.getPainter();
                        tap.setCaretColor( caretColor_btn.getBackground() );
                        editor.setElectricScroll( ( ( Integer ) electricScroll_sp.getValue() ).intValue() );
                        editor.getDocument().putProperty( javax.swing.text.PlainDocument.tabSizeAttribute, ( Integer ) tabSize_sp.getValue() );
                        editor.putClientProperty( InputHandler.SMART_HOME_END_PROPERTY, new Boolean( useSmartHome_cb.isSelected() ) );
                        tap.setSelectionColor( selectionColor_btn.getBackground() );
                        tap.setLineHighlightColor( lineHighlightColor_btn.getBackground() );
                        tap.setLineHighlightEnabled( showLineHighlight_cb.isSelected() );
                        tap.setBracketHighlightEnabled( showBracketHighlight_cb.isSelected() );
                        tap.setBracketHighlightColor( bracketHighlightColor_btn.getBackground() );
                        tap.setEOLMarkersPainted( showEolMarker_cb.isSelected() );
                        tap.setEOLMarkerColor( eolMarkerColor_btn.getBackground() );

                        settings.setCaretColor( caretColor_btn.getBackground( ) );
                        settings.setUseSmartHome( useSmartHome_cb.isSelected( ) );
                        settings.setElectricScroll( ( ( Integer ) electricScroll_sp.getValue() ).intValue() );
                        settings.setTabSize( ( ( Integer ) tabSize_sp.getValue() ).intValue() );
                        settings.setSelectionColor( selectionColor_btn.getBackground( ) );
                        settings.setLineHighlightColor( lineHighlightColor_btn.getBackground( ) );
                        settings.setShowLineHighlight( showLineHighlight_cb.isSelected( ) );
                        settings.setBracketHighlightColor( bracketHighlightColor_btn.getBackground( ) );
                        settings.setShowBracketHighlight( showBracketHighlight_cb.isSelected( ) );
                        settings.setEolMarkerColor( eolMarkerColor_btn.getBackground( ) );
                        settings.setShowEolMarker( showEolMarker_cb.isSelected( ) );
                        settings.save();
                    }
                };
        final ActionListener close_action = new ActionListener() {
                    public void actionPerformed( ActionEvent ae ) {
                        setVisible( false );
                        dispose();
                    }
                };
        ActionListener ok_action = new ActionListener() {
                    public void actionPerformed( ActionEvent ae ) {
                        apply_action.actionPerformed( null );
                        close_action.actionPerformed( null );
                    }
                };
        ok_btn.addActionListener( ok_action );
        apply_btn.addActionListener( apply_action );
        cancel_btn.addActionListener( close_action );

        pack();
        GUIUtils.center( parent, this );
        setVisible( true );
    }

    public static OptionChooser showDialog( JFrame parent, JEditTextArea editor ) {
        OptionChooser c = new OptionChooser( parent, editor );
        return c;
    }
}
