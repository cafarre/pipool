package es.fdvcode.pipool.srv.rele;

import com.pi4j.io.gpio.digital.DigitalOutput;

public interface GpioPinController {

	DigitalOutput provisionGpioPin(int gpioPin, String name, boolean isGpioPinHigh);
	DigitalOutput getGpioPin(int gpioPin);
}
