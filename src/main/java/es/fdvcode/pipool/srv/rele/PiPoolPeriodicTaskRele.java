package es.fdvcode.pipool.srv.rele;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import es.fdvcode.pipool.PiPoolContext;
import es.fdvcode.pipool.common.ObjJsonPrinter;
import es.fdvcode.pipool.model.rele.CalendarRele;
import es.fdvcode.pipool.model.rele.FranjaHoraria;
import es.fdvcode.pipool.model.rele.Rele;
import es.fdvcode.pipool.model.rele.RuleCondicio;
import es.fdvcode.pipool.model.rele.RuleRele;
import es.fdvcode.pipool.model.rele.StateRele;
import es.fdvcode.pipool.srv.ItemNotFoundException;
import es.fdvcode.pipool.srv.scheduler.PiPoolPeriodicTask;
import lombok.RequiredArgsConstructor;

/**
 * 
 * @author cfarrema
 *
 */
@Component
@RequiredArgsConstructor
public final class PiPoolPeriodicTaskRele implements PiPoolPeriodicTask {

	protected final Logger log = LoggerFactory.getLogger(this.getClass());
	
	@Value("${pipool.scheduler.reles.active}")
	private boolean active;
	
	@Value("${pipool.scheduler.reles.initialDelay}")
	private int initialDelay;
	
	@Value("${pipool.scheduler.reles.periodSeconds}")
	private int periodSeconds;

	private final RelesSrv relesSrv;
	private final RelesQuerySrv relesQuerySrv;
	private final PiPoolContext ctx;
	private final RuleReleEval ruleEval;
	final ObjJsonPrinter objJsonPrinter;
	
	@Override
	public void run() {
		log.debug("{} Initiated", this.getClass().getSimpleName());
		ctx.setDateLastExecutionSchedulerRele(new Date());

		try {
			List<Rele> relesAmbMaster = new ArrayList<>();
			
			//Actualitza activacions dels reles i estableix estat gpio
			Collection<Rele> list = relesQuerySrv.getSyncReles().values();
			for (Rele rele : list) {
				tractaRele(rele);
				
				//Agrupa els reles amb rele master
				if(rele.getIdReleMaster()!=null) {
					relesAmbMaster.add(rele);
				}
			}
			
			//Un cop tracats tots els reles, torna a revisar tots els que tenen master i actualitza estat dels reles que tenen Rele master NO activat
			for (Rele rele : relesAmbMaster) {

				Rele releMaster = getReleMaster(rele.getIdReleMaster());
				if(releMaster!=null) {
					StateRele stateMaster = releMaster.getCopyStateRele();
					
					StateRele state = rele.getCopyStateRele();
					if(state.teActivacionsActives() && !stateMaster.isOn() && (state.isActivacioReleMaster() || rele.isMasterOnObligatori())) {
						log.info("SCHEDULER RELES - DESACTIVANT [OFF] el RELE: {} a causa de RELE MASTER:{} Aturat.", rele.getId(), releMaster.getId());
						relesSrv.setOffMaster(rele, releMaster);
					}
					else if(state.teActivacionsActives() && stateMaster.isOn() && (state.isActivacioReleMaster() || rele.isMasterOnObligatori()) 
							&& !state.getActivadorReleMaster().isActivada()) {
						log.info("SCHEDULER RELES - DESACTIVANT [OFF] el RELE: {} a causa de RELE MASTER:{} ja no esta activat per l'Activador del slave {}.", rele.getId(), releMaster.getId(), state.getActivadorReleMaster());
						relesSrv.setOffMaster(rele, releMaster);
					}
					else if(!state.teActivacionsActives() && stateMaster.isOn()) {
						if(stateMaster.teActivacioDeReleSlaves()) {
							log.info("SCHEDULER RELES - ACTIVANT [ON] el RELE: {} a causa de RELE MASTER:{} Activat.", rele.getId(), releMaster.getId());
							relesSrv.setOnMaster(rele, releMaster);
						}
					}
				}
			}
			
		} catch (Exception e) {
			log.error("ERROR RUN PiPoolPeriodicTaskRele. Continua l'execució...", e);
		}
	}
	
	@Override
	public boolean isActive() {
		return active;
	}

	@Override
	public int getInitialDelay() {
		return initialDelay;
	}

	@Override
	public int getPeriodSeconds() {
		return periodSeconds;
	}
	
	@Override
	public TipusScheduler getTipus() {
		return TipusScheduler.RELES;
	}

	
	private void tractaRele(Rele rele) {
		log.trace("Tractant Rele id: {}", rele.getId());

		StateRele state = rele.getCopyStateRele();
		if(state.getCausa().equals(StateRele.CausaState.OFF_SHUTDOWN)) {
			log.trace("El rele {} s'ha marcat per Shutdown. No es farà cap activació AUTO.", rele.getId());
			return;
		}
		
		//Tractament AUTO-RULE
		if(state.getActivacioRule()!=null) {
			//Si tenim alguna Rule Rele Activada
			RuleRele rule = state.getActivacioRule();
			RuleCondicio cond = ruleEval.evalCompleixCondicionsDesactivacio(rule, rele);
			if(cond!=null) {
				log.info("SCHEDULER RELES - Es compleix la condicio de DESACTIVACIO:[{}] de la Rule: {} del RELE: {}", cond, rule, rele.getId());
				rule.desactivar();
				state.setActivacioRule(null);
			}
		}

		//Comprova si hi ha alguna Rule que es compleixi
		RuleRele ruleActivable = ruleEval.calcRuleActivable(rele);
		if(state.getActivacioRule()==null && ruleActivable!=null) {
			log.info("SCHEDULER RELES - Es compleixen TOTES les condicions de ACTIVACIO:[{}] del RELE: {}",  objJsonPrinter.print(ruleActivable), rele.getId());
			ruleActivable.activar();
			state.setActivacioRule(ruleActivable);
		}
		
		
		//Neteja activacions caducades
		if(state.teActivacioTemporal() && !state.teActivacioTemporalActiva()) {
			state.setActivacioTemporal(null);
		}
		if (state.teActivacioProgramada() && !state.teActivacioProgramadaActiva()) {
			state.setActivacioProgramada(null);
		}
		log.trace("Rele amb Activacions netejades: {}", objJsonPrinter.print(rele));
		
		//Tractament AUTO-PROGRAMACIO
		if(!state.teActivacionsActives()) {
			FranjaHoraria fr = getProximaActivacioProgramada(rele);
			if(fr!=null) {
				state.setActivacioProgramada(fr);
				log.trace("Rele amb Activacio nova Programada afegida: {}", objJsonPrinter.print(rele));
			}
		}
				
		
		//ACTIVACIO/DESACTIVACIO DE RELES
		if(state.isOn()){
			//Desactiva Rele si NO té ActivacionsActives
			if(!state.teActivacionsActives()) {
				log.info("SCHEDULER RELES - DESACTIVANT [OFF] el RELE: {}", rele.getId());
				relesSrv.setStateAuto(state, false);
			}
			else {
				rele.updateStateRele(state);
			}
		}
		else {
			//Activa Rele si té ActivacionsActives
			if(state.teActivacionsActives()) {
				log.info("SCHEDULER RELES - ACTIVANT [ON] el RELE: {}", rele.getId());
				relesSrv.setStateAuto(state, true);
			}
			else {
				rele.updateStateRele(state);
			}			
		}
		
		//Reles amb idMaster
		if(rele.getIdReleMaster()!=null) {
			tractaReleMaster(rele);
		}
	}
	
	private void tractaReleMaster(Rele rele) {
		log.trace("Tractant idMaster de Rele id: {}", rele.getId());
		
		StateRele state = rele.getCopyStateRele();
		
		Rele releMaster = getReleMaster(rele.getIdReleMaster());
		if(releMaster!=null) {
			StateRele stateMaster = releMaster.getCopyStateRele();
			
			//Revisa els reles desactivats per master i comprova que la desactivació encara sigui vigent
			if(state.isDesactivacioReleMaster()) {
	
				if(!state.teActivacioProgramadaActiva() && !state.teActivacioTemporalActiva()) {	//Si ja no te programacions o temporals actives implica que ja no pot estar desactivat per master
					state.setDesactivacioReleMaster(false);
					rele.updateStateRele(state);
					
					log.info("SCHEDULER RELES - El Rele {} ja no té programacions actives --> Deixa de estar en [DESACTIVAT per RELE MASTER] {}.", rele.getId(), releMaster.getId());
				}
				else { 		//Si encara te activacions programades o temporals actives Revisa si rele master esta actiu
						
					if(stateMaster.isOn()) {
						state.setDesactivacioReleMaster(false);
						rele.updateStateRele(state);
						
						log.info("SCHEDULER RELES - El Rele {} es pot activar perque el Master {} ja está actiu--> Deixa de estar [DESACTIVAT per RELE MASTER].", rele.getId(), releMaster.getId());
					}
				}
			}
		}
	}
	
	private Rele getReleMaster(String idReleMaster) {
		try {
			return relesQuerySrv.getRele(idReleMaster);
		} 
		catch (ItemNotFoundException e) {
			log.trace("El Rele idMaster:{}, no existeix", idReleMaster);
		}		
		
		return null;
	}
	
	private FranjaHoraria getProximaActivacioProgramada(Rele rele) {
		
		//Busca si hi ha noves activacions programades actives a posar
		for(CalendarRele cal : rele.getCalendars()) {
			FranjaHoraria fr = cal.getFranjaActiva();
			if(fr != null) {
				return fr;
			}
		}	
		return null;
	}
}
