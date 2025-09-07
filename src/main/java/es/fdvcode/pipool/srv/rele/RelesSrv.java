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
import es.fdvcode.pipool.model.rele.FranjaHoraria;
import es.fdvcode.pipool.model.rele.PersistibleRele;
import es.fdvcode.pipool.model.rele.Rele;
import es.fdvcode.pipool.model.rele.ResultatEvalCondicions;
import es.fdvcode.pipool.model.rele.RuleRele;
import es.fdvcode.pipool.model.rele.StateRele;
import es.fdvcode.pipool.model.rele.StateRele.CausaState;
import es.fdvcode.pipool.model.rele.StateRele.ModeRele;
import es.fdvcode.pipool.mqtt.homeassistant.PipoolEntitiesMqttSrv;
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
			
			try {
				DigitalOutput gpioPinOut = gpioController.provisionGpioPin(item.getGpioPin(), item.getNom(), state.isGpioPinHigh());
				
				// Comprueba si el estado del pin es diferente al estado objetivo y lo actualiza
                if (gpioPinOut.isHigh() != state.isGpioPinHigh()) {
                    gpioPinOut.high();
                }
				
                state.syncGpioPin(gpioPinOut.isHigh());
				item.updateStateRele(state);
				log.info("RELESRV - Init GPIOs -> S'ha inicialitzat el Pin GPIO:{} amb Nom:{}, isON:{} i GpioPinHigh:{}.", item.getGpioPin(), item.getNom(), state.isOn(), state.isGpioPinHigh());
				
				//Revisa si l'historic va acabar amb TEMP activat
				StateRele lastState = item.calcLastDesactivacioHistory();
				if(lastState!=null &&  CausaState.OFF_SHUTDOWN.equals(lastState.getCausa())) {
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
		this.updateState(state, isOn, modeRele);
		
		Rele rele = state.getRele();
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
		DigitalOutput  gpioPin = gpioController.getGpioPin(state.getRele().getGpioPin());
		if(gpioPin==null) {
			throw new RuntimeException("No s'ha pogut obtenir el GpioPin: " + state.getRele().getGpioPin());
		}
		
		state.setMode(mode);
		state.setOn(isOn);
		
		gpioPin.setState(state.isGpioPinHigh());

		//Sincronitza estat rele amb gpio
		state.syncGpioPin(gpioPin.isHigh());
		
		log.info("RELESRV - UPDATESTATE -> S'ha establert l'estat del Rele={} amb Mode={}, EstatRele={} i EstatPin={}.", state.getRele().getId(), state.getMode(), state.isOn(), state.isGpioPinHigh());
		
		pipoolMqtt.pubStateRele(state);
		
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
		
		for (RuleRele rule : rele.getRules()) {
			if(rule.getId().equals(idRule)) {
				ResultatEvalCondicions result = ruleEval.evalCompleixCondicionsActivacio(rule, rele);
				rele.setUltimResultatEvalCondicions(result);
				return result;
			}
		}
		
		return null;
	}
}
