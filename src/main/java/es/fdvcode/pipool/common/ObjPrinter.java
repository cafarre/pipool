package es.fdvcode.pipool.common;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * @author cfarrema
 *
 */
public class ObjPrinter {
	
	private static final String PACKAGE = "es.fdvcode.pipool";
	
	private boolean multiline=false;
	private Map<Object, Object> mapInProces = new HashMap<>();
	private final Logger log = LoggerFactory.getLogger(this.getClass());
	
	public ObjPrinter(boolean multiline) {
		this.multiline=multiline;
	}
	
	/**
	 * 
	 * @param obj
	 * @return
	 */
	public static String printObj(Object obj) {
		ObjPrinter pr = new ObjPrinter(false);
		return pr.print(obj);
	}

	public static String printObj(Object obj, boolean multiline) {
		ObjPrinter pr = new ObjPrinter(multiline);
		return pr.print(obj);
	}
	
	/**
	 * 
	 * @param obj
	 * @return
	 */
	public String print(Object obj) {
	
		StringBuilder sb = new StringBuilder();
		if(obj!=null) {
       		
       		Class<?> cl = obj.getClass();
       		if (obj instanceof Iterable 
           			|| obj instanceof Map
           			|| cl.isArray())  {
       			
       			sb.append("{");
       			if(multiline) {
       				sb.append("\n");
       			}
       			
           		if(obj instanceof Map) {
               		int i=1;
               		for(Entry<?,?> item : ((Map<?,?>)obj).entrySet()) {
    	                if(i!=1){
    	                	if(multiline){
    	                		sb.append("\n");
    	                	}
    	                	else {
    	                		sb.append(", ");
    	                	}
    	                }
               			i++;

               			sb.append(item.getKey()).append(":").append(print(item.getValue()));
               		}
           		}
           		else {
               		int i=1;
               		for(Object item : (Iterable<?>) obj) {
    	                if(i!=1){
    	                	if(multiline){
    	                		sb.append("\n");
    	                	}
    	                	else {
    	                		sb.append(", ");
    	                	}
    	                }
               			i++;

               			sb.append(i).append(":").append(print(item));
               		}
           		}
           		
           		sb.append("}");
           		if(multiline) {
           			sb.append("\n");
           		}
           	}       		
           	else if(!mapInProces.containsKey(obj) &&
           			!cl.isPrimitive() &&
           			!cl.isEnum() &&
           			cl.getPackage().getName().contains(PACKAGE)) {
           		
           		sb.append("-->(").append(obj.getClass().getName()).append("): ");
           		if(multiline) {
           			sb.append("\n");
           		}
           		sb.append(this.trataObjeto(obj));
           		
            } 
           	else  {
           		sb.append(obj);
           	}
       	}
       	else 
       	{
       		sb.append("<null>");
       	}		
		
		return sb.toString();
	}

	private String trataObjeto(Object obj) {
		StringBuilder sb = new StringBuilder(); 
		mapInProces.put(obj, obj);
		try {
            Field[] fieldlist = obj.getClass().getDeclaredFields();
            sb.append("[");
            if(multiline) {
            	sb.append("\n");
            }
            for (int i = 0; i < fieldlist.length; i++) {
                Field fld = fieldlist[i];
                if (!(java.lang.reflect.Modifier.isStatic(fld.getModifiers()) &&
                		java.lang.reflect.Modifier.isFinal(fld.getModifiers()))) {
                
	                fld.setAccessible(true);
	                if(i!=0){
	                	if(multiline){
	                		sb.append("\n");
	                	}
	                	else {
	                		sb.append(", ");
	                	}
	                }
	                sb.append(fld.getName());
	                sb.append("=");
	                
	                Object objInObj = fld.get(obj);
	                sb.append(print(objInObj));
                }
            }
            sb.append("]");
            if(multiline) { 
            	sb.append("\n");
            }
            mapInProces.remove(obj, obj);
        } 
		catch (Exception e) {
			log.error(e.getMessage(), e);
        }
		
		return sb.toString();
	}
}
