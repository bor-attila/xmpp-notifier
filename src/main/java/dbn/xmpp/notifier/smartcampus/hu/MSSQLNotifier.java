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

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.jivesoftware.smackx.pubsub.LeafNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import xmpp.notifier.smartcampus.hu.AbstractNotifier;

public abstract class MSSQLNotifier extends AbstractNotifier {
		
	protected Connection conn;
	
	protected String changeQuery = "SELECT 1";
	
	protected long lastState = 1L;
		
	protected final Logger log = LoggerFactory.getLogger(MSSQLNotifier.class);
	
	public MSSQLNotifier(LeafNode to, String connectionUrl) {
		super(to);
		this.connectionInit(connectionUrl);
	}

	public MSSQLNotifier(String host, String subdomain, String secretkey, String connectionUrl) {
		super(host, subdomain, secretkey);
		this.connectionInit(connectionUrl);
	}
	
	private void connectionInit(String connectionUrl){
		try {
			Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
			this.conn = DriverManager.getConnection(connectionUrl);
		} catch (ClassNotFoundException e) {
			log.error("MSSQL driver not found!",e);
			System.exit(-1);
		} catch (SQLException e) {
			log.error(e.getMessage());
			System.exit(-1);
		}
	}
	
	@Override
	public boolean hasChanged() {
		boolean changed = false;
		try {
			PreparedStatement ps = conn.prepareStatement(changeQuery);
			ResultSet rs = ps.executeQuery();
			if(rs.next()){
				long scn = Long.parseLong(rs.getString(1));
				if( lastState != scn ){
					lastState = scn;
					changed = true;
				}
			}
			rs.close();
			ps.close();
		} catch (Exception e) {
			log.error(e.getMessage());
		}
		return changed;
	}

}
