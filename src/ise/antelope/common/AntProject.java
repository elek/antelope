// $Id$
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

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Modifier;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Properties;
import java.util.Set;
import java.util.HashSet;
import java.util.Stack;
import java.util.Vector;

import org.apache.tools.ant.AntClassLoader;
import org.apache.tools.ant.BuildEvent;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.BuildListener;
import org.apache.tools.ant.PathTokenizer;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.ProjectComponent;
import org.apache.tools.ant.ProjectHelper;
import org.apache.tools.ant.Target;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.TaskAdapter;
import org.apache.tools.ant.input.DefaultInputHandler;
import org.apache.tools.ant.input.InputHandler;
import org.apache.tools.ant.types.FilterSet;
import org.apache.tools.ant.types.FilterSetCollection;
import org.apache.tools.ant.util.FileUtils;
import org.apache.tools.ant.util.JavaEnvUtils;

/**
 * Central representation of an Ant project. This class defines an Ant project
 * with all of its targets, tasks and various other properties. It also provides
 * the mechanism to kick off a build using a particular target name. <p>
 *
 * This class also encapsulates methods which allow files to be referred to
 * using abstract path names which are translated to native system file paths at
 * runtime. 
 *
 * danson: Made some modifications to be able to remove properties.
 * Much of the following is cut and paste from Project, mostly because Project
 * does not expose enough of its internals to be able to do the property
 * manipulation that I want to do. 
 *
 * @author    duncan@x180.com; Dale Anson, danson@germane-software.com
 * @version   $Revision$
 */

public class AntProject extends Project {

   /** Project properties map (usually String to String).  */
   private Hashtable properties = new Hashtable();
   /**
    * Map of "user" properties (as created in the Ant task, for example). Note
    * that these key/value pairs are also always put into the project
    * properties, so only the project properties need to be queried. Mapping is
    * String to String.
    */
   private Hashtable userProperties = new Hashtable();
   /**
    * Map of inherited "user" properties - that are those "user" properties that
    * have been created by tasks and not been set from the command line or a GUI
    * tool. Mapping is String to String.
    */
   private Hashtable inheritedProperties = new Hashtable();


   /** Creates a new Ant project.  */
   public AntProject() {
      super();
   }


   /**
    * Initialises the project. This involves setting the default task
    * definitions and loading the system properties.
    *
    * @exception BuildException  if the default task list cannot be loaded
    */
   public void init() throws BuildException {
      setJavaVersionProperty();

      String defs = "/org/apache/tools/ant/taskdefs/defaults.properties";

      try {
         Properties props = new Properties();
         InputStream in = this.getClass().getResourceAsStream( defs );
         if ( in == null ) {
            throw new BuildException( "Can't load default task list" );
         }
         props.load( in );
         in.close();

         Enumeration en = props.propertyNames();
         while ( en.hasMoreElements() ) {
            String key = ( String ) en.nextElement();
            String value = props.getProperty( key );
            try {
               Class taskClass = Class.forName( value );
               addTaskDefinition( key, taskClass );
            }
            catch ( NoClassDefFoundError ncdfe ) {
               log( "Could not load a dependent class ("
                    + ncdfe.getMessage() + ") for task " + key, MSG_DEBUG );
            }
            catch ( ClassNotFoundException cnfe ) {
               log( "Could not load class (" + value
                    + ") for task " + key, MSG_DEBUG );
            }
         }
      }
      catch ( IOException ioe ) {
         throw new BuildException( "Can't load default task list" );
      }

      String dataDefs = "/org/apache/tools/ant/types/defaults.properties";

      try {
         Properties props = new Properties();
         InputStream in = this.getClass().getResourceAsStream( dataDefs );
         if ( in == null ) {
            throw new BuildException( "Can't load default datatype list" );
         }
         props.load( in );
         in.close();

         Enumeration en = props.propertyNames();
         while ( en.hasMoreElements() ) {
            String key = ( String ) en.nextElement();
            String value = props.getProperty( key );
            try {
               Class dataClass = Class.forName( value );
               addDataTypeDefinition( key, dataClass );
            }
            catch ( NoClassDefFoundError ncdfe ) {
               // ignore...
            }
            catch ( ClassNotFoundException cnfe ) {
               // ignore...
            }
         }
      }
      catch ( IOException ioe ) {
         throw new BuildException( "Can't load default datatype list" );
      }

      setSystemProperties();
   }


   /**
    * Initialises the project. This is used when Antelope is a plugin as jEdit
    * loads ant.jar in a separate classloader from Antelope. The classloader
    * reference is necessary to load some property files from the ant.jar. This
    * involves setting the default task definitions and loading the system
    * properties.
    *
    * @param cl                  The classloader that loads Ant.
    * @exception BuildException  if the default task list cannot be loaded
    */
   public void init( ClassLoader cl ) throws BuildException {
      if ( cl == null ) {
         init();
         return ;
      }
      setJavaVersionProperty();

      String defs = "org/apache/tools/ant/taskdefs/defaults.properties";

      try {
         Properties props = new Properties();
         InputStream in = cl.getResourceAsStream( defs );
         if ( in == null ) {
            throw new BuildException( "Can't load default task list" );
         }
         props.load( in );
         in.close();

         Enumeration en = props.propertyNames();
         while ( en.hasMoreElements() ) {
            String key = ( String ) en.nextElement();
            String value = props.getProperty( key );
            try {
               Class taskClass = Class.forName( value );
               addTaskDefinition( key, taskClass );
            }
            catch ( NoClassDefFoundError ncdfe ) {
               log( "Could not load a dependent class ("
                    + ncdfe.getMessage() + ") for task " + key, MSG_DEBUG );
            }
            catch ( ClassNotFoundException cnfe ) {
               log( "Could not load class (" + value
                    + ") for task " + key, MSG_DEBUG );
            }
         }
      }
      catch ( IOException ioe ) {
         throw new BuildException( "Can't load default task list" );
      }

      String dataDefs = "org/apache/tools/ant/types/defaults.properties";

      try {
         Properties props = new Properties();
         InputStream in = cl.getResourceAsStream( dataDefs );
         if ( in == null ) {
            throw new BuildException( "Can't load default datatype list" );
         }
         props.load( in );
         in.close();

         Enumeration en = props.propertyNames();
         while ( en.hasMoreElements() ) {
            String key = ( String ) en.nextElement();
            String value = props.getProperty( key );
            try {
               Class dataClass = Class.forName( value );
               addDataTypeDefinition( key, dataClass );
            }
            catch ( NoClassDefFoundError ncdfe ) {
               // ignore...
            }
            catch ( ClassNotFoundException cnfe ) {
               // ignore...
            }
         }
      }
      catch ( IOException ioe ) {
         throw new BuildException( "Can't load default datatype list" );
      }

      setSystemProperties();
   }


   /**
    * Removes a user property.
    *
    * @param name  the name of the property to remove.
    */
   public synchronized void removeUserProperty( String name ) {
      userProperties.remove( name );
      properties.remove( name );
   }


   /**
    * Sets a user property, which cannot be overwritten by set/unset property
    * calls. Any previous value is overwritten. Modified from Ant's Project so
    * that if the value is an empty String or null, the property will be
    * removed.
    *
    * @param name   The name of property to set. Must not be <code>null</code>.
    * @param value  The new value of the property. Must not be <code>null</code>
    *      .
    * @see          #setProperty(String,String)
    */
   public synchronized void setUserProperty( String name, String value ) {
      if ( value == null || value.equals( "" ) ) {
         removeUserProperty( name );
      }
      else {
         userProperties.put( name, value );
         properties.put( name, value );
      }
   }


   /**
    * Sets a property. Any existing property of the same name is overwritten,
    * unless it is a user property.
    *
    * @param name   The name of property to set. Must not be <code>null</code>.
    * @param value  The new value of the property. Must not be <code>null</code>
    *      .
    */
   public synchronized void setProperty( String name, String value ) {
      // command line properties take precedence
      if ( null != userProperties.get( name ) ) {
         log( "Override ignored for user property " + name, MSG_VERBOSE );
         return ;
      }

      if ( null != properties.get( name ) ) {
         log( "Overriding previous definition of property " + name,
              MSG_VERBOSE );
      }

      log( "Setting project property: " + name + " -> " +
           value, MSG_DEBUG );
      properties.put( name, value );
   }


   /**
    * Sets a property if no value currently exists. If the property exists
    * already, a message is logged and the method returns with no other effect.
    *
    * @param name   The name of property to set. Must not be <code>null</code>.
    * @param value  The new value of the property. Must not be <code>null</code>
    *      .
    * @since        1.5
    */
   public synchronized void setNewProperty( String name, String value ) {
      if ( name == null || value == null ) {
         return ;
      }
      if ( null != properties.get( name ) ) {
         log( "Override ignored for property " + name, MSG_VERBOSE );
         return ;
      }
      log( "Setting project property: " + name + " -> " +
           value, MSG_DEBUG );
      properties.put( name, value );
   }


   /**
    * Sets a user property, which cannot be overwritten by set/unset property
    * calls. Any previous value is overwritten. Also marks these properties as
    * properties that have not come from the command line.
    *
    * @param name   The name of property to set. Must not be <code>null</code>.
    * @param value  The new value of the property. Must not be <code>null</code>
    *      .
    * @see          #setProperty(String,String)
    */
   public synchronized void setInheritedProperty( String name, String value ) {
      inheritedProperties.put( name, value );
      setUserProperty( name, value );
   }


   /**
    * Sets a property unless it is already defined as a user property (in which
    * case the method returns silently).
    *
    * @param name   The name of the property. Must not be <code>null</code>.
    * @param value  The property value. Must not be <code>null</code>.
    */
   private void setPropertyInternal( String name, String value ) {
      if ( null != userProperties.get( name ) ) {
         return ;
      }
      properties.put( name, value );
   }


   /**
    * Returns the value of a property, if it is set.
    *
    * @param name  The name of the property. May be <code>null</code>, in which
    *      case the return value is also <code>null</code>.
    * @return      the property value, or <code>null</code> for no match or if a
    *      <code>null</code> name is provided.
    */
   public String getProperty( String name ) {
      if ( name == null ) {
         return null;
      }
      String property = ( String ) properties.get( name );
      return property;
   }


   /**
    * Replaces ${} style constructions in the given value with the string value
    * of the corresponding data types.
    *
    * @param value               The string to be scanned for property
    *      references. May be <code>null</code>.
    * @return                    the given string with embedded property names
    *      replaced by values, or <code>null</code> if the given string is
    *      <code>null</code>.
    * @exception BuildException  if the given value has an unclosed property
    *      name, e.g. <code>${xxx</code>
    */
   public String replaceProperties( String value )
   throws BuildException {
      /// the next two lines are for Ant 1.6, they're not supported
      /// in Ant 1.5, maybe do a switch depending on the Ant version?
      ///PropertyHelper ph = PropertyHelper.getPropertyHelper(this);
      ///return ph.replaceProperties( this, value, properties );
      
      /// this is the Ant 1.5 way of doing it. The Ant 1.6 PropertyHelper
      /// should be used -- eventually. 
      return ProjectHelper.replaceProperties(this, value, properties);
   }


   /**
    * Returns the value of a user property, if it is set.
    *
    * @param name  The name of the property. May be <code>null</code>, in which
    *      case the return value is also <code>null</code>.
    * @return      the property value, or <code>null</code> for no match or if a
    *      <code>null</code> name is provided.
    */
   public String getUserProperty( String name ) {
      if ( name == null ) {
         return null;
      }
      String property = ( String ) userProperties.get( name );
      return property;
   }


   /**
    * Returns a copy of the properties table.
    *
    * @return   a hashtable containing all properties (including user
    *      properties).
    */
   public Hashtable getProperties() {
      Hashtable propertiesCopy = new Hashtable();

      Enumeration e = properties.keys();
      while ( e.hasMoreElements() ) {
         Object name = e.nextElement();
         Object value = properties.get( name );
         propertiesCopy.put( name, value );
      }

      return propertiesCopy;
   }


   /**
    * Returns a copy of the user property hashtable
    *
    * @return   a hashtable containing just the user properties
    */
   public Hashtable getUserProperties() {
      Hashtable propertiesCopy = new Hashtable();

      Enumeration e = userProperties.keys();
      while ( e.hasMoreElements() ) {
         Object name = e.nextElement();
         Object value = properties.get( name );
         propertiesCopy.put( name, value );
      }

      return propertiesCopy;
   }


   /**
    * Copies all user properties that have been set on the command line or a GUI
    * tool from this instance to the Project instance given as the argument. <p>
    *
    * To copy all "user" properties, you will also have to call {@link
    * #copyInheritedProperties copyInheritedProperties}.</p>
    *
    * @param other  the project to copy the properties to. Must not be null.
    * @since        Ant 1.5
    */
   public void copyUserProperties( Project other ) {
      Enumeration e = userProperties.keys();
      while ( e.hasMoreElements() ) {
         Object arg = e.nextElement();
         if ( inheritedProperties.containsKey( arg ) ) {
            continue;
         }
         Object value = userProperties.get( arg );
         other.setUserProperty( arg.toString(), value.toString() );
      }
   }


   /**
    * Copies all user properties that have not been set on the command line or a
    * GUI tool from this instance to the Project instance given as the argument.
    * <p>
    *
    * To copy all "user" properties, you will also have to call {@link
    * #copyUserProperties copyUserProperties}.</p>
    *
    * @param other  the project to copy the properties to. Must not be null.
    * @since        Ant 1.5
    */
   public void copyInheritedProperties( Project other ) {
      Enumeration e = inheritedProperties.keys();
      while ( e.hasMoreElements() ) {
         String arg = e.nextElement().toString();
         if ( other.getUserProperty( arg ) != null ) {
            continue;
         }
         Object value = inheritedProperties.get( arg );
         other.setInheritedProperty( arg, value.toString() );
      }
   }


   /**
    * Adds all system properties which aren't already defined as user properties
    * to the project properties.
    */
   public void setSystemProperties() {
      Properties systemP = System.getProperties();
      Enumeration e = systemP.keys();
      while ( e.hasMoreElements() ) {
         Object name = e.nextElement();
         String value = systemP.get( name ).toString();
         this.setPropertyInternal( name.toString(), value );
      }
   }


   /**
    * Sets the base directory for the project, checking that the given file
    * exists and is a directory.
    *
    * @param baseDir             The project base directory. Must not be <code>null</code>
    *      .
    * @exception BuildException  if the specified file doesn't exist or isn't a
    *      directory
    */
   public void setBaseDir( File baseDir ) throws BuildException {
      super.setBaseDir( baseDir );
      setPropertyInternal( "basedir", baseDir.getPath() );
   }


   /**
    * Sets the <code>ant.java.version</code> property and tests for unsupported
    * JVM versions. If the version is supported, verbose log messages are
    * generated to record the Java version and operating system name.
    *
    * @exception BuildException  if this Java version is not supported
    * @see                       org.apache.tools.ant.util.JavaEnvUtils#getJavaVersion
    */
   public void setJavaVersionProperty() throws BuildException {
      String javaVersion = JavaEnvUtils.getJavaVersion();
      super.setJavaVersionProperty();
      setPropertyInternal( "ant.java.version", javaVersion );
   }


   /**
    * Sets the name of the project, also setting the user property <code>ant.project.name</code>
    * .
    *
    * @param name  The name of the project. Must not be <code>null</code>.
    */
   public void setName( String name ) {
      super.setUserProperty( "ant.project.name", name );
      setUserProperty( "ant.project.name", name );
   }

}

