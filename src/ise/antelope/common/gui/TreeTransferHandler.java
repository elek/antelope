package ise.antelope.common.gui;

import javax.swing.*;
import java.awt.*;
import java.awt.datatransfer.*;
import javax.swing.tree.*;

/**
 * Modeled after code from Sun's java tutorial.
 */
public class TreeTransferHandler extends StringTransferHandler {
   private int[] indices = null;
   private int addIndex = -1; //Location where items were added
   private int addCount = 0;  //Number of items added.
   
   public boolean canImport(JComponent c, DataFlavor[] df) {
      return false;  
   }

   protected String exportString( JComponent c ) {
      JTree tree = (JTree)c;
      TreePath tp = tree.getSelectionPath();
      if (tp == null)
         return null;
      return "node: " + tp.toString();      
   }

   //Take the incoming string and wherever there is a
   //newline, break it into a separate item in the list.
   protected void importString( JComponent c, String str ) {
   }

   //If the remove argument is true, the drop has been
   //successful and it's time to remove the selected items
   //from the list. If the remove argument is false, it
   //was a Copy operation and the original list is left
   //intact.
   protected void cleanup( JComponent c, boolean remove ) {
   }
}
