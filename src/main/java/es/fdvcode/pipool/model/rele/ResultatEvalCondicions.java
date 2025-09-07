package es.fdvcode.pipool.model.rele;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(Include.NON_NULL)
public class ResultatEvalCondicions {

	private boolean resultatOK;
	private String motiu;
	private RuleCondicio condIncomplerta;
	
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

	public String getMotiu() {
		return this.motiu;
	}

	public boolean isResultatOK() {
		return this.resultatOK;
	}
}
