package es.fdvcode.pipool;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.info.BuildProperties;
import org.springframework.stereotype.Component;

@Component
public class AppVersionLogger implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(AppVersionLogger.class);

    private final BuildProperties buildProperties;

    public AppVersionLogger(BuildProperties buildProperties) {
        this.buildProperties = buildProperties;
    }

    @Override
    public void run(String... args) throws Exception {
    	String msg = """
    			\n
    			*****************************************************
    			*                                                   *
    			* Starting application '{}' version: {}  *
    			*                                                   *
    			*****************************************************
    			""";
        logger.info(msg, buildProperties.getName(), buildProperties.getVersion());
    }
}