package es.fdvcode.pipool.srv.sonda.atlasi2c.impl;

import static es.fdvcode.pipool.common.Delay.delay;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.pi4j.context.Context;
import com.pi4j.io.i2c.I2C;
import com.pi4j.io.i2c.I2CConfig;

import es.fdvcode.pipool.srv.sonda.atlasi2c.DeviceAtlasI2C;
import es.fdvcode.pipool.srv.sonda.atlasi2c.SondaAtlasErrorException;
import es.fdvcode.pipool.srv.sonda.atlasi2c.SondaAtlasStillProcessingException;
import jakarta.annotation.PreDestroy;

/**
 * 
 * @author cfarrema
 *
 */
public class DeviceAtlasI2CImpl implements DeviceAtlasI2C {

	private final Logger log = LoggerFactory.getLogger(DeviceAtlasI2C.class);
	
	private final Context pi4jContext;
	private final int address;
	private final String name;
	private I2C device;
	
	private static final byte RESPONSE_CODE_NO_DATA_SEND 		= (byte) 0xFF;
	private static final byte RESPONSE_CODE_STILL_PROCESSING 	= (byte) 0xFE;
	private static final byte RESPONSE_CODE_ERROR 				= (byte) 0x02;
	private static final byte RESPONSE_CODE_OK 					= (byte) 0x01;
	private static final byte END_OF_MESSAGE 					= (byte) 0x00;
	
	private static final int NUM_BYTES_DEF 		= 31;

	private static final int MAX_INTENTS_READ = 3;
	private static final int DELAY_MILIS_LONG = 1000;
	private static final int DELAY_MILIS_SHORT = 500;
	
	/**
	 * 
	 * @param bus
	 * @param address
	 * @throws IOException
	 * @throws UnsupportedBusNumberException 
	 */
	public DeviceAtlasI2CImpl(Context pi4jContext, int address, String name) {
		this.pi4jContext = pi4jContext;
		this.address = address;
		this.name = name;
		this.initBus();       
	}
	
	@Override
	public void initBus() {
		I2CConfig i2cConfig = I2C.newConfigBuilder(this.pi4jContext)
                .id("my.i2c.device." + address)
                .bus(1)
                .device(this.address)
                .build();
        this.device = this.pi4jContext.create(i2cConfig);
        log.info("Dispositivo I2C {} inicializado en address: {}", name, address);
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
		return this.device != null;
	}
	

	/**
	 * 
	 * @throws IOException
	 */
	@Override
	public void closeBus() {
		this.device.close();
	}

	/**
	 * 
	 * @return
	 * @throws IOException
	 */
	@Override
	public String readDevice() throws SondaAtlasStillProcessingException, SondaAtlasErrorException {
		return this.readDevice(NUM_BYTES_DEF);
	}
	
	/**
	 * 
	 * @param numBytes
	 * @return
	 * @throws IOException
	 * @throws SondaAtlasNoDataSendException 
	 * @throws SondaAtlasStillProcessingException 
	 * @throws SondaAtlasErrorException 
	 * @throws SondaAtlasErrorException 
	 */
	@Override
	public String readDevice(int numBytes) throws SondaAtlasStillProcessingException, SondaAtlasErrorException {
		byte[] buffer = new byte[numBytes];
		int r = device.read(buffer,0,numBytes);
		
		log.debug("DEBUG readDevice - {} = device.read({},0,{});", r, buffer, numBytes);
		
		//Tracta ResponseCode
		byte responseCode = buffer[0];
		if(responseCode == RESPONSE_CODE_NO_DATA_SEND) {
			return "";
		}
		else if (responseCode == RESPONSE_CODE_STILL_PROCESSING) {
			throw new SondaAtlasStillProcessingException();
		}
		else if (responseCode == RESPONSE_CODE_OK) {
			//Obte el resultat;
			StringBuilder sb = new StringBuilder();
			int i = 1; //comencem a partir del segon byte 
			while(i < r) {
				byte b = buffer[i];
				if(b==END_OF_MESSAGE) {
					i = r;	//sortim
				}
				else {
					sb.append(new String(new byte[]{b}));
				}
				i++;
			}
			
			return sb.toString();
		}
		else {
			throw new SondaAtlasErrorException(responseCode, responseCode==RESPONSE_CODE_ERROR);
		}
	}

	/**
	 * 
	 * @param command
	 * @throws IOException
	 */
	@Override
	public void writeDevice(String command) {
		byte[] bytes = command.getBytes();
		byte[] bytessend = new byte[bytes.length+1];
		System.arraycopy(bytes, 0, bytessend, 0, bytes.length);
		bytessend[bytes.length] = END_OF_MESSAGE;
		
		device.write(bytessend);
	}
	
	/**
	 * 
	 * @param command
	 * @return
	 * @throws IOException
	 * @throws SondaAtlasErrorException 
	 */
	@Override
	public String queryDevice(String command) throws SondaAtlasErrorException {
		this.writeDevice(command);
		
		if(command.toUpperCase().startsWith("R") || command.toUpperCase().startsWith("CAL")) {
			return this.delayedReadDevice(DELAY_MILIS_LONG, MAX_INTENTS_READ);
		}
		else if (command.toUpperCase().startsWith("SLEEP")) {
			return "SLEEP MODE ON";
		}
		else {
			return this.delayedReadDevice(DELAY_MILIS_SHORT, MAX_INTENTS_READ);
		}
	}
	
	private String delayedReadDevice(int milis, int numIntents) throws SondaAtlasErrorException {
		if(numIntents > 0) {
			delay(milis);
			
			try {
				return this.readDevice();
			} 
			catch (SondaAtlasStillProcessingException e) {
				log.info("Lectura NOK Sonda. Encara esta processant. Els milis {} de delay no han estat suficients. Tornem a intentar (numIntents={}).", milis, numIntents);
				return delayedReadDevice(DELAY_MILIS_SHORT, numIntents-1);
			}
		}
		else {
			throw new SondaAtlasErrorException((byte)0x03, false);
		}
	}
	
    @PreDestroy
    public void shutdown() {
        if (this.device != null) {
            this.device.close();
            log.info("Recurso del dispositivo I2C {} liberado.", name);
        }
    }
}
