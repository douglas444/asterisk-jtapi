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

package com.headissue.asterisk.jtapi.gjtapi.junit;

import java.lang.reflect.Constructor;

import junit.framework.Test;
import junit.framework.TestSuite;

public class AllTests extends TestSuite {

	public AllTests() {
		addTestSuite(DialplanParserCommandTest.class);
		addTestSuite(DialplanParserLineTest.class);
		addTestSuite(DialplanParserLocalTargetTest.class);
	}
	
	/**
	 * Run code with text interface. Empty arguments run all tests. 
	 * An argument of the form "packge.class.methodname" runs a specific test.
	 */
	public static void main(String[] args) throws Exception {
		if (args.length==0) {
			junit.textui.TestRunner.run(new AllTests());
		} else {
			String s = args[0];
			s = s.replace('/','.');
			Class _suiteClass = null;
			try {
				_suiteClass = Class.forName(s);
			} catch (ClassNotFoundException ex) {}
			Test t;
			if (_suiteClass != null) {
				if (TestSuite.class.isAssignableFrom(_suiteClass)) {
					t = (TestSuite) _suiteClass.newInstance();
				} else {
					t = new TestSuite(_suiteClass);
				}
			} else {
				int i = s.lastIndexOf('.');
				String _className  = s.substring(0,i);
				String _methodName = s.substring(i+1);
				Class c         = Class.forName(_className);
				Class[] ca      = {String.class};
				Constructor con = c.getConstructor(ca);
				Object[] oa = {_methodName};
				t = (Test) con.newInstance(oa);
			}
			junit.textui.TestRunner.run( t );
		}
	}
	
}
