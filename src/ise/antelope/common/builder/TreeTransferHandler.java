package ise.antelope.common.builder;

import javax.swing.*;
import java.awt.*;
import java.awt.datatransfer.*;
import javax.swing.tree.*;


public class TreeTransferHandler extends TransferHandler {

   protected Transferable createTransferable( JComponent c ) {
      try {
         TreeNode tp = (TreeNode)( ( JTree ) c ).getSelectionPath().getLastPathComponent();
         if ( tp == null )
            return null;
         String leaf = tp.toString();
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
      return false;
   }
}
