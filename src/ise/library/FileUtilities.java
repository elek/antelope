package ise.library;

import java.io.*;

/**
 * Some file copy utilities. These are rock solid.
 * @author Dale Anson, danson@germane-software.com
 */
public class FileUtilities {

   /**
    * Copies one file to another. If destination file exists, it will be
    * overwritten.
    *
    * @param from           file to copy
    * @param to             where to put it
    * @exception Exception  most likely an IOException
    */
   public static void copyFile( File from, File to ) throws Exception {
      if ( !from.exists() )
         return;
      FileInputStream in = new FileInputStream( from );
      FileOutputStream out = new FileOutputStream( to );
      byte[] buffer = new byte[8192];
      int bytes_read;
      while ( true ) {
         bytes_read = in.read( buffer );
         if ( bytes_read == -1 )
            break;
         out.write( buffer, 0, bytes_read );
      }
      out.flush();
      out.close();
      in.close();
   }

   /**
    * Copies a stream to a file. If destination file exists, it will be
    * overwritten. The input stream will be closed when this method returns.
    *
    * @param from           stream to copy from
    * @param to             file to write
    * @exception Exception  most likely an IOException
    */
   public static void copyToFile( InputStream from, File to ) throws Exception {
      copyToFile( from, true, to );
   }

   /**
    * Copies a stream to a file. If destination file exists, it will be
    * overwritten. The input stream may be closed when this method returns.
    *
    * @param from           stream to copy from
    * @param to             file to write
    * @param close          whether to close the input stream when done
    * @exception Exception  most likely an IOException
    */
   public static void copyToFile( InputStream from, boolean close, File to ) throws Exception {
      FileOutputStream out = new FileOutputStream( to );
      byte[] buffer = new byte[8192];
      int bytes_read;
      while ( true ) {
         bytes_read = from.read( buffer );
         if ( bytes_read == -1 )
            break;
         out.write( buffer, 0, bytes_read );
      }
      out.flush();
      out.close();
      if ( close )
         from.close();
   }
   
   /**
    * Copies a stream to another stream. The input stream will be closed when
    * this method returns.
    *
    * @param from           stream to copy from
    * @param to             file to write
    * @exception Exception  most likely an IOException
    */
   public static void copyToStream( InputStream from, OutputStream to ) throws Exception {
      byte[] buffer = new byte[8192];
      int bytes_read;
      while ( true ) {
         bytes_read = from.read( buffer );
         if ( bytes_read == -1 )
            break;
         to.write( buffer, 0, bytes_read );
      }
      to.flush();
      from.close();
   }

   /**
    * Copies a reader to a writer. The reader will be closed when
    * this method returns.
    *
    * @param from           Reader to read from
    * @param to             Writer to write to
    * @exception Exception  most likely an IOException
    */
   public static void copyToWriter( Reader from, Writer to ) throws Exception {
      char[] buffer = new char[8192];
      int chars_read;
      while ( true ) {
         chars_read = from.read( buffer );
         if ( chars_read == -1 )
            break;
         to.write( buffer, 0, chars_read );
      }
      to.flush();
      from.close();
   }
}

