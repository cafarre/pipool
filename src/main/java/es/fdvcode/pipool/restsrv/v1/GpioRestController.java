package es.fdvcode.pipool.restsrv.v1;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.pi4j.io.gpio.digital.DigitalOutput;

import es.fdvcode.pipool.restsrv.v1.response.RestResponse;
import es.fdvcode.pipool.srv.rele.GpioPinController;

/**
 * 
 * @author cfarrema
 *
 */

@RestController
@RequestMapping(GpioRestController.URIBASE)
public class GpioRestController {

	protected final Logger log;
	public static final String URIBASE = "api/v1/gpio";

	@Autowired
	private GpioPinController gPioCtr;

	/**
	 * default
	 */
	public GpioRestController() {
		this.log = LoggerFactory.getLogger(this.getClass());
	}


	/**
	 * 
	 * @param gpioPinNumber
	 * @return
	 */
	@PutMapping("/toggle")
	public RestResponse<Boolean> toggle(@RequestParam(value="pin") int gpioPinNumber) {

		log.info("Toggle GPIO {}.", gpioPinNumber);

		DigitalOutput gpioPin = gPioCtr.getGpioPin(gpioPinNumber);
		gpioPin.toggle();
		boolean result = gpioPin.isHigh();
		
		return new RestResponse<>(result, HttpStatus.OK);        
	}
	
	/**
	 * 
	 * @param gpioPinNumber
	 * @return
	 */
	@GetMapping("/state")
	public RestResponse<Boolean> getState(@RequestParam(value="pin") int gpioPinNumber) {

		log.info("Get State GPIO {}.", gpioPinNumber);

		DigitalOutput gpioPin = gPioCtr.getGpioPin(gpioPinNumber);
		boolean result = gpioPin.isHigh();
		
		return new RestResponse<>(result, HttpStatus.OK);        
	}	
}
