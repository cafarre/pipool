package es.fdvcode.pipool.restsrv.v1;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ScheduledExecutorService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import es.fdvcode.pipool.common.ObjJsonPrinter;
import es.fdvcode.pipool.restsrv.v1.response.RestResponse;
import es.fdvcode.pipool.srv.scheduler.PiPoolPeriodicTask;
import es.fdvcode.pipool.srv.scheduler.PiPoolScheduledTask;
import es.fdvcode.pipool.srv.scheduler.PiPoolSchedulerTask.TipusScheduler;
import es.fdvcode.pipool.srv.scheduler.SchedulerSrv;
import es.fdvcode.pipool.srv.scheduler.SchedulerUtil;
import lombok.RequiredArgsConstructor;

/**
 * 
 * @author cfarrema
 *
 */
@RestController
@RequestMapping(SchedulerRestController.URIBASE)
@RequiredArgsConstructor
public class SchedulerRestController {

	protected final Logger log = LoggerFactory.getLogger(this.getClass());
	public static final String URIBASE = "api/v1/scheduler";

	private final SchedulerSrv schedulerSrv;
	private final SchedulerUtil schedulerUtil;
	
	final ObjJsonPrinter objJsonPrinter;
	
	/**
	 * 
	 * @return
	 */
	@GetMapping("/info")
	public RestResponse<Map<String, String>> info() {

		log.info("REST - Get Info Scheduler.");
		Map<String, String> result = this.getInfo();
		log.info("Scheduler INFO: {}", objJsonPrinter.print(result));

		return new RestResponse<>(result, HttpStatus.OK);
	}

	/**
	 * 
	 * @param periodSeconds
	 * @return
	 */
	@PutMapping("/start/rele")
	public RestResponse<Map<String, String>> startRele(@RequestParam(value="period") int periodSeconds) {

		log.info("REST - Start Scheduler RELEs amb Peridicitat de {} segons.", periodSeconds);
		schedulerSrv.startPeriodicTask(TipusScheduler.RELES,  periodSeconds);
		
		Map<String, String> result = this.getInfo();
		log.info("Scheduler RELE INFO: {}", objJsonPrinter.print(result));

		return new RestResponse<>(result, HttpStatus.OK);
	}
	
	/**
	 * 
	 * @return
	 */
	@PutMapping("/stop/rele")
	public RestResponse<Map<String, String>> stopRele() {

		log.info("REST - Stop Scheduler RELEs.");
		schedulerUtil.stopScheduler(TipusScheduler.RELES);

		Map<String, String> result = this.getInfo();
		log.info("Scheduler RELE INFO: {}", objJsonPrinter.print(result));

		return new RestResponse<>(result, HttpStatus.OK);
	}
	
	/**
	 * 
	 * @param periodSeconds
	 * @return
	 */
	@PutMapping("/start/sonda")
	public RestResponse<Map<String, String>> startSonda(@RequestParam(value="period") int periodSeconds) {

		log.info("REST - Start Scheduler SONDAs amb Peridicitat de {} segons.", periodSeconds);
		schedulerSrv.startPeriodicTask(TipusScheduler.SONDES,  periodSeconds);
		
		Map<String, String> result = this.getInfo();
		log.info("Scheduler SONDA INFO: {}", objJsonPrinter.print(result));

		return new RestResponse<>(result, HttpStatus.OK);
	}
	
	/**
	 * 
	 * @return
	 */
	@PutMapping("/stop/sonda")
	public RestResponse<Map<String, String>> stopSonda() {

		log.info("REST - Stop Scheduler SONDAs.");
		schedulerUtil.stopScheduler(TipusScheduler.SONDES);

		Map<String, String> result = this.getInfo();
		log.info("Scheduler SONDA INFO: {}", objJsonPrinter.print(result));

		return new RestResponse<>(result, HttpStatus.OK);
	}
	
	/**
	 * 
	 * @param periodSeconds
	 * @return
	 */
	@PutMapping("/start/persist")
	public RestResponse<Map<String, String>> startPersist(@RequestParam(value="period") int periodSeconds) {

		log.info("REST - Start Scheduler PERSIST amb Peridicitat de {} segons.", periodSeconds);
		schedulerSrv.startPeriodicTask(TipusScheduler.PERSIST,  periodSeconds);
		
		Map<String, String> result = this.getInfo();
		log.info("Scheduler PERSIST INFO: {}", objJsonPrinter.print(result));

		return new RestResponse<>(result, HttpStatus.OK);
	}
	
	/**
	 * 
	 * @return
	 */
	@PutMapping("/stop/persist")
	public RestResponse<Map<String, String>> stopPersist() {

		log.info("REST - Stop Scheduler PERSIST.");
		schedulerUtil.stopScheduler(TipusScheduler.PERSIST);

		Map<String, String> result = this.getInfo();
		log.info("Scheduler PERSIST INFO: {}", objJsonPrinter.print(result));

		return new RestResponse<>(result, HttpStatus.OK);
	}
	
	private Map<String, String> getInfo() {
		
		Map<TipusScheduler, ScheduledExecutorService> mapSES =  schedulerUtil.getMapSchedulers();
		
		Map<String, String> result = new LinkedHashMap<>();
		
		//RELES
		PiPoolPeriodicTask task = schedulerSrv.getTaskRele();
		result.put("Reles.isActive", String.valueOf(task.isActive()));
		result.put("Reles.InitialDelay", String.valueOf(task.getInitialDelay()));
		result.put("Reles.PeriodSeconds", String.valueOf(task.getPeriodSeconds()));
		
		ScheduledExecutorService ses = mapSES.get(TipusScheduler.RELES);
		if(ses!=null) {
			result.put("Reles.isTerminated", String.valueOf(ses.isTerminated()));
			result.put("Reles.isShutdown", String.valueOf(ses.isShutdown()));
		}
		
		//SONDES
		task = schedulerSrv.getTaskSonda();
		result.put("Sondes.isActive", String.valueOf(task.isActive()));
		result.put("Sondes.InitialDelay", String.valueOf(task.getInitialDelay()));
		result.put("Sondes.PeriodSeconds", String.valueOf(task.getPeriodSeconds()));

		ses = mapSES.get(TipusScheduler.SONDES);
		if(ses!=null) {
			result.put("Sondes.isTerminated", String.valueOf(ses.isTerminated()));
			result.put("Sondes.isShutdown", String.valueOf(ses.isShutdown()));
		}
		
		//PERSIST
		task = schedulerSrv.getTaskPersist();
		result.put("Persist.isActive", String.valueOf(task.isActive()));
		result.put("Persist.InitialDelay", String.valueOf(task.getInitialDelay()));
		result.put("Persist.PeriodSeconds", String.valueOf(task.getPeriodSeconds()));

		ses = mapSES.get(TipusScheduler.PERSIST);
		if(ses!=null) {
			result.put("Persist.isTerminated", String.valueOf(ses.isTerminated()));
			result.put("Persist.isShutdown", String.valueOf(ses.isShutdown()));
		}

		//BACKUP
		PiPoolScheduledTask taskS = schedulerSrv.getTaskBackup();
		result.put("Backup.isActive", String.valueOf(taskS.isActive()));
		result.put("Backup.Hora", String.valueOf(taskS.getHora()));
		result.put("Backup.Minut", String.valueOf(taskS.getMinut()));

		ses = mapSES.get(TipusScheduler.BACKUP);
		if(ses!=null) {
			result.put("Backup.isTerminated", String.valueOf(ses.isTerminated()));
			result.put("Backup.isShutdown", String.valueOf(ses.isShutdown()));
		}
		
		return result;
	}
}
