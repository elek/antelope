
package ise.antelope.common.builder;

import com.wutka.dtd.*;

public class NodeAttribute implements java.io.Serializable {
   private String name = "";
   private String[] items = null;
   private String decl; // DTDDecl.toString
   private String value = "";

   public NodeAttribute() {}

   public NodeAttribute( DTDAttribute da ) {
      name = new String( da.getName() );
      if ( name == null )
         name = "";
      Object type = da.getType();
      if ( type != null && ( type instanceof DTDEnumeration || type instanceof DTDNotationList ) ) {
         String[] i = ( ( DTDEnumeration ) type ).getItem();
         items = new String[ i.length ];
         System.arraycopy( i, 0, items, 0, i.length );
      }
      decl = new String( da.getDecl().toString() );
      if ( da.getDefaultValue() != null )
         value = new String( da.getDefaultValue() );

   }

   public String getName() {
      return name;
   }

   public void setName( String n ) {
      name = n;
   }

   public String getValue() {
      return value;
   }

   public void setValue( String v ) {
      value = v;
   }

   public String[] getItems() {
      return items;
   }

   public String getDecl() {
      return decl;
   }

   public boolean isRequired() {
      if ( decl == null )
         return false;
      return decl.equals( DTDDecl.REQUIRED.name );
   }
   public String toString() {
      return "NodeAttribute, name=" + name + ", value=" + value;  
   }
}
