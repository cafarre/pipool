package es.fdvcode.pipool.model.sonda;

import java.text.ParseException;
import java.util.Date;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import es.fdvcode.pipool.srv.persist.Persistible;

/**
 * 
 * @author cfarrema
 *
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class PersistibleSonda implements Persistible {

	private String idSonda;
	private Date timestamp;
	private String valor;
	
	public PersistibleSonda(String lineaFile) {
		this.unmarshall(lineaFile);
	}
	
	public PersistibleSonda(String idSonda, String valor, Date timestamp) {
		this.idSonda=idSonda;
		this.valor = valor;
		this.timestamp = timestamp;
	}
	
	public String getIdSonda() {
		return idSonda;
	}

	public String getValor() {
		return valor;
	}

	@Override
	public Date getTimestamp() {
		return timestamp;
	}

	@Override
	public String marshall() {
		return idSonda + ";" + dateFormat.format(timestamp) + ";" + valor;
	}

	@Override
	public void unmarshall(String str) {
		String[] parts = str.split(";");
		this.idSonda=getValue(parts, 0);
		
		try {
			this.timestamp=dateFormat.parse(getValue(parts, 1));
		} catch (ParseException e) {}
		
		if(parts.length > 2) {
			this.valor=getValue(parts, 2);
		}
	}
	
	@Override
	public String toString() {
		return this.idSonda + ": " + this.timestamp + " - " + this.valor;
	}
}