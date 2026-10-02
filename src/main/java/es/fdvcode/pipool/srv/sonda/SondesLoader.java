package es.fdvcode.pipool.srv.sonda;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import es.fdvcode.pipool.model.sonda.Sonda;
import es.fdvcode.pipool.model.sonda.Sonda.TipusSonda;
import es.fdvcode.pipool.model.sonda.StateSonda;
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

	private abstract static class SondaConfigFileMixIn {
		@JsonIgnore
		abstract StateSonda getStateSonda();
	}

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
	 * Default Map: intenta carregar des del recurs /default-config/sondes.json
	 * i si falla, construeix la configuració per defecte programàticament.
	 */
	public void initDefaultMap() {
		log.info("Carreguem Sondes per defecte.");

		try (InputStream is = getClass().getResourceAsStream("/default-config/sondes.json")) {
			if (is != null) {
				ObjectMapper mapper = new ObjectMapper();
				TypeReference<List<Sonda>> mapType = new TypeReference<List<Sonda>>() {};
				List<Sonda> list = mapper.readValue(is, mapType);
				if (list != null && !list.isEmpty()) {
					synchronized (mapSondes) {
						this.mapSondes.clear();
						for (Sonda sonda : list) {
							mapSondes.put(sonda.getId(), sonda);
						}
					}
					log.info("Sondes per defecte carregades correctament des de /default-config/sondes.json. Total: {}", mapSondes.size());
					return;
				}
			}
		} catch (Exception e) {
			log.warn("No s'ha pogut carregar el recurs /default-config/sondes.json: {}", e.getMessage());
		}

		initDefaultProgrammaticMap();
	}

	public void initDefaultProgrammaticMap() {
		synchronized (mapSondes) {
			this.mapSondes.clear();

			Sonda sonda = new Sonda("sonda_temp", "Sonda Temp°C", TipusSonda.Atlas, "°C", "Temperature", 102, 1, 0.0, 60.0);
			mapSondes.put(sonda.getId(), sonda);

			sonda = new Sonda("sonda_orp", "Sonda ORP/Redox", TipusSonda.Atlas, "mV", null, 98, 2, 0.0, 1000.0);
			sonda.setIdReleCorrector("rele_bomba_clor");
			mapSondes.put(sonda.getId(), sonda);

			sonda = new Sonda("sonda_ph", "Sonda PH", TipusSonda.Atlas, "ph", null, 99, 3, 0.0, 14.0);
			sonda.setIdReleCorrector("rele_bomba_acid");
			mapSondes.put(sonda.getId(), sonda);

			sonda = new Sonda("temp_cpu_rpi", "Temp CPU rPi °C", TipusSonda.rPi, "°C", "Temperature", 1, 4, 0.0, 100.0);
			sonda.setIdReleCorrector("rele_fan");
			mapSondes.put(sonda.getId(), sonda);

			sonda = new Sonda("temp_shelly_flood", "Temp Caseta", TipusSonda.rPi, "°C", "Temperature", 5, 5, -20.0, 60.0);
			mapSondes.put(sonda.getId(), sonda);
		}
		log.info("Sondes per defecte carregades programàticament. Total: {}", mapSondes.size());
	}

	public Map<String, Sonda> getSondes() {
		if (mapSondes == null) {
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

		// JSON file to List Java
		TypeReference<List<Sonda>> mapType = new TypeReference<List<Sonda>>() {
		};
		List<Sonda> jsonToList = mapper.readValue(new File(fileConfig), mapType);

		if (jsonToList != null) {
			this.mapSondes.clear();

			for (Sonda sonda : jsonToList) {
				mapSondes.put(sonda.getId(), sonda);
			}
		}

		log.info("JSON de Sonda llegida i carregada OK.");
	}

	/**
	 * 
	 * @throws IOException
	 */
	public synchronized void writeJsonFile() throws IOException {
		ObjectMapper mapper = new ObjectMapper();
		mapper.enable(SerializationFeature.INDENT_OUTPUT);
		mapper.addMixIn(Sonda.class, SondaConfigFileMixIn.class);

		File targetFile = new File(fileConfig);
		File parentDir = targetFile.getParentFile();
		if (parentDir != null && !parentDir.exists()) {
			parentDir.mkdirs();
		}

		File tempFile = new File(targetFile.getAbsolutePath() + ".tmp");

		synchronized (mapSondes) {
			mapper.writeValue(tempFile, mapSondes.values());
		}

		Path tempPath = tempFile.toPath();
		Path targetPath = targetFile.toPath();
		try {
			Files.move(tempPath, targetPath, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
		} catch (AtomicMoveNotSupportedException e) {
			log.warn("ATOMIC_MOVE no suportat ({}), utilitzant reemplaçament estàndard.", e.getMessage());
			Files.move(tempPath, targetPath, StandardCopyOption.REPLACE_EXISTING);
		}

		log.info("JSON de Sonda grabat OK de forma atòmica a: {}", fileConfig);
	}

}
