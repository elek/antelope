package ise.antelope.common;

import java.awt.Point;
import javax.swing.tree.DefaultTreeModel;
import java.io.StringReader;
import java.io.File;
import java.io.FileReader;
import java.io.Reader;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Stack;

import org.xml.sax.InputSource;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.parsers.SAXParser;
import org.xml.sax.InputSource;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.LocatorImpl;
import org.xml.sax.helpers.DefaultHandler;
import org.xml.sax.Attributes;

/**
 * This tree model loads an XML document or fragment from either a file
 * or a String.
 * @author Dale Anson, danson@germane-software.com
 */
public class SAXTreeModel extends DefaultTreeModel {

   private File infile = null;
   private HashMap propertyFiles = null;

   /**
    * Creates a tree model based on the given xml.   
    */
   public SAXTreeModel( String xml ) {
      super( null );
      setRoot( load( xml ) );
   }

   public SAXTreeModel( File xmlFile ) {
      super( null );
      setRoot( load( xmlFile ) );
   }

   public SAXTreeNode load( String xml ) {
      StringReader sr = new StringReader( xml );
      return load( sr );
   }

   public SAXTreeNode load( File xmlFile ) {
      try {
         infile = xmlFile;
         FileReader fr = new FileReader( xmlFile );
         return load( fr );
      }
      catch ( Exception e ) {
         // don't rethrow the exception, it is almost always noise about nothing
         //throw new RuntimeException(e);
         return null;
      }
   }

   public SAXTreeNode load( Reader reader ) {
      try {
         InputSource source = new InputSource( reader );
         SAXParser parser = SAXParserFactory.newInstance().newSAXParser();
         SAXNodeHandler handler = new SAXNodeHandler( infile );
         handler.setDocumentLocator( new LocatorImpl() );
         parser.parse( source, handler );
         propertyFiles = handler.getPropertyFiles();
         return handler.getRoot();
      }
      catch ( Exception e ) {
         /// not so sure this one is noise...
         throw new RuntimeException( e );
         ///return null;
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
}
