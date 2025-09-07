package es.fdvcode.pipool.security;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.springframework.http.HttpStatus;

public class ErrorResponse {
	private String timestamp;
	private int status;
	private String error;
	private String message;
	private String path;
	
	public ErrorResponse(HttpStatus httpstatus, String message, String path) {
		DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		this.timestamp = df.format(new Date());
		
		this.status = httpstatus.value();
		this.error = httpstatus.getReasonPhrase();
		this.message = message;
		this.path = path;
	}

	public String getTimestamp() {
		return timestamp;
	}

	public int getStatus() {
		return status;
	}

	public String getError() {
		return error;
	}

	public String getMessage() {
		return message;
	}

	public String getPath() {
		return path;
	}
}
