package ise.antelope.common;

import ise.library.Nav;
import ise.library.Navable;
import ise.library.LambdaLayout;

import java.awt.BorderLayout;
import java.io.*;
import javax.swing.*;
import java.beans.Beans;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.tree.*;

public class SAXPanel extends JPanel implements Navable {

   private JTree tree = null;
   private CommonHelper _helper;
   private Nav _nav;

   public SAXPanel( CommonHelper ch ) {
      _helper = ch;
      setLayout( new BorderLayout() );
      try {
         tree = new JTree();
         tree.setCellRenderer( new SAXTreeCellRenderer() );
         //tree.setModel( new SAXTreeModel( f ) );

         add( new JScrollPane( tree ), BorderLayout.CENTER );


         JCheckBox show_attr_mi = new JCheckBox( "Show Attributes" );
         show_attr_mi.setSelected( true );
         add( show_attr_mi, BorderLayout.SOUTH );
         show_attr_mi.addActionListener( new ActionListener() {
                  public void actionPerformed( ActionEvent ae ) {
                     JCheckBox mi = ( JCheckBox ) ae.getSource();
                     SAXTreeCellRenderer renderer = ( SAXTreeCellRenderer ) tree.getCellRenderer();
                     renderer.setShowAttributes( mi.isSelected() );
                     tree.repaint();
                  }
               }
                                       );

         _nav = new Nav( this );
         JPanel bottom_panel = new JPanel( new LambdaLayout() );
         add( bottom_panel, BorderLayout.SOUTH );
         bottom_panel.add( show_attr_mi, "0,0" );
         bottom_panel.add( _nav, "1, 0" );


         MouseAdapter ma = new MouseAdapter() {
                  public void mouseClicked( MouseEvent evt ) {
                     int clicks = evt.getClickCount();
                     boolean rightClick = evt.isMetaDown();
                     if ( evt.getSource() instanceof JTree ) {
                        JTree tree = ( JTree ) evt.getSource();
                        TreePath path = tree.getClosestPathForLocation( evt.getX(),
                              evt.getY() );
                        if ( path != null ) {
                           int row = tree.getRowForPath( path );
                           Object object = path.getLastPathComponent();
                           if ( object instanceof SAXTreeNode ) {
                              SAXTreeNode node = ( SAXTreeNode ) object;
                              _helper.actionPerformed( new ActionEvent( node.getLocation(), CommonHelper.EDIT_EVENT, "" ) );
                              _nav.update( node.getLocation() );
                           }
                        }
                     }
                  }

               }
               ;
         tree.addMouseListener( ma );
      }
      catch ( Exception e ) {
         e.printStackTrace();
      }
   }

   public void setPosition( Object o ) {
      if ( o instanceof java.awt.Point ) {
         _helper.actionPerformed( new ActionEvent( o, CommonHelper.EDIT_EVENT, "" ) );
      }
   }

   /**
    * @return true if the file is an Ant build file, false if not.   
    */
   public boolean openBuildFile( File f ) {
      SAXTreeModel model = new SAXTreeModel( f );
      tree.setModel( model );
      model.nodeChanged( ( TreeNode ) model.getRoot() );
      tree.repaint();
      SAXTreeNode root = (SAXTreeNode)model.getRoot();
      if (root == null)
         return false;
      if (root.getName() == null)
         return false;
      return root.getName().equals("project");
   }
}
