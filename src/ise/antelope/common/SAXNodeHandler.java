package ise.antelope.common;

import java.awt.Point;
import java.io.File;
import java.io.FileReader;
import java.io.Reader;
import java.io.StringReader;
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

   public SAXNodeHandler( File in, SAXTreeNode root ) {
      infile = in;
      rootNode = root;
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
      if ( infile != null )
         child.setFile( infile );
      if ( qName.equals( "import" ) ) {
         int index = attributes.getIndex( "file" );
         if ( index > -1 ) {
            String filename = attributes.getValue( index );
            File f = new File( filename );
            if ( !f.exists() ) {
               f = new File( infile.getParent(), filename );
            }
            System.out.println("file = " + f.toString());
            if ( f.exists() ) {
               try {
                  System.out.println("attempting import");
                  InputSource source = new InputSource( new FileReader( f ) );
                  SAXParser parser = SAXParserFactory.newInstance().newSAXParser();
                  SAXNodeHandler handler = new SAXNodeHandler( f, getRoot() );
                  handler.setDocumentLocator( locator );
                  parser.parse( source, handler );
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
}

