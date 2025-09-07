package es.fdvcode.pipool.common;

public class Contains {

	public static boolean words(String str, String ... contains) {
		if(contains!=null && str!=null) {
			for(String contain : contains) {
				if(str.contains(contain)) {
					return true;	
				}
			}
		}
		return false;
	}	

}

