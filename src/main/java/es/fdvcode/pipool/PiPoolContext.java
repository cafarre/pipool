package es.fdvcode.pipool;

import java.util.Date;

import org.springframework.stereotype.Component;

/**
 * 
 * @author cfarrema
 *
 */
@Component
public class PiPoolContext {

	private Date dateAppStarted = new Date();
	private Date dateLastExecutionSchedulerRele;
	private Date dateLastExecutionSchedulerSonda;
	private Date dateLastExecutionSchedulerPersist;
	private Date dateLastExecutionSchedulerBackup;
	private Date dateLastExecutionSchedulerReboot;
	private Date dateLastExecutionSchedulerMqtt;

	public Date getDateAppStarted() {
		return dateAppStarted;
	}

	public String printDateAppStarted() {
		if(dateAppStarted!=null) {
			return dateAppStarted.toString();
		}
		else {
			return "";
		}
	}

	public Date getDateLastExecutionSchedulerRele() {
		return dateLastExecutionSchedulerRele;
	}

	public String printDateLastExecutionSchedulerRele() {
		if(dateLastExecutionSchedulerRele!=null){
			return dateLastExecutionSchedulerRele.toString();
		}
		else {
			return "";
		}
	}

	public void setDateLastExecutionSchedulerRele(Date dateLastExecutionSchedulerRele) {
		this.dateLastExecutionSchedulerRele = dateLastExecutionSchedulerRele;
	}

	public Date getDateLastExecutionSchedulerSonda() {
		return dateLastExecutionSchedulerSonda;
	}

	public String printDateLastExecutionSchedulerSonda() {
		if(dateLastExecutionSchedulerSonda!=null){
			return dateLastExecutionSchedulerSonda.toString();
		}
		else {
			return "";
		}
	}
	
	public void setDateLastExecutionSchedulerSonda(Date dateLastExecutionSchedulerSonda) {
		this.dateLastExecutionSchedulerSonda = dateLastExecutionSchedulerSonda;
	}

	public Date getDateLastExecutionSchedulerPersist() {
		return dateLastExecutionSchedulerPersist;
	}

	public String printDateLastExecutionSchedulerPersist() {
		if(dateLastExecutionSchedulerPersist!=null){
			return dateLastExecutionSchedulerPersist.toString();
		}
		else {
			return "";
		}
	}
	
	public void setDateLastExecutionSchedulerPersist(Date dateLastExecutionSchedulerPersist) {
		this.dateLastExecutionSchedulerPersist = dateLastExecutionSchedulerPersist;
	}
	
	public Date getDateLastExecutionSchedulerMqtt() {
		return dateLastExecutionSchedulerMqtt;
	}

	public String printDateLastExecutionSchedulerMqtt() {
		if(dateLastExecutionSchedulerMqtt!=null){
			return dateLastExecutionSchedulerMqtt.toString();
		}
		else {
			return "";
		}
	}
	
	public void setDateLastExecutionSchedulerMqtt(Date dateLastExecutionSchedulerMqtt) {
		this.dateLastExecutionSchedulerMqtt = dateLastExecutionSchedulerPersist;
	}

	public String printDateLastExecutionSchedulerBackup() {
		if(dateLastExecutionSchedulerBackup!=null){
			return dateLastExecutionSchedulerBackup.toString();
		}
		else {
			return "";
		}
	}
	
	public void setDateLastExecutionSchedulerBackup(Date dateLastExecutionSchedulerBackup) {
		this.dateLastExecutionSchedulerBackup = dateLastExecutionSchedulerBackup;
	}
	
	public String printDateLastExecutionSchedulerReboot() {
		if(dateLastExecutionSchedulerReboot!=null){
			return dateLastExecutionSchedulerReboot.toString();
		}
		else {
			return "";
		}
	}
	
	public void setDateLastExecutionSchedulerReboot(Date dateLastExecutionSchedulerReboot) {
		this.dateLastExecutionSchedulerReboot = dateLastExecutionSchedulerReboot;
	}
	
}
