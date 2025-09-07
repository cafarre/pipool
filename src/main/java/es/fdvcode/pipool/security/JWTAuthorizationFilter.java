package es.fdvcode.pipool.security;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.stream.Collectors;

import javax.crypto.SecretKey;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class JWTAuthorizationFilter extends OncePerRequestFilter {

	private Logger log = LogManager.getLogger(JWTAuthorizationFilter.class);
	
	private final String HEADER = "Authorization";
	private final String PREFIX = "Bearer ";
	
	@Value("${pipool.security.secret-jwt-key}")
	private String secretJwtKey;

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws ServletException, IOException {
		try {
			if (existeJWTToken(request, response)) {
				Claims claims = validateToken(request);
				if (claims.get("authorities") != null) {
					setUpSpringAuthentication(claims);
				} else {
					SecurityContextHolder.clearContext();
				}
			} else {
					SecurityContextHolder.clearContext();
			}
			chain.doFilter(request, response);
		} 
		catch (ExpiredJwtException e) {
			log.warn("Token Caducat. Validacio JWT NO superada del peticionari: {} amb token ({})", request.getRemoteAddr(), request.getHeader(HEADER));
			sendRestResponse(response, new ErrorResponse(HttpStatus.GONE, e.getMessage(), request.getRequestURI()));
		} 
		catch (UnsupportedJwtException | MalformedJwtException e) {
			log.warn("Validacio JWT NO superada del peticionari: {} amb token ({})", request.getRemoteAddr(), request.getHeader(HEADER));
			sendRestResponse(response, new ErrorResponse(HttpStatus.FORBIDDEN, e.getMessage(), request.getRequestURI()));
		}
	}	

	private Claims validateToken(HttpServletRequest request) {
		String jwtToken = request.getHeader(HEADER).replace(PREFIX, "");
		SecretKey key = Keys.hmacShaKeyFor(secretJwtKey.getBytes(StandardCharsets.UTF_8));
		return Jwts.parser()
		    .verifyWith(key)
		    .build()
		    .parseSignedClaims(jwtToken)
		    .getPayload();
	}

	/**
	 * Metodo para autenticarnos dentro del flujo de Spring
	 * 
	 * @param claims
	 */
	private void setUpSpringAuthentication(Claims claims) {
		@SuppressWarnings("unchecked")
		List<String> authorities = (List<String>) claims.get("authorities");

		UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(claims.getSubject(), null,
				authorities.stream().map(SimpleGrantedAuthority::new).collect(Collectors.toList()));
		SecurityContextHolder.getContext().setAuthentication(auth);

	}

	private boolean existeJWTToken(HttpServletRequest request, HttpServletResponse res) {
		String authenticationHeader = request.getHeader(HEADER);
		if (authenticationHeader == null || !authenticationHeader.startsWith(PREFIX)) {
			return false;
		}
		return true;
	}

	private void sendRestResponse(HttpServletResponse response, ErrorResponse errorResponse) throws IOException {
	    String serialized = new ObjectMapper().writeValueAsString(errorResponse);
	    
	    byte[] responseToSend = serialized.getBytes();;
        response.setHeader("Content-Type", "application/json");
        response.setStatus(errorResponse.getStatus());
        response.getOutputStream().write(responseToSend);
	}
}