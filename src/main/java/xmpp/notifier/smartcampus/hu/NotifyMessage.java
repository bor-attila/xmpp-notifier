package xmpp.notifier.smartcampus.hu;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.IllegalAddException;
import org.dom4j.io.DOMReader;
import org.dom4j.io.SAXReader;
import org.jivesoftware.smackx.pubsub.SimplePayload;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xmpp.packet.Message;
import org.xmpp.packet.PacketExtension;

public class NotifyMessage extends Message {
	
	Logger log = LoggerFactory.getLogger(NotifyMessage.class);
	
	private Object message;

	protected boolean validDocument;
	
	public NotifyMessage(String message) {
		this.message = message;
	}
	
	public NotifyMessage(Document message) {
		this.message = message;
	}
	
	public NotifyMessage(SimplePayload message) {
		this.message = message;
	}
	
	public NotifyMessage(org.w3c.dom.Document message){
		this.message = new DOMReader().read((org.w3c.dom.Document) message);
	}
	
	public Object getMessage() {
		return message;
	}
	
	final public void setBody() throws InvalidNotifyMessageException {
		Document doc = null;
		SimplePayload sp = null;
		
		if( message instanceof String ){
			super.setBody((String) message);
			return;
		}else if( message instanceof Document ){
			doc = (Document)message;
		}else if( message instanceof SimplePayload ){
			sp = (SimplePayload)message;
			try {  
		    	SAXReader reader = new SAXReader();
				reader.setIncludeInternalDTDDeclarations(true);
				doc = reader.read(new InputSource(new StringReader(sp.toXML().toString())));
		    } catch (Exception e) {  
		    	log.error(e.getMessage());
		    }
		}
		
		validate(doc);
		doc.setDocType(null);
		
		if( message instanceof Document ){
			try{
				addExtension(new PacketExtension(doc.getRootElement()));
			}catch(IllegalAddException e){
				log.error(e.getMessage());
			}
		}else if( message instanceof SimplePayload ){		
			message = new SimplePayload(((SimplePayload) message).getElementName(),((SimplePayload) message).getNamespace(),doc.getRootElement().asXML());
		}
	}

	final protected void validate(Document doc) throws InvalidNotifyMessageException {
		beforeValidate(doc);
		
		if( doc == null ){
			throw new InvalidNotifyMessageException("The document is null.");
		}
		
		if(doc.getDocType() == null){
			return;
		}
		
		validDocument = true;
		
		final List<SAXParseException> warnings = new ArrayList<>();
		final List<SAXParseException> errors = new ArrayList<>();
		final List<SAXParseException> fatalErrors = new ArrayList<>();
		
		SAXReader reader = new SAXReader();
		
		reader.setValidation(true);
		
		reader.setIncludeInternalDTDDeclarations(true);
		reader.setIncludeExternalDTDDeclarations(true);
		
		reader.setErrorHandler(new ErrorHandler() {
			@Override
			public void warning(SAXParseException exception) throws SAXException {
				warnings.add(exception);
			}
			
			@Override
			public void fatalError(SAXParseException exception) throws SAXException {
				errors.add(exception);
				validDocument = false;
			}
			
			@Override
			public void error(SAXParseException exception) throws SAXException {
				fatalErrors.add(exception);
				validDocument = false;
			}
		});
		
		try {
			reader.read(new InputSource(new StringReader(doc.asXML())));
		} catch (DocumentException e) {
			validDocument = false;
			log.error(e.getMessage());
		}
		
		afterValidate();
		if( !validDocument ){
			throw new InvalidNotifyMessageException(warnings, errors, fatalErrors);
		}
	}

	protected void afterValidate() {}

	protected void beforeValidate(Document doc) {}
	
}
