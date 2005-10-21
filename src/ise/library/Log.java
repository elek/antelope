package ise.library;

import java.io.*;

/**
 * Copyright 2003
 *
 * @version   $Revision$
 */
public class Log {

    private static File outfile = new File(System.getProperty("user.home"), "antelope_debug.log");
    private static final String LS = System.getProperty("line.separator");
    
    private static boolean canLog = true; //System.getProperty("antelope.log.on") != null;
    
    static {
        System.out.println("starting Log");   
    }

    /**
     * Description of the Method
     *
     * @param msg
     */
    public static void log(CharSequence msg) {
        if (!canLog)
            return;
        try {
            FileWriter fw = new FileWriter(outfile, true);
            fw.write(msg.toString());
            fw.write(LS);
            fw.flush();
            fw.close();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Description of the Method
     *
     * @param t
     */
    public static void log(Throwable t) {
        if (!canLog)
            return;
        log(getStackTrace(t));
    }
    
    public static String getStackTrace(Throwable t) {
        StringWriter sw = new StringWriter();
        t.printStackTrace(new PrintWriter(sw));
        return sw.toString();
    }
}

