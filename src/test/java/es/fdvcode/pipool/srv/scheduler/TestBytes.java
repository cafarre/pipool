package es.fdvcode.pipool.srv.scheduler;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class TestBytes {

	private final Logger log = LoggerFactory.getLogger(TestBytes.class);
	private static final byte END_OF_MESSAGE = (byte) 0x00;

    @Test
    void testBytes() {

		String command = "i";
		String cmdenviar = command;
		cmdenviar += Byte.toString(END_OF_MESSAGE);
		
		log.info("DEBUG - Comanda a enviar:{} bytes:{}.", cmdenviar, cmdenviar.getBytes());
		
		byte[] bytes = command.getBytes();
		List<Byte> listBytes = new ArrayList<>();
		for(byte b : bytes) {
			listBytes.add(b);
		}
		listBytes.add(END_OF_MESSAGE);
				
		log.info("DEBUG - Comanda a enviar bytes:{}.", listBytes);
	}

    @Test
    void testBytes2() {
		
		String command = "i";
		byte[] bytes = command.getBytes();
		
		byte[] bytes2 = new byte[bytes.length+1];
		System.arraycopy(bytes, 0, bytes2, 0, bytes.length);
		bytes2[bytes.length] = END_OF_MESSAGE;
		
		log.info("DEBUG - Comanda a enviar bytes:{}.", bytes2);
		
		StringBuilder sb = new StringBuilder();
		for(byte b : bytes2) {
			if(b!=END_OF_MESSAGE) {
				sb.append(b);
			}
			else {
				break;
			}
		}
		
		log.info("DEBUG - Comanda a enviar bytes:{}.", sb.toString());
	}

}
