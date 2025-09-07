package es.fdvcode.pipool.srv.sonda;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.springframework.stereotype.Component;

import es.fdvcode.pipool.model.rele.PersistibleRele;
import es.fdvcode.pipool.model.sonda.PersistibleSonda;

@Component
public class SondaBombaMatcher {

	public boolean bombaActiva(PersistibleSonda sonda, List<PersistibleRele> histBomba) {
		IntervalReleOn interval = getIntervalOn(sonda, histBomba);
		return (interval!=null);
	}
	
	public IntervalReleOn getIntervalOn(PersistibleSonda sonda, List<PersistibleRele> histBomba) {
		if(histBomba==null || sonda==null) return null;
		
		List<IntervalReleOn> intervalsOn = calcIntervalsOn(histBomba);		
		
		//Busca si la hora de la sonda esta dins d'una parella
		for(IntervalReleOn interval : intervalsOn) {
			if(sonda.getTimestamp().after(interval.getOn().getTimestamp())){

				if (interval.getOff()==null || sonda.getTimestamp().before(interval.getOff().getTimestamp())) {
					return interval;					
				}
			}
		}

		return null;
	}

	public IntervalReleOn getIntervalProper(PersistibleSonda sonda, List<PersistibleRele> histBomba) {
		if(histBomba==null || sonda==null) return null;
		
		List<IntervalReleOn> intervalsOn = calcIntervalsOn(histBomba);		
		
		//Busca si la hora de la sonda esta dins d'una parella
		for(IntervalReleOn interval : intervalsOn) {
			if(sonda.getTimestamp().after(interval.getOn().getTimestamp())){

				if (interval.getOff()==null || sonda.getTimestamp().before(interval.getOff().getTimestamp())) {
					return interval;					
				}
			}
		}

		//Busca la Fi més propera
		IntervalReleOn intervalProper=null;
		for(IntervalReleOn interval : intervalsOn) {
			Date dt = interval.getOn().getTimestamp();
			if(interval.getOff()!=null) {
				dt = interval.getOff().getTimestamp();
			}

			Date dtProper = null;
			if(intervalProper!=null) {
				if(intervalProper.getOff()!=null) {
					dtProper = intervalProper.getOff().getTimestamp();
				}
				else {
					intervalProper.getOn().getTimestamp();
				}
			}
			
			if(sonda.getTimestamp().after(dt)){

				if (intervalProper==null || dtProper.before(dt)) {
					intervalProper = interval;					
				}
			}
		}

		return intervalProper;
	}

	
	private List<IntervalReleOn> calcIntervalsOn(List<PersistibleRele> histBomba) {
		List<IntervalReleOn> result = new ArrayList<>();
		List<PersistibleRele> newHistBomba = new ArrayList<>();
		
		newHistBomba.addAll(histBomba);
		recursiveCalcIntervalsOn(newHistBomba, result);
		
		return result;
	}
	
	private void recursiveCalcIntervalsOn(List<PersistibleRele> histBomba, List<IntervalReleOn> result) {
		List<PersistibleRele> newHistBomba;

		IntervalReleOn interval = new IntervalReleOn();
		if(histBomba.size() > 0) {
			PersistibleRele event = histBomba.get(0);
			if(event.isOn()) {
				interval.setOn(event);
				
				if(histBomba.size() > 1) {
					PersistibleRele event2 = histBomba.get(1);
					if(!event2.isOn()) {
						interval.setOff(event2);

						result.add(interval);
						newHistBomba = histBomba.subList(2, histBomba.size());
						recursiveCalcIntervalsOn(newHistBomba, result);
					}
					else {
						newHistBomba = histBomba.subList(1, histBomba.size());
						recursiveCalcIntervalsOn(newHistBomba, result);
					}					
				}
				else {
					result.add(interval);
				}
			}
			else {
				newHistBomba = histBomba.subList(1, histBomba.size());
				recursiveCalcIntervalsOn(newHistBomba, result);
			}
		}
	}
	
	class IntervalReleOn {
		private PersistibleRele on;
		private PersistibleRele off;
		
		public PersistibleRele getOn() {
			return on;
		}
		public void setOn(PersistibleRele on) {
			this.on = on;
		}
		public PersistibleRele getOff() {
			return off;
		}
		public void setOff(PersistibleRele off) {
			this.off = off;
		}
		
		public String toString() {
			String str = on.getTimestamp().toString() + " --> ";
			if(off!=null && off.getTimestamp()!=null) {
				str = str + off.getTimestamp().toString();
			}
			else {
				str = str + "(ND)";
			}
			return str;
		}
	}

	
}
