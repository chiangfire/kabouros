package com.firecode.kabouros.common.util;

import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.List;

/**
 * @author jiang
 */
public final class ClassUtil {
	
	
	
	public static final boolean isBoolean(Class<?> clazz){
		
		return (Boolean.class.isAssignableFrom(clazz) || Boolean.class.getSimpleName().toLowerCase().equals(clazz.getName()));
	}
	
	
	public static Class<?> findGenericReturnType(Method method,boolean isNotNull) {
		Type genericType = method.getGenericReturnType();
		if(genericType instanceof ParameterizedType){
			Type type = ((ParameterizedType)genericType).getActualTypeArguments()[0];
		    return typeToClass(type);
		}
		if(isNotNull){
			throw new IllegalArgumentException(String.join("", 
			method.getDeclaringClass().getName(), ".",method.getName()," return type not generic type."));
		}
		return null;
	}
	
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public static Class<?> findCertainGenericType(Method method,Class annotationClass,boolean isNotNull){
		Class<?> returnType = method.getReturnType();
		if(null != returnType.getAnnotation(annotationClass)){
			
			return returnType;
		}
		Class<?> genericType = findGenericReturnType(method,false);
		if(List.class.isAssignableFrom(returnType) && null != genericType.getAnnotation(annotationClass)){
			
			return genericType;
		}
		Class<?> interfaceGeneric = findInterfaceGeneric(method.getDeclaringClass());
		if(null != interfaceGeneric && null != interfaceGeneric.getAnnotation(annotationClass)){
			
			return interfaceGeneric;
		}
		if(isNotNull) {
			throw new IllegalArgumentException(String.join("",method.getDeclaringClass().getName(),
					" and ",method.getName(),"() return type,"," There is no mappings of the ",annotationClass.getSimpleName()));
		}
		return null;
	}
	
	public static Class<?> findInterfaceGeneric(Class<?> declaringClass){
    	Type genericSuperclass = declaringClass.getGenericSuperclass();
    	if(null != genericSuperclass && genericSuperclass instanceof ParameterizedType){
    		ParameterizedType type = (ParameterizedType)genericSuperclass;
    		Type[] actualTypeArguments = type.getActualTypeArguments();
    		if(actualTypeArguments.length != 0){
    		    return typeToClass(actualTypeArguments[0]);
    		}
    	}else{
    		Type[] genericInterfaces = declaringClass.getGenericInterfaces();
        	if(null != genericInterfaces && genericInterfaces.length != 0){
        		if(genericInterfaces[0] instanceof ParameterizedType){
        			ParameterizedType type = (ParameterizedType)genericInterfaces[0];
        			Type[] actualTypeArguments = type.getActualTypeArguments();
            		if(actualTypeArguments.length != 0){
            			return typeToClass(actualTypeArguments[0]);
            		}
        		}
        	}
    	}
    	return null;
	}
	
	public static Class<?> typeToClass(Type genericType){
		try {
			return Class.forName(genericType.getTypeName());
		} catch (ClassNotFoundException e) {
			throw new IllegalArgumentException(String.join(" ", "deneric error",genericType.getTypeName()),e);
		}
	}
	
}
