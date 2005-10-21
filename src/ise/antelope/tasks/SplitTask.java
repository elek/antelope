
package ise.antelope.tasks;

import java.io.*;

import org.apache.tools.ant.*;
import org.apache.tools.ant.taskdefs.*;

/**
 * @author    Dale Anson
 * @version   $Revision$
 * @since     Ant 1.6
 */
public class SplitTask extends Task {

    private String prefix = "x";
    private int bytes = -1;
    private int lines = 1000;
    private String value = null;
    private File file = null;
    private File outputDir = null;
    private boolean failOnError = true;


    /**
     * Sets the prefix attribute of the SplitTask object
     *
     * @param x  The new prefix value
     */
    public void setPrefix(String x) {
        prefix = x;
    }

    /**
     * Use bytes or lines, not both. In general, use bytes for binary files,
     * lines for text.
     *
     * @param b  number of bytes per part.
     */
    public void setBytes(int b) {
        bytes = b;
        lines = -1;
    }

    /**
     * The linux split command allows modifiers: b for 512, k for 1K, m for 1
     * Meg.
     *
     * @param b  the number of bytes per part, with an optional modifier. If
     *      there is no modifier, treat same as setBytes(int).  For example,
     *      setSize("100k") is the same as setBytes(100 * 1024).
     */
    public void setSize(String b) {
        if (b == null)
            return;
        if (b.length() == 0)
            return;
        b = b.toLowerCase();
        String modifier = b.substring(b.length() - 1);
        int multiplier = 1;
        b = b.substring(0, b.length() - 1);
        if (modifier.equals("b")) {
            multiplier = 512;
        }
        else if (modifier.equals("k")) {
            multiplier = 1024;
        }
        else if (modifier.equals("m")) {
            multiplier = 1024 * 1024;
        }
        else {
            b = b + modifier;   
        }
        try {
            int size = Integer.parseInt(b) * multiplier;
            setBytes(size);
            return;
        }
        catch (NumberFormatException e) {
            throw new BuildException("Invalid bytes parameter.");
        }
    }

    /**
     * Use bytes or lines, not both. In general, use bytes for binary files,
     * lines for text.
     *
     * @param x  The new lines value
     */
    public void setLines(int x) {
        lines = x;
        bytes = -1;
    }

    /**
     * Split the text content of value of the given property.
     *
     * @param p  the name of the property whose value will be split.
     */
    public void setProperty(String p) {
        value = getProject().getProperty(p);
        if (value == null || value.equals(""))
            throw new BuildException("Property " + p + " has no value.");
    }

    /**
     * Split the given string.
     *
     * @param v  a string
     */
    public void setValue(String v) {
        if (v == null || v.equals(""))
            throw new BuildException("Value is null or empty.");
        value = v;
    }

    /**
     * Split the contents of the given file.
     *
     * @param f  the name of the file
     */
    public void setFile(File f) {
        file = f;
    }

    /**
     * Where to put the parts. If file has been set and output dir not set,
     * output to directory containing file.
     *
     * @param d  the output directory
     */
    public void setOutputdir(File d) {
        outputDir = d;
    }

    /**
     * Determines whether the build should fail if there is an error. Default is
     * true.
     *
     * @param fail  true or false
     */
    public void setFailonerror(boolean fail) {
        failOnError = fail;
    }


    /**
     * Split the given property or file into pieces.
     *
     * @exception BuildException  only if failOnError is true
     */
    public void execute() throws BuildException {
        // check params --
        // must have value or file
        if (value == null && file == null)
            throw new BuildException("Must have property, value, or file.");
        // if no file, must have outputDir
        if (file == null && outputDir == null)
            throw new BuildException("Must have output directory.");
        // must have only one of value or file
        if (value != null && file != null)
            throw new BuildException("Must not have more than one of property, value, or file.");

        try {
            if (value != null) {
                splitValue();
            }
            else {
                splitFile();
            }
        }
        catch (Exception e) {
            e.printStackTrace();
            if (failOnError) {
                throw new BuildException(e.getMessage());
            }
            else
                log(e.getMessage());
        }
    }

    /**
     * Description of the Method
     *
     * @exception Exception  Description of Exception
     */
    private void splitValue() throws Exception {
        if (!outputDir.exists() && !outputDir.mkdirs()) {
            throw new IOException("Unable to create output directory.");
        }

        StringReader reader = new StringReader(value);
        int bytes_read = 0;
        int suffix = 0;
        if (bytes > 0) {
            // make files all the same number of bytes
            char[] buffer = new char[bytes];
            while (bytes_read > -1) {
                bytes_read = reader.read(buffer, 0, bytes);
                if (bytes_read == -1)
                    break;
                FileWriter fw = new FileWriter(new File(outputDir, prefix + "." + String.valueOf(suffix)));
                fw.write(buffer, 0, bytes_read);
                fw.flush();
                fw.close();
                ++suffix;
            }
        }
        else {
            // make files all the same number of lines
            // reusing offset as line count
            LineNumberReader lnr = new LineNumberReader(reader);
            String line = lnr.readLine();
            FileWriter fw = new FileWriter(new File(outputDir, prefix + "." + String.valueOf(suffix)));
            while (line != null) {
                fw.write(line);
                if (lnr.getLineNumber() % lines == 0) {
                    fw.flush();
                    fw.close();
                    ++suffix;
                    fw = new FileWriter(new File(outputDir, prefix + "." + String.valueOf(suffix)));
                }
            }
            fw.flush();
            fw.close();
        }
    }

    /**
     * Description of the Method
     *
     * @exception Exception  Description of Exception
     */
    private void splitFile() throws Exception {
        if (!file.exists())
            throw new FileNotFoundException(file.toString());
        if (file.length() == 0)
            throw new BuildException("Zero length file.");
        if (outputDir == null)
            outputDir = file.getParentFile();
        if (!outputDir.exists() && !outputDir.mkdirs()) {
            throw new IOException("Unable to create output directory.");
        }

        int suffix = 0;
        if (bytes > 0) {
            // make files all the same number of bytes
            BufferedInputStream bis = new BufferedInputStream(new FileInputStream(file));
            int bytes_read = 0;
            byte[] buffer = new byte[bytes];
            while (bytes_read > -1) {
                bytes_read = bis.read(buffer, 0, bytes);
                if (bytes_read == -1)
                    break;
                FileOutputStream fos = new FileOutputStream(new File(outputDir, prefix + "." + String.valueOf(suffix)));
                fos.write(buffer, 0, bytes_read);
                fos.flush();
                fos.close();
                ++suffix;
            }
        }
        else {
            // make files all the same number of lines
            // reusing offset as line count
            LineNumberReader lnr = new LineNumberReader(new FileReader(file));
            String line = lnr.readLine();
            FileWriter fw = new FileWriter(new File(outputDir, prefix + "." + String.valueOf(suffix)));
            while (line != null) {
                fw.write(line);
                if (lnr.getLineNumber() % lines == 0) {
                    fw.flush();
                    fw.close();
                    ++suffix;
                    fw = new FileWriter(new File(outputDir, prefix + "." + String.valueOf(suffix)));
                }
            }
            fw.flush();
            fw.close();
        }
    }
}


