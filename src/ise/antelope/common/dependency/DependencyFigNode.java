
package ise.antelope.common.dependency;

import java.awt.*;
import java.util.*;

import ise.antelope.common.*;

import org.tigris.gef.presentation.*;
import org.tigris.gef.graph.presentation.*;

public class DependencyFigNode extends FigNode {
   Fig border, circle;
   FigText label;
   private Color GREEN = new Color( 0, 153, 51 );

   public DependencyFigNode(Object owner) {
      super();
      setOwner(owner);
      //border = new FigRect( -25, -25, 50, 50, Color.black, Color.white );
      
      Color outer = Color.black;
      Color inner = Color.black;

      SAXTreeNode node = ((DependencyNode)getOwner()).getNode();
      if (node != null) {
         if (node.isPrivate()) {
            outer = Color.gray;
            inner = Color.gray;
         }
         
         if (node.isImported())
            inner = Color.red;
         
         if (node.isDefaultTarget()) {
            outer = GREEN;
         }
         
      }
      else
         throw new RuntimeException("node is null!");
      
      
      
      
      circle = new FigCircle( -20, -20, 40, 40, outer, outer );
      FigCircle wc = new FigCircle(-15, -15, 30, 30, Color.white, Color.white);
      FigCircle rc = new FigCircle(-10, -10, 20, 20, inner, inner);
      
      label = new FigText( -20, 20, 40, 40 );
      label.setText(node.getAttributeValue("name"));
      label.setLineWidth( 0 );
      label.setFilled(false);
      label.setJustification( FigText.JUSTIFY_CENTER );
      
      //addFig(border);
      addFig(circle);
      addFig(wc);
      addFig(rc);
      addFig(label);
   }
   
   public boolean isResizable() {
      return false;  
   }
   
   public Fig deepSelect(Rectangle r) {
      return this;
   }
   
   public void setOwner( Object own ) {
      super.setOwner( own );
      if ( !( own instanceof DependencyNode ) )
         return ;
   }

   public void addPort( NetPort port ) {
      Fig fig = new DependencyFigCircle( 0, 0, 10, 10, Color.red, Color.red );
      fig.setVisible(false);
      addFig( fig );
      bindPort( port, fig );
      alignPorts();
   }

   private void alignPorts() {
      ArrayList figs = ( ArrayList ) getFigs( new ArrayList() );
      ArrayList port_figs = new ArrayList();
      Iterator it = figs.iterator();
      while ( it.hasNext() ) {
         Fig fig = ( Fig ) it.next();
         if ( fig instanceof DependencyFigCircle ) {
            port_figs.add( fig );
         }
      }
      double interval = ( double ) 360 / ( double ) port_figs.size();
      if ( interval == 360 )
         interval = 0;
      double radius = 20;
      it = port_figs.iterator();
      Rectangle r = circle.getBounds();
      int cx = r.x + ( r.width / 2 );
      int cy = r.y + ( r.height / 2 );
      
      for ( int i = 0; it.hasNext(); i++ ) {
         Fig fig = ( Fig ) it.next();
         fig.setBounds(cx - 5, cy - 5, 10, 10);
      }
   }

   public String getPrivateData() {
      return "text=\"" + label.getText() + "\"";
   }
   
   public void setPrivateData( String data ) {
      StringTokenizer tokenizer = new StringTokenizer( data, "=\"' " );

      while ( tokenizer.hasMoreTokens() ) {
         String tok = tokenizer.nextToken();
         if ( tok.equals( "text" ) ) {
            String s = tokenizer.nextToken();
            label.setText( s );
         }
      }
   }
}
