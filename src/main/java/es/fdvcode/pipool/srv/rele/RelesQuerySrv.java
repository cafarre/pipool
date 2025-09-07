package es.fdvcode.pipool.srv.rele;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Component;

import com.pi4j.io.gpio.digital.DigitalOutput;

import es.fdvcode.pipool.model.rele.PersistibleRele;
import es.fdvcode.pipool.model.rele.Rele;
import es.fdvcode.pipool.model.rele.StateRele;
import es.fdvcode.pipool.model.rele.StateRele.CausaState;
import es.fdvcode.pipool.srv.ItemNotFoundException;
import lombok.RequiredArgsConstructor;

/**
 * 
 * @author cfarrema
 *
 */
@Component
@RequiredArgsConstructor
public class RelesQuerySrv {
	private final RelesLoader relesLoader;
	private final GpioPinController gpioController;
	private final RelesPersister relesPersister;
	
	public Map<String, Rele> getSyncReles() {
		Map<String, Rele> mapReles = relesLoader.getReles();
		syncAllReles(mapReles);
		return mapReles;
	}

	public Map<String, Rele> getNoSyncReles() {
		Map<String, Rele> mapReles = relesLoader.getReles();
		return mapReles;
	}

	
	private void syncAllReles(Map<String, Rele> mapReles) {
		for(Rele rele : mapReles.values()) {
			syncRele(rele);
		}
	}
	
	/**
	 * 
	 * @param rele
	 */
	public void syncRele(Rele rele) {
		DigitalOutput  gpioPin = gpioController.getGpioPin(rele.getGpioPin());
		
		//Sincronitza estat rele amb gpio
		StateRele state = rele.getCopyStateRele();
		boolean stateOldOn = state.isOn();
		boolean gpioPinIsHigh = gpioPin.isHigh();
		state.syncGpioPin(gpioPinIsHigh);
		
		if(state.isOn()!=stateOldOn) {
			rele.setNewStateRele(state, CausaState.SYNCGPIO, "RELESRV - SyncRele -> S'ha canviat estat Rele per Sync amb GPIO. GpioPin="+ gpioPinIsHigh +".");
		}
		else {
			rele.updateStateRele(state);
		}		
	}
	
	/**
	 * 
	 * @param idRele
	 * @return
	 * @throws ItemNotFoundException
	 */
	public Rele getRele(String idRele) throws ItemNotFoundException {

		Map<String, Rele> mapReles = relesLoader.getReles();
		Rele rele = mapReles.get(idRele);
		if (rele == null) {
            throw new ItemNotFoundException();
        }
		
		syncRele(rele);
				
		return rele;        
	}	
	
	public String getReleName(String idRele) {
		try {
			Rele rele = this.getRele(idRele);
			return rele.getNom();
		} catch (ItemNotFoundException e) {
			return idRele;
		}
	}
	
	public List<PersistibleRele> getHistorial(String idRele, int numDies) throws ItemNotFoundException {
		
		Rele rele = this.getRele(idRele);
		
		List<PersistibleRele> lprReaded = relesPersister.loadHistory(idRele, numDies);
		List<StateRele> histReaded = relesPersister.convert(rele, lprReaded);

		List<StateRele> listState = new ArrayList<>();
		listState.addAll(rele.getHistoric());
		for(StateRele state : histReaded) {
			if(!listState.contains(state)) {
				listState.add(state);
			}
		}
		
		listState.sort(new Comparator<StateRele>() {

			@Override
			public int compare(StateRele o1, StateRele o2) {
				return o1.getTimestamp().compareTo(o2.getTimestamp());
			}
		});
		
		List<PersistibleRele> result = relesPersister.convert(listState); 
		return result;
	}	
}
