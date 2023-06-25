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

public class DialplanParserLocalTargetTest extends TestCase {
	
	public DialplanParserLocalTargetTest(String s) { super(s); }
	
  public void test0() throws Exception {
  	DialplanContextParser.LocalTarget lt = new DialplanContextParser.LocalTarget("");
  	assertTrue("empty=parserError", lt.isParseError());
  }

  public void test1() throws Exception {
  	DialplanContextParser.LocalTarget lt = new DialplanContextParser.LocalTarget("local/4711@acontext,30");
  	assertEquals("acontext",lt.getContext());
  	assertEquals("4711",lt.getExtension());
  }
	
  public void test2() throws Exception {
  	DialplanContextParser.LocalTarget lt = new DialplanContextParser.LocalTarget("local/4711@acontext");
  	assertEquals("acontext",lt.getContext());
  	assertEquals("4711",lt.getExtension());
  }

  public void test3() throws Exception {
  	DialplanContextParser.LocalTarget lt = new DialplanContextParser.LocalTarget("local/4711@acontext/r");
  	assertEquals("acontext",lt.getContext());
  	assertEquals("4711",lt.getExtension());
  }
  
}