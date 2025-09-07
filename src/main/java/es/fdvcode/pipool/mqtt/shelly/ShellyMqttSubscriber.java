package es.fdvcode.pipool.mqtt.shelly;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class ShellyMqttSubscriber {

	private final Logger log = LoggerFactory.getLogger(ShellyMqttSubscriber.class);
	
	/*
	 *
	 * [INFO ] 2021-12-20 15:42:05 - MQTT MSG RECEIVED: {}GenericMessage [payload=announce, headers={mqtt_receivedRetained=false, mqtt_id=0, mqtt_duplicate=false, id=da3ba4ee-cead-42a7-976a-e823d082e10f, mqtt_receivedTopic=shellies/shellyflood-ABE866/command, mqtt_receivedQos=0, timestamp=1640011325603}]
Message from topic Shelly --> announce from Topic: shellies/shellyflood-ABE866/command
[INFO ] 2021-12-20 15:42:06 - MQTT MSG RECEIVED: {}GenericMessage [payload=true, headers={mqtt_receivedRetained=false, mqtt_id=1, mqtt_duplicate=false, id=7e4e79e6-1d1e-0939-d3ac-6bac470e319b, mqtt_receivedTopic=shellies/shellyflood-ABE866/online, mqtt_receivedQos=2, timestamp=1640011326020}]
Message from topic Shelly --> true from Topic: shellies/shellyflood-ABE866/online
[INFO ] 2021-12-20 15:42:06 - MQTT MSG RECEIVED: {}GenericMessage [payload={"id":"shellyflood-ABE866","model":"SHWT-1","mac":"E8DB84ABE866","ip":"192.168.1.67","new_fw":false,"fw_ver":"20211109-125548/v1.11.7-g682a0db"}, headers={mqtt_receivedRetained=false, mqtt_id=2, mqtt_duplicate=false, id=c477c3ce-9c27-c6d3-99dc-fcdc59be9703, mqtt_receivedTopic=shellies/announce, mqtt_receivedQos=2, timestamp=1640011326023}]
Message from topic Shelly --> {"id":"shellyflood-ABE866","model":"SHWT-1","mac":"E8DB84ABE866","ip":"192.168.1.67","new_fw":false,"fw_ver":"20211109-125548/v1.11.7-g682a0db"} from Topic: shellies/announce
[INFO ] 2021-12-20 15:42:06 - MQTT MSG RECEIVED: {}GenericMessage [payload={"id":"shellyflood-ABE866","model":"SHWT-1","mac":"E8DB84ABE866","ip":"192.168.1.67","new_fw":false,"fw_ver":"20211109-125548/v1.11.7-g682a0db"}, headers={mqtt_receivedRetained=false, mqtt_id=3, mqtt_duplicate=false, id=c393ccb1-7c7a-36df-d1a6-7d1ef34e4b2a, mqtt_receivedTopic=shellies/shellyflood-ABE866/announce, mqtt_receivedQos=2, timestamp=1640011326025}]
Message from topic Shelly --> {"id":"shellyflood-ABE866","model":"SHWT-1","mac":"E8DB84ABE866","ip":"192.168.1.67","new_fw":false,"fw_ver":"20211109-125548/v1.11.7-g682a0db"} from Topic: shellies/shellyflood-ABE866/announce
[INFO ] 2021-12-20 15:42:06 - MQTT MSG RECEIVED: {}GenericMessage [payload={"wifi_sta":{"connected":true,"ssid":"","ip":"192.168.1.67","rssi":-83},"cloud":{"enabled":false,"connected":false},"mqtt":{"connected":true},"time":"","unixtime":0,"serial":1,"has_update":false,"mac":"E8DB84ABE866","cfg_changed_cnt":0,"actions_stats":{"skipped":0},"is_valid":true,"flood":false,"tmp":{"value":8.25,"units":"C","tC":8.25,"tF":46.85,"is_valid":true},"bat":{"value":96,"voltage":2.93},"act_reasons":["sensor"],"rain_sensor":false,"update":{"status":"unknown","has_update":false,"new_version":"","old_version":"20211109-125548/v1.11.7-g682a0db"},"ram_total":51544,"ram_free":38452,"fs_size":233681,"fs_free":146835,"uptime":3}, headers={mqtt_receivedRetained=false, mqtt_id=4, mqtt_duplicate=false, id=5f06df48-42e7-f60d-767a-ad98d4e929a7, mqtt_receivedTopic=shellies/shellyflood-ABE866/info, mqtt_receivedQos=2, timestamp=1640011326026}]
Message from topic Shelly --> {"wifi_sta":{"connected":true,"ssid":"","ip":"192.168.1.67","rssi":-83},"cloud":{"enabled":false,"connected":false},"mqtt":{"connected":true},"time":"","unixtime":0,"serial":1,"has_update":false,"mac":"E8DB84ABE866","cfg_changed_cnt":0,"actions_stats":{"skipped":0},"is_valid":true,"flood":false,"tmp":{"value":8.25,"units":"C","tC":8.25,"tF":46.85,"is_valid":true},"bat":{"value":96,"voltage":2.93},"act_reasons":["sensor"],"rain_sensor":false,"update":{"status":"unknown","has_update":false,"new_version":"","old_version":"20211109-125548/v1.11.7-g682a0db"},"ram_total":51544,"ram_free":38452,"fs_size":233681,"fs_free":146835,"uptime":3} from Topic: shellies/shellyflood-ABE866/info
[INFO ] 2021-12-20 15:42:06 - MQTT MSG RECEIVED: {}GenericMessage [payload=8.25, headers={mqtt_receivedRetained=false, mqtt_id=5, mqtt_duplicate=false, id=f1c1e615-958a-3b4a-e5a9-f7cc83681187, mqtt_receivedTopic=shellies/shellyflood-ABE866/sensor/temperature, mqtt_receivedQos=2, timestamp=1640011326031}]
Message from topic Shelly --> 8.25 from Topic: shellies/shellyflood-ABE866/sensor/temperature
[INFO ] 2021-12-20 15:42:06 - MQTT MSG RECEIVED: {}GenericMessage [payload=false, headers={mqtt_receivedRetained=false, mqtt_id=6, mqtt_duplicate=false, id=2794c938-5862-ad7b-58dc-2bb083411413, mqtt_receivedTopic=shellies/shellyflood-ABE866/sensor/flood, mqtt_receivedQos=2, timestamp=1640011326034}]
Message from topic Shelly --> false from Topic: shellies/shellyflood-ABE866/sensor/flood
[INFO ] 2021-12-20 15:42:06 - MQTT MSG RECEIVED: {}GenericMessage [payload=96, headers={mqtt_receivedRetained=false, mqtt_id=7, mqtt_duplicate=false, id=27ddfc91-fb9f-e3d0-ac93-8db0c494f39f, mqtt_receivedTopic=shellies/shellyflood-ABE866/sensor/battery, mqtt_receivedQos=2, timestamp=1640011326035}]
Message from topic Shelly --> 96 from Topic: shellies/shellyflood-ABE866/sensor/battery
[INFO ] 2021-12-20 15:42:06 - MQTT MSG RECEIVED: {}GenericMessage [payload=0, headers={mqtt_receivedRetained=false, mqtt_id=8, mqtt_duplicate=false, id=fa149dd3-0e4c-6fd8-6aca-2ff95b0bc9af, mqtt_receivedTopic=shellies/shellyflood-ABE866/sensor/error, mqtt_receivedQos=2, timestamp=1640011326036}]
Message from topic Shelly --> 0 from Topic: shellies/shellyflood-ABE866/sensor/error
[INFO ] 2021-12-20 15:42:06 - MQTT MSG RECEIVED: {}GenericMessage [payload=["sensor"], headers={mqtt_receivedRetained=false, mqtt_id=9, mqtt_duplicate=false, id=28c3adc7-160f-c565-04ee-5def757e4257, mqtt_receivedTopic=shellies/shellyflood-ABE866/sensor/act_reasons, mqtt_receivedQos=2, timestamp=1640011326038}]
Message from topic Shelly --> ["sensor"] from Topic: shellies/shellyflood-ABE866/sensor/act_reasons

	 */
	
	public void handle(Object payload, Object headers, String topic) {
		String shellyFloodPrefix = "shellies/shellyflood-ABE866/sensor/";
		
		if(payload==null) payload="";
			
        if(topic.startsWith(shellyFloodPrefix)) {
    		log.info("SHELLY-FLOOD -> {} = {}", topic.replaceAll(shellyFloodPrefix, topic), payload.toString());
    		
    		//TODO: actualitzar nou tipus de sensor pipool
        }
        
	}

}
