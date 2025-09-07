package es.fdvcode.pipool.model.rele;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

/**
 * 
 * @author cfarrema
 *
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(Include.NON_NULL)
public class FranjaHoraria implements ActivadorReles {

	public enum TipusFranja {Normal, Calculada, Dinamica}
		
	protected TipusFranja tipus = TipusFranja.Normal;
	protected boolean activarRelesSlaves = false;
	protected int horaIni;
	protected int minutIni;
	protected int secondIni;
	//protected boolean[] diesFranjaHoraria = {false, false, false, false, false, false, false};
	protected int duracioSeconds; 		//No informar per tipus calculada
	protected int duracioMinima=1770; 	//Nomes informar per tipus calculada, per defecta 30 minuts - 30 segons
	protected List<RuleRele> rules = new ArrayList<>();
	protected Date dtActivada;
	
	
		
	/**
	 * default
	 * Is empty
	 */
	public FranjaHoraria(){}

	/**
	 * Només té sentit per la inicialització hardcode (sense fitxer conf)
	 * 
	 * @param horaIni
	 * @param minutIni
	 * @param secondIni
	 * @param duracio - en minuts
	 */
	public FranjaHoraria(int horaIni, int minutIni, int secondIni, int duracioSeconds) {
		this(horaIni, minutIni, secondIni, duracioSeconds, false);
	}

	
	/**
	 * Només té sentit per les Temporals
	 * 
	 * @param horaIni
	 * @param minutIni
	 * @param secondIni
	 * @param duracio - en minuts
	 */
	public FranjaHoraria(int horaIni, int minutIni, int secondIni, int duracioSeconds, boolean activarRelesSlaves) {
		this.horaIni = horaIni;
		this.minutIni = minutIni;
		this.secondIni = secondIni;
		this.duracioSeconds = duracioSeconds;
		this.activarRelesSlaves = activarRelesSlaves;
	}

	

	public TipusFranja getTipus() {
		return tipus;
	}

	public int getHoraIni() {
		return horaIni;
	}
	public int getMinutIni() {
		return minutIni;
	}

	public int getSecondIni() {
		return secondIni;
	}

	public int getDuracioSeconds() {
		return duracioSeconds;
	}

	public int getDuracioMinima() {
		return duracioMinima;
	}

	public Date getDtActivada() {
		return dtActivada;
	}

	public boolean isActivarRelesSlaves() {
		return activarRelesSlaves;
	}
	
	@JsonIgnore
	public boolean isActivada() {
		return getSecondsToEnd()>=0;
	}

	public List<RuleRele> getRules() {
		return rules;
	}

	public void setDuracioSeconds(int duracioSeconds) {
		if(this.duracioMinima <  duracioSeconds) {
			this.duracioSeconds = duracioSeconds;
		}
		else {
			this.duracioSeconds = this.duracioMinima;
		}
	}
	
	public void setDtActivada(Date dtActivada) {
		this.dtActivada = dtActivada;
	}
	
	
	
//	public boolean[] getDiesFranjaHoraria() {
//		return diesFranjaHoraria;
//	}
//
//	public boolean getDiaFranjaHoraria(int diaSemana) {
//		return diaSemana >=0 && diaSemana < diesFranjaHoraria.length && diesFranjaHoraria[diaSemana];
//	}

	
	@JsonIgnore
	/**
	 * 
	 * @return minuts que falten per acabar una franja actualment activa. Si no esta activa retorna -1.
	 */
	public Integer getSecondsToEnd() {
		
		Calendar calIni;
		if(this.dtActivada == null) {
			calIni = Calendar.getInstance();
			calIni.set(Calendar.HOUR_OF_DAY, horaIni);
			calIni.set(Calendar.MINUTE, minutIni);
			calIni.set(Calendar.SECOND, secondIni);
			calIni.set(Calendar.MILLISECOND, 0);
		}
		else {
			calIni = Calendar.getInstance();
			calIni.setTime(this.dtActivada);
		}
		
		Calendar calFin = Calendar.getInstance();
		calFin.setTime(calIni.getTime());
		calFin.add(Calendar.SECOND, getDuracioSeconds());
		
		Calendar calNow = Calendar.getInstance();
		
		//int diaSem = LocalDate.now().getDayOfWeek().getValue();
//		if(calIni.getTimeInMillis() <= calNow.getTimeInMillis() && 
//				calNow.getTimeInMillis() <= calFin.getTimeInMillis() &&
//				this.getDiaFranjaHoraria(diaSem)) {
		if(calIni.getTimeInMillis() <= calNow.getTimeInMillis() && calNow.getTimeInMillis() <= calFin.getTimeInMillis()) {
			
			long diff = calFin.getTimeInMillis() - calNow.getTimeInMillis();
	        int diffSeconds = (int) diff / 1000;
	        
	        this.dtActivada = calIni.getTime();
	        
			return diffSeconds;
		}
		else {
			this.dtActivada=null;
			return -1;
		}
	}

	@JsonIgnore
	public String getId() {
		return Integer.toString(horaIni) + ":" + 
				Integer.toString(minutIni) + ":" + 
				Integer.toString(secondIni) + "-->" + 
				Integer.toString(getDuracioSeconds()/60) + ":" + 
				Integer.toString(getDuracioSeconds()%60);
	}

	@Override
	public String toString() {
		return this.getId();
	}
}
