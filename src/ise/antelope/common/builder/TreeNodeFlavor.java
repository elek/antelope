package ise.antelope.common.builder;

import java.awt.datatransfer.*;

public class TreeNodeFlavor extends DataFlavor {
   
   
   public TreeNodeFlavor() {
      super(javax.swing.tree.TreeNode.class, "TreeNode");
   }
   
}
