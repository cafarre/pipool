package es.fdvcode.pipool.restsrv.v1;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import es.fdvcode.pipool.common.ObjJsonPrinter;
import es.fdvcode.pipool.model.rele.FranjaHoraria;
import es.fdvcode.pipool.model.rele.PersistibleRele;
import es.fdvcode.pipool.model.rele.Rele;
import es.fdvcode.pipool.model.rele.ResultatEvalCondicions;
import es.fdvcode.pipool.model.rele.StateRele;
import es.fdvcode.pipool.restsrv.v1.response.RestResponse;
import es.fdvcode.pipool.srv.ItemNotFoundException;
import es.fdvcode.pipool.srv.rele.RelesLoader;
import es.fdvcode.pipool.srv.rele.RelesPersister;
import es.fdvcode.pipool.srv.rele.RelesQuerySrv;
import es.fdvcode.pipool.srv.rele.RelesSrv;
import lombok.RequiredArgsConstructor;

/**
 * 
 * @author cfarrema
 *
 */
@RestController
@RequestMapping(RelesRestController.URIBASE)
@RequiredArgsConstructor
public class RelesRestController {

	protected final Logger log = LoggerFactory.getLogger(this.getClass());
	public static final String URIBASE = "api/v1/reles";

	private final RelesSrv relesSrv;
	private final RelesQuerySrv relesQuerySrv;
	private final RelesLoader relesLoader;
	private final RelesPersister relesPersister;
	final ObjJsonPrinter objJsonPrinter;


	/**
	 * 
	 * @param idRele
	 * @return
	 */
	@GetMapping("/{id}")
	public RestResponse<Rele> getRele(@PathVariable("id") String idRele) {

		log.info("REST - Get Rele with id={}.", idRele);
		
		Rele rele;
		try {
			rele = relesQuerySrv.getRele(idRele);
			log.debug("Rele INFO: {}", objJsonPrinter.print(rele));
			return new RestResponse<>(rele, HttpStatus.OK);
		} 
		catch (ItemNotFoundException e) {
        	log.warn("Rele with id={} not found.", idRele);
            return new RestResponse<>(HttpStatus.NOT_FOUND);
		}
	}

	
	/**
	 * 
	 * @param idRele
	 * @return
	 */
	@GetMapping("/{id}/history")
	public RestResponse<List<PersistibleRele>> getHistorial(
			@PathVariable("id") String idRele,
			@RequestParam(value="numdies", required=false, defaultValue="1") int numDies) {

		log.info("REST - Get Historial Rele with id={}, numdies={}.", idRele, numDies);
		
		//Consulta historial
		List<PersistibleRele> list;
		try {
			list = relesQuerySrv.getHistorial(idRele, numDies);
		} catch (ItemNotFoundException e) {
			log.warn("Rele with id={} not found.", idRele);
            return new RestResponse<>(HttpStatus.NOT_FOUND);
		}
		
		return new RestResponse<>(list, HttpStatus.OK);
	}

	@GetMapping("/{id}/evalrule")
	public RestResponse<ResultatEvalCondicions> evalRule(
			@PathVariable("id") String idRele,
			@RequestParam(required=true) String idRule) {

		log.info("REST - Eval Rule Rele with id={}, idRule={}.", idRele, idRule);
		
		//Persisteix canvis pendents
		ResultatEvalCondicions result;
		try {
			result = relesSrv.evalRule(idRele, idRule);
			return new RestResponse<>(result, HttpStatus.OK);
		} catch (ItemNotFoundException e) {
        	log.warn("Rele with id={} not found.", idRele);
            return new RestResponse<>(HttpStatus.NOT_FOUND);
		}
	}
	
	/**
	 * 
	 * @param idRele
	 * @param minuts
	 * @return
	 */
	@PutMapping("/{id}/ontemporal")
	public RestResponse<Rele> onTemporal(
			@PathVariable("id") String idRele,
			@RequestParam(required=false, defaultValue="0") int minuts,
			@RequestParam(required=false, defaultValue="0") int seconds,
			@RequestParam(required=false, defaultValue="false") boolean activarSlaves) {
		
		log.info("REST - Set Rele [ON TEMPORAL] with id={}, minuts={}, seconds={}, activarSlaves={}.", idRele, minuts, seconds, activarSlaves);

		Calendar cal = Calendar.getInstance();
		int horaIni = cal.get(Calendar.HOUR_OF_DAY);
		int minIni = cal.get(Calendar.MINUTE);
		int secIni = cal.get(Calendar.SECOND);
		
		int secondsTotal = minuts*60 + seconds;
		
		FranjaHoraria fr = new FranjaHoraria(horaIni, minIni, secIni, secondsTotal, activarSlaves);
		
		Rele rele;
		try {
			rele = relesSrv.setOnTemporal(idRele, fr);
			
			log.debug("Rele INFO: {}", objJsonPrinter.print(rele));
			return new RestResponse<>(rele, HttpStatus.OK);       
		} 
		catch (ItemNotFoundException e) {
        	log.warn("Rele with id={} not found.", idRele);
            return new RestResponse<>(HttpStatus.NOT_FOUND);
		}
	}
	
	/**
	 * 
	 * @param idRele
	 * @return
	 */
	@PutMapping("/{id}/canceltemporal")
	public RestResponse<Rele> cancelTemporal(@PathVariable("id") String idRele) {
		
		log.info("REST - Cancel Rele [OFF TEMPORAL] with id={}.", idRele);
		Rele rele;
		try {
			rele = relesSrv.cancelTemporal(idRele);
			
			log.debug("Rele INFO: {}", objJsonPrinter.print(rele));
			return new RestResponse<>(rele, HttpStatus.OK);       
		} 
		catch (ItemNotFoundException e) {
        	log.warn("Rele with id={} not found.", idRele);
            return new RestResponse<>(HttpStatus.NOT_FOUND);
		}
	}	
	
	/**
	 * 
	 * @param idRele
	 * @return
	 */
	@PutMapping("/{id}/on")
	public RestResponse<Rele> on(@PathVariable("id") String idRele) {
		log.info("REST - Set Rele [ON MANUAL] with id={}.", idRele);
		return this.setStateManual(idRele, true);        
	}
	
	/**
	 * 
	 * @param idRele
	 * @return
	 */
	@PutMapping("/{id}/off")
	public RestResponse<Rele> off(@PathVariable("id") String idRele) {
		log.info("REST - Set Rele [OFF MANUAL] with id={}.", idRele);
		return this.setStateManual(idRele, false);        
	}

	/**
	 * 
	 * @param idRele
	 * @return
	 */
	@PutMapping("/{id}/auto")
	public RestResponse<Rele> auto(@PathVariable("id") String idRele) {
		log.info("REST - Set Rele [AUTO] with id={}.", idRele);
		try {
			Rele rele = relesQuerySrv.getRele(idRele);
			relesSrv.setModeAuto(rele);
			
			return new RestResponse<>(rele, HttpStatus.OK);       
		} 
		catch (ItemNotFoundException e) {
        	log.warn("Rele with id={} not found.", idRele);
            return new RestResponse<>(HttpStatus.NOT_FOUND);
		}
	}

	/**
	 * 
	 * @param idRele
	 * @return
	 */
	@PutMapping("/{id}/manual")
	public RestResponse<Rele> manual(@PathVariable("id") String idRele) {
		log.info("REST - Set Rele [MANUAL] with id={}.", idRele);
		try {
			Rele rele = relesQuerySrv.getRele(idRele);
			relesSrv.setModeManual(rele);
			
			return new RestResponse<>(rele, HttpStatus.OK);       
		} 
		catch (ItemNotFoundException e) {
        	log.warn("Rele with id={} not found.", idRele);
            return new RestResponse<>(HttpStatus.NOT_FOUND);
		}
	}

	
	/**
	 * 
	 * @param idRele
	 * @return
	 */
	@PutMapping("/{id}/resetConsum")
	public RestResponse<Rele> resetConsum(
			@PathVariable("id") String idRele,
			@RequestParam(required=false, defaultValue="0") int valor) {
		log.info("REST - Reset Consum del Rele with id={} i valor={}.", idRele, valor);
		Rele rele;
		try {
			rele = relesSrv.resetConsum(idRele, valor);
			
			return new RestResponse<>(rele, HttpStatus.OK);       
		} 
		catch (ItemNotFoundException e) {
        	log.warn("Rele with id={} not found.", idRele);
            return new RestResponse<>(HttpStatus.NOT_FOUND);
		}
	}
	
	/**
	 * 
	 * @return
	 */
	@GetMapping("/allinfo")
	public RestResponse<Collection<Rele>> getAllInfo() {

		log.info("REST - Get State of All Reles.");
		
		Map<String, Rele> map = relesQuerySrv.getSyncReles();
		
		//Ordena per camp ordre
		List<Rele> list = map.entrySet().stream()
		        .sorted((p1,p2)-> {
		        	return Integer.valueOf(p1.getValue().getOrdre()).compareTo(p2.getValue().getOrdre());
		        }).map(Map.Entry::getValue)
		        .collect(Collectors.toList());
		
		for(Rele rele : list) {
			StateRele state = rele.getCopyStateRele();
			log.info("Rele id:{}, releON:{}, estatPin:{}", rele.getId(), state.isOn(), state.isGpioPinHigh());
		}
		
		return new RestResponse<>(list, HttpStatus.OK);        
	}

	/**
	 * 
	 * @return
	 */
	@GetMapping("/allstate")
	public RestResponse<List<String>> getAllState() {

		log.info("REST - Get State of All Reles View.");
		
		List<String> result = new ArrayList<>();
		for(Rele rele : relesQuerySrv.getSyncReles().values()) {
			StateRele state = rele.getCopyStateRele();
			
			log.info("Rele id:{}, releON:{}, estatPin:{}", rele.getId(), state.isOn(), state.isGpioPinHigh());
			
			StringBuilder txt = new StringBuilder();
			txt.append("Rele ID:").append(rele.getId()).append(", Rele ON:").append(state.isOn()).append(", Estat Pin:").append(state.isGpioPinHigh());
			result.add(txt.toString());
		}
		
		return new RestResponse<>(result, HttpStatus.OK);        
	}
	
	/**
	 * 
	 * @return
	 */
	@PutMapping("/reloadall")
	public RestResponse<Collection<Rele>> reload() {
		log.info("REST - RELOAD All Rele's.");
		
		//Persisteix en fitxer
		Collection<Rele> listRele = relesQuerySrv.getNoSyncReles().values();
		this.relesPersister.doPersistencia(listRele);
		
		try {
			relesLoader.loadJsonFile();
			relesSrv.loadHistory();
		} catch (IOException e) {
        	log.error("Error al recarregar Json de Rele's.", e);
            return new RestResponse<>(HttpStatus.SERVICE_UNAVAILABLE);
		}
		
		return getAllInfo();
	}
	
	
	private RestResponse<Rele> setStateManual(String idRele, boolean isHigh) {
		Rele rele;
		try {
			rele = relesSrv.setStateManual(idRele, isHigh);
			
			log.debug("Rele INFO: {}", objJsonPrinter.print(rele));
			return new RestResponse<>(rele, HttpStatus.OK);       
		} 
		catch (ItemNotFoundException e) {
        	log.warn("Rele with id={} not found.", idRele);
            return new RestResponse<>(HttpStatus.NOT_FOUND);
		}
	}		
}
