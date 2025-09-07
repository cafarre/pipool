package es.fdvcode.pipool.restsrv.v1;

import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import javax.crypto.SecretKey;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import es.fdvcode.pipool.restsrv.v1.response.RestResponse;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

/**
 * 
 * @author cfarrema
 *
 */

@RestController
@RequestMapping(LoginRestController.URIBASE)
public class LoginRestController {

	protected final Logger log;
	public static final String URIBASE = "api/v1/login";

	@Value("${pipool.security.secret-jwt-key}")
	private String secretJwtKey;
	@Value("${pipool.security.jwt-seconds-expiration}")
	private int jwtSecondsExpiration;

	@Value("${spring.security.user.name}")
	private String username;

	
	/**
	 * default
	 */
	public LoginRestController() {
		this.log = LoggerFactory.getLogger(this.getClass());
	}


	/**
	 * 
	 * @param gpioPinNumber
	 * @return
	 */
	@GetMapping("/token")
	public RestResponse<String> token() {

		log.info("Login get Token");
		
		String jwt = getJWTToken(username);
	    log.info("Login OK -> Token JWT: " + jwt);
		
		return new RestResponse<>(jwt, HttpStatus.OK);        
	}
	
	private String getJWTToken(String username) {
		List<GrantedAuthority> grantedAuthorities = AuthorityUtils.commaSeparatedStringToAuthorityList("ROLE_USER");
		
		SecretKey key = Keys.hmacShaKeyFor(secretJwtKey.getBytes(StandardCharsets.UTF_8));
		String token = Jwts
				.builder()
				.id("PIPOOLJWT")
				.subject(username)
				.claim("authorities",
						grantedAuthorities.stream()
								.map(GrantedAuthority::getAuthority)
								.collect(Collectors.toList()))
				.issuedAt(new Date(System.currentTimeMillis()))
				.expiration(new Date(System.currentTimeMillis() + jwtSecondsExpiration*1000))
				.signWith(key ).compact();

		return "Bearer " + token;
	}		
	
	
}
