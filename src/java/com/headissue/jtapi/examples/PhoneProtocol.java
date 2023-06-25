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

import java.text.DateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import javax.telephony.*;
import javax.telephony.callcontrol.CallControlCall;

import com.headissue.jtapi.test.EventMonitor;

/**
 * Example application that produces something like call detail
 * records out of the JTAPI events. An example is an application
 * that wants to list the incoming calls and the talk duration.
 * 
 * @author jw
 */
public class PhoneProtocol implements TerminalConnectionListener {

	Provider provider;
	Map call2logEntry = new HashMap();

	public static final void main(String args[]) throws Exception {  	 
    if (args.length<1) {
    	System.err.println("java com.headissue.jtapi.examples.PhoneProtocol <Provider; Provider Args>");
    	System.err.println("");
    	return;
    }
    JtapiPeer _peer = JtapiPeerFactory.getJtapiPeer(null);
    Provider _provider = _peer.getProvider(args[0]);
    PhoneProtocol pp = new PhoneProtocol();
    pp.init(_provider);
    EventMonitor mon = new EventMonitor(_provider);
    mon.listenOnEverything();
    
    // block until a key is pressed
    System.in.read();
    System.exit(0);
  }
	
	public void init(Provider _provider) throws ResourceUnavailableException, MethodNotSupportedException {
		provider = _provider;
    System.out.print("Attaching to terminals: ");
    Terminal ta[] = _provider.getTerminals();
    for (int i=0; i<ta.length; i++) {
    	ta[i].addCallListener(this);
    	System.out.print(ta[i].getName()+" ");
    }
    System.out.println("done");
    System.out.print("Attaching to addresses: ");
    Address addr[] = _provider.getAddresses();
    for (int i=0; i<addr.length; i++) {
    	addr[i].addCallListener(this);
    	System.out.print(addr[i].getName()+" ");
    }
    System.out.println("done");
	}
	
	public LogEntry dispatch(CallEvent cv) {
		Call c = cv.getCall();
		LogEntry mc = (LogEntry) call2logEntry.get(c);
		if (mc==null) {
			synchronized (call2logEntry) {
				mc = (LogEntry) call2logEntry.get(c);
				if (mc==null) {
					mc = new LogEntry(c);
					call2logEntry.put(c,mc);
				}
			}
		}
		return mc;
	}

	public void terminalConnectionActive(TerminalConnectionEvent ev) {
		System.err.println("tcActive");
		dispatch(ev).terminalActive(ev);
	}
	
	public void connectionCreated(ConnectionEvent ev) {
		System.err.println("created");
		dispatch(ev).created(ev);
	}
	
	public void connectionInProgress(ConnectionEvent ev) {
		System.err.println("progress");
		dispatch(ev).progress(ev);
	}
	
	public void connectionAlerting(ConnectionEvent ev) {
		System.err.println("alert");
		dispatch(ev).alert(ev);
	}
	
	public void callInvalid(CallEvent ev) {
	}
	
	public void connectionConnected(ConnectionEvent ev) {
		dispatch(ev).connected(ev);
	}

	public void terminalConnectionCreated(TerminalConnectionEvent ev) {
	}

	public void terminalConnectionDropped(TerminalConnectionEvent ev) {
	}

	public void terminalConnectionPassive(TerminalConnectionEvent ev) {
	}

	public void terminalConnectionRinging(TerminalConnectionEvent ev) {
	}

	public void terminalConnectionUnknown(TerminalConnectionEvent ev) {
	}

	public void connectionDisconnected(ConnectionEvent ev) {
		Call c = ev.getCall();
		if (c.getState() == Call.INVALID) {
			// everyone hung up if the call is invalid
			// we cannot take the callInvalid events, because
			// the call is no more associated to an address and terminal
			// and therefore our listener does not get any call events 
			// hmm, strange JTAPI definition ;jw
			System.err.println("drop");
			dispatch(ev).hangup();
		}
	}

	public void connectionFailed(ConnectionEvent ev) {
	}

	public void connectionUnknown(ConnectionEvent ev) {
	}

	public void callActive(CallEvent ev) {
	}

	public void callEventTransmissionEnded(CallEvent ev) {
	}

	public void singleCallMetaProgressStarted(MetaEvent ev) {
	}

	public void singleCallMetaProgressEnded(MetaEvent ev) {
	}

	public void singleCallMetaSnapshotStarted(MetaEvent ev) {
	}

	public void singleCallMetaSnapshotEnded(MetaEvent ev) {
	}

	public void multiCallMetaMergeStarted(MetaEvent ev) {
	}

	public void multiCallMetaMergeEnded(MetaEvent ev) {
	}

	public void multiCallMetaTransferStarted(MetaEvent ev) {
	}

	public void multiCallMetaTransferEnded(MetaEvent ev) {
	}
    	
	public static class LogEntry {
		
		CallControlCall call;
		
		boolean directionSet;
		boolean incomingCall;
		String remoteAddress = null;
		String localAddress = null;
		String connectedTerminal = null;
		Connection connectedConnection1 = null;
		
		long createdStamp = System.currentTimeMillis();
		long alertStamp = 0;
		long answeredStamp = 0;
		long hangupStamp = 0;
		
		LogEntry(Call _call) {
			if (!(_call instanceof CallControlCall)) {
				throw new RuntimeException("This only works for providers that implement the CallControlCall");
			}
			call = (CallControlCall) _call;
		}
		
		/**
		 * Depending on the events we get, we might already have filled
		 * in some data.
		 */
		void checkFill() {
			// check and set call direction
			if (!directionSet) {
				// if we know the originating terminal,this call is from us
				if (call.getCallingTerminal()!=null) {
					incomingCall = false;
				}
				directionSet = true;
			}
			// set addresses
			// assert(directionSet);
			if (call.getCallingAddress()!=null) {
				if (incomingCall) {
					if (remoteAddress == null) remoteAddress = call.getCallingAddress().getName();
				} else {
					if (localAddress == null) localAddress = call.getCallingAddress().getName();					
				}
			}
			if (call.getCalledAddress()!=null) {
				if (incomingCall) {
					if (localAddress == null) localAddress = call.getCalledAddress().getName();
				} else {
					if (remoteAddress == null) remoteAddress = call.getCalledAddress().getName();
				}
			}
		}

		void terminalActive(TerminalConnectionEvent ev) {
			TerminalConnection tc = ev.getTerminalConnection();
			if (connectedTerminal==null) {
				connectedTerminal = tc.getTerminal().getName();
			}
		}
		
		void created(ConnectionEvent event) {
			// created timestamp is already filled in in constructor
			checkFill();
		}

		void progress(ConnectionEvent event) {
			checkFill();
		}

		/** Record that the call alerted */
		void alert(ConnectionEvent event) {
			checkFill();
			// maybe we have more than one alert, if a call gets transfered
			if (alertStamp == 0) {
				alertStamp = System.currentTimeMillis();
			}
		}
		
		/** Record that the call is established */
		void connected(ConnectionEvent event) {
			checkFill();
			// one leg of the call is initially connected
			// so we count if we have two connections
			// connected
			if (connectedConnection1==null) {
				connectedConnection1 = event.getConnection();
			} else {
				if (connectedConnection1!=event.getConnection()) {
					if (answeredStamp==0) {
						answeredStamp = System.currentTimeMillis();
					}
				}
			}
		}
		
		/** All connections finished */
		void hangup() {
			if (answeredStamp>0 && hangupStamp==0) {
				hangupStamp = System.currentTimeMillis();
			}
			// don't hold resources
			connectedConnection1 = null;
			System.out.println(getLogInformation());
		}
				
		/**
		 * Primitive for leading 0 or spaces. Extend string to the length of _pattern by
		 * adding the first charactes of _pattern to it. E.g. a fill(num,"00") produces a 
		 * two digit number. Only works for s.length<=_pattern.length.
		 */
		static String fill(String s, String _pattern) {
			int i = _pattern.length()-s.length();
			if (i>0) {
				return _pattern.substring(0,i)+s;
			}
			return s;
		}
		
		
		public String clockStamp(long _stamp) {
			Calendar c = Calendar.getInstance();
			c.setTimeInMillis(_stamp);
			int _hour = c.get(Calendar.HOUR_OF_DAY);
			int _min = c.get(Calendar.MINUTE);
			int _sec = c.get(Calendar.SECOND);
			return fill(_hour+"","00")+":"+fill(_min+"","00")+":"+fill(_sec+"","00")+" ";
		}
		
		/**
		 * Print a timestamp to the output.
		 */
		public String timeStamp(long _stamp) {
			Calendar c = Calendar.getInstance();
			c.setTimeInMillis(_stamp);
			int _month = c.get(Calendar.MONTH);
			int _day = c.get(Calendar.DAY_OF_MONTH);
			int _hour = c.get(Calendar.HOUR_OF_DAY);
			int _min = c.get(Calendar.MINUTE);
			int _sec = c.get(Calendar.SECOND);
			String[] ma = new String[]{"Jan","Feb","Mar","Apr","May","Jun","Jul","Aug","Sep","Oct","Nov","Dec"};
			return ma[_month]+" "+fill(_day+"","  ")+" "+fill(_hour+"","00")+":"+fill(_min+"","00")+":"+fill(_sec+"","00")+" ";
		}
		
		public String getLogInformation() {
			return "CDR: timeStamp="+ timeStamp(createdStamp) +", "+
				"direction="+ (incomingCall ? "I" : "O") +", "+
				"local="+ localAddress +", "+
				"remote="+ remoteAddress +", "+
				"terminal="+ connectedTerminal +", "+
				"answered="+ (answeredStamp>0 ? clockStamp(answeredStamp) : "<n/a>") +", "+
				"talkTime(sec)="+ (hangupStamp>0 ? ((hangupStamp - answeredStamp) / 1000)+"" : "<n/a>");
		}
		
	}

}
	

