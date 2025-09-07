package es.fdvcode.pipool.srv.sonda;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;

import org.springframework.stereotype.Component;

import es.fdvcode.pipool.model.sonda.PersistibleSonda;
import es.fdvcode.pipool.model.sonda.Sonda;
import es.fdvcode.pipool.model.sonda.StateSonda;
import es.fdvcode.pipool.srv.persist.Persister;

@Component
public class SondesPersister extends Persister<PersistibleSonda, Sonda> {

	private static final String FILENAME_PERSIST = "sondes";
	private static final String PATH_PERSIST = "sondes/";
	
	@Override
	public void doPersistencia(Collection<Sonda> listSondes) {
		List<PersistibleSonda> list = new ArrayList<>();
		for(Sonda sonda : listSondes) {
			List<PersistibleSonda> listSonda = convert(sonda.extractHistoric());
			list.addAll(listSonda);
		}
		
		//graba a disc
		doPersistencia(list);
		
		log.info("Persistencia de SONDES OK.");
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
	protected Comparator<PersistibleSonda> getComparatorPersistible() {
		return (p1,p2)-> {
			return p1.getTimestamp().compareTo(p2.getTimestamp());
		};
	}
	
	@Override
	public List<PersistibleSonda> loadHistory(String id, int numDies) {
		List<PersistibleSonda> list = this.loadFromDisc(numDies);
		
		List<PersistibleSonda> result = new ArrayList<>();
		for(PersistibleSonda p : list) {
			if(id.equals(p.getIdSonda())) {
				result.add(p);
			}
		}
		
		return result;
	}
	
	public List<PersistibleSonda> convert(List<StateSonda> listState) {
		List<PersistibleSonda> list = new ArrayList<>();
		for(StateSonda state : listState) {
			PersistibleSonda item = convert(state);
			list.add(item);
		}
		
		return list;
	}
	
	public List<StateSonda> convert(Sonda sonda, List<PersistibleSonda> listPersist) {
		List<StateSonda> list = new ArrayList<>();
		for(PersistibleSonda persist : listPersist) {
			StateSonda item = convert(sonda, persist);
			list.add(item);
		}
		
		return list;
	}
	
	private PersistibleSonda convert(StateSonda state) {
		if(state!=null) {
			return new PersistibleSonda(state.getSonda().getId(), state.getValor(), state.getTimestamp()); 
		}else {
			return null;
		}
	}

	private StateSonda convert(Sonda sonda, PersistibleSonda persist) {
		if(persist!=null) {
			return new StateSonda(sonda, persist.getValor(), persist.getTimestamp()); 
		}else {
			return null;
		}
	}

	@Override
	protected String getCapsalera() {
		return "SONDA;VALOR;TIMESTAMP";
	}


	@Override
	public PersistibleSonda newInstance(String linea) {
		return new PersistibleSonda(linea);
	}
}
