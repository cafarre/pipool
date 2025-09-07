package es.fdvcode.pipool.security;

import static org.springframework.security.config.Customizer.withDefaults;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;


/*
 * REQUES:
 * 	- /api/ping -> open
 *  - /api/login -> open
 *  - all -> token jwt
 */

@Configuration
@EnableWebSecurity
public class ApplicationSecurity {
	
	@Autowired
    private JWTAuthorizationFilter jwtAuthorizationFilter;

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        // Desactivamos CSRF ya que es una API REST sin estado
        http.csrf(csrf -> csrf.disable());

        // Configuramos la política de sesión sin estado para evitar sesiones en el servidor
        http.sessionManagement(management -> management.sessionCreationPolicy(SessionCreationPolicy.STATELESS));

        // Añadimos el filtro JWT después del filtro de autenticación de nombre de usuario/contraseña
        http.addFilterAfter(jwtAuthorizationFilter, UsernamePasswordAuthenticationFilter.class);

        // Configuramos las reglas de autorización para las peticiones HTTP
        http.authorizeHttpRequests(authorize -> authorize
                // La ruta de ping no requiere autenticación
                .requestMatchers("/api/v1/rpistate/ping").permitAll()
                // La ruta de login requiere autenticación HTTP básica
                .requestMatchers("/api/v1/login/token").authenticated()
                // Todas las demás peticiones también requieren autenticación
                .anyRequest().authenticated()
        );

        // Configuramos la autenticación HTTP básica para la ruta de login
        http.httpBasic(withDefaults());

        return http.build();
    }
	
//	@Autowired
//	private ApplicationContext applicationContext;
//	
//    @Bean
//    SecurityFilterChain basicAuthWebSecurityConfigurationSecurityFilterChain(HttpSecurity http) throws Exception {
//        http.sessionManagement(management -> management.sessionCreationPolicy(SessionCreationPolicy.STATELESS));
//
//        http.csrf(csrf -> csrf.disable())
//            .requestMatchers(matchers -> matchers
//                    .antMatchers("/api/v1/login/token"))
//            .authorizeRequests(authorize -> authorize
//                            .anyRequest().authenticated()
//            )
//            .httpBasic(withDefaults());
//        return http.build();
//    }
//    	
//
//    @Bean
//    SecurityFilterChain jwtSecurityFilterChain(HttpSecurity http) throws Exception {
//        http.sessionManagement(management -> management.sessionCreationPolicy(SessionCreationPolicy.STATELESS));
//        JWTAuthorizationFilter jwtaf = applicationContext.getBean(JWTAuthorizationFilter.class);
//
//        http.csrf(csrf -> csrf.disable())
//            .addFilterAfter(jwtaf, UsernamePasswordAuthenticationFilter.class)
//            .authorizeRequests(requests -> requests.antMatchers("/api/v1/rpistate/ping")
//                    .permitAll()
//                    .anyRequest().authenticated());
//        return http.build();
//    }    
}

