package es.fdvcode.pipool.srv.scheduler;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertTrue;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import es.fdvcode.pipool.model.rele.Rele;
import org.junit.jupiter.api.Test;

class RelesLoaderTest {

//	@Test
//	public void testWriteJson() {
//
//		//Object to JSON in file
//		try {
//			File file = new File("conf/reles2.json");
//
//			RelesLoader loader = new RelesLoader();
//			loader.initDefaultMap();
//			
//			ObjectMapper mapper = new ObjectMapper();
//			mapper.enable(SerializationFeature.INDENT_OUTPUT);
//			
//			mapper.writeValue(file, loader.getReles().values());
//			assertTrue(true);
//		} 
//		catch (IOException e) {
//			System.out.println(e.getMessage());
//			e.printStackTrace();
//			assertTrue(false);
//		}
//	}
    
    @Test
    void testLoadJson() {

		Map<String, Rele> mapReles = new HashMap<>();
		
		try {
			ObjectMapper mapper = new ObjectMapper();
						
			//JSON file to List Java
			TypeReference<List<Rele>> mapType = new TypeReference<List<Rele>>() {};
	    	List<Rele> jsonToList = mapper.readValue(new File("conf/reles.json"), mapType);

	    	if(jsonToList!=null) {
	    		mapReles.clear();
	    		
	    		for(Rele rele : jsonToList) {
	    			mapReles.put(rele.getId(), rele);
	    		}
	    	}			
			
			assertTrue(true);
		} catch (IOException e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
			assertTrue(false);
		}
	}
}
