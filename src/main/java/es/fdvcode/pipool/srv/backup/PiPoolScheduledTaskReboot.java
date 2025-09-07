package es.fdvcode.pipool.srv.backup;

import java.util.Collection;
import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import es.fdvcode.pipool.PiPoolContext;
import es.fdvcode.pipool.common.ObjPrinter;
import es.fdvcode.pipool.common.ParameterizedMessage;
import es.fdvcode.pipool.model.rele.Rele;
import es.fdvcode.pipool.model.rele.StateRele;
import es.fdvcode.pipool.srv.rele.RelesQuerySrv;
import es.fdvcode.pipool.srv.scheduler.PiPoolSchedulerTask;
import es.fdvcode.pipool.srv.scheduler.SchedulerUtil;
import lombok.RequiredArgsConstructor;

/**
 * 
 * @author cfarrema
 *
 */
@Component
@RequiredArgsConstructor
public final class PiPoolScheduledTaskReboot implements PiPoolSchedulerTask {

	protected final Logger log = LoggerFactory.getLogger(this.getClass());

	private final PiPoolContext ctx;
	private final RelesQuerySrv relesQuerySrv;
	private final SchedulerUtil schedUtil;
	
	@Override
	public void run() {
		log.info("{} Initiated", this.getClass().getSimpleName());
		ctx.setDateLastExecutionSchedulerReboot(new Date());

		//Comprova que no hi hagi cap rele activat temporalment o manualment
		Collection<Rele> list = relesQuerySrv.getSyncReles().values();
		for (Rele rele : list) {
			StateRele state = rele.getCopyStateRele(); 
			if(state.teActivacionsActives()) {
				
				//Replanifica per quan acabi o al cap d'una hora.
				Integer secToEnd = state.getSecondsToEnd();
				if(secToEnd!=null) {
					log.info("No es pot fer reboot, per rele {} conectat. Aplaçat fins d'aqui a {} segons.", rele.getId(), secToEnd);
					schedUtil.startScheduledTaskDelayed(this, secToEnd + 60);
				}
				else {
					log.info("No es pot fer reboot, per rele {} conectat. Aplaçat provisionalment fins d'aqui a 1 hora.", rele.getId());
					schedUtil.startScheduledTaskDelayed(this, 3600);
				}
				
				return;
			}
		}
		
		//REBOOT rPI
		String[] cmd = {"sudo reboot"};
		try {
			
			log.info("PREPARAT per Executar SystemCommand [{}] -> {}", cmd[0]);
			//Renici de la Pi
			Process p = Runtime.getRuntime().exec(cmd);

			log.info("RESULTAT Execució SystemCommand [{}] -> {}", cmd[0], ObjPrinter.printObj(p));
		} 
		catch (Exception e) {
			ParameterizedMessage msg = new ParameterizedMessage("Error al executar script [{}] -> {}.", cmd, e.getMessage()); 
			log.error(msg.getFormattedMessage());
			log.error("ERROR RUN PiPoolScheduledTaskReboot. Continua l'execució...", e);
		}
	}
	
	@Override
	public TipusScheduler getTipus() {
		return TipusScheduler.REBOOT;
	}

	@Override
	public boolean isActive() {
		return true;
	}
}
