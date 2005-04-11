package ise.antelope.launcher;

import java.net.URL;

public class Args {

    private URL[] urls;
    private String[] args;
    
    
    public void setURLs(URL[] urls) {
        this.urls = urls;   
    }
    
    public URL[] getURLs() {
        return urls;   
    }
    
    public void setArgs(String[] args) {
        this.args = args;   
    }
    
    public String[] getArgs() {
        return args;   
    }
}
