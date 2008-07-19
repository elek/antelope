/*
*  The Apache Software License, Version 1.1
*
*  Copyright (c) 2000-2002 The Apache Software Foundation.  All rights
*  reserved.
*
*  Redistribution and use in source and binary forms, with or without
*  modification, are permitted provided that the following conditions
*  are met:
*
*  1. Redistributions of source code must retain the above copyright
*  notice, this list of conditions and the following disclaimer.
*
*  2. Redistributions in binary form must reproduce the above copyright
*  notice, this list of conditions and the following disclaimer in
*  the documentation and/or other materials provided with the
*  distribution.
*
*  3. The end-user documentation included with the redistribution, if
*  any, must include the following acknowlegement:
*  "This product includes software developed by the
*  Apache Software Foundation (http://www.apache.org/)."
*  Alternately, this acknowlegement may appear in the software itself,
*  if and wherever such third-party acknowlegements normally appear.
*
*  4. The names "The Jakarta Project", "Ant", and "Apache Software
*  Foundation" must not be used to endorse or promote products derived
*  from this software without prior written permission. For written
*  permission, please contact apache@apache.org.
*
*  5. Products derived from this software may not be called "Apache"
*  nor may "Apache" appear in their names without prior written
*  permission of the Apache Group.
*
*  THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
*  WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
*  OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
*  DISCLAIMED.  IN NO EVENT SHALL THE APACHE SOFTWARE FOUNDATION OR
*  ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
*  SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
*  LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
*  USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
*  ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
*  OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
*  OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
*  SUCH DAMAGE.
*  ====================================================================
*
*  This software consists of voluntary contributions made by many
*  individuals on behalf of the Apache Software Foundation.  For more
*  information on the Apache Software Foundation, please see
*  <http://www.apache.org/>.
*/
package ise.antelope.common;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintStream;
import java.io.StringReader;
import java.io.File;

import java.util.Hashtable;
import java.util.Iterator;
import java.util.StringTokenizer;

import java.util.logging.Logger;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Formatter;
import java.util.logging.Handler;
import java.util.logging.ConsoleHandler;
import java.util.logging.SimpleFormatter;

import org.apache.tools.ant.Project;
import org.apache.tools.ant.BuildEvent;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.util.StringUtils;
import org.apache.tools.ant.util.DateUtils;

import ise.library.Log;

/**
 * Writes build events to a java.util.logging.Logger. This is Ant's
 * DefaultLogger modified for Antelope. Log messages have associated Level:<br>
 * <ul>
 *   <li> error = build failed
 *   <li> warning = build succeeded
 *   <li> config = build started, task, target
 *   <li> info = all else
 * </ul>
 * This logger logs all messages to a java.util.logging.Logger. It uses a custom
 * print stream to intercept System.err and System.out. Users can set output
 * streams via <code>setOutputPrintStream</code> and <code>setErrorPrintStream</code>
 * , however, all messages go to both streams.<p>
 *
 * This logger is selfish -- all other loggers in the namespace for this logger will
 * be forcibly removed from the logging framework and only loggers added by this logger
 * will be added to the logging framework.<p>
 *
 * This logger will react to certain properties if set in the project:<br>
 *
 * <ul>
 *   <li> property name: antlogger.echo, value=true/false, mimics calling
 *   setEcho
 *   <li> property name: antlogger.namespace, value=string, mimics calling
 *   setNamespaceForLogger
 *   <li> property name: antlogger.file, value=filename, if used, will add a
 *   java.util.logging.FileHandler to the current Logger. If file doesn't exist,
 *   it will be created. If the file does exist, it will be overwritten unless
 *   the next property is true.
 *   <li> property name: antlogger.file.append, value=true/false, default is
 *   false. If true, output will be appended to the file set in the
 *   antlogger.file property. If false, the file will be overwritten.
 * </ul>
 * <br>
 * These properties are read and set at the start of each call to <code>buildStarted</code>
 * .
 *
 * @author    Matt Foemmel
 * @author    Dale Anson
 * @created   August 16, 2002
 */
public class AntLogger implements org.apache.tools.ant.BuildLogger {

   // some spaces for nice indenting
   private String targetIndent = "   ";
   private String taskIndent = targetIndent + targetIndent;
   private String msgIndent = taskIndent + targetIndent;

   /**
    * Storage for real System.out.
    */
   private PrintStream systemOut;

   /**
    * Storage for real System.err.
    */
   private PrintStream systemErr;

   /**
    * Storage for user defined output.
    */
   private PrintStream out = null;

   /**
    * Storage for user defined output.
    */
   private PrintStream err = null;

   /**
    * Should output be echoed to System.out and System.err? Default is false.
    */
   private boolean echo = false;

   /**
    * Lowest level of message to write out
    */
   protected int msgOutputLevel = Project.MSG_INFO;

   /**
    * Time of the start of the build
    */
   private long startTime = System.currentTimeMillis();

   /**
    * Convenience for local system line separator
    */
   protected final static String lSep = System.getProperty( "line.separator" );

   /**
    * Whether or not to show build events, default is true.
    */
   public boolean SHOW_BUILD_EVENTS = true;

   /**
    * Whether or not to show target events, default is false.
    */
   public boolean SHOW_TARGET_EVENTS = false;

   /**
    * Whether or not to show task events, default is false.
    */
   public boolean SHOW_TASK_EVENTS = false;

   /**
    * Whether or not to show log messages, default is true.
    */
   public boolean SHOW_LOG_MSGS = true;

   /**
    * Namespace for logger
    */
   public static String NAMESPACE = "ise.antelope.Antelope";

   /**
    * java.util.logging.Logger for namespace
    */
   private Logger logger = Logger.getLogger( NAMESPACE );

   /**
    * AntFileHandler is a handler that can be "turned on" by setting properties.
    */
   private AntFileHandler fileHandler = null;

   /**
    * LogRecords can have parameters, the namespace is added as the only parameter.
    */
   private Object[] parameters = new String[]{NAMESPACE};

   /**
    * Boolean to tell if this logger is open for business.
    */
   private boolean open = false;


   /**
    * Default constructor.
    */
   public AntLogger() {
      this( NAMESPACE );
   }

   /**
    * Constructor.
    * @param namespace namespace for java.util.logging.Logger
    */
   public AntLogger( String namespace ) {
      NAMESPACE = namespace;
      initLogger();
      ///open();
   }


   /**
    * Opens this logger for logging.  Intercepts standard in and out.
    */
   public void open() {
      if ( !open ) {
         systemOut = System.out;
         systemErr = System.err;
         PrintStream ps_out = createPrintStream();
         PrintStream ps_err = createPrintStream();
         System.setOut( ps_out );
         System.setErr( ps_err );
         open = true;
      }
   }


   /**
    * Closes this logger. As logger replaces System.out and System.err with custom
    * print streams, calling this method resets System.out and System.err to their
    * original values.
    */
   public void close() {
      if ( !open )
         return ;
      try {
         System.out.flush();
         System.err.flush();
         System.setOut( systemOut );
         System.setErr( systemErr );
         if ( fileHandler != null ) {
            fileHandler.close();
         }
      }
      catch ( Exception ignored ) {}
      open = false;
   }


   /**
    * Sets the highest level of message this logger should respond to. Only
    * messages with a message level lower than or equal to the given level should
    * be written to the log. <P>
    *
    * Constants for the message levels are in the Ant Project class. The order of
    * the levels, from least to most verbose, is <code>MSG_ERR</code>, <code>MSG_WARN</code>
    * , <code>MSG_INFO</code>, <code>MSG_VERBOSE</code>, <code>MSG_DEBUG</code>.
    * <P>
    *
    * The default message level for DefaultLogger is Project.MSG_INFO.
    *
    * @param level  the logging level for the logger.
    */
   public void setMessageOutputLevel( int level ) {
      this.msgOutputLevel = level;
   }


   /**
    * <strong>Does nothing!</strong>
    *
    * @param outputStream  ignored
    */
   public void setOutputPrintStream( PrintStream outputStream ) {}


   /**
    * <strong>Does nothing!</strong>
    *
    * @param errorStream  ignored
    */
   public void setErrorPrintStream( PrintStream errorStream ) {}


   /**
    * <strong>Does nothing!</strong><br>
    * Not used, emacs mode is always on.
    *
    * @param b  ignored
    */
   public void setEmacsMode( boolean b ) { }


   /**
    * Responds to a build being started by getting our properties set and remembering the current time.
    *
    * @param event  Ignored.
    */
   public void buildStarted( BuildEvent event ) {
      handleProperties( event );
      open();
      log( " " );
      // note: ConsolePluginHandler uses this message to show the Console plugin,
      // so if you change this message, be sure to update the ConsolePluginHandler
      log( Level.CONFIG, "===== BUILD STARTED =====" + lSep );
      startTime = System.currentTimeMillis();
   }


   /**
    * Prints whether the build succeeded or failed, any errors the occured during
    * the build, and how long the build took.
    *
    * @param event  An event with any relevant extra information. Must not be
    *      <code>null</code>.
    */
   public void buildFinished( BuildEvent event ) {
      //handleProperties(event);
      //if ( SHOW_BUILD_EVENTS ) {
      Throwable error = event.getException();
      StringBuffer message = new StringBuffer();

      if ( error == null ) {
         message.append( lSep );
         message.append( "BUILD SUCCESSFUL" );
         log( Level.WARNING, message.toString() );
      }
      else {
         message.append( lSep );
         message.append( "BUILD FAILED" );
         message.append( lSep );

         if ( Project.MSG_VERBOSE <= msgOutputLevel ||
                 !( error instanceof BuildException ) ) {
            message.append( StringUtils.getStackTrace( error ) );
         }
         else {
            if ( error instanceof BuildException ) {
               message.append( error.toString() ).append( lSep );
            }
            else {
               message.append( error.getMessage() ).append( lSep );
            }
         }
         log( Level.SEVERE, message.toString() );
      }
      message = new StringBuffer();
      //message.append( "Total time: " );
      //message.append( formatTime( System.currentTimeMillis() - startTime ) );
      message.append( lSep );
      // note: ConsolePluginHandler uses this message to set focus back to the buffer,
      // so if you change this message, be sure to update the ConsolePluginHandler
      message.append( "===== BUILD FINISHED =====" ).append( lSep );
      //message.append( new java.util.Date().toString()).append(lSep);

      String msg = message.toString();
      log( Level.CONFIG, msg );
      //}
      close();
   }


   /**
    * Logs a message to say that the target has started if this logger allows
    * SHOW_TARGET_EVENT messages.
    *
    * @param event  An event with any relevant extra information. Must not be
    *      <code>null</code>.
    */
   public void targetStarted( BuildEvent event ) {
      handleProperties( event );
      if ( SHOW_TARGET_EVENTS ) {
         String msg = targetIndent + "<" + event.getTarget().getName() + "> ";
         log( Level.CONFIG, msg );
      }
      messageLogged( event );
   }


   /**
    * Logs the target name.
    *
    * @param event  Ignored.
    */
   public void targetFinished( BuildEvent event ) {
      if ( SHOW_TARGET_EVENTS ) {
         String msg = targetIndent + "</" + event.getTarget().getName() + "> ";
         log( Level.CONFIG, msg );
      }
      messageLogged( event );
   }


   /**
    * Logs a message to say that the task has started if this logger allows
    * SHOW_TASK_EVENT messages.
    *
    * @param event  Ignored.
    */
   public void taskStarted( BuildEvent event ) {
      if ( SHOW_TASK_EVENTS ) {
         String msg = taskIndent + "<" + event.getTask().getTaskName() + "> ";
         log( Level.CONFIG, msg );
      }
      messageLogged( event );
   }


   /**
    * Logs task name.
    *
    * @param event  Ignored.
    */
   public void taskFinished( BuildEvent event ) {
      if ( SHOW_TASK_EVENTS ) {
         String msg = taskIndent + "</" + event.getTask().getTaskName() + "> ";
         log( Level.CONFIG, msg );
      }
      messageLogged( event );
   }


   /**
    * Logs a message, if the priority is suitable and SHOW_LOG_MESSAGES.
    *
    * @param event  A BuildEvent containing message information. Must not be
    *      <code>null</code> .
    */
   public void messageLogged( BuildEvent event ) {
      String msg = event.getMessage();
      if ( msg == null || msg.length() == 0 ) {
         msg = "" ;
      }
      if ( SHOW_LOG_MSGS ) {
         int priority = event.getPriority();
         // Filter out messages based on priority
         if ( priority <= msgOutputLevel ) {
            StringBuffer message = new StringBuffer();
            message.append( msgIndent );
            message.append( msg );
            log( message.toString() );
         }
      }
   }


   /**
    * Convenience method to format a specified length of time.
    *
    * @param millis  Length of time to format, in milliseonds.
    * @return        the time as a formatted string.
    * @see           DateUtils#formatElapsedTime(long)
    */
   protected static String formatTime( final long millis ) {
      return DateUtils.formatElapsedTime( millis );
   }


   /**
    * Logs build output to a java.util.logging.Logger.
    *
    * @param message  Message being logged. <code>null</code> messages are not
    *      logged, however, zero-length strings are.
    * @param level    the log level
    */
   protected void log( Level level, String message ) {
      if ( message == null ) {
         message = "" ;
      }

      Log.log(this, message);

      // log the message
      LogRecord record = new LogRecord( level, message );
      record.setParameters(parameters);
      logger.log( record );

      // write the message to the original System.out if desired
      if ( echo ) {
         ///systemOut.println( message );
      }
   }


   /**
    * Logs build output to a java.util.logging.Logger.
    *
    * @param message  Message to log. <code>null</code> messages are not logged,
    * however, zero-length strings are.
    */
   protected void log( String message ) {
      log( Level.INFO, message );
   }


   /**
    * If set to true, output from this logger will be echoed to the original
    * System.out and System.err print streams. The default setting is false.
    *
    * @param echo  The new echo value
    */
   public void setEcho( boolean echo ) {
      this.echo = echo;
   }


   /**
    * Gets the echo state, set setEcho.
    *
    * @return   The echo value
    */
   public boolean getEcho() {
      return echo;
   }


   /**
    *  Initializes the logger. This will install a ConsoleHandler by default.
    */
   private void initLogger() {
      logger = Logger.getLogger( NAMESPACE );
      logger.setUseParentHandlers( false );
      removeAllLogHandlers();
      logger.setLevel( Level.ALL );

      // default log handler is a ConsoleHandler
      ConsoleHandler console = new ConsoleHandler();
      console.setLevel( Level.ALL );
      console.setFormatter( new PlainFormatter() );
      logger.addHandler( console );
   }

   /**
    *  Removes all log handlers from the logger for our namespace.
    */
   public void removeAllLogHandlers() {
      if ( logger == null ) {
         return ;
      }
      synchronized ( logger ) {
         Handler[] handlers = logger.getHandlers();
         for ( int i = 0; i < handlers.length; i++ ) {
            logger.removeHandler( handlers[ i ] );
         }
      }
   }

   /**
    * The simplest formatter possible.
    */
   public class PlainFormatter extends Formatter {
      /**
       * Format the given log record and return the formatted string.
       *
       * @param record  the log record to be formatted.
       * @return        the formatted log record
       */
      public String format( LogRecord record ) {
         return record.getMessage() + lSep;
      }
   }

   /**
    * Calls internal methods based on the settings of certain properties
    * in the Project. Properties can be<br>
    * <ul>
    * <li>antlogger.echo, value is true/false, mimics <code>setEcho</code>.
    * <li>antlogger.namespace, value is a string, mimics <code>setNamespaceForLogger</code>.
    * <li>antlogger.file, value is a String per FileLogger 'pattern' API.
    * <li>antlogger.file.append, value is true/false, append to the log file or overwrite.
    * </ul>
    *
    * @param event  The build event from the buildStarted method.
    */
   private void handleProperties( BuildEvent event ) {
      // handle echo
      Object o = event.getProject().getProperty( "antlogger.echo" );
      if ( o == null )
         o = System.getProperty( "antlogger.echo" );
      if ( o != null ) {
         setEcho( Project.toBoolean( o.toString() ) );
      }

      // set up the file handler
      o = event.getProject().getProperty( "antlogger.file" );
      if ( o == null )
         o = System.getProperty( "antlogger.file" );
      if ( o != null ) {
         if ( fileHandler == null ) {
            String filename = o.toString();
            o = event.getProject().getProperty( "antlogger.file.append" );
            if ( o == null )
               o = System.getProperty( "antlogger.file.append" );
            boolean append = false;
            if ( o != null ) {
               append = Project.toBoolean( o.toString() );
            }
            try {
               fileHandler = new AntFileHandler( new File( filename ), append );
               fileHandler.setLevel( Level.ALL );
               fileHandler.setFormatter( new PlainFormatter() );
               logger.addHandler( fileHandler );
            }
            catch ( Exception ignored ) {
               // silently ignore any error -- the alternative is to bail out
               // of the build.
            }
         }
      }
      else {
         if ( fileHandler != null ) {
            logger.removeHandler( fileHandler );
         }
      }
   }

   /**
    * Creates a special print stream for this logger. Everything written to this
    * stream is sent to the <code>log</code> method for logging.
    *
    * @return   a print stream
    */
   private PrintStream createPrintStream() {
      PrintStream ps = new PrintStream(
               new java.io.OutputStream() {
                   StringBuffer line = new StringBuffer();
                  public void write( int b ) {
                      /*
                      if ((byte)b == '\n') {
                        log(line.toString());
                        line = new StringBuffer();
                      }
                      else {
                        line.append((byte)b);
                      }
                      */
                     byte[] bytes = {( byte ) b};
                     write( bytes, 0, 1 );
                  }

                  public void write( byte[] bytes, int offset, int length ) {
                      String s = new String(bytes, offset, length);
                      log(s);
                  }
               }
            );
      return ps;
   }

}

