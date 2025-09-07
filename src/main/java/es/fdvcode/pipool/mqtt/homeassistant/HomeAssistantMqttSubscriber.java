package es.fdvcode.pipool.mqtt.homeassistant;

import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import es.fdvcode.pipool.model.rele.Rele;
import es.fdvcode.pipool.model.rele.StateRele;
import es.fdvcode.pipool.srv.rele.RelesQuerySrv;
import es.fdvcode.pipool.srv.rele.RelesSrv;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class HomeAssistantMqttSubscriber {

	private final Logger log = LoggerFactory.getLogger(HomeAssistantMqttSubscriber.class);

	@Value("${pipool.mqtt.topics-prefix.ha}")
	private String topicPrefix;
	
	final RelesSrv relesSrv;
	final RelesQuerySrv relesQuerySrv;

	private Date heartbeatHA = new Date();
	
	//TODO
	/*
	 * -- Atendre topics:
	 * 		- homeassistant/status = online/offline que indica estat HA --> quan esta online, reenviar estat de tots reles i sondes
	 * 		- homeassistant/pipool/resync per reenviar estat de tots reles i sondes
	 * 		- homeassistant/pipool/restart per reiniciar pipool
	 * 		- homeassistant/pipool/reboot per reiniciar rpipool
	 * 		- homeassistant/switch/* per acatar les ordres de HA sobre els reles
	 * 		- homeassistant/light/* per acatar les ordres de HA sobre les llums
	 *  	
	 */
	
	public void handle(Object payload, Object headers, String topic) {
		if(topic!=null && ((topic.contains("/pipool_rele_") && topic.endsWith("/set")) 
				|| topic.endsWith("/heartbeat"))) {
			log.info("Message RECEIVED from topic HA --> {} / Payload: {}", topic, payload);
		}
		else {
			return;
		}

        if((topicPrefix + "/switch/pipool_rele_bomba/set").equalsIgnoreCase(topic)) {
        	executeCommandHA("rele_bomba", payload.toString());
        }
        else if((topicPrefix + "/switch/pipool_rele_fan/set").equalsIgnoreCase(topic)) {
        	executeCommandHA("rele_fan", payload.toString());
        }
        else if((topicPrefix + "/switch/pipool_rele_bomba_clor/set").equalsIgnoreCase(topic)) {
        	executeCommandHA("rele_bomba_clor", payload.toString());
        }
        else if((topicPrefix + "/switch/pipool_rele_bomba_acid/set").equalsIgnoreCase(topic)) {
        	executeCommandHA("rele_bomba_acid", payload.toString());
        }
        else if((topicPrefix + "/switch/pipool_rele_lfi/set").equalsIgnoreCase(topic)) {
        	executeCommandHA("rele_lfi", payload.toString());
        }
        else if((topicPrefix + "/light/pipool_rele_llums/set").equalsIgnoreCase(topic)) {
        	executeCommandHA("rele_llums", payload.toString());
        }
        else if((topicPrefix + "/light/pipool_rele_llums_jardi/set").equalsIgnoreCase(topic)) {
        	executeCommandHA("rele_llums_jardi", payload.toString());
        }
        else if((topicPrefix + "/switch/pipool_rele_bomba_auto/set").equalsIgnoreCase(topic)) {
        	executeCommandAutoHA("rele_bomba", payload.toString());
        }
        else if((topicPrefix + "/switch/pipool_rele_fan_auto/set").equalsIgnoreCase(topic)) {
        	executeCommandAutoHA("rele_fan", payload.toString());
        }
        else if((topicPrefix + "/switch/pipool_rele_bomba_clor_auto/set").equalsIgnoreCase(topic)) {
        	executeCommandAutoHA("rele_bomba_clor", payload.toString());
        }
        else if((topicPrefix + "/switch/pipool_rele_bomba_acid_auto/set").equalsIgnoreCase(topic)) {
        	executeCommandAutoHA("rele_bomba_acid", payload.toString());
        }
        else if((topicPrefix + "/switch/pipool_rele_lfi_auto/set").equalsIgnoreCase(topic)) {
        	executeCommandAutoHA("rele_lfi", payload.toString());
        }
        else if((topicPrefix + "/switch/pipool_rele_llums_auto/set").equalsIgnoreCase(topic)) {
        	executeCommandAutoHA("rele_llums", payload.toString());
        }
        else if((topicPrefix + "/switch/pipool_rele_llums_jardi_auto/set").equalsIgnoreCase(topic)) {
        	executeCommandAutoHA("rele_llums_jardi", payload.toString());
        }
        else if((topicPrefix + "/heartbeat").equalsIgnoreCase(topic)) {
        	executeUpdateHeartbeat(payload.toString());
        }
	}
		
	public Date getHeartbeatHA() {
		return heartbeatHA;
	}

	private void executeCommandHA(String idRele, String payload) {
		Rele rele;
		try {
			rele = relesQuerySrv.getRele(idRele);
		} catch (Exception e) {
			log.error(e.getMessage());
			return;
		}
		StateRele state = rele.getCopyStateRele();

		if(state.isOn()) {
			if("ON".equalsIgnoreCase(payload)) {
			}
			else if("OFF".equalsIgnoreCase(payload)) {
				relesSrv.setStateHA(state, false);					
			}
		}
		else {
			if("ON".equalsIgnoreCase(payload)) {
				relesSrv.setStateHA(state, true);
			}
			else if("OFF".equalsIgnoreCase(payload)) {
			}
		}
	}
	
	private void executeCommandAutoHA(String idRele, String payload) {
		Rele rele;
		try {
			rele = relesQuerySrv.getRele(idRele);
		} catch (Exception e) {
			log.error(e.getMessage());
			return;
		}
		StateRele state = rele.getCopyStateRele();

		if(state.getMode().equals(StateRele.ModeRele.AUTO)) {
			if("ON".equalsIgnoreCase(payload)) {
			}
			else if("OFF".equalsIgnoreCase(payload)) {
				relesSrv.setModeManual(rele);					
			}
		}
		else {
			if("ON".equalsIgnoreCase(payload)) {
				relesSrv.setModeAuto(rele);
			}
			else if("OFF".equalsIgnoreCase(payload)) {
			}
		}
	}
	
	private void executeUpdateHeartbeat(String payload) {
		this.heartbeatHA = new Date();
	}
}
