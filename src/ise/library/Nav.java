
package ise.library;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.*;
import java.util.*;

/**
 * Provides navigation ability for a client object, has a "back" and "forward"
 * button to move through a list of objects.
 *
 * @author   Dale Anson, danson@germane-software.com, August 2002
 */
public class Nav extends JToolBar implements ActionListener {
   /**
    * Action command to go to the previous item.
    */
   public final static String BACK = "back";
   /**
    * Action command to go to the next item.
    */
   public final static String FORWARD = "forward";
   /**
    * Action command to indicate that it is okay to go back.
    */
   public final static String CAN_GO_BACK = "canGoBack";
   /**
    * Action command to indicate that it is not okay to go back.
    */
   public final static String CANNOT_GO_BACK = "cannotGoBack";
   /**
    * Action command to indicate that it is okay to go forward.
    */
   public final static String CAN_GO_FORWARD = "canGoForward";
   /**
    * Action command to indicate that it is not okay to go forward.
    */
   public final static String CANNOT_GO_FORWARD = "cannotGoForward";

   private JButton back, forward;
   private Stack backStack;
   private Stack forwardStack;
   private Object currentNode = null;
   private Navable client;

   /**
    * @param client  the client object to provide navigation for
    */
   public Nav( Navable client ) {
      if ( client == null )
         throw new IllegalArgumentException( "client cannot be null" );
      this.client = client;
      setFloatable( false );
      putClientProperty( "JToolBar.isRollover", Boolean.TRUE );

      // set up the buttons
      back = new JButton( new ImageIcon(getClass().getClassLoader().getResource("images/Back16.gif")) );
      forward = new JButton( new ImageIcon(getClass().getClassLoader().getResource("images/Forward16.gif")) );
      back.setMargin( new Insets( 0, 0, 0, 0 ) );
      forward.setMargin( new Insets( 0, 0, 0, 0 ) );
      back.setActionCommand( BACK );
      forward.setActionCommand( FORWARD );
      back.addActionListener( this );
      forward.addActionListener( this );
      add( back );
      add( forward );

      // set up the history stacks
      backStack = new Stack();
      forwardStack = new Stack();
      clearStacks();
   }

   /**
    * The action handler for this class. Actions can be invoked by calling this
    * method and passing an ActionEvent with one of the action commands defined
    * in this class (BACK, FORWARD, etc).
    *
    * @param ae  the action event to kick a response.
    */
   public void actionPerformed( ActionEvent ae ) {
      if ( ae.getActionCommand().equals( BACK ) ) {
         goBack();
      }
      else if ( ae.getActionCommand().equals( FORWARD ) ) {
         goForward();
      }
      else if ( ae.getActionCommand().equals( CAN_GO_BACK ) ) {
         back.setEnabled( true );
      }
      else if ( ae.getActionCommand().equals( CANNOT_GO_BACK ) ) {
         back.setEnabled( false );
      }
      else if ( ae.getActionCommand().equals( CAN_GO_FORWARD ) ) {
         forward.setEnabled( true );
      }
      else if ( ae.getActionCommand().equals( CANNOT_GO_FORWARD ) ) {
         forward.setEnabled( false );
      }
   }

   /**
    * Clears the history.
    */
   public void clearStacks() {
      backStack.clear();
      forwardStack.clear();
      setButtonState();
   }

   /**
    * Moves to the previous item in the "back" history.
    */
   public void goBack() {
      if ( !backStack.empty() ) {
         if ( currentNode != null )
            forwardStack.push( currentNode );
         currentNode = backStack.pop();
         navigate( currentNode );
      }
      setButtonState();
   }

   /**
    * Moves to the next item in the "forward" history.
    */
   public void goForward() {
      if ( !forwardStack.empty() ) {
         if ( currentNode != null )
            backStack.push( currentNode );
         currentNode = forwardStack.pop();
         navigate( currentNode );
      }
      setButtonState();
   }

   /**
    * Invokes the client to go to the given object.
    *
    * @param node  where to go
    */
   private void navigate( Object node ) {
      client.setPosition( node );
   }

   /**
    * Sets the state of the navigation buttons.
    */
   private void setButtonState() {
      back.setEnabled( !backStack.empty() );
      forward.setEnabled( !forwardStack.empty() );
   }

   /**
    * Updates the stacks and button state based on the given node. Pushes the
    * node on to the "back" history, clears the "forward" history.
    *
    * @param node  an object
    */
   public void update( Object node ) {
      if ( node != currentNode ) {
         if ( currentNode != null )
            backStack.push( currentNode );
         currentNode = node;
         forwardStack.clear();
      }
      setButtonState();
   }
}

