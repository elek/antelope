package ise.antelope.app;

import java.awt.*;
import java.awt.event.*;
import java.util.regex.*;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.text.JTextComponent;
import ise.library.*;

/**
 * A dialog for searching in the output panel.
 *
 * @author    Dale Anson, danson@germane-software.com
 * @version   $Revision$
 */
public class FindDialog extends JDialog {

   private JTextComponent textarea = null;


   /**
    * Constructor for FindDialog
    *
    * @param type
    * @param ta      Description of the Parameter
    */
   public FindDialog( JFrame parent, JTextComponent ta ) {
      super( parent, "Find in Output", true );
      this.textarea = ta;
      textarea.requestFocus();

      JPanel panel = new JPanel();
      KappaLayout layout = new KappaLayout();
      panel.setLayout( layout );
      panel.setBorder( new javax.swing.border.EmptyBorder( 11, 11, 11, 11 ) );
      setContentPane( panel );

      JLabel find_label = new JLabel( "Find:" );
      final JTextField to_find = new JTextField( 20 );
      JButton find_btn = new JButton( "Find" );
      JButton find_next_btn = new JButton( "Find Next" );
      JButton cancel_btn = new JButton( "Close" );
      final JCheckBox wrap_cb = new JCheckBox( "Wrap search" );
      wrap_cb.setSelected(true);

      panel.add( find_label, "0, 0, 1, 1, W, w, 3" );
      panel.add( to_find, "0, 1, 1, 1, 0, w, 3" );
      panel.add( wrap_cb, "0, 2, 1, 1, 0, w, 3" );

      JPanel btn_panel = new JPanel( new KappaLayout() );
      btn_panel.add( find_btn, "0, 0, 1, 1, 0, w, 3" );
      btn_panel.add( find_next_btn, "0, 1, 1, 1, 0, w, 3" );
      btn_panel.add( cancel_btn, "0, 2, 1, 1, 0, w, 3" );
      panel.add( btn_panel, "1, 0, 1, 3, 0, h, 5" );

      find_btn.addActionListener(
         new ActionListener() {
            public void actionPerformed( ActionEvent ae ) {
               String text_to_find = to_find.getText();
               if ( text_to_find == null || text_to_find.length() == 0 ) {
                  return;
               }
               try {
                  String doc = textarea.getDocument().getText(0, textarea.getDocument().getLength());
                  Pattern pattern = Pattern.compile( text_to_find, Pattern.DOTALL );
                  Matcher matcher = pattern.matcher( doc );
                  if ( matcher.find() ) {
                     int start = matcher.start();
                     int end = matcher.end();
                     String found = doc.substring( start, end );
                     textarea.setCaretPosition( start );
                     textarea.select( start, end );
                  }
               }
               catch ( Exception e ) {
                  e.printStackTrace();
               }
            }
         } );
      find_next_btn.addActionListener(
         new ActionListener() {
            public void actionPerformed( ActionEvent ae ) {
               String text_to_find = to_find.getText();
               if ( text_to_find == null || text_to_find.length() == 0 ) {
                  return;
               }
               try {
                  int initial_caret = textarea.getCaretPosition();
                  int caret = initial_caret;
                  String doc = textarea.getDocument().getText(0, textarea.getDocument().getLength());
                  Pattern pattern = Pattern.compile( text_to_find, Pattern.DOTALL );
                  Matcher matcher = pattern.matcher( doc );
                  if ( matcher.find() ) {
                     boolean done = false;
                     while ( !done ) {
                        matcher = pattern.matcher( doc );
                        while ( matcher.find() ) {
                           int start = matcher.start();
                           int end = matcher.end();
                           if ( start < caret ) {
                              continue;
                           }
                           caret = end;
                           String found = doc.substring( start, end );
                           textarea.setCaretPosition( start );
                           textarea.select( start, end );
                           done = true;
                           break;
                        }
                        if ( wrap_cb.isSelected() ) {
                           caret = 0;
                        }
                        else {
                           break;
                        }
                     }
                  }
               }
               catch ( Exception e ) {
                  e.printStackTrace();
               }
            }
         } );

      cancel_btn.addActionListener(
         new ActionListener() {
            public void actionPerformed( ActionEvent ae ) {
               setVisible( false );
               dispose();
            }
         } );
      pack();
      to_find.requestFocus();
   }
}

