package es.fdvcode.pipool.srv.scheduler;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

class TestXor {

    @Test
    void testXor() {

		 boolean[] all = { false, true };
	    for (boolean a : all) {
	        for (boolean b: all) {
	            boolean c = !(a ^ b);
	            System.out.println(a + " ^ " + b + " = " + c);
	        }
	    }
	    
	    assertTrue(true);
	}
}
