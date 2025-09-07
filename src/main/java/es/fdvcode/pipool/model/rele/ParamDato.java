package es.fdvcode.pipool.model.rele;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(Include.NON_NULL)
public class ParamDato {
	private String camp;
	private String valor;
	
	public ParamDato() {}

	public ParamDato(String camp, String valor) {
		this.camp=camp;
		this.valor=valor;
	}
	
	public String getCamp() {
		return camp;
	}
	public String getValor() {
		return valor;
	}
	
	public String toString() {
		return this.camp + "-" + this.valor;
	}
}