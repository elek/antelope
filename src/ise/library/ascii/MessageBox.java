/*
Copyright (c) Dale Anson, 2004
Redistribution and use in source and binary forms, with or without
modification, are permitted provided that the following conditions are met:
 1. Redistributions of source code must retain the above copyright notice,
 this list of conditions and the following disclaimer.
 2. Redistributions in binary form must reproduce the above copyright
 notice, this list of conditions and the following disclaimer in the
 documentation and/or other materials provided with the distribution.
 3. The name of the author may not be used to endorse or promote products
 derived from this software without specific prior written permission.
THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR IMPLIED
WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO
EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS;
OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR
OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF
ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
*/

package ise.library.ascii;

import java.io.*;
import java.text.*;
import java.util.*;

/**
 * An ascii text message box.
 *
 * @version   $Revision$
 */
public class MessageBox {

    private static String LS = System.getProperty( "line.separator" );
    private static int MAX_WIDTH = 60;
    
    public static void setMaxWidth(int width) {
        MAX_WIDTH = width;   
    }
    
    public static int getMaxWidth() {
        return MAX_WIDTH;   
    }

    /**
     * Create a box with the given text.
     *
     * @param text the message
     * @return a nicely formatted message box
     */
    public static String box( CharSequence text ) {
        return box( null, text );
    }

    /**
     * Create a box with the given text and title.
     *
     * @param title title to be placed above the message box
     * @param text the message
     * @return a nicely formatted message box
     */
    public static String box( CharSequence title, CharSequence text ) {
        if ( title == null && text == null )
            return "";
        if ( title != null && text == null ) {
            text = title;
            title = null;
        }
        List title_lines = getLines( title );
        List text_lines = getLines( text );
        int width = Math.max( getWidth( title_lines ), getWidth( text_lines ) );
        StringBuffer sb = new StringBuffer();
        sb.append( LS );
        if ( title_lines != null )
            sb.append( boxTitle( title_lines, width ) );
        sb.append( boxText( text_lines, width ) );
        return sb.toString();
    }

    /**
     * Gets the width of the widest line in the given list
     *
     * @param text
     * @return      The width value
     */
    private static int getWidth( List lines ) {
        if ( lines == null )
            return 0;
        int width = 0;
        for ( Iterator it = lines.iterator(); it.hasNext(); ) {
            width = Math.max( width, ( ( String ) it.next() ).length() );
        }
        return width;
    }

    /**
     * Breaks a single long line into several shorter lines of no more than
     * MAX_WIDTH characters. 
     *
     * @param line the line to wrap
     * @return      a list of lines, each of which will be no more than 
     * MAX_WIDTH characters long.
     */
    private static List wrapLine( String line ) {
        List list = new ArrayList();
        if ( line.length() <= MAX_WIDTH ) {
            list.add( line );
            return list;
        }
        BreakIterator words = BreakIterator.getWordInstance();
        words.setText( line );
        StringBuffer sb = new StringBuffer();
        int start = words.first();
        int first = start;
        int end = start;
        for ( end = words.next();
                end != BreakIterator.DONE;
                start = end, end = words.next() ) {
            sb.append( line.substring( start, end ) );
            if ( sb.length() > MAX_WIDTH )
                break;
        }
        String subline = line.substring( first, end );
        if ( subline.startsWith( " " ) && !subline.startsWith( "  " ) ) {
            subline = subline.substring( 1 );
        }
        list.add( subline );
        if (line.substring(end).length() > 0)
            list.addAll( wrapLine( line.substring( end + 1 ) ) );
        return list;
    }

    /**
     * Reads the given text and separates each line into an item in the returned
     * list. Lines longer than MAX_WIDTH will be broken into shorter lines.
     *
     * @param text
     * @return      The lines value
     */
    private static List getLines( CharSequence text ) {
        if ( text == null )
            return null;
        ArrayList lines = new ArrayList();
        BufferedReader br = new BufferedReader( new StringReader( text.toString() ) );
        String line = null;
        try {
            while ( true ) {
                line = br.readLine();
                if ( line == null )
                    break;
                line = line.replaceAll( "[\t]", "   " );
                lines.addAll( wrapLine( line ) );
            }
        }
        catch ( Exception e ) {
            //e.printStackTrace();
        }
        return lines;
    }

    /**
     * Get the top of the box
     *
     * @param lines
     * @param width
     * @return       Description of the Returned Value
     */
    private static String boxTitle( List lines, int width ) {
        return boxText( lines, width, false );
    }

    /**
     * Get the body of the box
     *
     * @param lines
     * @param width
     * @return       Description of the Returned Value
     */
    private static String boxText( List lines, int width ) {
        return boxText( lines, width, true );
    }

    /**
     * Get a box, with or without a bottom
     *
     * @param lines lines of text to put in box
     * @param width pad each line to be this wide
     * @param withBottom maybe add a bottom line
     * @return            Description of the Returned Value
     */
    private static String boxText( List lines, int width, boolean withBottom ) {
        String hline = getHR( width + 2 );
        StringBuffer sb = new StringBuffer();

        // top line
        sb.append( hline );

        // body
        for ( Iterator it = lines.iterator(); it.hasNext(); ) {
            String line = ( String ) it.next();
            sb.append( "| " );
            sb.append( line );
            for ( int i = line.length(); i < width; i++ ) {
                sb.append( " " );
            }
            sb.append( " |" ).append( LS );
        }

        // bottom line
        if ( withBottom )
            sb.append( hline );
        return sb.toString();
    }

    /**
     * Gets a horizontal rule. A horizontal rule looks like this:<br>
     * +-------------------------+<br>
     * The actual width of the rule will be 
     * <code>width + 2</code>
     * 
     *
     * @param width the number of dashes in the rule
     * @return       a line with a leading + followed by <code>width</code> 
     * dashes, followed by another +.
     */
    private static String getHR( int width ) {
        StringBuffer sb = new StringBuffer();
        sb.append( "+" );
        for ( int i = 0; i < width; i++ ) {
            sb.append( "-" );
        }
        sb.append( "+" );
        sb.append( LS );
        return sb.toString();
    }

    /**
     * The main program for the StringBox class
     *
     * @param args  The command line arguments
     */
    public static void main( String[] args ) {
        StringBuffer sb = new StringBuffer();
        sb.append( "1) jdbc.oracle.whatever:@hoser" ).append( LS );
        sb.append( "\tcom.oracle.jdbc.Driver" ).append( LS );
        sb.append( "x) Cancel" );
        sb.append( "Character boundary analysis allows users to interact with characters as they expect to, for example, when moving the cursor around through a text string. Character boundary analysis provides correct navigation of through character strings, regardless of how the character is stored. For example, an accented character might be stored as a base character and a diacritical mark. What users consider to be a character can differ between languages." );
        System.out.println( MessageBox.box( "Select a database:", sb.toString() ) );

        sb = new StringBuffer();
        sb.append( "-> Error: Something unusual has occured to cause the driver to fail. Please report this exception: Exception: java.sql.SQLException: FATAL:  user 'Bulldog' does not exist\n" );
        sb.append( "\n" );
        sb.append( "Stack Trace:\n" );
        sb.append( "\n" );
        sb.append( "java.sql.SQLException: FATAL:  user 'bulldog' does not exist\n" );
        sb.append( "at org.postgresql.Connection.openConnection(Connection.java:241)\n" );
        sb.append( "at org.postgresql.Driver.connect(Driver.java:122)\n" );
        sb.append( "at org.apache.commons.dbcp.DriverConnectionFactory.createConnection(ctionFactory.java:37)\n" );
        sb.append( "at org.apache.commons.dbcp.PoolableConnectionFactory.makeObject(PoolableConnectionFactory.java 90)\n" );
        sb.append( "at org.apache.commons.pool.impl.GenericObjectPool.borrowObject(GenericObjectPool.java 771)\n" );
        sb.append( "at org.apache.commons.dbcp.PoolingDataSource.getConnection(PoolingDataSource.java 5)\n" );
        sb.append( "at ise.dbconsole.DbConsoleDAO.getConnection(DbConsoleDAO.java:829)\n" );
        sb.append( "at ise.dbconsole.DbConsoleDAO.getDbInfo(DbConsoleDAO.java:152)\n" );
        sb.append( "at ise.dbconsole.command.DbInfoCommand.execute(DbInfoCommand.java:49\n" );
        sb.append( "at ise.dbconsole.command.DbInfoCommand.execute(DbInfoCommand.java:41\n" );
        sb.append( "at ise.dbconsole.command.SetupDAOCommand2.createDAO(SetupDAOCommand2.java:163)\n" );
        sb.append( "at ise.dbconsole.command.SetupDAOCommand2.setupDAO(SetupDAOCommand2.javaa:145)\n" );
        sb.append( "at ise.dbconsole.command.SetupDAOCommand2.execute(SetupDAOCommand2.java:69)\n" );
        sb.append( "at ise.dbconsole.command.SetupDAOCommand2.execute(SetupDAOCommand2.java:56)\n" );
        sb.append( "at ise.dbconsole.DbConsole.executeCommand(DbConsole.java:521)\n" );
        sb.append( "at ise.dbconsole.DbConsole$1.run(DbConsole.java:158)\n" );
        sb.append( "End of Stack Trace\n" );
        System.out.println( MessageBox.box( "Here's an exception:", sb.toString() ) );
        
    }
}

