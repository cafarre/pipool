package es.fdvcode.pipool.model.rele;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(Include.NON_NULL)
public class RuleCondicio {

	public enum OperandCondicio {E, NE, G, GE, L, LE};
	public enum TipusDatoCondicio {Sonda, RuleCondicioInfo, Rele};
	public enum DatoCondicio {
		temp_cpu_rpi(TipusDatoCondicio.Sonda),
		sonda_ph(TipusDatoCondicio.Sonda),
		sonda_orp(TipusDatoCondicio.Sonda),
		sonda_temp(TipusDatoCondicio.Sonda),
		DuracioSeconds(TipusDatoCondicio.RuleCondicioInfo),
		VolumMlInjectats(TipusDatoCondicio.RuleCondicioInfo),
		VolumMlAcumulatDia(TipusDatoCondicio.RuleCondicioInfo),
		SegonsReleActivat(TipusDatoCondicio.RuleCondicioInfo),
		SegonsReleParat(TipusDatoCondicio.RuleCondicioInfo),
		SegonsLecturaSonda(TipusDatoCondicio.RuleCondicioInfo),
		HoraActual(TipusDatoCondicio.RuleCondicioInfo),
		SegonsPiPoolArrancat(TipusDatoCondicio.RuleCondicioInfo),
		ExcedentsSolars(TipusDatoCondicio.RuleCondicioInfo),
		ExcedentsTimestampNivell1(TipusDatoCondicio.RuleCondicioInfo),
		ExcedentsTimestampNivell2(TipusDatoCondicio.RuleCondicioInfo),
		ImportacioXarxa(TipusDatoCondicio.RuleCondicioInfo),
		ImportacioXarxaTimestampNivell(TipusDatoCondicio.RuleCondicioInfo),
		rele_bomba(TipusDatoCondicio.Rele),
		rele_lfi(TipusDatoCondicio.Rele),
		rele_fan(TipusDatoCondicio.Rele),
		rele_llums(TipusDatoCondicio.Rele),
		rele_llums_jardi(TipusDatoCondicio.Rele),
		rele_bomba_clor(TipusDatoCondicio.Rele),
		rele_bomba_acid(TipusDatoCondicio.Rele);
		
	    
	    private TipusDatoCondicio type;
	 
	    private DatoCondicio(TipusDatoCondicio type) {
	        this.type = type;
	    }
	 
	    public TipusDatoCondicio getType() {
	        return type;
	    }
	}
	
	//Definicio
	private DatoCondicio dato;
	private List<ParamDato> paramsDato;
	
	private OperandCondicio operand;
	private String valor;
		
	//Valors dinamics calculats en runtime
	private String valorDatoCondicio;
	private boolean cumpleCondicio;

	
	public DatoCondicio getDato() {
		return dato;
	}
	public List<ParamDato> getParamsDato() {
		return paramsDato;
	}

	public OperandCondicio getOperand() {
		return operand;
	}
	public String getValor() {
		return valor;
	}

	public String getValorDatoCondicio() {
		return valorDatoCondicio;
	}
	public void setValorDatoCondicio(String valorDatoCondicio) {
		this.valorDatoCondicio = valorDatoCondicio;
	}
	
	public boolean isCumpleCondicio() {
		return cumpleCondicio;
	}
	public void setCumpleCondicio(boolean cumpleCondicio) {
		this.cumpleCondicio = cumpleCondicio;
	}
	
	public ParamDato getParamDato(int pos) {
		if(this.paramsDato==null) {
			return null;
		}
		
		if((pos + 1) <= this.paramsDato.size()) {
			return this.paramsDato.get(pos);
		}
		else {
			return null;
		}
	}

	public ParamDato getParamDato(String key) {
		if(this.paramsDato==null) {
			return null;
		}
		
		Map <String, ParamDato> map = new HashMap<>();
		for (ParamDato param : this.paramsDato) {
			map.put(param.getCamp(), param);
		}

		return map.get(key);
	}
	
	public String toString() {
		String dato = this.dato.toString();
		if(this.paramsDato!=null && this.paramsDato.size() > 0) {
			dato = dato + "[" + this.paramsDato.get(0) + "]";
		}
		return "RuleCondicio: " + dato + " --> (" + this.valorDatoCondicio + " " + this.operand + " " + this.valor + ")";
	}
}
