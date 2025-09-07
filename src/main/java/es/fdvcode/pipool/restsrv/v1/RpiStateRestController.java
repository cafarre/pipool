package es.fdvcode.pipool.restsrv.v1;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import es.fdvcode.pipool.PiPoolContext;
import es.fdvcode.pipool.common.ObjJsonPrinter;
import es.fdvcode.pipool.common.SystemCommand;
import es.fdvcode.pipool.restsrv.v1.response.RestResponse;
import es.fdvcode.pipool.srv.persist.PiPoolPeriodicTaskPersist;


/**
 * 
 * @author cfarrema
 *
 */
@RestController
@RequestMapping(RpiStateRestController.URIBASE)
public class RpiStateRestController {

	protected final Logger log = LoggerFactory.getLogger(this.getClass());
	public static final String URIBASE = "api/v1/rpistate";
	
	@Autowired
	private PiPoolContext piPoolCtx;

	@Autowired
	private SystemCommand sysCmd;

	@Autowired
	PiPoolPeriodicTaskPersist persister;

	@Autowired 
	ObjJsonPrinter objJsonPrinter;
	
	@Autowired
	private Environment environment;
	
	/**
	 * 
	 * @return
	 */
	@GetMapping("/info")
	public RestResponse<Map<String, String>> info() {

		log.info("REST - Get Info rPi.");
		Map<String, String> result = this.getInfo();
		log.info("rPi INFO: {}", objJsonPrinter.print(result));
		return new RestResponse<>(result, HttpStatus.OK);
	}

	/**
	 * 
	 * @return
	 */
	@GetMapping("/ping")
	public RestResponse<Boolean> ping() {
		return new RestResponse<>(true, HttpStatus.OK);
	}

	/**
	 * 
	 * @return
	 */
	@PutMapping("/reboot")
	public RestResponse<Boolean> reboot() {

		log.info("REST - Reboot rPi request.");
		Process p;
		try {
			//Persistencia de fitxers abans d'apagar
			persister.run();
			
			//Renici de la Pi
			p = Runtime.getRuntime().exec(new String[]{"sudo reboot"});

			log.info("Reboot OK rPi. Process: {}", objJsonPrinter.print(p));
			return new RestResponse<>(true, HttpStatus.OK);
		} 
		catch (IOException e) {
			log.error("ERROR al fer reboot de la rPi", e);
			return new RestResponse<>(false, HttpStatus.OK);
		}
	}

	
	private Map<String, String> getInfo() {

		Map<String, String> result = new LinkedHashMap<>();

		//PiPool Context
		try {
			result.put("DateTimeAppStarted", piPoolCtx.printDateAppStarted());
			result.put("DateLastExecutionSchedulerRele", piPoolCtx.printDateLastExecutionSchedulerRele());
			result.put("DateLastExecutionSchedulerSonda", piPoolCtx.printDateLastExecutionSchedulerSonda());
			result.put("DateLastExecutionSchedulerPersist", piPoolCtx.printDateLastExecutionSchedulerPersist());
			result.put("DateLastExecutionSchedulerBackup", piPoolCtx.printDateLastExecutionSchedulerBackup());
		} 
		catch (Exception e) {
			log.error(e.getMessage(), e);
		}

		//RPI Specific info
		if(!"Local".equalsIgnoreCase(environment.getActiveProfiles()[0])) {
			//SystemInfo
//			try {
//				result.put("OsName", String.valueOf(SystemInfo.getOsName()));
//				result.put("OsVersion", String.valueOf(SystemInfo.getOsVersion()));
//				result.put("ModelName", String.valueOf(SystemInfo.getModelName()));
//				result.put("Processor", String.valueOf(SystemInfo.getProcessor()));
//				result.put("ClockFrequencyCore", String.valueOf(SystemInfo.getClockFrequencyCore()));
//				result.put("CpuTemperature", String.valueOf(SystemInfo.getCpuTemperature()));
//				result.put("CpuVoltage", String.valueOf(SystemInfo.getCpuVoltage()));
//				result.put("MemoryTotal", String.valueOf(SystemInfo.getMemoryTotal()));
//				result.put("MemoryUsed", String.valueOf(SystemInfo.getMemoryUsed()));
//				result.put("MemoryFree", String.valueOf(SystemInfo.getMemoryFree()));
//			} 
//			catch (Exception e) {
//				log.error(e.getMessage(), e);
//			}
		
			//Networkinfo
			try {
//				result.put("Hostname", String.valueOf(NetworkInfo.getHostname()));
//				result.put("IPAddress", String.valueOf(NetworkInfo.getIPAddress()));
				
				String str = sysCmd.executeCommandQuery(new String[]{"wpa_cli","list_network"});
				result.put("NetworkList", str);
			} catch (Exception e) {
				log.error(e.getMessage(), e);
			}
		}
				
		return result;
	}
}
