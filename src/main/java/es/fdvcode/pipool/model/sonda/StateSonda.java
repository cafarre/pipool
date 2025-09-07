package es.fdvcode.pipool.model.sonda;

import java.util.Date;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * 
 * @author cfarrema
 *
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class StateSonda implements Cloneable {

	@JsonIgnore
	private Sonda sonda;
	
	private String valor;
	private Date timestamp;
	
	/**
	 * default
	 */
	public StateSonda() {}
	
	/**
	 * 
	 * @param sonda
	 */
	public StateSonda(Sonda sonda, String valor) {
		this(sonda, valor, new Date());
	}

	public StateSonda(Sonda sonda, String valor, Date timestamp) {
		this.sonda=sonda;
		this.valor = valor;
		this.timestamp = timestamp;
	}

	public String getValor() {
		return valor;
	}

	public Date getTimestamp() {
		return timestamp;
	}
	
	public String getStrTimestamp() {
		if(timestamp!=null) {
			return timestamp.toString();
		}
		return "";
	}
	
	

	public Sonda getSonda() {
		return sonda;
	}

	@Override
	protected StateSonda clone() {
		try {
			return (StateSonda) super.clone();
		} catch (CloneNotSupportedException e) {
			return this;
		}
	}
}