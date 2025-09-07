package es.fdvcode.pipool;

import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

/**
 * 
 * @author cfarrema
 *
 */
@Component
@RequiredArgsConstructor
public class ApplicationStartup implements ApplicationListener<ApplicationReadyEvent> {

	private final ApplicationContext ctx;
	
	@Override
	public void onApplicationEvent(final ApplicationReadyEvent event) {
		ctx.getBean(PiPoolInitializer.class).init();
		return;
  }
}