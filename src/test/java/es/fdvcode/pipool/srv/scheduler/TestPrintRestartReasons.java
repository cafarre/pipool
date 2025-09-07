package es.fdvcode.pipool.srv.scheduler;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import es.fdvcode.pipool.common.ObjPrinter;
import es.fdvcode.pipool.srv.sonda.atlasi2c.impl.SondaAtlas.DeviceStatusResponse;
import es.fdvcode.pipool.srv.sonda.atlasi2c.impl.SondaAtlas.RestartReasons;

class TestPrintRestartReasons {

	private final Logger log = LoggerFactory.getLogger(TestPrintRestartReasons.class);

    @Test
    void test1() {
		DeviceStatusResponse obj = new DeviceStatusResponse(RestartReasons.POWERED_OFF, "1.10");
		log.info(ObjPrinter.printObj(obj));
	}

}
