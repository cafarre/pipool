package es.fdvcode.pipool.srv.sonda.atlasi2c.impl;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.pi4j.context.Context;

import es.fdvcode.pipool.model.sonda.Sonda;
import es.fdvcode.pipool.srv.sonda.atlasi2c.DeviceAtlasI2C;
import es.fdvcode.pipool.srv.sonda.atlasi2c.SondaAtlasErrorException;

/**
 * 
 * Implementació de metodes Generics de Sondes Atlas 
 * 
 * Metodes soportats (Comuns):
 * 		L		enable/disable LED
 * 		R		returns a single reading
 * 		i		device information
 * 		Sleep	enter sleep mode/low power
 * 		Status	retrieve status information
 * 
 * @author cfarrema
 *
 */
public abstract class SondaAtlas {

	protected final Logger log = LoggerFactory.getLogger(this.getClass());
	protected final Context pi4jContext;
	protected final DeviceAtlasI2C device;
	private final int address;
	private final String name;
	private final boolean modeTest;

	public SondaAtlas(int address, String name, boolean modeTest, Context pi4jContext) {
		this.address=address;
		this.name = name;
		this.pi4jContext = pi4jContext;
		this.modeTest = modeTest;
		if(modeTest) {
			this.device = new DeviceTestI2CImpl(address);
		}
		else {
			this.device = new DeviceAtlasI2CImpl(pi4jContext, address, name);
		}
	}

	public boolean isModeTest() {
		return this.modeTest;
	}

	public boolean isBusInitiated() {
		return device!=null && device.isBusInitated();
	}

	
	public int getAddress() {
		return address;
	}
	
	public String getName() {
		return this.name;
	}

	/**
	 * 
	 * @param sonda
	 * @return
	 * @throws IOException
	 * @throws UnsupportedBusNumberException 
	 * @throws SondaAtlasErrorException 
	 */
	public String cmdTakeReading(Sonda sonda, boolean saveHistoric) throws SondaAtlasErrorException {
	
		String strValor = execCmd(sonda, "R");
		if (sonda.isReadingValid(strValor)) {
			sonda.setLecturaSonda(strValor, saveHistoric);
		} else {
			log.warn("cmdTakeReading: Lectura descartada per a sonda {}: valor invàlid ({})", sonda.getId(), strValor);
		}
		
		return strValor;
	}

	/**
	 * 
	 * @param sonda
	 * @throws IOException
	 * @throws UnsupportedBusNumberException 
	 * @throws SondaAtlasErrorException 
	 */
	public void cmdLedOn(Sonda sonda) throws SondaAtlasErrorException {
		this.execCmd(sonda, "L,1");
	}

	/**
	 * 
	 * @param sonda
	 * @throws IOException
	 * @throws UnsupportedBusNumberException 
	 * @throws SondaAtlasErrorException 
	 */
	public void cmdLedOff(Sonda sonda) throws SondaAtlasErrorException {
		this.execCmd(sonda, "L,0");
	}

	/**
	 * 
	 * @param sonda
	 * @return
	 * @throws IOException
	 * @throws UnsupportedBusNumberException 
	 * @throws SondaAtlasErrorException 
	 */
	public boolean cmdLedState(Sonda sonda) throws SondaAtlasErrorException {
		String response = this.execCmd(sonda, "L,?");
		String[] parts = response.split(",");
		
		boolean result = "1".equals(parts[1]);
		
		return result;
	}

	/**
	 * 
	 * @param sonda
	 * @throws IOException
	 * @throws UnsupportedBusNumberException 
	 * @throws SondaAtlasErrorException 
	 */
	public void cmdCalibrationClear(Sonda sonda) throws SondaAtlasErrorException {
		this.execCmd(sonda, "Cal,clear");
	}

	/**
	 * 
	 * @param sonda
	 * @return
	 * @throws IOException
	 * @throws UnsupportedBusNumberException 
	 * @throws SondaAtlasErrorException 
	 */
	public int cmdCalibrationGetState(Sonda sonda) throws SondaAtlasErrorException {
		String response = this.execCmd(sonda, "Cal,?");
		
		String[] parts = response.split(",");
		
		return Integer.valueOf(parts[1]); 
	}
	
	/**
	 * 
	 * @param sonda
	 * @return
	 * @throws IOException
	 * @throws UnsupportedBusNumberException 
	 * @throws SondaAtlasErrorException 
	 */
	public DeviceInfoResponse cmdGetDeviceInfo(Sonda sonda) throws SondaAtlasErrorException {
		String response = this.execCmd(sonda, "i");
		
		if(response.contains(",")) {
			String[] parts = response.split(",");
			return new DeviceInfoResponse(parts[1], parts[2]);
		}
		else {
			return null;
		}
	}	

	/**
	 * 
	 * @param sonda
	 * @return
	 * @throws IOException
	 * @throws UnsupportedBusNumberException 
	 * @throws SondaAtlasErrorException 
	 */
	public DeviceStatusResponse cmdGetDeviceStatus(Sonda sonda) throws SondaAtlasErrorException {
		String response = this.execCmd(sonda, "Status");
		
		String[] parts = response.split(",");
		
		return new DeviceStatusResponse(RestartReasons.getRestartReasons(parts[1]), parts[2]);
	}	
	
	/**
	 * 
	 * @param sonda
	 * @throws IOException
	 * @throws UnsupportedBusNumberException 
	 * @throws SondaAtlasErrorException 
	 */
	public void cmdSleep(Sonda sonda) throws SondaAtlasErrorException {
		this.execCmd(sonda, "Sleep");
	}
	
	/**
	 * 
	 * @param sonda
	 * @param command
	 * @return
	 * @throws IOException
	 * @throws UnsupportedBusNumberException 
	 * @throws SondaAtlasErrorException 
	 */
	public String execCmd(Sonda sonda, String command) throws SondaAtlasErrorException {
		
		log.debug("Exec Command [{}] en Sonda [{}].", command, sonda.getId());
		if(!device.isBusInitated()) {
			device.initBus();
		}
		
		String valor = device.queryDevice(command);
		
		log.info("Resultat Exec Command [{}] en Sonda [{}]: {}", command, sonda.getId(), valor.trim());
		
		return valor;
	}

	
	public static class DeviceInfoResponse {
		private String device;
		private String firmware;
		
		public DeviceInfoResponse(String device, String firmware) {
			this.device=device;
			this.firmware=firmware;
		}

		public String getDevice() {
			return device;
		}

		public String getFirmware() {
			return firmware;
		}
	}
	
	public static class DeviceStatusResponse {
		
		private RestartReasons restartReason;
		private String firmware;
		
		public DeviceStatusResponse(RestartReasons restartReason, String firmware) {
			this.restartReason=restartReason;
			this.firmware=firmware;
		}

		public RestartReasons getRestartReason() {
			return restartReason;
		}

		public String getFirmware() {
			return firmware;
		}
	}
	
	public static enum RestartReasons {
	    POWERED_OFF("P", "Powered Off"),
	    SOFTWARE_RESET("S", "Software Reset"),
	    BROWN_OUT("B", "Brown Out"),
	    WATCHDOG("W", "Watchdog"),
	    UNKNOWN("U", "Unknown");
	    
	 
	    private String code;
	    private String label;
	 
	    /**
	     * A mapping between the integer code and its corresponding Status to facilitate lookup by code.
	     */
	    private static Map<String, RestartReasons> map;
	 
	    private RestartReasons(String code, String label) {
	        this.code = code;
	        this.label = label;
	    }
	 
	    public static RestartReasons getRestartReasons(String code) {
	        if (map == null) {
	            initMapping();
	        }
	        return map.get(code);
	    }
	 
	    private static void initMapping() {
	        map = new HashMap<>();
	        for (RestartReasons s : values()) {
	            map.put(s.code, s);
	        }
	    }
	 
	    public String getCode() {
	        return code;
	    }
	 
	    public String getLabel() {
	        return label;
	    }
	 
	    @Override
	    public String toString() {
	        final StringBuilder sb = new StringBuilder();
	        sb.append("{code=").append(code).append(", label='").append(label).append("'}");
	        return sb.toString();
	    }
	}
	
}