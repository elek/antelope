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
      //list.addMouseListener( new AttributeViewer( list ) );
      list.addMouseListener( new MenuPopup() );

   }

   class MenuPopup extends MouseAdapter {
      private JPopupMenu pm = new JPopupMenu();
      private ElementPanel child = null;

      public MenuPopup() {
         JMenuItem props_mi = new JMenuItem( "Properties" );
         JMenuItem delete_mi = new JMenuItem( "Delete" );
         pm.add( props_mi );
         pm.add( delete_mi );

         props_mi.addActionListener( new ActionListener() {
                  public void actionPerformed( ActionEvent ae ) {
                     if ( child == null )
                        return ;
                     new AttributeViewer( child );
                  }
               }
                                   );
      }
      public void mousePressed( MouseEvent me ) {
         doPopup( me );
      }
      public void mouseReleased( MouseEvent me ) {
         doPopup( me );
      }
      private void doPopup( MouseEvent me ) {
         if ( me.isPopupTrigger() ) {
            JList list = getList();
            int index = list.locationToIndex( me.getPoint() );
            if ( index > -1 ) {
               list.setSelectedIndex( index );
               child = (ElementPanel)list.getSelectedValue();
               GUIUtils.showPopupMenu( pm, ElementPanel.this, me.getX(), me.getY() );
            }

         }
      }

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

   public void setAttributes( Map attrs ) {
      attributes = attrs == null ? null : new TreeMap( attrs );
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
         if ( name.equals( "taskname" ) && isTask() )
            continue;
         DTDAttribute attribute = ( DTDAttribute ) attributes.get( name );
         DTDDecl decl = attribute.getDecl();
         boolean required = decl.equals( DTDDecl.REQUIRED );
         sb.append( "<span>&nbsp;&nbsp;" );
         if ( required )
            sb.append( "<font color=red>*</font>" );
         String value = attribute.getDefaultValue();
         sb.append( name ).append( ":&nbsp&nbsp;</span><span>" ).append( value == null ? "" : value ).append( "</span><br>" );
      }
      sb.append( "</html>" );
      return sb.toString();
   }

   public String toXML() {
      return toString();
   }

   protected JList getList() {
      return list;
   }
}
