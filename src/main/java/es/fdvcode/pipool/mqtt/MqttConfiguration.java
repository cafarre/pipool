package es.fdvcode.pipool.mqtt;

import org.aopalliance.aop.Advice;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.integration.channel.DirectChannel;
import org.springframework.integration.core.MessageProducer;
import org.springframework.integration.mqtt.core.DefaultMqttPahoClientFactory;
import org.springframework.integration.mqtt.core.MqttPahoClientFactory;
import org.springframework.integration.mqtt.inbound.MqttPahoMessageDrivenChannelAdapter;
import org.springframework.integration.mqtt.outbound.MqttPahoMessageHandler;
import org.springframework.integration.mqtt.support.DefaultPahoMessageConverter;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessageHandler;
import org.springframework.messaging.MessageHeaders;
import org.springframework.messaging.MessagingException;
import org.springframework.retry.interceptor.RetryInterceptorBuilder;

import es.fdvcode.pipool.mqtt.homeassistant.HomeAssistantMqttSubscriber;
import es.fdvcode.pipool.mqtt.shelly.ShellyMqttSubscriber;

@Configuration
public class MqttConfiguration {

	private final Logger log = LoggerFactory.getLogger(MqttConfiguration.class);
	
	private final String MQTT_CLIENTID_PUB = "pipoolMqtt-ClientPub";
	private final String MQTT_CLIENTID_SUB = "pipoolMqtt-ClientSub";
	
	@Value("${pipool.mqtt.urlbroker}")
	private String urlMqttBroker;
	
	@Value("${pipool.mqtt.topics-prefix.shelly}")
	private String topicPrefixShelly;
	
	@Value("${pipool.mqtt.topics-prefix.ha}")
	private String topicPrefixHA;

	@Value("${pipool.mqtt.user}")
	private String mqttUser;

	@Value("${pipool.mqtt.password}")
	private String mqttPassword;

	
    @Bean
    MessageChannel mqttInputChannel() {
        return new DirectChannel();
    }
    
    @Bean
    MessageProducer inbound(MqttPahoClientFactory factory) {
    	
    	//TODO!! Nomes acceptar els topics que m'interessen. Ara ho estem rebent TOT!!!???!!!
    	
    	String[] topics = {
			this.topicPrefixHA + "/switch/pipool_rele_bomba/#",
			this.topicPrefixHA + "/switch/pipool_rele_fan/#",
			this.topicPrefixHA + "/switch/pipool_rele_bomba_clor/#",
			this.topicPrefixHA + "/switch/pipool_rele_bomba_acid/#",
			this.topicPrefixHA + "/switch/pipool_rele_lfi/#",
			this.topicPrefixHA + "/light/pipool_rele_llums/#",
			this.topicPrefixHA + "/light/pipool_rele_llums_jardi/#",
			this.topicPrefixHA + "/switch/pipool_rele_bomba_auto/#",
			this.topicPrefixHA + "/switch/pipool_rele_fan_auto/#",
			this.topicPrefixHA + "/switch/pipool_rele_bomba_clor_auto/#",
			this.topicPrefixHA + "/switch/pipool_rele_bomba_acid_auto/#",
			this.topicPrefixHA + "/switch/pipool_rele_lfi_auto/#",
			this.topicPrefixHA + "/switch/pipool_rele_llums_auto/#",
			this.topicPrefixHA + "/switch/pipool_rele_llums_jardi_auto/#",
			this.topicPrefixHA + "/heartbeat"
			//this.topicPrefixShelly + "/#"
    	};
    	
    	MqttPahoMessageDrivenChannelAdapter adapter = new MqttPahoMessageDrivenChannelAdapter(MQTT_CLIENTID_SUB, factory, topics);
        adapter.setCompletionTimeout(5000);
        adapter.setConverter(new DefaultPahoMessageConverter());
        adapter.setQos(2);
        adapter.setOutputChannel(mqttInputChannel());
        
        log.info("Try MQTT Connection to '{}' for topics '{}'.", urlMqttBroker, topics);
        
        return adapter;
    }
    
    @Bean
    @ServiceActivator(inputChannel = "mqttInputChannel")
    MessageHandler handler(HomeAssistantMqttSubscriber subscriberHA, ShellyMqttSubscriber subscriberShelly) {
        return new MessageHandler() {

            @Override
            public void handleMessage(Message<?> message) throws MessagingException {
            	log.debug("MQTT MSG RECEIVED: {}" + message);
            	MessageHeaders headers = message.getHeaders();
            	String topic = (String) headers.getOrDefault("mqtt_receivedTopic", "");
            	
            	if(topic.startsWith(topicPrefixHA)) {
            		subscriberHA.handle(message.getPayload(), headers, topic);
            	}
            	else if(topic.startsWith(topicPrefixShelly)) {
            		subscriberShelly.handle(message.getPayload(), headers, topic);
            	}
            }

        };
    }
    
    @Bean
    MqttPahoClientFactory mqttClientFactory() {
        DefaultMqttPahoClientFactory factory = new DefaultMqttPahoClientFactory();
        MqttConnectOptions options = new MqttConnectOptions();
        
        options.setServerURIs(new String[] {"tcp://" + urlMqttBroker });
        options.setCleanSession(true);
        options.setUserName(mqttUser);
        options.setKeepAliveInterval(90);
        options.setPassword(mqttPassword.toCharArray());
        
        factory.setConnectionOptions(options);
        
        return factory;
    }

    @Bean
    Advice retryAdvice() {
        return RetryInterceptorBuilder.stateless()
                .maxAttempts(5) // Número máximo de intentos
                .backOffOptions(1000, 2.0, 10000) // 1s de espera inicial, factor de crecimiento de 2.0, máximo 10s
                .build();
    }

    @Bean
    @ServiceActivator(inputChannel = "mqttOutboundChannel", adviceChain = "retryAdvice")
    MessageHandler mqttOutbound(MqttPahoClientFactory mqttClientFactory) {
        MqttPahoMessageHandler messageHandler = new MqttPahoMessageHandler(MQTT_CLIENTID_PUB, mqttClientFactory);
        messageHandler.setAsync(true);
        messageHandler.setDefaultTopic(topicPrefixHA);
        return messageHandler;
    }
    
    @Bean
    MessageChannel mqttOutboundChannel() {
        return new DirectChannel();
    }

//    @MessagingGateway(defaultRequestChannel = "mqttOutboundChannel")
//    public interface MqttGateway {
//        void sendToMqtt(String data, @Header(MqttHeaders.TOPIC) String topic);
//    }
}
