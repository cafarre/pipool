package es.fdvcode.pipool.common;

public class Delay {

	public static void delay(int miliseconds)
	{
		try {
			Thread.sleep(miliseconds);
		} catch (Exception ex) {}
	}
}
