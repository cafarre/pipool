package es.fdvcode.pipool.restsrv.filters;

import java.io.IOException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.FilterConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;

@Component
@Order(1)
public class FilterLogging implements Filter {

	private final static Logger log = LogManager.getLogger(FilterLogging.class);

	@Override
	public void init(FilterConfig arg0) throws ServletException {
		log.info("Initializing filter :{}", this);
	}

	
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
    	HttpServletRequest req = (HttpServletRequest) request;
		
    	String authorization = req.getHeader("Authorization");
    	if(authorization !=null) {
    		authorization = authorization.subSequence(0, 10) + "...";
    	}
    	
		long startTime = System.currentTimeMillis();
		log.info("Logging START Request  {} : {} (Auth:{})", req.getMethod(), req.getRequestURI(), authorization);
		
		chain.doFilter(request, response);
		long duration = System.currentTimeMillis() - startTime;
		
		log.info("Logging END Response :{} / Time ellapsed :{}", req.getRequestURI(), duration);
    }

	@Override
	public void destroy() {
		log.warn("Destructing filter :{}", this);
	}

}