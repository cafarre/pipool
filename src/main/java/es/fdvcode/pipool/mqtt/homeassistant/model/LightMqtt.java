package es.fdvcode.pipool.mqtt.homeassistant.model;

public class LightMqtt extends EntityMqtt {
		
	public LightMqtt(String entityId, String name, String state, String topicPrefix, String deviceName) {
		super(entityId, name, state, topicPrefix, deviceName);
	}

	@Override
	public String getDomain() {
		return "light";
	}
	
	public String getConfigPayload() {
		StringBuilder sb = new StringBuilder();
		sb.append(this.getPrefixConfigPayload())
			.append("\"cmd_t\": \"").append(this.getAbreviatedCommandTopic()).append("\", ")
			.append(this.getSufixConfigPayload());	
			
		return sb.toString();
	}
}
