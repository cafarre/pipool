package es.fdvcode.pipool.srv.sonda.atlasi2c.impl;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.pi4j.context.Context;

import lombok.RequiredArgsConstructor;

/**
 * 
 * @author cfarrema
 *
 */
@Component
@RequiredArgsConstructor
public class FactorySondaAtlas {
	private final Context pi4jContext;
	
	@Value("${pipool.sondes.atlas.modeTest}")
	private boolean modeTest;
	
	/**
	 * 
	 * @param address
	 * @return
	 * @throws IOException
	 * @throws UnsupportedBusNumberException
	 */
	public SondaAtlas newInstance(int address)  {
		if(address == 98) {
			return new SondaAtlasORP(modeTest, pi4jContext);
		}
		else if(address == 99) {
			return new SondaAtlasPH(modeTest, pi4jContext);
		}
		else if(address == 102) {
			return new SondaAtlasRTD(modeTest, pi4jContext);
		}
		else {
			return null;
		}
	}
}
