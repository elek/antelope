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
public class SAXTreeNode extends DefaultMutableTreeNode implements Cloneable {

   private String name;
   private Point location;
   private Attributes attributes;
   private File file = null;
   private boolean isImported = false;
   private boolean isCalled = false;
   private boolean isTask = false;
   private boolean isType = false;
   private boolean isTarget = false;
   private boolean isProject = false;

   public SAXTreeNode( String name, Point location, Attributes attr ) {
      this( name, location, attr, null );
   }

   public SAXTreeNode( String name, Point location, Attributes attr, File f ) {
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

   public void setFile( File file ) {
      this.file = file;
   }

   public File getFile() {
      return file;
   }

   /**
    * Set to true if this node has been imported by the &lt;import&gt; task.
    */
   public void setImported( boolean b ) {
      isImported = b;
   }

   /**
    * @return true if this node was imported by an &lt;import&gt; task.   
    */
   public boolean isImported() {
      return isImported;
   }

   /**
    * Set to true if this node was called by either the &lt;ant&gt; 
    * or &lt;antcall&gt; tasks.   
    */
   public void setCalled( boolean b ) {
      isCalled = b;
   }

   /**
    * @return true if this node is the target node for an &lt;ant&gt; 
    * or &lt;antcall&gt; task   
    */
   public boolean isCalled() {
      return isCalled;
   }
   
   public void setType(boolean b) {
      isType = b;  
   }
   
   public boolean isType() {
      return isType;  
   }
   
   public void setTask(boolean b) {
      isTask = b;  
   }
   
   public boolean isTask() {
      return isTask;  
   }
   
   public void setTarget(boolean b) {
      isTarget = b;  
   }
   
   public boolean isTarget() {
      return isTarget;  
   }
   
   public void setProject(boolean b) {
      isProject = b;  
   }
   
   public boolean isProject() {
      return isProject;  
   }

   public String toString() {
      StringBuffer sb = new StringBuffer();
      sb.append( "[" ).append( getName() ).append( ";" );
      if ( getAttributes() != null )
         sb.append( getAttributes().toString() ).append( ";" );
      if ( getFile() != null )
         sb.append( getFile().toString() ).append( ";" );
      sb.append(isImported() ? "imported;" : "not imported;");
      sb.append(isCalled() ? "called]" : "not called]");
      return sb.toString();
   }
}
