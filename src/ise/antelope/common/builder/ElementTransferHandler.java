package ise.antelope.common.builder;

import javax.swing.*;
import java.awt.datatransfer.*;
import com.wutka.dtd.*;
import java.util.*;

public class ElementTransferHandler extends TransferHandler {
   private int[] indices = null;
   private int addIndex = -1; //Location where items were added
   private int addCount = 0;  //Number of items added.

   protected Transferable createTransferable( JComponent c ) {
      JList list = null;
      if ( c instanceof ElementPanel ) {
         ElementPanel ep = ( ElementPanel ) c;
         list = ep.getList();
      }
      else if ( c instanceof JList ) {
         list = ( JList ) c;
      }
      else {
         return null;
      }
      indices = list.getSelectedIndices();
      Object[] values = list.getSelectedValues();

      StringBuffer buff = new StringBuffer();

      for ( int i = 0; i < values.length; i++ ) {
         Object val = values[ i ];
         buff.append( val == null ? "" : val.toString() );
         if ( i != values.length - 1 ) {
            buff.append( "\n" );
         }
      }
      System.out.println( buff.toString() );
      return new AntTransferable( buff.toString() );
   }

   /**
    * @param c an ElementPanel
    * @return TransferHandler.COPY_OR_MOVE
    */
   public int getSourceActions( JComponent c ) {
      return COPY_OR_MOVE;
   }

   /**
    * @param df an array of data flavors
    * @return true if any of the flavors is an ElementFlavor
    */
   public boolean canImport( JComponent c, DataFlavor[] df ) {
      for ( int i = 0; i < df.length; i++ ) {
         if ( df[ i ] instanceof ElementFlavor )
            return true;
      }
      return false;
   }

   /**
    * @param c the component to receive the data
    * @param t the data to import
    * @return true if the data was inserted into the component
    */
   public boolean importData( JComponent c, Transferable t ) {
      try {
         // 'data' is one of two things -- 1) it is a single word, which means
         // it is a raw element name, or 2) it is already formatted as html.
         // Determine the second case by checking for <html> at the start of the
         // string, if not, assume the first case. The html has the element name
         // surrounded by <b> tags. Element attributes are separated by <br>,
         // with attribute name and value enclosed individually in <span> tags.
         // Enumerations or lists have the individual value items wrapped in a
         // <ul> with individual items separated by <li>. There are no line-
         // enders (\n, \r).
         String data = (String)t.getTransferData( new ElementFlavor() );
         if (data != null && data.startsWith("<html>")) {
            int start = "<html><b>".length();
            int end = data.indexOf("</b>");
            System.out.println(start + " : " + end);
            data = data.substring(start, end);
         }
         DTDElement el = ( DTDElement ) DNDConstants.ANT_DTD.elements.get( data.toString() );
         
         StringBuffer sb = new StringBuffer();
         if ( el != null ) {
            Hashtable attrs = el.attributes;
            Object[] attr_names = attrs.keySet().toArray();
            Arrays.sort( attr_names );
            String item_name = data.toString();

            sb.append( "<html><b>" + item_name + "</b><br>" );
            for ( int i = 0; i < attr_names.length; i++ ) {
               String name = attr_names[ i ].toString();
               if ( name.equals( "taskname" ) )
                  continue;
               DTDAttribute attribute = ( DTDAttribute ) attrs.get( name );
               Object type = attribute.getType();
               if ( type instanceof DTDEnumeration || type instanceof DTDNotationList ) {
                  String[] items = ( ( DTDEnumeration ) type ).getItem();
                  sb.append( "<span><ul>" );
                  for ( int j = 0; j < items.length; j++ )
                     sb.append( "<li>" ).append( items[ j ].toString() );
                  sb.append( "</ul></span>" );
               }
               else {
                  // assume String type
                  sb.append( "<span>" ).append( name ).append( "</span><span></span><br>" );
               }
            }
            sb.append( "<html>" );
         }
         System.out.println( "ElementTransferhandler: " + sb.toString() );
         if ( data != null ) {
            /// need to add a child component to c based on the element type.
            /// the element type is the data
            ///c.add( ( JComponent ) data );
            JList list = null;
            if ( c instanceof ElementPanel ) {
               ElementPanel ep = ( ElementPanel ) c;
               list = ep.getList();
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
            listModel.add( index++, sb.toString() );

            return true;
         }
      }
      catch ( Exception e ) {
         e.printStackTrace();
      }
      return false;
   }
}
