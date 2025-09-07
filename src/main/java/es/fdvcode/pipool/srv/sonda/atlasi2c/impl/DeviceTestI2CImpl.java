package es.fdvcode.pipool.srv.sonda.atlasi2c.impl;

import static es.fdvcode.pipool.common.Delay.delay;

import java.io.IOException;

import es.fdvcode.pipool.srv.sonda.atlasi2c.DeviceAtlasI2C;

/**
 * 
 * @author cfarrema
 *
 */
public class DeviceTestI2CImpl implements DeviceAtlasI2C {

	private final int address;
	
	private static final int NUM_BUTES_DEF = 31;
	private static final int DELAY_LONG = 2;
	private static final int DELAY_SHORT = 1;

	/**
	 * 
	 * @param bus
	 * @param address
	 * @throws IOException
	 * @throws UnsupportedBusNumberException 
	 */
	public DeviceTestI2CImpl(int address) {
		this.address = address;
	}
	
	@Override
	public int getAddress() {
		return address;
	}

	/**
	 * 
	 * @throws UnsupportedBusNumberException
	 * @throws IOException
	 */
	@Override
	public boolean isBusInitated()  {
		return true;
	}
	

	/**
	 * 
	 * @throws IOException
	 */
	@Override
	public void closeBus() {
	}

	/**
	 * 
	 * @return
	 * @throws IOException
	 */
	@Override
	public String readDevice() {
		return this.readDevice(NUM_BUTES_DEF);
	}
	
	/**
	 * 
	 * @param numBytes
	 * @return
	 * @throws IOException
	 */
	@Override
	public String readDevice(int numBytes) {
		if(this.address == 98) {
			return "650.00";
		}
		else if(this.address == 99) {
			return "7.20";
		}
		else if(this.address == 102) {
			return "3.755";
		}
		return "<?>";
	}

	/**
	 * 
	 * @param command
	 * @throws IOException
	 */
	@Override
	public void writeDevice(String command) {
	}
	
	/**
	 * 
	 * @param command
	 * @return
	 * @throws IOException
	 */
	@Override
	public String queryDevice(String command) {
		this.writeDevice(command);
		
		if(command.toUpperCase().startsWith("R") || command.toUpperCase().startsWith("CAL")) {
			delay(DELAY_LONG);
		}
		else if (command.toUpperCase().startsWith("SLEEP")) {
			return "sleep mode";
		}
		else {
			delay(DELAY_SHORT);
		}
		
		return this.readDevice();
	}

	@Override
	public void initBus() {
	}
}
