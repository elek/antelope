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

import ise.library.PrivilegedAccessor;

import java.awt.Color;
import javax.swing.*;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.BuildListener;
import org.apache.tools.ant.BuildEvent;

import java.io.*;
import java.util.*;
import javax.xml.parsers.*;
import org.apache.tools.ant.*;
import org.w3c.dom.*;
import org.xml.sax.AttributeList;

/**
 * The progress bar show progress of a running Ant target by counting the number
 * of tasks that the target performs and incrementing the progress bar as each
 * task is completed. The assumption is that a single target will be called.
 * While this target may call other targets, the progress is based on the initial
 * target. The initial target must be set prior to the build starting by calling
 * <code>setExecutingTarget</code>.
 * <p>
 * Ant is not particularly friendly about providing information about itself --
 * Project does not tell which target is currently executing, BuildEvent does not
 * tell which target started the execution cycle, TaskContainer provides no
 * information about nested tasks, and so on. This class has a number of hacks
 * to get around these limitiations, some of which are strictly against the
 * spirit of Java programming (e.g., I use PrivilegedAccessor to read the private
 * fields in RuntimeConfigurable). These hacks make this a fragile class as it
 * is not strictly based on the public Ant API.
 * <p>
 * Ant 1.6 made it even harder. This class needs a bit more work.
 * @author Dale Anson
 */
public class AntProgressListener extends JProgressBar implements BuildListener {

    private int total;  // total number of tasks that will be executed by the target
    private Color errorColor = Color.red;   // build failed
    private Color defaultColor = new Color( 0, 153, 51 );   // a nice green for build succeeded
    private double ant_version = 0;

    private HashMap project_cache = new HashMap();

    /**
     * Default constructor sets up the progress bar values.
     */
    public AntProgressListener() {
        setStringPainted( true );
    }

    /**
     * Resets the progress bar to 0 and to default color.
     * @param be not used    
     */
    public void buildStarted( BuildEvent be ) {
        setStringPainted( true );
        setValue( 0 );
        setString( null );
        total = 0;
        setForeground( defaultColor );
    }

    /**
     * Set the progress par to maximum and adjust the color based on build
     * failure or success.
     * @param be not used    
     */
    public void buildFinished( BuildEvent be ) {
        if ( be.getException() != null ) {
            setForeground( errorColor );
            setString( "Failed" );
        }
        else {
            setString( "Complete" );
        }
        setIndeterminate( false );
        setValue( getMaximum() );
    }

    /**
     * Counts the number of tasks to be ran. This includes tasks in dependency
     * targets as well as tasks nested inside task containers. This value is used
     * as the maximum value for the progress bar.
     * @param project the project containing the executing target, must not be
     * null.
     * @param target_list a list of target names to execute, must not be null.    
     */
    public void setExecutingTarget( Project project, ArrayList target_list ) {
        // XXX this needs more work...
        if ( project == null )
            throw new IllegalArgumentException( "project is null" );
        if ( target_list == null )
            throw new IllegalArgumentException( "target list is null" );
        Hashtable targets = project.getTargets();
        Iterator it = target_list.iterator();
        while ( it.hasNext() ) {
            String target_name = ( String ) it.next();
            Target target = ( Target ) targets.get( target_name );
            total += countTasks( target );
        }

        if ( total == 1 ) {
            setIndeterminate( true );
        }
        setMaximum( total );
    }

    /**
     * Show the name of the currently executing target in the status.    
     */
    public void targetStarted( BuildEvent be ) {
        setString( be.getTarget().getName() );
    }

    /**
     * no-op    
     */
    public void targetFinished( BuildEvent be ) {
        // does nothing
    }

    /**
     * no-op    
     */
    public void taskStarted( BuildEvent be ) {
        // does nothing
    }

    /**
     * Sets progress bar to maximum value.
     * @param be not used    
     */
    public void taskFinished( BuildEvent be ) {
        setValue( getValue() + 1 );
        total += 1;
    }

    /**
     * no-op    
     */
    public void messageLogged( BuildEvent be ) {
        // does nothing
    }

    /**
     * Counts the number of tasks performed by the given target. If this target calls
     * other targets (either via &lt;ant&gt; or &lt;antcall&gt; tasks), the returned
     * count will include the count of those targets as well. Tasks contained by 
     * dependency targets are automatically counted, as are task containers. Bottom
     * line is any task that will be executed by calling this target will be counted.
     * @param target the target to count tasks in
     * @return the number of tasks performed by this target    
     */
    public int countTasks( Target target ) {
        if ( target == null )
            return 0;
        int cnt = doCountTasks( target );
        return cnt;
    }

    /**
     * Called from countTasks, necessary for recursion into subtargets.
     * Counts the number of tasks performed by the given target. If this target calls
     * other targets (either via &lt;ant&gt; or &lt;antcall&gt; tasks), the returned
     * count will include the count of those targets as well.
     * @param target the target to count tasks in
     * @return the number of tasks performed by this target    
     */
    private int doCountTasks( Target target ) {
        int dependency_count = doCountDependencies( target );
        int task_count = doTasks( target );
        return dependency_count + task_count;
    }

    /**
     * Counts the number of tasks performed by targets that the given target 
     * depends on. If those targets call
     * other targets (either via &lt;ant&gt; or &lt;antcall&gt; tasks), the returned
     * count will include the count of those targets as well.
     * @param target the target to count dependency tasks for
     * @return the number of tasks performed dependency targets for this target    
     */
    private int doCountDependencies( Target target ) {
        if ( target == null )
            return 0;
        int task_count = 0;
        Enumeration dependencies = target.getDependencies();
        while ( dependencies.hasMoreElements() ) {
            Object depend = dependencies.nextElement();
            Project project = target.getProject();
            Hashtable targets = project.getTargets();
            Target t = ( Target ) targets.get( depend.toString() );
            int cnt = doCountTasks( t );
            task_count += cnt ;
        }
        return task_count;
    }


    /**
     * Counts the number of tasks performed by the given target. If this target calls
     * other targets (either via &lt;ant&gt; or &lt;antcall&gt; tasks), the returned
     * count will include the count of those targets as well.
     * @param target the target to count tasks in
     * @return the number of tasks performed by this target    
     */
    private int doTasks( Target target ) {
        if ( target == null )
            return 0;

        int task_count = 0;
        Task[] tasks = target.getTasks();
        for ( int i = 0; i < tasks.length; i++ ) {
            Task task = tasks[ i ];
            String task_name = task.getTaskName();
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
            if ( task instanceof TaskContainer ) {
                if ( attrs == null )
                    continue;
                task_count += attrs.size();
            }
            else if ( task_name.equals( "antcall" ) || task_name.equals( "call" ) ) {
                // count the tasks in the target specified by an 'antcall' task
                if ( attrs == null )
                    continue;
                Iterator it = attrs.keySet().iterator();
                while ( it.hasNext() ) {
                    String name = ( String ) it.next();
                    String value = ( String ) attrs.get( name );
                    if ( name.equals( "target" ) ) {
                        Hashtable targets = target.getProject().getTargets();
                        Target subtarget = ( Target ) targets.get( value );
                        task_count += doCountTasks( subtarget );
                    }
                }
            }
            else if ( task_name.equals( "ant" ) ) {
                // count the tasks in the target specified by an 'ant' task. This target will
                // be in another build file, so need to grab the build file name
                // and directory and load a project from it.
                if ( attrs == null )
                    continue;
                String antfile = "build.xml";
                String dir = "";
                String subtarget = "";
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

                File f;
                if ( dir.equals( "" ) )
                    f = new File( target.getProject().getBaseDir(), antfile );
                else
                    f = new File( dir, antfile );
                Project p = createProject( f, target.getProject().getProperties() );
                if ( p != null ) {
                    if ( subtarget.equals( "" ) )
                        subtarget = p.getDefaultTarget();
                    Hashtable targets = p.getTargets();
                    Target remote_target = ( Target ) targets.get( subtarget );
                    task_count += doCountTasks( remote_target );
                }
            }
            else {
                task_count += countSubTasks( target, rc );
                ++task_count;   // add 1 for this task
            }
        }
        return task_count;
    }

    /**
     * Counts tasks in task containers. This is a recursive method in that it calls
     * itself if any of the tasks in the task container are also task containers.
     * @param target the target containing the task in question
     * @param rc the RuntimeConfigurable representing the task
     * @return the number of nested tasks.     
     */
    private int countSubTasks( Target target, RuntimeConfigurable rc ) {
        int task_count = 0;
        try {
            // too bad that RuntimeConfigurable didn't provide public
            // access to its "children" field, then I wouldn't have to
            // resort to molesting its kids.
            // XXX changed in Ant 1.5.1
            Enumeration children = null;

            // this is for Ant 1.5:
            if ( AntUtils.getAntVersion() == 1.50 ) {
                Vector kids = ( Vector ) PrivilegedAccessor.getValue( rc, "children" );
                children = kids.elements();
            }
            else {
                // this is for Ant 1.5.1 and later
                children = ( Enumeration ) PrivilegedAccessor.invokeMethod( rc, "getChildren", null );
            }
            if ( children == null )
                return 0;
            Hashtable task_defs = target.getProject().getTaskDefinitions();
            while ( children.hasMoreElements() ) {
                RuntimeConfigurable element = ( RuntimeConfigurable ) children.nextElement();
                String tag = element.getElementTag();
                if ( task_defs.containsKey( tag ) ) {
                    ++task_count;
                    task_count += countSubTasks( target, element );
                }
            }
        }
        catch ( Exception e ) {
            e.printStackTrace();
        }
        return task_count;
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
        Project p = ( Project ) project_cache.get( build_file );
        if ( p == null ) {
            p = new Project();
            project_cache.put( build_file, p );
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
        }
        return p;
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
