package es.fdvcode.pipool.srv.sonda.atlasi2c;

public class SondaAtlasErrorException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private byte responseCode;
	private boolean reponseCodeExpected=true;
	
	public SondaAtlasErrorException(byte responseCode, boolean codeExpected) {
		this.responseCode=responseCode;
		this.reponseCodeExpected = codeExpected;
	}

	public byte getResponseCode() {
		return responseCode;
	}

	public boolean isReponseCodeExpected() {
		return reponseCodeExpected;
	}
}
