package es.fdvcode.pipool;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.pi4j.Pi4J;
import com.pi4j.context.Context;

@Configuration
public class Pi4JConfig {

    /**
     * Define el contexto de Pi4J como un bean de Spring.
     * Spring se encargará de inicializar el contexto cuando la aplicación arranque
     * y de cerrarlo cuando la aplicación se detenga.
     *
     * @return El contexto de Pi4J.
     */
    @Bean
    Context pi4jContext() {
        return Pi4J.newAutoContext();
    }
}
