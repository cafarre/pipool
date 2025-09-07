package es.fdvcode.pipool.restsrv.v1.response;

import java.util.Collection;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import es.fdvcode.pipool.model.rele.Rele;
import es.fdvcode.pipool.model.sonda.Sonda;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(Include.NON_NULL)
public class QuadreComandament {

	private Collection<Rele> infoReles;
	private Collection<Sonda> infoSondes;
	private Map<String, String> infoRpi;
	
	
	public Collection<Rele> getInfoReles() {
		return infoReles;
	}
	public void setInfoReles(Collection<Rele> infoReles) {
		this.infoReles = infoReles;
	}
	public Collection<Sonda> getInfoSondes() {
		return infoSondes;
	}
	public void setInfoSondes(Collection<Sonda> infoSondes) {
		this.infoSondes = infoSondes;
	}
	public Map<String, String> getInfoRpi() {
		return infoRpi;
	}
	public void setInfoRpi(Map<String, String> infoRpi) {
		this.infoRpi = infoRpi;
	}
}
