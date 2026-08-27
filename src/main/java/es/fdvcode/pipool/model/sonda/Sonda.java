package es.fdvcode.pipool.model.sonda;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * 
 * @author cfarrema
 *
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class Sonda{

	public enum TipusSonda {Atlas, rPi};
	
	//Definicio
	private String id;
	private String nom;
	private String unitats;
	private String haDeviceClass;
	private int ordre = 0;
	
	//tipus
	private TipusSonda tipusSonda = TipusSonda.Atlas;
	private int address;
	private String idReleCorrector;
	private Double minValor;
	private Double maxValor;

	//Estat Actual Sonda
	@JsonIgnore
	private StateSonda stateSonda;

	@JsonIgnore
	private List<StateSonda> historic=Collections.synchronizedList(new ArrayList<>());
	
	/**
	 * default (requerido por Jackson per a deserealització JSON)
	 */
	public Sonda() {}
	
	public Sonda(String id, String nom, TipusSonda tipusSonda, String unitats, String haDeviceClass, int address, int ordre, Double minValor, Double maxValor) {
		super();
		this.id = id;
		this.nom = nom;
		this.unitats = unitats;
		this.haDeviceClass = haDeviceClass;
		this.tipusSonda = tipusSonda;
		this.address = address;
		this.ordre = ordre;
		this.minValor = minValor;
		this.maxValor = maxValor;
	}
	
	public String getId() {
		return id;
	}
	public String getNom() {
		return nom;
	}

	public String getUnitats() {
		return unitats;
	}
	
	public String getHaDeviceClass() {
		return haDeviceClass;
	}

	public TipusSonda getTipusSonda() {
		return tipusSonda;
	}

	public int getAddress() {
		return address;
	}
	public int getOrdre() {
		return ordre;
	}	
	public String getIdReleCorrector() {
		return idReleCorrector;
	}

	public Double getMinValor() {
		return minValor;
	}

	public void setMinValor(Double minValor) {
		this.minValor = minValor;
	}

	public Double getMaxValor() {
		return maxValor;
	}

	public void setMaxValor(Double maxValor) {
		this.maxValor = maxValor;
	}

	public boolean isReadingValid(String strValor) {
		if (strValor == null || strValor.trim().isEmpty()) {
			return false;
		}
		try {
			double val = Double.parseDouble(strValor.trim());
			if (minValor != null && val < minValor) {
				return false;
			}
			if (maxValor != null && val > maxValor) {
				return false;
			}
			return true;
		} catch (NumberFormatException e) {
			return false;
		}
	}

	@JsonProperty("stateSonda")
	public synchronized StateSonda getStateSonda() {
		if(this.stateSonda==null) {
			return null;
		}
		else {
			return this.stateSonda.clone();
		}
	}
	
	public synchronized void setLecturaSonda(String valor, boolean saveHistoric) {
		this.stateSonda = new StateSonda(this, valor);
		if(saveHistoric) {
			this.historic.add(stateSonda);
		}
	}
	
	/**
	 * 
	 * @return
	 */
	public synchronized List<StateSonda> extractHistoric() {
		List<StateSonda> list = this.historic;
		this.historic = new ArrayList<>();
		return list;
	}
	
	public synchronized List<StateSonda> getHistoric() {
		return historic;
	}
	
	public synchronized void setHistoric(List<StateSonda> historic) {
		this.historic = historic;
	}
	
}
