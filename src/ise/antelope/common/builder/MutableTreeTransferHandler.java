package ise.antelope.common.builder;

import javax.swing.*;
import java.awt.*;
import java.awt.datatransfer.*;
import javax.swing.tree.*;


public class MutableTreeTransferHandler extends TransferHandler {

   protected Transferable createTransferable( JComponent c ) {
      try {
         TreePath tp = ( ( JTree ) c ).getSelectionPath();
         if ( tp == null )
            return null;
         String leaf = tp.getLastPathComponent().toString();
         if (leaf.equals(DNDConstants.TASK) || leaf.equals(DNDConstants.TYPE))
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
      if (c instanceof JTree) {
         for (int i = 0; i < df.length; i++) {
            if (df[i] instanceof TreePathFlavor)
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
         TreePath src_tp = (TreePath)t.getTransferData(new TreePathFlavor());
         if (src_tp == null)
            throw new Exception("no data available");
         
         // get the destination tree and model
         JTree tree = (JTree)c;
         DefaultTreeModel model = (DefaultTreeModel)tree.getModel();
         
         // keep track of the nodes that were expanded in the destination tree
         // so that the visual state of the tree can be restored after a drop
         boolean[] expanded_model = new boolean[tree.getRowCount()];
         for (int i = 0; i < expanded_model.length; i++) {
            expanded_model[i] = tree.isExpanded(i);  
         }
         
         // handle the drop -- get the source
         DroppableTreeNode src_leaf = (DroppableTreeNode)src_tp.getLastPathComponent();
         Object o = src_leaf.getUserObject();
         ElementPanel src_ep = o instanceof ElementPanel ? (ElementPanel)o : new ElementPanel(src_tp);
         
         // get the destination
         TreePath dest_tp = tree.getSelectionPath();
         DroppableTreeNode dest_node = (DroppableTreeNode)dest_tp.getLastPathComponent();
         ElementPanel dest_ep = (ElementPanel)dest_node.getUserObject();
         if (!dest_ep.canAccept(dest_tp))
            return false;
         ElementPanel new_ep = new ElementPanel(new TreePath(src_ep.getName()));
         System.out.println("new_ep = " + new_ep);
         dest_node.add(new DroppableTreeNode(new_ep, true));
         
         // nodeStructureChanged may collapse expanded rows, so expand rows
         // that were expanded before the drop
         model.nodeStructureChanged(dest_node);
         for(int i = 0; i < expanded_model.length; i++) {
            if(expanded_model[i]) {
               tree.expandRow(i);  
            }
         }
         // then expand the node that just received the drop
         tree.expandPath(dest_tp);
         
         return true;
      }
      catch(Exception e) {
         e.printStackTrace();
         return false;
      }
   }
}
