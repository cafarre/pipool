package es.fdvcode.pipool.srv.rele;

import java.util.Calendar;
import java.util.Date;
import java.util.Map;

import org.springframework.stereotype.Component;

import es.fdvcode.pipool.PiPoolContext;
import es.fdvcode.pipool.model.rele.CalendarRele;
import es.fdvcode.pipool.model.rele.FranjaHoraria;
import es.fdvcode.pipool.model.rele.ParamDato;
import es.fdvcode.pipool.model.rele.Rele;
import es.fdvcode.pipool.model.rele.ResultatEvalCondicions;
import es.fdvcode.pipool.model.rele.RuleCondicio;
import es.fdvcode.pipool.model.rele.RuleCondicio.DatoCondicio;
import es.fdvcode.pipool.model.rele.RuleCondicio.OperandCondicio;
import es.fdvcode.pipool.model.rele.RuleCondicio.TipusDatoCondicio;
import es.fdvcode.pipool.model.rele.RuleRele;
import es.fdvcode.pipool.model.rele.StateRele;
import es.fdvcode.pipool.model.rele.StateRele.ModeRele;
import es.fdvcode.pipool.model.sonda.Sonda;
import es.fdvcode.pipool.model.sonda.StateSonda;
import es.fdvcode.pipool.srv.ItemNotFoundException;
import es.fdvcode.pipool.srv.sonda.SondesQuerySrv;
import lombok.RequiredArgsConstructor;

/**
 * 
 * TODO:
 *  
 * PDT - Permetre definir Rules de desactivació amb valors en funció de valor activació (a mes ORP llegit menys Clor injectat i viceversa)
 * PDT - Permetre programar xocs de Clor definits en calendari, però cumplin certs limits. 
 * PDT - Permetre simplificar definicio de Rules: Per horaris, moltes rules son identiques amb 40 o 50 linees i nomes canvia un valor! 
 *  
 * OK - aplicar un retard de 30 segons abans de començar a injectar liquids
 * OK - després de injectar liquids, no permetre parar bomba fins al cap de 20 minuts per que faci la mescla
 * OK - engegar lfi cada vegada que injecta liquids
 * OK - Asegurar que quan esta activat per rule nomes es revisen les condicions de desactivacio. Exemple: Si activo a ma el clor, s'activa el lfi (ok) però el filtre nomes s'activa 5 segons (?¿) 
 * OK - El rele lfi s'activa per rule, quan s'acaba la rule s'hauria de parar en comptes de quedar activat per master.
 * OK - Control volum injectat de una garrafa
 * OK - Permetre posar a 0 el volum injectat d'una garrafa
 * OK -- asegurar que abans de activar s'ha fet lectura sondes
 * OK -- a les 22:00 posar una mica mes de clor per la nit.
 * OK -- guardar en fitxer l'estat dels reles manuals. Si es reinicia la Pi cal conservar els reles en mode manual.
 * 
 * 
 * Possibles reques:
 * Pdt -- persistencia a cada injecció (separar en un fitxer per cada bomba liquid?)
 * Pdt -- si el valor ORP baixa molt (per exemple a la nit) activar bomba i injectar clor
 * Pdt -- pre avançar injecció de phminus abans de injectar clor
 * Pdt -- email alertes bomba
 * Pdt -- injecció de liquid calculat en funció dels nivells
 *  
 * Pdt -- bug calculo ON a les 0h
 * Pdt -- Permetre engegar temporal quan encara està activada una programació (potser es mes de android?)
 * 
 * @author farredevilar
 *
 */

@Component
@RequiredArgsConstructor
public class RuleReleEval {

	protected final SondesQuerySrv sondesSrv;
	protected final RelesQuerySrv relesQuerySrv;
	private final PiPoolContext piPoolCtx;


	/**
	 * Evalua les Rules i retorna la primera que compleix les condicions de activacio, en cas contrari retorna null.
	 * 
	 * @param rele
	 * @return
	 */
	public RuleRele calcRuleActivable(Rele rele) {
		if(rele==null || rele.getRules()==null || !rele.isRulesOn()) {
			return null;
		}
		
		//Rules de les franjes horaries activades en el moment actual
		boolean rulesDeFranjaActivaEvaluades = false;
		Map<String, Rele> mapReles = relesQuerySrv.getNoSyncReles();
		for(Rele item : mapReles.values()) {
			for(CalendarRele cal : item.getCalendars()) {
				for(FranjaHoraria fr : cal.getListFrangesHoraries()) {
					if(fr.isActivada()) {
						for(RuleRele rule: fr.getRules()) {
							if(rele.getId().equals(rule.getIdReleAplicar())) {
								ResultatEvalCondicions result = evalCompleixCondicionsActivacio(rule, rele, fr);
								rele.setUltimResultatEvalCondicions(result);
								if(result.isResultatOK()) {
									return rule;
								}
								else {
									rulesDeFranjaActivaEvaluades=true;
								}
							}
						}
					}
				}
			}
		}
		
		if(!rulesDeFranjaActivaEvaluades) {
			//Evalua les regles per buscar la primera que es compleixi
			for(RuleRele rule : rele.getRules()) {
				ResultatEvalCondicions result = evalCompleixCondicionsActivacio(rule, rele);
				rele.setUltimResultatEvalCondicions(result);
				if(result.isResultatOK()) {
					return rule;
				}
			}
		}
		return null;
	}

	public ResultatEvalCondicions evalCompleixCondicionsActivacio(RuleRele rule, Rele rele, FranjaHoraria fr) {
		if(!fr.isActivada()) {
			return new ResultatEvalCondicions(false, "FORA_FRANJA_HORARIA");
		}

		ResultatEvalCondicions result = this.evalCompleixCondicionsActivacio(rule, rele);
		rele.setUltimResultatEvalCondicions(result);

		return result;
	}
	
	/**
	 * Evalua si la RuleRele compleix TOTES les condicions d'activacio. En cas contrari retorna la condicio que ha provocat l'incompliment.
	 * 
	 * @param rule
	 * @param rele
	 * @return
	 */
	public ResultatEvalCondicions evalCompleixCondicionsActivacio(RuleRele rule, Rele rele) {
		if(rule.getCondicionsActivacio()==null) {
			return new ResultatEvalCondicions(false, "SENSE_CONDICIONS");
		}
		
		if(rule.isActivada()) { 
			return new ResultatEvalCondicions(false, "RULE_JA_ACTIVADA");
		}

		if(ModeRele.MANUAL.equals(rele.getCopyStateRele().getMode())) { 
			return new ResultatEvalCondicions(false, "RELE_MODE_MANUAL");
		}

		RuleCondicio darreraCondicioIncomplerta = null;
		for(RuleCondicio cond :rule.getCondicionsActivacio()) {
			cond.setCumpleCondicio(isCompleixRuleCondicio(cond, rule, rele));
			
			if(!cond.isCumpleCondicio()) {
				darreraCondicioIncomplerta = cond;
			}
		}
		
		if(darreraCondicioIncomplerta != null) {
			return new ResultatEvalCondicions(false, "CONDICIO_KO", darreraCondicioIncomplerta);
		}

		//Comprova si tot i estar la rule activa, compleix la condicio de activació respecta quan temps fa que es va activar per ultim cop
		Date lastDt = rule.getDtAturada(); 
		if(lastDt==null) {
			//Obte la data de l'historic
			StateRele last = rele.calcLastActivacioHistory();
			if(last!=null) {
				lastDt = last.getTimestamp();
			}
		}
		
		if(lastDt!=null) {
			int diff = (int)(new Date().getTime() - lastDt.getTime()) / 1000;
			if(diff < rule.getSegonsFinsProximaActivacio()) {
				return new ResultatEvalCondicions(false, "DELAY_ENTRE_ACTIVACIONS: " + rule.getSegonsFinsProximaActivacio());
			}
		}				

		return new ResultatEvalCondicions(true, "OK");
	}
	
	/**
	 * Evalua si la RuleRele compleix ALGUNA de les condicions de desactivacio.
	 * 
	 * @param rule
	 * @param rele
	 * @return
	 */
	public RuleCondicio evalCompleixCondicionsDesactivacio(RuleRele rule, Rele rele) {
		if(rule.getCondicionsDesactivacio()==null) return null;
		
		for(RuleCondicio cond :rule.getCondicionsDesactivacio()) {
			cond.setCumpleCondicio(isCompleixRuleCondicio(cond, rule, rele));
			
			if(cond.isCumpleCondicio()) {
				return cond;
			}
		}
		return null;
	}
	
	private boolean isCompleixRuleCondicio(RuleCondicio condicio, RuleRele rule, Rele rele) {
		try {
			String valor = getValor(condicio, rule, rele);
			condicio.setValorDatoCondicio(valor);
			
			Float floatVal;
			Float floatCondVal;
			if(valor!=null && valor.length() > 0) {
				try {
					if(valor.contains(".")) {
						floatVal = Float.valueOf(valor);
					}
					else {
						floatVal = Float.valueOf(valor+".0f");
					}
					
					if(condicio.getValor().contains(".")) {
						floatCondVal = Float.valueOf(condicio.getValor());
					}
					else {
						floatCondVal = Float.valueOf(condicio.getValor()+".0f");
					}
				} catch (NumberFormatException e) {
					return false;
				}
				
				
				return evalCondicioActivacio(condicio, floatVal, floatCondVal);
			}
			else {
				return false;
			}
		} 
		catch (ItemNotFoundException e) {
			return false;
		}
	}
	
	private boolean evalCondicioActivacio(RuleCondicio condicio, Float valor, Float condicioValor) {

		if(OperandCondicio.E.equals(condicio.getOperand())) {
			return valor.equals(condicioValor);
		}
		else if(OperandCondicio.NE.equals(condicio.getOperand())) {
			return !valor.equals(condicioValor);
		} 
		else if(OperandCondicio.G.equals(condicio.getOperand())) {
			return valor > condicioValor;
		} 
		else if(OperandCondicio.GE.equals(condicio.getOperand())) {
			return valor >= condicioValor;
		} 
		else if(OperandCondicio.L.equals(condicio.getOperand())) {
			return valor < condicioValor;
		} 
		else if(OperandCondicio.LE.equals(condicio.getOperand())) {
			return valor <= condicioValor;
		} 
		
		return false;
	}	

	private String getValor(RuleCondicio condicio, RuleRele rule, Rele rele) throws ItemNotFoundException {
		
		if(condicio.getDato().getType().equals(TipusDatoCondicio.RuleCondicioInfo)) {
			if(condicio.getDato().equals(DatoCondicio.DuracioSeconds)) {
				if(rule.getDtActivada()!=null) {
					int diff = (int)(new Date().getTime() - rule.getDtActivada().getTime()) / 1000;
					return String.valueOf(diff);
				}
			}
			else if(condicio.getDato().equals(DatoCondicio.VolumMlInjectats)) {
				if(rule.getDtActivada()!=null) {
					int segonsActivat = (int)(new Date().getTime() - rule.getDtActivada().getTime()) / 1000;
					double consumSegon = rele.getConsumHora() / 3600;
					
					return String.valueOf(consumSegon * segonsActivat);
				}
			}
			else if(condicio.getDato().equals(DatoCondicio.VolumMlAcumulatDia)) {
				Double consumAvui = rele.getConsumAvui();
				if(consumAvui!=null) {
					return consumAvui.toString();
				}
			}
			else if(condicio.getDato().equals(DatoCondicio.SegonsReleActivat)) {
				ParamDato param = condicio.getParamDato("idRele");
				
				if(param!=null) {
					Rele releCond = relesQuerySrv.getRele(param.getValor());
					StateRele state = releCond.getCopyStateRele();
					
					if(state.isOn()) {
						long dtActivat = state.getTimestamp().getTime();
						long now = new Date().getTime();
						return String.valueOf((now - dtActivat)/1000);
					}
					else {
						return "";
					}
				}
			}
			else if(condicio.getDato().equals(DatoCondicio.SegonsReleParat)) {
				ParamDato param = condicio.getParamDato("idRele");
				
				if(param!=null) {
					Rele releCond = relesQuerySrv.getRele(param.getValor());
					StateRele state = releCond.getCopyStateRele();
					
					if(!state.isOn()) {
						long dtParat = state.getTimestamp().getTime();
						long now = new Date().getTime();
						return String.valueOf((now - dtParat)/1000);
					}
					else {
						return "";
					}
				}
			}
			else if(condicio.getDato().equals(DatoCondicio.SegonsLecturaSonda)) {
				ParamDato param = condicio.getParamDato("idSonda");
				
				if(param!=null) {
					Sonda sonda = sondesSrv.getSonda(param.getValor());
					StateSonda state = sonda.getStateSonda();

					long dtLectura = state.getTimestamp().getTime();
					long now = new Date().getTime();
					return String.valueOf((now - dtLectura)/1000);
				}
				return "";
			}		
			else if(condicio.getDato().equals(DatoCondicio.HoraActual)) {
				Calendar avui = Calendar.getInstance();
				int hora = avui.get(Calendar.HOUR_OF_DAY);
				
				return String.valueOf(hora);
			}	
			
			/* TODO Excedents */
			else if(condicio.getDato().equals(DatoCondicio.SegonsPiPoolArrancat)) {
				long dtInit = piPoolCtx.getDateAppStarted().getTime();
				long now = new Date().getTime();
				return String.valueOf((now - dtInit)/1000);
			}
			else if(condicio.getDato().equals(DatoCondicio.ExcedentsSolars)) {
				long excedents = piPoolCtx.getDateAppStarted().getTime();
				return String.valueOf(excedents);
			}
			else if(condicio.getDato().equals(DatoCondicio.ExcedentsTimestampNivell1)) {
				long excedents = piPoolCtx.getDateAppStarted().getTime();
				return String.valueOf(excedents);
			}
			else if(condicio.getDato().equals(DatoCondicio.ExcedentsTimestampNivell2)) {
				long excedents = piPoolCtx.getDateAppStarted().getTime();
				return String.valueOf(excedents);
			}
			else if(condicio.getDato().equals(DatoCondicio.ImportacioXarxa)) {
				long excedents = piPoolCtx.getDateAppStarted().getTime();
				return String.valueOf(excedents);
			}
			else if(condicio.getDato().equals(DatoCondicio.ImportacioXarxaTimestampNivell)) {
				long excedents = piPoolCtx.getDateAppStarted().getTime();
				return String.valueOf(excedents);
			}


		}
		else if(condicio.getDato().getType().equals(TipusDatoCondicio.Sonda)) {
			Sonda sonda = sondesSrv.getSonda(condicio.getDato().toString());

			StateSonda state = sonda.getStateSonda();
			String valor = "";
			if(state!=null) {
				valor = state.getValor();
			}
			
			return valor;
		}
		else if(condicio.getDato().getType().equals(TipusDatoCondicio.Rele)) {
			
			Rele releCond = relesQuerySrv.getRele(condicio.getDato().toString());
			StateRele state = releCond.getCopyStateRele();
			String valor = "";
			if(state!=null) {
				valor = (state.isOn()) ? "1" : "0";
			}
			
			return valor;
		}
		
		return null;
	}
}
