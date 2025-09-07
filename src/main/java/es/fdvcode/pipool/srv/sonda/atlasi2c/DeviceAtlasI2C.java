package es.fdvcode.pipool.srv.sonda.atlasi2c;

public interface DeviceAtlasI2C {

	void initBus();
	
	boolean isBusInitated();
	
	void closeBus();
	
	String queryDevice(String command) throws SondaAtlasErrorException;

	void writeDevice(String command);

	String readDevice(int numBytes) throws SondaAtlasStillProcessingException, SondaAtlasErrorException;

	String readDevice() throws SondaAtlasStillProcessingException, SondaAtlasErrorException;

	int getAddress();

}