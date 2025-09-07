package es.fdvcode.pipool.srv.scheduler;

public interface PiPoolPeriodicTask extends PiPoolSchedulerTask {

	int getPeriodSeconds();

	int getInitialDelay();
}