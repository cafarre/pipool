package es.fdvcode.pipool.restsrv.v1.sonda;

import java.io.IOException;
import java.util.Collection;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import es.fdvcode.pipool.common.ObjJsonPrinter;
import es.fdvcode.pipool.model.sonda.PersistibleSondaBomba;
import es.fdvcode.pipool.model.sonda.Sonda;
import es.fdvcode.pipool.model.sonda.StateSonda;
import es.fdvcode.pipool.restsrv.v1.response.RestResponse;
import es.fdvcode.pipool.srv.sonda.SondesLoader;
import es.fdvcode.pipool.srv.sonda.SondesPersister;
import es.fdvcode.pipool.srv.sonda.SondesQuerySrv;
import es.fdvcode.pipool.srv.sonda.SondesSrv;
import lombok.RequiredArgsConstructor;

/**
 * 
 * @author cfarrema
 *
 */

@RestController
@RequiredArgsConstructor
public abstract class SondesRestController {

	protected final Logger log = LoggerFactory.getLogger(this.getClass());
	public static final String URIBASE = "api/v1/sonda";

	private final SondesLoader sondesLoader;
	protected final SondesSrv sondesSrv;
	protected final SondesQuerySrv sondesQuerySrv;
	private final SondesPersister sondesPersister;
	final ObjJsonPrinter objJsonPrinter;

	
	protected abstract int getAddressSonda();
	
	protected abstract String getTipusSonda();
	
	/**
	 * 
	 * @return
	 */
	@GetMapping("/get")
	public RestResponse<Sonda> getSonda() {


		log.info("REST - Get Sonda {} with address={}.", getTipusSonda(), this.getAddressSonda());
		
		Sonda sonda;
		try {
			sonda = sondesQuerySrv.getSonda(this.getAddressSonda());
			log.info("Sonda {} INFO: {}", getTipusSonda(), objJsonPrinter.print(sonda));
			return new RestResponse<>(sonda, HttpStatus.OK);
		} 
		catch (Exception e) {
        	log.warn("Sonda {} with address={} not found.", getTipusSonda(), this.getAddressSonda(), e);
            return new RestResponse<>(HttpStatus.NOT_FOUND);
		}
	}

	/**
	 * 
	 * @return
	 */
	@GetMapping("/history")
	public RestResponse<List<PersistibleSondaBomba>> getHistory(@RequestParam(value="numdies", required=false, defaultValue="1") int numDies) {

		log.info("REST - Get Historial Sonda {} with address={}.", getTipusSonda(), this.getAddressSonda());
		
		try {
			List<PersistibleSondaBomba> list = sondesQuerySrv.getHistorial(this.getAddressSonda(), numDies);
			return new RestResponse<>(list, HttpStatus.OK);
		} 
		catch (Exception e) {
        	log.warn("Sonda {} with address={} not found.", getTipusSonda(), this.getAddressSonda(), e);
            return new RestResponse<>(HttpStatus.NOT_FOUND);
		}
	}
	
	/**
	 * 
	 * @return
	 */
	@GetMapping("/getall")
	public RestResponse<List<Sonda>> getAllSondes() {

		log.info("REST - Get All Sondes.");
		
		for(Sonda sonda: sondesQuerySrv.getListSondes()) {
			StateSonda state = sonda.getStateSonda();
			String valor = "";
			if(state!=null) {
				valor = state.getValor();
			}
			log.info("Sonda Id:{}, Valor:{}, Unitats:{}", sonda.getId(), valor, sonda.getUnitats());			
		}
		
		return new RestResponse<>(sondesQuerySrv.getListSondes(), HttpStatus.OK);
	}

	/**
	 * 
	 * @return
	 */
	@PutMapping("/reloadall")
	public RestResponse<List<Sonda>> reload() {
		log.info("REST - RELOAD All Sonda's.");
		
		Collection<Sonda> listSondes = sondesQuerySrv.getListSondes();
		this.sondesPersister.doPersistencia(listSondes);
		
		try {
			sondesLoader.loadJsonFile();
		} catch (IOException e) {
        	log.error("Error al recarregar Json de Sondes's.", e);
            return new RestResponse<>(HttpStatus.SERVICE_UNAVAILABLE);
		}
		
		return getAllSondes();
	}
}
