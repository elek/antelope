package ise.antelope.common.builder;

import java.awt.datatransfer.*;

/**
 * Package Ant objects for movement.
 */
public class AntTransferable extends StringSelection {

   private static DataFlavor[] flavors = null;
   private String type = null;

   /**
    * @param type the type of Ant element being transfered, e.g., target, task,
    * type, etc.
    */
   public AntTransferable( String type ) {
      super( type );
      this.type = type;
      try {
         flavors = new DataFlavor[ 1 ];
         flavors[ 0 ] = new ElementFlavor(type);
      }
      catch ( Exception e ) {
         e.printStackTrace();
      }
   }

   /**
    * @param df ignored
    * @return regardless of the requested DataFlavor, the returned object is
    * always a string containing the type passed to the constructor.
    */
   public Object getTransferData( DataFlavor df ) {
      return type;
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
         throw new IllegalArgumentException( "flavor cannot be null" );
      for ( int i = 0; i < flavors.length; i++ ) {
         if ( df.equals( flavors[ i ] ) ) {
            return true;
         }
      }
      return false;
   }
}
