package es.fdvcode.pipool.common;

public class Nvl {

	public static String nvl(Object obj, String nulval) {
		if(obj!=null) {
			return obj.toString();
		}
		else {
			return nulval;
		}
	}
	
	public static Integer nvl(Integer num, Integer nulval) {
		if(num!=null) {
			return num;
		}
		else {
			return nulval;
		}
	}
	
	public static Double nvl(Double num, Double nulval) {
		if(num!=null) {
			return num;
		}
		else {
			return nulval;
		}
	}	

	public static String nvl(String num, Double nulval) {
		if(num!=null) {
			return num;
		}
		else {
			return String.valueOf(nulval);
		}
	}	

}

