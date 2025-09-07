package es.fdvcode.pipool.srv.rele;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.pi4j.context.Context;
import com.pi4j.io.gpio.digital.DigitalOutput;
import com.pi4j.io.gpio.digital.DigitalOutputConfig;
import com.pi4j.io.gpio.digital.DigitalState;

public class GpioPinControllerProd implements GpioPinController {

	private Context pi4jContext;
	private final Map<Integer, DigitalOutput> provisionedGpios = new ConcurrentHashMap<>();
	
	public GpioPinControllerProd(Context pi4jContext) {
		this.pi4jContext = pi4jContext;
	}
	
	@Override
	public DigitalOutput provisionGpioPin(int gpioPin, String name, boolean isGpioPinHigh) {
		var config = DigitalOutputConfig.newBuilder(pi4jContext)
                .id("gpio-" + gpioPin)
                .name(name)
                .address(gpioPin)
                .initial(isGpioPinHigh ? DigitalState.HIGH : DigitalState.LOW)
                .build();
           
		DigitalOutput gpioPinOut = pi4jContext.digitalOutput().create(config);
		this.provisionedGpios.put(gpioPin, gpioPinOut);
		return gpioPinOut;
	}

	@Override
	public DigitalOutput getGpioPin(int gpioPin) {
		DigitalOutput gpioPinOut = this.provisionedGpios.get(gpioPin);
		if(gpioPinOut == null) {
			gpioPinOut = provisionGpioPin(gpioPin, String.valueOf(gpioPin), true);
		}
		return gpioPinOut;
	}
}
