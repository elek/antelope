package ise.antelope.launcher.subjar;

import java.net.*;
import java.io.*;

public class Handler extends URLStreamHandler {
    
    public URLConnection openConnection(URL url) throws IOException {
        URLConnection c = new SubJarURLConnection(url);
        c.connect();
        return c;
    }
}

