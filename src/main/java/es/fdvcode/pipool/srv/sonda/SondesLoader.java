package es.fdvcode.pipool.srv.sonda;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import es.fdvcode.pipool.model.sonda.Sonda;
import es.fdvcode.pipool.model.sonda.Sonda.TipusSonda;
import jakarta.annotation.PostConstruct;

/**
 * 
 * @author cfarrema
 *
 */
@Component
public class SondesLoader {

	private final Logger log = LoggerFactory.getLogger(this.getClass());
	
	private final Map<String, Sonda> mapSondes = new HashMap<>();

	@Value("${pipool.sondes.file}")
	private String fileConfig;
	
	/**
	 * constructor spring
	 */
	@PostConstruct
	public void initClass() {
		
		log.info("Inicialitza SONDES's.");

		try {
			loadJsonFile();
		} catch (IOException e) {
			log.error("Error al llegir Json de Sondes.", e);
			initDefaultMap();
		}
		
		log.info("S'han trobat {} sondes definides.", mapSondes.size());
	}

	/**
	 * Default Map
	 */
	public void initDefaultMap() {
		
		log.info("Carreguem Sondes per defecte.");
		
		Sonda sonda = new Sonda("sonda_orp", "Sonda ORP/Redox", TipusSonda.Atlas, "mV", "None", 98, 2);
		mapSondes.put(sonda.getId(), sonda);

		sonda = new Sonda("sonda_ph", "Sonda PH", TipusSonda.Atlas, "ph", "None", 99, 3);
		mapSondes.put(sonda.getId(), sonda);

		sonda = new Sonda("sonda_temp", "Sonda TempºC", TipusSonda.Atlas, "ºC", "Temperature", 102, 1);
		mapSondes.put(sonda.getId(), sonda);

		sonda = new Sonda("temp_cpu_rpi", "Temp CPU rPi ºC", TipusSonda.rPi, "ºC", "Temperature", 1, 4);
		mapSondes.put(sonda.getId(), sonda);
	}
	
	public Map<String, Sonda> getSondes() {
		if(mapSondes==null) {
			initClass();
		}
		return mapSondes;
	}
	
	/**
	 * 
	 * @throws JsonGenerationException
	 * @throws JsonMappingException
	 * @throws IOException
	 */
	public void loadJsonFile() throws IOException {
		ObjectMapper mapper = new ObjectMapper();
		
		//JSON file to List Java
		TypeReference<List<Sonda>> mapType = new TypeReference<List<Sonda>>() {};
    	List<Sonda> jsonToList = mapper.readValue(new File(fileConfig), mapType);

    	if(jsonToList!=null) {
    		this.mapSondes.clear();
    		
    		for(Sonda sonda : jsonToList) {
    			mapSondes.put(sonda.getId(), sonda);
    		}
    	}
    	
    	log.info("JSON de Sonda llegida i carregada OK.");
	}	

	/**
	 * 
	 * @throws IOException
	 */
	public void writeJsonFile() throws IOException {
		ObjectMapper mapper = new ObjectMapper();
		mapper.enable(SerializationFeature.INDENT_OUTPUT);
		
		//Object to JSON in file
		mapper.writeValue(new File(fileConfig), mapSondes.values());

		log.info("JSON de Sonda grabat OK.");
	}

}
