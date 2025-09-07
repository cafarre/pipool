package es.fdvcode.pipool.mqtt.homeassistant;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import es.fdvcode.pipool.mqtt.MqttEmitter;
import es.fdvcode.pipool.mqtt.homeassistant.model.EntityMqtt;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class HomeAssistantMqttPublisher {

	private final Logger log = LoggerFactory.getLogger(this.getClass());
	private static int QOS = 1;
	
	private final MqttEmitter emitter;

	public void config(EntityMqtt entity) {
		emitter.sendToMqtt(entity.getConfigPayload(), entity.getConfigurationTopic(), QOS, true);
		log.info("MQTT PUB CONFIG -> Topic: {} - Payload: {}", entity.getConfigurationTopic(), entity.getConfigPayload());
	}
	
	public void state(EntityMqtt entity) {
		emitter.sendToMqtt(entity.getStatePayload(), entity.getStateTopic(), QOS, true);
		log.info("MQTT PUB STATE -> Topic: {} - Payload: {}", entity.getStateTopic(), entity.getStatePayload());
	}
	
	public void delete(EntityMqtt entity) {
		emitter.sendToMqtt("", entity.getConfigurationTopic(), QOS, true);
		log.info("MQTT PUB DELETE -> Topic: {} - Payload: Blank", entity.getConfigurationTopic());
	}

}
