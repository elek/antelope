package ise.antelope.common;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeModel;
import java.awt.Point;
import java.io.File;
import org.xml.sax.Attributes;
import org.xml.sax.helpers.AttributesImpl;

/**
 * This tree node captures some information about the node location from the
 * SAX parser: the node name, the node attributes, and the node location in
 * the original xml document.
 * @author Dale Anson, danson@germane-software.com
 */
public class SAXTreeNode extends DefaultMutableTreeNode implements Cloneable{

   private String name;
   private Point location;
   private Attributes attributes;
   private File file = null;
   private boolean isImported = false;

   public SAXTreeNode( String name, Point location, Attributes attr ) {
      this(name, location, attr, null);
   }
   
   public SAXTreeNode( String name, Point location, Attributes attr, File f) {
      super( name );
      this.name = name;
      this.location = location;
      if ( attr != null )
         this.attributes = new AttributesImpl( attr );
      file = f;
   }

   public Attributes getAttributes() {
      return attributes;
   }

   public Point getLocation() {
      return location;
   }

   public String getName() {
      return name;
   }
   
   public void setFile(File file) {
      this.file = file;
   }
   
   public File getFile() {
      return file;  
   }
   
   public void setImported(boolean b) {
      isImported = b;  
   }
   
   public boolean isImported() {
      return isImported;  
   }
   
}
