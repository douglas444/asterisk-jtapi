/*
 *  (C) Copyright headissue GmbH 2005; Jens Wilke
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

import javax.telephony.JtapiPeer;
import javax.telephony.JtapiPeerFactory;

/**
 * Try to instanciate the DefaultJtapiPeer directly without the JTAPI
 * method. This is a simple test class because the method JtapiPeerFactory.getJtapiPeer(null)
 * does not swallow the exceptions thrown be the peer class and just throws
 * its own exception which does not allow any further diagnostic.
 * @author jw
 */
public class CheckDefaultPeer {
	
	public static void main(String[] args) throws Exception {
		JtapiPeer _peer = null;
		try {
			_peer = JtapiPeerFactory.getJtapiPeer(null);
		} catch(Exception ex) {
			System.err.println("JtapiPeerFactory was not able to instanciate the default JTAPI peer.");
			System.err.println("Got exception: "+ex);
			System.err.println("Trying directly: ");
			Class _peerClass = Class.forName("DefaultJtapiPeer");
			_peer = (JtapiPeer) _peerClass.newInstance();
		}
		System.out.println("Got JtapiPeer: "+_peer.getClass());
		// bb: _peer -> services ." Supported services:\n" | .
		String[] sa = _peer.getServices();
		System.out.println("Supported services:");
		for (int i=0; i<sa.length; i++) {
			System.out.println(sa[i]);
		}
	}
	
}