// $Id$
/*
 * Based on the Apache Software License, Version 1.1
 *
 * Copyright (c) 2002 Dale Anson.  All rights reserved.
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
 *       "This product includes software developed by Dale Anson,
 *        danson@users.sourceforge.net."
 *    Alternately, this acknowlegement may appear in the software itself,
 *    if and wherever such third-party acknowlegements normally appear.
 *
 * 4. The name "Antelope" must not be used to endorse or promote products derived
 *    from this software without prior written permission. For written
 *    permission, please contact danson@users.sourceforge.net.
 *
 * 5. Products derived from this software may not be called "Antelope"
 *    nor may "Antelope" appear in their names without prior written
 *    permission of Dale Anson.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL DALE ANSON OR ANY PROJECT
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 * ====================================================================
 */

package ise.antelope.plugin;

import console.*;

/**
 * A Console shell to display Ant output. Also uses the "stop" button to stop a
 * running build.
 * 
 * 2006-03-18 Patched for Console 4.2.4
 *  
 * @version $Id$
 * 
 * @author Dale Anson, danson@germane-software.com
 * 
 * 
 */
public class AntelopeShell extends Shell
{

	private Thread runner = null;

	public AntelopeShell()
	{
		super("Antelope");
	}

	public void printInfoMessage(Output output)
	{
		output.print(null, "Antelope shell, shows output of running Ant targets.");
	}

	/**
	 * Stops the build process.
	 */
	public void stop(Console console)
	{
		if (runner != null && runner.isAlive())
			runner.interrupt();
	}

	public boolean waitFor(Console console)
	{
		return false;
	}

	public void setRunner(Thread runner)
	{
		this.runner = runner;
	}

	public void execute(Console console, String input, Output output, Output error,
		String command)
	{
		// TODO Auto-generated method stub

	}
}
