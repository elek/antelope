package ise.antelope.common.builder;

import java.awt.datatransfer.*;

public class ElementFlavor extends DataFlavor {
   
   private String type = null;
   
   public ElementFlavor() {
      this("");  
   }
   
   public ElementFlavor(String type) {
      super(ise.antelope.common.builder.ElementPanel.class, "ElementPanel");
      this.type = type;
   }
   
   public String getType() {
      return type;  
   }
   
}
