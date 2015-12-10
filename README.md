XMPP Notifier
=============

A small external component for watching and reacting changes on a file or in a database.

You have 2 channel options for delivering notifications:

PubSub
------

1. Create a connection

```java
XMPPTCPConnectionConfiguration config = XMPPTCPConnectionConfiguration.builder()
			.setXmppDomain(JidCreate.domainBareFrom("192.168.0.57"))
			.setSecurityMode( ConnectionConfiguration.SecurityMode.disabled )
			.setPort(5222)
            .setUsernameAndPassword("test", "test")
            .setDebuggerEnabled(false).build();
XMPPTCPConnection xmppConnection = new XMPPTCPConnection(config);
xmppConnection.connect().login();
``` 

2. Create or get a LeafNode and configure it

```java
PubSubManager manager = PubSubManager.getInstance(xmppConnection);
//manager.createNode("test"); -- create
LeafNode nd = manager.getNode("MyTestNode");
ConfigureForm form = new ConfigureForm(Type.submit);
form.setAccessModel(AccessModel.open);
form.setDeliverPayloads(true);
form.setNotifyRetract(true);
form.setPersistentItems(true);
form.setPublishModel(PublishModel.open);
nd.sendConfigurationForm(form);
```

Ok, we are ready to publish something.

Direct to a JID
---------------

The second option is deliver directly to a JID by an external component (Openfire).
This option doesn't need special settings, but you must create a shared secret in openfire admin.

Let's do this. Extend the Abstract Notifier.
============================================

```java
	public class Sample extends AbstractNotifier {
	
		public Sample(String host, String subdomain, String password) {
			super(host,subdomain,password); 
		}
		
		@Override
		public boolean loop() throws InvalidNotifyMessageException {
			if( hasChanged() ){
				sendNotice(new NotifyMessage("text"));
			}
			return true;
		}
		
		@Override
		public boolean hasChanged() {
			//something happend ?
		}
		
	}
```

If you want to break the loop, just return false.

Changed a file ? No problem.

```java
	public class FileSample extends FileNotifier {
		
		public FileSample(String host, String subdomain, String secretkey, String absolutePath) {
			super(host, subdomain, secretkey, absolutePath);
		}

		@Override
		public boolean loop() throws InvalidNotifyMessageException {
			if(hasChanged()){
				sendNotice(new NotifyMessage("File changed!"));
			}
			return true;
		}		
	}
```

The FileNotifier is automatic watching.

MySQL or MSSQL ? No problem.

``java
public class MSSQLSample extends MSSQLNotifier {
		private int counter = 0;
		
		public MSSQLSample(String host, String subdomain, String secretkey, String connectionUrl) {
			super(host, subdomain, secretkey, connectionUrl);
			this.changeQuery = "SELECT ABS(CHECKSUM(NewId())) % 14";
		}

		@Override
		public boolean loop() throws InvalidNotifyMessageException {
			if(hasChanged()){
				sendNotice(new NotifyMessage("Database changed!"));
				counter++;
			}
			return counter <= 5;
		}		
	}
```

``java
public class MySQLSample extends MySQLNotifier {
		private int counter = 0;
		
		public MSSQLSample(String host, String subdomain, String secretkey, String connectionUrl) {
			super(host, subdomain, secretkey, connectionUrl);
			this.changeQuery = "SELECT ABS(CHECKSUM(NewId())) % 14";
		}

		@Override
		public boolean loop() throws InvalidNotifyMessageException {
			if(hasChanged()){
				sendNotice(new NotifyMessage("Database changed!"));
				counter++;
			}
			return counter <= 5;
		}		
	}
```

Just set an query which return a single LONG, not more not less. eg. SELECT COUNT() ...

How to run it ...

```java
	try( XmlSample test = new XmppNotifierSample().new XmlSample(leafNode) ) {
		test.startThread();
		while(test.isAlive()){
			Thread.sleep(1000);
		}
	} catch (ComponentException e) {			
		System.err.println(e.getMessage());
	}
```

Instead of string you can also send an XML.

sendNotice(new NotifyMessage(new SimplePayload("note", "namesace", "<note>test</note>)));

*!IMPORTANT The XML's with DTD will be automatically validated, and the notification will be purged if the XML is not valid.*

*For databases you shuold include your own driver!*

To receive the messages use : 
```java 
xmppConnection.addSyncStanzaListener()
```

for nodeItems
```java
node.addItemEventListener()
```

