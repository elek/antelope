
package ise.antelope.common.builder;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.*;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.border.*;
import javax.swing.tree.*;
import javax.swing.tree.DefaultMutableTreeNode;
import java.awt.Point;
import com.wutka.dtd.*;


/**
 * Renders a TreeNode.
 * @author Dale Anson, danson@germane-software.com
 */
public class ProjectTreeCellRenderer extends DefaultTreeCellRenderer {

   private boolean showAttributes = true;

   public ProjectTreeCellRenderer() {
      this( true );
   }

   public ProjectTreeCellRenderer( boolean showAttr ) {
      showAttributes = showAttr;
   }

   public void setShowAttributes( boolean showAttr ) {
      showAttributes = showAttr;
   }

   public boolean getShowAttributes() {
      return showAttributes;
   }

   private String getLabelText( DroppableTreeNode node ) {
      if ( node != null ) {
         StringBuffer name = new StringBuffer();
         ElementPanel ep = ( ElementPanel ) node.getUserObject();

         Map attrs = ep.getAttributes();
         if ( ep.getName().equals( DNDConstants.TARGET ) ) {
            DTDAttribute da = (DTDAttribute)attrs.get("name");
            String value = da.getDefaultValue();
            name.append( "<html>" );
            if ( attrs != null ) {
               name.append( "<b>" );
               name.append( value );
               name.append( "</b>" );
            }
            if ( showAttributes ) {
               Iterator it = attrs.keySet().iterator();
               while ( it.hasNext() ) {
                  String attr_name = ( String ) it.next();
                  if ( !attr_name.equals( "name" ) ) {
                     da = ( DTDAttribute ) attrs.get( attr_name );
                     value = da.getDefaultValue();
                     if ( value != null && value.length() > 0 ) {
                        name.append( " " );
                        name.append( attr_name );
                        name.append( "=\"" );
                        name.append( value );
                        name.append( "\"" );
                     }
                  }
               }
            }
         }
         else {
            name.append( ep.getName() );
            Iterator it = attrs.keySet().iterator();
            while ( it.hasNext() ) {
               String attr_name = ( String ) it.next();
               if ( !attr_name.equals( "name" ) ) {
                  DTDAttribute da = ( DTDAttribute ) attrs.get( attr_name );
                  String value = da.getDefaultValue();
                  if ( value != null && value.length() > 0 ) {
                     name.append( " " );
                     name.append( attr_name );
                     name.append( "=\"" );
                     name.append( value );
                     name.append( "\"" );
                  }
               }
            }
         }
         return name.toString();
      }
      return null;
   }

   public ImageIcon getImageIcon( DroppableTreeNode node ) {
      ImageIcon icon = null;
      if ( node != null ) {
         ElementPanel ep = ( ElementPanel ) node.getUserObject();
         if ( ep.getName().equals( DNDConstants.TARGET ) ) {
            java.net.URL url = getClass().getClassLoader().getResource( "images/Target16.gif" );
            if ( url != null )
               icon = new ImageIcon( url );
         }
      }
      return icon;
   }

   public Component getTreeCellRendererComponent( JTree tree, Object value, boolean selected, boolean expanded, boolean leaf, int row, boolean hasFocus ) {
      JLabel nodeLabel = ( JLabel ) super.getTreeCellRendererComponent( tree, value, selected, expanded, leaf, row, hasFocus );
      DefaultMutableTreeNode treenode = ( DefaultMutableTreeNode ) value;
      if ( treenode != null && treenode instanceof DroppableTreeNode ) {
         nodeLabel.setText( getLabelText( ( DroppableTreeNode ) treenode ) );
         nodeLabel.setIcon( getImageIcon( ( DroppableTreeNode ) treenode ) );
      }
      else
         nodeLabel.setText( value.toString() );
      return nodeLabel;
   }
}

