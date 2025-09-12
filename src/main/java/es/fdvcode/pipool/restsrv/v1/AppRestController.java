package es.fdvcode.pipool.restsrv.v1;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import es.fdvcode.pipool.PiPoolContext;
import es.fdvcode.pipool.common.ObjJsonPrinter;
import es.fdvcode.pipool.common.SystemCommand;
import es.fdvcode.pipool.restsrv.v1.response.QuadreComandament;
import es.fdvcode.pipool.restsrv.v1.response.RestResponse;
import es.fdvcode.pipool.restsrv.v1.sonda.SondaTempRestController;
import es.fdvcode.pipool.srv.persist.PiPoolPeriodicTaskPersist;

/**
 * 
 * @author cfarrema
 *
 */
@RestController
@RequestMapping(AppRestController.URIBASE)
public class AppRestController {

	protected final Logger log = LoggerFactory.getLogger(this.getClass());
	public static final String URIBASE = "api/v1/app";

	@Autowired
	private PiPoolContext piPoolCtx;

	@Autowired
	private SystemCommand sysCmd;

	@Autowired
	PiPoolPeriodicTaskPersist persister;
	
	@Autowired 
	RpiStateRestController restRpi;

	@Autowired 
	RelesRestController restReles;

	@Autowired 
	SondaTempRestController restSondes;
	
	@Autowired 
	ObjJsonPrinter objJsonPrinter;
	
	
	/**
	 * 
	 * @return
	 */
	@GetMapping("/info")
	public RestResponse<Map<String, String>> info() {

		log.info("REST - Get Info App PiPool.");
		Map<String, String> result = this.getInfo();
		log.info("App PiPool INFO: {}", objJsonPrinter.print(result));
		return new RestResponse<>(result, HttpStatus.OK);
	}

	/**
	 * 
	 * @return
	 */
	@GetMapping("/quadrecom")
	public RestResponse<QuadreComandament> quadrecom() {

		log.info("REST - Get Info QuadreComandament PiPool.");
		QuadreComandament result = new QuadreComandament();
		result.setInfoRpi(restRpi.info().getBody().getBody());
		result.setInfoReles(restReles.getAllInfo().getBody().getBody());
		result.setInfoSondes(restSondes.getAllSondes().getBody().getBody());
		
		return new RestResponse<>(result, HttpStatus.OK);
	}

	
	/**
	 * 
	 * @return
	 */
	@PutMapping("/restart")
	public RestResponse<Boolean> restart() {

		log.info("REST - Restart PiPool App.");
		try {
			//Persisteix en fitxer
			persister.run();
			
			//Reboot App
			sysCmd.runScript("./restart.sh");
			return new RestResponse<>(true, HttpStatus.OK);
		} 
		catch (IOException | InterruptedException e) {
			log.error("ERROR al fer Restart de la App PiPool.", e);
			return new RestResponse<>(false, HttpStatus.OK);
		}
	}

	/**
	 * 
	 * @return
	 */
	@PutMapping("/persist")
	public RestResponse<Boolean> persist() {

		log.info("REST - Persist Files PiPool App.");
		//Persisteix en fitxer
		persister.run();
		return new RestResponse<>(true, HttpStatus.OK);
	}


	/**
	 * 
	 * @return
	 */
	@PutMapping("/push")
	public RestResponse<Boolean> push() {

		log.info("REST - Pull&Push changes to GitHub.");
		try {
			//Persisteix en fitxer
			persister.run();
			
			//Pull & Push changes to GitHub
			String script = "./pushchanges.sh";
			String strOut = sysCmd.runScript(script);
			log.info("RESULTAT Execució SystemCommand [{}] -> {}", script, strOut);

			return new RestResponse<>(true, HttpStatus.OK);
		} 
		catch (IOException | InterruptedException e) {
			log.error("ERROR al fer pushchanges a GitHub.", e);
			return new RestResponse<>(false, HttpStatus.OK);
		}
	}

	/**
	 * 
	 * @return
	 */
	@PutMapping("/pull")
	public RestResponse<Boolean> pull() {

		log.info("REST - Pull changes from GitHub.");
		try {
			//Pull changes from GitHub
			String script = "./pullchanges.sh";
			String strOut = sysCmd.runScript(script);
			log.info("RESULTAT Execució SystemCommand [{}] -> {}", script, strOut);			
			return new RestResponse<>(true, HttpStatus.OK);
		} 
		catch (IOException | InterruptedException e) {
			log.error("ERROR al fer pullchanges from GitHub.", e);
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

		return result;
	}
}
