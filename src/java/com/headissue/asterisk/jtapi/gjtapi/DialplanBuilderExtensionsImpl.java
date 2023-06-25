/*
 *  (C) Copyright 2005 headissue GmbH; Jens Wilke
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class DialplanBuilderExtensionsImpl extends DialplanBuilder {
	
	Map terminals = new HashMap();
	Map addresses = new HashMap();
	Map terminal2context = new HashMap();

  /**
   * Retrieve the dialplan of the terminalContext from asterisk and build up
   * a Map of the terminals we found.
   * If the terminalContext is identical to the incomingContext then we
   * add the terminal name/number as address to this terminal
   */
	void parseTerminalContext() {
		Iterator i = config.terminalContext.initialIterator();
		while(i.hasNext()) {
			String _currentTerminalContext = (String) i.next();
			DialplanContextParser _parser = 
				new DialplanContextParser(managerConnection, _currentTerminalContext);
	  	while (_parser.next()) {
	  		String ext = _parser.getId();
	  		if (terminals.get(ext)==null) {
	  			terminal2context.put(ext,_currentTerminalContext);
	  			ArrayList _addresses = new ArrayList();
	  			terminals.put(ext, _addresses);
	  			// the terminal name is also an address
	  			if (config.incomingContext.initialContains(_currentTerminalContext)) {
	  				_addresses.add(ext);
	      		ArrayList _terminals = (ArrayList) addresses.get(ext); 
	      		if (_terminals==null) {
	      			_terminals = new ArrayList();
	      			addresses.put(ext, _terminals);
	      		}
	      		_terminals.add(ext);
	  			}
	  		}
	  	}
		}   
	}
	
  /** 
   * Retrieve the dialplan of the incomingContext from asterisk and build up
   * a Map of the addresses found.
   */
	void parseIncomingContext() {
		Iterator i = config.incomingContext.initialIterator();
		while (i.hasNext()) {
			String _currentIncomingContext = (String) i.next();
			DialplanContextParser _parser = 
				new DialplanContextParser(managerConnection, _currentIncomingContext);
			while (_parser.next()) {
				String _extensionId = _parser.getId();
				ArrayList _terminals = (ArrayList) addresses.get(_extensionId); 
				if (_terminals==null) {
					_terminals = new ArrayList();
					addresses.put(_extensionId, _terminals);
				}
	  		// handle goto: detect and handle an alias or multiple addresses
	  		if (_parser.getPriority()==1 && 
	  				_parser.getCommand().equals("goto") &&
	  				config.terminalContext.initialContains(_parser.getGotoContext())) {
	  			ArrayList _terminalAddresses = (ArrayList) terminals.get(_parser.getGotoExtensionId());
	  			// this extension is an address for the terminal
	  			if (_terminalAddresses!=null) {
	  				_terminalAddresses.add(_extensionId);
	  			}
	  			// remove this extension from the terminal list
	  			// because this is an alias
	  			if (config.terminalContext.initialContains(_currentIncomingContext)){
	  				terminals.remove(_extensionId);
	  			}
	  		}
	  		if (_parser.getPriority()==1 &&
	  				_parser.getCommand().equals("dial") &&
	  				_parser.getArgument().toLowerCase().startsWith(("local"))) {
	  			DialplanContextParser.LocalTarget lta[] = _parser.getDialLocalTargets();
	  			for (int j=0; j<lta.length; j++) {
	  				DialplanContextParser.LocalTarget lt = lta[j];
						if (config.terminalContext.initialContains(lt.context)) {
							if (lt.extension!=null) {
								ArrayList l = (ArrayList) terminals.get(lt.extension);
								if (l!=null) {
									l.add(_extensionId);
								}   	 						
	  					}
	      			if (config.terminalContext.initialContains(_currentIncomingContext)){
	      				terminals.remove(_extensionId);
	      			}
	  				}
	  			}
	  		}
	  	}
		}
	}

	public void build() {
		parseTerminalContext();
		parseIncomingContext();
	}

	public Map getAddress2Terminals() {
		return addresses;
	}
	
	public Map getTerminal2Context() {
		return terminal2context;
	}

	public Map getTerminal2Addresses() {
		return terminals;
	}
	
}
