package com.firecode.kabouros.common.util;

import java.util.Arrays;
import java.util.List;
/**
 * @author jiang
 */
public final class ObjectUtil {
	
	public static List<?> objectToList(Object object){
		if(object instanceof List){
			
			return (List<?>)object;
		}
		if(object instanceof Object[]){
			
			return Arrays.asList((Object[])object);
		}
		return Arrays.asList(object);
	}

}
