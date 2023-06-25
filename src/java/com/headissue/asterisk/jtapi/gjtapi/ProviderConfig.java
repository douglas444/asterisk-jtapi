/*
 *  (C) Copyright 2005-2006 headissue GmbH; Jens Wilke
 *  (C) Copyright 2006 Chirasys s.r.o; Martin Sladecek
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
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.telephony.ProviderUnavailableException;

import net.sf.asterisk.manager.AuthenticationFailedException;
import net.sf.asterisk.manager.ManagerConnection;
import net.sf.asterisk.manager.ManagerConnectionFactory;
import net.sf.asterisk.manager.TimeoutException;
import net.sf.asterisk.manager.action.CommandAction;
import net.sf.asterisk.manager.response.CommandResponse;
import net.sf.asterisk.util.Log;
import net.sf.asterisk.util.LogFactory;

import com.headissue.asterisk.jtapi.AsteriskJtapiProvider;


public class ProviderConfig {
	
	public static class ContextSet {
		private HashSet set = new HashSet();
		private HashSet initial = new HashSet();
		
		private void addInitial(String _context) {
			add(_context);
			initial.add(_context);
		}
				
		private void add(String _context) {
			set.add(_context);
		}
		
		public boolean contains(String s){
			return set.contains(s);
		}
		
		public Iterator initialIterator(){
			return initial.iterator();
		}
		
		public boolean initialContains(String s){
			return initial.contains(s);
		}
		
		public String toString() {
			String _result = "[";
			for (Iterator i = set.iterator();i.hasNext();){
				_result += " "+i.next()+",";
			}
			return _result+"]";
			
		}
		
	}
	
  Map configuration;
  private ManagerConnection managerConnection;
  public ContextSet incomingContext;
  public ContextSet terminalContext;
  public String outgoingContext;
  int debug;
  Log logger = LogFactory.getLog(AsteriskJtapiProvider.class);

  public ProviderConfig(Map _configuration) throws TimeoutException,
  		AuthenticationFailedException, IOException {
  	configuration = _configuration;
  	debug = getInt("Debug", 0);
    incomingContext = getContextSet("IncomingContext");
    // DEBUG System.out.println(incomingContext);
    // DEBUG System.out.println(incomingContext.initial);
    terminalContext = getContextSet("TerminalContext");
    outgoingContext = getString("OutgoingContext");
  }
  
  /**
   * Build a set of all contexts that include _context (transitive!).
   * @param _context a context we search for
   * @param _lines A dialplan context returned by Asterisk
   */
  private Set getIncludingContexts(String _context, List _lines) {
  	Set _set = new HashSet();
  	String _currentContext = null;
  	for (Iterator i = _lines.iterator();i.hasNext();){
  		String _line = (String)i.next();
  		if (_line.startsWith("[ Context ")){
  			_currentContext = _line.substring(_line.indexOf('\'')+1
  					,_line.indexOf('\'',_line.indexOf('\'')+1));
  			continue;
  		}
  		if (_line.matches(".*\\QInclude =>\\E.*\\Q'"+_context+"'\\E.*")){
  			_set.add(_currentContext);
  			// also add all conetexts that include the _currentContext, which contains
  			// the context we search for
  			_set.addAll(getIncludingContexts(_currentContext,_lines));
  		}
  	}
  	return _set;
  }
  
  private ManagerConnection getManagerConnection() throws TimeoutException,
  		AuthenticationFailedException, IOException {
    if (managerConnection == null){
    	ManagerConnectionFactory mcf = new ManagerConnectionFactory();
    	mcf.setHostname(getString("Server"));
    	mcf.setPort(getInt("Port"));
    	mcf.setUsername(getString("Login"));
    	mcf.setPassword(getString("Password"));
			try {
				managerConnection = mcf.getManagerConnection();
			} catch (IOException e) {
				e.printStackTrace();
			}
    }
    if (!managerConnection.isConnected()){
			managerConnection.login();
    }
    return managerConnection;
  }
  
  public ContextSet getContextSet(String _contextList) throws TimeoutException,
  		AuthenticationFailedException,IOException{
  	ContextSet cs = new ContextSet();
  	String[] _contexts = getString(_contextList).split(",");
  	for (int i=0; i<_contexts.length; i++) {
  		cs.addInitial(_contexts[i]);
  	}
    List result = null;
    try {
    	CommandAction ca = new CommandAction("show dialplan");
			CommandResponse cr = (CommandResponse) getManagerConnection().sendAction(ca);
			result = cr.getResult();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (TimeoutException e) {
			e.printStackTrace();
		}
		for (int i=0; i<_contexts.length; i++) {
			Set ct = getIncludingContexts(_contexts[i], result);
			for (Iterator j=ct.iterator(); j.hasNext();){
				cs.add((String)j.next());
			}
    } 
  	return cs;
  }
  
  /**
   * Throw an exception if parameter parsing fails
   * @param _exceptionText
   * @throws ProviderUnavailableException with _exceptionText and CAUSE_INVALID_ARGUMENT
   */
  void throwException(String _exceptionText) throws ProviderUnavailableException {
		int _cause = ProviderUnavailableException.CAUSE_INVALID_ARGUMENT;
  	throw new ProviderUnavailableException(_cause, _exceptionText);
  }
  
  /**
   * Gets a string from the configuration properties and trows exception if not present.
   * @throws ProviderUnavailableException
   */
  public String getString(String key) throws ProviderUnavailableException  {
  	String s = (String) configuration.get(key);
  	if (s==null) {
  		throwException("Missing configuration property: "+key);
  	}
  	return s;
  }
  
  /**
   * Get configuration string with default value
   * @param _default default value if no property exists
   */
  public String getString(String key, String _default) throws ProviderUnavailableException {
  	String s = (String) configuration.get(key);
  	if (s==null) {
  		return _default;
  	}
  	return s;
  }
  
  /**
   * Get configuration value and convert it to an int
   */
  public int getInt(String key) throws ProviderUnavailableException {
  	String s = (String) configuration.get(key);
  	if (s==null) {
  		throwException("Missing configuartion property: "+key);
  	}
  	int i = 0;
  	try {
  		i = Integer.parseInt(s);
  	} catch (NumberFormatException e) {
  		throwException("Integer property expected: "+key+"="+s);
  	}
  	return i;
  }
  
  /**
   * Get configuration value and convert it to an int. If no value is present, use
   * default value
   */
  public int getInt(String key, int _default) throws ProviderUnavailableException {
  	String s = (String) configuration.get(key);
  	if (s==null) {
  		return _default;
  	}
  	int i = 0;
  	try {
  		i = Integer.parseInt(s);
  	} catch (NumberFormatException e) {
  		throwException("Integer property expected: "+key+"="+s);
  	}
  	return i;
  }
	
}
