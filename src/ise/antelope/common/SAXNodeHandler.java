package ise.antelope.common;

import java.awt.Point;
import java.io.File;
import java.io.FileReader;
import java.io.Reader;
import java.io.StringReader;
import java.util.Arrays;
import java.util.List;
import java.util.Stack;
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
 * Builds a tree of TreeNodes as a SAX parser reads an xml file.
 *
 * @author    Dale Anson, danson@germane-software.com
 * @version   $Revision$
 */
public class SAXNodeHandler extends DefaultHandler {

   /** Description of the Field */
   private Locator locator = null;
   /** Description of the Field */
   private Locator docLocator = null;
   /** Description of the Field */
   private Stack stack = new Stack();
   /** Description of the Field */
   private SAXTreeNode rootNode = null;
   /** Description of the Field */
   private File infile = null;
   
   private boolean isImported = false;

   private List taskList = Arrays.asList(new String[]{"propertyfile", "importtypelib", "vsscheckin", "sql", "cvspass", "p4reopen", "csc", "dirname", "wlrun", "p4label", "p4revert", "replaceregexp", "get", "jjtree", "sleep", "jarlib-display", "dependset", "zip", "patch", "jspc", "style", "test", "tstamp", "unwar", "vsshistory", "icontract", "cvschangelog", "p4submit", "ccmcheckin", "p4change", "bzip2", "sync", "p4delete", "vssadd", "javadoc", "p4integrate", "translate", "signjar", "cclock", "chown", "vajload", "jarlib-available", "WsdlToDotnet", "buildnumber", "jpcovmerge", "ejbjar", "war", "rename", "sequential", "serverdeploy", "property", "subant", "move", "ildasm", "copydir", "cccheckin", "ccunlock", "wljspc", "fixcrlf", "sosget", "pathconvert", "record", "p4sync", "exec", "ccmklabel", "p4edit", "manifest", "maudit", "antlr", "netrexxc", "jpcovreport", "execon", "ccmcheckout", "ant", "xmlvalidate", "xslt", "p4resolve", "iplanet-ejbc", "ccmcheckintask", "gzip", "native2ascii", "ccrmtype", "starteam", "ear", "input", "presetdef", "rmic", "checksum", "mail", "loadfile", "vsscheckout", "stylebook", "soscheckin", "mimemail", "stlabel", "gunzip", "concat", "cab", "touch", "parallel", "splash", "antcall", "ccmkbl", "cccheckout", "typedef", "p4have", "filter", "xmlproperty", "import", "copy", "jsharpc", "symlink", "antstructure", "script", "ccmcreatetask", "rpm", "delete", "replace", "mmetrics", "attrib", "waitfor", "untar", "loadproperties", "available", "echoproperties", "chgrp", "vajexport", "bunzip2", "whichresource", "copyfile", "p4labelsync", "vsscreate", "macrodef", "ejbc", "unjar", "vbc", "wsdltodotnet", "mkdir", "condition", "cvs", "tempfile", "junitreport", "taskdef", "echo", "ccupdate", "java", "vsslabel", "renameext", "basename", "javadoc2", "tar", "vsscp", "vajimport", "p4fstat", "setproxy", "p4counter", "wlstop", "ilasm", "soscheckout", "apply", "ccuncheckout", "jarlib-resolve", "jlink", "cvstagdiff", "javacc", "chmod", "pvcs", "jarlib-manifest", "jar", "ccmklbtype", "sound", "scriptdef", "defaultexcludes", "mparse", "blgenclient", "uptodate", "jjdoc", "genkey", "javah", "ccmreconfigure", "fail", "unzip", "javac", "p4add", "jpcoverage", "soslabel", "depend", "vssget", "deltree", "ddcreator"});
   private List typeList = Arrays.asList(new String[]{"patternset", "assertions", "propertyset", "filterset", "libfileset", "filterreader", "scriptfilter", "extension", "fileset", "dirset", "filelist", "filterchain", "path", "classfileset", "selector", "xmlcatalog", "description", "mapper", "zipfileset", "substitution", "extensionSet", "regexp"});
   
   
   /** Description of the Method */
   public SAXNodeHandler() {
      this( null );
   }

   /**
    * Description of the Method
    *
    * @param in
    */
   public SAXNodeHandler( File in ) {
      infile = in;
   }

   /**
    * Sets the documentLocator attribute of the SAXNodeHandler object
    *
    * @param locator  The new documentLocator value
    */
   public void setDocumentLocator( Locator locator ) {
      this.locator = locator;
   }


   /**
    * Gets the root attribute of the SAXNodeHandler object
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
      Locator l = new LocatorImpl( locator );
      SAXTreeNode child = new SAXTreeNode( qName, new Point( l.getLineNumber(), l.getColumnNumber() ), attributes );
      child.setImported(isImported);
      setKind(qName, child);
      if ( infile != null )
         child.setFile( infile );
      
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
                  parser.parse( source, this );

                  // restore the previous settings
                  rootNode = old_root;
                  locator = old_locator;
                  docLocator = old_doc_locator;
                  infile = old_infile;
                  isImported = old_is_imported;
               }
               catch ( Exception e ) {
                  e.printStackTrace();
               }
            }
         }
      }
      
      if ( stack.empty() ) {
         rootNode = child;
      }
      else {
         SAXTreeNode parent = ( SAXTreeNode ) stack.peek();
         parent.add( child );
      }
      stack.push( child );
   }


   /**
    * Description of the Method
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
    * Description of the Method
    *
    * @param name              Description of the Parameter
    * @param publicId          Description of the Parameter
    * @param systemId          Description of the Parameter
    * @exception SAXException  Description of the Exception
    */
   public void notationDecl( String name, String publicId, String systemId ) throws SAXException {}


   /**
    * Description of the Method
    *
    * @param name              Description of the Parameter
    * @param publicId          Description of the Parameter
    * @param systemId          Description of the Parameter
    * @param noticationName    Description of the Parameter
    * @exception SAXException  Description of the Exception
    */
   public void unparsedEntityDecl( String name, String publicId, String systemId, String noticationName ) throws SAXException {}

   /**
    * Description of the Method
    *
    * @param publicId
    * @param systemId
    * @return          Description of the Returned Value
    */
   public InputSource resolveEntity( String publicId, String systemId ) {
      //System.out.println( "infile = " + infile );
      //System.out.println( "systemId = " + systemId );
      if ( systemId != null ) {
         if ( systemId.toLowerCase().startsWith( "http" ) ) {
            try {
               return new InputSource( new java.net.URL( systemId ).openStream() );
            }
            catch ( Exception e ) {
               return null;
            }
         }
         else {
            if ( systemId.startsWith( "file:" ) )
               systemId = systemId.substring( "file:".length() );
            File f = new File( systemId );
            //System.out.println( "file = " + f.toString() );
            if ( !f.exists() && infile != null ) {
               File dir = infile;
               if ( !dir.isDirectory() )
                  dir = dir.getParentFile();
               f = new File( dir, f.getName() );
               //System.out.println( "file = " + f.toString() );
               try {
                  return new InputSource( new java.io.FileInputStream( f ) );
               }
               catch ( Exception e ) {
                  return null;
               }
            }
            else {
               try {
                  return new InputSource( new java.net.URL( systemId ).openStream() );
               }
               catch ( Exception e ) {
                  return null;
               }
            }
         }
      }
      return null;
   }
   
   private void setKind(String qname, SAXTreeNode node) {
      if (qname.equals("project"))
         node.setProject(true);
      else if (qname.equals("target"))
         node.setTarget(true);
      else if (taskList.contains(qname))
         node.setTask(true);
      else if (typeList.contains(qname))
         node.setType(true);
   }
}

