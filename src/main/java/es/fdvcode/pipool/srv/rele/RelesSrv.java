package es.fdvcode.pipool.srv.rele;

import java.io.IOException;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.pi4j.io.gpio.digital.DigitalOutput;

import es.fdvcode.pipool.common.ParameterizedMessage;
import es.fdvcode.pipool.model.rele.CalendarRele;
import es.fdvcode.pipool.model.rele.FranjaHoraria;
import es.fdvcode.pipool.model.rele.PersistibleRele;
import es.fdvcode.pipool.model.rele.Rele;
import es.fdvcode.pipool.model.rele.ResultatEvalCondicions;
import es.fdvcode.pipool.model.rele.RuleRele;
import es.fdvcode.pipool.model.rele.StateRele;
import es.fdvcode.pipool.model.rele.StateRele.CausaState;
import es.fdvcode.pipool.model.rele.StateRele.ModeRele;
import es.fdvcode.pipool.mqtt.homeassistant.PipoolEntitiesMqttSrv;
import es.fdvcode.pipool.restsrv.v1.dto.ReleConfigDto;
import es.fdvcode.pipool.srv.ItemNotFoundException;
import lombok.RequiredArgsConstructor;

/**
 * 
 * @author cfarrema
 *
 */
@Component
@RequiredArgsConstructor
public class RelesSrv {

	private final Logger log = LoggerFactory.getLogger(RelesSrv.class);
	private final RelesQuerySrv relesQuery;
	private final RelesLoader relesLoader;
	private final RelesPersister relesPersister;
	private final RuleReleEval ruleEval;
	private final PipoolEntitiesMqttSrv pipoolMqtt;
	private final GpioPinController gpioController;
	
	@Value("${pipool.reles.numDiesHistoria}")
	private int numDiesHistoria;
	
	/**
	 * initGpios
	 */
	public void initGpios() {
		
		log.info("Inicialitza GPios dels RELE's.");
		Map<String, Rele> mapReles = relesLoader.getReles();
		try {
			this.loadHistory();
		} catch (IOException e) {
			log.error("Error al carregar historia de RELES.", e);
		}

		
		for(Rele item : mapReles.values()) {
			StateRele state = item.getCopyStateRele();
			if (!item.isEnabled()) {
				state.setOn(false);
			}
			
			try {
				DigitalOutput gpioPinOut = gpioController.provisionGpioPin(item.getGpioPin(), item.getNom(), state.isGpioPinHigh());
				
				// Comprueba si el estado del pin es diferente al estado objetivo y lo actualiza
                if (gpioPinOut.isHigh() != state.isGpioPinHigh()) {
                    gpioPinOut.high();
                }
				
                state.syncGpioPin(gpioPinOut.isHigh());
				item.updateStateRele(state);
				log.info("RELESRV - Init GPIOs -> S'ha inicialitzat el Pin GPIO:{} amb Nom:{}, isON:{} i GpioPinHigh:{}.", item.getGpioPin(), item.getNom(), state.isOn(), state.isGpioPinHigh());
				
				//Revisa si l'historic va acabar amb TEMP activat (només si el relé està habilitat)
				StateRele lastState = item.calcLastDesactivacioHistory();
				if(item.isEnabled() && lastState!=null && CausaState.OFF_SHUTDOWN.equals(lastState.getCausa())) {
					StateRele lastActiv = item.calcLastActivacioHistory();
					if(lastActiv!=null &&  CausaState.ON_TEMP.equals(lastActiv.getCausa())) {
						this.setOnTemporal(item, lastActiv.getActivacioTemporal());
					}
				}			
			} 
			catch (Exception e) {
                log.error("Error al inicializar el pin GPIO " + item.getGpioPin(), e);
            }
		}
	}
	
	/**
	 * 
	 * @param idRele
	 * @param franjaTemporal
	 * @return
	 * @throws ItemNotFoundException
	 */
	public Rele setOnTemporal(String idRele, FranjaHoraria franjaTemporal) throws ItemNotFoundException {
		Rele rele = relesQuery.getRele(idRele);
		this.setOnTemporal(rele, franjaTemporal);
		return rele;
	}
	
	public StateRele setOnTemporal(Rele rele, FranjaHoraria franjaTemporal) {
		if (!rele.isEnabled()) {
			throw new IllegalStateException("El relé [" + rele.getId() + "] està DESHABILITAT i no es pot activar temporalment.");
		}
		
		StateRele state = rele.getCopyStateRele();
		
		state.setActivacioTemporal(franjaTemporal);
		state.setMode(ModeRele.AUTO);
				
		//Si el relé está parat, cal engegar-lo
		ParameterizedMessage msg;  
		if(!state.isOn()) {

			//Nomes engegar si no esta parat manual
			if(state.isDesactivacioManual()) {
				msg = new ParameterizedMessage("RELESRV - SET ON TEMPORAL -> Rele={}: S'ha establert la franjaTemporal={} i però no s'ha activat perque esta en MODE MANUA.", rele.getId(), franjaTemporal.getId());
			}
			else {
				state = this.updateState(state, true, ModeRele.AUTO);
				msg = new ParameterizedMessage("RELESRV - SET ON TEMPORAL -> Rele={}: S'ha establert la franjaTemporal={} i s'ha ACTIVAT RELE [ON].", rele.getId(), franjaTemporal.getId());
			}
		}
		else {
			msg = new ParameterizedMessage("RELESRV - SET ON TEMPORAL -> Rele={}: S'ha establert la franjaTemporal={} sense canviar estat, ja estava activat.", rele.getId(), franjaTemporal.getId());
		}
		
		//Aplica el canvi de estat:
		StateRele newState = rele.setNewStateRele(state, CausaState.ON_TEMP, msg.getFormattedMessage());
		log.info(msg.getFormattedMessage());
		
		return newState;
	}

	/**
	 * 
	 * @param idRele
	 * @return
	 * @throws ItemNotFoundException
	 */
	public Rele cancelTemporal(String idRele) throws ItemNotFoundException {
		Rele rele = relesQuery.getRele(idRele);
		StateRele state = rele.getCopyStateRele();
		state.setActivacioTemporal(null);
		
		//Si el relé está engegat, cal parar-lo
		ParameterizedMessage msg;
		if(state.isOn()) {

			//Nomes parar si no esta engegat manual
			if(state.isActivacioManual()) {
				msg = new ParameterizedMessage("RELESRV - CANCEL ON TEMPORAL -> Rele={}: S'ha cancelat la Activació Temporal però no s'ha desactivat perque esta en MODE MANUAL.", rele.getId());
			}
			else {
				state = this.updateState(state, false, ModeRele.AUTO);
				msg = new ParameterizedMessage("RELESRV - CANCEL ON TEMPORAL -> Rele={}: S'ha cancelat la Activació Temporal i s'ha DESACTIVAT RELE [OFF].", rele.getId());
			}
			
		}
		else {
			msg = new ParameterizedMessage("RELESRV - CANCEL ON TEMPORAL -> Rele={}: S'ha cancelat la Activació Temporal sense canviar estat, ja estava desactivat.", rele.getId());
		}
		
		//Aplica el canvi de estat:
		rele.setNewStateRele(state, CausaState.OFF_TEMP, msg.getFormattedMessage());
		log.info(msg.getFormattedMessage());
		
		return rele;
	}
	
	/**
	 * 
	 * @param rele
	 * @param releMaster
	 * @return
	 */
	public StateRele setOffMaster(Rele rele, Rele releMaster) {
		relesQuery.syncRele(rele);
		
		StateRele state = rele.getCopyStateRele();
		if(state.isActivacioReleMaster()) {
			state.setActivadorReleMaster(null);
			state.setDesactivacioReleMaster(false);
		}
		else {
			state.setDesactivacioReleMaster(true);
		}
				
		//Si el relé está engegat, cal parar-lo
		ParameterizedMessage msg=null;
		if(state.isOn()) {

			//Nomes parar si no esta engegat manual o bé el rele master es obligatori que estigui activat
			if(!state.isActivacioManual() || rele.isMasterOnObligatori()) {
				state = this.updateState(state, false, ModeRele.AUTO);
				msg = new ParameterizedMessage("RELESRV - SET OFF RELEMASTER -> Rele={}: S'ha establert [DESACTIVACIO MASTER] i s'ha desactivat rele [OFF]. El ReleMaster es:{}.", rele.getId(), releMaster.getId());
			}
			else {
				return state;
			}
			
		}
		else {
			msg = new ParameterizedMessage("RELESRV - SET OFF RELEMASTER -> Rele={}: S'ha establert [DESACTIVACIO MASTER] sense canviar estat, ja estava aturat.", rele.getId());
		}
		
		//Aplica el canvi de estat:
		StateRele newState = rele.setNewStateRele(state, CausaState.OFF_MASTER, msg.getFormattedMessage());
		log.info(msg.getFormattedMessage());
		
		return newState;
	}

	public StateRele setOnMaster(Rele rele, Rele releMaster) {
		if (!rele.isEnabled()) {
			log.warn("RELESRV - SET ON RELEMASTER -> Rele={} està DESHABILITAT. No s'activa per ReleMaster={}.", rele.getId(), releMaster.getId());
			return rele.getCopyStateRele();
		}
		
		relesQuery.syncRele(rele);
		
		StateRele state = rele.getCopyStateRele();
		StateRele stateMaster = releMaster.getCopyStateRele();
		
		if(stateMaster.teActivacioRuleActiva()) {
			state.setActivadorReleMaster(stateMaster.getActivacioRule());
		}
		else if(stateMaster.teActivacioTemporalActiva()) {
			state.setActivadorReleMaster(stateMaster.getActivacioTemporal());
		}
		else if(stateMaster.teActivacioProgramadaActiva()) {
			state.setActivadorReleMaster(stateMaster.getActivacioProgramada());
		}
		
		state.setDesactivacioReleMaster(false);
		
		//Si el relé está parat, cal engegar-lo
		ParameterizedMessage msg;
		if(!state.isOn()) {

			//Nomes engegar si no esta parat manual
			if(state.isDesactivacioManual()) {
				msg = new ParameterizedMessage("RELESRV - SET ON RELEMASTER -> Rele={}: S'ha establert [ACTIVACIO MASTER] però no s'ha activat perque esta en MODE OFF MANUAL.", rele.getId());
			}
			else {
				state = this.updateState(state, true, ModeRele.AUTO);
				msg = new ParameterizedMessage("RELESRV - SET ON RELEMASTER -> Rele={}: S'ha establert [ACTIVACIO MASTER] i s'ha activat rele [ON]. El ReleMaster es:{}.", rele.getId(), releMaster.getId());
			}
			
		}
		else {
			msg = new ParameterizedMessage("RELESRV - SET ON RELEMASTER -> Rele={}: S'ha establert [ACTIVACIO MASTER] sense canviar estat, ja estava activat.", rele.getId());
		}
		
		//Aplica el canvi de estat:
		StateRele newState = rele.setNewStateRele(state, CausaState.ON_MASTER, msg.getFormattedMessage());
		log.info(msg.getFormattedMessage());
		
		return newState;
	}

	
	/**
	 * 
	 * @param idRele
	 * @param isOn
	 * @return
	 * @throws ItemNotFoundException
	 */
	public Rele setStateManual(String idRele, boolean isOn) throws ItemNotFoundException {
		Rele rele = relesQuery.getRele(idRele);
		if (isOn && !rele.isEnabled()) {
			throw new IllegalStateException("El relé [" + idRele + "] està DESHABILITAT i no es pot activar manualment.");
		}
		this.setStateManual(rele.getCopyStateRele(), isOn);

		return rele;
	}

	/**
	 * 
	 * @param rele
	 * @param isOn
	 * @return
	 * @throws ItemNotFoundException
	 */
	public StateRele setStateManual(StateRele state, boolean isOn) {
		this.updateState(state, isOn, ModeRele.MANUAL);
		
		Rele rele = state.getRele();
		ParameterizedMessage msg = new ParameterizedMessage("RELESRV SET STATE MANUAL -> S'ha establert el Rele={} amb Mode={}, EstatRele={} i EstatPin={}.", rele.getId(), state.getMode(), state.isOn(), state.isGpioPinHigh());
		
		//Aplica el canvi de estat:
		CausaState causa = isOn ? CausaState.ON : CausaState.OFF;
		return rele.setNewStateRele(state, causa, msg.getFormattedMessage());
	}
	
	public StateRele setStateHA(StateRele state, boolean isOn) {
		return this.setStateHA(state, isOn, ModeRele.MANUAL);
	}
	
	public StateRele setStateHA(StateRele state, boolean isOn, ModeRele modeRele) {
		Rele rele = state.getRele();
		if (isOn && rele != null && !rele.isEnabled()) {
			log.warn("RELESRV SET STATE HA -> El relé [{}] està DESHABILITAT. No s'activa per HA.", rele.getId());
			if (pipoolMqtt != null) {
				pipoolMqtt.pubStateRele(state);
			}
			return state;
		}
		this.updateState(state, isOn, modeRele);
		
		ParameterizedMessage msg = new ParameterizedMessage("RELESRV SET STATE HA -> S'ha establert el Rele={} amb Mode={}, EstatRele={} i EstatPin={}.", rele.getId(), state.getMode(), state.isOn(), state.isGpioPinHigh());
		
		//Aplica el canvi de estat:
		CausaState causa = isOn ? CausaState.ON_HA : CausaState.OFF_HAOFFLINE;
		
		return rele.setNewStateRele(state, causa, msg.getFormattedMessage());
	}
	
	public StateRele setStateShutdown(StateRele state) {
		this.updateState(state, false, state.getMode());
		
		Rele rele = state.getRele();
		ParameterizedMessage msg = new ParameterizedMessage("RELESRV SET STATE OFF-SHUTDOWN -> S'ha establert el Rele={} amb Mode={}, EstatRele={} i EstatPin={}.", rele.getId(), state.getMode(), state.isOn(), state.isGpioPinHigh());
		
		return rele.setNewStateRele(state, CausaState.OFF_SHUTDOWN, msg.getFormattedMessage());
	}	

	/**
	 * 
	 * @param idRele
	 * @param isOn
	 * @return
	 * @throws ItemNotFoundException
	 */
	public Rele setStateAuto(String idRele, boolean isOn) throws ItemNotFoundException {
		Rele rele = relesQuery.getRele(idRele);
		this.setStateAuto(rele.getCopyStateRele(), isOn);
		
		return rele; 	
	}

	/**
	 * 
	 * @param rele
	 * @param isOn
	 * @return
	 * @throws ItemNotFoundException
	 */
	public StateRele setStateAuto(StateRele state, boolean isOn) {
		this.updateState(state, isOn, ModeRele.AUTO);
		
		ParameterizedMessage msg = new ParameterizedMessage("RELESRV SET STATE AUTO -> S'ha establert el Rele={} amb Mode={}, EstatRele={} i EstatPin={}.", state.getRele().getId(), state.getMode(), state.isOn(), state.isGpioPinHigh());
		
		//Aplica el canvi de estat:
		if(isOn) {
			if(state.teActivacioRuleActiva()) {
				return state.getRele().setNewStateRele(state, CausaState.ON_RULE, msg.getFormattedMessage());	
			}
			else {
				return state.getRele().setNewStateRele(state, CausaState.ON, msg.getFormattedMessage());
			}
		}
		else {
			return state.getRele().setNewStateRele(state, CausaState.OFF, msg.getFormattedMessage());
		}
	}

	
	
	/**
	 * 
	 * @param idRele
	 * @return
	 * @throws ItemNotFoundException
	 */
	public StateRele setModeAuto(Rele rele) {
		return this.setMode(rele, ModeRele.AUTO);
	}
	
	public StateRele setModeManual(Rele rele) {
		return this.setMode(rele, ModeRele.MANUAL);
	}
	
	public StateRele setMode(Rele rele, ModeRele modeRele) {
		StateRele state = rele.getCopyStateRele();
		state.setMode(modeRele);

		ParameterizedMessage msg = new ParameterizedMessage("RELESRV - SET MODE {} -> Rele={}: S'ha establert el Rele en mode [{}] sense canviar EstatRele={} i EstatPin={}.", modeRele, rele.getId(), modeRele, state.isOn(), state.isGpioPinHigh());
		
		//Aplica el canvi de estat:
		StateRele newState = rele.setNewStateRele(state, CausaState.CHANGE_MODE, msg.getFormattedMessage());

		log.info(msg.getFormattedMessage());
		
		pipoolMqtt.pubStateRele(state);
		
		return newState;
	}
	
	
	/**
	 * 
	 * @param idRele
	 * @return
	 * @throws ItemNotFoundException
	 */
	public Rele resetConsum(String idRele, int valor) throws ItemNotFoundException {
		Rele rele = relesQuery.getRele(idRele);
		
		StateRele state = rele.getCopyStateRele();
		state.setConsumRele(valor);
		
		ParameterizedMessage msg = new ParameterizedMessage("RELESRV - RESET CONSUM RELE={} al valor={}.", rele.getId(), valor);
		
		//Aplica el canvi de estat:
		rele.setNewStateRele(state, CausaState.RESET_CONSUM, msg.getFormattedMessage(), valor);

		log.info(msg.getFormattedMessage());
		
		pipoolMqtt.pubStateRele(state);
		
		return rele;
	}
	
	/**
	 * 
	 * @param rele
	 * @param isOn
	 */
	private StateRele updateState(StateRele state, boolean isOn, ModeRele mode) {
		if (isOn && state.getRele() != null && !state.getRele().isEnabled()) {
			throw new IllegalStateException("El relé [" + state.getRele().getId() + "] està DESHABILITAT i no es pot activar sota cap circumstància.");
		}
		
		state.setMode(mode);
		state.setOn(isOn);
		
		if (gpioController != null) {
			DigitalOutput gpioPin = gpioController.getGpioPin(state.getRele().getGpioPin());
			if(gpioPin==null) {
				throw new RuntimeException("No s'ha pogut obtenir el GpioPin: " + state.getRele().getGpioPin());
			}
			gpioPin.setState(state.isGpioPinHigh());
			state.syncGpioPin(gpioPin.isHigh());
		} else {
			state.syncGpioPin(state.isGpioPinHigh());
		}
		
		log.info("RELESRV - UPDATESTATE -> S'ha establert l'estat del Rele={} amb Mode={}, EstatRele={} i EstatPin={}.", state.getRele().getId(), state.getMode(), state.isOn(), state.isGpioPinHigh());
		
		if (pipoolMqtt != null) {
			pipoolMqtt.pubStateRele(state);
		}
		
		return state;
	}
	
	
	public void loadHistory() throws IOException {
        		
		Map<String, Rele> mapReles = relesLoader.getReles();
		for(Rele rele : mapReles.values()) {
			List<PersistibleRele> list = relesPersister.loadHistory(rele.getId(), numDiesHistoria);
			List<StateRele> listState = relesPersister.convert(rele, list);
			
			listState.sort(new Comparator<StateRele>() {

				@Override
				public int compare(StateRele o1, StateRele o2) {
					return o1.getTimestamp().compareTo(o2.getTimestamp());
				}
			});
			
			rele.setHistoric(listState);
			
			//revisa si l'historic va acabar en MODE Manual
			//Desactivat perque peta i evita que pipool arranqui. Cal mirar això després que tots els GPIO estiguin inicialitzats, NO ABANS.
//			StateRele lastState = rele.calcLastDesactivacioHistory();
//			if(lastState!=null && ModeRele.MANUAL.equals(lastState.getMode())) {
//				this.setStateManual(rele.getCopyStateRele(), false);
//			}
		}
    	
    	log.info("Historial de Rele llegit i carregat OK.");
	}
	
	public ResultatEvalCondicions evalRule(String idRele, String idRule) throws ItemNotFoundException {
		Rele rele = relesQuery.getRele(idRele);
		
		if (rele.getRules() != null) {
			for (RuleRele rule : rele.getRules()) {
				if(rule.getId().equals(idRule)) {
					ResultatEvalCondicions result = ruleEval.evalRuleOnDemand(rule, rele);
					rele.setUltimResultatEvalCondicions(result);
					return result;
				}
			}
		}
		
		if (rele.getCalendars() != null) {
			for (CalendarRele cal : rele.getCalendars()) {
				if (cal.getListFrangesHoraries() != null) {
					for (FranjaHoraria fr : cal.getListFrangesHoraries()) {
						if (fr.getRules() != null) {
							for (RuleRele rule : fr.getRules()) {
								if (rule.getId().equals(idRule)) {
									ResultatEvalCondicions result = ruleEval.evalRuleOnDemand(rule, rele);
									rele.setUltimResultatEvalCondicions(result);
									return result;
								}
							}
						}
					}
				}
			}
		}
		
		return null;
	}

	public Rele updateCalendars(String idRele, List<CalendarRele> calendars) throws ItemNotFoundException, IOException {
		Rele rele = relesQuery.getRele(idRele);
		if (calendars == null) {
			throw new IllegalArgumentException("La llista de calendaris no pot ser null.");
		}
		
		for (CalendarRele cal : calendars) {
			if (cal.getDiaIni() < 1 || cal.getDiaIni() > 31 || cal.getDiaFin() < 1 || cal.getDiaFin() > 31) {
				throw new IllegalArgumentException("Els dies de calendari han d'estar entre 1 i 31.");
			}
			if (cal.getMesIni() < 1 || cal.getMesIni() > 12 || cal.getMesFin() < 1 || cal.getMesFin() > 12) {
				throw new IllegalArgumentException("Els mesos de calendari han d'estar entre 1 i 12.");
			}
			cal.setRele(rele);
			
			if (cal.getListFrangesHoraries() != null) {
				for (FranjaHoraria fr : cal.getListFrangesHoraries()) {
					if (fr.getHoraIni() < 0 || fr.getHoraIni() > 23) {
						throw new IllegalArgumentException("L'hora inicial ha d'estar entre 0 i 23.");
					}
					if (fr.getMinutIni() < 0 || fr.getMinutIni() > 59) {
						throw new IllegalArgumentException("El minut inicial ha d'estar entre 0 i 59.");
					}
					if (fr.getSecondIni() < 0 || fr.getSecondIni() > 59) {
						throw new IllegalArgumentException("El segon inicial ha d'estar entre 0 i 59.");
					}
					if (fr.getDuracioSeconds() < 0) {
						throw new IllegalArgumentException("La duració en segons no pot ser negativa.");
					}
				}
			}
		}
		
		rele.setCalendars(calendars);
		relesLoader.writeJsonFile();
		log.info("Calendaris del Rele [{}] actualitzats i persistits a JSON correctament.", idRele);
		return rele;
	}

	public Rele updateRules(String idRele, List<RuleRele> rules) throws ItemNotFoundException, IOException {
		Rele rele = relesQuery.getRele(idRele);
		if (rules == null) {
			throw new IllegalArgumentException("La llista de regles no pot ser null.");
		}
		rele.setRules(rules);
		relesLoader.writeJsonFile();
		log.info("Regles del Rele [{}] actualitzades i persistides a JSON correctament.", idRele);
		return rele;
	}

	public Rele updateConfig(String idRele, ReleConfigDto dto) throws ItemNotFoundException, IOException {
		Rele rele = relesQuery.getRele(idRele);
		if (dto == null) {
			throw new IllegalArgumentException("El payload de configuració no pot ser null.");
		}
		if (dto.getNom() != null && !dto.getNom().trim().isEmpty()) {
			rele.setNom(dto.getNom().trim());
		}
		if (dto.getSecondsDuradaCicles() != null) {
			if (dto.getSecondsDuradaCicles() < 1) {
				throw new IllegalArgumentException("La durada dels cicles ha de ser com a mínim 1 segon.");
			}
			rele.setSecondsDuradaCicles(dto.getSecondsDuradaCicles());
		}
		if (dto.getConsumHora() != null) {
			if (dto.getConsumHora() < 0) {
				throw new IllegalArgumentException("El consum per hora no pot ser negatiu.");
			}
			rele.setConsumHora(dto.getConsumHora());
		}
		if (dto.getUnitatConsumHora() != null) {
			rele.setUnitatConsumHora(dto.getUnitatConsumHora());
		}
		if (dto.getRulesOn() != null) {
			rele.setRulesOn(dto.getRulesOn());
		}
		if (dto.getMasterOnObligatori() != null) {
			rele.setMasterOnObligatori(dto.getMasterOnObligatori());
		}
		if (dto.getIdReleMaster() != null) {
			rele.setIdReleMaster(dto.getIdReleMaster().trim().isEmpty() ? null : dto.getIdReleMaster().trim());
		}
		if (dto.getMqttEnabled() != null) {
			rele.setMqttEnabled(dto.getMqttEnabled());
		}
		if (dto.getMqttConsumSensorEnabled() != null) {
			rele.setMqttConsumSensorEnabled(dto.getMqttConsumSensorEnabled());
		}
		if (dto.getOrdre() != null) {
			rele.setOrdre(dto.getOrdre());
		}
		if (dto.getEnabled() != null) {
			rele.setEnabled(dto.getEnabled());
			if (!dto.getEnabled()) {
				StateRele state = rele.getCopyStateRele();
				state.setActivacioTemporal(null);
				state.setActivacioProgramada(null);
				if (state.getActivacioRule() != null) {
					state.getActivacioRule().desactivar();
					state.setActivacioRule(null);
				}
				if (state.isOn()) {
					this.setStateManual(state, false);
				} else {
					rele.updateStateRele(state);
				}
			}
		}
		
		relesLoader.writeJsonFile();
		log.info("Configuració del Rele [{}] actualitzada i persistida a JSON correctament.", idRele);
		return rele;
	}

	public Rele enableRele(String idRele) throws ItemNotFoundException, IOException {
		Rele rele = relesQuery.getRele(idRele);
		rele.setEnabled(true);
		relesLoader.writeJsonFile();
		log.info("RELESRV - Relé [{}] HABILITAT correctament i persistit a JSON.", idRele);
		if (pipoolMqtt != null) {
			pipoolMqtt.pubConfigAll();
			pipoolMqtt.pubStateRele(rele.getCopyStateRele());
		}
		return rele;
	}

	public Rele disableRele(String idRele) throws ItemNotFoundException, IOException {
		Rele rele = relesQuery.getRele(idRele);
		rele.setEnabled(false);
		
		StateRele state = rele.getCopyStateRele();
		state.setActivacioTemporal(null);
		state.setActivacioProgramada(null);
		if (state.getActivacioRule() != null) {
			state.getActivacioRule().desactivar();
			state.setActivacioRule(null);
		}
		
		if (state.isOn()) {
			this.setStateManual(state, false);
		} else {
			rele.updateStateRele(state);
		}
		
		relesLoader.writeJsonFile();
		log.info("RELESRV - Relé [{}] DESHABILITAT correctament, forçat a OFF i persistit a JSON.", idRele);
		if (pipoolMqtt != null) {
			pipoolMqtt.pubConfigAll();
			pipoolMqtt.pubStateRele(rele.getCopyStateRele());
		}
		return rele;
	}
}
