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

import org.dom4j.Element;
import org.jivesoftware.smack.SmackException.NoResponseException;
import org.jivesoftware.smack.SmackException.NotConnectedException;
import org.jivesoftware.smack.XMPPException.XMPPErrorException;
import org.jivesoftware.smackx.pubsub.LeafNode;
import org.jivesoftware.smackx.pubsub.PayloadItem;
import org.jivesoftware.smackx.pubsub.SimplePayload;
import org.jivesoftware.whack.ExternalComponentManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xmpp.component.ComponentException;
import org.xmpp.component.ComponentManager;
import org.xmpp.packet.IQ;
import org.xmpp.packet.IQ.Type;
import org.xmpp.packet.JID;
import org.xmpp.packet.Packet;

/**
 * @author Bőr Attila
 */
public abstract class AbstractNotifier implements INotifier  {
	
	/**
	 * Logger.
	 */
	private final Logger log = LoggerFactory.getLogger(AbstractNotifier.class);
	
	/**
	 * The XMPP 'service discovery items' namespace.
	 * 
	 * @see <a href="http://xmpp.org/extensions/xep-0030.html">XEP-0030</a>
	 */
	public static final String NAMESPACE_DISCO_ITEMS = "http://jabber.org/protocol/disco#items";

	/**
	 * The XMPP 'service discovery info' namespace.
	 * 
	 * @see <a href="http://xmpp.org/extensions/xep-0030.html">XEP-0030</a>
	 */
	public static final String NAMESPACE_DISCO_INFO = "http://jabber.org/protocol/disco#info";
	
	/**
	 * The default subdomain.It's strongly recommended to change after implementation.
	 */
	private String subdomain = "xmppnotifier";
	
	/**
	 * The main loop declared in @run method will loop in every <i>interval</i> second.
	 */
	protected int interval = 5000;
				
	/**
	 * Implementation of the ComponentManager interface for external components. This implementation follows JEP-0014.
	 */
	private ExternalComponentManager ecm = null;
	
	/**
	 * Description of the notifier.
	 */
	protected String description;
		
	/**
	 * Destination jabberd ID.
	 */
	protected Object to;
	
	/**
	 * Jabberd ID of the notifier.
	 */
	protected JID from = null;
	
	/**
	 * The thread where the notifier will run.
	 */
	private Thread thread;
		
	public AbstractNotifier(LeafNode to){
		this.thread = new Thread(this,subdomain);
		this.to = to;
	}
	
	public AbstractNotifier(String host,String subdomain,String secretkey,int port) {
		this.thread = new Thread(this,subdomain);
		this.subdomain = subdomain;
		this.description = "Broadcast your message over XMPP.";
		this.to = new JID("admin", host, "desktop");
		this.ecm = new ExternalComponentManager(host, port);
		this.ecm.setSecretKey(subdomain, secretkey);
		try {
			this.initialize((JID)this.to,this.ecm);
		} catch (ComponentException e) {
			log.error("Initialization failed.",e);
		}
	}
	
	public void startThread() {	
		this.thread.start();
	}
	
	public AbstractNotifier(String host,String subdomain,String secretkey){
		this(host,subdomain,secretkey,5275);
	}

	public String getName() {
		return this.thread.getName();
	}

	public void setName(String name) {
		this.thread.setName(name);
	}

	public void setTo(JID to) {
		this.to = to;
	}

	final public String getDescription() {
		return description;
	}

	final public void close() throws Exception {
		log.debug("Closing component ...");
		try {
			if( ecm != null ){
				ecm.removeComponent(subdomain);
			}
		} catch (ComponentException e) {
			log.error(e.getMessage());
		}
		this.thread.interrupt();
	}
	
	@Override
	public void processPacket(Packet packet) {
		log.debug("Incoming packet. {}",packet.toXML());
		if(packet instanceof IQ){
			IQ iq = (IQ)packet;
			if( iq.getType() == Type.get && iq.getChildElement().getNamespaceURI().compareTo(NAMESPACE_DISCO_INFO) == 0 ){
				ecm.sendPacket(this, handleDiscoInfo(iq));
			}
		}
	}
		
	@Override
	final public void initialize(JID jid, ComponentManager componentManager) throws ComponentException {
		if( ecm == null ){
			ecm = (ExternalComponentManager) componentManager;
		}
		ecm.addComponent(subdomain, this);
		ecm.setMultipleAllowed(subdomain, true);
	}

	@Override
	public void start() {
		log.info("The notifier is starting up.");
	}

	@Override
	public void shutdown() {
		log.info("Shutting down the notifier.");
	}
	
	/**
	 * Shows that is the internal thread is alive;
	 * 
	 * @return
	 */
	final public boolean isAlive(){
		return this.thread.isAlive();
	}
	
	/**
	 * Sets the bot JID.
	 * 
	 * @param from
	 */
	final public void setFrom(String from) {
		this.from = new JID(from);
	}
	
	@Override
	final public void run() {
		try {
			while( loop() ){
				Thread.sleep(interval);
			}
		} catch (InvalidNotifyMessageException e) {
			log.error("The notifier tried to send an invalid message. ({})",e.getMessage());
		} catch (InterruptedException e) {
			log.error("InterruptedException exception raised. Message : {}",e.getMessage());
		}
	}
	
	/**
	 * Sends a extended message. 
	 */
	final public void sendNotice(NotifyMessage msg) throws InvalidNotifyMessageException{
		if( to instanceof JID ){
			msg.setTo((JID)to);
			
			if( from != null )
				msg.setFrom(from);
			else
				msg.setFrom(this.getName());

			msg.setBody();
			new SimplePayload("book", "pubsub:test:book", "Two Towers");
			log.trace("Sending notice {}",msg.toXML());
			
			ecm.sendPacket(this, msg);
		}else if( to instanceof LeafNode ){
			try {
				msg.setBody();
				SimplePayload sp = (SimplePayload) msg.getMessage();
				((LeafNode) to).send(new PayloadItem<SimplePayload>("message",sp));
			} catch (NoResponseException | XMPPErrorException | NotConnectedException | InterruptedException e) {
				log.error(e.getMessage());
			}
			
		}
	}
	
	/**
	 * Creates a disco stanza.
	 * 
	 * @param iq
	 * @return
	 */
	final protected IQ handleDiscoInfo(IQ iq) {
		
		final IQ replyPacket = IQ.createResultIQ(iq);
		
		final Element responseElement = replyPacket.setChildElement("query",NAMESPACE_DISCO_INFO);

		// identity
		responseElement.addElement("identity")
				.addAttribute("category","client")
				.addAttribute("type","bot")
				.addAttribute("description",getDescription())
				.addAttribute("name", getName());
		// features
		responseElement.addElement("feature").addAttribute("var",NAMESPACE_DISCO_INFO);
		
		return replyPacket;
	}
	
	/**
	 * In general case we always return true, 
	 * so strongly recommended to override this method.
	 */
	@Override
	public boolean hasChanged() {
		return true;
	}
}	
