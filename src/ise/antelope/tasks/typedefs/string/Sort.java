package ise.antelope.tasks.typedefs.string;

import java.util.*;

/**
 * Copyright 2003
 *
 * @version   $Revision$
 */
public class Sort implements StringOp {
    
    private String separator = null;
    
    public void setSeparator(String s) {
        separator = s;
    }
    
    /**
     * Description of the Method
     *
     * @param s
     * @return   Description of the Returned Value
     */
    public String execute(String s) {
        if (s == null)
            return "";
        List list = new ArrayList();
        StringTokenizer st = separator == null ? new StringTokenizer(s) : new StringTokenizer(s, separator);
        while(st.hasMoreTokens()) {
            list.add(st.nextToken());   
        }
        Collections.sort(list);
        StringBuffer sorted = new StringBuffer();
        for (Iterator it = list.iterator(); it.hasNext(); ) {
            sorted.append((String)it.next());
            if (it.hasNext())
                sorted.append(separator);
        }
        return sorted.toString();
    }
}


