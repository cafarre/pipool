package es.fdvcode.pipool.restsrv.v1.sonda;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import es.fdvcode.pipool.common.ObjJsonPrinter;
import es.fdvcode.pipool.srv.sonda.SondesLoader;
import es.fdvcode.pipool.srv.sonda.SondesPersister;
import es.fdvcode.pipool.srv.sonda.SondesQuerySrv;
import es.fdvcode.pipool.srv.sonda.SondesSrv;

/**
 * Operacions propies PH:
 * - CalibrationTemp
 *  
 * @author cfarrema
 *
 */
@RestController
@RequestMapping(SondaTempCpuRestController.URIBASE)
public class SondaTempCpuRestController extends SondesRestController {

	public SondaTempCpuRestController(SondesLoader sondesLoader, SondesSrv sondesSrv, SondesQuerySrv sondesQuerySrv,
			SondesPersister sondesPersister, ObjJsonPrinter objJsonPrinter) {
		super(sondesLoader, sondesSrv, sondesQuerySrv, sondesPersister, objJsonPrinter);
	}

	public static final String URIBASE = SondesRestController.URIBASE + "/tempcpu";

	@Override
	protected int getAddressSonda() {
		return 1;
	}

	@Override
	protected String getTipusSonda() {
		return "TempCpu";
	}
}
