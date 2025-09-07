package es.fdvcode.pipool.mqtt;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class MqttEmitter implements MqttGateway {

	private final Logger log = LoggerFactory.getLogger(MqttConfiguration.class);
	
	@Autowired
	private MqttGateway gtw;
	
	@Override
	public void sendToMqtt(String data, String topic) {
		try {
			gtw.sendToMqtt(data, topic);
		} catch (Throwable e) {
			log.warn(e.getMessage());
		}
	}

	@Override
	public void sendToMqtt(String data, String topic, Integer qos, Boolean retained) {
		try {
			gtw.sendToMqtt(data, topic, qos, retained);
		} catch (Throwable e) {
			log.warn(e.getMessage());
		}
	}
}
