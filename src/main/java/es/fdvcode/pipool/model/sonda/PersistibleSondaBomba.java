package es.fdvcode.pipool.model.sonda;

import java.util.Date;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * 
 * @author cfarrema
 *
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class PersistibleSondaBomba {

	private String idSonda;
	private Date timestamp;
	private String valor;
	private Boolean bombaOn;
	private Double consum;
	
	public PersistibleSondaBomba(PersistibleSonda sonda, boolean bombaOn) {
		this.idSonda=sonda.getIdSonda();
		this.valor = sonda.getValor();
		this.timestamp = sonda.getTimestamp();
		this.bombaOn = bombaOn;
	}
	
	public String getIdSonda() {
		return idSonda;
	}

	public String getValor() {
		return valor;
	}

	public Date getTimestamp() {
		return timestamp;
	}

	public Boolean getBombaOn() {
		return bombaOn;
	}
	
	public Double getConsum() {
		return consum;
	}

	public void setConsum(Double consum) {
		this.consum = consum;
	}

}