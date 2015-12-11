import dbn.xmpp.notifier.smartcampus.hu.MySQLNotifier;
import xmpp.notifier.smartcampus.hu.InvalidNotifyMessageException;
import xmpp.notifier.smartcampus.hu.NotifyMessage;

public class Test {

	public class MySQLSample extends MySQLNotifier {
		
		private String secondQuery = "SELECT COUNT(id) FROM users";
		
		private long secondLastState = 0L;
		
		public MySQLSample(String host, String subdomain, String secretkey, String connectionUrl) {
			super(host, subdomain, secretkey, connectionUrl);
			this.changeQuery = "SELECT unix_timestamp(ts) as t FROM users ORDER BY t DESC LIMIT 1";
			this.hasChanged();
		}

		@Override
		public boolean loop() throws InvalidNotifyMessageException {
			long lastStateTemp = this.lastState;
			String queryTemp = this.changeQuery;
			if(hasChanged()){
				sendNotice(new NotifyMessage("Database changed!"));
				return true;
			}
			this.changeQuery = this.secondQuery;
			this.lastState = this.secondLastState;
			if(hasChanged()){
				sendNotice(new NotifyMessage("Database changed!"));
				this.changeQuery = queryTemp;
				this.secondLastState = this.lastState;
				this.lastState = lastStateTemp;
				return true;
			}else{
				this.changeQuery = queryTemp;
				this.lastState = lastStateTemp;
			}
			return true;
		}		
	}
	
	public static void main(String[] args) {
		try(MySQLSample s = new Test().new MySQLSample("192.168.0.58","test","YtkNHcxv8vf2YVBQ","jdbc:mysql://192.168.0.58:3306/openfire?user=openfire&password=openfire")){
			s.startThread();
			while(s.isAlive()){
				Thread.sleep(1000);
			}
		}catch (Exception e) {
			e.printStackTrace();
		}

	}
	
}
