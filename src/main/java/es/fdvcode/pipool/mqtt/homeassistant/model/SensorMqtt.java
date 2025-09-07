package es.fdvcode.pipool.mqtt.homeassistant.model;

public class SensorMqtt extends EntityMqtt {
	
	protected String deviceClass;
	protected String units;
	
	public SensorMqtt(String entityId, String name, String state, String topicPrefix, String deviceName) {
		this(entityId, name, state, null, null, topicPrefix, deviceName);
	}

	public SensorMqtt(String entityId, String name, String state, String deviceClass, String units, String topicPrefix, String deviceName) {
		super(entityId, name, state, topicPrefix, deviceName);
		this.deviceClass = deviceClass;
		this.units = units;
	}
	

	@Override
	public String getDomain() {
		return "sensor";
	}

	@Override
	public String getPrefixConfigPayload() {
		StringBuilder sb = new StringBuilder();
		sb.append(super.getPrefixConfigPayload());
		
		if(deviceClass!=null) {
			sb.append("\"device_class\": \"").append(deviceClass).append("\", ");
		}
		
		if(units!=null) {
			sb.append("\"unit_of_measurement\": \"").append(units).append("\", ");
		}
		
		return sb.toString();
	}
	
	@Override
	public String getConfigPayload() {
		StringBuilder sb = new StringBuilder();
		sb.append(this.getPrefixConfigPayload());
		sb.append(this.getSufixConfigPayload());	
		
		return sb.toString();
	}
}
