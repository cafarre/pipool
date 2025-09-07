package es.fdvcode.pipool.socketsrv;

//import com.corundumstudio.socketio.SocketIOServer;

//@Component
public class PipoolSocketIoServer {

//	private final Logger log = LoggerFactory.getLogger(this.getClass());
//	
//	@Value("${socketio.host}")
//	private String host;
//
//	@Value("${socketio.port}")
//	private Integer port;

//	@Autowired
//	private SocketIOServer server;
//	
//	@PostConstruct
//	public void init() {
//		server.addEventListener("test", String.class, (client, data, ackSender) -> {
//			log.info("Event test rebut.");
//		});
//	}
//	
//    public void emitBroadcast(String name) {
//    	this.emitBroadcast(name, null);
//    }
//    
//    public void emitBroadcast(String name, Object data) {
//    	server.getBroadcastOperations().sendEvent(name, data);
//    }


    
//	private SocketIoSocket socket;
//	
//    @PostConstruct
//    public SocketIoServer socketIOServer() {
//	  final ServerWrapper serverWrapper = new ServerWrapper(host, port, null); // null means "allow all" as stated in https://github.com/socketio/engine.io-server-java/blob/f8cd8fc96f5ee1a027d9b8d9748523e2f9a14d2a/engine.io-server/src/main/java/io/socket/engineio/server/EngineIoServerOptions.java#L26
//      try {
//          serverWrapper.startServer();
//      } catch (Exception e) {
//          e.printStackTrace();
//      }
//      SocketIoServer server = serverWrapper.getSocketIoServer();
//      SocketIoNamespace ns = server.namespace("/");
//      ns.on("connection", args -> {
//              SocketIoSocket socket = (SocketIoSocket) args[0];
//              System.out.println("Client " + socket.getId() + " (" + socket.getInitialHeaders().get("remote_addr") + ") has connected.");
//
//              socket.on("message", new Emitter.Listener() {
//                  @Override
//                  public void call(Object... args) {
//                      System.out.println("[Client " + socket.getId() + "] " + args);
//                      socket.send("message", "test message", 1);
//                  }
//              });
//              
//          });
//    }
//
//	public SocketIoSocket getSocket() {
//		return socket;
//	}
}
