
package ise.antelope.common.dependency;

import java.awt.Color;
import java.util.*;
import org.tigris.gef.base.*;
import org.tigris.gef.demo.*;
import org.tigris.gef.graph.presentation.*;
import org.tigris.gef.presentation.*;

public class DependencyEdge extends NetEdge {

   private Hashtable _attrs = null;
   private String _label = null;

   public DependencyEdge() {
      super();
   }

   public DependencyEdge( String label ) {
      super();
      _label = label;
   }

   public String getLabel() {
      return _label;
   }

   public void setLabel( String label ) {
      _label = label;
   }

   public void initialize( Hashtable args ) {
      _attrs = args;
   }

   public String getId() {
      return toString();
   }

   public Object getAttributes() {
      return _attrs;
   }

   public Object getAttribute( Object key ) {
      return _attrs.get( key );
   }

   public Object setAttribute( Object key, Object value ) {
      return _attrs.put( key, value );
   }

   public FigEdge makePresentation( Layer lay ) {
      FigEdge line = new FigEdgePoly();
      if ( _label != null ) {
         FigText label = new FigText( 10, 30, 90, 20 );
         label.setText( _label );
         label.setTextColor( Color.black );
         label.setTextFilled( false );
         label.setFilled( false );
         label.setLineWidth( 0 );
         line.addPathItem( label, new PathConvPercent( line, 50, 10 ) );
      }
      line.setDestArrowHead( new MidLineArrowHead(20) );
      return line;
   }

}
