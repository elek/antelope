package ise.antelope.common.builder;

import javax.swing.tree.*;

public class DroppableTreeNode extends DefaultMutableTreeNode implements Cloneable {
   private boolean drop = false;
   
   public DroppableTreeNode(Object userObject, boolean canDrop) {
      super(userObject, canDrop);
      drop = canDrop;
   }
   
   public boolean canDrop() {
      return drop;
   }
   
   public void setCanDrop(boolean b) {
      drop = b;  
   }
}
