package es.fdvcode.pipool.model.rele;

import java.util.Date;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(Include.NON_NULL)
public class RuleRele implements ActivadorReles {

	private String id;
	private boolean activateOnTrue;
	private boolean activarRelesSlaves;
	private int segonsFinsProximaActivacio;
	
	private List<RuleCondicio> condicionsActivacio;
	private List<RuleCondicio> condicionsDesactivacio;
	
	private Date dtActivada;
	private Date dtAturada;
	
	private String idReleAplicar; //només per quan està dins una franja horaria

		
	public String getId() {
		return id;
	}
	public boolean isActivateOnTrue() {
		return activateOnTrue;
	}
	
	public boolean isActivarRelesSlaves() {
		return activarRelesSlaves;
	}
	
	public int getSegonsFinsProximaActivacio() {
		return segonsFinsProximaActivacio;
	}
	public List<RuleCondicio> getCondicionsActivacio() {
		return condicionsActivacio;
	}
	public List<RuleCondicio> getCondicionsDesactivacio() {
		return condicionsDesactivacio;
	}	
	
	public Date getDtActivada() {
		return dtActivada;
	}
	
	public Date getDtAturada() {
		return dtAturada;
	}

	public boolean isActivada() {
		return dtActivada!=null && dtAturada == null;
	}
	
	public String getIdReleAplicar() {
		return idReleAplicar;
	}
	
	public void activar() {
		this.dtActivada = new Date();
		this.dtAturada = null;
	}
	
	public void desactivar() {
		this.dtAturada = new Date();
	}	
	
	public String toString() {
		return "RuleRele: " + this.id;
	}
}
