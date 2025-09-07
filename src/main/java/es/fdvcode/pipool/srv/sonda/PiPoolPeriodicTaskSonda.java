package es.fdvcode.pipool.srv.sonda;

import java.util.Date;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import es.fdvcode.pipool.PiPoolContext;
import es.fdvcode.pipool.common.ParameterizedMessage;
import es.fdvcode.pipool.model.sonda.Sonda;
import es.fdvcode.pipool.srv.scheduler.PiPoolPeriodicTask;
import es.fdvcode.pipool.srv.sonda.atlasi2c.SondaAtlasErrorException;
import lombok.RequiredArgsConstructor;

/**
 * 
 * @author cfarrema
 *
 */
@Component
@RequiredArgsConstructor
public final class PiPoolPeriodicTaskSonda implements PiPoolPeriodicTask {

	protected final Logger log = LoggerFactory.getLogger(this.getClass());
	
	public static float lastTemperatura=4.0f;
	
	@Value("${pipool.scheduler.sondes.active}")
	private boolean active;

	@Value("${pipool.scheduler.sondes.initialDelay}")
	private int initialDelay;
	
	@Value("${pipool.scheduler.sondes.periodSeconds}")
	private int periodSeconds;
	
	private final SondesSrv sondesSrv;
	private final SondesQuerySrv sondesQuerySrv;
	private final PiPoolContext ctx;
	
	@Override
	public void run() {
		log.debug("{} Initiated", this.getClass().getSimpleName());
		ctx.setDateLastExecutionSchedulerSonda(new Date());

		try {
			List<Sonda> list = sondesQuerySrv.getListSondes();

			for (Sonda sonda : list) {
				try {
					sondesSrv.readSonda(sonda, true);
					
					if("SondaTemp".equals(sonda.getId())) {
						String val = sonda.getStateSonda().getValor();
						lastTemperatura = Float.parseFloat(val);
					}
				} 
				catch (SondaAtlasErrorException e) {
					log.error("ERROR de lectura de la SONDA Atlas:{}. CodiError:{} - IsErrorExpected:{}.", sonda.getId(), e.getResponseCode(), e.isReponseCodeExpected(), e);
				}
				catch (NumberFormatException | UnsupportedOperationException | InterruptedException e) {
					log.error("ERROR al tractar la SONDA rPi:{}.", sonda.getId(), e);
				}
				catch (Exception e) {
					log.error("ERROR al tractar la SONDA:{}.", sonda.getId(), e);
				}
			}
			
		} catch (Exception e) {
			log.error("ERROR RUN PiPoolPeriodicTaskSonda. Continua l'execucio...", e);
		}
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
		return TipusScheduler.SONDES;
	}
}
