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
package xmpp.notifier.smartcampus.hu;

import org.xmpp.component.Component;

/**
 * The default Notifier schema, which contains some default settings.
 * 
 * @author Bőr Attila
 */
public interface INotifier extends Runnable, AutoCloseable, Component {
		
	/**
	 * When something changes this method will send the message to the server.
	 * 
	 * @param msg The message.
	 * @throws InvalidXMessageException When the NotifyMessage is a XML ddocument, and it's not valid.
	 */
	void sendNotice(NotifyMessage msg) throws InvalidNotifyMessageException;
	
	/**
	 * {@inheritDoc}
	 * 
	 * This method should containt the main loop.
	 */
	@Override
	void run();
		
	/**
	 * This MUST be overridden be the programmer. While this loop returns true,
	 * the loop in the run method should run.
	 * 
	 * @return If false the notification server will down.
	 * @throws InvalidNotifyMessageException if the message is invalid, this will break the loop
	 */
	boolean loop() throws InvalidNotifyMessageException;
	
	
	/**
	 * This SHOULD BE invoked from @see loop()
	 * 
	 * @return if true something has Changed
	 */
	boolean hasChanged();
	
}