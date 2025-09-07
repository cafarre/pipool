package es.fdvcode.pipool.model.rele;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import es.fdvcode.pipool.model.rele.FranjaHoraria.TipusFranja;
import es.fdvcode.pipool.srv.sonda.PiPoolPeriodicTaskSonda;

/**
 * 
 * @author cfarrema
 *
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class CalendarRele {

	protected String id;
	protected String nom;
	
	@JsonBackReference
	protected Rele rele;
	
	protected int diaIni;
	protected int mesIni;
	protected int diaFin;
	protected int mesFin;
		
	protected List<FranjaHoraria> listFrangesHoraries = new ArrayList<>();
	
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getNom() {
		return nom;
	}
	public void setNom(String nom) {
		this.nom = nom;
	}
	public Rele getRele() {
		return rele;
	}
	public void setRele(Rele rele) {
		this.rele = rele;
	}

	public int getDiaIni() {
		return diaIni;
	}
	public void setDiaIni(int diaIni) {
		this.diaIni = diaIni;
	}
	public int getMesIni() {
		return mesIni;
	}
	public void setMesIni(int mesIni) {
		this.mesIni = mesIni;
	}
	public int getDiaFin() {
		return diaFin;
	}
	public void setDiaFin(int diaFin) {
		this.diaFin = diaFin;
	}
	public int getMesFin() {
		return mesFin;
	}
	public void setMesFin(int mesFin) {
		this.mesFin = mesFin;
	}

	public List<FranjaHoraria> getListFrangesHoraries() {
		return listFrangesHoraries;
	}
	public void setListFrangesHoraries(List<FranjaHoraria> listFrangesHoraries) {
		this.listFrangesHoraries = listFrangesHoraries;
	}
	
	
	@JsonIgnore
	public boolean isActiu() {
		
		LocalDateTime dateNow = getDataActual();
		LocalDateTime dateIni = LocalDateTime.of(dateNow.getYear(), mesIni, diaIni, 0, 1);
		LocalDateTime dateFin = LocalDateTime.of(dateNow.getYear(), mesFin, diaFin, 23, 59);

		//Contemplar canvi de any!!
		if(dateIni.compareTo(dateFin) > 0) {
			if(dateNow.compareTo(dateFin) > 0) {
				dateFin=dateFin.plusYears(1);
			}
			else {
				dateIni=dateFin.minusYears(1);
			}
		}		

		if(dateIni.compareTo(dateNow) <= 0 && dateNow.compareTo(dateFin) <= 0) {
			return true;
		}
		return false;
	}
	
	@JsonIgnore
	public FranjaHoraria getFranjaActiva() {
		FranjaHoraria activa = null;
		if(this.isActiu()) {
			int seconds = calculaSecondsDuradaCicle();
			for(FranjaHoraria fr : this.getListFrangesHoraries()) {
				if(TipusFranja.Calculada.equals(fr.getTipus())) {
					fr.setDuracioSeconds(seconds);
				}
				if(fr.isActivada()) {
					activa = fr;
				}
			}
		}
		return activa;
	}
	
	private int calculaSecondsDuradaCicle() {
		float temp = getCurrentTemp();
		//int seconds = (int) (temp / 10 * duradaCicles / numCicles);
		
		int duradaDiariaTotal = (int) ((temp / 10) * this.rele.getSecondsDuradaCicles());
		int duradaDiariaCalculada = duradaDiariaTotal - getSecondsTotalsCalendar();
		
		int seconds = 0;
		if(this.getNumFranjesCalculades() > 0 ) {
			seconds = duradaDiariaCalculada / this.getNumFranjesCalculades();
		}

		int duradaConsumida = this.rele.getSecondsActivatAvui();
		if(duradaConsumida + seconds >  duradaDiariaTotal) {
			seconds = duradaDiariaTotal - duradaConsumida;
		}
		
		return seconds;
	}
	
	private float getCurrentTemp() {
		return PiPoolPeriodicTaskSonda.lastTemperatura;
	}
	
	
	@JsonIgnore
	public ProximaFranja getProximaFranjaActiva() {

		LocalDateTime dateNow = getDataActual();
		LocalDateTime dateIni = getDataActual();
		if(!this.isActiu()) {
			dateIni = LocalDateTime.of(dateNow.getYear(), this.mesIni, this.diaIni, dateNow.getHour(), dateNow.getMinute());
			if(dateNow.compareTo(dateIni) > 0) {
				dateIni=dateIni.plusYears(1);
			}
		}
		
		//TODO: pendent tenir en compte els dies de la setmana pels que està activa una Franja
		
		List<ProximaFranja> listProximes = new ArrayList<>();
		for(FranjaHoraria fr : this.getListFrangesHoraries()) {
			ProximaFranja prox = new ProximaFranja(fr, dateIni);
			listProximes.add(prox);
		}
		Collections.sort(listProximes, (p1,p2)-> {
			return p1.getSecondsToStartFranja().compareTo(p2.getSecondsToStartFranja());
		});
		
		if(listProximes.isEmpty()) {
			return null;
		}
		else {
			return listProximes.get(0);
		}
	}
	
	@JsonIgnore
	public LocalDateTime getDataActual() {
		return LocalDateTime.now();
	}
	
	@JsonIgnore
	public int getSecondsTotalsCalendar() {
		int seconds = 0;
		
		if(this.isActiu()) {
			for(FranjaHoraria fr : this.getListFrangesHoraries()) {
				if(TipusFranja.Normal.equals(fr.tipus)) {
					seconds = seconds + fr.getDuracioSeconds();
				}
			}
		}
		
		return seconds;
	}
	
	@JsonIgnore
	public int getNumFranjesCalculades() {
		int num = 0;
		
		if(this.isActiu()) {
			for(FranjaHoraria fr : this.getListFrangesHoraries()) {
				if(TipusFranja.Calculada.equals(fr.tipus)) {
					num++;
				}
			}
		}
		
		return num;
	}	

	public String toString() {
		return "CalendarRele: " + this.id;
	}
}
