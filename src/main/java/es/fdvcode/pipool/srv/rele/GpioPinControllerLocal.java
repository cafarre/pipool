package es.fdvcode.pipool.srv.rele;

import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import com.pi4j.common.Metadata;
import com.pi4j.context.Context;
import com.pi4j.exception.InitializeException;
import com.pi4j.exception.ShutdownException;
import com.pi4j.io.binding.DigitalBinding;
import com.pi4j.io.exception.IOException;
import com.pi4j.io.gpio.digital.DigitalOutput;
import com.pi4j.io.gpio.digital.DigitalOutputConfig;
import com.pi4j.io.gpio.digital.DigitalOutputProvider;
import com.pi4j.io.gpio.digital.DigitalState;
import com.pi4j.io.gpio.digital.DigitalStateChangeListener;

public class GpioPinControllerLocal implements GpioPinController {

	@Override
	public DigitalOutput provisionGpioPin(int gpioPin, String name, boolean isGpioPinHigh) {
		GpioPinLocal pin = new GpioPinLocal(name, isGpioPinHigh); 
		return pin;
	}

	@Override
	public DigitalOutput getGpioPin(int gpioPin) {
		return provisionGpioPin(gpioPin, String.valueOf(gpioPin), true);
	}


	
	public class GpioPinLocal implements DigitalOutput {

		private String name;
		private boolean isHigh=false;
		
		public GpioPinLocal(String name, boolean isHigh) {
			this.name = name;
			this.isHigh = isHigh;
		}
		
		@Override
		public DigitalState state() {
			return DigitalState.getState(isHigh);
		}

		@Override
		public DigitalOutput addListener(DigitalStateChangeListener... listener) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public DigitalOutput removeListener(DigitalStateChangeListener... listener) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public DigitalOutputConfig config() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public DigitalOutput name(String name) {
			return null;
		}

		@Override
		public DigitalOutput description(String description) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public DigitalOutputProvider provider() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public Object initialize(Context context) throws InitializeException {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public Object shutdown(Context context) throws ShutdownException {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public String id() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public String name() {
			return name;
		}

		@Override
		public String description() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public Metadata metadata() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public boolean isOn() {
			return this.state().equals(DigitalState.HIGH);
		}

		@SuppressWarnings("rawtypes")
		@Override
		public DigitalOutput bind(DigitalBinding... binding) {
			// TODO Auto-generated method stub
			return null;
		}

		@SuppressWarnings("rawtypes")
		@Override
		public DigitalOutput unbind(DigitalBinding... binding) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public DigitalOutput on() throws IOException {
			this.isHigh = true;
			return this;
		}

		@Override
		public DigitalOutput off() throws IOException {
			this.isHigh = false;
			return this;
		}

		@Override
		public DigitalOutput state(DigitalState state) throws IOException {
			if(state.equals(DigitalState.HIGH)) {
				return this.on();
			}
			else {
				return this.off();
			}
		}

		@Override
		public DigitalOutput pulse(int interval, TimeUnit unit, DigitalState state, Callable<Void> callback)
				throws IOException {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public Future<?> pulseAsync(int interval, TimeUnit unit, DigitalState state, Callable<Void> callback) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public DigitalOutput blink(int delay, int duration, TimeUnit unit, DigitalState state,
				Callable<Void> callback) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public Future<?> blinkAsync(int delay, int duration, TimeUnit unit, DigitalState state,
				Callable<Void> callback) {
			// TODO Auto-generated method stub
			return null;
		}
		

	}

}
