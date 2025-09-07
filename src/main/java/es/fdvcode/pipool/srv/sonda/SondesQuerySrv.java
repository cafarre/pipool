package es.fdvcode.pipool.srv.sonda;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import es.fdvcode.pipool.model.rele.PersistibleRele;
import es.fdvcode.pipool.model.rele.Rele;
import es.fdvcode.pipool.model.sonda.PersistibleSonda;
import es.fdvcode.pipool.model.sonda.PersistibleSondaBomba;
import es.fdvcode.pipool.model.sonda.Sonda;
import es.fdvcode.pipool.model.sonda.Sonda.TipusSonda;
import es.fdvcode.pipool.model.sonda.StateSonda;
import es.fdvcode.pipool.srv.ItemNotFoundException;
import es.fdvcode.pipool.srv.rele.RelesQuerySrv;
import es.fdvcode.pipool.srv.sonda.SondaBombaMatcher.IntervalReleOn;
import es.fdvcode.pipool.srv.sonda.atlasi2c.impl.FactorySondaAtlas;
import es.fdvcode.pipool.srv.sonda.atlasi2c.impl.SondaAtlas;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;

/**
 * 
 * @author cfarrema
 *
 */
@Component
@RequiredArgsConstructor
public class SondesQuerySrv {

	private final Logger log= LoggerFactory.getLogger(this.getClass());

	final FactorySondaAtlas factory;
	final Environment env;

	private final SondesLoader sondesLoader;
	private final SondesPersister sondesPersister;
	private final SondaBombaMatcher matcher;
	private final RelesQuerySrv relesQuery;
	
	@Value("${pipool.reles.idReleBomba}")
	private String idReleBomba;

	@Value("${pipool.reles.numDiesHistoria}")
	private int numDiesHistoriaReles;
	
	private Map<Integer, SondaAtlas> mapSondesAtlas = new HashMap<>();
	private Map<Integer, Sonda> mapSondes = new HashMap<>();

	/**
	 * constructor spring
	 */
	@PostConstruct
	public void initSondesAtlas() {
		for(Sonda sonda : this.getMapSondes().values()) {
			if(TipusSonda.Atlas.equals(sonda.getTipusSonda())) {
				SondaAtlas sondaAtlas = mapSondesAtlas.get(sonda.getAddress());
				if(sondaAtlas==null) {
					sondaAtlas = factory.newInstance(sonda.getAddress());
					if(sondaAtlas.isBusInitiated()){
						mapSondesAtlas.put(sonda.getAddress(), sondaAtlas);
						mapSondes.put(sonda.getAddress(), sonda);
						
						log.info("Sonda Atlas {} iniciada OK.", sonda.getId());
					}
				}
			}
			else {
				mapSondes.put(sonda.getAddress(), sonda);
			}
		}
	}

	/**
	 * 
	 * @return
	 */
	public Map<String, Sonda> getMapSondes() {
		return sondesLoader.getSondes();
	}

	public List<Sonda> getListSondes() {
		Collection<Sonda> col = this.getMapSondes().values();
		List<Sonda> list = new ArrayList<>(col);
		Collections.sort(list, (s1,s2)-> {
			return Integer.valueOf(s1.getOrdre()).compareTo(s2.getOrdre());
		});
		
		return list;
	}

	
	/**
	 * 
	 * @param idSonda
	 * @return
	 * @throws ItemNotFoundException
	 */
	public Sonda getSonda(String idSonda) throws ItemNotFoundException {

		Map<String, Sonda> mapSondes = sondesLoader.getSondes();
		Sonda sonda = mapSondes.get(idSonda);
		if (sonda == null) {
            throw new ItemNotFoundException();
        }
		
		return sonda;
	}
	
	public Sonda getSonda(int address) throws ItemNotFoundException {

		Sonda sonda = mapSondes.get(address);
		if (sonda == null) {
            throw new ItemNotFoundException();
        }
		
		return sonda;
	}	
	
	public Map<Integer, SondaAtlas> getSondesAtlas() {
		return this.mapSondesAtlas;
	}

	public SondaAtlas getSondaAtlas(Sonda sonda) {
		return this.mapSondesAtlas.get(sonda.getAddress());
	}
	
		
	public List<PersistibleSondaBomba> getHistorial(int addressSonda, int numDies) throws ItemNotFoundException {
//		Collection<Sonda> listSondes = this.getListSondes();
//		sondesPersister.doPersistencia(listSondes);
		
		Sonda sonda = this.getSonda(addressSonda);
		List<PersistibleSonda> lpsReaded = sondesPersister.loadHistory(sonda.getId(), numDies);
		List<StateSonda> histReaded = sondesPersister.convert(sonda, lpsReaded);
		
		List<StateSonda> listState = new ArrayList<>();
		listState.addAll(sonda.getHistoric());
		for(StateSonda state : histReaded) {
			if(!listState.contains(state)) {
				listState.add(state);
			}
		}
		
		listState.sort(new Comparator<StateSonda>() {
			@Override
			public int compare(StateSonda o1, StateSonda o2) {
				return o1.getTimestamp().compareTo(o2.getTimestamp());
			}
		});
		
		
		List<PersistibleSonda> lpsHist = sondesPersister.convert(listState);
		
		
		Rele releCorrector = null;
		try {
			releCorrector = relesQuery.getRele(sonda.getIdReleCorrector());
		} catch (ItemNotFoundException e) {}
		
		//Obté historial del rele bomba
		List<PersistibleRele> histBomba = relesQuery.getHistorial(idReleBomba, numDiesHistoriaReles);

		//Obté historial del rele corrector
		List<PersistibleRele> histCorrector = null;
		if(releCorrector!=null) {
			histCorrector = relesQuery.getHistorial(releCorrector.getId(), numDiesHistoriaReles);
		}
		
		//Creua les dades
		List<PersistibleSondaBomba> list = new ArrayList<>();
		for(PersistibleSonda pSonda : lpsHist) {
			boolean bombaOn = matcher.bombaActiva(pSonda, histBomba);
			IntervalReleOn interval = matcher.getIntervalProper(pSonda, histCorrector);
			
			PersistibleSondaBomba sondaBomba = new PersistibleSondaBomba(pSonda, bombaOn);
			if(releCorrector!=null && interval!=null) {
				double consum = 0;
				if(interval.getOff()!=null) {
					consum = interval.getOff().getConsumRele();
				}
				else if(interval.getOn()!=null) {
					consum = interval.getOn().getConsumRele();
				}
				sondaBomba.setConsum(consum);
			}
			list.add(sondaBomba);
		}
		
		return list;
	}
}
