package es.fdvcode.pipool.restsrv.v1.response;

import java.util.Date;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonPropertyOrder({ "horaRpiStr", "body", "error"})
public class RestResponseBody<T> {

	private T body;
	private String error;

	public RestResponseBody() {
	}

	public RestResponseBody(T body) {
		this.body=body;
	}
	
	public RestResponseBody(String error) {
		this.error=error;
	}	

	public RestResponseBody(T body, String error) {
		this.body=body;
		this.error=error;
	}	

	
	public Date getHoraRpi() {
		return new Date();
	}

	public String getHoraRpiStr() {
		return new Date().toString();
	}
	
	@JsonInclude(Include.NON_NULL)
	public T getBody() {
		return body;
	}

	@JsonInclude(Include.NON_NULL)
	public String getError() {
		return error;
	}
}
