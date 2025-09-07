package es.fdvcode.pipool;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.core.env.Profiles;

import com.pi4j.context.Context;

import es.fdvcode.pipool.srv.rele.GpioPinController;
import es.fdvcode.pipool.srv.rele.GpioPinControllerLocal;
import es.fdvcode.pipool.srv.rele.GpioPinControllerProd;

@Configuration
public class DIConfigGpioController {

	@Bean
	GpioPinController gpioController(Environment env, ObjectProvider<Context> pi4j) {
		if (env.acceptsProfiles(Profiles.of("Local"))) {
	    	return new GpioPinControllerLocal();
	    }
	    return new GpioPinControllerProd(pi4j.getObject());
	}	
}
