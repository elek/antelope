package ise.antelope.common.builder;

import java.awt.datatransfer.*;

/**
 * Package Ant objects for movement.
 */
public class AntTransferable implements Transferable {

   private static DataFlavor[] flavors = null;
   private ElementPanel data = null;

   /**
    * @param an ElementPanel to drag   
    */
   public AntTransferable( ElementPanel data ) {
      this.data = data;
      init();
   }


   /**
    * Set up the supported flavors: DataFlavor.stringFlavor for a raw string containing
    * an Ant element name (e.g. task, target, etc), or an ElementFlavor containing
    * an ElementPanel.
    */
   private void init() {
      try {
         flavors = new DataFlavor[ 1 ];
         flavors[ 0 ] = new ElementFlavor( );
      }
      catch ( Exception e ) {
         e.printStackTrace();
      }
   }

   /**
    * @param df the flavor type desired for the data. Acceptable values are
    * DataFlavor.stringFlavor or ElementFlavor.
    * @return if df is DataFlavor.stringFlavor, returns a raw string containing
    * an Ant element name, if ElementFlavor, returns an ElementPanel.
    */
   public Object getTransferData( DataFlavor df ) {
      if ( df == null )
         return null;
      if ( df instanceof ElementFlavor  )
         return data;
      return null;
   }

   /**
    * @return an array containing a single ElementFlavor.   
    */
   public DataFlavor[] getTransferDataFlavors() {
      return flavors;
   }

   /**
    * @param df the flavor to check
    * @return true if df is an ElementFlavor
    */
   public boolean isDataFlavorSupported( DataFlavor df ) {
      if ( df == null )
         return false;
      for ( int i = 0; i < flavors.length; i++ ) {
         if ( df.equals( flavors[ i ] ) ) {
            return true;
         }
      }
      return false;
   }
}
