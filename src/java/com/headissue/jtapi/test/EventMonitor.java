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

import java.util.Calendar;
import java.util.HashMap;
import java.io.PrintStream;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

import javax.telephony.*;
import javax.telephony.callcontrol.CallControlCall;

/**
 * Register for events on all terminals and addresses and print out
 * the signalled event plus current status.
 * 
 * @author jw
 */
public class EventMonitor implements TerminalConnectionListener {
	
	public static final void main(String args[]) throws Exception {  	 
		if (args.length<1) {
			System.err.println("java com.headissue.jtapi.test.EventMonitor <Provider; Provider Args> [<Terminal>]");
			System.err.println("");
			System.err.println("<JTapi Implementation>");
			return;
		}
		JtapiPeer _peer = JtapiPeerFactory.getJtapiPeer(null);
		String[] sa = _peer.getServices();
		System.out.println("Services found: ");
    for (int i=0; i<sa.length; i++) {
    	System.out.println(sa[i]);
    }
    EventMonitor mon = new EventMonitor(null, args[0]);
    mon.showDialplan();
    mon.listenOnEverything();
    System.out.println("Waiting for events");
    System.in.read();
    System.exit(0);
  }
  	
	HashMap call2id = new HashMap();
	int callCnt = 0;
	PrintStream output = System.out;
	PrintStream debug = System.out;
	Provider provider;
	
	public EventMonitor(Provider p) {
		provider = p;
	}
	
	public EventMonitor(String _jtapiPeer, String _providerArguments) throws JtapiPeerUnavailableException {
		JtapiPeer _peer = JtapiPeerFactory.getJtapiPeer(_jtapiPeer);
		provider = _peer.getProvider(_providerArguments);
	}
	
  public void showDialplan() {
    output.println("Dialplan:");
    output.println("( <terminal> <= <address1> [ , <address2>,...] ):");
    try {
	    Terminal[] ta = provider.getTerminals();
	    for (int i=0; i<ta.length; i++) {
	    	output.print(ta[i].getName()+" <= ");
	    	Address[] aa = null;
	    	try {
	    		// in case of no assiciated address we get an exception
	    		// (tzzz, bad api design! ;jw)
	    		aa = ta[i].getAddresses();
	    	} catch (Exception e) {
	    		output.println(e+"");
	    		continue;
	    	}
	    	for (int j=0; j<aa.length; j++) {
	    		if (j>0) { System.out.print(", "); }
	    		output.print(aa[j].getName());
	    	}
	    	output.println();
	    }
		} catch (ResourceUnavailableException e) {
			debug.println("no terminals available");
		}
  }
	
	public void listenOnEverything() {
    debug.print("monitor: Attaching to terminals: ");
		try {
			Terminal[] ta = provider.getTerminals();
	    for (int i=0; i<ta.length; i++) {
					ta[i].addCallListener(this);
					System.out.print(ta[i].getName()+" ");
	    }
	    debug.println("done");
		} catch (ResourceUnavailableException e) {
			debug.println("no terminals available");
		} catch (MethodNotSupportedException e) {
			debug.println("Method addCallListener on terminal is not supported!");
		}

    debug.print("monitor: Attaching to addresses: ");
		try {
	    Address addr[] = provider.getAddresses();
	    for (int i=0; i<addr.length; i++) {
	    	addr[i].addCallListener(this);
	    	System.out.print(addr[i].getName()+" ");
	    }
	    debug.println("done");
		} catch (ResourceUnavailableException e) {
			debug.println("no terminals available");
		} catch (MethodNotSupportedException e) {
    	debug.println("Method addCallListener on address is not supported!");
		}

	}
	
	/**
	 * Returns an implemented interface of the objects' class
	 * which is of type _interfaceType. This is used to print
	 * usefull debug information e.g. for the type of event we received.
	 */
	public Class filterInterface(Class _interfaceType, Class _objClass) {
		Class ca[] = _objClass.getInterfaces();
		for (int i=0; i<ca.length; i++) {
			if (_interfaceType.isAssignableFrom(ca[i])) {
				return ca[i];
			}
		}
		Class sc = _objClass.getSuperclass();
		if (sc!=null) {
			return filterInterface(_interfaceType, sc);
		}
		return null;
	}
	
	public String shortClassName(Class c) {
		String s = c.getName();
		int i = s.lastIndexOf('.');
		if (i>0) {
			return s.substring(i+1);
		}
		return s;
	}
	
	/**
	 * Return the call id. The call id is genereated by the EventMonitor for 
	 * each unique call object.
	 */
	synchronized int calcCallId(Call _call) {
		Integer __callNo = (Integer) call2id.get(_call);
		if (__callNo==null) {
			int _callNo = callCnt++;
			call2id.put(_call, new Integer(_callNo));
			return _callNo;
		} else {
			return __callNo.intValue();
		}
	}
	
	/**
	 * Return the connection number within the call. The format is:
	 * &lt;callId>/&lt;connectionNo>
	 */
	String calcConNo(Connection c) {
		Connection ca[] = c.getCall().getConnections();
		int idx = -1;
		while (ca!=null && (idx+1)<ca.length && ca[++idx]!=c) { }
		return calcCallId(c.getCall())+"/"+idx;
	}
	
	/**
	 * Return a unique identifier for this TerminalConnection. The format
	 * is: &lt;callId>/&lt;connectionNo>/&lt;terminalConnectionNo>.
	 */
	String calcTCId(TerminalConnection tc) {
		TerminalConnection ta[] = tc.getConnection().getTerminalConnections();
		int idx = -1;
		while (ta!=null && (idx+1)<ta.length && ta[++idx]!=tc) { }
		return 
			calcConNo(tc.getConnection())+
			"/"+idx;
	}
	
	/**
	 * Convert a defined constant to a string. This is used by example for the
	 * call states which are defined as Call.ACTIVE, Call.IDLE, Call.INVALID. We
	 * use the reflection API to find out the name of a constant. This is done by
	 * scanning fields that are declared static and are all upper case. 
	 * 
	 * @param c the class where the constant is defined
	 * @param _prefix a name prefix that is used if no constant is found
	 * @param _signal the signal number/constant that is defined by the class
	 * @return a string representation of the signal number
	 */
	public static String constant2Name(Class c, String _prefix, int _signal) {
		Field fa[] = c.getFields();
		for (int i=0; i<fa.length; i++) {
			Field f = fa[i];
			if ((f.getModifiers()&Modifier.STATIC)>0 &&
					f.getType()==Integer.TYPE) {
				String s = f.getName().toUpperCase();
				if (f.getName().equals(s)) {
					try {
						int x = f.getInt(c);
						// DEBUG System.out.println(s+"="+x);
						if (x==_signal) {
							return f.getName();
						}
					} catch (Exception ex) {
					}
				}
			}
		}
		return _prefix+_signal;
	}
	
	/**
	 * Return a string representation of the call states defined by the call class.
	 */
	public static String callState2Name(int _state) {
		return constant2Name(Call.class, "CALL_STATE", _state);
		/*-
		switch(_state) {
		case Call.ACTIVE: 
			return "ACTIVE";
		case Call.IDLE:
			return "IDLE";
		case Call.INVALID:
			return "INVALID";
		default:	
			return _state+"";
		}
		-*/
	}

	/**
	 * Return a string representation of the state defined by the TerminalConnection class.
	 */
	public static String terminalConnectionState2Name(int _state) {
		return constant2Name(TerminalConnection.class, "TERMINAL_CONNECTION_STATE", _state);
	}
		
	/**
	 * Return a string representation of the state defined by the Connection class.
	 */
	public static String connectionState2Name(int _state) {
		return constant2Name(Connection.class, "CONNECTION_STATE", _state);
		/*- straight-forward impl
		switch(_state) {
		case Connection.ALERTING:
			return "ALERTING";
		case Connection.CONNECTED:
			return "CONNECTED";
		case Connection.DISCONNECTED:
			return "DISCONNECTED";
		case Connection.FAILED:
			return "FAILED";
		case Connection.IDLE:
			return "IDLE";
		case Connection.INPROGRESS:
			return "INPROGRESS";
		case Connection.UNKNOWN:
			return "UNKNOWN";
		default:
			return _state+"";
		}
		-*/
	}
	
	/**
	 * Return a string representation of the causes defined by the javax.telephony.Event class.
	 */
	public static String cause2Name(int _cause)  {
		return constant2Name(javax.telephony.Event.class, "CAUSE", _cause);
	}
	
	/**
	 * Print all we know about the call. This is the state the associated connections and 
	 * terminalconnections plus their information fields.
	 */
	public void printCallInformation(Call c) {
		int _state = c.getState();
		Connection[] ca = c.getConnections();
		if (ca==null) {
			ca = new Connection[0];
		}
		String _callClass = 
			shortClassName(filterInterface(Call.class, c.getClass()));
		String txt = "";
		if (c instanceof CallControlCall) {
			CallControlCall ccc = (CallControlCall) c;
			txt += ",calledAddress=\""+ (ccc.getCalledAddress()!=null ? ccc.getCalledAddress().getName() : "null")+"\"";
			txt += ",callingAddress=\""+(ccc.getCallingAddress()!=null ? ccc.getCallingAddress().getName() : "null")+"\"";
		}
		output.println(" "+_callClass+"#"+calcCallId(c)+
				"(state="+callState2Name(_state)+
				",connectionCnt="+ca.length+txt+")");
		
		// print connections
		for (int i=0; i<ca.length; i++) {
			Connection con = ca[i];
			_state = con.getState();
			String _conClass = 
				shortClassName(filterInterface(Connection.class, con.getClass()));
			Address a = con.getAddress();
			String _addrClass = 
				shortClassName(filterInterface(Address.class, a.getClass()));
			TerminalConnection tca[] = con.getTerminalConnections();
			output.println("  "+_conClass+"#"+calcConNo(con) +"("+
					"state="+connectionState2Name(_state)+
					",address="+_addrClass+"(name=\""+a.getName()+"\")"+
					",terminalConnectionCnt="+(tca!=null ? tca.length+"" : "null")+")");
			
			// print terminal connections, if any
			for (int j=0; tca!=null && j<tca.length; j++) {
				TerminalConnection tc = tca[j];
				String _tcClass = 
					 shortClassName(filterInterface(TerminalConnection.class, tc.getClass()));
				output.println("   "+_tcClass+"#"+calcTCId(tc)+"("+
						"state="+terminalConnectionState2Name(tc.getState())+
						",terminal=\""+tc.getTerminal().getName()+"\")");
			}
		}
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
	
	/**
	 * Print a timestamp to the output.
	 */
	public void timeStamp() {
		Calendar c = Calendar.getInstance();
		int _month = c.get(Calendar.MONTH);
		int _day = c.get(Calendar.DAY_OF_MONTH);
		int _hour = c.get(Calendar.HOUR_OF_DAY);
		int _min = c.get(Calendar.MINUTE);
		int _sec = c.get(Calendar.SECOND);
		String[] ma = new String[]{"Jan","Feb","Mar","Apr","May","Jun","Jul","Aug","Sep","Oct","Nov","Dec"};
		output.print(ma[_month]+" "+fill(_day+"","  ")+" "+fill(_hour+"","00")+":"+fill(_min+"","00")+":"+fill(_sec+"","00")+" ");
	}
	
	/**
	 * Receive an event and print its information
	 * 
	 * @param _name the event name
	 * @param ev the event object
	 */
	public synchronized void receiveAllEvents(String _name, Event ev) {
		Call c = null;
		timeStamp();
		output.print(">>"+_name+"<< ");
		if (ev instanceof TerminalConnectionEvent) {
			TerminalConnectionEvent te = (TerminalConnectionEvent) ev;
			c = te.getCall();
			TerminalConnection tc = te.getTerminalConnection();
			Class cl = filterInterface(TerminalConnectionEvent.class, ev.getClass());
			output.println(shortClassName(cl)+"#"+calcTCId(tc)+
					"(id="+te.getID()+
					",cause="+cause2Name(te.getCause())+")");
		} else if (ev instanceof ConnectionEvent) {
			ConnectionEvent ce = (ConnectionEvent) ev;
			c = ce.getCall();
			Class cl = filterInterface(ConnectionEvent.class, ev.getClass());
			output.println(shortClassName(cl)+"#"+calcConNo(ce.getConnection())+
					"(id="+ce.getID()+
					",cause="+cause2Name(ce.getCause())+")");		
		} else if (ev instanceof CallEvent) {
			CallEvent ce = (CallEvent) ev;
			c = ce.getCall();
			Class cl = filterInterface(CallEvent.class, ev.getClass());
			output.println(shortClassName(cl)+"#"+calcCallId(c)+
					"(id="+ce.getID()+
					",cause="+cause2Name(ce.getCause())+")");		
		}
		if (c!=null) {
			printCallInformation(c);
		} else {
			output.println("Unknown event received: "+ev.toString());
		}
	}
	
	
	public void terminalConnectionActive(TerminalConnectionEvent arg0) {
		receiveAllEvents("terminalConnectionActive", arg0);
		
	}
	
	public void terminalConnectionCreated(TerminalConnectionEvent arg0) {
		receiveAllEvents("terminalConnectionCreated", arg0);
		
	}
	
	public void terminalConnectionDropped(TerminalConnectionEvent arg0) {
		receiveAllEvents("terminalConnectionDropped", arg0);
		
	}
	
	public void terminalConnectionPassive(TerminalConnectionEvent arg0) {
		receiveAllEvents("terminalConnectionPassive", arg0);
		
	}
	
	public void terminalConnectionRinging(TerminalConnectionEvent arg0) {
		receiveAllEvents("terminalConnectionRinging", arg0);
		
	}
	
	public void terminalConnectionUnknown(TerminalConnectionEvent arg0) {
		receiveAllEvents("terminalConnectionUnknown", arg0);
		
	}
	
	public void connectionAlerting(ConnectionEvent arg0) {
		receiveAllEvents("connectionAlerting", arg0);
		
	}
	
	public void connectionConnected(ConnectionEvent arg0) {
		receiveAllEvents("connectionConnected", arg0);
		
	}
	
	public void connectionCreated(ConnectionEvent arg0) {
		receiveAllEvents("connectionCreated", arg0);
		
	}
	
	public void connectionDisconnected(ConnectionEvent arg0) {
		receiveAllEvents("connectionDisconnected", arg0);
		
	}
	
	public void connectionFailed(ConnectionEvent arg0) {
		receiveAllEvents("connectionFailed", arg0);
		
	}
	
	public void connectionInProgress(ConnectionEvent arg0) {
		receiveAllEvents("connectionInProgress", arg0);
		
	}
	
	public void connectionUnknown(ConnectionEvent arg0) {
		receiveAllEvents("connectionUnknown", arg0);
		
	}
	
	public void callActive(CallEvent arg0) {
		receiveAllEvents("callActive", arg0);
		
	}
	
	public void callInvalid(CallEvent arg0) {
		receiveAllEvents("callInvalid", arg0);
		
	}
	
	public void callEventTransmissionEnded(CallEvent arg0) {
		receiveAllEvents("callEventTransmissionEnded", arg0);		
	}
	
	public void singleCallMetaProgressStarted(MetaEvent arg0) {
		receiveAllEvents("singleCallMetaProgressStarted", arg0);
		
	}
	
	public void singleCallMetaProgressEnded(MetaEvent arg0) {
		receiveAllEvents("singleCallMetaProgressEnded", arg0);
		
	}
	
	public void singleCallMetaSnapshotStarted(MetaEvent arg0) {
		receiveAllEvents("singleCallMetaSnapshotStarted", arg0);
		
	}
	
	public void singleCallMetaSnapshotEnded(MetaEvent arg0) {
		receiveAllEvents("singleCallMetaSnapshotEnded", arg0);
		
	}
	
	public void multiCallMetaMergeStarted(MetaEvent arg0) {
		receiveAllEvents("multiCallMetaMergeStarted", arg0);
		
	}
	
	public void multiCallMetaMergeEnded(MetaEvent arg0) {
		receiveAllEvents("multiCallMetaMergeEnded", arg0);
		
	}
	
	public void multiCallMetaTransferStarted(MetaEvent arg0) {
		receiveAllEvents("multiCallMetaTransferStarted", arg0);
		
	}
	
	public void multiCallMetaTransferEnded(MetaEvent arg0) {
		receiveAllEvents("multiCallMetaTransferEnded", arg0);
		
	}
	
}
