package es.fdvcode.pipool.model.rele;

import java.time.ZoneId;
import java.util.Date;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import es.fdvcode.pipool.srv.persist.Persistible;

/**
 * 
 * @author cfarrema
 *
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(Include.NON_NULL)
public class StateRele implements Cloneable {

	public enum ModeRele {AUTO, MANUAL}
	public enum TipusCausaState {ON, OFF, OTHERS}
	public enum CausaState {
		INIT(TipusCausaState.OTHERS),
		SYNCGPIO(TipusCausaState.OTHERS), //??
		LOAD_HISTORY(TipusCausaState.OTHERS),
		ON(TipusCausaState.ON),
		ON_TEMP(TipusCausaState.ON),
		ON_MASTER(TipusCausaState.ON),
		ON_RULE(TipusCausaState.ON),
		ON_HA(TipusCausaState.ON),
		OFF(TipusCausaState.OFF),
		OFF_TEMP(TipusCausaState.OFF),
		OFF_MASTER(TipusCausaState.OFF),
		OFF_SHUTDOWN(TipusCausaState.OFF),
		OFF_HAOFFLINE(TipusCausaState.OFF),
		RESET_CONSUM(TipusCausaState.OTHERS),
		CHANGE_MODE(TipusCausaState.OTHERS);
		
	    
	    private TipusCausaState type;
	 
	    private CausaState (TipusCausaState type) {
	        this.type = type;
	    }
	 
	    public TipusCausaState getType() {
	        return type;
	    }
	}
	
	
	@JsonIgnore
	private Rele rele;
	
	private String idRele;
	
	//Estat Rele
	private boolean isOn=false;

	//Estat GPIO
	private boolean gpioPinHigh=false;

	//Mode
	private ModeRele mode=ModeRele.AUTO;

	//CausaState
	private CausaState causa=CausaState.INIT;
	
	//Timestamp ON/OFF
	private Date timestamp = new Date();
	
	//Descripcio
	private String descripcio;
	
	//Estat Activacions AUTO: Programades / Temporals / Rule
	private FranjaHoraria activacioTemporal=null;
	private FranjaHoraria activacioProgramada=null;
	private RuleRele activacioRule=null;

	//Activador Rele Master
	private ActivadorReles activadorReleMaster=null;
	
	//Flags
	private boolean desactivacioReleMaster=false;
	private boolean activacioRuleActiva=false;
	
	//Consum
	private double consumRele;
	
	/**
	 * default
	 */
	public StateRele() {}


	/**
	 * 
	 * @param rele
	 * @param isOn
	 * @param mode
	 * @param desactivacioReleMaster
	 * @param descripcio
	 */
	public StateRele(Rele rele, boolean isOn, ModeRele mode, CausaState causa, String descripcio, Double consumRele) {
		this.rele = rele;
		this.idRele = rele.getId();
		this.setOn(isOn);
		
		this.mode = mode;
		this.causa = causa;
		this.descripcio = descripcio;
		this.consumRele = consumRele;
	}

	public StateRele(
			Rele rele, 
			boolean isOn, 
			ModeRele mode, 
			CausaState causa,
			String descripcio, 
			Double consumRele, 
			Date timestamp) {
		this(rele, isOn, mode, causa, descripcio, consumRele);
		this.timestamp = timestamp;
	}
	
	/*
	 * SETTER's: ATENCIO: només son per preparar nous objectes, si es manipula un objecte existent no tindrà efecte sobre Pipool, doncs el getStateRele sempre retorna un clone.
	 */
	public void setOn(boolean isOn) {
		this.isOn = isOn;
		this.gpioPinHigh = rele.calcGpioPinFromReleOn(isOn);
		this.timestamp = new Date();
	}

	public void setMode(ModeRele mode) {
		this.mode = mode;
	}
	
	public void setCausa(CausaState causa) {
		this.causa = causa;
	}	

	public void setDescripcio(String descripcio) {
		this.descripcio = descripcio;
	}

	public void setDesactivacioReleMaster(boolean desactivacioReleMaster) {
		this.desactivacioReleMaster = desactivacioReleMaster;
	}
	
	public void setActivadorReleMaster(ActivadorReles activadorReleMaster) {
		this.activadorReleMaster = activadorReleMaster;
	}
	
	public void setActivacioTemporal(FranjaHoraria activacioTemporal) {
		this.activacioTemporal = activacioTemporal;
	}

	public void setActivacioProgramada(FranjaHoraria activacioProgramada) {
		this.activacioProgramada = activacioProgramada;
	}	
	public void setActivacioRule(RuleRele activacioRule) {
		this.activacioRule = activacioRule;
		this.activacioRuleActiva = (activacioRule!=null);
	}
	
	public void syncGpioPin(boolean GpioPinisHigh) {
		boolean releOnObjectiu = this.rele.calcReleOnFromGpioPin(GpioPinisHigh);
		
		if(releOnObjectiu != this.isOn()) {
			this.setOn(releOnObjectiu);
		}
	}
	
	public void setConsumRele(double consumRele) {
		this.consumRele = consumRele;
	}	
	
	
	/*
	 * GETTERs
	 */
	public String getIdRele() {
		return this.idRele;	
	}

	public boolean isOn() {
		return this.isOn;
	}

	public Rele getRele() {
		return this.rele;
	}

	public boolean isGpioPinHigh() {
		return this.gpioPinHigh;
	}

	public ModeRele getMode() {
		return this.mode;
	}

	public CausaState getCausa() {
		return this.causa;
	}
	
	public Date getTimestamp() {
		return this.timestamp;
	}

	public boolean isDesactivacioReleMaster() {
		return this.desactivacioReleMaster;
	}
	
	public ActivadorReles getActivadorReleMaster() {
		return this.activadorReleMaster;
	}
	
	public FranjaHoraria getActivacioTemporal() {
		return this.activacioTemporal;
	}

	public FranjaHoraria getActivacioProgramada() {
		return this.activacioProgramada;
	}
	
	public RuleRele getActivacioRule() {
		return this.activacioRule;
	}
	public boolean isActivacioRuleActiva() {
		return this.activacioRuleActiva;
	}
	
	
	public String getDescripcio() {
		return this.descripcio;
	}
	
	

	public double getConsumRele() {
		return this.consumRele;
	}


	/**
	 * 
	 * @return
	 */
	public boolean teActivacioTemporal() {
		return this.activacioTemporal!=null;
	}
	
	/**
	 * 
	 * @return
	 */
	public boolean teActivacioTemporalActiva() {
		return teActivacioTemporal() && this.activacioTemporal.isActivada();
	}

	/**
	 * 
	 * @return
	 */
	public boolean teActivacioProgramada() {
		return this.activacioProgramada!=null;
	}
	
	/**
	 * 
	 * @return
	 */
	public boolean teActivacioProgramadaActiva() {
		return this.teActivacioProgramada() && this.activacioProgramada.isActivada();
	}

	/**
	 * 
	 * @return
	 */
	public boolean teActivacioRule() {
		return this.activacioRule!=null;
	}

	public boolean isActivacioReleMaster() {
		return this.activadorReleMaster!=null;
	}
	
	/**
	 * 
	 * @return
	 */
	public boolean teActivacioRuleActiva() {
		return this.teActivacioRule() && this.activacioRuleActiva && this.activacioRule.isActivateOnTrue();
	}
	
	@JsonIgnore
	public boolean isDesactivacioManual() {
		return ModeRele.MANUAL.equals(this.mode) && !this.isOn;
	}

	@JsonIgnore
	public boolean isActivacioManual() {
		return ModeRele.MANUAL.equals(this.mode) && this.isOn;
	}

	/**
	 * Retorna els segons que falten per posar en off el rele. Retorna 'null' si es indeterminat o està parat.
	 * 
	 * @return
	 */
	public Integer getSecondsToEnd() {
		Integer result = null;
		if(this.activacioTemporal!=null) {
			result = this.activacioTemporal.getSecondsToEnd();
		}

		if(this.activacioProgramada!=null) {
			Integer secProg = this.activacioProgramada.getSecondsToEnd();
			if(result==null || (secProg.intValue() > result.intValue())) {
				result = secProg;
			}
		}
		
		return result;
	}

	public Date getDateNext() {
		
		ProximaFranja prox = this.rele.getProximaFranjaActiva();
		
		if(prox!=null) {
			return Date.from(prox.getFutureDate().atZone(ZoneId.systemDefault()).toInstant());
		}
		return null;
	}	

	
	/**
	 * 
	 * @return
	 */
	public boolean teActivacions() {
		
		boolean desactivacioForzada = this.isDesactivacioManual() || 
				this.isDesactivacioReleMaster();
		
		boolean teAlgunaActivacio = this.isActivacioManual() || 
				this.teActivacioTemporal() || 
				this.teActivacioProgramada() || 
				this.teActivacioRule() ||
				this.isActivacioReleMaster(); 
		
		return  !desactivacioForzada && teAlgunaActivacio;
	}
	
	/**
	 * 
	 * @return
	 */
	public boolean teActivacionsActives() {
		
		boolean desactivacioForzada = this.isDesactivacioManual() || 
				this.isDesactivacioReleMaster();
		
		boolean teAlgunaActivacio = this.isActivacioManual() || 
				this.teActivacioTemporalActiva() || 
				this.teActivacioProgramadaActiva() ||
				this.teActivacioRuleActiva() || 
				this.isActivacioReleMaster(); 
		
		return  !desactivacioForzada && teAlgunaActivacio;
	}

	public boolean teActivacioDeReleSlaves() {
		return 	((this.getActivacioProgramada()!=null && this.getActivacioProgramada().isActivarRelesSlaves()) 
				|| (this.getActivacioRule()!=null && this.getActivacioRule().isActivarRelesSlaves())
				|| (this.getActivacioTemporal()!=null && this.getActivacioTemporal().isActivarRelesSlaves()));

	}
	
	@Override
	protected StateRele clone() {
		try {
			return (StateRele) super.clone();
		} catch (CloneNotSupportedException e) {
			return this;
		}
	}

	@Override
	public String toString() {
		return "StateRele: " + this.rele.getId();
	}


	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((rele == null) ? 0 : rele.hashCode());
		result = prime * result + ((timestamp == null) ? 0 : timestamp.hashCode());
		return result;
	}


	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		
		if (obj == null) {
			return false;
		}
		
		if (getClass() != obj.getClass()) {
			return false;
		}
		
		StateRele other = (StateRele) obj;
		if (rele == null) {
			if (other.rele != null) {
				return false;
			}
		} 
		else {
			if(other.rele==null) {
				return false;
			}
			else if (!rele.getId().equals(other.rele.getId())) {
				return false;
			}
		}
		
		if (timestamp == null) {
			if (other.timestamp != null) {
				return false;
			}
		} 
		else {
			if(other.timestamp==null) {
				return false;
			}
			
			String strTim = Persistible.dateFormat.format(timestamp);
			String strTimOther = Persistible.dateFormat.format(other.timestamp);
			if (!strTim.equals(strTimOther)) {
				return false;
			}
		}
		
		
		return true;
	}
	
}
