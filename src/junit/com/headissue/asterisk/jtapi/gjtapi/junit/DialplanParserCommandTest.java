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

import java.util.ArrayList;
import java.util.List;

import junit.framework.TestCase;

import com.headissue.asterisk.jtapi.gjtapi.DialplanContextParser;

public class DialplanParserCommandTest extends TestCase {
  	
  	public DialplanParserCommandTest(String s) { super(s); }
/*[ Context 'extensions' created by 'pbx_config' ]
  '40' =>           1. Goto(terminals|alert3waitbusy|1)           [pbx_config]
  '41' =>           1. Goto(terminals|wait1busy|1)                [pbx_config]

-= 2 extensions (2 priorities) in 1 contexts. =-
*/
  	public void testSkipComment() {
  		List l = new ArrayList();
  		l.add("[ Context 'extensions' created by 'pbx_config' ]");
  		l.add("    '40' =>           1. Goto(terminals|alert3waitbusy|1)           [pbx_config]");
  		
  		DialplanContextParser dp = new DialplanContextParser(l,"extensions");
  		assertTrue("at least one line", dp.next());
  		assertTrue("recognized as goto", dp.getCommand().equals("goto"));
  	}
  	
  	public void testGotoContext() {
  		List l = new ArrayList();
  		l.add("    '40' =>           1. Goto(terminals|alert3waitbusy|1)           [pbx_config]");
  		
  		DialplanContextParser dp = new DialplanContextParser(l,"extensions");
  		assertTrue("at least one line", dp.next());
  		assertTrue("recognized as goto", dp.getCommand().equals("goto"));
  		assertTrue("context is terminals", dp.getGotoContext().equals("terminals"));
  	}

  	public void testGotoDefaultContext() {
  		List l = new ArrayList();
  		l.add("    '40' =>           1. Goto(alert3waitbusy|1)           [pbx_config]");
  		
  		DialplanContextParser dp = new DialplanContextParser(l,"extensions");
  		assertTrue("at least one line", dp.next());
  		assertTrue("recognized as goto", dp.getCommand().equals("goto"));
  		assertTrue("context is extensions", dp.getGotoContext().equals("extensions"));
  	}

  	public void testGotoExtensionId2val() {
  		List l = new ArrayList();
  		l.add("    '40' =>           1. Goto(alert|1)           [pbx_config]");
  		
  		DialplanContextParser dp = new DialplanContextParser(l,"extensions");
  		assertTrue("at least one line", dp.next());
  		assertTrue("recognized as goto", dp.getCommand().equals("goto"));
  		assertTrue("extension id is alert", dp.getGotoExtensionId().equals("alert"));
  	}

  	public void testGotoExtensionId3val() {
  		List l = new ArrayList();
  		l.add("    '40' =>           1. Goto(context|alert|1)           [pbx_config]");
  		
  		DialplanContextParser dp = new DialplanContextParser(l,"extensions");
  		assertTrue("at least one line", dp.next());
  		assertTrue("recognized as goto", dp.getCommand().equals("goto"));
  		assertTrue("extension id is alert", dp.getGotoExtensionId().equals("alert"));
  	}
 
}