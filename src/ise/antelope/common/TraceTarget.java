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

import java.io.*;
import java.util.*;
import java.util.regex.*;
import javax.xml.parsers.*;
import org.apache.tools.ant.*;
import org.w3c.dom.*;
import org.xml.sax.AttributeList;
import ise.library.PrivilegedAccessor;

/**
 * Traces the execution path of an Ant target.
 *
 * @author    Dale Anson, danson@germane-software.com
 * @version   $Revision$
 */
public class TraceTarget {

    /**
     * Convenience for the system line separator.
     */
    private final static String NL = System.getProperty( "line.separator" );

    /**
     * Description of the Field
     */
    private List unknown_properties = new ArrayList();

    /**
     * Trace the execution path of an Ant target.
     *
     * @param target  The target to trace.
     * @return        String describing the execution path of the target.
     */
    public String traceTarget( Target target ) {
        StringBuffer sb = new StringBuffer();
        sb.append( "Tracing target: " + target.getName() ).append( NL );
        sb.append( "Legend: <target>[task]" ).append( NL ).append( NL );
        sb.append( "<" + target.getName() + ">" ).append( NL );
        sb.append( doTrace( target ) );
        sb.append( "</" + target.getName() + ">" ).append( NL ).append( NL );
        sb.append( "Done tracing target: " + target.getName() ).append( NL ).append( NL );
        return sb.toString();
    }

    /**
     * Does the trace, necessary for recursion.
     *
     * @param target  target to trace
     * @return        description of execution path
     */
    private String doTrace( Target target ) {
        StringBuffer sb = new StringBuffer();
        sb.append( doDependencies( target ) );
        sb.append( doTasks( target ) );
        return sb.toString();
    }

    /**
     * Traces targets that the original target depends on.
     *
     * @param target  a dependency target
     * @return        trace of the dependency target
     */
    private String doDependencies( Target target ) {
        StringBuffer sb = new StringBuffer();
        Enumeration dependencies = target.getDependencies();
        while ( dependencies.hasMoreElements() ) {
            Object depend = dependencies.nextElement();
            Project project = target.getProject();
            Hashtable targets = project.getTargets();
            Target t = ( Target ) targets.get( depend.toString() );
            sb.append( doTrace( t ) );
        }
        return sb.toString();
    }

    /**
     * Traces tasks performed by the target. Two tasks are treated specially,
     * 'ant' and 'antcall', as both of these call other targets. The targets
     * specified by these two tasks will be traced as well.
     *
     * @param target  the target
     * @return        description of tasks performed by the target
     */
    private String doTasks( Target target ) {
        StringBuffer sb = new StringBuffer();
        Task[] tasks = target.getTasks();
        for ( int i = 0; i < tasks.length; i++ ) {
            Task task = tasks[ i ];
            String task_name = task.getTaskName();
            if ( task_name.equals( "antcall" ) ) {
                // trace the target specified by an 'antcall' task
                Hashtable attrs = null;
                RuntimeConfigurable rc = task.getRuntimeConfigurableWrapper();
                try {
                    if ( AntUtils.getAntVersion() >= 1.60 ) {
                        attrs = rc.getAttributeMap();
                    }
                    else {
                        attrs = makeMap( ( AttributeList ) PrivilegedAccessor.invokeMethod( rc, "getAttributes", null ) );
                    }
                }
                catch ( Exception e ) {
                    e.printStackTrace();
                    continue;
                }
                if ( attrs != null ) {
                    Iterator it = attrs.keySet().iterator();
                    while ( it.hasNext() ) {
                        String name = ( String ) it.next();
                        String value = ( String ) attrs.get( name );
                        if ( name.equals( "target" ) ) {
                            Project p = target.getProject();
                            if ( p == null )
                                break;
                            Hashtable targets = p.getTargets();
                            Target subtarget = ( Target ) targets.get( value );
                            if ( subtarget == null ) {
                                sb.append( "Error: <antcall> calling non-existant target " ).append( value ).append( NL );
                            }
                            else {
                                sb.append( "<" ).append( target.getName() ).append( ">" );
                                sb.append( "[" ).append( task_name ).append( " target=" );
                                sb.append( quote( subtarget.toString() ) ).append( "]" ).append( NL );
                                sb.append( doTrace( subtarget ) );
                            }
                        }
                    }
                }
            }
            else if ( task_name.equals( "ant" ) ) {
                // trace the target specified by an 'ant' task. This target will
                // be in another build file, so need to grab the build file name
                // and directory and load a project from it.
                Hashtable attrs = null;
                RuntimeConfigurable rc = task.getRuntimeConfigurableWrapper();
                try {
                    if ( AntUtils.getAntVersion() >= 1.60 ) {
                        attrs = ( Hashtable ) PrivilegedAccessor.getValue( rc, "getAttributeMap" );
                    }
                    else {
                        attrs = makeMap( ( AttributeList ) PrivilegedAccessor.getValue( rc, "getAttributes" ) );
                    }
                }
                catch ( Exception e ) {
                    // ignored
                }
                String antfile = "build.xml";
                String dir = "";
                String subtarget = "";
                if (attrs != null) {
                    Iterator it = attrs.keySet().iterator();
                    while ( it.hasNext() ) {
                        String name = ( String ) it.next();
                        String value = ( String ) attrs.get( name );
                        if ( name.equals( "antfile" ) )
                            antfile = value;
                        if ( name.equals( "dir" ) )
                            dir = value;
                        if ( name.equals( "target" ) )
                            subtarget = value;
                    }
                }
                sb.append( "<" ).append( target.getName() ).append( ">" );
                sb.append( "[" ).append( task_name ).append( " antfile=" ).append( quote( antfile ) );
                sb.append( " dir=" ).append( quote( dir ) );
                sb.append( ", " ).append( "target=" );
                sb.append( quote( subtarget ) ).append( "]" ).append( NL );

                File f;
                if ( dir.equals( "" ) )
                    f = new File( target.getProject().getBaseDir(), antfile );
                else
                    f = new File( dir, antfile );
                Project p = createProject( f, target.getProject().getProperties() );
                if ( subtarget.equals( "" ) )
                    subtarget = p.getDefaultTarget();
                if ( p != null ) {
                    Hashtable targets = p.getTargets();
                    Target remote_target = ( Target ) targets.get( subtarget );
                    sb.append( doTrace( remote_target ) );
                }
            }
            else if ( task_name.equals( "property" ) ) {
                String property_name = "";
                String property_value = "";
                Hashtable attrs = null;
                RuntimeConfigurable rc = task.getRuntimeConfigurableWrapper();
                try {
                    if ( AntUtils.getAntVersion() >= 1.60 ) {
                        attrs = ( Hashtable ) PrivilegedAccessor.getValue( rc, "getAttributeMap" );
                    }
                    else {
                        attrs = makeMap( ( AttributeList ) PrivilegedAccessor.getValue( rc, "getAttributes" ) );
                    }
                }
                catch ( Exception e ) {
                    // ignored
                }
                if (attrs != null) {
                    Iterator it = attrs.keySet().iterator();
                    while ( it.hasNext() ) {
                        String name = ( String ) it.next();
                        String value = ( String ) attrs.get( name );
                        if ( name.equals( "name" ) )
                            property_name = value;
                        if ( name.equals( "value" ) )
                            property_value = parseValue( value, target.getProject() );
                    }
                }
                target.getProject().setProperty( property_name, property_value );
                sb.append( "<" ).append( target.getName() ).append( ">" );
                sb.append( "[" ).append( task_name ).append( " name=" ).append( quote( property_name ) );
                sb.append( "," ).append( " value=" );
                sb.append( quote( property_value ) ).append( "]" ).append( NL );
            }
            else {
                sb.append( "<" ).append( target.getName() ).append( ">" );
                sb.append( "[" ).append( task_name );
                Hashtable attrs = null;
                RuntimeConfigurable rc = task.getRuntimeConfigurableWrapper();
                try {
                    if ( AntUtils.getAntVersion() >= 1.60 ) {
                        attrs = ( Hashtable ) PrivilegedAccessor.getValue( rc, "getAttributeMap" );
                    }
                    else {
                        attrs = makeMap( ( AttributeList ) PrivilegedAccessor.getValue( rc, "getAttributes" ) );
                    }
                }
                catch ( Exception e ) {
                    // ignored
                }
                if (attrs != null) {
                    Iterator it = attrs.keySet().iterator();
                    while ( it.hasNext() ) {
                        String name = ( String ) it.next();
                        String value = ( String ) attrs.get( name );
                        value = parseValue( value, target.getProject() );
                        sb.append( " " ).append( name ).append( "=" ).append( quote( value ) );
                    }
                }
                sb.append( "]" ).append( NL );
            }
        }
        return sb.toString();
    }

    /**
     * Description of the Method
     *
     * @param s
     * @return   Description of the Returned Value
     */
    private String quote( String s ) {
        return new StringBuffer().append( "\"" ).append( s ).append( "\"" ).toString();
    }

    /**
     * Parses a property value and replaces Ant property variables with actual values.
     *
     * @param value    the value to parse
     * @param project  an Ant project
     * @return         the actual value
     */
    private String parseValue( String value, Project project ) {
        if ( value == null )
            return "";
        if ( value.length() == 0 )
            return value;

        // project properties
        Map props = project.getProperties();

        // project references
        Map refs = project.getReferences();

        // storage for results
        StringBuffer sb = new StringBuffer();

        // look for Ant variables like ${build.dir}
        Pattern pattern = Pattern.compile( "\\$\\{.+?\\}", Pattern.DOTALL );
        Matcher matcher = pattern.matcher( value );

        // parse the input value -- it could have more than one Ant variable,
        // like $(install.dir}/${jar.file}, so need to find them all, find their
        // actual values, and splice the actuals in place of the variables. Of course,
        // there might not be any matches, in which case, just return the entire input
        // value.
        int index = 0;
        boolean found = false;
        while ( matcher.find() ) {
            found = true;
            String match = matcher.group();
            sb.append( value.substring( index, matcher.start() ) );
            String var = trimCurly( match );
            String val = ( String ) props.get( var );
            if ( val == null ) {
                val = match;
                if ( !unknown_properties.contains( match ) ) {
                    unknown_properties.add( match );
                }
            }
            sb.append( val );
            index = matcher.end();
        }
        if ( !found ) {
            // could be a reference
            Object val = refs.get( value );
            if ( val != null ) {
                found = true;
                sb.append( val.toString() );
            }
        }
        return found ? sb.toString() : value;
    }

    /**
     * Trims the ${} from a value, so given '${build.num}', this method
     * would return 'build.num'.
     *
     * @param value  something like ${build.num}
     * @return       the value minus the $ and curly brackets
     */
    private String trimCurly( String value ) {
        StringBuffer sb = new StringBuffer( value );
        if ( value.startsWith( "${" ) ) {
            sb.deleteCharAt( 0 );
            sb.deleteCharAt( 0 );
        }
        if ( value.endsWith( "}" ) )
            sb.deleteCharAt( sb.length() - 1 );
        return sb.toString();
    }

    /**
     *  Sets up an Ant project for the given build file.
     *
     * @param build_file  a build file
     * @param inherited   inherited properties from another project
     * @return            an Ant Project initialized and configured with the
     *      given build file
     */
    private Project createProject( File build_file, Hashtable inherited ) {
        // configure the project
        Project p = new Project();
        try {
            p.init();   // this takes as much as 9 seconds the first time, less than 1/2 second later
            ProjectHelper ph = ProjectHelper.getProjectHelper();
            ph.parse( p, build_file );
            p.setUserProperty( "ant.file", build_file.getAbsolutePath() );

            // copy the inherited properties
            if ( inherited == null )
                return p;
            Iterator it = inherited.keySet().iterator();
            while ( it.hasNext() ) {
                Object key = it.next();
                p.setUserProperty( ( String ) key, ( String ) inherited.get( key ) );
            }
        }
        catch ( Exception e ) {
            return null;
        }
        return p;
    }

    /**
     * There may be property values that aren't known during a trace as they
     * may be created on the fly. These unknown values are stored during a trace
     * and can be retrieved after a trace from here.
     *
     * @return   a list of properties whose values weren't know during the trace.
     */
    public List getUnknownProperties() {
        return unknown_properties;
    }

    /**
     * I have some plans for this...
     *
     * @param file  an xml file to create a document from
     * @return      The document
     */
    private Document getDocument( File file ) {
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.parse( file );
            return doc;
        }
        catch ( Exception e ) {
            return null;
        }
    }

    private Hashtable makeMap( AttributeList list ) {
        if ( list == null )
            return null;
        Hashtable map = new Hashtable();
        for ( int i = 0; i < list.getLength(); i++ ) {
            map.put( list.getName( i ), list.getValue( i ) );
        }
        return map;
    }

}

