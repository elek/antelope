package ise.antelope.plugin;

import java.awt.Color;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import console.*;
import errorlist.*;
import org.gjt.sp.jedit.View;
import org.gjt.sp.util.Log;
import org.gjt.sp.jedit.gui.DockableWindowManager;


/**
 * This log handler writes messages to the Console plugin.
 *
 * @author   Dale Anson
 */
public class ConsolePluginHandler extends Handler {

    private AntelopePluginPanel _panel = null;
    private View _view = null;
    private DefaultErrorSource _error_source = null;
    private Console _console = null;
    private AntelopeShell _shell = null;
    private Color GREEN = new Color( 0, 153, 51 );

    public ConsolePluginHandler( AntelopePluginPanel panel ) {
        _panel = panel;
        _view = panel.getView();
        _error_source = panel.getErrorSource();
        setupConsole();
    }
    
    public ConsolePluginHandler(View view, DefaultErrorSource es) {
       _view = view;
       _error_source = es;
       setupConsole();
    }
    
    private void setupConsole() {
        // set up the console for output display
        _shell = AntelopePlugin.SHELL;
        DockableWindowManager mgr = _view.getDockableWindowManager();
        _console = ( Console ) mgr.getDockable( "console" );
        if ( _console == null ) {
            mgr.addDockableWindow( "console" );
            _console = ( Console ) mgr.getDockable( "console" );
        }
        _console.setShell( _shell );
    }


    /**
     * Writes the log record to the Console plugin.
     *
     * @param record  a LogRecord
     */
    public void publish( LogRecord record ) {
        if ( _panel.getBuildFile() == null )
            return ;
        String dir = _panel.getBuildFile().getParent();
        if ( dir == null )
            return ;

        // trim the message for parsing by the ConsolePlugin, but print out
        // the full message. This keeps the message formatting nice while letting
        // the error parser work correctly.
        String parseline = record.getMessage().trim();
        String line = record.getMessage();

        // bring the console to the front
        showConsole( );

        if ( _panel.useErrorParsing() ) {
            int type = ConsolePlugin.parseLine( _view, parseline, dir, _error_source );
            switch ( type ) {
                case ErrorSource.ERROR:
                    _console.print( _console.getErrorColor(), parseline );
                    return ;
                case ErrorSource.WARNING:
                    _console.print( _console.getWarningColor(), parseline );
                    return ;
                default:
                    // fall through on purpose
            }
        }
        _console.print( getColorForLevel( record.getLevel() ), parseline );
    }

    public void showConsole( ) {
        DockableWindowManager mgr = _view.getDockableWindowManager();
        mgr.showDockableWindow( "console" );
    }
    
    public AntelopeShell getShell() {
        return _shell;   
    }

    private Color getColorForLevel( Level level ) {
        if ( level.equals( Level.SEVERE ) )
            return Color.RED;
        if ( level.equals( Level.CONFIG ) )
            return Color.BLACK;
        if ( level.equals( Level.WARNING ) )
            return GREEN;
        return Color.BLUE;
    }

    /**
     * No-op.
     */
public void flush() { }

    /**
     * No-op.
     */
    public void close() { }
}

