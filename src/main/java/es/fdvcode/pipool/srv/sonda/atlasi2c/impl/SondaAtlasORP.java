package es.fdvcode.pipool.srv.sonda.atlasi2c.impl;

import java.io.IOException;

import com.pi4j.context.Context;

import es.fdvcode.pipool.model.sonda.Sonda;
import es.fdvcode.pipool.srv.sonda.atlasi2c.SondaAtlasErrorException;


/**
 * Implementació de metodes de Sonda Atlas ORP
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
 * 
 * 
 * 
 * @author cfarrema
 *
 */
public class SondaAtlasORP extends SondaAtlas {
	
	public SondaAtlasORP(boolean modeTest, Context pi4jContext) {
		super(98, "ORP", modeTest, pi4jContext);
	}
	
	/**
	 * 
	 * @param sonda
	 * @throws IOException
	 * @throws UnsupportedBusNumberException 
	 * @throws SondaAtlasErrorException 
	 */
	public void cmdCalibrationSet(Sonda sonda, String mV) throws SondaAtlasErrorException {
		this.execCmd(sonda, "Cal," + mV);
	}	
}
