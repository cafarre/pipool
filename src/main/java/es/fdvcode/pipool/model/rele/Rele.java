package es.fdvcode.pipool.model.rele;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.fasterxml.jackson.annotation.JsonProperty;

import es.fdvcode.pipool.model.rele.StateRele.CausaState;
import es.fdvcode.pipool.model.rele.StateRele.ModeRele;
import es.fdvcode.pipool.model.rele.StateRele.TipusCausaState;

/**
 * 
 * @author cfarrema
 *
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(Include.NON_NULL)
public class Rele{
	//Definicio
	private String id;
	private String nom;
	private String mqttType="SWITCH";
	private boolean mqttEnabled=true;
	private boolean mqttConsumSensorEnabled=true;
	private int gpioPin;
	private boolean gpioStateWhenReleOn; //Indica en quin estat del Gpio el Rele està en posició ON.
	private String idReleMaster; //Optional
	private boolean masterOnObligatori; //Indica si per activar aquest rele, el rele master ha d'estar actiu.
	private int ordre=0;
	private Integer secondsDuradaCicles;
	private String unitatConsumHora;
	private double consumHora;
	
	//DefinicioCalendaris
	@JsonManagedReference
	private List<CalendarRele> calendars;
	
	private boolean rulesOn=true;
	private List<RuleRele> rules;
	
	//Estat Rele
	@JsonIgnore
	private StateRele stateRele=newInitialStateRele();
	
	@JsonIgnore
	private List<StateRele> historicPendent=Collections.synchronizedList(new ArrayList<>());

	@JsonIgnore
	private List<StateRele> historic=Collections.synchronizedList(new ArrayList<>());

	private ResultatEvalCondicions ultimResultatEvalCondicions; 
	
	/**
	 * default
	 */
	public Rele() {}
	
	/**
	 * 
	 * @param id
	 * @param nom
	 * @param gpioPin
	 * @param gpioStateWhenReleOn
	 * @param idReleMaster
	 */
	public Rele(String id, String nom, int gpioPin, boolean gpioStateWhenReleOn, String idReleMaster, int ordre, String mqttType, boolean mqttEnabled, boolean mqttConsumSensorEnabled) {
		super();
		this.id = id;
		this.nom = nom;
		this.mqttType = mqttType;
		this.mqttEnabled = mqttEnabled;
		this.mqttConsumSensorEnabled = mqttConsumSensorEnabled;
		this.ordre=ordre;
		this.gpioPin = gpioPin;
		this.gpioStateWhenReleOn = gpioStateWhenReleOn;
		this.idReleMaster=idReleMaster;
		this.historicPendent.add(stateRele);
	}
	
	public String getId() {
		return id;
	}
	public String getNom() {
		return nom;
	}

	public String getMqttType() {
		return mqttType;
	}

	public boolean isMqttEnabled() {
		return mqttEnabled;
	}

	public boolean isMqttConsumSensorEnabled() {
		return mqttConsumSensorEnabled;
	}

	public int getOrdre() {
		return ordre;
	}	
	
	public int getGpioPin() {
		return gpioPin;
	}

	public boolean getGpioStateWhenReleOn() {
		return gpioStateWhenReleOn;
	}

	public String getIdReleMaster() {
		return idReleMaster;
	}

	public List<CalendarRele> getCalendars() {
		if(this.calendars==null) {
			this.calendars = new ArrayList<>();
		}
		return this.calendars;
	}
	
	public void setCalendars(List<CalendarRele> calendars) {
		this.calendars = calendars;
	}
	
	public List<RuleRele> getRules() {
		return rules;
	}
	
	public boolean isRulesOn() {
		return rulesOn;
	}

	public Integer getSecondsDuradaCicles() {
		if(secondsDuradaCicles==null) {
			return 1;
		}
		return secondsDuradaCicles;
	}

	public void setSecondsDuradaCicles(Integer secondsDuradaCicles) {
		this.secondsDuradaCicles = secondsDuradaCicles;
	}

	public String getUnitatConsumHora() {
		return unitatConsumHora;
	}

	public Double getConsumHora() {
		return consumHora;
	}

	public boolean isMasterOnObligatori() {
		return masterOnObligatori;
	}

	@JsonProperty("stateRele")
	public synchronized StateRele getCopyStateRele() {
		if(this.stateRele==null) {
			return newInitialStateRele();
		}
		else {
			return this.stateRele.clone();
		}		
	}
	
//	@JsonIgnore
//	public synchronized StateRele noCopyStateRele() {
//		return this.stateRele;
//	}
	
	@JsonIgnore
	private StateRele newInitialStateRele() {
		return new StateRele(this, false, ModeRele.AUTO, CausaState.INIT, "Estat inicial arranc Pipool", 0.0);
	}
	
	public StateRele setNewStateRele(StateRele oldstate, CausaState causa, String descripcio, double consum) {
		StateRele newState = new StateRele(this, oldstate.isOn(), oldstate.getMode(), causa, descripcio, consum);
		
		modifStateRele(oldstate, newState);
		
		this.stateRele = newState;
		this.addHistoric();
		
		return newState;
		
	}
	
	public StateRele setNewStateRele(StateRele oldstate, CausaState causa, String descripcio) {
		return this.setNewStateRele(oldstate, causa, descripcio, this.getConsumTotalRele());
	}

	public void updateStateRele(StateRele newState) {
		this.modifStateRele(newState, this.stateRele);
	}
	
	private void modifStateRele(StateRele stateFrom, StateRele stateTo) {
		stateTo.setActivacioProgramada(stateFrom.getActivacioProgramada());
		stateTo.setActivacioTemporal(stateFrom.getActivacioTemporal());
		stateTo.setActivacioRule(stateFrom.getActivacioRule());
		
		stateTo.setActivadorReleMaster(stateFrom.getActivadorReleMaster());
		stateTo.setDesactivacioReleMaster(stateFrom.isDesactivacioReleMaster());
	}

	private synchronized void addHistoric() {
		
		StateRele newState = this.getCopyStateRele();
		
		this.historic.add(newState);
		this.historicPendent.add(newState);
		
		purgaHistoric();
	}

	private synchronized void purgaHistoric() {
		Calendar avui = Calendar.getInstance();
		avui.set(Calendar.HOUR_OF_DAY, 0);
		avui.set(Calendar.MINUTE, 0);
		avui.set(Calendar.SECOND, 0);
		avui.set(Calendar.MILLISECOND, 0);
		avui.add(Calendar.DAY_OF_MONTH, -2);
		
		List<StateRele> list = Collections.synchronizedList(new ArrayList<>());
		for(StateRele state : this.historic) {
			if(state.getTimestamp().getTime() >= avui.getTimeInMillis()) {
				list.add(state);
			}
		}
		
		this.historic = list;
	}
	
	/**
	 * 
	 * @param isOn
	 * @return
	 */
	public boolean calcGpioPinFromReleOn(boolean isOn) {
		return !(isOn ^ this.getGpioStateWhenReleOn());
	}

	/**
	 * 
	 * @param GpioPinisHigh
	 * @return
	 */
	public boolean calcReleOnFromGpioPin(boolean GpioPinisHigh) {
		return !(GpioPinisHigh ^ this.getGpioStateWhenReleOn()); 
	}
	
	/**
	 * 
	 * @return
	 */
	public synchronized List<StateRele> extractHistoricPendent() {
		List<StateRele> list = this.historicPendent;
		this.historicPendent = Collections.synchronizedList(new ArrayList<>());
		return list;
	}
	
	public synchronized List<StateRele> getHistoric() {
		return historic;
	}
	
	public synchronized void setHistoric(List<StateRele> list) {
		if(list==null) return;
		
		this.historic.clear();
		
		for(StateRele state : list) {
			this.historic.add(state);	
		}
		
		this.stateRele.setConsumRele(this.getConsumAcumulatHistoric());
		
		//Afageix l'estat actual del Rele (estat INIT) en el historic, així quedarà registrat cada vegada que s'ha iniciat pipool i evitem problemes si es para quan està ON.
		this.addHistoric();
	}

	
	@JsonIgnore
	public ProximaFranja getProximaFranjaActiva() {

		List<ProximaFranja> listProximes = new ArrayList<>();
		for(CalendarRele cal : this.getCalendars()) {
			ProximaFranja prox = cal.getProximaFranjaActiva();
			listProximes.add(prox);
		}
		Collections.sort(listProximes, (p1,p2)-> {
			return Integer.valueOf(p1.getSecondsToStartFranja()).compareTo(p2.getSecondsToStartFranja());
		});
		
		if(listProximes.isEmpty()) {
			return null;
		}
		else {
			return listProximes.get(0);
		}
	}
	
	public int getSecondsActivatAvui() {
		Calendar avui = Calendar.getInstance();
		avui.set(Calendar.HOUR_OF_DAY, 0);
		avui.set(Calendar.MINUTE, 0);
		avui.set(Calendar.SECOND, 0);
		avui.set(Calendar.MILLISECOND, 0);

		return this.calcSecondsActivat(avui);
	}
	
	public int calcSecondsActivat(Calendar dataMinima) {
		
		int res = 0;
		Date timeIni=null;
		for(StateRele state : this.historic) {
			if(state.getTimestamp().getTime() < dataMinima.getTimeInMillis()) {
				continue;
			}
			
			if(state.isOn()) {
				if(timeIni==null) {
					timeIni = state.getTimestamp();
				}
			}
			else {
				if(timeIni!=null) {
					Date timeFin = state.getTimestamp();
					int diff = (int)(timeFin.getTime() - timeIni.getTime()) / 1000;
					
					res = res + diff;
					timeIni=null;
				}
			}
		}
		
		//Si encara esta engegat
		if(timeIni!=null) {
			Date timeFin = new Date();
			int diff = (int)(timeFin.getTime() - timeIni.getTime()) / 1000;
			
			res = res + diff;
		}
		
		return res;
	}
	
	public Double getConsumUltimaActivacio() {
		StateRele state = this.getCopyStateRele();
		if(state.isOn()) {
			Calendar cal = Calendar.getInstance();
			cal.setTime(state.getTimestamp());
			
			return calcConsum(cal);
		}
		else {
			StateRele stateHist = calcLastActivacioHistory();
			if(stateHist == null) {
				return 0.0;
			}

			Calendar cal = Calendar.getInstance();
			cal.setTime(stateHist.getTimestamp());
			
			return calcConsum(cal);
		}
	}
	
	public Double getConsumPendentConsolidar() {
		StateRele state = this.getCopyStateRele();
		if(state.isOn()) {
			Calendar cal = Calendar.getInstance();
			cal.setTime(state.getTimestamp());
			
			return calcConsum(cal);
		}
		else {
			StateRele stateHist = calcLastDesactivacioHistory();
			if(stateHist == null) {
				return 0.0;
			}

			Calendar cal = Calendar.getInstance();
			cal.setTime(stateHist.getTimestamp());
			
			return calcConsum(cal);
		}
	}	
	
	public Double getConsumTotalRele() {
		Double consumHist = this.getConsumAcumulatHistoric(); 
		Double consumAct = this.getConsumPendentConsolidar();
		
		return consumHist + consumAct;
	}
	
	public Double getConsumAvui() {
		Calendar avui = Calendar.getInstance();
		avui.set(Calendar.HOUR_OF_DAY, 0);
		avui.set(Calendar.MINUTE, 0);
		avui.set(Calendar.SECOND, 0);
		avui.set(Calendar.MILLISECOND, 0);
		
		return this.calcConsum(avui);
	}
	
	public Double calcConsum(Calendar dataMinima) {
		
		int segonsActivat = calcSecondsActivat(dataMinima);
		
		double consumSegon = this.consumHora / 3600;
		double res = segonsActivat * consumSegon;
		
		return res;
	}
	
	public Double getConsumAcumulatHistoric() {
		return calcConsumAcumulatHistoric(this.historic.size()-1);
	}
	
	private Double calcConsumAcumulatHistoric(int index) {
		if(this.historic!=null && this.historic.size() > index && index >=0) {
			StateRele state = this.historic.get(index);
			if(state == null) {
				return 0.0;
			}
//			else if(state.getCausa().getType().equals(TipusCausaState.ON) || state.getCausa().getType().equals(TipusCausaState.OFF)) {
//				return state.getConsumRele();
//			}
			else {
				//return calcConsumAcumulatHistoric(index-1);
				return state.getConsumRele();
			}
		}
		return 0.0;
	}
	
	/**
	 * Obté l'ultim estat d'activació de l'històric
	 * @return
	 */
	public StateRele calcLastActivacioHistory() {
		return calcLastActivacioHistory(this.historic.size()-1);
	}

	public StateRele calcLastActivacioHistory(int index) {
		if(this.historic!=null && this.historic.size() > index  && index >= 0) {
			StateRele state = this.historic.get(index);
			if(state.isOn() && state.getCausa().getType().equals(TipusCausaState.ON)) {
				return state;
			}
			else {
				return calcLastActivacioHistory(index-1);
			}
		}
		return null;
	}
	
	public StateRele calcLastDesactivacioHistory() {
		return calcLastDesactivacioHistory(this.historic.size()-1);
	}
	
	public StateRele calcLastDesactivacioHistory(int index) {
	    if (this.historic == null || index < 0) {
	        return null;
	    }
	    for (int i = index; i >= 0 && i < this.historic.size(); i--) {
	        StateRele state = this.historic.get(i);
	        if (state != null && !state.isOn() && state.getCausa().getType().equals(TipusCausaState.OFF)) {
	            return state;
	        }
	    }
	    return null;
	}
	
	public String toString() {
		return "Rele: " + this.id;
	}

	public ResultatEvalCondicions getUltimResultatEvalCondicions() {
		return ultimResultatEvalCondicions;
	}

	public void setUltimResultatEvalCondicions(ResultatEvalCondicions ultimResultatEvalCondicions) {
		this.ultimResultatEvalCondicions = ultimResultatEvalCondicions;
	}
}
