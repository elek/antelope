
package ise.antelope.common;

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
import org.xml.sax.Attributes;


/**
 * Renders a SAXTreeNode.
 * @author Dale Anson, danson@germane-software.com
 */
public class SAXTreeCellRenderer extends DefaultTreeCellRenderer {

   private boolean showAttributes = true;

   public SAXTreeCellRenderer() {
      this( true );
   }

   public SAXTreeCellRenderer( boolean showAttr ) {
      showAttributes = showAttr;
   }

   public void setShowAttributes( boolean showAttr ) {
      showAttributes = showAttr;
   }

   public boolean getShowAttributes() {
      return showAttributes;
   }

   private String getLabelText( SAXTreeNode node ) {
      if ( node != null ) {
         StringBuffer name = new StringBuffer();
         Attributes attr = node.getAttributes();
         if ( node.getName().equals( "target" ) ) {
            name.append( "<html>" );
            if ( attr != null ) {
               name.append( "<b>" );
               name.append( attr.getValue( attr.getIndex( "name" ) ) );
               name.append( "</b>" );
            }
            if ( attr != null && showAttributes ) {
               for ( int i = 0; i < attr.getLength(); i++ ) {
                  if ( !attr.getQName( i ).equals( "name" ) ) {
                     name.append( " " );
                     name.append( attr.getQName( i ) );
                     name.append( "=\"" );
                     name.append( attr.getValue( i ) );
                     name.append( "\"" );
                  }
               }
            }
         } else {
            name.append( node.getName() );
            if ( attr != null && showAttributes ) {
               for ( int i = 0; i < attr.getLength(); i++ ) {
                  name.append( " " );
                  name.append( attr.getQName( i ) );
                  name.append( "=\"" );
                  name.append( attr.getValue( i ) );
                  name.append( "\"" );
               }
            }
         }
         return name.toString();
      }
      return null;
   }

   public ImageIcon getImageIcon( SAXTreeNode node ) {
      ImageIcon icon = null;
      if ( node != null ) {
         if ( node.getName().equals( "target" ) ) {
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
      if ( treenode != null && treenode instanceof SAXTreeNode ) {
         nodeLabel.setText( getLabelText( ( SAXTreeNode ) treenode ) );
         nodeLabel.setIcon( getImageIcon( ( SAXTreeNode ) treenode ) );
      } else
         nodeLabel.setText( value.toString() );
      return nodeLabel;
   }
}

