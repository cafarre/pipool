package es.fdvcode.pipool.mqtt.homeassistant.model;

public class SubSensorMqtt extends SensorMqtt {
	
	private String stateTopic;
	private String stateName;
	
	public SubSensorMqtt(String entityId, String name, String state, String topicPrefix, String deviceName, String stateTopic, String stateName) {
		this(entityId, name, state, null, null, topicPrefix, deviceName, stateTopic, stateName);
	}

	public SubSensorMqtt(String entityId, String name, String state, String deviceClass, String units, String topicPrefix, String deviceName, String stateTopic, String stateName) {
		super(entityId, name, state, deviceClass, units, topicPrefix, deviceName);
		this.stateTopic = stateTopic;
		this.stateName = stateName;
	}
	

	public String getStateTopic() {
		return stateTopic;
	}

	public String getStateName() {
		return stateName;
	}

	@Override
	public String getConfigPayload() {
		StringBuilder sb = new StringBuilder();
		sb.append(this.getPrefixConfigPayload());
		sb.append("\"schema\": \"json\", ");

		sb.append("\"value_template\": \"{{ value_json.").append(this.stateName).append("}}\", ");
		
		sb.append(this.getSufixConfigPayload());	
		
		return sb.toString();
	}
	
	@Override
	public String getSufixConfigPayload() {
		StringBuilder sb = new StringBuilder();
		sb.append("\"stat_t\": \"").append(stateTopic).append("\"}");	
			
		return sb.toString();
	}
}
