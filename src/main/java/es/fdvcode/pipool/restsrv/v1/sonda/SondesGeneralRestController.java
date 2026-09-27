package es.fdvcode.pipool.restsrv.v1.sonda;

import java.io.IOException;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import es.fdvcode.pipool.common.ObjJsonPrinter;
import es.fdvcode.pipool.model.sonda.Sonda;
import es.fdvcode.pipool.restsrv.v1.dto.SondaConfigDto;
import es.fdvcode.pipool.restsrv.v1.response.RestResponse;
import es.fdvcode.pipool.srv.ItemNotFoundException;
import es.fdvcode.pipool.srv.sonda.SondesQuerySrv;
import es.fdvcode.pipool.srv.sonda.SondesSrv;
import lombok.RequiredArgsConstructor;

/**
 * Controlador REST general per a la gestió de sondes per identificador (ID).
 * Permet consultar i editar qualsevol sonda (Atlas, rPi, Shelly, etc.).
 * 
 * @author cfarrema
 */
@RestController
@RequestMapping({ "api/v1/sondes", "api/v1/sonda" })
@RequiredArgsConstructor
public class SondesGeneralRestController {

	private final Logger log = LoggerFactory.getLogger(this.getClass());

	private final SondesQuerySrv sondesQuerySrv;
	private final SondesSrv sondesSrv;
	private final ObjJsonPrinter objJsonPrinter;

	/**
	 * Obté la llista de totes les sondes configurades.
	 * 
	 * @return
	 */
	@GetMapping
	public RestResponse<List<Sonda>> getAllSondes() {
		log.info("REST - Get All Sondes (General Controller).");
		return new RestResponse<>(sondesQuerySrv.getListSondes(), HttpStatus.OK);
	}

	/**
	 * Obté la informació d'una sonda pel seu ID (ex: sonda_ph, sonda_orp, sonda_temp, temp_shelly_flood, temp_cpu_rpi).
	 * 
	 * @param idSonda
	 * @return
	 */
	@GetMapping("/{id}")
	public RestResponse<Sonda> getSondaById(@PathVariable("id") String idSonda) {
		log.info("REST - Get Sonda with id={}.", idSonda);
		try {
			Sonda sonda = sondesQuerySrv.getSonda(idSonda);
			log.info("Sonda id={} INFO: {}", idSonda, objJsonPrinter.print(sonda));
			return new RestResponse<>(sonda, HttpStatus.OK);
		} catch (ItemNotFoundException e) {
			log.warn("Sonda with id={} not found.", idSonda);
			return new RestResponse<>(HttpStatus.NOT_FOUND);
		}
	}

	/**
	 * Actualitza la configuració d'una sonda pel seu ID (límits, relé corrector, nom, etc.).
	 * 
	 * @param idSonda
	 * @param dto
	 * @return
	 */
	@PutMapping("/{id}/config")
	public RestResponse<Sonda> updateConfig(
			@PathVariable("id") String idSonda,
			@RequestBody SondaConfigDto dto) {

		log.info("REST - Update Config Sonda with id={}.", idSonda);
		try {
			Sonda sonda = sondesSrv.updateConfig(idSonda, dto);
			return new RestResponse<>(sonda, HttpStatus.OK);
		} catch (ItemNotFoundException e) {
			log.warn("Sonda with id={} not found.", idSonda);
			return new RestResponse<>(HttpStatus.NOT_FOUND);
		} catch (IllegalArgumentException e) {
			log.warn("Dades invàlides en actualitzar config de la sonda {}: {}", idSonda, e.getMessage());
			return new RestResponse<>(HttpStatus.BAD_REQUEST, e.getMessage());
		} catch (IOException e) {
			log.error("Error d'E/S al persistir config de la sonda {}.", idSonda, e);
			return new RestResponse<>(HttpStatus.INTERNAL_SERVER_ERROR, "Error al guardar fitxer de configuració");
		}
	}

	/**
	 * Actualitza la configuració d'una sonda (àlies de /{id}/config).
	 * 
	 * @param idSonda
	 * @param dto
	 * @return
	 */
	@PutMapping("/{id}")
	public RestResponse<Sonda> updateSonda(
			@PathVariable("id") String idSonda,
			@RequestBody SondaConfigDto dto) {
		return this.updateConfig(idSonda, dto);
	}

}
