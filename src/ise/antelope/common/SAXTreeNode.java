package ise.antelope.common;

import javax.swing.tree.DefaultMutableTreeNode;
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
public class SAXTreeNode extends DefaultMutableTreeNode {

   private String name;
   private Point location;
   private Attributes attributes;
   private File file = null;

   public SAXTreeNode( String name, Point location, Attributes attr ) {
      super( name );
      this.name = name;
      this.location = location;
      if ( attr != null )
         this.attributes = new AttributesImpl( attr );
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

}
