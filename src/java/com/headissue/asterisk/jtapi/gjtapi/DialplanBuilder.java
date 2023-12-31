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

import java.util.Map;
import net.sf.asterisk.manager.ManagerConnection;

public abstract class DialplanBuilder {
	
	ProviderConfig config;
	ManagerConnection managerConnection;
	
	public void setConfig(ProviderConfig _config) {
		config = _config;
	}
	
	public void setManagerConnection(ManagerConnection _managerConnection) {
		managerConnection = _managerConnection;
	}
	
	/** Build the dialplan */
	public abstract void build(); 
	
	/** Get addresses which map the terminals */
	public abstract Map getAddress2Terminals();
	
	/** Get terminals which map to addresses */
	public abstract Map getTerminal2Addresses();
	
	/** Get terminals which map to context */
	public abstract Map getTerminal2Context();
	
}
