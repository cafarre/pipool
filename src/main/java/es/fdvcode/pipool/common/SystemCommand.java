package es.fdvcode.pipool.common;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.helpers.FormattingTuple;
import org.slf4j.helpers.MessageFormatter;
import org.springframework.stereotype.Component;

@Component
public class SystemCommand {

	protected final Logger log = LoggerFactory.getLogger(this.getClass());
	
	public String executeCommandQuery(String[] command) throws IOException, InterruptedException {

		StringBuilder output = new StringBuilder();

		Process process = Runtime.getRuntime().exec(command);
		int exitValue = process.waitFor();
	    if (exitValue != 0) {
			BufferedReader reader = new BufferedReader(new InputStreamReader(process.getErrorStream()));
			String line;
			while ((line = reader.readLine())!= null) {
				output.append(line).append("\n");
			}
			FormattingTuple msg = MessageFormatter.format("Error al executar commanda [{}] -> {}.", command, output);
	        throw new RuntimeException(msg.getMessage());
	    }
	    else {
			BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
			String line;
			while ((line = reader.readLine())!= null) {
				output.append(line).append("\n");
			}
	    
			return output.toString();
	    }
	}
	
	public String runScript(String script) throws IOException, InterruptedException {
	    ProcessBuilder processBuilder = new ProcessBuilder(script); //script="./nameOfScript.sh"
	    processBuilder.inheritIO();
	    Process process = processBuilder.start();

	    int exitValue = process.waitFor();
	    StringBuilder output = new StringBuilder();
	    if (exitValue != 0) {
			BufferedReader reader = new BufferedReader(new InputStreamReader(process.getErrorStream()));
			String line;
			while ((line = reader.readLine())!= null) {
				output.append(line).append("\n");
			}
			
			FormattingTuple msg = MessageFormatter.format("Error al executar script [{}] -> {}.", script, output);
	        throw new RuntimeException(msg.getMessage());
	    }
	    else {
			BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
			String line;
			while ((line = reader.readLine())!= null) {
				output.append(line).append("\n");
			}
	    
			return output.toString();
	    }
	}	
	
}
