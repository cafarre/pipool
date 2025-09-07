package es.fdvcode.pipool.common;

import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

@Component
public class ObjJsonPrinter {
	
	private ObjectMapper objectMapper = new ObjectMapper();
	
	public String print(Object obj) {
		return print(obj, false);
	}

	public String print(Object obj, boolean multiline) {
		String result = null; 
		try {
			result = objectMapper.writeValueAsString(obj);
		} catch (JsonProcessingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return result;
	}
}
