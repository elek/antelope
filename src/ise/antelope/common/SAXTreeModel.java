package ise.antelope.common;

import java.awt.Point;
import javax.swing.tree.DefaultTreeModel;
import java.io.StringReader;
import java.io.File;
import java.io.FileReader;
import java.io.Reader;
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
   
   private static File infile = null;

   public SAXTreeModel( String xml ) {
      super( load( xml ) );
   }

   public SAXTreeModel( File xmlFile ) {
      super( load( xmlFile ) );
   }

   public static SAXTreeNode load( String xml ) {
      StringReader sr = new StringReader( xml );
      return load( sr );
   }

   public static SAXTreeNode load( File xmlFile ) {
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

   public static SAXTreeNode load( Reader reader ) {
      try {
         InputSource source = new InputSource( reader );
         SAXParser parser = SAXParserFactory.newInstance().newSAXParser();
         SAXNodeHandler handler = new SAXNodeHandler(infile);
         handler.setDocumentLocator( new LocatorImpl() );
         parser.parse( source, handler );
         return handler.getRoot();
      }
      catch ( Exception e ) {
         /// not so sure this one is noise...
         throw new RuntimeException(e);
         ///return null;
      }
   }
   
}
