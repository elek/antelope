package ise.antelope.common.builder;

import java.awt.datatransfer.*;

public class TreePathFlavor extends DataFlavor {
   
   
   public TreePathFlavor() {
      super(javax.swing.tree.TreePath.class, "TreePath");
   }
   
}
