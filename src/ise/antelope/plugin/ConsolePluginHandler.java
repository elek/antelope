package ise.antelope.plugin;

import java.awt.Color;
import java.awt.Font;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import javax.swing.SwingUtilities;
import console.*;
import errorlist.*;
import org.gjt.sp.jedit.jEdit;
import org.gjt.sp.jedit.View;
import org.gjt.sp.jedit.gui.DockableWindowManager;


/**
 * This log handler writes messages to the Console plugin.
 *
 * @author   Dale Anson
 */
public class ConsolePluginHandler extends Handler {

    private AntelopePluginPanel _panel = null;
    private View view = null;
    private DefaultErrorSource error_source = null;
    private Console console = null;
    private AntelopeShell shell = null;
    private Color GREEN = new Color( 0, 153, 51 );
    private Color YELLOW = new Color( 0xefef8f );
    private Color RED = new Color( 0xff4444 );
    private Color foreground;

    public ConsolePluginHandler( AntelopePluginPanel panel ) {
        _panel = panel;
        view = panel.getView();
        error_source = panel.getErrorSource();
        setupConsole();
        foreground = jEdit.getColorProperty( "view.fgColor" );
    }

    public ConsolePluginHandler( View view, DefaultErrorSource es ) {
        view = view;
        error_source = es;
        setupConsole();
    }

    private void setupConsole() {
        // set up the console for output display
        shell = AntelopePlugin.getShell();
        DockableWindowManager mgr = view.getDockableWindowManager();
        console = ( Console ) mgr.getDockable( "console" );
        if ( console == null ) {
            mgr.addDockableWindow( "console" );
            console = ( Console ) mgr.getDockable( "console" );
        }
        console.setShell( shell );
        Font font = view.getEditPane().getTextArea().getPainter().getFont();
        console.getConsolePane().setFont( font );
    }


    /**
     * Writes the log record to the Console plugin.
     *
     * @param record  a LogRecord
     */
    public void publish( LogRecord record ) {

        /* some jedit 'update' messages get dumped on System.out, which is what
        Ant writes to, so those messages end up here mixed with the Ant output.
        This may not be sufficient, but all I've seen so far are DockableWindowUpdate
        and ErrorSourceUpdate messages, so this regex works for now. */
        if ( record.getMessage().matches( "(.*)?(Update\\[)(.*)" ) ) {
            return ;
        }
        if ( _panel.getBuildFile() == null ) {
            return ;
        }
        String dir = _panel.getBuildFile().getParent();
        if ( dir == null ) {
            return ;
        }

        /* trim the message for parsing by the ConsolePlugin, but print out
        the full message. This keeps the message formatting nice while letting
        the error parser work correctly. */
        String parseline = record.getMessage().trim();

        // bring the console to the front, but only at the start and end of a build.
        if ( parseline.indexOf( "===== BUILD STARTED =====" ) > -1 ) {
            showConsole( );
        }

        // give focus back to the buffer at the end of the build
        if ( parseline.indexOf( "===== BUILD FINISHED =====" ) > -1 ) {
            view.goToBuffer( view.getBuffer() );
        }

        if ( _panel.useErrorParsing() ) {
            int type = ConsolePlugin.parseLine( view, parseline, dir, error_source );
            Output output = console.getOutput( "Antelope" );
            switch ( type ) {
                case ErrorSource.ERROR:
                    output.print( console.getErrorColor(), parseline );
                    return ;
                case ErrorSource.WARNING:
                    output.print( console.getWarningColor(), parseline );
                    return ;
                default:
                    // fall through on purpose
            }
        }
        console.getOutput( "Antelope" ).print( getColorForLevel( record.getLevel() ), parseline );
    }

    public void showConsole( ) {
        SwingUtilities.invokeLater(
            new Runnable() {
                public void run() {
                    DockableWindowManager mgr = view.getDockableWindowManager();
                    mgr.showDockableWindow( "console" );
                }
            }
        );
    }

    public AntelopeShell getShell() {
        return shell;
    }

    private Color getColorForLevel( Level level ) {
        if ( level.equals( Level.SEVERE ) )
            return RED;
        if ( level.equals( Level.CONFIG ) )
            return YELLOW;
        if ( level.equals( Level.WARNING ) )
            return GREEN;
        return foreground;
    }

    /**
     * No-op.
     */
    public void flush() {
        // does nothing
    }

    /**
     * No-op.
     */
    public void close() {
        // does nothing
    }
}