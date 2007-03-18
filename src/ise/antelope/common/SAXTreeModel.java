package ise.antelope.common;

import java.awt.Point;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.DefaultMutableTreeNode;
import java.io.*;
import java.util.*;
import java.util.logging.*;

import org.xml.sax.InputSource;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.parsers.SAXParser;
import org.xml.sax.*;
import org.xml.sax.helpers.*;
import javax.swing.JOptionPane;

/**
 * This tree model loads an XML document or fragment from either a file
 * or a String.
 * @author Dale Anson, danson@germane-software.com
 */
public class SAXTreeModel extends DefaultTreeModel {

    private File infile = null;
    private HashMap propertyFiles = null;
    private Logger _logger = Logger.getLogger( "ise.antelope.Antelope" );

    /**
     * Creates a tree model based on the given xml.
     */
    public SAXTreeModel( String xml ) {
        super( null );
        setRoot( load( xml ) );
        try {
            validate();
        }
        catch ( BuildFileException e ) {
            _logger.warning( "Warning:" + Constants.NL + e.getMessage() );
        }
    }

    /**
     * Creates a tree model based on the given xml file.
     * @param the build/xml file to load
     */
    public SAXTreeModel( File xmlFile ) {
        super( null );
        setRoot( load( xmlFile ) );
        try {
            validate();
        }
        catch ( BuildFileException e ) {
            _logger.warning( "Warning:" + Constants.NL + e.getMessage() );
        }
    }

    public SAXTreeNode load( String xml ) {
        try {
            StringReader sr = new StringReader( xml );
            return load( sr );
        }
        catch ( Exception e ) {
            StringBuffer sb = new StringBuffer();
            sb.append( "<html>Error loading xml " ).append( "<p>" );
            sb.append( "The specific error is: " ).append( e.getMessage() ).append( "<p>" );
            _logger.severe( sb.toString() );
            return null;
        }
    }

    public SAXTreeNode load( File xmlFile ) {
        try {
            infile = xmlFile;
            FileReader fr = new FileReader( xmlFile );
            return load( fr );
        }
        catch ( FileNotFoundException fnf ) {
            StringBuffer sb = new StringBuffer();
            sb.append( "<html>Error loading build file: " ).append( xmlFile.getAbsolutePath() ).append( "<br>" );
            sb.append( "file not found!" );
            _logger.severe( sb.toString() );
            fnf.printStackTrace();
            return null;
        }
        catch ( SAXParseException spe ) {
            StringBuffer sb = new StringBuffer();
            sb.append( "<html>Error loading build file: " ).append( xmlFile.getAbsolutePath() ).append( "<br>" );
            sb.append( "somewhere at or before line number: " ).append( spe.getLineNumber() ).append( ", and column number: " ).append( spe.getColumnNumber() ).append( "<p>" );
            sb.append( "The specific error is: " ).append( spe.getMessage() ).append( "<p>" );
            _logger.severe( sb.toString() );
            spe.printStackTrace();
            return null;
        }
        catch ( Exception se ) {
            StringBuffer sb = new StringBuffer();
            sb.append( "<html>Error loading build file: " ).append( xmlFile.getAbsolutePath() ).append( "<br>" );
            sb.append( "The specific error is: " ).append( se.getMessage() ).append( "<p>" );
            _logger.severe( sb.toString() );
            se.printStackTrace();
            return null;
        }
    }

    private SAXTreeNode load( Reader reader ) throws Exception {
        InputSource source = new InputSource( reader );
        SAXParser parser = SAXParserFactory.newInstance().newSAXParser();
        SAXNodeHandler handler = new SAXNodeHandler( infile );
        handler.setDocumentLocator( new LocatorImpl() );
        parser.parse( source, handler );
        propertyFiles = handler.getPropertyFiles();
        SAXTreeNode root = handler.getRoot();
        sort( root );
        return root;
    }

    private void sort( SAXTreeNode root ) {
        if ( root == null )
            return ;
        if (root.getChildCount() == 0)
            return;
        List children = new ArrayList();
        Enumeration en = root.children();
        while ( en.hasMoreElements() ) {
            SAXTreeNode child = (SAXTreeNode)en.nextElement();
            children.add( child );
        }
        Collections.sort( children );
        root.removeAllChildren();
        for (Iterator it = children.iterator(); it.hasNext(); ) {
            SAXTreeNode child = (SAXTreeNode)it.next();
            root.add( child );
            sort( child );
        }
    }

    /**
     * Property files are files that properties are loaded from, for example, from
     * a property task with a file attribute or a loadproperties task. The
     * propertyFile hashmap keeps track of those files and the time the property file
     * was last modified. This can be used to determine if a build file should be
     * reloaded.
     * @return a hashmap containing a File as key and its last modified time
     * as value as a Long. May be null.
     */
    public HashMap getPropertyFiles() {
        return propertyFiles;
    }

    /**
     * @return true if any file tracked by getPropertyFiles is newer than when it
     * was first looked at.
     */
    public boolean shouldReload() {
        if ( propertyFiles == null )
            return false;
        Iterator it = propertyFiles.keySet().iterator();
        while ( it.hasNext() ) {
            File f = ( File ) it.next();
            Long lastModified = ( Long ) propertyFiles.get( f );
            if ( lastModified != null && lastModified.longValue() != f.lastModified() )
                return true;
        }
        return false;
    }

    /**
     * Returns a map of targets in the current build file, including any imported
     * or included targets from other files.  Key is the target name,
     * value is a SAXTreeNode.
     * @return a list of targets in the current build file. Key is the target name,
     * value is a SAXTreeNode.
     */
    public Map getTargets() {
        // list of targets, key is target name, value is a SAXTreeNode representing a target
        Map list = new LinkedHashMap();

        // project children
        Enumeration en = ( ( SAXTreeNode ) getRoot() ).children();

        while ( en.hasMoreElements() ) {
            SAXTreeNode child = ( SAXTreeNode ) en.nextElement();
            // add target directly
            if ( child.isTarget() )
                list.put( child.getAttributeValue( "name" ), child );
            // add subproject targets
            else if ( child.isProject() )
                addSubProjectTargets( child, list );
        }
        return list;
    }

    private void addSubProjectTargets( SAXTreeNode project, Map list ) {
        Enumeration en = project.children();
        String project_name = project.getAttributeValue( "name" );
        if ( project_name == null )
            project_name = "";
        while ( en.hasMoreElements() ) {
            SAXTreeNode child = ( SAXTreeNode ) en.nextElement();
            // add target directly
            if ( child.isTarget() ) {
                String name = getPQName( child );
                list.put( getPQName( child ), child );
            }
            // add subproject targets
            else if ( child.isProject() )
                addSubProjectTargets( child, list );
        }
    }

    /**
     * Currently validates the targets in the build file that this model represents. Does nothing
     * if the file is not an Ant build file, otherwise, checks each target for a "depends"
     * attribute and checks that each dependant target actually exists. Eventually will
     * perform additional validation.
     * @exception BuildFileException on any error found in the build file.
     */
    private void validate() throws BuildFileException {
        StringBuffer sb = new StringBuffer();

        // check target dependencies for each target
        Map targets = getTargets();
        Iterator it = targets.keySet().iterator();
        while ( it.hasNext() ) {
            String name = ( String ) it.next();
            SAXTreeNode node = ( SAXTreeNode ) targets.get( name );
            String depends = node.getAttributeValue( "depends" );
            if ( depends != null ) {
                StringTokenizer st = new StringTokenizer( depends, "," );

                // check that each dependant target exists
                while ( st.hasMoreTokens() ) {
                    String dname = st.nextToken().trim();
                    SAXTreeNode dnode = ( SAXTreeNode ) targets.get( dname );
                    if ( dnode == null ) {
                        // try to find the target using the project-qualified name
                        String pn = getPQName( node );
                        dname = pn.substring( 0, pn.lastIndexOf( "." ) + 1 ) + dname;
                        dnode = ( SAXTreeNode ) targets.get( dname );
                    }
                    if ( dnode == null ) {
                        sb.append( "Target \"" ).append( name ).append( "\" depends on non-existant target \"" ).append( dname ).append( "\"." ).append( Constants.NL );
                    }
                }
            }
        }
        if ( sb.length() > 0 )
            throw new BuildFileException( sb.toString() );
    }

    /**
     * Returns the project-qualified name of the given node, e.g. projectname.targetname
     * @return the project-qualified name of the given node.
     */
    public String getPQName( SAXTreeNode node ) {
        if ( node.isProject() ) {
            return node.getAttributeValue( "name" );
        }
        else if ( node.isTarget() ) {
            String pn = ( ( SAXTreeNode ) node.getParent() ).getAttributeValue( "name" );
            return pn + "." + node.getAttributeValue( "name" );
        }
        else {
            SAXTreeNode parent = ( SAXTreeNode ) node.getParent();
            String rtn = node.getName();
            while ( parent != null ) {
                if ( parent.isTarget() ) {
                    rtn = parent.getAttributeValue( "name" ) + "." + rtn;
                }
                else if ( parent.isProject() ) {
                    return parent.getAttributeValue( "name" ) + "." + rtn;
                }
                else
                    rtn = parent.getName() + "." + rtn;
                parent = ( SAXTreeNode ) parent.getParent();
            }
            return rtn;
        }
    }
}
