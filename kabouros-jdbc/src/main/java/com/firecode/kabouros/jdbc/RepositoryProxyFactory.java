package com.firecode.kabouros.jdbc;
/**
 * @author jiang
 */
public interface RepositoryProxyFactory {
	
	public Object createRepositoryProxy(Class<?> clazz);

}
