package es.fdvcode.pipool.mqtt.homeassistant.model;

import java.util.ArrayList;
import java.util.List;

public class MultipleSensorMqtt extends EntityMqtt {
	
	private List<SubSensorMqtt> sensors = new ArrayList<>();

	public MultipleSensorMqtt(String uniqueId, String topicPrefix, String deviceName) {
		super(uniqueId, "", "", topicPrefix, deviceName);
	}

	public List<SubSensorMqtt> getSensors() {
		return sensors;
	}

	public void addSensor(SubSensorMqtt sensor) {
		this.sensors.add(sensor);
	}

	@Override
	public String getStateTopic() {
		return getBaseTopic() + "/state";
	}
	
	@Override
	public String getStatePayload() {
		StringBuilder sb = new StringBuilder();
		sb.append("{");
		int count = 1;
		for(SubSensorMqtt sensor : sensors) {
			if(count >= 2) {
				sb.append(", ");
			}
			sb.append("\"").append(sensor.getStateName()).append("\": ");
			sb.append("\"").append(sensor.getState()).append("\"");
			
			count++;
		}
		sb.append("}");
		
		return sb.toString();
	}
	
	@Override
	public String getDomain() {
		return "sensor";
	}

	@Override
	public String getConfigPayload() {
		return "";
	}
}
