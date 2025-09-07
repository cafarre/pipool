package es.fdvcode.pipool.mqtt.homeassistant;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import es.fdvcode.pipool.model.rele.StateRele;
import es.fdvcode.pipool.model.sonda.Sonda;
import es.fdvcode.pipool.mqtt.homeassistant.model.EntityMqtt;
import es.fdvcode.pipool.mqtt.homeassistant.model.MultipleSensorMqtt;

//TODO
/*
 * -- informar el device en payload config: https://www.home-assistant.io/integrations/switch.mqtt/
 */

@Component
public class PipoolEntitiesMqttSrv {

	private final Logger log= LoggerFactory.getLogger(this.getClass());
	
	@Autowired
	private PipoolEntitiesMqttLoader loader;
	
	@Autowired
	private HomeAssistantMqttPublisher mqttPub;
	
	public void pubConfigAll() {
		
		try {
			List<EntityMqtt> list = loader.loadAllEntities();
			if(list==null) {
				return;
			}
			
			for(EntityMqtt entity : list) {
				if(entity instanceof MultipleSensorMqtt) {
					MultipleSensorMqtt multiple = (MultipleSensorMqtt) entity;
					for(EntityMqtt subentity : multiple.getSensors()) {
						mqttPub.config(subentity);
					}
				}
				else {
					mqttPub.config(entity);	
				}
			}
			
		} catch (Exception e) {
			log.warn("Error al publicar algun MQTT.", e);
		}

	}

	public void pubStateAllReles() {
		try {
			List<EntityMqtt> list = loader.loadAllReles();
			for(EntityMqtt entity : list) {
				mqttPub.state(entity);
			}
		} catch (Exception e) {
			log.warn("Error al publicar algun MQTT.", e);
		}		
	}
	
	
	public void pubStateRele(String idRele) {
		try {
			List<EntityMqtt> list = loader.loadRele(idRele);
			for(EntityMqtt entity : list) {
				mqttPub.state(entity);
			}
		} catch (Exception e) {
			log.warn("Error al publicar algun MQTT.", e);
		}		
	}

	public void pubStateRele(StateRele rele) {
		try {
			List<EntityMqtt> list = loader.loadRele(rele);
			for(EntityMqtt entity : list) {
				mqttPub.state(entity);
			}
		} catch (Exception e) {
			log.warn("Error al publicar algun MQTT.", e);
		}		
	}

	public void pubStateLastStatus() {
		try {
			EntityMqtt ent = loader.loadLastDateTimeEntity();
			mqttPub.state(ent);
		} catch (Exception e) {
			log.warn("Error al publicar algun MQTT.", e);
		}		
	}
	
	public void pubStateSonda(String idSonda) {
		try {
			EntityMqtt entity = loader.loadSonda(idSonda);
			mqttPub.state(entity);
		} catch (Exception e) {
			log.warn("Error al publicar algun MQTT.", e);
		}
	}

	
	public void pubStateSonda(Sonda sonda) {
		try {
			EntityMqtt entity = loader.loadSonda(sonda);
			mqttPub.state(entity);
		} catch (Exception e) {
			log.warn("Error al publicar algun MQTT.", e);
		}
	}
	
	public void pubDeleteAll() {
		try {
			List<EntityMqtt> list = loader.loadAllEntities();
			if(list==null) {
				return;
			}
			
			for(EntityMqtt entity : list) {
				mqttPub.delete(entity);
			}
		
		} catch (Exception e) {
			log.warn("Error al publicar algun MQTT.", e);
		}
	}
}
