package es.fdvcode.pipool.mqtt.homeassistant;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import es.fdvcode.pipool.model.rele.Rele;
import es.fdvcode.pipool.model.rele.StateRele;
import es.fdvcode.pipool.model.rele.StateRele.ModeRele;
import es.fdvcode.pipool.model.sonda.Sonda;
import es.fdvcode.pipool.mqtt.homeassistant.model.EntityMqtt;
import es.fdvcode.pipool.mqtt.homeassistant.model.LightMqtt;
import es.fdvcode.pipool.mqtt.homeassistant.model.MqttType;
import es.fdvcode.pipool.mqtt.homeassistant.model.MultipleSensorMqtt;
import es.fdvcode.pipool.mqtt.homeassistant.model.SensorMqtt;
import es.fdvcode.pipool.mqtt.homeassistant.model.SubSensorMqtt;
import es.fdvcode.pipool.mqtt.homeassistant.model.SwitchMqtt;
import es.fdvcode.pipool.srv.ItemNotFoundException;
import es.fdvcode.pipool.srv.rele.RelesQuerySrv;
import es.fdvcode.pipool.srv.sonda.SondesQuerySrv;
import lombok.RequiredArgsConstructor;


@Component
@RequiredArgsConstructor
public class PipoolEntitiesMqttLoader {

	private final RelesQuerySrv relesSrv;
	private final SondesQuerySrv sondesSrv;
	
	@Value("${pipool.mqtt.device-name}")
	private String deviceName;

	@Value("${pipool.mqtt.topics-prefix.ha}")
	private String topicPrefixHA;

	
	public List<EntityMqtt> loadAllEntities() {
		List<EntityMqtt> entities = new ArrayList<EntityMqtt>();
		
		entities.addAll(loadAllReles());
		
		//Sondes
		Collection<Sonda> sondes = sondesSrv.getListSondes();
		for(Sonda sonda : sondes) {
			EntityMqtt entity = this.loadSonda(sonda);
			if(entity!=null) { 
				entities.add(entity);
			}
		}

		entities.add(loadLastDateTimeEntity());
		
		return entities;
	}

	public EntityMqtt loadLastDateTimeEntity() {
		//TODO: sensors de estat de rPi i Pipool
		//Hora Pipool
		SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm"); 
		EntityMqtt entity = new SensorMqtt("last_status", 
				"Last Status", 
				sdf.format(new Date()),
				null,
				null,
				topicPrefixHA,
				deviceName);
		
		return entity;
	}
	
	public List<EntityMqtt> loadAllReles() {
		List<EntityMqtt> entities = new ArrayList<EntityMqtt>();
		//Reles
		Collection<Rele> reles = relesSrv.getSyncReles().values();
		for(Rele rele : reles) {
			List<EntityMqtt> list = this.loadRele(rele.getCopyStateRele());
			if(list!=null) { 
				entities.addAll(list);
			}
		}
		
		return entities;
	}
	
	public List<EntityMqtt> loadRele(String idRele) {
		Rele rele;
		try {
			rele = relesSrv.getRele(idRele);
		} catch (ItemNotFoundException e) {
			return null;
		}
		
		return loadRele(rele.getCopyStateRele());
	}
	
	public List<EntityMqtt> loadRele(StateRele state) {
		String isOn = state.isOn() ? "ON": "OFF";
		Rele rele = state.getRele();
		
		List<EntityMqtt> list = new ArrayList<>();
		if(!rele.isMqttEnabled()) {
			return list;
		}
		
		EntityMqtt entity = null;
		MqttType type = MqttType.valueOf(rele.getMqttType());
		if(MqttType.SWITCH.equals(type)) {
			entity = new SwitchMqtt(rele.getId(), rele.getNom(), isOn, topicPrefixHA, deviceName);
		}
		else if(MqttType.LIGHT.equals(type)) {
			entity = new LightMqtt(rele.getId(), rele.getNom(), isOn, topicPrefixHA, deviceName);
		}

		if(entity != null) {
			list.add(entity);
			
			//Switch mode Auto
			String isAuto = ModeRele.AUTO.equals(state.getMode()) ? "ON": "OFF";
			EntityMqtt entityAuto = new SwitchMqtt(rele.getId() + "_auto", rele.getNom() + " - Auto", isAuto, topicPrefixHA, deviceName);
			list.add(entityAuto);

			//Sensors
			MultipleSensorMqtt sensors = new MultipleSensorMqtt(rele.getId() + "_sensors", topicPrefixHA, deviceName);
			sensors.addSensor(new SubSensorMqtt(
					rele.getId() + "_cause", 
					rele.getNom() + " - Cause", 
					state.getCausa().name(), 
					topicPrefixHA,
					deviceName,
					sensors.getStateTopic(),
					"cause"));
			
			if(rele.isMqttConsumSensorEnabled()) {
				String value = roundValue(state.getConsumRele());
				sensors.addSensor(new SubSensorMqtt(
						rele.getId() + "_consum", 
						rele.getNom() + " - Consum", 
						value,
						"volume", 
						"mL",
						topicPrefixHA,
						deviceName,						
						sensors.getStateTopic(),
						"consume"
						));
			}
						
			list.add(sensors);
		}
		
		return list;
	}
	
	public EntityMqtt loadSonda(String idSonda) {
		Sonda sonda;
		try {
			sonda = sondesSrv.getSonda(idSonda);
		} catch (ItemNotFoundException e) {
			return null;
		}
		
		return loadSonda(sonda);
	}
	
	public EntityMqtt loadSonda(Sonda sonda) {
		if(sonda.getStateSonda() == null) {
			return null;
		}
		
		String state = roundValue(sonda.getStateSonda().getValor());
		EntityMqtt entity = new SensorMqtt(sonda.getId(), 
				sonda.getNom(), 
				state,
				sonda.getHaDeviceClass(),
				sonda.getUnitats(),
				topicPrefixHA,
				deviceName);
		
		return entity;
	}
	
	private String roundValue(String value) {
		String result = "";
		try {
			double d = Double.parseDouble(value);
			result = roundValue(d);
		} catch (NumberFormatException e) {
		}
		
		return result;
	}

	private String roundValue(double d) {
		double roundOff = Math.round(d * 100.0) / 100.0;
		return String.valueOf(roundOff);
	}

}
