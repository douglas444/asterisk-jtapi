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

package com.headissue.asterisk.jtapi.gjtapi;

import net.sourceforge.gjtapi.CallId;

public class AsteriskCall implements CallId {
	boolean originate = false;
	String uniqueId;
	String terminal;
	String address;
	String callerId;
	
	/**
	 * Asterisk channel of this call
	 */
	String channel;

	/** 
	 * A call object existist for every uniqueId within Asterisk,
	 * if Asterisk assigns a new uniqueId to the identical call,
	 * e.g. when originating a call realCall points to the original
	 * call object (instead it self) so we send the events for the call
	 * that is known by GJTAPI
	 **/
	AsteriskCall realCall = this;
	
	/** Reference to the other call in asterisk that we are connected to. */
	AsteriskCall leg2 = null;
	
	String calledAddress;
	
	/**
	 * The very first caller id that is set is stored here. Used for
	 * outgoing calls to have a reasonable address. Needs work.
	 */
  String firstCallerId;
}
