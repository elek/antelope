package ise.antelope.common;

import java.awt.Point;
import java.io.File;
import java.io.FileReader;
import java.io.Reader;
import java.io.StringReader;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Stack;
import java.util.logging.*;
import javax.swing.JOptionPane;
import javax.swing.tree.DefaultTreeModel;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import org.xml.sax.Attributes;

import org.xml.sax.InputSource;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;
import org.xml.sax.helpers.LocatorImpl;

/**
 * Builds a tree of SAXTreeNodes as a SAX parser reads an xml file. Handles 
 * files included with xml entity includes as well as files imported using the
 * &lt;import&gt; task introduced in Ant 1.6.
 *
 * @author    Dale Anson, danson@germane-software.com
 * @version   $Revision$
 */
public class SAXNodeHandler extends DefaultHandler {

    /** current locator */
    private Locator locator = null;
    /** another locator -- is this used any more? */
    private Locator docLocator = null;
    /** used to store nodes as the tree is being built */
    private Stack stack = new Stack();
    /** the root node of the tree */
    private SAXTreeNode rootNode = null;
    /** the source file  */
    private File infile = null;

    private boolean isImported = false;

    private HashMap propertyFiles = null;

    private Logger _logger = Logger.getLogger( "ise.antelope.Antelope" );

    // need to update occassionally as new versions of Ant are released.
    // these lists are from Ant 1.6.1.
    private List taskList = Arrays.asList( new String[] {"propertyfile", "importtypelib", "vsscheckin", "sql", "cvspass", "p4reopen", "csc", "dirname", "wlrun", "p4label", "p4revert", "replaceregexp", "get", "jjtree", "sleep", "jarlib-display", "dependset", "zip", "patch", "jspc", "style", "test", "tstamp", "unwar", "vsshistory", "icontract", "cvschangelog", "p4submit", "ccmcheckin", "p4change", "bzip2", "sync", "p4delete", "vssadd", "javadoc", "p4integrate", "translate", "signjar", "cclock", "chown", "vajload", "jarlib-available", "WsdlToDotnet", "buildnumber", "jpcovmerge", "ejbjar", "war", "rename", "sequential", "serverdeploy", "property", "subant", "move", "ildasm", "copydir", "cccheckin", "ccunlock", "wljspc", "fixcrlf", "sosget", "pathconvert", "record", "p4sync", "exec", "ccmklabel", "p4edit", "manifest", "maudit", "antlr", "netrexxc", "jpcovreport", "execon", "ccmcheckout", "ant", "xmlvalidate", "xslt", "p4resolve", "iplanet-ejbc", "ccmcheckintask", "gzip", "native2ascii", "ccrmtype", "starteam", "ear", "input", "presetdef", "rmic", "checksum", "mail", "loadfile", "vsscheckout", "stylebook", "soscheckin", "mimemail", "stlabel", "gunzip", "concat", "cab", "touch", "parallel", "splash", "antcall", "ccmkbl", "cccheckout", "typedef", "p4have", "filter", "xmlproperty", "import", "copy", "jsharpc", "symlink", "antstructure", "script", "ccmcreatetask", "rpm", "delete", "replace", "mmetrics", "attrib", "waitfor", "untar", "loadproperties", "available", "echoproperties", "chgrp", "vajexport", "bunzip2", "whichresource", "copyfile", "p4labelsync", "vsscreate", "macrodef", "ejbc", "unjar", "vbc", "wsdltodotnet", "mkdir", "condition", "cvs", "tempfile", "junitreport", "taskdef", "echo", "ccupdate", "java", "vsslabel", "renameext", "basename", "javadoc2", "tar", "vsscp", "vajimport", "p4fstat", "setproxy", "p4counter", "wlstop", "ilasm", "soscheckout", "apply", "ccuncheckout", "jarlib-resolve", "jlink", "cvstagdiff", "javacc", "chmod", "pvcs", "jarlib-manifest", "jar", "ccmklbtype", "sound", "scriptdef", "defaultexcludes", "mparse", "blgenclient", "uptodate", "jjdoc", "genkey", "javah", "ccmreconfigure", "fail", "unzip", "javac", "p4add", "jpcoverage", "soslabel", "depend", "vssget", "deltree", "ddcreator"} );
    private List typeList = Arrays.asList( new String[] {"patternset", "assertions", "propertyset", "filterset", "libfileset", "filterreader", "scriptfilter", "extension", "fileset", "dirset", "filelist", "filterchain", "path", "classfileset", "selector", "xmlcatalog", "description", "mapper", "zipfileset", "substitution", "extensionSet", "regexp"} );


    /** Constructor */
    public SAXNodeHandler() {
        this( null );
    }

    /**
     * Constructor
     *
     * @param in the source file
     */
    public SAXNodeHandler( File in ) {
        this( in, false );
    }

    public SAXNodeHandler( File in, boolean imported ) {
        infile = in;
        isImported = imported;
    }

    /**
     * Sets the locator attribute of the SAXNodeHandler object
     *
     * @param locator  The new locator value
     */
    public void setDocumentLocator( Locator locator ) {
        this.locator = locator;
    }


    /**
     * Gets the root node of the SAXNodeHandler object
     *
     * @return   The root value
     */
    public SAXTreeNode getRoot() {
        return rootNode;
    }


    /** Description of the Method */
    public void startDocument() {
        docLocator = new LocatorImpl( locator );
    }


    /**
     * Description of the Method
     *
     * @param uri               Description of the Parameter
     * @param localName         Description of the Parameter
     * @param qName             Description of the Parameter
     * @param attributes        Description of the Parameter
     * @exception SAXException  Description of the Exception
     */
    public void startElement( String uri, String localName, String qName, Attributes attributes ) throws SAXException {
        try {
            Locator l = new LocatorImpl( locator );
            SAXTreeNode child = new SAXTreeNode( qName, new Point( l.getLineNumber(), l.getColumnNumber() ), attributes );
            child.setImported( isImported );

            // maybe mark this element as a project, target, task, or type
            setKind( qName, child );

            // set the source file for the node. Elements from included files will
            // have a systemId set, elements in the base file will not, nor will
            // imported files.
            /// might need to deal with http as well as file
            if ( l.getSystemId() != null && l.getSystemId().startsWith( "file:" ) ) {
                String sid = l.getSystemId();
                if ( sid.startsWith( "file:" ) )
                    sid = sid.substring( "file:".length() );
                File f = new File( sid );
                if ( !f.exists() && infile != null ) {
                    File dir = infile;
                    if ( !dir.isDirectory() )
                        dir = dir.getParentFile();
                    f = new File( dir, f.getName() );
                }
                child.setFile( f );
            }
            else if ( infile != null ) {
                child.setFile( infile );
            }

            // for the <import> task, load the imported file
            if ( qName.equals( "import" ) ) {
                // verify the imported file exists
                int index = attributes.getIndex( "file" );
                if ( index > -1 ) {
                    String filename = attributes.getValue( index );
                    File f = new File( filename );
                    if ( !f.exists() ) {
                        f = new File( infile.getParent(), filename );
                    }
                    if ( f.exists() ) {
                        try {
                            // stash the current settings
                            SAXTreeNode old_root = getRoot();
                            Locator old_locator = locator;
                            Locator old_doc_locator = docLocator;
                            File old_infile = infile;
                            boolean old_is_imported = isImported;

                            // adjust the settings for the imported file
                            infile = f;
                            isImported = true;

                            // do the import
                            InputSource source = new InputSource( new FileReader( f ) );
                            SAXParser parser = SAXParserFactory.newInstance().newSAXParser();
                            SAXNodeHandler handler = new SAXNodeHandler( f, true );
                            parser.parse( source, handler );
                            child = handler.getRoot();

                            // restore the previous settings
                            rootNode = old_root;
                            locator = old_locator;
                            docLocator = old_doc_locator;
                            infile = old_infile;
                            isImported = old_is_imported;
                        }
                        catch ( Exception e ) {
                            StringBuffer sb = new StringBuffer();
                            sb.append( "<html>Error loading imported file: " ).append( f.getAbsolutePath() ).append( "<br>" );
                            sb.append( "at line number: " ).append( l.getLineNumber() ).append( ", col number: " ).append( l.getColumnNumber() ).append( "<p>" );
                            sb.append( "The specific error is: " ).append( e.getMessage() ).append( "<p>" );
                            sb.append( "Do you want to load the rest of the file?" );
                            int rtn = JOptionPane.showConfirmDialog( null, sb.toString(), "Error loading build file", JOptionPane.ERROR_MESSAGE );
                            if ( rtn == JOptionPane.NO_OPTION )
                                throw new SAXException( e );
                        }
                    }
                }
            }

            // maybe store some dependant property files
            if ( qName.equals( "property" ) && child.getAttributeValue( "file" ) != null ) {
                addPropertyFile( child, "file" );
            }
            else if ( qName.equals( "loadproperties" ) && child.getAttributeValue( "srcfile" ) != null ) {
                addPropertyFile( child, "srcfile" );
            }


            // add the child to the parent node
            if ( stack.empty() ) {
                rootNode = child;
            }
            else {
                SAXTreeNode parent = ( SAXTreeNode ) stack.peek();
                parent.add( child );
                // check if this node is the default target for the build file
                if ( child.isTarget() ) {
                    String default_target = parent.getAttributeValue( "default" );
                    String child_target = child.getAttributeValue( "name" );
                    if ( default_target != null && child_target != null && default_target.equals( child_target ) ) {
                        child.setDefaultTarget( true );
                    }
                }
            }


            stack.push( child );
        }
        catch ( SAXException se ) {
            StringBuffer sb = new StringBuffer();
            sb.append( "<html>Error loading build file " );
            sb.append( "at line number: " ).append( locator.getLineNumber() ).append( ", col number: " ).append( locator.getColumnNumber() ).append( "<p>" );
            sb.append( "The specific error is: " ).append( se.getMessage() ).append( "<p>" );
            throw new SAXException(sb.toString());
        }
    }


    /**
     * Handles the end of an element.
     *
     * @param uri               Description of the Parameter
     * @param localName         Description of the Parameter
     * @param qName             Description of the Parameter
     * @exception SAXException  Description of the Exception
     */
    public void endElement( String uri, String localName, String qName ) throws SAXException {
        stack.pop();
    }


    /**
     * Resolves an entity.
     *
     * @param publicId   the publicId
     * @param systemId   the systemId
     * @return          an InputSource
     */
    public InputSource resolveEntity( String publicId, String systemId ) {
        if ( systemId != null ) {
            if ( systemId.toLowerCase().startsWith( "http" ) ) {
                try {
                    InputSource is = new InputSource( new java.net.URL( systemId ).openStream() );
                    is.setPublicId( publicId );
                    is.setSystemId( systemId );
                    return is;
                }
                catch ( Exception e ) {
                    StringBuffer sb = new StringBuffer();
                    sb.append( "<html>Error loading included resource: " ).append( systemId ).append( "<br>" );
                    sb.append( "at line number: " ).append( locator.getLineNumber() ).append( ", col number: " ).append( locator.getColumnNumber() ).append( "<p>" );
                    sb.append( "The specific error is: " ).append( e.getMessage() ).append( "<p>" );
                    sb.append( "Do you want to load the rest of the file?" );
                    int rtn = JOptionPane.showConfirmDialog( null, sb.toString(), "Error loading build file", JOptionPane.ERROR_MESSAGE );
                    if ( rtn == JOptionPane.NO_OPTION )
                        throw new RuntimeException( e );
                    return null;
                }
            }
            else {
                String sid = new String( systemId );
                if ( sid.startsWith( "file:" ) )
                    sid = sid.substring( "file:".length() );
                File f;
                if (sid.startsWith("..")) {
                    // handle relative paths
                    if (infile == null)
                        return null;
                    if (!infile.isDirectory())
                        f = new File(infile.getParentFile().getParentFile(), sid.substring(2));
                    else
                        f = new File(infile.getParentFile(), sid.substring(2));
                }
                else {
                    // handle fully qualified name or in same directory
                    f = new File( sid );
                    if (!f.exists()) {
                        if (infile == null)
                            return null;
                        if (!infile.isDirectory())
                            f = new File(infile.getParentFile(), sid);
                        else
                            f = new File(infile, sid);
                    }
                }
                if ( f.exists() ) {
                    try {
                        InputSource is = new InputSource( new FileReader( f ) );
                        is.setPublicId( publicId );
                        is.setSystemId( systemId );
                        return is;
                    }
                    catch ( Exception e ) {
                        StringBuffer sb = new StringBuffer();
                        sb.append( "<html>Error loading included resource: " ).append( systemId ).append( "<br>" );
                        sb.append( "at line number: " ).append( locator.getLineNumber() ).append( ", col number: " ).append( locator.getColumnNumber() ).append( "<p>" );
                        sb.append( "The specific error is: " ).append( e.getMessage() ).append( "<p>" );
                        sb.append( "Do you want to load the rest of the file?" );
                        int rtn = JOptionPane.showConfirmDialog( null, sb.toString(), "Error loading build file", JOptionPane.ERROR_MESSAGE );
                        if ( rtn == JOptionPane.NO_OPTION )
                            throw new RuntimeException( e );
                        return null;
                    }
                }
                else {
                    try {
                        InputSource is = new InputSource( new java.net.URL( systemId ).openStream() );
                        is.setPublicId( publicId );
                        is.setSystemId( systemId );
                        System.out.println("is is " + is.toString());
                        return is;
                    }
                    catch ( Exception e ) {
                        StringBuffer sb = new StringBuffer();
                        sb.append( "<html>Error loading included resource: " ).append( systemId ).append( "<br>" );
                        sb.append( "at line number: " ).append( locator.getLineNumber() ).append( ", col number: " ).append( locator.getColumnNumber() ).append( "<p>" );
                        sb.append( "The specific error is: " ).append( e.getMessage() ).append( "<p>" );
                        sb.append( "Do you want to load the rest of the file?" );
                        int rtn = JOptionPane.showConfirmDialog( null, sb.toString(), "Error loading build file", JOptionPane.ERROR_MESSAGE );
                        if ( rtn == JOptionPane.NO_OPTION )
                            throw new RuntimeException( e );
                        return null;
                    }
                }
            }
        }
        return null;
    }

    /**
     * Sets the kind attribute of the SAXTreeNode depending on if it is a project,
     * target, task, or type.
     *
     * @param qname  the name of the node
     * @param node   the node itself
     */
    private void setKind( String qname, SAXTreeNode node ) {
        if ( qname.equals( "project" ) )
            node.setProject( true );
        else if ( qname.equals( "target" ) )
            node.setTarget( true );
        else if ( taskList.contains( qname ) )
            node.setTask( true );
        else if ( typeList.contains( qname ) )
            node.setType( true );
    }

    /**
     * Add a file and timestamp to the propertyFile hashmap.   
     */
    private void addPropertyFile( SAXTreeNode child, String attr_name ) {
        if ( propertyFiles == null )
            propertyFiles = new HashMap();
        String filename = child.getAttributeValue( attr_name );
        File f = new File( filename );
        if ( !f.exists() ) {
            if ( child.getFile() != null ) {
                f = new File( child.getFile().getParentFile(), filename );
            }
        }
        if ( f.exists() )
            propertyFiles.put( f, new Long( f.lastModified() ) );
        else {
            propertyFiles.put(filename, new Long(0));
            //_logger.warning("Warning:" + Constants.NL + "Property file " + filename + " not found, will attempt to resolve later.");
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
}

