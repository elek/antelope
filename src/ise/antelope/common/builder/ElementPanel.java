package ise.antelope.common.builder;

import java.awt.*;
import java.awt.event.*;
import java.util.*;
import javax.swing.*;
import javax.swing.tree.TreePath;
import ise.library.*;
import com.wutka.dtd.*;


public class ElementPanel extends JPanel implements java.io.Serializable {

   private JList list = null;
   private TreePath tree_path = null;
   private String element_name = null;
   private String xml = null;
   private boolean isTask = false;
   private DTDElement element = null;

   private TreeMap attributes = null;

   public ElementPanel( TreePath tp ) {
      tree_path = tp;
      element_name = tree_path.getLastPathComponent().toString();
      isTask = tp.getPathCount() == 3;

      element = ( DTDElement ) DNDConstants.ANT_DTD.elements.get( element_name );
      if ( element != null ) {
         attributes = new TreeMap( element.attributes );
      }


      setLayout( new BorderLayout() );
      setBorder( new DropShadowBorder() );
      setTransferHandler( new ElementTransferHandler() );
      add( new JLabel( element_name ), BorderLayout.NORTH );
      list = new JList( new DefaultListModel() );
      add( list, BorderLayout.CENTER );
      list.setTransferHandler( new ElementTransferHandler() );
      list.setDragEnabled( true );
      list.addMouseListener( new AttributeViewer( list ) );
   }
   
   public String getName() {
      return element_name;  
   }
   
   public boolean isTask() {
      return isTask;  
   }
   
   public TreePath getTreePath() {
      return tree_path;  
   }

   /**
    * @return a Map containing the DTD attributes for this element. The map keys
    * are the names of the attributes, the values are DTDAttributes.
    */
   public Map getAttributes() {
      return attributes;
   }
   
   public void setAttributes(Map attrs) {
      attributes = attrs == null ? null : new TreeMap(attrs);
   }

   public void setXML( String xml ) {}

   public String toString() {
      if ( attributes == null )
         return element_name;

      StringBuffer sb = new StringBuffer();
      sb.append( "<html><b>" + element_name + "</b><br>" );
      Iterator it = attributes.keySet().iterator();
      while ( it.hasNext() ) {
         String name = it.next().toString();
         if (name.equals("taskname") && isTask())
            continue;
         DTDAttribute attribute = ( DTDAttribute ) attributes.get( name );
         DTDDecl decl = attribute.getDecl();
         boolean required = decl.equals( DTDDecl.REQUIRED );
         sb.append( "<span>&nbsp;&nbsp;" );
         if ( required )
            sb.append( "<font color=red>*</font>" );
         String value = attribute.getDefaultValue();
         sb.append( name ).append( ":&nbsp&nbsp;</span><span>").append(value == null ? "" : value).append("</span><br>" );
         Object type = attribute.getType();
         if ( type instanceof DTDEnumeration || type instanceof DTDNotationList ) {
            String[] items = ( ( DTDEnumeration ) type ).getItem();
            sb.append( "<span><ul>" );
            for ( int j = 0; j < items.length; j++ )
               sb.append( "<li>" ).append( items[ j ].toString() );
            sb.append( "</ul></span>" );
         }
      }
      sb.append( "<html>" );
      return sb.toString();
   }

   public String toXML() {
      return toString();
   }

   protected JList getList() {
      return list;
   }
}
