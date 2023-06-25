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

import junit.framework.TestCase;

import com.headissue.asterisk.jtapi.gjtapi.DialplanContextParser;

public class DialplanParserLineTest extends TestCase {

	public DialplanParserLineTest(String s) { super(s); }
	
	public void test0() {
		DialplanContextParser.Line l = new DialplanContextParser.Line();
		assertTrue("comment", l.isComment());
	}
	
 	public void test1() {
		DialplanContextParser.Line l = new DialplanContextParser.Line();
		l.set("[  bla]");
		assertTrue("comment", l.isComment());
	}
	
 	public void test2() {
		DialplanContextParser.Line l = new DialplanContextParser.Line();
		l.set("'12'= 1. Dial(\"Zap/1/2\"");
		assertTrue("newExtension", l.isNewExtension());
	}
 	
}