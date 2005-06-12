package ise.antelope.common;

import java.io.*;

/**
 * Copyright 2003
 *
 * @version   $Revision$
 */
public class Log {

    private static File outfile = new File(System.getProperty("user.home"), "antelope_debug.log");
    private static final String LS = System.getProperty("line.separator");
    
    private static boolean canLog = System.getProperty("antelope.log.on") != null;

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
        StringWriter sw = new StringWriter();
        t.printStackTrace(new PrintWriter(sw));
        log(sw.toString());
    }
}

