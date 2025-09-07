package es.fdvcode.pipool.mqtt.homeassistant.model;

public class BinarySensorMqtt extends EntityMqtt {
	
	public BinarySensorMqtt(String entityId, String name, String state, String topicPrefix, String deviceName) {
		super(entityId, name, state, topicPrefix, deviceName);
	}
	
	@Override
	public String getDomain() {
		return "binary_sensor";
	}

	@Override
	public String getConfigPayload() {
		StringBuilder sb = new StringBuilder();
		sb.append(this.getPrefixConfigPayload());
		sb.append(this.getSufixConfigPayload());	
		
		return sb.toString();
	}
}
