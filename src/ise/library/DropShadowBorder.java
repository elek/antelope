package ise.library;

import java.awt.*;
import javax.swing.border.*;

/**
 * A drop shadow border. Draws a 1 pixel line completely around the component,
 * and a drop shadow effect on the right and bottom sides.
 * @author Dale Anson, 25 Feb 2004
 * @version $Revision$
 */
public class DropShadowBorder extends AbstractBorder {

   private int _width = 3;
   private Color _color = Color.BLACK;

   /**
    * Drop shadow with default width of 3 pixels and black color.
    */
   public DropShadowBorder() {
      this( 3 );
   }

   /**
    * Drop shadow, default shadow color is black.
    * @param width the width of the shadow.
    */
   public DropShadowBorder( int width ) {
      this(width, Color.BLACK);
   }
   
   /**
    * Drop shadow.
    * @param width the width of the shadow.
    * @param color the color of the shadow.
    */
   public DropShadowBorder(int width, Color color) {
      _width = width;
      _color = color;
   }

   public Insets getBorderInsets( Component c ) {
      return new Insets( 1, 1, _width + 1, _width + 1 );
   }

   public Insets getBorderInsets( Component c, Insets insets ) {
      insets.top = 1;
      insets.left = 1;
      insets.bottom = _width + 1;
      insets.right = _width + 1;
      return insets;
   }

   public boolean isBorderOpaque() {
      return true;
   }

   public void paintBorder( Component c, Graphics g, int x, int y, int width, int height ) {
      Color old_color = g.getColor();
      int x1, y1, x2, y2;
      g.setColor( _color );
      
      // outline the component with a 1-pixel wide line
      g.drawRect(x, y, width - _width - 1, height - _width - 1);

      // draw the drop shadow
      for ( int i = 0; i <= _width; i++ ) {
         // bottom shadow
         x1 = x + _width;
         y1 = y + height - i;
         x2 = x + width;
         y2 = y1;
         g.drawLine( x1, y1, x2, y2 );
         
         // right shadow
         x1 = x + width - _width + i;
         y1 = y + _width;
         x2 = x1;
         y2 = y + height;
         g.drawLine( x1, y1, x2, y2 );
      }

      // fill in the corner rectangles with the background color of the parent
      // container
      if ( c.getParent() != null ) {
         g.setColor( c.getParent().getBackground() );
         for ( int i = 0; i <= _width; i++ ) {
            x1 = x;
            y1 = y + height - i;
            x2 = x + _width;
            y2 = y1;
            g.drawLine( x1, y1, x2, y2 );
            x1 = x + width - _width;
            y1 = y + i;
            x2 = x + width ;
            y2 = y1;
            g.drawLine( x1, y1, x2, y2 );
         }
         // add some slightly darker colored triangles
         g.setColor(g.getColor().darker());
         for ( int i = 0; i < _width; i++ ) {
            // bottom left triangle
            x1 = x + i + 1;
            y1 = y + height - _width + i;
            x2 = x + _width;
            y2 = y1;
            g.drawLine( x1, y1, x2, y2 );
            
            // top right triangle
            x1 = x + width - _width;
            y1 = y + i + 1;
            x2 = x1 + i ;
            y2 = y1;
            g.drawLine( x1, y1, x2, y2 );
         }
      }

      g.setColor( old_color );
   }
}
