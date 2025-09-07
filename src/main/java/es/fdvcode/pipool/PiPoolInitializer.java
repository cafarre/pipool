package es.fdvcode.pipool;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import es.fdvcode.pipool.srv.rele.RelesSrv;
import es.fdvcode.pipool.srv.scheduler.SchedulerSrv;

/**
 * 
 * @author cfarrema
 *
 */
@Component
public final class PiPoolInitializer {

	private final Logger log = LoggerFactory.getLogger(this.getClass());
	
	@Autowired
	private RelesSrv relesSrv;
	
	@Autowired 
	PiPoolContext ctx;

	@Autowired
	private SchedulerSrv plannerSrv;
	
	/**
	 * init
	 */
	public void init() {
		
		log.info("Inicialització de PiPool.");
		
		relesSrv.initGpios();
		plannerSrv.startActiveSchedulers();
	}
}
