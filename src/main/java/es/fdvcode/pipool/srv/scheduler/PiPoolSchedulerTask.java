package es.fdvcode.pipool.srv.scheduler;

public interface PiPoolSchedulerTask extends Runnable {

	public enum TipusScheduler {RELES, SONDES, PERSIST, BACKUP, REBOOT, MQTT}
	
	boolean isActive();

	TipusScheduler getTipus();

}