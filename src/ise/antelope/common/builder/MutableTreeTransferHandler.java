package ise.antelope.common.builder;

import javax.swing.*;
import java.awt.*;
import java.awt.datatransfer.*;
import javax.swing.tree.*;

/**
 * Handles dropping nodes on a tree.
 * @author Dale Anson
 */
public class MutableTreeTransferHandler extends TransferHandler {

   protected Transferable createTransferable( JComponent c ) {
      try {
         TreeNode tp = (TreeNode)( ( JTree ) c ).getSelectionPath().getLastPathComponent();
         if ( tp == null )
            return null;
         String leaf = tp.toString();
         if ( leaf.equals( DNDConstants.TASK ) || leaf.equals( DNDConstants.TYPE ) )
            return null;
         return new TreeTransferable( tp );
      }
      catch ( ClassCastException cce ) {
         return null;
      }
   }

   public int getSourceActions( JComponent c ) {
      return COPY_OR_MOVE;
   }

   public boolean canImport( JComponent c, DataFlavor[] df ) {
      if ( c instanceof JTree ) {
         for ( int i = 0; i < df.length; i++ ) {
            if ( df[ i ] instanceof TreeNodeFlavor )
               return true;
         }
      }
      return false;
   }

   /**
    * @param c the component to receive the data, must be a JTree
    * @param t the data to import
    * @return true if the data was inserted into the component
    */
   public boolean importData( JComponent c, Transferable t ) {
      try {
         // get the data, no point in doing anything else if it's not available
         TreeNode src_tp = ( TreeNode ) t.getTransferData( new TreeNodeFlavor() );
         if ( src_tp == null )
            throw new Exception( "no data available" );

         // get the destination tree and model
         JTree tree = ( JTree ) c;
         DefaultTreeModel model = ( DefaultTreeModel ) tree.getModel();

         // keep track of the nodes that were expanded in the destination tree
         // so that the visual state of the tree can be restored after a drop
         boolean[] expanded_model = new boolean[ tree.getRowCount() ];
         for ( int i = 0; i < expanded_model.length; i++ ) {
            expanded_model[ i ] = tree.isExpanded( i );
         }

         // handle the drop -- get the destination and make sure it can accept
         // the drop
         TreePath dest_tp = tree.getSelectionPath();
         if (dest_tp == null ) {
            System.out.println("dest_tp is null");
            return false;
         }
         DroppableTreeNode dest_node = ( DroppableTreeNode ) dest_tp.getLastPathComponent();
         if (dest_node == null) {
            System.out.println("dest_node is null");
            return false;
         }
         ElementPanel dest_ep = ( ElementPanel ) dest_node.getUserObject();
         if (dest_ep == null) {
            System.out.println("dest_ep is null");
            return false;
         }
         if ( !dest_ep.canAccept( dest_tp ) )
            return false;

         // handle the drop -- get the source
         DroppableTreeNode src_leaf = ( DroppableTreeNode ) src_tp;
         Object o = src_leaf.getUserObject();
         ElementPanel src_ep;
         DroppableTreeNode new_node = null;
         if (o instanceof ElementPanel) {
            src_ep = (ElementPanel)o;  
            DefaultMutableTreeNode root_node = (DefaultMutableTreeNode)model.getRoot();
            model.nodeStructureChanged(root_node);
            DroppableTreeNode parent_node = (DroppableTreeNode)src_leaf.getParent();
            parent_node.remove(src_leaf);
            model.nodeStructureChanged(parent_node);
            new_node = src_leaf;
         }
         else {
            src_ep = new ElementPanel(src_tp);
            new_node = new DroppableTreeNode(src_ep, true);
         }
         dest_node.add(new_node);
         System.out.println(new_node.getParent());
         
         // nodeStructureChanged may collapse expanded rows, so expand rows
         // that were expanded before the drop
         model.nodeStructureChanged( dest_node );
         for ( int i = 0; i < expanded_model.length; i++ ) {
            if ( expanded_model[ i ] ) {
               tree.expandRow( i );
            }
         }
         // then expand the node that just received the drop
         tree.expandPath( dest_tp );

         return true;
      }
      catch ( Exception e ) {
         e.printStackTrace();
         return false;
      }
   }
}
