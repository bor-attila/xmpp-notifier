/**
 * Copyright 2015 Bőr Attila 
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); 
 * you may not use this file except in compliance with the License. 
 * You may obtain a copy of the License at 
 * 
 *  http://www.apache.org/licenses/LICENSE-2.0 
 *  
 *  Unless required by applicable law or agreed to in writing, software 
 *  distributed under the License is distributed on an "AS IS" BASIS, 
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
 *  See the License for the specific language governing permissions and 
 *  limitations under the License. 
 */
package dbn.xmpp.notifier.smartcampus.hu;

import java.io.File;

import xmpp.notifier.smartcampus.hu.AbstractNotifier;

public abstract class FileNotifier extends AbstractNotifier {
	
	private File file = null;
	
	private long lastModified = 0;
	
	public FileNotifier(String host, String subdomain, String secretkey,String absolutePath) {
		super(host, subdomain, secretkey);
		this.file = new File(absolutePath);
		lastModified = this.file.lastModified();
	}

	@Override
	public boolean hasChanged() {
		long lm = this.file.lastModified();
		if(lm != lastModified){
			this.lastModified = lm;
			return true;
		}
		return false;
	}
	
}