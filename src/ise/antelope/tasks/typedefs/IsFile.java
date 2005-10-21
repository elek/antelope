package ise.antelope.tasks.typedefs;

import java.io.*;

/**
 * Copyright 2003
 *
 * @version   $Revision$
 */
public class IsFile implements FileOp {
    
    /**
     * Checks if the given file is a file
     *
     * @param f a file
     * @return true if the file is a file, not a directory.
     */
    public String execute(File f) {
        if (f == null)
            throw new IllegalArgumentException("file cannot be null");
        return f.isFile() ? "true" : "false";
    }
}


