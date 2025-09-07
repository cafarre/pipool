package es.fdvcode.pipool.model.rele;

import static es.fdvcode.pipool.common.Nvl.nvl;

import java.text.ParseException;
import java.util.Date;

import es.fdvcode.pipool.model.rele.StateRele.CausaState;
import es.fdvcode.pipool.model.rele.StateRele.ModeRele;
import es.fdvcode.pipool.srv.persist.Persistible;

/**
 * 
 * @author cfarrema
 *
 */
public class PersistibleRele implements Persistible {

	private String idRele;
	private Date timestamp;
	private boolean isOn=false;
	private boolean gpioPinHigh=false;
	private ModeRele mode=ModeRele.AUTO;
	private CausaState causa=CausaState.LOAD_HISTORY;
	private String descripcio;
	
	private String activacioTemporal;
	private String activacioProgramada;	
	private String activacioRule;
	
	private boolean activacioReleMaster=false;
	private boolean desactivacioReleMaster=false;

	private double consumHora;
	private double consumRele;
	
	
	public PersistibleRele(String lineaFile) {
		this.unmarshall(lineaFile);
	}
	
	public PersistibleRele(
			String idRele, 
			Date timestamp, 
			boolean isOn, 
			boolean gpioPinHigh, 
			ModeRele mode,
			CausaState causa, 
			String descripcio,
			String activacioProgramada,
			String activacioTemporal, 
			String activacioRule,
			boolean activacioReleMaster,
			boolean desactivacioReleMaster,
			double consumHora,
			double consumRele) {
		
		super();
		this.idRele = idRele;
		this.timestamp = timestamp;
		this.isOn = isOn;
		this.gpioPinHigh = gpioPinHigh;
		this.mode = mode;
		this.causa = causa;
		this.descripcio = descripcio;
		this.desactivacioReleMaster = desactivacioReleMaster;
		this.activacioTemporal = activacioTemporal;
		this.activacioProgramada = activacioProgramada;
		this.activacioReleMaster = activacioReleMaster;
		this.activacioRule = activacioRule;
		this.consumHora = consumHora;
		this.consumRele = consumRele;
	}

	public String getIdRele() {
		return idRele;
	}

	public boolean isOn() {
		return isOn;
	}

	public boolean isGpioPinHigh() {
		return gpioPinHigh;
	}

	public ModeRele getMode() {
		return mode;
	}

	public CausaState getCausa() {
		return causa;
	}
	
	public String getDescripcio() {
		return descripcio;
	}

	public boolean isDesactivacioReleMaster() {
		return desactivacioReleMaster;
	}

	public String getActivacioTemporal() {
		return activacioTemporal;
	}

	public String getActivacioProgramada() {
		return activacioProgramada;
	}

	public boolean isActivacioReleMaster() {
		return activacioReleMaster;
	}

	public String getActivacioRule() {
		return activacioRule;
	}
	
	public double getConsumHora() {
		return consumHora;
	}

	public double getConsumRele() {
		return consumRele;
	}
	
	@Override
	public Date getTimestamp() {
		return timestamp;
	}
	
	@Override
	public String marshall() {
		StringBuilder sb = new StringBuilder();
		sb.append(idRele).append(";");
		sb.append(dateFormat.format(timestamp)).append(";");
		sb.append(isOn).append(";");
		sb.append(gpioPinHigh).append(";");
		sb.append(mode).append(";");
		sb.append(causa).append(";");
		sb.append(desactivacioReleMaster).append(";");
		sb.append(activacioTemporal).append(";");
		sb.append(activacioProgramada).append(";");
		sb.append(descripcio).append(";");
		sb.append(activacioReleMaster).append(";");
		sb.append(activacioRule).append(";");
		sb.append(getDoubleRounded(consumHora)).append(";");
		sb.append(getDoubleRounded(consumRele)).append(";");
		
		return sb.toString();
	}

	@Override
	public void unmarshall(String lineaFile) {
		String[] parts = lineaFile.split(";");
		int i=0;
		this.idRele=getValue(parts, i++);
		try {
			this.timestamp=dateFormat.parse(getValue(parts, i++));
		} catch (ParseException e) {}
		this.isOn=Boolean.parseBoolean(getValue(parts, i++));
		this.gpioPinHigh=Boolean.parseBoolean(getValue(parts, i++));
		this.mode=ModeRele.valueOf(getValue(parts, i++));
		if(parts.length==14) {
			this.causa=CausaState.valueOf(getValue(parts, i++));	
		}
		this.desactivacioReleMaster=Boolean.parseBoolean(getValue(parts, i++));
		this.activacioTemporal=getValue(parts, i++);
		this.activacioProgramada=getValue(parts, i++);
		this.descripcio=getValue(parts, i++);
		this.activacioReleMaster=Boolean.parseBoolean(getValue(parts, i++));
		this.activacioRule=getValue(parts, i++);
		this.consumHora=Double.parseDouble(nvl(getValue(parts, i++),0.0));
		this.consumRele=Double.parseDouble(nvl(getValue(parts, i++),0.0));
	}
	
	@Override
	public String toString() {
		return this.idRele + ": " + this.timestamp + " - " + this.causa;
	}
}