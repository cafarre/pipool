package es.fdvcode.pipool.model.rele;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(Include.NON_NULL)
public class ResultatEvalCondicions {

	private boolean resultatOK;
	private String motiu;
	private RuleCondicio condIncomplerta;
	private boolean ruleActivada;
	private List<RuleCondicio> condicionsActivacio;
	private List<RuleCondicio> condicionsDesactivacio;
	
	public ResultatEvalCondicions() {}
	
	public ResultatEvalCondicions(boolean resultatOK, String motiu) {
		this(resultatOK, motiu, null);
	}
	
	public ResultatEvalCondicions(boolean resultatOK, String motiu, RuleCondicio condIncomplerta) {
		this.resultatOK = resultatOK;
		this.motiu = motiu;
		this.condIncomplerta = condIncomplerta;
	}
	
	public RuleCondicio getCondicioIncomplerta() {
		return this.condIncomplerta;
	}

	public void setCondicioIncomplerta(RuleCondicio condIncomplerta) {
		this.condIncomplerta = condIncomplerta;
	}

	public String getMotiu() {
		return this.motiu;
	}

	public void setMotiu(String motiu) {
		this.motiu = motiu;
	}

	public boolean isResultatOK() {
		return this.resultatOK;
	}

	public void setResultatOK(boolean resultatOK) {
		this.resultatOK = resultatOK;
	}

	public boolean isRuleActivada() {
		return this.ruleActivada;
	}

	public void setRuleActivada(boolean ruleActivada) {
		this.ruleActivada = ruleActivada;
	}

	public List<RuleCondicio> getCondicionsActivacio() {
		return this.condicionsActivacio;
	}

	public void setCondicionsActivacio(List<RuleCondicio> condicionsActivacio) {
		this.condicionsActivacio = condicionsActivacio;
	}

	public List<RuleCondicio> getCondicionsDesactivacio() {
		return this.condicionsDesactivacio;
	}

	public void setCondicionsDesactivacio(List<RuleCondicio> condicionsDesactivacio) {
		this.condicionsDesactivacio = condicionsDesactivacio;
	}
}
