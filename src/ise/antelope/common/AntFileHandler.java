// $Id$
/*
* Based on the Apache Software License, Version 1.1
*
* Copyright (c) 2002 Dale Anson.  All rights reserved.
*
* Redistribution and use in source and binary forms, with or without
* modification, are permitted provided that the following conditions
* are met:
*
* 1. Redistributions of source code must retain the above copyright
*    notice, this list of conditions and the following disclaimer.
*
* 2. Redistributions in binary form must reproduce the above copyright
*    notice, this list of conditions and the following disclaimer in
*    the documentation and/or other materials provided with the
*    distribution.
*
* 3. The end-user documentation included with the redistribution, if
*    any, must include the following acknowlegement:
*       "This product includes software developed by Dale Anson,
*        danson@users.sourceforge.net."
*    Alternately, this acknowlegement may appear in the software itself,
*    if and wherever such third-party acknowlegements normally appear.
*
* 4. The name "Antelope" must not be used to endorse or promote products derived
*    from this software without prior written permission. For written
*    permission, please contact danson@users.sourceforge.net.
*
* 5. Products derived from this software may not be called "Antelope"
*    nor may "Antelope" appear in their names without prior written
*    permission of Dale Anson.
*
* THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
* WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
* OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
* DISCLAIMED.  IN NO EVENT SHALL DALE ANSON OR ANY PROJECT
* CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
* SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
* LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
* USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
* ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
* OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
* OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
* SUCH DAMAGE.
* ====================================================================
*/


package ise.antelope.common;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.logging.Handler;
import java.util.logging.LogRecord;

public class AntFileHandler extends Handler {

   private FileWriter writer;
   private File file;
   private String lSep = System.getProperty( "line.separator" );

   /**
    * Overwrites any existing file.   
    * @param file the file to write log messages to.   
    */
   public AntFileHandler ( File file ) throws IOException {
      this( file, false );
   }

   /**
    * Write log messages to a file. Can overwrite an existing file or append to
    * it depending on the <code>append</code> attribute.
    * @param file the file to write log messages to.
    * @param append whether to append to an existing file.
    */
   public AntFileHandler ( File file, boolean append ) throws IOException {
      this.file = file;
      writer = new FileWriter( file, append );
   }

   /**
    * @return the absolute pathname of the file this handler writes to.
    */
   public String getFile() {
      return file.getAbsolutePath();
   }

   /**
    * Writes the log record to the file.
    *
    * @param record  a LogRecord
    */
   public void publish( LogRecord record ) {

      try {
         String line = record.getMessage();
         writer.write( line + lSep );
      }
      catch ( Exception ignored ) {}
   }

   /**
    * Logging can be turned off or on by setting a system property equal to the
    * namespace for the logger plus a dot plus the full file name, for example,
    * if the logging namespace is "foo" and the full file name is "/tmp/mylog.txt",
    * then calling <p>
    * <code>System.setProperty("foo" + "." + "/tmp/mylog.txt", "off") <code><p> 
    * will turn off logging and calling <p>
    * <code>System.setProperty("foo" + "." + "/tmp/mylog.txt", "on")</code><p>
    * will turn logging back on. This handler errs on the side of logging, if these
    * properties are not set, the record will be logged.
    * @param record the log record to check.   
    */
   public boolean isLoggable( LogRecord record ) {
      Object[] params = record.getParameters();
      if ( params == null || params.length == 0 )
         return true;

      // if not null, then first parameter should be the logger namespace
      String namespace = ( String ) params[ 0 ];

      // check system properties for key named namespace.filename
      String key = namespace + "." + getFile();
      String value = System.getProperty( key );
      if ( value == null )
         return true;

      // if value is off, false, or no, return false
      value = value.toLowerCase();
      if ( value.equals("off") || value.equals("false") || value.equals("no") ) 
         return false;
      else
         return true;
   }

   /**
    * Flushes the output stream.
    */
   public void flush() {
      try {
         writer.flush();
      }
      catch ( Exception ignored ) {}
   }

   /**
    * Closes the output stream. Nothing more can be written by this handler.
    */
   public void close() {
      try {
         writer.close();
      }
      catch ( Exception ignored ) {}
   }

}
