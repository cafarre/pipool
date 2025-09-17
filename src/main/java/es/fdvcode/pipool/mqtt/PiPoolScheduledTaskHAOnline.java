package es.fdvcode.pipool.mqtt;

import java.io.IOException;
import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import es.fdvcode.pipool.common.SystemCommand;
import es.fdvcode.pipool.model.rele.Rele;
import es.fdvcode.pipool.model.rele.StateRele;
import es.fdvcode.pipool.model.rele.StateRele.CausaState;
import es.fdvcode.pipool.model.rele.StateRele.ModeRele;
import es.fdvcode.pipool.mqtt.homeassistant.HomeAssistantMqttSubscriber;
import es.fdvcode.pipool.srv.persist.PiPoolPeriodicTaskPersist;
import es.fdvcode.pipool.srv.rele.RelesQuerySrv;
import es.fdvcode.pipool.srv.rele.RelesSrv;
import es.fdvcode.pipool.srv.scheduler.PiPoolPeriodicTask;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;

/**
 * 
 * @author cfarrema
 *
 */
@Component
@RequiredArgsConstructor
public final class PiPoolScheduledTaskHAOnline implements PiPoolPeriodicTask {

	protected final Logger log = LoggerFactory.getLogger(this.getClass());

	@Value("${pipool.scheduler.haonline.active}")
	private boolean active;
	
	@Value("${pipool.scheduler.haonline.periodSeconds}")
	private int periodSeconds;

	@Value("${pipool.scheduler.haonline.initialDelay}")
	private int initialDelay;
	
	@Value("${pipool.scheduler.haonline.max-minutes-offline}")
	private int maxMinutesOffline;
	
	@Value("${pipool.scripts.restart}")
	private String script_restart;

	private final HomeAssistantMqttSubscriber mqtt;
	private final RelesSrv relesSrv;
	private final RelesQuerySrv relesQuerySrv;
	private final PiPoolPeriodicTaskPersist persister;
	private final SystemCommand sysCmd;

	
	@PostConstruct
	public void run() {
		
		ScheduledExecutorService ses = Executors.newSingleThreadScheduledExecutor();
		Runnable runGetToken = new Runnable() {
			@Override
			public void run() {
				log.info("{} Initiated", this.getClass().getSimpleName());

				try {
					Calendar limit = Calendar.getInstance();
					limit.setTime(mqtt.getHeartbeatHA());
					limit.add(Calendar.MINUTE, maxMinutesOffline);
					
					//Si fa una hora que no tenim batec de HA
					if(limit.getTimeInMillis() < new Date().getTime()) {
						log.info("FA {} MINUTS QUE NO HI HA CAP HEARTBEAT DE HA. Ultim: {}", maxMinutesOffline, mqtt.getHeartbeatHA());
						pararBombesEngegadesPerHA();
						
						//Reiniciem per si s'ha quedat enganxat MQTT
						reiniciaApp();
					}
					
				} catch (Exception e) {
					log.error("ERROR RUN PiPoolScheduledTaskHAOnline. Continua l'execució...", e);
				}
				
				if(active) {
					ses.schedule(this, periodSeconds, TimeUnit.SECONDS);
				}
			}
		};

		ses.schedule(runGetToken, initialDelay, TimeUnit.SECONDS);
	}
	
	private void pararBombesEngegadesPerHA() {
		for(Rele rele : relesQuerySrv.getSyncReles().values()) {
			StateRele state = rele.getCopyStateRele();
			if(state.isOn() && state.getCausa().equals(CausaState.ON_HA)) {
				relesSrv.setStateHA(state, false, ModeRele.AUTO);
			}
		}
	}
	
	private void reiniciaApp() {
		log.info("Restart PiPool App.");
		try {
			//Persisteix en fitxer
			persister.run();
			
			//Reboot App
			sysCmd.runScript(script_restart);
		} 
		catch (IOException | InterruptedException e) {
			log.error("ERROR al fer Restart de la App PiPool.", e);
		}
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
