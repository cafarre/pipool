package es.fdvcode.pipool.srv.scheduler;

public interface PiPoolScheduledTask extends PiPoolSchedulerTask {

	int getHora();

	int getMinut();
}