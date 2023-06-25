/*
 *  (C) Copyright 2005 headissue GmbH; Jens Wilke
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */

package com.headissue.asterisk.jtapi.gjtapi;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

import javax.telephony.ProviderUnavailableException;

import net.sf.asterisk.manager.ManagerConnection;
import net.sf.asterisk.manager.TimeoutException;
import net.sf.asterisk.manager.action.CommandAction;
import net.sf.asterisk.manager.response.CommandResponse;

/**
 * Parser for an Asterisk dialplan context.
 * This class gets an Asterisk dialplan context as list of
 * lines and provides methods to extract information out of it, just as command,
 * argruments, extension id etc.
 * 
 * @author jw
 * @version $Id: DialplanContextParser.java,v 1.5 2006/06/29 08:44:11 jwilke Exp $
 */
public class DialplanContextParser {
	
	ManagerConnection conn;
	
	DialplanLines lines = null;
	
	String id;
	int priority;
	String command;
	String argument;
	String context;
	
	Stack includeStack = new Stack();
	
	/**
	 * @param _dialplanLines the dialplan context as strings
	 * @param _context the context used for local gotos
	 */
	public DialplanContextParser(ManagerConnection _conn, String _context) {
		conn = _conn;
		context = _context;
		nest(context, true);
	}
	
	/**
	 * Construct the parser without manager connection and therefore enable to
	 * handle includes. Used by the JUnit test cases.
	 * @param _lines
	 * @param _context
	 */
	public DialplanContextParser(List _lines, String _context) {
		context = _context;
		lines = new DialplanLines(_lines);
	}
	
	void nest(String _context) {
		nest(_context, false);
	}
	
	void nest(String _context, boolean _toplevel) {
		// we don't set the different context because everything appears to
		// be in the initial context
		if (lines!=null) {
			includeStack.push(lines);
		}
    CommandAction cmd = new CommandAction();
    cmd.setCommand("show dialplan "+_context);
    CommandResponse cr;
		try {
			cr = (CommandResponse) conn.sendAction(cmd);
			lines = new DialplanLines(cr.getResult());
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (IllegalStateException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (TimeoutException e) {
			e.printStackTrace();
		}
		String _firstLine = (String) lines.lines.get(0);
		if (_firstLine.startsWith("There is no existence of")) {
			if (_toplevel) {
				throw new ProviderUnavailableException("Missing dialplan context: "+_context);
			} else {
				// silently ignore missing includes, because asterisk does the same
				unnest();
			}
		}
		
	}

	void unnest() {
		lines = (DialplanLines) includeStack.pop();
	}
	
	public String getId() {
		return this.id;
	}
	
	public int getPriority() {
		return priority;
	}
	
	public String getCommand() {
		return command;
	}
	
	public String getArgument() {
		return argument;
	}
		
	/**
	 * If command is a goto statement return the destination context.
	 * If no explicit context is specified we return "our" context.
	 */
	public String getGotoContext() {
		String[] sa = argument.split("\\|");
		if (sa.length==3) {
			return sa[0]; 
		}
		return context;
	}
	
	public String getGotoExtensionId() {
		String[] sa = argument.split("\\|");
		// for sa.length==1 we have only priority
		if (sa.length<=1) { return null; }
		return sa[sa.length-2];
	}
	
	public boolean next() {
		while (lines.next()) {
			Line lp = lines.getLine();
			// DEBUG System.out.println(lp.line);
			if (lp.isComment()) { continue; }
			if (lp.isInclude()) {
				nest(lp.readIncludeContext());
				continue;
			}
			if (lp.isNewExtension()) {
				this.id = lp.readId();
			}
			if (lp.isHint()) {
				// FIXME: do something usefull with hints
				continue;
			}
			if (lp.isNextPriority()) {
				// FIXME: do something usefull with next prios
				continue;
			}
			priority = lp.readPriority();
			command = lp.readCommand().toLowerCase();
			argument = lp.readArgument();
			return true;
		}
		if (includeStack.size()>0) {
			unnest();
			return next();
		}
		return false;
	}
	
	public LocalTarget[] getDialLocalTargets() {
		String[] _targets = argument.split("&");
		ArrayList al = new ArrayList();
		for(int i=0; i<_targets.length; i++) {		
			String s = _targets[i];
			if (!s.toLowerCase().startsWith("local")) {
				continue;
			}
			al.add(new LocalTarget(s));
		}
		LocalTarget[] lt = new LocalTarget[al.size()];
		al.toArray(lt);
		return lt;
	}
	
	/** Parse argument to dial, something like: local/234@context,30,r */
	public static class LocalTarget {
		
		String context;
		String extension;
		
		public LocalTarget(String s) {
			if (s.startsWith("local/")) {
				s = s.substring(6);
				int i1 = s.indexOf('@');
				if (i1>0) {
					extension = s.substring(0,i1);
					int i2 = s.indexOf(',',i1);
					if (i2>0) {
						context = s.substring(i1+1,i2);
					} else {
						i2 = s.indexOf("/",i1);
						if (i2>0) {
							context = s.substring(i1+1,i2);
						} else {
							context = s.substring(i1+1);
						}
					}	
				}
			}
		}
		
		public String getContext() { return context; }
		public String getExtension() { return extension; }
		public boolean isParseError() { return context==null || extension==null; }
		
	}
	
	public static class DialplanLines {
		List lines;
		int index = -1;
		
		public DialplanLines(List _lines) {
			lines = _lines;
		}
		
		public boolean next() {
			index ++;
			return index<lines.size();
		}

		public Line getLine() {
			Line lp = new Line();
			lp.set(lines.get(index).toString());
			return lp;
		}
		
	}
	
	/**
	 * Parsing primitives for a  dialpan line
	 */
	public static class Line {
		
		String originalLine;
		String line;
		
		public void set(String s) {
			line = originalLine = s;
		}
		

		/** skip out any cruft */
		public boolean isComment() {
			if (line==null || line.length()==0 || line.charAt(0) == '[') {
				return true;
			}
			// asterisk 1.2 summary comment
			if (line.charAt(0) == '-') {
				return true;		
			}
			return false;
		}
		
		public boolean isNewExtension() {
			line = line.trim();
			return line.charAt(0) == '\'';
		}
		
		public boolean isInclude() {
			line = line.trim();
			return line.startsWith("Include");
		}
		
		public String readIncludeContext() {
			int i = line.indexOf('\'');
			if (i>0) {
				int j = line.indexOf('\'',i+1);
				return line.substring(i+1,j);	
			}
			outch("include context expected");
			return null;
		}
		
		public String readId() {
			int i = line.indexOf('\'',1);
			if (i>0) {
				String s = line.substring(1,i);
				line = line.substring(i+1).trim();
				if (line.charAt(0)!='=') {
					outch("extension = exeptected");
				}
				// skip =>
				line = line.substring(2).trim();
				return s;
			}
			outch("extension ' expected");
			return null;
		}

		public boolean isHint() {
			return line.startsWith("hint:");
		}
		
		public boolean isNextPriority() {
			return line.startsWith("n");
		}
		
		public void skipPriority() {
			int i = line.indexOf('.');
			if (i>0) {
				line = line.substring(i+1).trim();
			return;
			}
			outch("priority expected");
		}
		
		public int readPriority() {
			int i = line.indexOf('.');
			if (i>0) {
				int res = Integer.parseInt(line.substring(0,i));
				line = line.substring(i+1).trim();
				return res;
			}
			outch("index number expected");
			return 0;
		}
		
		public String readCommand() {
			int i = line.indexOf('(');
			if (i>0) {
				String s = line.substring(0,i);
				line = line.substring(i+1).trim();
				return s;
			}
			outch("command expected");
			return null;
		}
		
		public String readArgument() {
			int i = line.indexOf(')');
			if (i>=0) {
				String s = line.substring(0,i);
				line = line.substring(i+1).trim();
				return s;
			}
			outch("argument expected");
			return null;
		}
		
		void outch(String s) {
			s += ", at: "+
			originalLine.substring(0,originalLine.length()-line.length())+
			"^"+line;
			throw new Exception(s);
		}
		
	}
	
	public static class Exception extends RuntimeException {
		
		private static final long serialVersionUID = 1L;
		
		public Exception(String s) {
			super(s);
		}	
		
	}
	
}
