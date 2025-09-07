package es.fdvcode.pipool.mqtt;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import es.fdvcode.pipool.mqtt.homeassistant.PipoolEntitiesMqttSrv;
import es.fdvcode.pipool.srv.scheduler.PiPoolPeriodicTask;
import jakarta.annotation.PostConstruct;

/**
 * 
 * @author cfarrema
 *
 */
@Component
public final class PiPoolScheduledTaskMqtt implements PiPoolPeriodicTask {

	protected final Logger log = LoggerFactory.getLogger(this.getClass());

	@Value("${pipool.scheduler.mqtt.active}")
	private boolean active;
	
	@Value("${pipool.scheduler.mqtt.periodSeconds}")
	private int periodSeconds;

	@Value("${pipool.scheduler.haonline.initialDelay}")
	private int initialDelay;
	
	
	@Autowired
	private PipoolEntitiesMqttSrv pipoolMqtt;
		

	@PostConstruct
	public void run() {
		
		ScheduledExecutorService ses = Executors.newSingleThreadScheduledExecutor();
		Runnable runGetToken = new Runnable() {
			@Override
			public void run() {
				log.info("{} Initiated", this.getClass().getSimpleName());

				try {
					pipoolMqtt.pubStateLastStatus();
					
				} catch (Exception e) {
					log.error("ERROR RUN PiPoolScheduledTaskMqtt. Continua l'execució...", e);
				}
				
				if(active) {
					ses.schedule(this, periodSeconds, TimeUnit.SECONDS);
				}
			}
		};

		ses.schedule(runGetToken, initialDelay, TimeUnit.SECONDS);
	}
	
	
	@Override	
	public int getPeriodSeconds() {
		return periodSeconds;
	}

	@Override
	public boolean isActive() {
		return active;
	}

	@Override
	public TipusScheduler getTipus() {
		return TipusScheduler.MQTT;
	}

	@Override
	public int getInitialDelay() {
		return initialDelay;
	}
}
