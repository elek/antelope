
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
         if ( node.isTarget() || node.isProject() ) {
            name.append( "<html>" );
            if ( attr != null ) {
               name.append( "<b>" );
               boolean p = node.isPrivate();
               if ( p )
                  name.append( "<i>" );
               name.append( attr.getValue( attr.getIndex( "name" ) ) );
               if ( p )
                  name.append( "</i>" );
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
            if ( showAttributes && node.isImported() ) {
               name.append( " (imported from " ).append( node.getFile() ).append( ")" );
            }
         }
         else {
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
         if ( node.isProject() ) {
            String image_src = "";
            if ( node.isImported() )
               image_src = "images/red_ant.gif";
            else
               image_src = "images/ant.gif";
            java.net.URL url = getClass().getClassLoader().getResource( image_src );
            if ( url != null )
               icon = new ImageIcon( url );
         }
         else if ( node.isTarget() ) {
            String image_src = "";

            if ( node.isImported() ) {
               if ( node.isDefaultTarget() )
                  image_src = "images/GreenRedTarget16.gif";
               else {
                  if ( node.isPrivate() )
                     image_src = "images/GrayRedTarget16.gif";
                  else
                     image_src = "images/BlackRedTarget16.gif";
               }
            }
            else {
               if ( node.isDefaultTarget() )
                  image_src = "images/GreenTarget16.gif";
               else {
                  if ( node.isPrivate() )
                     image_src = "images/GrayTarget16.gif";
                  else
                     image_src = "images/Target16.gif";
               }
            }

            java.net.URL url = getClass().getClassLoader().getResource( image_src );
            if ( url != null )
               icon = new ImageIcon( url );
         }
         else if ( node.isTask() ) {
            String image_src = "images/Wrench16.gif";
            java.net.URL url = getClass().getClassLoader().getResource( image_src );
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
      }
      else
         nodeLabel.setText( value.toString() );
      return nodeLabel;
   }
}

