package es.fdvcode.pipool.srv.persist;

import java.util.Collection;
import java.util.Date;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import es.fdvcode.pipool.PiPoolContext;
import es.fdvcode.pipool.model.rele.Rele;
import es.fdvcode.pipool.model.rele.StateRele;
import es.fdvcode.pipool.model.sonda.Sonda;
import es.fdvcode.pipool.mqtt.homeassistant.PipoolEntitiesMqttSrv;
import es.fdvcode.pipool.srv.rele.RelesPersister;
import es.fdvcode.pipool.srv.rele.RelesQuerySrv;
import es.fdvcode.pipool.srv.rele.RelesSrv;
import es.fdvcode.pipool.srv.scheduler.PiPoolPeriodicTask;
import es.fdvcode.pipool.srv.sonda.SondesPersister;
import es.fdvcode.pipool.srv.sonda.SondesQuerySrv;
import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;

/**
 * 
 * @author cfarrema
 *
 */
@Component
@RequiredArgsConstructor
public final class PiPoolPeriodicTaskPersist implements PiPoolPeriodicTask {

	protected final Logger log = LoggerFactory.getLogger(this.getClass());

	@Value("${pipool.scheduler.persist.active}")
	private boolean active;

	@Value("${pipool.scheduler.persist.initialDelay}")
	private int initialDelay;
	
	@Value("${pipool.scheduler.persist.periodSeconds}")
	private int periodSeconds;
	
	private final SondesPersister sondesPersister;
	private final RelesPersister relesPersister;
	private final RelesSrv relesSrv;
	private final RelesQuerySrv relesQuerySrv;
	private final SondesQuerySrv sondesSrv;
	private final PiPoolContext ctx;
	private final PipoolEntitiesMqttSrv pipoolMqtt;
	
	@Override
	public void run() {
		log.info("{} Initiated", this.getClass().getSimpleName());
		ctx.setDateLastExecutionSchedulerPersist(new Date());

		try {
			Collection<Sonda> listSondes = sondesSrv.getListSondes();
			sondesPersister.doPersistencia(listSondes);
			
			Collection<Rele> listRele = relesQuerySrv.getNoSyncReles().values();
			relesPersister.doPersistencia(listRele);
		} catch (Exception e) {
			log.error("ERROR RUN PiPoolPeriodicTaskPersist. Continua l'execució...", e);
		}
		
		publicaMqtt();
	}
	
	private void publicaMqtt() {
		pipoolMqtt.pubConfigAll();
		pipoolMqtt.pubStateAllReles();
		
		//pipoolMqtt.pubDeleteAll();
	}
	
	@Override
	public boolean isActive() {
		return active;
	}

	@Override
	public int getInitialDelay() {
		return initialDelay;
	}

	@Override
	public int getPeriodSeconds() {
		return periodSeconds;
	}

	@Override
	public TipusScheduler getTipus() {
		return TipusScheduler.PERSIST;
	}
	
	/**
	 * destroy
	 */
	@PreDestroy
    public void destroy() {
		log.info("Tancant RELES de forma controlada.");
	
        Map<String, Rele> mapReles = relesQuerySrv.getNoSyncReles();
		for(Rele rele : mapReles.values()) {
			StateRele state = rele.getCopyStateRele();
			if(state.isOn()) {
				relesSrv.setStateShutdown(state);
			}
		}

        log.info("Tancant PiPool PERSISTER de forma controlada.");
        this.run();
    }	
}
