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
import es.fdvcode.pipool.srv.sonda.atlasi2c.impl.SondaAtlasRTD;

/**
 * Operacions propies PH:
 * - CalibrationTemp
 *  
 * @author cfarrema
 *
 */
@RestController
@RequestMapping(SondaTempRestController.URIBASE)
public class SondaTempRestController extends SondesAtlasRestController {

	public SondaTempRestController(SondesLoader sondesLoader, SondesSrv sondesSrv, SondesQuerySrv sondesQuerySrv,
			SondesPersister sondesPersister, ObjJsonPrinter objJsonPrinter) {
		super(sondesLoader, sondesSrv, sondesQuerySrv, sondesPersister, objJsonPrinter);
	}


	public static final String URIBASE = SondesRestController.URIBASE + "/temp";

	@Override
	protected int getAddressSonda() {
		return 102;
	}

	@Override
	protected String getTipusSonda() {
		return "RTD";
	}


	/**
	 * 
	 * @return
	 */
	@PutMapping("/setcalibration")
	public RestResponse<Void> calibration(@RequestParam String temp) {
		return this.genericSondaRestMethod("SET CALIBRATION Temp", Void.class, Collections.singletonMap("Valor temp a calibrar", temp),
				(s,sa,p) -> {
					
					SondaAtlasRTD sondaRTD = (SondaAtlasRTD)sa;  
					sondaRTD.cmdCalibrationSet(s, temp);

					log.info("Sonda {} INFO: {}", getTipusSonda(), objJsonPrinter.print(s));
					return null;
				});
	}		
}
