/*
* The Apache Software License, Version 1.1
*
* Copyright (c) 2001-2003 The Apache Software Foundation.  All rights
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
* 4. The names "Ant" and "Apache Software
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

package ise.library;

import java.io.*;
import java.util.Hashtable;
import java.util.Locale;
import java.util.Vector;

/**
 * ***** Dale Anson: Modified to not be Ant specific by removing "extends Condition"
 * and replacing BuildExceptions. Added getEnvironmentValue and associated methods.
 * *****
 * Condition that tests the OS type.
 *
 * @author Stefan Bodewig
 * @author Magesh Umasankar
 * @since Ant 1.4
 * @version $Revision$
 */
public class Os {
   private static final String OS_NAME =
      System.getProperty( "os.name" ).toLowerCase( Locale.US );
   private static final String OS_ARCH =
      System.getProperty( "os.arch" ).toLowerCase( Locale.US );
   private static final String OS_VERSION =
      System.getProperty( "os.version" ).toLowerCase( Locale.US );
   private static final String PATH_SEP =
      System.getProperty( "path.separator" );

   private String family;
   private String name;
   private String version;
   private String arch;

   private static Hashtable environment;

   /**
    * Default constructor
    *
    */
   public Os() {}

   /**
    * Constructor that sets the family attribute
    *
    * @param family a String value
    */
   public Os( String family ) {
      setFamily( family );
   }

   /**
    * Sets the desired OS family type
    *
    * @param f      The OS family type desired<br />
    *               Possible values:<br />
    *               <ul>
    *               <li>dos</li>
    *               <li>mac</li>
    *               <li>netware</li>
    *               <li>os/2</li>
    *               <li>tandem</li>
    *               <li>unix</li>
    *               <li>windows</li>
    *               <li>win9x</li>
    *               <li>z/os</li>
    *               <li>os/400</li>
    *               </ul>
    */
   public void setFamily( String f ) {
      family = f.toLowerCase( Locale.US );
   }

   /**
    * Sets the desired OS name
    *
    * @param name   The OS name
    */
   public void setName( String name ) {
      this.name = name.toLowerCase( Locale.US );
   }

   /**
    * Sets the desired OS architecture
    *
    * @param arch   The OS architecture
    */
   public void setArch( String arch ) {
      this.arch = arch.toLowerCase( Locale.US );
   }

   /**
    * Sets the desired OS version
    *
    * @param version   The OS version
    */
   public void setVersion( String version ) {
      this.version = version.toLowerCase( Locale.US );
   }

   /**
    * Determines if the OS on which Ant is executing matches the type of
    * that set in setFamily.
    * @see Os#setFamily(String)
    */
   public boolean eval() {
      return isOs( family, name, arch, version );
   }

   /**
    * Determines if the OS on which Ant is executing matches the
    * given OS family.
    * @param family the family to check for
    * @return true if the OS matches
    * @since 1.5
    */
   public static boolean isFamily( String family ) {
      return isOs( family, null, null, null );
   }

   /**
    * Determines if the OS on which Ant is executing matches the
    * given OS name.
    *
    * @param name the OS name to check for
    * @return true if the OS matches
    * @since 1.7
    */
   public static boolean isName( String name ) {
      return isOs( null, name, null, null );
   }

   /**
    * Determines if the OS on which Ant is executing matches the
    * given OS architecture.
    *
    * @param arch the OS architecture to check for
    * @return true if the OS matches
    * @since 1.7
    */
   public static boolean isArch( String arch ) {
      return isOs( null, null, arch, null );
   }

   /**
    * Determines if the OS on which Ant is executing matches the
    * given OS version.
    *
    * @param version the OS version to check for
    * @return true if the OS matches
    * @since 1.7
    */
   public static boolean isVersion( String version ) {
      return isOs( null, null, null, version );
   }

   /**
    * Determines if the OS on which Ant is executing matches the
    * given OS family, name, architecture and version
    *
    * @param family   The OS family
    * @param name   The OS name
    * @param arch   The OS architecture
    * @param version   The OS version
    * @return true if the OS matches
    * @since 1.7
    */
   public static boolean isOs( String family, String name, String arch,
         String version ) {
      boolean retValue = false;

      if ( family != null || name != null || arch != null
              || version != null ) {

         boolean isFamily = true;
         boolean isName = true;
         boolean isArch = true;
         boolean isVersion = true;

         if ( family != null ) {
            if ( family.equals( "windows" ) ) {
               isFamily = OS_NAME.indexOf( "windows" ) > -1;
            }
            else if ( family.equals( "os/2" ) ) {
               isFamily = OS_NAME.indexOf( "os/2" ) > -1;
            }
            else if ( family.equals( "netware" ) ) {
               isFamily = OS_NAME.indexOf( "netware" ) > -1;
            }
            else if ( family.equals( "dos" ) ) {
               isFamily = PATH_SEP.equals( ";" ) && !isFamily( "netware" );
            }
            else if ( family.equals( "mac" ) ) {
               isFamily = OS_NAME.indexOf( "mac" ) > -1;
            }
            else if ( family.equals( "tandem" ) ) {
               isFamily = OS_NAME.indexOf( "nonstop_kernel" ) > -1;
            }
            else if ( family.equals( "unix" ) ) {
               isFamily = PATH_SEP.equals( ":" )
                     && !isFamily( "openvms" )
                     && ( !isFamily( "mac" ) || OS_NAME.endsWith( "x" ) );
            }
            else if ( family.equals( "win9x" ) ) {
               isFamily = isFamily( "windows" )
                     && ( OS_NAME.indexOf( "95" ) >= 0
                          || OS_NAME.indexOf( "98" ) >= 0
                          || OS_NAME.indexOf( "me" ) >= 0
                          || OS_NAME.indexOf( "ce" ) >= 0 );
            }
            else if ( family.equals( "z/os" ) ) {
               isFamily = OS_NAME.indexOf( "z/os" ) > -1
                     || OS_NAME.indexOf( "os/390" ) > -1;
            }
            else if ( family.equals( "os/400" ) ) {
               isFamily = OS_NAME.indexOf( "os/400" ) > -1;
            }
            else if ( family.equals( "openvms" ) ) {
               isFamily = OS_NAME.indexOf( "openvms" ) > -1;
            }
            else {
               /*
               throw new BuildException(
                   "Don\'t know how to detect os family \""
                   + family + "\"");
                   */ 
               return false;
            }
         }
         if ( name != null ) {
            isName = name.equals( OS_NAME );
         }
         if ( arch != null ) {
            isArch = arch.equals( OS_ARCH );
         }
         if ( version != null ) {
            isVersion = version.equals( OS_VERSION );
         }
         retValue = isFamily && isName && isArch && isVersion;
      }
      return retValue;
   }

   /**
    * Dale Anson: One of the main reasons I borrowed this class from Ant was to
    * be able to read environment variables. It makes sense to add a method to
    * easily fetch the value of an environment variable here.
    * @param name the name of an environment variable. Much of this code was
    * copied from org.apache.tools.ant.taskdefs.Execute.
    * @return the value of the environment variable, or null if there is no value
    * for the given name
    */
   public static String getEnvironmentValue( String name ) {
      if ( environment != null ) {
         return ( String ) environment.get( name );
      }
      environment = new Hashtable();

      try {
         String[] env_cmd = getProcEnvCommand();
         Process process = Runtime.getRuntime().exec( env_cmd );
         InputStream is = new BufferedInputStream( process.getInputStream() );
         ByteArrayOutputStream baos = new ByteArrayOutputStream();
         ise.library.FileUtilities.copy( is, baos );

         BufferedReader in = new BufferedReader( new StringReader( baos.toString() ) );


         // this portion copied from org.apache.tools.ant.taskdefs.Execute //
         Vector procEnvironment = new Vector();
         String var = null;
         String line, lineSep = System.getProperty( "line.separator" );
         while ( ( line = in.readLine() ) != null ) {
            if ( line.indexOf( '=' ) == -1 ) {
               // Chunk part of previous env var (UNIX env vars can
               // contain embedded new lines).
               if ( var == null ) {
                  var = lineSep + line;
               }
               else {
                  var += lineSep + line;
               }
            }
            else {
               // New env var...append the previous one if we have it.
               if ( var != null ) {
                  procEnvironment.addElement( var );
               }
               var = line;
            }
         }
         // Since we "look ahead" before adding, there's one last env var.
         if ( var != null ) {
            procEnvironment.addElement( var );
         }
         // end copy from Execute //

         // now split out the names from the values and populate a Hashtable
         if ( procEnvironment.size() > 0 ) {
            java.util.Iterator it = procEnvironment.iterator();
            while ( it.hasNext() ) {
               var = ( String ) it.next();
               int index = var.indexOf( "=" );
               String key = var.substring( 0, index );
               String value = var.substring( index + 1 );
               environment.put( key, value );
            }
         }
      }
      catch ( Exception ignored ) {}
      return getEnvironmentValue( name );
   }

   /**
    * Dale Anson: Copied from org.apache.tools.ant.taskdefs.Execute. It's a private method in
    * that class, so I can't access it directly.
    */
   private static String[] getProcEnvCommand() {
      if ( Os.isFamily( "os/2" ) ) {
         // OS/2 - use same mechanism as Windows 2000
         String[] cmd = {"cmd", "/c", "set" };
         return cmd;
      }
      else if ( Os.isFamily( "windows" ) ) {
         // Determine if we're running under XP/2000/NT or 98/95
         if ( !Os.isFamily( "win9x" ) ) {
            // Windows XP/2000/NT
            String[] cmd = {"cmd", "/c", "set" };
            return cmd;
         }
         else {
            // Windows 98/95
            String[] cmd = {"command.com", "/c", "set" };
            return cmd;
         }
      }
      else if ( Os.isFamily( "z/os" ) || Os.isFamily( "unix" ) ) {
         // On most systems one could use: /bin/sh -c env

         // Some systems have /bin/env, others /usr/bin/env, just try
         String[] cmd = new String[ 1 ];
         if ( new File( "/bin/env" ).canRead() ) {
            cmd[ 0 ] = "/bin/env";
         }
         else if ( new File( "/usr/bin/env" ).canRead() ) {
            cmd[ 0 ] = "/usr/bin/env";
         }
         else {
            // rely on PATH
            cmd[ 0 ] = "env";
         }
         return cmd;
      }
      else if ( Os.isFamily( "netware" ) || Os.isFamily( "os/400" ) ) {
         // rely on PATH
         String[] cmd = {"env"};
         return cmd;
      }
      else if ( Os.isFamily( "openvms" ) ) {
         String[] cmd = {"show", "logical"};
         return cmd;
      }
      else {
         // MAC OS 9 and previous
         //TODO: I have no idea how to get it, someone must fix it
         String[] cmd = null;
         return cmd;
      }
   }


}
