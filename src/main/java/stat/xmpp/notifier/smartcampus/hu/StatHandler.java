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
package stat.xmpp.notifier.smartcampus.hu;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import org.apache.commons.io.IOUtils;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

public class StatHandler implements HttpHandler {
	
	private Monitor monitor;

	private String template;
	
	public StatHandler(int port) {
		InputStream in = null;
		try {
			this.monitor = new Monitor(port,this);
			in = StatHandler.class.getClassLoader().getResource("index.html").openStream();
			template = IOUtils.toString(in);
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			IOUtils.closeQuietly(in);
		}
		this.monitor.start();
	}
		
	@Override
	public void handle(HttpExchange e) throws IOException {
        e.sendResponseHeaders(200, template.length());
        OutputStream os = e.getResponseBody();
        os.write(template.getBytes());
        os.close();
	}
	
	/**
	 * Change HTML template on runtime.
	 * 
	 * @param htmltemplate
	 */
	public void setTemplate(String htmltemplate) {
		this.template = htmltemplate;
	}
	
}
