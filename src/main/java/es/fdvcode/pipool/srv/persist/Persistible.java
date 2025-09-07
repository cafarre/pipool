package es.fdvcode.pipool.srv.persist;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.SimpleDateFormat;
import java.util.Date;

public interface Persistible {

	static SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy-HH:mm:ss");
	
	Date getTimestamp();
	String marshall();
	void unmarshall(String str);

	default public String getValue(String[] parts, int pos) {
		if(pos >= parts.length) {
			return null;
		}
		
		if("null".equals(parts[pos])) {
			return null;
		}
		
		return parts[pos];
	}	
	
	default public double getDoubleRounded(double dbl) {
		BigDecimal bd = new BigDecimal(dbl);
		bd = bd.setScale(2, RoundingMode.HALF_UP);
		return bd.doubleValue();
	}
}