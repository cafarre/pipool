package es.fdvcode.pipool.srv.scheduler;

import static es.fdvcode.pipool.common.Delay.delay;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import es.fdvcode.pipool.srv.scheduler.PiPoolSchedulerTask.TipusScheduler;

/**
 * 
 * @author cfarrema
 *
 */
@Component
public final class SchedulerUtil {

	private final Logger log = LoggerFactory.getLogger(this.getClass());
	
	private Map<TipusScheduler, ScheduledExecutorService> mapSchedulers = new HashMap<>();

		
	/**
	 * 
	 * @param period
	 */
	public void startPeriodicTask(PiPoolPeriodicTask task, Integer period) {
		
		int initialDelay, periodSeconds;
		
		initialDelay = task.getInitialDelay();
		if(period!=null) {
			periodSeconds = period;
		}
		else {
			periodSeconds = task.getPeriodSeconds();
		}
		
		log.info("Start PeriodicTask Scheduler {} amb initialDelay={} y periodSeconds={}.", task.getTipus(), initialDelay, periodSeconds);
		ScheduledExecutorService ses = Executors.newSingleThreadScheduledExecutor();
		ses.scheduleAtFixedRate(task, initialDelay, periodSeconds, TimeUnit.SECONDS);
		mapSchedulers.put(task.getTipus(), ses);
	}
	
	public void startScheduledTaskDelayed(PiPoolSchedulerTask task, int secDelayed) {
		LocalTime time = LocalTime.now().plusSeconds(secDelayed);
		startScheduledTask(task, time.getHour(), time.getMinute());
	}
	
	/**
	 * 
	 * @param period
	 */
	public void startScheduledTask(PiPoolSchedulerTask task, int hora, int minut) {
		
		ScheduledExecutorService ses = Executors.newSingleThreadScheduledExecutor();
		long delay = computeNextDelay(hora, minut);
		
		log.info("Start ScheduledTask Scheduler {} amb hora={}, minut={} y delay={}.", task.getTipus(), hora, minut, delay);
        ses.schedule(()-> {
        		//Executa tasca backup
        		task.run();
        		
        		//Espera 1 segon
        		delay(1000);
        		
        		//Reporgrama la tasca per l'endema
        		startScheduledTask(task, hora, minut);
        	}, 
        	delay, 
        	TimeUnit.SECONDS);
		
		mapSchedulers.put(task.getTipus(), ses);
	}	
	
	/**
	 * stopScheduler
	 */
	public void stopScheduler(TipusScheduler tipus) {
		ScheduledExecutorService ses = this.mapSchedulers.get(tipus);
		
		if(ses != null) {
			ses.shutdown();
			
			try {
	            ses.awaitTermination(1, TimeUnit.DAYS);
	        } catch (InterruptedException ex) {
	            log.error(ex.getMessage(), ex);
	        }			
		}
		log.info("Scheduler {} Stopped.", tipus);
	}


	public Map<TipusScheduler, ScheduledExecutorService> getMapSchedulers() {
		return mapSchedulers;
	}
	
	
    private long computeNextDelay(int targetHour, int targetMin) 
    {
        LocalDateTime localNow = LocalDateTime.now();
        ZoneId currentZone = ZoneId.systemDefault();
        ZonedDateTime zonedNow = ZonedDateTime.of(localNow, currentZone);
        ZonedDateTime zonedNextTarget = zonedNow.withHour(targetHour).withMinute(targetMin).withSecond(0);
        if(zonedNow.compareTo(zonedNextTarget) > 0) {
            zonedNextTarget = zonedNextTarget.plusDays(1);
        }

        Duration duration = Duration.between(zonedNow, zonedNextTarget);
        return duration.getSeconds();
    }	
}
