package es.fdvcode.pipool.restsrv.v1.sonda;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import es.fdvcode.pipool.common.ObjJsonPrinter;
import es.fdvcode.pipool.common.ParameterizedMessage;
import es.fdvcode.pipool.model.sonda.Sonda;
import es.fdvcode.pipool.model.sonda.Sonda.TipusSonda;
import es.fdvcode.pipool.model.sonda.StateSonda;
import es.fdvcode.pipool.restsrv.v1.response.RestResponse;
import es.fdvcode.pipool.srv.ItemNotFoundException;
import es.fdvcode.pipool.srv.sonda.SondesLoader;
import es.fdvcode.pipool.srv.sonda.SondesPersister;
import es.fdvcode.pipool.srv.sonda.SondesQuerySrv;
import es.fdvcode.pipool.srv.sonda.SondesSrv;
import es.fdvcode.pipool.srv.sonda.atlasi2c.SondaAtlasErrorException;
import es.fdvcode.pipool.srv.sonda.atlasi2c.impl.SondaAtlas;
import es.fdvcode.pipool.srv.sonda.atlasi2c.impl.SondaAtlas.DeviceInfoResponse;
import es.fdvcode.pipool.srv.sonda.atlasi2c.impl.SondaAtlas.DeviceStatusResponse;

/**
 * Operacions Comuns:
 * - read
 * - readAll
 * 
 * - ledon
 * - ledoff
 * - ledstate
 * - ledonall
 * - ledoffall
 * 
 * - deviceinfo
 * - devicestatus
 * 
 * - sleep
 * - sleepall
 * - wakeup
 * - wakeupall
 * 
 * - CalibrationClear
 * - CalibrationState
 * 
 * - execCommand
 * 
 * Operacions propies PH:
 * - CalibrationPH1Mid
 * - CalibrationPH2Low
 * - CalibrationPH3High
 * - GetSlope
 * - GetTempCompensation
 * - SetTempCompensation
 * 
 *
 * Operacions propies ORP:
 * - CalibrationORP
 * 
 * Operacions propies RTD: 
 * - CalibrationTemp
 * 
 * 
 * 
 * @author cfarrema
 *
 */

@RestController
public abstract class SondesAtlasRestController extends SondesRestController {

	public SondesAtlasRestController(SondesLoader sondesLoader, SondesSrv sondesSrv, SondesQuerySrv sondesQuerySrv,
			SondesPersister sondesPersister, ObjJsonPrinter objJsonPrinter) {
		super(sondesLoader, sondesSrv, sondesQuerySrv, sondesPersister, objJsonPrinter);
	}

	protected final Logger log = LoggerFactory.getLogger(this.getClass());
	
	/**
	 * 
	 * @param txtAction
	 * @param type
	 * @param met
	 * @return
	 */
	protected <T> RestResponse<T> genericSondaRestMethod(String txtAction, Class<T> type, CallbackMethod<T> met) {
		return this.genericSondaRestMethod(txtAction, type, null, met);
	}
	
	/**
	 * 
	 * @param txtAction
	 * @param params
	 * @param type
	 * @param met
	 * @return
	 */
	protected <T> RestResponse<T> genericSondaRestMethod(String txtAction, Class<T> type, Map<String, String> params, CallbackMethod<T> met) {
		StringBuilder strTrace = new StringBuilder();
		strTrace.append("REST - {} en Sonda {} with address={}.");
		
		//Arrays.asList(txtAction, getTipusSonda(), this.getAddressSonda());
		List<String> lstParams = new ArrayList<>();
		lstParams.add(txtAction); 
		lstParams.add(getTipusSonda());
		lstParams.add(String.valueOf(this.getAddressSonda()));
		
		if(params!=null) {
			int i=1;
			for(String key : params.keySet()) {
				strTrace.append(" Valor [{}]-[{}]=[{}].");
				lstParams.add(String.valueOf(i));
				lstParams.add(key);
				lstParams.add(params.get(key));
				i++;
			}
		}
		
		log.info(strTrace.toString(), lstParams.toArray());
		
		Sonda sonda;
		try {
			sonda = sondesQuerySrv.getSonda(this.getAddressSonda());
			
			SondaAtlas sondaAtlas = sondesQuerySrv.getSondaAtlas(sonda);
			T result = type.cast(met.execute(sonda, sondaAtlas, params));
			
			if(result==null) {
				return new RestResponse<>(HttpStatus.OK);
			}
			else {
				return new RestResponse<>(result, HttpStatus.OK);
			}
		} 
		catch (ItemNotFoundException e) {
			ParameterizedMessage msg = new ParameterizedMessage("ERROR al fer {}. Sonda {} with address={} not found.", txtAction, getTipusSonda(), this.getAddressSonda());
        	log.warn(msg.getFormattedMessage());
            return new RestResponse<>(HttpStatus.NOT_FOUND, msg.getFormattedMessage());
		} catch (SondaAtlasErrorException e) {
			ParameterizedMessage msg = new ParameterizedMessage("ERROR al fer {} en Sonda {} with address={}. CodiError:{} - IsErrorExpected:{}.", txtAction, getTipusSonda(), this.getAddressSonda(), e.getResponseCode(), e.isReponseCodeExpected(), e);
			log.error(msg.getFormattedMessage());
			return new RestResponse<>(HttpStatus.SERVICE_UNAVAILABLE, msg.getFormattedMessage());
		}		
		catch (NumberFormatException | UnsupportedOperationException | InterruptedException e) {
			ParameterizedMessage msg = new ParameterizedMessage("ERROR al fer {} en Sonda {} rPi:{} with address={}.", txtAction, getTipusSonda(), this.getAddressSonda(), e);
			log.error(msg.getFormattedMessage());
			return new RestResponse<>(HttpStatus.SERVICE_UNAVAILABLE, msg.getFormattedMessage());
		} 
		catch (Exception e) {
			ParameterizedMessage msg = new ParameterizedMessage("ERROR al fer {} en Sonda {} with address={}.", txtAction, getTipusSonda(), this.getAddressSonda(), e); 
			log.error(msg.getFormattedMessage());
			return new RestResponse<>(HttpStatus.SERVICE_UNAVAILABLE, msg.getFormattedMessage());
		}
	}
	
	
	/**
	 * 
	 * @return
	 */
	@GetMapping("/read")
	public RestResponse<Sonda> read() {
		return this.genericSondaRestMethod("READ", Sonda.class,
				(s,sa,p) -> {
					sondesSrv.readSonda(s, true);
					log.info("Sonda {} INFO: {}", getTipusSonda(), objJsonPrinter.print(s));
					return s;
				});
	}
	

	/**
	 * 
	 * @return
	 */
	@GetMapping("/readall")
	public RestResponse<List<Sonda>> readAll() {

		log.info("REST - Read State of All Sondes.");
		
		for(Sonda sonda: sondesQuerySrv.getListSondes()) {
			try {
				sondesSrv.readSonda(sonda, true);

				StateSonda state = sonda.getStateSonda();
				log.info("Sonda Id:{}, Valor:{}, Unitats:{}", sonda.getId(), state.getValor(), sonda.getUnitats());
			} 
			catch (IOException e) {
				ParameterizedMessage msg = new ParameterizedMessage("ERROR al fer read de la SONDA:{}.", sonda.getId(), e);
				log.error(msg.getFormattedMessage());
			}
			catch (SondaAtlasErrorException e) {
				ParameterizedMessage msg = new ParameterizedMessage("ERROR al fer read de la SONDA Atlas:{}. CodiError:{} - IsErrorExpected:{}.", sonda.getId(), e.getResponseCode(), e.isReponseCodeExpected(), e);
				log.error(msg.getFormattedMessage());
			}
			catch (NumberFormatException | UnsupportedOperationException | InterruptedException e) {
				ParameterizedMessage msg = new ParameterizedMessage("ERROR al fer read de la SONDA rPi:{}.", sonda.getId(), e);
				log.error(msg.getFormattedMessage());
			}
			catch (Exception e) {
				ParameterizedMessage msg = new ParameterizedMessage("ERROR al fer read de la SONDA Atlas:{}.", sonda.getId(), e);
				log.error(msg.getFormattedMessage());
			}

		}
		
		return new RestResponse<>(sondesQuerySrv.getListSondes(), HttpStatus.OK);        
	}

	
	/**
	 * 
	 * @return
	 */
	@PutMapping("/ledon")
	public RestResponse<Void> ledOn() {

		return this.genericSondaRestMethod("LED ON", Void.class,
				(s,sa,p) -> {
					sa.cmdLedOn(s);
					return null;
				});
	}
	

	/**
	 * 
	 * @return
	 */
	@PutMapping("/ledoff")
	public RestResponse<Void> ledOff() {
		return this.genericSondaRestMethod("LED OFF", Void.class,
				(s,sa,p) -> {
					sa.cmdLedOff(s);
					return null;
				});
	}	
		
	
	/**
	 * 
	 * @return
	 */
	@GetMapping("/ledstate")
	public RestResponse<Boolean> ledState() {
		return this.genericSondaRestMethod("LED GET STATE", Boolean.class,
				(s,sa,p) -> {
					boolean result = sa.cmdLedState(s);
					log.info("Sonda {} - LED is ON? {}", s.getId(), result);
					return result;
				});
	}

	
	/**
	 * 
	 * @return
	 */
	@PutMapping("/ledonall")
	public RestResponse<Boolean> ledOnAll() {

		log.info("REST - LED ON All Sondes.");
		
		boolean result = true;
		for(Sonda sonda: sondesQuerySrv.getListSondes()) {
			if(TipusSonda.Atlas.equals(sonda.getTipusSonda())) {
				try {
					SondaAtlas sondaAtlas = sondesQuerySrv.getSondaAtlas(sonda);
					sondaAtlas.cmdLedOn(sonda);
				} catch (SondaAtlasErrorException e) {
					ParameterizedMessage msg = new ParameterizedMessage("ERROR al fer LED ON a la SONDA:{} with address={}. CodiError:{} - IsErrorExpected:{}.", sonda.getId(), this.getAddressSonda(), e.getResponseCode(), e.isReponseCodeExpected(), e);
					log.error(msg.getFormattedMessage());
					result = false;
				} 
				catch (Exception e) {
					ParameterizedMessage msg = new ParameterizedMessage("ERROR al fer LED ON a la SONDA:{}.", sonda.getId(), e);
					log.error(msg.getFormattedMessage());
					result = false;
				}
			}
		}
		
		return new RestResponse<>(result, HttpStatus.OK);        
	}	

	/**
	 * 
	 * @return
	 */
	@PutMapping("/ledoffall")
	public RestResponse<Boolean> ledOffAll() {

		log.info("REST - LED OFF All Sondes.");
		
		boolean result = true;
		for(Sonda sonda: sondesQuerySrv.getListSondes()) {
			if(TipusSonda.Atlas.equals(sonda.getTipusSonda())) {
				try {
					SondaAtlas sondaAtlas = sondesQuerySrv.getSondaAtlas(sonda);
					sondaAtlas.cmdLedOff(sonda);
				} catch (SondaAtlasErrorException e) {
					ParameterizedMessage msg = new ParameterizedMessage("ERROR al fer LED ON a la SONDA:{} with address={}. CodiError:{} - IsErrorExpected:{}.", sonda.getId(), this.getAddressSonda(), e.getResponseCode(), e.isReponseCodeExpected(), e);
					log.error(msg.getFormattedMessage());
					result = false;
				} 
				catch (Exception e) {
					ParameterizedMessage msg = new ParameterizedMessage("ERROR al fer LED ON a la SONDA:{}.", sonda.getId(), e);
					log.error(msg.getFormattedMessage());
					result = false;
				}
			}
		}
		
		return new RestResponse<>(result, HttpStatus.OK);        
	}	
	
	
	/**
	 * 
	 * @return
	 */
	@GetMapping("/info")
	public RestResponse<DeviceInfoResponse> info() {

		return this.genericSondaRestMethod("GET INFO", DeviceInfoResponse.class,
				(s,sa,p) -> {
					DeviceInfoResponse result = sa.cmdGetDeviceInfo(s);
					log.info("Sonda {} DeviceInfoResponse: {}", getTipusSonda(), objJsonPrinter.print(result));
					return result;
				});
	}
	
	
	/**
	 * 
	 * @return
	 */
	@GetMapping("/status")
	public RestResponse<DeviceStatusResponse> status() {
		
		return this.genericSondaRestMethod("GET STATUS", DeviceStatusResponse.class,
				(s,sa,p) -> {
					DeviceStatusResponse result = sa.cmdGetDeviceStatus(s);
					log.info("Sonda {} DeviceStatusResponse: {}", getTipusSonda(), objJsonPrinter.print(result));
					return result;
				});
	}
	
	
	/**
	 * 
	 * @return
	 */
	@PutMapping("/sleep")
	public RestResponse<Void> sleep() {
		return this.genericSondaRestMethod("SLEEP", Void.class,
				(s,sa,p) -> {
					sa.cmdSleep(s);
					log.info("Sleep Mode ON en Sonda {} .", s.getId());
					
					return null;
				});
	}
	
	
	/**
	 * 
	 * @return
	 */
	@PutMapping("/sleepall")
	public RestResponse<Boolean> sleepAll() {

		log.info("REST - SLEEP All Sondes.");
		
		boolean result = true;
		for(Sonda sonda: sondesQuerySrv.getListSondes()) {
			if(TipusSonda.Atlas.equals(sonda.getTipusSonda())) {
				try {
					SondaAtlas sondaAtlas = sondesQuerySrv.getSondaAtlas(sonda);
					sondaAtlas.cmdSleep(sonda);
				} catch (SondaAtlasErrorException e) {
					ParameterizedMessage msg = new ParameterizedMessage("ERROR al fer SLEEP a la SONDA:{} with address={}. CodiError:{} - IsErrorExpected:{}.", sonda.getId(), this.getAddressSonda(), e.getResponseCode(), e.isReponseCodeExpected(), e);
					log.error(msg.getFormattedMessage());
					result = false;
				} 
				catch (Exception e) {
					ParameterizedMessage msg = new ParameterizedMessage("ERROR al fer SLEEP a la SONDA:{}.", sonda.getId(), e);
					log.error(msg.getFormattedMessage());
					result = false;					
				}
			}
		}
		
		return new RestResponse<>(result, HttpStatus.OK);        
	}
	
	/**
	 * 
	 * @return
	 */
	@PutMapping("/wakeup")
	public RestResponse<Void> wakeup() {
		return this.genericSondaRestMethod("WAKEUP", Void.class,
				(s,sa,p) -> {
					sa.cmdTakeReading(s, true);
					log.info("WAKEUP - Sleep Mode OFF en Sonda {} .", s.getId());
					
					return null;
				});
	}

	/**
	 * 
	 * @return
	 */
	@PutMapping("/wakeupall")
	public RestResponse<Boolean> wakeupAll() {

		log.info("REST - SLEEP All Sondes.");
		
		boolean result = true;
		for(Sonda sonda: sondesQuerySrv.getListSondes()) {
			if(TipusSonda.Atlas.equals(sonda.getTipusSonda())) {
				try {
					SondaAtlas sondaAtlas = sondesQuerySrv.getSondaAtlas(sonda);
					sondaAtlas.cmdTakeReading(sonda, true);
				} catch (SondaAtlasErrorException e) {
					ParameterizedMessage msg = new ParameterizedMessage("ERROR al fer WAKEUP a la SONDA:{} with address={}. CodiError:{} - IsErrorExpected:{}.", sonda.getId(), this.getAddressSonda(), e.getResponseCode(), e.isReponseCodeExpected(), e);
					log.error(msg.getFormattedMessage());
					result = false;
				} 
				catch (Exception e) {
					ParameterizedMessage msg = new ParameterizedMessage("ERROR al fer WAKEUP a la SONDA:{}.", sonda.getId(), e);
					log.error(msg.getFormattedMessage());
					result = false;
				}
			}
		}
		
		return new RestResponse<>(result, HttpStatus.OK);        
	}	
	
	
	/**
	 * 
	 * @return
	 */
	@PutMapping("/calibrationclear")
	public RestResponse<Void> calibrationClear() {
		return this.genericSondaRestMethod("CALIBRATION CLEAR", Void.class,
				(s,sa,p) -> {
					sa.cmdCalibrationClear(s);
					log.info("CALIBRATION CLEAR en Sonda {} .", s.getId());
					
					return null;
				});
	}		
	

	/**
	 * 
	 * @return
	 */
	@GetMapping("/calibrationstate")
	public RestResponse<Integer> calibrationGetState() {
		return this.genericSondaRestMethod("CALIBRATION GET STATE", Integer.class,
				(s,sa,p) -> {
					int result = sa.cmdCalibrationGetState(s);
					
					log.info("SONDA: {} - CALIBRATION GET STATE: {}", s.getId(),result);
					return result;
				});
	}		
	
	
	/**
	 * 
	 * @param cmd
	 * @return
	 */
	@PutMapping("/execmd")
	public RestResponse<String> execCommand(@RequestParam String cmd) {
		return this.genericSondaRestMethod("EXEC COMMAND", String.class, Collections.singletonMap("Comanda", cmd), 
				(s,sa,p) -> {
					String result = sa.execCmd(s, cmd);
					
					log.info("SONDA: {} - Resultat EXEC COMMAND:{} --> {}", s.getId(), cmd, result);
					return result;
				});
	}
	
	/**
	 * 
	 * @author cfarrema
	 *
	 * @param <T>
	 */
	@FunctionalInterface
	interface CallbackMethod<T> {

		T execute(Sonda sonda, SondaAtlas sondaAtlas, Map<String, String> params) throws IOException, SondaAtlasErrorException, NumberFormatException, UnsupportedOperationException, InterruptedException;
	}
}
