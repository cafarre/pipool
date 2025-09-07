package es.fdvcode.pipool.srv.rele;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import es.fdvcode.pipool.common.ObjJsonPrinter;
import es.fdvcode.pipool.model.MqttType;
import es.fdvcode.pipool.model.rele.CalendarRele;
import es.fdvcode.pipool.model.rele.FranjaHoraria;
import es.fdvcode.pipool.model.rele.Rele;
import jakarta.annotation.PostConstruct;


/**
 * 
 * @author cfarrema
 *
 */
@Component
public class RelesLoader {

	private final Logger log = LoggerFactory.getLogger(this.getClass());
	
	private final Map<String, Rele> mapReles = new HashMap<>();

	@Autowired 
	ObjJsonPrinter objJsonPrinter;

	
	@Value("${pipool.reles.file}")
	private String fileConfig;


	
	/**
	 * spring constructor
	 */
	@PostConstruct
	public void initClass() {
		
		log.info("Inicialitza RELE's.");

		try {
			loadJsonFile();
		} catch (IOException e) {
			log.error("Error al llegir Json de Reles.", e);
			initDefaultMap();
		}
		
		log.info("S'han trobat {} reles definits.", mapReles.size());
	}

	/**
	 * default map
	 */
	public void initDefaultMap() {
		
		log.info("Carreguem reles per defecte.");
		
		Rele rele = new Rele("rele_bomba", "Motor Bomba Filtre", 25, false, null, 1, MqttType.SWITCH.name(), true, false);
		rele.setSecondsDuradaCicles(10800);
		mapReles.put(rele.getId(), rele);

		List<CalendarRele> listCalendars = new ArrayList<>();
		CalendarRele cal = new CalendarRele();
		cal.setId("ProgBombaEstiu");
		cal.setNom("Programació Bomba Estiu");
		cal.setRele(rele);
		cal.setDiaIni(1);
		cal.setMesIni(6);
		cal.setDiaFin(30);
		cal.setMesFin(9);
		cal.getListFrangesHoraries().add(new FranjaHoraria(8, 0, 0, 0));
		cal.getListFrangesHoraries().add(new FranjaHoraria(11, 0, 0, 180*60));
		cal.getListFrangesHoraries().add(new FranjaHoraria(16, 0, 0, 0));
		cal.getListFrangesHoraries().add(new FranjaHoraria(22, 0, 0, 0));

		listCalendars.add(cal);
		
		cal = new CalendarRele();
		cal.setId("ProgBombaHivern");
		cal.setNom("Programació Bomba Hivern");
		cal.setRele(rele);
		cal.setDiaIni(1);
		cal.setMesIni(10);
		cal.setDiaFin(30);
		cal.setMesFin(5);
		cal.getListFrangesHoraries().add(new FranjaHoraria(6, 0, 0, 180*60));
		cal.getListFrangesHoraries().add(new FranjaHoraria(21, 0, 0, 90*60));

		listCalendars.add(cal);
		rele.setCalendars(listCalendars);
		
		rele = new Rele("rele_lfi", "Motor Bomba LFI", 24, false, rele.getId(), 2, MqttType.SWITCH.name(), true, false);
		mapReles.put(rele.getId(), rele);

		listCalendars = new ArrayList<>();
		cal = new CalendarRele();
		cal.setId("ProgBombaLfiEstiu");
		cal.setNom("Programació Bomba LFI Estiu");
		cal.setRele(rele);
		cal.setDiaIni(1);
		cal.setMesIni(6);
		cal.setDiaFin(30);
		cal.setMesFin(9);
		cal.getListFrangesHoraries().add(new FranjaHoraria(16, 0, 0, 180*60));
		cal.getListFrangesHoraries().add(new FranjaHoraria(22, 0, 0, 90*60));

		listCalendars.add(cal);
				
		cal = new CalendarRele();
		cal.setId("ProgBombaLfiHivern");
		cal.setNom("Programació Bomba LFI Hivern");
		cal.setRele(rele);
		cal.setDiaIni(1);
		cal.setMesIni(10);
		cal.setDiaFin(30);
		cal.setMesFin(5);
		cal.getListFrangesHoraries().add(new FranjaHoraria(6, 0, 0, 180*60));
		cal.getListFrangesHoraries().add(new FranjaHoraria(21, 0, 0, 90*60));

		listCalendars.add(cal);
		rele.setCalendars(listCalendars);
				
		rele = new Rele("rele_llums", "Llums Piscina", 23, false, null, 3, MqttType.LIGHT.name(), true, false);
		mapReles.put(rele.getId(), rele);
	}
	
	public Map<String, Rele> getReles() {
		if(mapReles==null) {
			initClass();
		}
		return mapReles;
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
		TypeReference<List<Rele>> mapType = new TypeReference<List<Rele>>() {};
    	List<Rele> jsonToList = mapper.readValue(new File(fileConfig), mapType);

    	if(jsonToList!=null) {
    		synchronized (mapReles) {
        		this.mapReles.clear();
        		
        		for(Rele rele : jsonToList) {
        			mapReles.put(rele.getId(), rele);
        		}
			}
    	}
    	
    	log.info("JSON de Rele llegit i carregat OK: {}", objJsonPrinter.print(mapReles));
	}	

	/**
	 * 
	 * @throws JsonGenerationException
	 * @throws JsonMappingException
	 * @throws IOException
	 */
	public void writeJsonFile() throws IOException {
		ObjectMapper mapper = new ObjectMapper();
		mapper.enable(SerializationFeature.INDENT_OUTPUT);
		
		//Object to JSON in file
		mapper.writeValue(new File(fileConfig), mapReles.values());

		log.info("JSON de Rele grabat OK.");
	}

}
