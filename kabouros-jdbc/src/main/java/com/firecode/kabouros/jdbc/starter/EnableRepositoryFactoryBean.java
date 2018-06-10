package com.firecode.kabouros.jdbc.starter;

import org.springframework.beans.factory.FactoryBean;

import com.firecode.kabouros.jdbc.RepositoryProxyFactory;
/**
 * @author jiang
 */
public class EnableRepositoryFactoryBean implements FactoryBean<Object>{
	
	public static final String REPOSITORY_PROPERTY_NAME = "repositoryClass";
	
	public static final String REPOSITORY_PROCESSOR_NAME = "enableRepositoryProcessor";
	
	private RepositoryProxyFactory repositoryProxyFactory;
	
	private Class<?> repositoryClass;

	@Override
	public Object getObject() throws Exception {
		
		return repositoryProxyFactory.createRepositoryProxy(repositoryClass);
	}

	@Override
	public Class<?> getObjectType() {
		
		return repositoryClass;
	}

	@Override
	public boolean isSingleton() {
		
		return true;
	}

	public void setRepositoryClass(Class<?> repositoryClass) {
		
		this.repositoryClass = repositoryClass;
	}

	public void setRepositoryProxyFactory(RepositoryProxyFactory repositoryProxyFactory) {
		this.repositoryProxyFactory = repositoryProxyFactory;
	}
	
}
