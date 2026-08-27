package es.fdvcode.pipool.srv.sonda;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import es.fdvcode.pipool.model.sonda.Sonda;
import es.fdvcode.pipool.model.sonda.Sonda.TipusSonda;
import es.fdvcode.pipool.mqtt.homeassistant.PipoolEntitiesMqttSrv;
import es.fdvcode.pipool.srv.ItemNotFoundException;
import es.fdvcode.pipool.srv.sonda.atlasi2c.SondaAtlasErrorException;
import es.fdvcode.pipool.srv.sonda.atlasi2c.impl.FactorySondaAtlas;
import es.fdvcode.pipool.srv.sonda.atlasi2c.impl.SondaAtlas;
import es.fdvcode.pipool.srv.sonda.atlasi2c.impl.SondaAtlasPH;
import es.fdvcode.pipool.srv.sonda.atlasi2c.impl.SondaAtlasRTD;
import lombok.RequiredArgsConstructor;

/**
 * 
 * @author cfarrema
 *
 */
@Component
@RequiredArgsConstructor
public class SondesSrv {

	private static final String ID_SONDA_PH = "sonda_ph"; 
	
	private final Logger log= LoggerFactory.getLogger(this.getClass());

	final FactorySondaAtlas factory;
	final Environment env;

	private final SondesQuerySrv sondesQuerySrv;
	private final PipoolEntitiesMqttSrv pipoolMqtt;
	
	@Value("${pipool.reles.idReleBomba}")
	private String idReleBomba;

	@Value("${pipool.reles.numDiesHistoria}")
	private int numDiesHistoriaReles;
	
	public void readSonda(Sonda sonda, boolean saveHistoric) throws IOException, SondaAtlasErrorException, NumberFormatException, InterruptedException {
		if(TipusSonda.Atlas.equals(sonda.getTipusSonda())) {
			readSondaAtlas(sonda, saveHistoric);
		}
		else if (TipusSonda.rPi.equals(sonda.getTipusSonda())) {
			readSondaRPi(sonda, saveHistoric);
		}		
		
		if (sonda.getStateSonda() != null && sonda.isReadingValid(sonda.getStateSonda().getValor())) {
			pipoolMqtt.pubStateSonda(sonda);
		} else {
			log.warn("No es publica l'estat per MQTT de la sonda {} per no tenir una lectura vàlida actualment.", sonda.getId());
		}
	}
	
	
	private void readSondaAtlas(Sonda sonda, boolean saveHistoric) throws SondaAtlasErrorException {
		log.trace("Tractant Sonda Atlas id: {}", sonda.getId());
		
		SondaAtlas sondaAtlas = sondesQuerySrv.getSondaAtlas(sonda);
		int maxIntents = 3;
		String strValor = null;
		boolean lecturaValida = false;
		
		for (int i = 0; i < maxIntents; i++) {
			strValor = sondaAtlas.execCmd(sonda, "R");
			if (sonda.isReadingValid(strValor)) {
				lecturaValida = true;
				break;
			}
			log.warn("Lectura de la sonda {} no vàlida ({}) en el seu intent {}/{}.", sonda.getId(), strValor, i + 1, maxIntents);
			try {
				Thread.sleep(500);
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
			}
		}
		
		if (lecturaValida) {
			sonda.setLecturaSonda(strValor, saveHistoric);
			
			//Si hem llegit temperatura fem compensation a SondaPH
			if(sondaAtlas instanceof SondaAtlasRTD) {
				//TODO: posar en fitxer config de sondes?
				try {
					Sonda sondaPh = sondesQuerySrv.getSonda(ID_SONDA_PH);
					SondaAtlasPH sondaAtlasPh = (SondaAtlasPH)sondesQuerySrv.getSondaAtlas(sondaPh);
					sondaAtlasPh.cmdSetTempCompensation(sondaPh, strValor);
					
				} catch (ItemNotFoundException e) {
					log.warn("No es troba la SONDA amb ID={}.", ID_SONDA_PH);
				}
			}
		} else {
			log.error("Sonda Atlas ID={} ha retornat valors no vàlids ({}) després de {} intents. Es descarta la lectura.", sonda.getId(), strValor, maxIntents);
		}
	}
	
	private void readSondaRPi(Sonda sonda, boolean saveHistoric) throws NumberFormatException, IOException, InterruptedException {
		
		if (sonda.getAddress()==1) {
			//rPI Temp Cpu
			String strValor;
			if("Local".equals(env.getActiveProfiles()[0])) {
				strValor = "51.4";
			}
			else {
				double cpuTemperature = getCpuTemperature();
				strValor = String.valueOf(cpuTemperature);
			}
			if (sonda.isReadingValid(strValor)) {
				sonda.setLecturaSonda(strValor, saveHistoric);
			} else {
				log.error("Lectura de sonda rPi {} no vàlida ({}). Es descarta.", sonda.getId(), strValor);
			}
		}
	}
	
	private double getCpuTemperature() throws IOException, InterruptedException, NumberFormatException {
        // La herramienta de línea de comandos para obtener la temperatura
		String[] command = {"vcgencmd", "measure_temp"};

        // Crea un nuevo proceso para ejecutar el comando
        Process process = new ProcessBuilder(command).start();

        // Lee la salida del comando
        BufferedReader reader = new BufferedReader(
            new InputStreamReader(process.getInputStream()));
        String line = reader.readLine();

        if (line != null) {
            // El comando devuelve una cadena como "temp=45.6'C"
            // Parseamos la cadena para extraer solo el valor numérico
            String tempStr = line.substring(line.indexOf("=") + 1, line.lastIndexOf("'C"));
            double temperature = Double.parseDouble(tempStr);

            // Espera a que el proceso termine y verifica el código de salida
            int exitCode = process.waitFor();
            if (exitCode != 0) {
                log.warn("El comando falló con el código de salida: " + exitCode);
            }
            return temperature;
        } else {
            throw new IOException("No se pudo obtener la temperatura de la CPU.");
        }
    }
}
