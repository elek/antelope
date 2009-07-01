package ise.antelope.common;

import java.util.logging.Level;
import java.util.logging.Logger;

public class LoggerFactory {

    private static class SingletonHolder {
        private static LoggerFactory INSTANCE = new LoggerFactory();   
    }
    
    private LoggerFactory() {
        
    }
    
    public static LoggerFactory getInstance() {
        return SingletonHolder.INSTANCE;   
    }
    
    public Logger createLogger() {
        Logger logger = Logger.getLogger( "ise.antelope.Antelope" );
        logger.setUseParentHandlers( false );
        logger.setLevel( Level.ALL );
        return logger;
        
        
    }
    
    public AntLogger createAntLogger() {
        return new AntLogger();
    }
}