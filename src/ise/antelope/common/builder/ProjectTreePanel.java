package ise.antelope.common.builder;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.tree.*;

public class ProjectTreePanel extends JPanel {

   private JTree project_tree = null;

   public ProjectTreePanel() {
      project_tree = new JTree();

      DroppableTreeNode root_node = new DroppableTreeNode( new ElementPanel( new TreePath( DNDConstants.PROJECT ) ), true );
      DefaultTreeModel tree_model = new DefaultTreeModel( root_node );
      project_tree.setModel( tree_model );
      add( project_tree );
      project_tree.setTransferHandler( new MutableTreeTransferHandler() );
      project_tree.setDragEnabled( true );
      setBackground( project_tree.getBackground() );
      project_tree.addMouseListener( new MouseDelegate() );
      project_tree.setCellRenderer( new ProjectTreeCellRenderer() );
      project_tree.setExpandsSelectedPaths(true);
   }

   class MouseDelegate extends MouseAdapter {
      public void mousePressed( MouseEvent me ) {
         TreePath tp = project_tree.getClosestPathForLocation( me.getX(), me.getY() );
         if ( me.isPopupTrigger() ) {
            ElementPanel ep = ( ElementPanel ) tp.getLastPathComponent();
            MouseListener[] ml = ep.getMouseListeners();
            for ( int i = 0; i < ml.length; i++ ) {
               ml[ i ].mousePressed( me );
            }
         }
         else {
            project_tree.setSelectionPath( tp );
         }
      }
      public void mouseReleased( MouseEvent me ) {
         TreePath tp = project_tree.getClosestPathForLocation( me.getX(), me.getY() );
         if ( me.isPopupTrigger() ) {
            DroppableTreeNode node = ( DroppableTreeNode ) tp.getLastPathComponent();
            ElementPanel ep = ( ElementPanel ) node.getUserObject();
            MouseListener[] ml = ep.getMouseListeners();
            for ( int i = 0; i < ml.length; i++ ) {
               ml[ i ].mouseReleased( me );
            }
         }
         else {
            project_tree.setSelectionPath( tp );
         }
      }
   }

}
