package es.fdvcode.pipool.srv.sonda.atlasi2c.impl;

import java.io.IOException;

import com.pi4j.context.Context;

import es.fdvcode.pipool.model.sonda.Sonda;
import es.fdvcode.pipool.srv.sonda.atlasi2c.SondaAtlasErrorException;

/**
 * Implementació de metodes de Sonda Atlas PH
 * 
 * Metodes soportats (Comuns):
 * 		L		enable/disable LED
 * 		R		returns a single reading
 * 		i		device information
 * 		Sleep	enter sleep mode/low power
 * 		Status	retrieve status information
 * 		Cal		performs calibration
 * 
 * Metodes soportats (Propis):
 * 		Slope	returns the slope of the pH probe
 * 		T		temperature compensation
 * 		Cal		performs calibration
 * 
 * 
 * 
 * @author cfarrema
 *
 */
public class SondaAtlasPH extends SondaAtlas {
	
	public SondaAtlasPH(boolean modeTest, Context pi4jContext) {
		super(99, "PH", modeTest, pi4jContext);
	}
	
	/**
	 * 
	 * @param sonda
	 * @throws IOException
	 * @throws UnsupportedBusNumberException 
	 * @throws SondaAtlasErrorException 
	 */
	public void cmdCalibrationMid(Sonda sonda, String ph) throws SondaAtlasErrorException {
		this.execCmd(sonda, "Cal,mid," + ph);
	}

	/**
	 * 
	 * @param sonda
	 * @throws IOException
	 * @throws UnsupportedBusNumberException 
	 * @throws SondaAtlasErrorException 
	 */
	public void cmdCalibrationLow(Sonda sonda, String ph) throws SondaAtlasErrorException {
		this.execCmd(sonda, "Cal,low," + ph);
	}

	/**
	 * 
	 * @param sonda
	 * @throws IOException
	 * @throws UnsupportedBusNumberException 
	 * @throws SondaAtlasErrorException 
	 */
	public void cmdCalibrationHigh(Sonda sonda, String ph) throws SondaAtlasErrorException {
		this.execCmd(sonda, "Cal,high," + ph);
	}
	
	/**
	 * 
	 * @param sonda
	 * @return
	 * @throws IOException
	 * @throws UnsupportedBusNumberException 
	 * @throws SondaAtlasErrorException 
	 */
	public SlopeResponse cmdGetSlope(Sonda sonda) throws SondaAtlasErrorException {
		String response = this.execCmd(sonda, "Slope,?");
		
		String[] parts = response.split(",");
		
		return new SlopeResponse(parts[1], parts[2]);
	}

	/**
	 * 
	 * @param sonda
	 * @throws IOException
	 * @throws UnsupportedBusNumberException 
	 * @throws SondaAtlasErrorException 
	 */
	public void cmdSetTempCompensation(Sonda sonda, String temp) throws SondaAtlasErrorException {
		this.execCmd(sonda, "T," + temp);
	}
	
	/**
	 * 
	 * @param sonda
	 * @return
	 * @throws IOException
	 * @throws UnsupportedBusNumberException 
	 * @throws SondaAtlasErrorException 
	 */
	public String cmdGetTempCompensation(Sonda sonda) throws SondaAtlasErrorException {
		String response = this.execCmd(sonda, "T,?");
		
		String[] parts = response.split(",");
		
		return parts[1];
	}
	
	public class SlopeResponse {
		private String acid;
		private String base;
		
		public SlopeResponse(String acid, String base) {
			this.acid=acid;
			this.base=base;
		}

		public String getAcid() {
			return acid;
		}

		public String getBase() {
			return base;
		}
	}
}
