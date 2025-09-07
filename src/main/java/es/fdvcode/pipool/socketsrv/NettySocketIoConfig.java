package es.fdvcode.pipool.socketsrv;

//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//import org.springframework.beans.factory.annotation.Value;
//import com.corundumstudio.socketio.HandshakeData;
//import com.corundumstudio.socketio.SocketIOServer;

//@Configuration
public class NettySocketIoConfig {

//	private final Logger log = LoggerFactory.getLogger(this.getClass());
//	
//	@Value("${socketio.host}")
//	private String host;
//
//	@Value("${socketio.port}")
//	private Integer port;
//
//    //@Bean
//    SocketIOServer socketIOServer() {
//    	com.corundumstudio.socketio.Configuration config = new com.corundumstudio.socketio.Configuration();
//        config.setHostname(host);
//        config.setPort(port);
//        SocketIOServer server = new SocketIOServer(config);
//        
//    	server.addConnectListener(client -> {
//            HandshakeData handshakeData = client.getHandshakeData();
//            log.info("Client[{}] - Connected to socket.io server through '{}'", client.getSessionId().toString(), handshakeData.getUrl());
//        });
//    	
//    	server.addDisconnectListener(client -> {
//            log.info("Client[{}] - Disconnected from socket.io server.", client.getSessionId().toString());
//        });
//    	server.startAsync();
//    	
////    	try {
////			Thread.sleep(2000);
////		} catch (InterruptedException e) {
////			log.warn(e.getMessage());
////		}
//    	
//    	return server;
//    }
}
