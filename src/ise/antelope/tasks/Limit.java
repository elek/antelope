/*
*  The Apache Software License, Version 1.1
*
*  Copyright (c) 2001-2002 The Apache Software Foundation.  All rights
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
package ise.antelope.tasks;

import java.util.Enumeration;
import java.util.Vector;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.TaskContainer;

/**
 * Limits the amount of time that a task or set of tasks can run. This is useful
 * for tasks that may "hang" or otherwise not complete in a timely fashion. This
 * task is done when either the maxwait time has expired or all nested tasks are
 * complete, whichever is first.
 *
 * @author   Dale Anson, danson@germane-software.com
 */
public class Limit extends Task implements TaskContainer {

   // storage for nested tasks
   private Vector tasks = new Vector();

   // units are in milliseconds, default value is 3 minutes.
   private long maxwait = 180 * 1000;

   // storage for task currently executing
   private Task currentTask = null;

   // used to control thread stoppage
   private Thread taskRunner = null;

   // should the build fail if the time limit has expired? Default is no.
   private boolean failOnError = false;

   private Exception exception = null;


   /**
    * Add a task to wait on.
    *
    * @param task                A task to execute
    * @exception BuildException  won't happen
    */
   public void addTask( Task task ) throws BuildException {
      tasks.addElement( task );
   }


   /**
    * How long to wait for all nested tasks to complete. Default is to wait 180
    * seconds (= 3 minutes).
    *
    * @param wait  time to wait in seconds, set to 0 to wait forever.
    */
   public void setMaxwait( int wait ) {
      // internally, maxwait is in milliseconds
      maxwait = wait * 1000;
   }


   /**
    * Should the build fail if the time limit has expired on this task? Default
    * is no.
    *
    * @param fail  if true, fail the build if the time limit has been reached.
    */
   public void setFailonerror( boolean fail ) {
      failOnError = fail;
   }


   /**
    * Execute all nested tasks, but stopping execution of nested tasks after
    * maxwait seconds or when all tasks are done, whichever is first.
    *
    * @exception BuildException  Description of the Exception
    */
   public void execute() throws BuildException {
      try {
         // start executing nested tasks
         final Thread runner =
            new Thread() {
               public void run() {
                  Enumeration en = tasks.elements();
                  while ( en.hasMoreElements() ) {
                     if ( taskRunner != this ) {
                        break;
                     }
                     currentTask = ( Task ) en.nextElement();
                     try {
                        currentTask.perform();
                     }
                     catch ( Exception e ) {
                        if ( failOnError ) {
                           exception = e;
                           return ;
                        }
                        else {
                           exception = e;
                        }
                     }
                  }
               }
            };
         taskRunner = runner;
         runner.start();
         runner.join( maxwait );

         // stop executing the nested tasks
         if ( runner.isAlive() ) {
            taskRunner = null;
            runner.interrupt();
            int index = tasks.indexOf( currentTask );
            StringBuffer not_ran = new StringBuffer();
            for ( int i = index + 1; i < tasks.size(); i++ ) {
               not_ran.append( '<' ).append( ( ( Task ) tasks.get( i ) ).getTaskName() ).append( '>' );
               if ( i < tasks.size() - 1 ) {
                  not_ran.append( ", " );
               }
            }
            StringBuffer msg = new StringBuffer();
            msg.append( "Interrupted task <" )
            .append( currentTask.getTaskName() )
            .append( ">. Waited " )
            .append( ( maxwait / 1000 ) )
            .append( " seconds, but this task did not complete." )
            .append( ( not_ran.length() > 0 ?
                  " The following tasks did not execute: " + not_ran.toString() + "." :
                  "" ) );
            if ( failOnError ) {
               throw new BuildException( msg.toString() );
            }
            else {
               log( msg.toString() );
            }
         }
         else if (failOnError && exception != null) {
            throw new BuildException(exception);  
         }
      }
      catch ( Exception e ) {
         throw new BuildException( e );
      }
   }
}

