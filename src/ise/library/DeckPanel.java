package ise.library;

import java.awt.CardLayout;
import java.awt.Component;
import javax.swing.JPanel;

/**
 * This panel uses a CardLayout and provides a simple wrapper to the 
 * card layout methods for showing a particular card.
 * @author Dale Anson, danson@germane-software.com
 */
public class DeckPanel extends JPanel {

   private CardLayout _layout;

   public DeckPanel() {
      _layout = new CardLayout();
      setLayout( _layout );
   }

   public void first() {
      _layout.first( this );
      repaint();
   }

   public void last() {
      _layout.last( this );
      repaint();
   }

   public void next() {
      _layout.next( this );
      repaint();
   }

   public void previous() {
      _layout.previous( this );
      repaint();
   }

   public void show( String name ) {
      _layout.show( this, name );
      repaint();
   }
}
