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

package com.headissue.asterisk.jtapi.gjtapi;

import java.util.ArrayList;

public class Terminal {

	String name;

	ArrayList addresses;

	public Terminal(String _name) {
		name = _name;
	}

	public void addAddress(String _addr) {
		addresses.add(_addr);
	}
	
	public String[] getAdresses() {
		String[] sa = new String[addresses.size()];
		addresses.toArray(sa);
		return sa;
	}

	public boolean equals(Object o) {
		if (o instanceof Terminal && name != null) {
			return name.equals(((Terminal) o).name);
		}
		return false;
	}

	public int hashCode() {
		if (name != null) {
			return name.hashCode();
		}
		return super.hashCode();
	}

}
