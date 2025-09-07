package es.fdvcode.pipool.srv.sonda.atlasi2c.impl;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import com.pi4j.context.Context;

import es.fdvcode.pipool.model.sonda.Sonda;
import es.fdvcode.pipool.srv.sonda.atlasi2c.SondaAtlasErrorException;


/**
 * Implementació de metodes de Sonda Atlas RTD (Temperatura)
 * 
 * Metodes soportats:
 * 		L		enable/disable LED
 * 		R		returns a single reading
 * 		i		device information
 * 		Sleep	enter sleep mode/low power
 * 		Status	retrieve status information
 * 		Cal		performs calibration
 * 
 * Metodes soportats (Propis):
 * 		Cal		performs calibration
 * 		S		temperature scale (°C, °K, °F)
 *		D		enable/disable data logger
 *
 *
 * 
 * @author cfarrema
 *
 */
public class SondaAtlasRTD extends SondaAtlas {
	
	public SondaAtlasRTD(boolean modeTest, Context pi4jContext) {
		super(102, "RTD", modeTest, pi4jContext);
	}
	
	/**
	 * 
	 * @param sonda
	 * @throws IOException
	 * @throws UnsupportedBusNumberException 
	 * @throws SondaAtlasErrorException 
	 */
	public void cmdCalibrationSet(Sonda sonda, String grausC) throws SondaAtlasErrorException {
		this.execCmd(sonda, "Cal," + grausC);
	}
	
	/**
	 * 
	 * @param sonda
	 * @throws IOException
	 * @throws UnsupportedBusNumberException 
	 * @throws SondaAtlasErrorException 
	 */
	public void cmdScaleCelsius(Sonda sonda) throws SondaAtlasErrorException {
		this.execCmd(sonda, "S,c");
	}

	/**
	 * 
	 * @param sonda
	 * @throws IOException
	 * @throws UnsupportedBusNumberException 
	 * @throws SondaAtlasErrorException 
	 */
	public void cmdScaleKelvin(Sonda sonda) throws SondaAtlasErrorException {
		this.execCmd(sonda, "S,k");
	}

	/**
	 * 
	 * @param sonda
	 * @throws IOException
	 * @throws UnsupportedBusNumberException 
	 * @throws SondaAtlasErrorException 
	 */
	public void cmdScaleFahrenheit(Sonda sonda) throws SondaAtlasErrorException {
		this.execCmd(sonda, "S,f");
	}

	
	/**
	 * 
	 * @param sonda
	 * @return
	 * @throws IOException
	 * @throws UnsupportedBusNumberException 
	 * @throws SondaAtlasErrorException 
	 */
	public ScaleTemp cmdScaleGet(Sonda sonda) throws SondaAtlasErrorException {
		String response = this.execCmd(sonda, "S,?");
		String[] parts = response.split(",");
		
		return ScaleTemp.getScaleTemp(parts[1]);
	}

	/**
	 * 
	 * @param sonda
	 * @throws IOException
	 * @throws UnsupportedBusNumberException 
	 * @throws SondaAtlasErrorException 
	 */
	public void cmdDataLoggerOff(Sonda sonda) throws SondaAtlasErrorException {
		this.execCmd(sonda, "D,0");
	}

	
	/**
	 * 
	 * @param sonda
	 * @return
	 * @throws IOException
	 * @throws UnsupportedBusNumberException 
	 * @throws SondaAtlasErrorException 
	 */
	public String cmdDataLoggerGet(Sonda sonda) throws SondaAtlasErrorException {
		String response = this.execCmd(sonda, "D,?");
		String[] parts = response.split(",");
		
		return parts[1];
	}
	
	public enum ScaleTemp {
	    CELSIUS("c", "Celsius"),
	    KELVIN("k", "Kelvin"),
	    FAHRENHEIT("f", "Fahrenheit");
	    
	 
	    private String code;
	    private String label;
	 
	    /**
	     * A mapping between the integer code and its corresponding Status to facilitate lookup by code.
	     */
	    private static Map<String, ScaleTemp> map;
	 
	    private ScaleTemp(String code, String label) {
	        this.code = code;
	        this.label = label;
	    }
	 
	    public static ScaleTemp getScaleTemp(String code) {
	        if (map == null) {
	            initMapping();
	        }
	        return map.get(code);
	    }
	 
	    private static void initMapping() {
	        map = new HashMap<>();
	        for (ScaleTemp s : values()) {
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
	        sb.append("ScaleTemp");
	        sb.append("{code=").append(code);
	        sb.append(", label='").append(label).append('\'');
	        sb.append('}');
	        return sb.toString();
	    }
	}

}

