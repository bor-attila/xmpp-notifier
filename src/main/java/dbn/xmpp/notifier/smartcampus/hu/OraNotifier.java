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


public abstract class OraNotifier extends AbstractNotifier {
	
	private boolean useFlashBackThreshold = false;
	
	protected Connection conn;
	
	protected String changeQuery = "SELECT 1 FROM dual";
	
	protected long lastState = -1L;
	
	protected boolean flashBackOn = false;

	protected long threshold = 1L;
	
	protected final Logger log = LoggerFactory.getLogger(OraNotifier.class);
	
	public OraNotifier(LeafNode to, String connectionUrl) {
		super(to);
		this.connectionInit(connectionUrl);
	}

	public OraNotifier(String host, String subdomain, String secretkey,String connectionUrl) {
		super(host, subdomain, secretkey);
		this.connectionInit(connectionUrl);
	}
	
	private void connectionInit(String connectionUrl){
		try {
			Class.forName("oracle.jdbc.driver.OracleDriver");
			this.conn = DriverManager.getConnection(connectionUrl);
		} catch (ClassNotFoundException e) {
			log.error("JDBC driver not found!",e);
			System.exit(-1);
		} catch (SQLException e) {
			log.error(e.getMessage());
			System.exit(-1);
		}
		
		try {
			PreparedStatement ps = conn.prepareStatement("SELECT FLASHBACK_ON FROM V$DATABASE");
			ResultSet rs = ps.executeQuery();
			if(rs.next()){
				flashBackOn = rs.getString(1).compareTo("YES") == 0;
			}
			rs.close();
			ps.close();
		} catch (SQLException e) {
			log.info("Cannot get state of SCN. (Probably insuficient rights ?)");
			log.error(e.getMessage());
		}
		
		if(flashBackOn){
			log.info("Flashback is on!");
			changeQuery = "SELECT dbms_flashback.get_system_change_number FROM dual";
		}else{
			log.info("Flashback is off, using fallback SQL!");
		}
	}
	
	public void setChangeQuery(String changeQuery) {
		this.changeQuery = changeQuery;
	}

	public boolean isFlashBackOn() {
		return flashBackOn;
	}
	

	public boolean isUseFlashBackThreshold() {
		return useFlashBackThreshold;
	}
	
	public long getThreshold() {
		return threshold;
	}

	public void setThreshold(long threshold) {
		if( threshold <= 0 ){
			log.error("The threshold should be greater than 0.");
		}else{
			this.threshold = threshold;			
		}
	}

	public void setUseFlashBackThreshold(boolean useFlashBackThreshold) {
		if(flashBackOn){
			this.useFlashBackThreshold = useFlashBackThreshold;
		}else{
			log.error("Flashback is disabled.");
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
				log.trace("SCN : {}",scn);
				if( useFlashBackThreshold ){
					if(Math.abs(lastState - scn) >= this.threshold){
						changed = true;
					}
					lastState = scn;
				}else if( lastState != scn ){
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
