package es.fdvcode.pipool.restsrv.v1.sonda;

import java.util.Collections;
import org.springframework.web.bind.annotation.GetMapping;
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
import es.fdvcode.pipool.srv.sonda.atlasi2c.impl.SondaAtlasPH;
import es.fdvcode.pipool.srv.sonda.atlasi2c.impl.SondaAtlasPH.SlopeResponse;

/**
 * Operacions propies PH:
 * - CalibrationPH1Mid
 * - CalibrationPH2Low
 * - CalibrationPH3High
 * - GetSlope
 * - GetTempCompensation
 * - SetTempCompensation
 *  
 * @author cfarrema
 *
 */
@RestController
@RequestMapping(SondaPHRestController.URIBASE)
public class SondaPHRestController extends SondesAtlasRestController {

	public SondaPHRestController(SondesLoader sondesLoader, SondesSrv sondesSrv, SondesQuerySrv sondesQuerySrv,
			SondesPersister sondesPersister, ObjJsonPrinter objJsonPrinter) {
		super(sondesLoader, sondesSrv, sondesQuerySrv, sondesPersister, objJsonPrinter);
	}


	public static final String URIBASE = SondesRestController.URIBASE + "/ph";

	@Override
	protected int getAddressSonda() {
		return 99;
	}

	@Override
	protected String getTipusSonda() {
		return "PH";
	}

	/**
	 * 
	 * @return
	 */
	@PutMapping("/setcalibrationmid")
	public RestResponse<Void> calibrationMid(@RequestParam String ph) {
		return this.genericSondaRestMethod("SET CALIBRATION PH MID", Void.class, Collections.singletonMap("Valor ph a calibrar", ph),
			(s,sa,p) -> {
				
				SondaAtlasPH sondaPH = (SondaAtlasPH)sa;  
				sondaPH.cmdCalibrationMid(s, ph);

				return null;
			});
	}		
	
	/**
	 * 
	 * @return
	 */
	@PutMapping("/setcalibrationlow")
	public RestResponse<Void> calibrationLow(@RequestParam String ph) {
		return this.genericSondaRestMethod("SET CALIBRATION PH LOW", Void.class, Collections.singletonMap("Valor ph a calibrar", ph),
			(s,sa,p) -> {
				
				SondaAtlasPH sondaPH = (SondaAtlasPH)sa;  
				sondaPH.cmdCalibrationLow(s, ph);

				return null;
			});
	}		
	
	/**
	 * 
	 * @return
	 */
	@PutMapping("/setcalibrationhigh")
	public RestResponse<Void> calibrationHigh(@RequestParam String ph) {
		return this.genericSondaRestMethod("SET CALIBRATION PH HIGH", Void.class, Collections.singletonMap("Valor ph a calibrar", ph),
			(s,sa,p) -> {
				
				SondaAtlasPH sondaPH = (SondaAtlasPH)sa;  
				sondaPH.cmdCalibrationHigh(s, ph);

				return null;
			});
	}
	
	/**
	 * 
	 * @return
	 */
	@GetMapping("/slope")
	public RestResponse<SlopeResponse> slope() {
		return this.genericSondaRestMethod("GET SLOPE", SlopeResponse.class,
			(s,sa,p) -> {
				
				SondaAtlasPH sondaPH = (SondaAtlasPH)sa;  
				SlopeResponse result = sondaPH.cmdGetSlope(s);

				log.info("Sonda {} - SLOPE = {}", s.getId(), objJsonPrinter.print(result));
				return result;
			});
	}		
	
	
	/**
	 * 
	 * @return
	 */
	@GetMapping("/gettempcompensation")
	public RestResponse<String> getTempCompensation() {
		return this.genericSondaRestMethod("GET TEMP COMPENSATION", String.class,
			(s,sa,p) -> {
				
				SondaAtlasPH sondaPH = (SondaAtlasPH)sa;  
				String result = sondaPH.cmdGetTempCompensation(s);

				log.info("Sonda {} - TEMP COMPENSATION = {}", s.getId(), result);
				return result;
			});
	}
		
	
	/**
	 * 
	 * @return
	 */
	@PutMapping("/settempcompensation")
	public RestResponse<Void> setTempCompensation(@RequestParam String temp) {
		return this.genericSondaRestMethod("SET TEMP COMPENSATION", Void.class, Collections.singletonMap("Valor temperatura", temp),
			(s,sa,p) -> {
				
				SondaAtlasPH sondaPH = (SondaAtlasPH)sa;  
				sondaPH.cmdSetTempCompensation(s, temp);

				return null;
			});
	}		
}
