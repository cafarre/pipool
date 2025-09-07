package es.fdvcode.pipool.common;

import org.slf4j.helpers.FormattingTuple;
import org.slf4j.helpers.MessageFormatter;

public class ParameterizedMessage {

	private FormattingTuple msg;
	public ParameterizedMessage(String text, Object...args) {
		this.msg = MessageFormatter.arrayFormat(text, args);
	}
	
	public String getFormattedMessage() {
		return msg.getMessage();
	}
	
	public String toString() {
		return getFormattedMessage();
	}
}
