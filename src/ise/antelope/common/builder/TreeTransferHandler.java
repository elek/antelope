package ise.antelope.common.builder;

import javax.swing.*;
import java.awt.*;
import java.awt.datatransfer.*;
import javax.swing.tree.*;

/**
 * Modeled after code from Sun's java tutorial.
 */
public class TreeTransferHandler extends TransferHandler {

   protected Transferable createTransferable( JComponent c ) {
      TreePath tp = ( ( JTree ) c ).getSelectionPath();
      Object[] steps = tp.getPath();
      if ( steps.length == 0 || steps.length == 1 )
         return null;
      if ( !steps[ 0 ].toString().equals( DNDConstants.PROJECT ) )
         return null;


      if ( steps[ 1 ].toString().equals( DNDConstants.TARGET ) ) {
         return new AntTransferable( steps[ 1 ].toString() );
      }
      if ( steps.length > 2 ) {
         if ( steps[ 1 ].toString().equals( DNDConstants.TASK ) ||
                 steps[ 1 ].toString().equals( DNDConstants.TYPE ) ) {
            return new AntTransferable( steps[ steps.length - 1 ].toString() );
         }
      }
      return null;
   }

   public int getSourceActions( JComponent c ) {
      return COPY_OR_MOVE;
   }

   public boolean canImport( JComponent c, DataFlavor[] df ) {
      return false;
   }

   protected String exportString( JComponent c ) {
      JTree tree = ( JTree ) c;
      TreePath tp = tree.getSelectionPath();
      if ( tp == null )
         return null;
      return "node: " + tp.toString();
   }
}
