package com.firecode.kabouros.jdbc.starter.annotation;


import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.ResourceLoaderAware;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.type.AnnotationMetadata;

import com.firecode.kabouros.jdbc.starter.ClassPathRepositoryScanner;

/**
 * @author jiang
 */
public class EnableRepositoryScannerRegistrar implements ImportBeanDefinitionRegistrar, ResourceLoaderAware {

	private ResourceLoader resourceLoader;

	@Override
	public void registerBeanDefinitions(AnnotationMetadata importingClassMetadata, BeanDefinitionRegistry registry) {
		ClassPathRepositoryScanner scanner = new ClassPathRepositoryScanner();
		if(null != resourceLoader){
			scanner.setResourceLoader(resourceLoader);
		}
		scanner.register(importingClassMetadata, registry);
	}
	
	@Override
	public void setResourceLoader(ResourceLoader resourceLoader) {
		this.resourceLoader = resourceLoader;
	}

}
