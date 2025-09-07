package es.fdvcode.pipool.srv.rele;

import static es.fdvcode.pipool.common.Nvl.nvl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;

import org.springframework.stereotype.Component;

import es.fdvcode.pipool.model.rele.FranjaHoraria;
import es.fdvcode.pipool.model.rele.PersistibleRele;
import es.fdvcode.pipool.model.rele.Rele;
import es.fdvcode.pipool.model.rele.StateRele;
import es.fdvcode.pipool.srv.persist.Persister;

@Component
public class RelesPersister extends Persister<PersistibleRele, Rele> {

	private static final String FILENAME_PERSIST = "reles";
	private static final String PATH_PERSIST = "reles/";
	
	@Override
	public void doPersistencia(Collection<Rele> listReles) {
		List<PersistibleRele> list = new ArrayList<>();
		for(Rele rele : listReles) {
			List<PersistibleRele> listPers = convert(rele.extractHistoricPendent());
			list.addAll(listPers);
		}
		
		//graba a disc
		doPersistencia(list);
		
		log.info("Persistencia de RELES OK.");
	}

	
	@Override
	protected String getFilenamePrefix() {
		return FILENAME_PERSIST;
	}
	
	@Override
	protected String getEspecificPath() {
		return PATH_PERSIST;
	}
	
	
	@Override
	protected Comparator<PersistibleRele> getComparatorPersistible() {
		return (p1,p2)-> {
			return p1.getTimestamp().compareTo(p2.getTimestamp());
		};
	}

	@Override
	public List<PersistibleRele> loadHistory(String id, int numDies) {
		List<PersistibleRele> list = this.loadFromDisc(numDies);
		
		List<PersistibleRele> result = new ArrayList<>();
		for(PersistibleRele p : list) {
			if(id.equals(p.getIdRele())) {
				result.add(p);
			}
		}
		
		return result;
	}
	
	public List<PersistibleRele> convert(List<StateRele> listStates) {
		List<PersistibleRele> list = new ArrayList<>();
		for(StateRele state : listStates) {
			PersistibleRele item = convert(state);
			list.add(item);
		}
		
		return list;
	}

	
	public PersistibleRele convert(StateRele state) {
		if(state!=null) {
			return new PersistibleRele(state.getRele().getId(), 
					state.getTimestamp(),
					state.isOn(), 
					state.isGpioPinHigh(),
					state.getMode(),
					state.getCausa(),
					state.getDescripcio(),
					nvl(state.getActivacioProgramada(), ""),
					nvl(state.getActivacioTemporal(), ""),
					nvl(state.getActivacioRule(), ""),
					state.isActivacioReleMaster(),
					state.isDesactivacioReleMaster(),
					nvl(state.getRele().getConsumHora(), 0.0),
					nvl(state.getConsumRele(), 0.0)); 
		}else {
			return null;
		}
	}

	public List<StateRele> convert(Rele rele, List<PersistibleRele> list) {
		List<StateRele> res = new ArrayList<>();
		
		if(list!=null) {
			for(PersistibleRele pers : list) {
				StateRele state = convert(pers, rele);
				if(state!=null) {
					res.add(state);
				}
			}
		}
		
		return res;
	}
	
	public StateRele convert(PersistibleRele pers, Rele rele) {
		if(pers==null) return null;
		StateRele state = new StateRele(
				rele, 
				pers.isOn(), 
				pers.getMode(), 
				pers.getCausa(),
				pers.getDescripcio(), 
				pers.getConsumRele(), 
				pers.getTimestamp());
		
		if(pers.getActivacioTemporal()!=null) {
			//TODO: falta saber si cal activar reles slaves!!
			FranjaHoraria fr  = this.parseFranja(pers.getActivacioTemporal(), false);
			state.setActivacioTemporal(fr);
		}
		
		return state;
	}
	
	private FranjaHoraria parseFranja(String txtFranja, boolean releSlave) {
		try {
			//txtFranja = 14:27:23-->60:0
			
			String[] parts = txtFranja.split("-->");
			String[] partsHora = parts[0].split(":");
			String[] partsDuracio = parts[1].split(":");
			
			int hora = Integer.parseInt(partsHora[0]);
			int minut = Integer.parseInt(partsHora[1]);
			int segon = Integer.parseInt(partsHora[2]);
			int duracio = Integer.parseInt(partsDuracio[0]) * 60 + Integer.parseInt(partsDuracio[1]);
			FranjaHoraria fr = new FranjaHoraria(hora, minut, segon, duracio, releSlave);
			return fr;
		}
		catch(Exception ex) {
			return null;
		}
	}

	
	@Override
	protected String getCapsalera() {
		
//		StringBuilder sb = new StringBuilder();
//		sb.append(idRele).append(";");
//		sb.append(dateFormat.format(timestamp)).append(";");
//		sb.append(isOn).append(";");
//		sb.append(gpioPinHigh).append(";");
//		sb.append(mode).append(";");
//		sb.append(desactivacioReleMaster).append(";");
//		sb.append(activacioTemporal).append(";");
//		sb.append(activacioProgramada).append(";");
//		sb.append(descripcio).append(";");
//		sb.append(activacioReleMaster).append(";");
//		sb.append(activacioRule).append(";");	
		
		return "IDRELE;TIMESTAMP;IS_ON;GPIOPINHIGH;MODE;CAUSA;DESACTRELEMASTER;ACITVTEMPORAL;ACTIVPROGRAMADA;DESCRIPCIO;ACTRELEMASTER;ACTIVRULE;CONSUMHORA;CONSUMRELE";
	}


	@Override
	public PersistibleRele newInstance(String linea) {
		return new PersistibleRele(linea);
	}
}
