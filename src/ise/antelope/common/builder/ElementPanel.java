package ise.antelope.common.builder;

import java.awt.*;
import java.awt.event.*;
import java.util.*;
import javax.swing.*;
import javax.swing.tree.*;
import ise.library.*;
import com.wutka.dtd.*;


public class ElementPanel extends JPanel implements java.io.Serializable, java.lang.Cloneable {

   private JList list = null;
   private TreePath tree_path = null;
   // element_name is the dtd name, e.g. target, bunzip, etc
   private String element_name = null;
   // display name is the string to show, e.g. the name for a target rather
   // than "target".
   private String display_name = "";
   private String xml = null;
   private boolean isTaskOrType = false;
   private DTDElement element = null;

   private TreeMap attributes = null;

   public ElementPanel( TreePath tp ) {
      this((TreeNode)tp.getLastPathComponent());
   }
   
   public ElementPanel(TreeNode tn) {
      //tree_path = tp;
      //element_name = tree_path.getLastPathComponent().toString();
      element_name = tn.toString();
      display_name = element_name;
      //isTaskOrType = tp.getPathCount() == 3;

      element = ( DTDElement ) DNDConstants.ANT_DTD.elements.get( element_name );
      if ( element != null ) {
         attributes = new TreeMap();
         Iterator it = element.attributes.keySet().iterator();
         while(it.hasNext()) {
            String name = (String)it.next();
            DTDAttribute attr = (DTDAttribute)element.attributes.get(name);
            NodeAttribute na = new NodeAttribute(attr);
            attributes.put(name, na);
         }
         
         // get display name if target
         NodeAttribute na = (NodeAttribute)attributes.get("name");
         if ((na == null || na.getValue().equals("")) && element.getName().equals(DNDConstants.TARGET)) {
            String rtn = JOptionPane.showInputDialog(null, "Enter name for target:");
            if (rtn != null){
               display_name = rtn;
               na = new NodeAttribute();
               na.setName("name");
               na.setValue(display_name);
               attributes.put("name", na);
            }
         }
      }

      // do some specific checking
      //if ( element_name.equals( DNDConstants.TARGET ) ) {
      //   element_name = JOptionPane.showInputDialog( null, "2 Enter target name:" );
      //}

      setLayout( new BorderLayout() );
      setBorder( new DropShadowBorder() );
      setTransferHandler( new ElementTransferHandler() );
      add( new JLabel( display_name ), BorderLayout.NORTH );
      list = new JList( new DefaultListModel() );
      add( list, BorderLayout.CENTER );
      list.setTransferHandler( new ElementTransferHandler() );
      list.setDragEnabled( true );
      addMouseListener( new MenuPopup() );

   }

   class MenuPopup extends MouseAdapter {
      private JPopupMenu pm = new JPopupMenu();

      public MenuPopup() {
         JMenuItem props_mi = new JMenuItem( "Properties" );
         JMenuItem delete_mi = new JMenuItem( "Delete" );
         pm.add( props_mi );
         pm.add( delete_mi );

         props_mi.addActionListener( new ActionListener() {
                  public void actionPerformed( ActionEvent ae ) {
                     new AttributeViewer( ElementPanel.this );
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
            GUIUtils.showPopupMenu( pm, me.getComponent(), me.getX(), me.getY() );
         }
      }

   }

   /**
    * @return the dtd element name, e.g. target, description, javac, etc.   
    */
   public String getName() {
      return element_name;
   }
   
   public String getDisplayName() {
      return display_name;  
   }

   public boolean isTaskOrType() {
      return isTaskOrType;
   }

   public TreePath getTreePath() {
      return tree_path;
   }

   public boolean canAccept( TreePath tp ) {
      return true;
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
      sb.append( "<html><b>" + display_name + "</b><br>" );
      Iterator it = attributes.keySet().iterator();
      while ( it.hasNext() ) {
         String name = it.next().toString();
         if ( name.equals( "taskname" ) && isTaskOrType() )
            continue;
         NodeAttribute attribute = (NodeAttribute)attributes.get(name);
         boolean required = attribute.isRequired();;
         sb.append( "<span>&nbsp;&nbsp;" );
         if ( required ) {
            //sb.append( "<font color=red>*</font>" );
            String value = attribute.getValue();
            sb.append( name ).append( ":&nbsp&nbsp;</span><span>" ).append( value == null ? "" : value ).append( "</span><br>" );
         }
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
