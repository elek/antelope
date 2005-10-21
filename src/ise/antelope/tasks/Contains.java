/*
* The Apache Software License, Version 1.1
*
* Copyright (c) 2001-2002 The Apache Software Foundation.  All rights
* reserved.
*
* Redistribution and use in source and binary forms, with or without
* modification, are permitted provided that the following conditions
* are met:
*
* 1. Redistributions of source code must retain the above copyright
*    notice, this list of conditions and the following disclaimer.
*
* 2. Redistributions in binary form must reproduce the above copyright
*    notice, this list of conditions and the following disclaimer in
*    the documentation and/or other materials provided with the
*    distribution.
*
* 3. The end-user documentation included with the redistribution, if
*    any, must include the following acknowlegement:
*       "This product includes software developed by the
*        Apache Software Foundation (http://www.apache.org/)."
*    Alternately, this acknowlegement may appear in the software itself,
*    if and wherever such third-party acknowlegements normally appear.
*
* 4. The names "The Jakarta Project", "Ant", and "Apache Software
*    Foundation" must not be used to endorse or promote products derived
*    from this software without prior written permission. For written
*    permission, please contact apache@apache.org.
*
* 5. Products derived from this software may not be called "Apache"
*    nor may "Apache" appear in their names without prior written
*    permission of the Apache Group.
*
* THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
* WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
* OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
* DISCLAIMED.  IN NO EVENT SHALL THE APACHE SOFTWARE FOUNDATION OR
* ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
* SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
* LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
* USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
* ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
* OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
* OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
* SUCH DAMAGE.
* ====================================================================
*
* This software consists of voluntary contributions made by many
* individuals on behalf of the Apache Software Foundation.  For more
* information on the Apache Software Foundation, please see
* <http://www.apache.org/>.
*/
package ise.antelope.tasks;

import java.util.*;

import org.apache.tools.ant.taskdefs.condition.IsTrue;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.PropertyHelper;

/**
 * @author Dale Anson
 */
public class Contains extends IsTrue {
    
    private String propertyName = null;
    
    private String string, subString;
    private boolean caseSensitive = true;

    /**
     * The name of a property value to search in.
     * @param s the property name 
     * @since Ant 1.5
     */
    public void setProperty(String s) {
        propertyName = s;   
    }
    
    /**
     * The string to search in.
     * @param string the string to search in
     * @since Ant 1.5
     */
    public void setString(String string) {
        this.string = string;
    }

    /**
     * The string to search for.
     * @param subString the string to search for
     * @since Ant 1.5
     */
    public void setSubstring(String subString) {
        this.subString = subString;
    }

    /**
     * Whether to search ignoring case or not.
     * @param b if true, ignore case
     * @since Ant 1.5
     */
    public void setCasesensitive(boolean b) {
        caseSensitive = b;
    }

    /**
     * @since Ant 1.5
     * @return true if the substring is within the string
     * @exception BuildException if the attributes are not set correctly
     */
    public boolean eval() throws BuildException {
        String in = string;
        if (in == null)
            in = getProject().getProperty(propertyName);
        if (in == null)
            in = getProject().getUserProperty(propertyName);
        if (in == null)
            return false;
        
        if (subString == null)
            throw new BuildException("subString must be set in contains.");
        
        return caseSensitive
            ? in.indexOf(subString) > -1
            : in.toLowerCase().indexOf(subString.toLowerCase()) > -1;
    }

}
