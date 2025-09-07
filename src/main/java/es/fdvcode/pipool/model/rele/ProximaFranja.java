package es.fdvcode.pipool.model.rele;

import java.time.Duration;
import java.time.LocalDateTime;

public class ProximaFranja {

	private LocalDateTime futureDate;
	private FranjaHoraria franja;
	
	public ProximaFranja(FranjaHoraria franja, LocalDateTime futureDate) {
		this.franja = franja;
		
		LocalDateTime timeNow = LocalDateTime.now();
        this.futureDate = futureDate.withHour(franja.getHoraIni()).withMinute(franja.getMinutIni()).withSecond(0);
        
        if(timeNow.compareTo(this.futureDate) > 0) {
        	this.futureDate = this.futureDate.plusDays(1);
        }
	}
	
	public LocalDateTime getFutureDate() {
		return futureDate;
	}
	public FranjaHoraria getFranja() {
		return franja;
	}
	
	public Integer getSecondsToStartFranja() {
		LocalDateTime timeNow = LocalDateTime.now();
        Duration duration = Duration.between(timeNow, this.futureDate);
        return (int)duration.getSeconds();
	}
}
