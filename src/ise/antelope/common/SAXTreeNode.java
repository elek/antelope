package ise.antelope.common;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeModel;
import java.awt.Point;
import java.io.File;
import org.xml.sax.Attributes;
import org.xml.sax.helpers.AttributesImpl;
import ise.library.TableArray;

/**
 * This tree node captures some information about the node location from the
 * SAX parser: the node name, the node attributes, and the node location in
 * the original xml document.
 * @author Dale Anson, danson@germane-software.com
 */
public class SAXTreeNode extends DefaultMutableTreeNode implements Cloneable, Comparable {

    private String name;
    private Point location;
    private Attributes attributes;
    private File file = null;
    private boolean isImported = false;
    private boolean isCalled = false;
    private boolean isTask = false;
    private boolean isType = false;
    private boolean isTarget = false;
    private boolean isDefaultTarget = false;
    private boolean isProject = false;
    protected int ordinal = 3;

    public static int PROJECT = 1;
    public static int TARGET = 2;
    public static int TYPE = 3;
    public static int TASK = 4;

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

    /**
     * Converts the org.xml.sax.Attributes to a TableArray.
     * The table has these columns in this order:
     * <ol>
     * <li>local name</li>
     * <li>qualified name</li>
     * <li>type</li>
     * <li>uri</li>
     * <li>value</li>
     * </ol>
     * The table array is indexed at (0, 0).
     * /// Is this any more useful than the Attributes themselves???
     * @return a TableArray containing the attributes for this node.
     */
    public TableArray getAttributeTable() {
        TableArray ta = new TableArray();
        Attributes attr = getAttributes();
        for ( int i = 0; i < attr.getLength(); i++ ) {
            ta.put( 0, i, attr.getLocalName( i ) );
            ta.put( 1, i, attr.getQName( i ) );
            ta.put( 2, i, attr.getType( i ) );
            ta.put( 3, i, attr.getValue( i ) );
        }
        return ta;
    }

    public String getAttributeValue( String name ) {
        if ( attributes == null )
            return null;
        int index = attributes.getIndex( name );
        return index > -1 ? attributes.getValue( index ) : null;
    }

    public Point getLocation() {
        return location;
    }

    public String getName() {
        return name;
    }

    /**
     * Indicates whether this node is a private target. Only makes sense if the node
     * is indeed a target.
     * @return true if any of these conditions are satisfied:<br>
     *    <ul>
     *    <li>The target name contains one or more "."</li>
     *    <li>The target name starts with a "-"</li>
     *    <li>The description attribute is null or empty</li>
     *    </ul>
     */
    public boolean isPrivate() {
        if ( !isTarget() )
            return false;
        String name = getAttributeValue( "name" );
        if ( name.indexOf( "." ) > 0 ) {
            return true;
        }
        if ( name.startsWith( "-" ) ) {
            return true;
        }
        String description = getAttributeValue( "description" );
        if ( description == null || description.equals( "" )
           ) {
            return true;
        }
        return false;
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

    public void setType( boolean b ) {
        isType = b;
        ordinal = TYPE;
    }

    public boolean isType() {
        return isType;
    }

    public void setTask( boolean b ) {
        isTask = b;
        ordinal = TASK;
    }

    public boolean isTask() {
        return isTask;
    }

    public void setTarget( boolean b ) {
        isTarget = b;
        ordinal = TARGET;
    }

    public boolean isTarget() {
        return isTarget;
    }

    public void setDefaultTarget( boolean b ) {
        isDefaultTarget = b;
    }

    public boolean isDefaultTarget() {
        return isDefaultTarget;
    }

    public void setProject( boolean b ) {
        isProject = b;
        ordinal = PROJECT;
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
        sb.append( isImported() ? "imported;" : "not imported;" );
        sb.append( isDefaultTarget() ? "default; " : "not default;" );
        sb.append( isCalled() ? "called]" : "not called]" );
        return sb.toString();
    }

    public int compareTo( Object o ) {
        if (o == null)
            return 1;
        SAXTreeNode node = (SAXTreeNode)o;
        int value = (new Integer(ordinal)).compareTo(new Integer(node.ordinal));
        if (value != 0)
            return  value;
        String name1 = getName();
        String name2 = node.getName();
        value = name1.compareTo(name2);
        if (value != 0)
            return value;
        name1 = getAttributeValue( "name" ) == null ? getName() : getAttributeValue( "name" );
        name2 = node.getAttributeValue( "name" ) == null ? node.getName() : node.getAttributeValue( "name" );
        value = name1.compareTo(name2);
        if (value != 0)
            return value;
        return toString().compareTo(node.toString());
    }
}
