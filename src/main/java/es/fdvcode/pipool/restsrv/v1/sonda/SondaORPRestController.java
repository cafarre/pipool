package es.fdvcode.pipool.restsrv.v1.sonda;

import java.util.Collections;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import es.fdvcode.pipool.common.ObjJsonPrinter;
import es.fdvcode.pipool.restsrv.v1.response.RestResponse;
import es.fdvcode.pipool.srv.sonda.SondesLoader;
import es.fdvcode.pipool.srv.sonda.SondesPersister;
import es.fdvcode.pipool.srv.sonda.SondesQuerySrv;
import es.fdvcode.pipool.srv.sonda.SondesSrv;
import es.fdvcode.pipool.srv.sonda.atlasi2c.impl.SondaAtlasORP;

/**
 * Operacions propies ORP:
 * - CalibrationORP
 *  
 * @author cfarrema
 *
 */
@RestController
@RequestMapping(SondaORPRestController.URIBASE)
public class SondaORPRestController extends SondesAtlasRestController {

	public SondaORPRestController(SondesLoader sondesLoader, SondesSrv sondesSrv, SondesQuerySrv sondesQuerySrv,
			SondesPersister sondesPersister, ObjJsonPrinter objJsonPrinter) {
		super(sondesLoader, sondesSrv, sondesQuerySrv, sondesPersister, objJsonPrinter);
	}

	public static final String URIBASE = SondesRestController.URIBASE + "/orp";

	
	@Override
	protected int getAddressSonda() {
		return 98;
	}

	@Override
	protected String getTipusSonda() {
		return "ORP";
	}

	/**
	 * 
	 * @return
	 */
	@PutMapping("/setcalibration")
	public RestResponse<Void> calibration(@RequestParam(value="mv") String mV) {
		
		return this.genericSondaRestMethod("SET CALIBRATION mV", Void.class, Collections.singletonMap("Valor mV a calibrar", mV),
				(s,sa,p) -> {
					
					SondaAtlasORP sondaORP = (SondaAtlasORP)sa;  
					sondaORP.cmdCalibrationSet(s, mV);

					log.info("Sonda {} INFO: {}", getTipusSonda(), objJsonPrinter.print(s));
					return null;
				});
	}
}
