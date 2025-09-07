package es.fdvcode.pipool.srv.scheduler;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import es.fdvcode.pipool.srv.backup.PiPoolScheduledTaskBackup;
import es.fdvcode.pipool.srv.persist.PiPoolPeriodicTaskPersist;
import es.fdvcode.pipool.srv.rele.PiPoolPeriodicTaskRele;
import es.fdvcode.pipool.srv.scheduler.PiPoolSchedulerTask.TipusScheduler;
import es.fdvcode.pipool.srv.sonda.PiPoolPeriodicTaskSonda;
import lombok.RequiredArgsConstructor;

/**
 * 
 * @author cfarrema
 *
 */
@Component
@RequiredArgsConstructor
public final class SchedulerSrv {

	private final Logger log = LoggerFactory.getLogger(this.getClass());
	
	private final SchedulerUtil schedulerUtil;
	private final PiPoolPeriodicTaskRele taskRele;
	private final PiPoolPeriodicTaskSonda taskSonda;
	private final PiPoolPeriodicTaskPersist taskPersist;
	private final PiPoolScheduledTaskBackup taskBackup;

	/**
	 * 
	 * @param tipus
	 */
	public void startActiveSchedulers() {
		if(taskRele.isActive()){
			this.startPeriodicTaskRele();
		}

		if(taskSonda.isActive()){
			this.startPeriodicTaskSonda();
		}

		if(taskPersist.isActive()){
			this.startPeriodicTaskPersist();
		}
		
		if(taskBackup.isActive()){
			this.startScheduledTaskBackup();
		}
	}


	/**
	 * 
	 * @param tipus
	 */
	public void startPeriodicTaskRele() {
		schedulerUtil.startPeriodicTask(this.taskRele, this.taskRele.getPeriodSeconds());
	}

	/**
	 * 
	 * @param tipus
	 */
	public void startPeriodicTaskSonda() {
		schedulerUtil.startPeriodicTask(this.taskSonda, this.taskSonda.getPeriodSeconds());
	}

	/**
	 * 
	 * @param tipus
	 */
	public void startPeriodicTaskPersist() {
		schedulerUtil.startPeriodicTask(this.taskPersist, this.taskPersist.getPeriodSeconds());
	}

	
	/**
	 * 
	 * @param period
	 */
	public void startPeriodicTask(TipusScheduler tipus, Integer period) {
		schedulerUtil.startPeriodicTask((PiPoolPeriodicTask)getTask(tipus), period);
	}
	
	private PiPoolSchedulerTask getTask(TipusScheduler tipus) {
		if(TipusScheduler.RELES.equals(tipus)) {
			return this.taskRele;			
		}
		else if(TipusScheduler.SONDES.equals(tipus)) {
			return this.taskSonda;
		}
		else if(TipusScheduler.PERSIST.equals(tipus)) {
			return this.taskPersist;
		}
		else if(TipusScheduler.BACKUP.equals(tipus)) {
			return this.taskBackup;
		}
		return null;
	}
	
	public ScheduledFuture<?> startTaskNow(TipusScheduler tipus) {
		log.info("Start Task Scheduler NOW {}.", tipus);
		ScheduledExecutorService ses = schedulerUtil.getMapSchedulers().get(tipus);
		if(ses!=null) {
			return ses.scheduleAtFixedRate(getTask(tipus), 0, 1, TimeUnit.MILLISECONDS);
		}
		return null;
	}
	
	/**
	 * 
	 * @param tipus
	 */
	public void startScheduledTaskBackup() {
		this.startScheduledTaskBackup(this.taskBackup.getHora(), this.taskBackup.getMinut());
	}	

	/**
	 * 
	 * @param tipus
	 */
	public void startScheduledTaskBackup(int hora, int minut) {
		schedulerUtil.startScheduledTask(this.taskBackup, hora, minut);
	}	
	

	public PiPoolPeriodicTask getTaskRele() {
		return taskRele;
	}


	public PiPoolPeriodicTaskSonda getTaskSonda() {
		return taskSonda;
	}


	public PiPoolPeriodicTaskPersist getTaskPersist() {
		return taskPersist;
	}

	public PiPoolScheduledTaskBackup getTaskBackup() {
		return taskBackup;
	}
}
