package es.fdvcode.pipool.srv.backup;

import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import es.fdvcode.pipool.PiPoolContext;
import es.fdvcode.pipool.common.ParameterizedMessage;
import es.fdvcode.pipool.common.SystemCommand;
import es.fdvcode.pipool.srv.persist.PiPoolPeriodicTaskPersist;
import es.fdvcode.pipool.srv.scheduler.PiPoolScheduledTask;
import es.fdvcode.pipool.srv.scheduler.SchedulerUtil;
import lombok.RequiredArgsConstructor;

/**
 * 
 * @author cfarrema
 *
 */
@Component
@RequiredArgsConstructor
public final class PiPoolScheduledTaskBackup implements PiPoolScheduledTask {

	protected final Logger log = LoggerFactory.getLogger(this.getClass());

	@Value("${pipool.scheduler.backup.active}")
	private boolean active;

	@Value("${pipool.scheduler.backup.hora}")
	private int hora;
	
	@Value("${pipool.scheduler.backup.minut}")
	private int minut;
	
	private final PiPoolContext ctx;
	private final SystemCommand sysCmd;
	final PiPoolPeriodicTaskPersist persister;
	private final SchedulerUtil schedUtil;
	private final PiPoolScheduledTaskReboot taskReboot;
	
	@Override
	public void run() {
		log.info("{} Initiated", this.getClass().getSimpleName());
		ctx.setDateLastExecutionSchedulerBackup(new Date());

		//Executa la persistencia de fitxers abans de fer backup
		persister.run();
		
		//BACKUP
		String script = "./pushchanges.sh";
		try {
			String strOut = sysCmd.runScript(script);
			log.info("RESULTAT Execució SystemCommand [{}] -> {}", script, strOut);
		} 
		catch (Exception e) {
			ParameterizedMessage msg = new ParameterizedMessage("Error al executar script [{}] -> {}.", script, e.getMessage()); 
			log.error(msg.getFormattedMessage());
			log.error("ERROR RUN PiPoolScheduledTaskBackup. Continua l'execució...", e);
		}
		
		//Planifica el REBOOT PI en un minut
		schedUtil.startScheduledTaskDelayed(taskReboot, 120);		
	}
	
	@Override
	public boolean isActive() {
		return active;
	}

	@Override
	public int getHora() {
		return hora;
	}

	@Override
	public int getMinut() {
		return minut;
	}

	@Override
	public TipusScheduler getTipus() {
		return TipusScheduler.BACKUP;
	}
}
