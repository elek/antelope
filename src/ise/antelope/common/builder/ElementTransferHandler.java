package ise.antelope.common.builder;

import javax.swing.*;
import javax.swing.tree.TreePath;
import java.awt.datatransfer.*;
import com.wutka.dtd.*;
import java.util.*;

public class ElementTransferHandler extends TransferHandler {
   private int[] indices = null;
   private int addIndex = -1; //Location where items were added
   private int addCount = 0;  //Number of items added.

   /**
    * @param c the component containing the data to transfer. If the component is 
    * an ElementPanel, the panel is the data, if the component is a JList, get
    * the selected item, which should be an ElementPanel, and use it as the data.
    */
   protected Transferable createTransferable( JComponent c ) {
      JList list = null;
      if ( c instanceof ElementPanel ) {
         ElementPanel ep = ( ElementPanel ) c;
         return new AntTransferable( ep );
      }
      else if ( c instanceof JList ) {
         list = ( JList ) c;
         Object o = list.getSelectedValue();
         if ( o instanceof ElementPanel ) {
            ElementPanel ep = ( ElementPanel ) list.getSelectedValue();
            return new AntTransferable( ep );
         }
      }
      return null;
   }

   /**
    * @param c an ElementPanel
    * @return TransferHandler.COPY_OR_MOVE
    */
   public int getSourceActions( JComponent c ) {
      return COPY_OR_MOVE;
   }

   /**
    * @param c the component to receive the data
    * @param df an array of data flavors
    * @return true if any of the flavors is an ElementFlavor
    */
   public boolean canImport( JComponent c, DataFlavor[] df ) {
      for ( int i = 0; i < df.length; i++ ) {
         if ( df[ i ] instanceof ElementFlavor )
            return true;
         if (df[i] instanceof TreePathFlavor)
            return true;
      }
      return false;
   }

   /**
    * @param c the component to receive the data, must be an ElementPanel or a JList
    * @param t the data to import
    * @return true if the data was inserted into the component
    */
   public boolean importData( JComponent c, Transferable t ) {
      try {
         // get the data, it will either be a string or an ElementPanel
         ElementPanel ep = null;
         Object data = t.getTransferData( new ElementFlavor() );
         if(data != null)
            ep = (ElementPanel)data;
         else {
            data = t.getTransferData(new TreePathFlavor());
            if (data != null){
               TreePath tp = (TreePath)data;
               ep = new ElementPanel(tp);
            }
            else
               return false;
         }
         
         if ( ep != null ) {
            JList list = null;
            if ( c instanceof ElementPanel ) {
               ElementPanel parent = ( ElementPanel ) c;
               list = parent.getList();
            }
            else if ( c instanceof JList ) {
               list = ( JList ) c;
            }
            else {
               return false;
            }
            
            DefaultListModel listModel = ( DefaultListModel ) list.getModel();
            int index = list.getSelectedIndex();

            //Prevent the user from dropping data back on itself.
            //For example, if the user is moving items #4,#5,#6 and #7 and
            //attempts to insert the items after item #5, this would
            //be problematic when removing the original items.
            //So this is not allowed.
            if ( indices != null && index >= indices[ 0 ] - 1 &&
                    index <= indices[ indices.length - 1 ] ) {
               indices = null;
               return false;
            }

            int max = listModel.getSize();
            if ( index < 0 ) {
               index = max;
            }
            else {
               index++;
               if ( index > max ) {
                  index = max;
               }
            }
            addIndex = index;
            listModel.add( index++, ep );

            return true;
         }
      }
      catch ( Exception e ) {
         e.printStackTrace();
      }
      return false;
   }
}
