
package ise.antelope.common.dependency;

import java.awt.Color;
import java.util.*;
import org.tigris.gef.base.*;
import org.tigris.gef.demo.*;
import org.tigris.gef.graph.presentation.*;
import org.tigris.gef.presentation.*;
import ise.antelope.common.*;

public class DependencyNode extends NetNode {

   private Hashtable _attrs = null;
   private String _id = "";
   private SAXTreeNode _node = null;
   private DependencyFigNode _fig_node = null;

   public DependencyNode(SAXTreeNode node) {
      _node = node;
      _id = node.getAttributeValue("name");
   }

   public SAXTreeNode getNode() {
      return _node;  
   }
   
   public void initialize( Hashtable args ) {
      _attrs = args;
   }
   
   public void addPort(NetPort port) {
      super.addPort(port);
      activatePort(port);
   }

   public void activatePort(NetPort port) {
      if (_fig_node == null)
         makePresentation(null);
      _fig_node.addPort(port);
   }

   public String getId() {
      return _id;
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

   public FigNode makePresentation( Layer lay ) {
      if ( _fig_node == null )
         _fig_node = new DependencyFigNode(this);
      return _fig_node;
   }
}
