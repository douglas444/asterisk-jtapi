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

package com.headissue.jtapi.examples;

import javax.telephony.*;

/**
 * Pretty print the dialplan 
 * 
 * @author jw
 */
public class ShowDialplan {

public static final void main(String args[]) throws Exception {  	 
    if (args.length<1) {
    	System.err.println("java com.headissue.asterisktpi.samples.ShowDialplan <Provider; Provider Args>");
    	System.err.println("");
    	System.err.println("<JTapi Implementation>");
    	return;
    }
	
    JtapiPeer _peer = JtapiPeerFactory.getJtapiPeer(null);
    String[] sa = _peer.getServices();
    for (int i=0; i<sa.length; i++) {
    	System.out.println(sa[i]);
    }
    Provider _provider = _peer.getProvider(args[0]);
    Terminal[] ta = _provider.getTerminals();
    for (int i=0; i<ta.length; i++) {
    	System.out.print(ta[i].getName()+" <= ");
    	Address[] aa = ta[i].getAddresses();
    	for (int j=0; j<aa.length; j++) {
    		if (j>0) { System.out.print(", "); }
    		System.out.print(aa[j].getName());
    	}
    	System.out.println();
    }
  }
	
}