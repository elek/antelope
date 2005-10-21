package ise.antelope.tasks;

import java.io.File;
import java.util.*;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.DirectoryScanner;
import org.apache.tools.ant.TaskContainer;
import org.apache.tools.ant.Target;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.types.FileSet;

/**
 * Modeled after the TestSuite provided by jUnit.
 *
 * @author Dale Anson
 */
public class Suite extends Task implements TaskContainer, TestStatisticAccumulator {

    // name for the suite
    private String name = null;

    // vector to hold nested testcases
    private Vector tasks = null;

    // vector to hold filesets
    private Vector filesets = null;

    // stats accumulators
    private int totalTestCount = 0;
    private int totalRanCount = 0;
    private int totalPassedCount = 0;
    private int totalFailedCount = 0;

    // should the results be shown?
    private boolean showSummary = true;

    // should the tests be ran?
    private boolean enabled = true;

    // should Asserts be enabled?
    private boolean assertEnabled = true;

    public void init() {
        super.init();
        setTaskName( "suite" );
    }

    /**
     * Add a testcase to the suite.
     * @param tc the testcase to add
     */
    public void addTestCase( TestCase tc ) {
        addTask( tc );
    }

    /**
     * Add a stuie to the suite.
     * @param s the suite to add
     */
    public void addSuite( Suite s ) {
        addTask( s );
    }

    /**
     * Add a task to execute, most likely a testcase, but really can be any task. <p>
     *
     * @param task  Nested task to execute. <p>
     */
    public void addTask( Task task ) {
        if ( tasks == null )
            tasks = new Vector();
        tasks.addElement( task );
    }

    /**
     * Add a fileset to the suite.  For each file in the fileset, a testcase
     * will be created.
     */
    public void addFileset( FileSet fs ) {
        if ( tasks == null )
            tasks = new Vector();
        tasks.addElement( fs );
    }

    /**
     * Set a name for the suite, optional attribute.
     * @param s the name for the suite.
     */
    public void setName( String s ) {
        name = s;
    }

    /**
     * @return the name of the suite, may be null.    
     */
    public String getName() {
        return name;
    }

    /**
     * @param b if true, execute the test.  This is handy for enabling or disabling
     * groups of tests by setting a single property.  Optional, default
        * is true, the suite should run.
     */
    public void setEnabled( boolean b ) {
        enabled = b;
    }

    /**
     * Should asserts be enabled? Asserts are enabled by default.
     * @param b if false, disable asserts
     */
    public void setAssertsenabled( boolean b ) {
        assertEnabled = b;
    }

    /**
     * Should the results be shown?
     * @param b show the results if true
     */
    public void setShowsummary( boolean b ) {
        showSummary = b;
    }

    public int getTestCaseCount() {
        return totalTestCount;
    }

    public int getRanCount() {
        return totalRanCount;
    }

    public int getPassedCount() {
        return totalPassedCount;
    }

    public int getFailedCount() {
        return totalFailedCount;
    }

    /** Run tests. */
    public void execute() {
        if ( !enabled )
            return ;

        String ae = assertEnabled ? "true" : "false";
        if ( assertEnabled )
            getProject().setProperty( "ant.enable.asserts", ae );

        try {
            // get the setUp and tearDown targets
            Target setUp = null;
            Target tearDown = null;
            Hashtable targets = getProject().getTargets();
            Enumeration en = targets.keys();
            while ( en.hasMoreElements() ) {
                String target_name = ( String ) en.nextElement();
                if ( target_name.equals( "setUp" ) )
                    setUp = ( Target ) targets.get( target_name );
                else if ( target_name.equals( "tearDown" ) )
                    tearDown = ( Target ) targets.get( target_name );
            }

            // run the setUp target
            if ( setUp != null )
                setUp.execute();

            // create testcases out of any filesets, maintaining order with
            // testcases already added.
            Vector testcases = new Vector();
            if ( tasks != null && tasks.size() > 0 ) {
                for ( Enumeration e = tasks.elements(); e.hasMoreElements(); ) {
                    Object o = e.nextElement();
                    if ( o instanceof FileSet ) {
                        loadTestFiles( ( FileSet ) o, testcases );
                    }
                    else {
                        testcases.addElement( o );
                    }
                }

                // actually execute the testcases
                for ( Enumeration e = testcases.elements(); e.hasMoreElements(); ) {
                    Task task = ( Task ) e.nextElement();
                    task.perform();
                }
            }

            // run the tearDown target
            if ( tearDown != null )
                tearDown.execute();

            // tabulate the results.  The individual testcases contain their
            // own stats, so it is just a matter of reading them and accumulating
            // the totals.
            for ( Enumeration e = testcases.elements(); e.hasMoreElements(); ) {
                Task task = ( Task ) e.nextElement();
                if ( task instanceof TestStatisticAccumulator ) {
                    TestStatisticAccumulator acc = ( TestStatisticAccumulator ) task;
                    totalTestCount += acc.getTestCaseCount();
                    totalRanCount += acc.getRanCount();
                    totalPassedCount += acc.getPassedCount();
                    totalFailedCount += acc.getFailedCount();
                }
            }
            if ( showSummary ) {
                log( getSummary() );
            }

        }
        catch ( Exception ex ) {
            ex.printStackTrace();
            throw new BuildException( ex.getMessage() );
        }
    }

    public String getSummary() {
        StringBuffer sb = new StringBuffer();
        String ls = System.getProperty( "line.separator" );
        if ( name != null && name.length() > 0 ) {
            sb.append( "++-----------------------------------------++" ).append( ls );
            sb.append( "++" ).append( ls );
            sb.append( "++ " ).append( name ).append( ls );
            sb.append( "++" ).append( ls );
        }
        sb.append( "++-- Totals -------------------------------++" ).append( ls );
        sb.append( "++ Total Ran " ).append( totalRanCount ).append( " out of " ).append( totalTestCount ).append( " tests." ).append( ls );
        sb.append( "++ Total Passed: " ).append( totalPassedCount ).append( ls );
        sb.append( "++ Total Failed: " ).append( totalFailedCount ).append( ls );
        sb.append( "++-----------------------------------------++" ).append( ls );
        return sb.toString();
    }

    /**
     * Create TestCases from the files specified in a FileSet.
     * @param fs the fileset to use for testcases
     * @param destination where to store the newly created TestCases.
     */
    protected void loadTestFiles( FileSet fs, Vector destination ) {
        File d = fs.getDir( getProject() );
        DirectoryScanner ds = fs.getDirectoryScanner( getProject() );
        String[] files = ds.getIncludedFiles();
        String[] dirs = ds.getIncludedDirectories();
        if ( files.length > 0 ) {
            for ( int j = 0; j < files.length; j++ ) {
                File f = new File( d, files[ j ] );
                TestCase tc = createTestCase( f );
                destination.addElement( tc );
            }
        }

        if ( dirs.length > 0 ) {
            for ( int j = dirs.length - 1; j >= 0; j-- ) {
                File dir = new File( d, dirs[ j ] );
                String[] dirFiles = dir.list();
                if ( dirFiles != null && dirFiles.length > 0 ) {
                    for ( int i = 0; i < dirFiles.length; i++ ) {
                        File f = new File( dir, dirFiles[ i ] );
                        TestCase tc = createTestCase( f );
                        destination.addElement( tc );
                    }
                }
            }
        }
    }

    private TestCase createTestCase( File f ) {
        TestCase tc = new TestCase();
        tc.init();
        tc.setFile( f );
        tc.setProject( getProject() );
        return tc;
    }
}
