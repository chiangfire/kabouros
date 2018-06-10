package com.firecode.kabouros.jdbc;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
/**
 * @author jiang
 */
public class RepositoryFactoryInvocationHandler implements InvocationHandler{
	
	private final RepositoryProxyManager proxyRepositoryManager;

	public RepositoryFactoryInvocationHandler(RepositoryProxyManager prm) {
		this.proxyRepositoryManager = prm;
	}

	@Override
	public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
		try {
			if ("equals".equals(method.getName())) {
				
				return (proxy == args[0]);
			}
			if ("hashCode".equals(method.getName())) {
				
				return System.identityHashCode(proxy);
			}
			if("toString".equals(method.getName())){
				
				return proxyRepositoryManager.toString();
			}
			return this.proxyRepositoryManager.invokeProxyMethod(method, args);
		}catch (InvocationTargetException ex) {
			
			throw ex.getTargetException();
		}
	}

}
