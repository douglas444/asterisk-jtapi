/*
 *  (C) Copyright 2005-2006 headissue GmbH; Jens Wilke
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

package com.headissue.jtapi.test;

import javax.telephony.*;
import com.headissue.jtapi.test.EventMonitor;

public class MakeCall {
	
	Terminal terminal;
	Provider provider;

	public static final void main(String args[]) throws Exception {  	 
    if (args.length<2) {
    	System.err.println("java ...MakeCall <Provider; Provider Args> <Terminal> <Addr> <Dest>");
    	System.err.println("");
    	System.err.println("<JTapi Implementation>");
    	return;
    }
	
    JtapiPeer _peer = JtapiPeerFactory.getJtapiPeer(null);
    Provider _provider = _peer.getProvider(args[0]);
    
    EventMonitor em = new EventMonitor(_provider);
    em.listenOnEverything();
    
    Terminal _terminal = _provider.getTerminal(args[1]);
    Address _addr = _terminal.getAddresses()[0];
    Call _call = _provider.createCall();
    _call.connect(_terminal, _addr, args[3]);
    // block until a key is pressed
    // System.in.read();
  }
    	
}
