package ise.antelope.common.dependency;

import java.awt.*;
import org.tigris.gef.base.*;
import org.tigris.gef.presentation.*;

/**
 * An arrow head that can be placed in the middle of a line rather than at the
 * ends. Of course, setting offset to 0 will put the arrow head at the end anyway.
 * @author Dale Anson, April 2004
 */
public class MidLineArrowHead extends ArrowHeadTriangle {

   private int _offset = 0;

   /**
    * @param offset the offset from the end of the line in pixels to place the
    * arrowhead.
    */
   public MidLineArrowHead( int offset ) {
      _offset = offset;
   }

   /**
    * Overridden to position the arrow head at the specified offset.   
    */
   public void paintAtHead( Graphics g, Fig path ) {
      Graphics2D g2 = ( Graphics2D ) g;
      Stroke oldStroke = g2.getStroke();
      g2.setStroke( new BasicStroke( path.getLineWidth() ) );
      paint( g2, path.pointAlongPerimeter( _offset + 5 ), path.pointAlongPerimeter( _offset ) );
      g2.setStroke( oldStroke );
   }

   /**
    * Overridden to position the arrow head at the specified offset.   
    */
   public void paintAtTail( Graphics g, Fig path ) {
      Graphics2D g2 = ( Graphics2D ) g;
      Stroke oldStroke = g2.getStroke();
      g2.setStroke( new BasicStroke( path.getLineWidth() ) );
      int len = path.getPerimeterLength();
      paint( g2, path.pointAlongPerimeter( len - ( _offset + 5 ) ), path.pointAlongPerimeter( len - _offset ) );
      g2.setStroke( oldStroke );
   }

}
