package es.fdvcode.pipool.restsrv.v1.response;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.MultiValueMap;

public class RestResponse<T> extends ResponseEntity<RestResponseBody<T>>{

	public RestResponse(HttpStatus status) {
		super(status);
	}

	public RestResponse(MultiValueMap<String, String> headers, HttpStatus status) {
		super(headers, status);
	}

	public RestResponse(T body, HttpStatus status) {
		super(new RestResponseBody<T>(body), status);
	}

	public RestResponse(T body, MultiValueMap<String, String> headers, HttpStatus status) {
		super(new RestResponseBody<T>(body), headers, status);
	}
	
	
	public RestResponse(HttpStatus status, String error) {
		super(new RestResponseBody<T>(error),status);
	}

	public RestResponse(MultiValueMap<String, String> headers, HttpStatus status, String error) {
		super(new RestResponseBody<T>(error),headers, status);
	}

	public RestResponse(T body, HttpStatus status, String error) {
		super(new RestResponseBody<T>(body, error), status);
	}

	public RestResponse(T body, MultiValueMap<String, String> headers, HttpStatus status, String error) {
		super(new RestResponseBody<T>(body, error), headers, status);
	}	
}
