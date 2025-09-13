package es.fdvcode.pipool.common;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.helpers.FormattingTuple;
import org.slf4j.helpers.MessageFormatter;
import org.springframework.stereotype.Component;

import lombok.AllArgsConstructor;
import lombok.Data;

@Component
public class SystemCommand {

	protected final Logger log = LoggerFactory.getLogger(this.getClass());
	
	public SystemResult executeCommandQuery(String ... command) throws IOException, InterruptedException {
		Process process = Runtime.getRuntime().exec(command);
	    try {
			return handleProcess(process);
		} catch (Exception e) {
			FormattingTuple msg = MessageFormatter.format("Error al executar commanda [{}] -> {}.", command, e.getMessage());
			throw new RuntimeException(msg.getMessage());
		} 	    
	}
	
	public SystemResult runScript(String script) throws IOException, InterruptedException {
	    ProcessBuilder processBuilder = new ProcessBuilder(script); //script="./nameOfScript.sh"
	    processBuilder.inheritIO();
	    processBuilder.redirectErrorStream(true); // Fusiona stdout y stderr
	    Process process = processBuilder.start();
	    
	    try {
			return handleProcess(process);
		} catch (Exception e) {
			FormattingTuple msg = MessageFormatter.format("Error al executar script [{}] {}.", script, e.getMessage());
			throw new RuntimeException(msg.getMessage());
		} 
	}	
	
	private SystemResult handleProcess(Process process) throws IOException, InterruptedException {
		BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
		String line;
	    StringBuilder output = new StringBuilder();
		while ((line = reader.readLine())!= null) {
			output.append(line).append("\n");
		}
		
		int exitValue = process.waitFor();
		
	    if (exitValue != 0) {
	    	FormattingTuple msg = MessageFormatter.arrayFormat("ExitValue: {} -> {}.", new Object[] {exitValue, output});
	        throw new RuntimeException(msg.getMessage());
	    }
	    else {
			return new SystemResult(output.toString(), exitValue);
	    }
	}
	
	@Data
	@AllArgsConstructor
	public class SystemResult {
		private String out;
		private int exitValue;
	}
	
}
