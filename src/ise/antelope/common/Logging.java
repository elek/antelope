/*
* The Apache Software License, Version 1.1
*
* Copyright (c) 2001-2002 The Apache Software Foundation.  All rights
* reserved.
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
*       "This product includes software developed by the
*        Apache Software Foundation (http://www.apache.org/)."
*    Alternately, this acknowlegement may appear in the software itself,
*    if and wherever such third-party acknowlegements normally appear.
*
* 4. The names "The Jakarta Project", "Ant", and "Apache Software
*    Foundation" must not be used to endorse or promote products derived
*    from this software without prior written permission. For written
*    permission, please contact apache@apache.org.
*
* 5. Products derived from this software may not be called "Apache"
*    nor may "Apache" appear in their names without prior written
*    permission of the Apache Group.
*
* THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
* WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
* OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
* DISCLAIMED.  IN NO EVENT SHALL THE APACHE SOFTWARE FOUNDATION OR
* ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
* SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
* LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
* USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
* ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
* OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
* OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
* SUCH DAMAGE.
* ====================================================================
*
* This software consists of voluntary contributions made by many
* individuals on behalf of the Apache Software Foundation.  For more
* information on the Apache Software Foundation, please see
* <http://www.apache.org/>.
*/
package ise.antelope.common;

import org.apache.tools.ant.Task;
import org.apache.tools.ant.TaskContainer;
import org.apache.tools.ant.BuildException;

import java.io.File;
import java.util.Vector;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.logging.Logger;



/**
 * Creates file loggers on the fly. Uses the logging facilities from the 
 * java.util.logging package. A new logger is created like this:<p>
 * &lt;logger filename="/tmp/logfile.txt" namespace="foo" append="true"/&gt;
 * <p>
 * Logging can be turned off like this:<p>
 * &lt;logger filename="/tmp/logfile.txt" namespace="foo" write="false"/&gt;
 * <p>and back on by:<p>
 * &lt;logger filename="/tmp/logfile.txt" namespace="foo" write="false"/&gt;
 * <p>
 * Defaults are to append (that is, do not overwrite an existing file) and to
 * write. Only the first usage of the 'append' attribute counts, subsequent
 * uses are ignored, while 'write' can be turned on or off as many times as
 * desired.
 *
 * @author Dale Anson, danson@germane-software.com
 * @since Ant 1.5
 */
public class Logging extends Task {

   // attribute storage
   private String namespace = AntLogger.NAMESPACE;
   private File file = null;
   private String append = null;
   private String write = null;

   // list of files that already have handlers.
   HashMap filenames = new HashMap();

   /**
    * Set the filename to log to. Required.
    * @param name the filename.
    */
   public void setFile( File file ) {
      this.file = file;
   }

   /**
    * Set the value of the property. Default is true.
    * @param value the value of the property.
    */
   public void setAppend( String append ) {
      this.append = append;
   }

   /**
    * Should the underlying handler write or not? This is the "on/off" switch
    * for the logger. Optional, default is "on".
    * @param write Ant boolean   
    */
   public void setWrite( String write ) {
      this.write = write;
   }

   /**
    * Execute this task.
    */
   public void execute() throws BuildException {
      // check for the required filename attribute
      if ( file == null  )
         throw new BuildException( "The 'file' attribute is required." );

      // check for the required namespace attribute
      if ( namespace == null || namespace.equals( "" ) )
         throw new BuildException( "The 'namespace' attribute is required." );

      // check for the optional attributes 'write' and 'append'
      boolean doAppend = true;
      boolean doWrite = true;
      if ( append != null )
         doAppend = getProject().toBoolean( append );
      if ( write != null )
         doWrite = getProject().toBoolean( write );
      else {
         write = "true";
         doWrite = true;
      }

      // check if a handler needs to be created
      if ( filenames.get( file.getAbsolutePath() ) == null ) {
         try {
            AntFileHandler handler = new AntFileHandler( file, doAppend );
            filenames.put( file.getAbsolutePath(), file );
            Logger logger = Logger.getLogger( namespace );
            logger.addHandler( handler );
         }
         catch ( Exception ignored ) {}
      }

      System.setProperty( namespace + "." + file.getAbsolutePath(), write );

   }
}
