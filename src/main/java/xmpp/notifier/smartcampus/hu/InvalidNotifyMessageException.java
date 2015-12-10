package xmpp.notifier.smartcampus.hu;

import java.util.List;

import org.xml.sax.SAXParseException;

public class InvalidNotifyMessageException extends Exception {
	
	private static final long serialVersionUID = -558814505713992734L;
	
	private List<SAXParseException> warnings;
	
	private List<SAXParseException> errors;
	
	private List<SAXParseException> fatalErrors;
	
	public InvalidNotifyMessageException(String message) {
		super(message);
		this.warnings = null;
		this.errors = null;
		this.fatalErrors = null;
	}

	public InvalidNotifyMessageException(List<SAXParseException> warnings, List<SAXParseException> errors,
			List<SAXParseException> fatalErrors) {
		super();
		this.warnings = warnings;
		this.errors = errors;
		this.fatalErrors = fatalErrors;
	}

	public List<SAXParseException> getWarnings() {
		return warnings;
	}

	public List<SAXParseException> getErrors() {
		return errors;
	}

	public List<SAXParseException> getFatalErrors() {
		return fatalErrors;
	}
	
	public void printWarnings(){
		for (SAXParseException saxParseException : warnings) {
			System.out.println(saxParseException.getMessage());
		}
	}
	
	public void printErrors(){
		for (SAXParseException saxParseException : errors) {
			System.err.println(saxParseException.getMessage());
		}
	}
	
	public void printFatalErrors(){
		for (SAXParseException saxParseException : fatalErrors) {
			System.err.println(saxParseException.getMessage());
		}
	}
	
}