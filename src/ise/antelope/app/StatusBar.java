package ise.antelope.app;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.*;
import ise.library.*;

/**
 * Simple status bar for Antelope. Provides an area for showing status messages
 * and the line offset and total line count of the build file.
 * @author Dale Anson
 */
public class StatusBar extends JPanel {

   private JTextField _field;
   private JTextField _line;
   private LambdaLayout _layout;
   public StatusBar() {
      _layout = new LambdaLayout();
      setLayout( _layout );
      _field = new JTextField( 80 );
      _field.setEditable( false );
      _field.setBackground( Color.WHITE );
      _line = new JTextField();
      _line.setEditable( false );
      _line.setBackground( Color.WHITE );
      add( _field, "0, 0, 1, 1, 0, wh, 1" );
      add( _line, "1, 0, R, 1, 0, wh, 1" );
   }

   public void setStatus( String status ) {
      if ( status == null )
         status = "";
      _field.setText( status );
      _layout.layoutContainer( this );
   }

   public String getStatus() {
      return _field.getText();
   }

   public void setLine( int line, int total ) {
      _line.setText( String.valueOf( line ) + ":" + String.valueOf( total ) );
      _layout.layoutContainer( this );
   }

   public int getLine() {
      return Integer.parseInt( _line.getText() );
   }
}
