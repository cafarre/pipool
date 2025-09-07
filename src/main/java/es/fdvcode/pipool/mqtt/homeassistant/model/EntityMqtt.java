package es.fdvcode.pipool.mqtt.homeassistant.model;

/*
 * Setting up a switch is similar but requires a command_topic as mentioned in the MQTT switch documentation.

Configuration topic: homeassistant/switch/irrigation/config
State topic: homeassistant/switch/irrigation/state
Command topic: homeassistant/switch/irrigation/set
Payload: {"name": "garden", "command_topic": "homeassistant/switch/irrigation/set", "state_topic": "homeassistant/switch/irrigation/state"}
mosquitto_pub -h 127.0.0.1 -p 1883 -t "homeassistant/switch/irrigation/config" \
  -m '{"name": "garden", "command_topic": "homeassistant/switch/irrigation/set", "state_topic": "homeassistant/switch/irrigation/state"}'
Bash
Set the state.

mosquitto_pub -h 127.0.0.1 -p 1883 -t "homeassistant/switch/irrigation/set" -m ON
 */

public abstract class EntityMqtt {
	protected String topicPrefix = "homeassistant";
	protected String deviceName = "pipool";
	protected String uniqueId;
	protected String name;
	protected String state;

	
	public abstract String getDomain();
	public abstract String getConfigPayload();
	
	public EntityMqtt(String uniqueId, String name, String state, String topicPrefix, String deviceName) {
		super();
		this.uniqueId = deviceName.toLowerCase() + "_" + uniqueId;
		this.name = deviceName + " - " + name;
		this.state = state;
		this.deviceName = deviceName;
		this.topicPrefix = topicPrefix;
	}
	
	
	public String getUniqueId() {
		return uniqueId;
	}
	public String getName() {
		return name;
	}
	public String getState() {
		return state;
	}
	public String getTopicPrefix() {
		return topicPrefix;
	}
	public String getDeviceName() {
		return deviceName;
	}
	
	public String getBaseTopic() {
		return this.topicPrefix + "/" + this.getDomain() + "/" + this.getUniqueId();
	}
	
	public String getConfigurationTopic() {
		return  getBaseTopic() + "/config";
	}

	public String getStateTopic() {
		return getBaseTopic() + "/state";
	}
	
	public String getCommandTopic() {
		return getBaseTopic() + "/set";
	}

	public String getAbreviatedStateTopic() {
		return "~/state";
	}
	
	public String getAbreviatedCommandTopic() {
		return "~/set";
	}
	
	public String getPrefixConfigPayload() {
		StringBuilder sb = new StringBuilder();
		sb.append("{")
			.append("\"~\": \"").append(this.getBaseTopic()).append("\", ")
			.append("\"unique_id\": \"").append(this.getUniqueId()).append("\", ")	
			.append("\"name\": \"").append(this.getName()).append("\", ");
			
		return sb.toString();
	}
	
	public String getSufixConfigPayload() {
		StringBuilder sb = new StringBuilder();
		sb.append("\"stat_t\": \"").append(this.getAbreviatedStateTopic()).append("\"}");	
			
		return sb.toString();
	}
	
	public String getStatePayload() {
		return this.getState();
	}

}
