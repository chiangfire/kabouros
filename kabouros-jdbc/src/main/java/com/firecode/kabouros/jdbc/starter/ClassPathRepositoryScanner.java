package com.firecode.kabouros.jdbc.starter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.BeanDefinitionStoreException;
import org.springframework.beans.factory.config.RuntimeBeanReference;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.core.annotation.AnnotationAttributes;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternUtils;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.core.type.classreading.CachingMetadataReaderFactory;
import org.springframework.core.type.classreading.MetadataReader;
import org.springframework.core.type.classreading.MetadataReaderFactory;
import org.springframework.util.ClassUtils;
import org.springframework.util.StringUtils;

import com.firecode.kabouros.jdbc.annotation.EnableRepository;
import com.firecode.kabouros.jdbc.starter.annotation.EnableRepositoryScan;
/**
 * @author jiang
 * custom Class<?> scanner
 * spring Class<?> scanner <ClassPathScanningCandidateComponentProvider>
 */
public class ClassPathRepositoryScanner {
	
	private static final Log LOG = LogFactory.getLog(ClassPathRepositoryScanner.class);
	
	private static final String RESOURCE_PATTERN = "/**/*.class";

	private ResourceLoader resourceLoader;
	
	private ResourcePatternResolver resourcePatternResolver;
	
	private MetadataReaderFactory metadataReaderFactory;
	
	
	public void register(AnnotationMetadata importingClassMetadata, BeanDefinitionRegistry registry) {
		AnnotationAttributes annoAttrs = AnnotationAttributes.fromMap(
				importingClassMetadata.getAnnotationAttributes(EnableRepositoryScan.class.getName()));
		List<String> basePackages = new ArrayList<String>(5);
		for (String pkg : annoAttrs.getStringArray("basePackages")) {
			if (StringUtils.hasText(pkg)) {
				basePackages.add(pkg);
			}
		}
		if(basePackages.isEmpty()){
			StackTraceElement[] stacks = Thread.currentThread().getStackTrace();
			StackTraceElement stackTraceElement = stacks[stacks.length-1];
			String applicationClassName = stackTraceElement.getClassName();
			int index = applicationClassName.indexOf(".",applicationClassName.indexOf(".") + 1);
			basePackages.add(applicationClassName.substring(0,index));
		}
		
		String repositoryProxyFactory = annoAttrs.getString("repositoryProxyFactoryRef");
		if(null != resourceLoader){
			this.resourcePatternResolver = ResourcePatternUtils.getResourcePatternResolver(resourceLoader);
			this.metadataReaderFactory = new CachingMetadataReaderFactory(resourceLoader/*this.resourcePatternResolver*/);
		}else{
			this.resourcePatternResolver = new PathMatchingResourcePatternResolver();
			this.metadataReaderFactory = new CachingMetadataReaderFactory(this.resourcePatternResolver);
		}
		Set<Class<?>> doScan = this.doScan(basePackages.toArray(new String[basePackages.size()]));
		if(!doScan.isEmpty()){
			for(Iterator<Class<?>> iterator = doScan.iterator();iterator.hasNext();){
				Class<?> clazz = iterator.next();
           	    AbstractBeanDefinition beanDefinition = BeanDefinitionBuilder.genericBeanDefinition(clazz).getBeanDefinition();
				//RootBeanDefinition beanDefinition = new RootBeanDefinition();
				beanDefinition.setAutowireMode(AbstractBeanDefinition.AUTOWIRE_BY_TYPE);
           	    String simpleNameString = beanDefinition.getBeanClassName();
           	    if(simpleNameString.contains(".")){
           		    simpleNameString = simpleNameString.substring(simpleNameString.lastIndexOf(".")+1);
           		    String first = String.valueOf(simpleNameString.charAt(0));
           		    simpleNameString = simpleNameString.replaceFirst(first, first.toLowerCase());
           	    }
           	    //beanDefinition.getConstructorArgumentValues().addGenericArgumentValue(beanDefinition.getBeanClassName());
           	    beanDefinition.setBeanClass(EnableRepositoryFactoryBean.class);
           	    if(!StringUtils.isEmpty(repositoryProxyFactory)){
           	    	beanDefinition.getPropertyValues().add(EnableRepositoryFactoryBean.REPOSITORY_PROCESSOR_NAME, new RuntimeBeanReference(repositoryProxyFactory));
           	    }
           	    beanDefinition.getPropertyValues().addPropertyValue(EnableRepositoryFactoryBean.REPOSITORY_PROPERTY_NAME, clazz);
           	    beanDefinition.setLazyInit(true);
           	    //String generateBeanName = BeanDefinitionReaderUtils.generateBeanName(beanDefinition, registry);
           	    LOG.info(String.join("", "Bean ","'",clazz.getName(),"'"));
           	    //register
           	    if(!registry.containsBeanDefinition(simpleNameString)){
           	    	registry.registerBeanDefinition(simpleNameString,beanDefinition);
           	    }
			}
		}
	}
	
	private Set<Class<?>> doScan(String... basePackages){
		Set<Class<?>> candidates = new LinkedHashSet<Class<?>>();
		for(int i=0,length=basePackages.length;i<length;i++){
			String basePackage = basePackages[i];
			if(StringUtils.hasText(basePackage)){
	            String pattern = ResourcePatternResolver.CLASSPATH_ALL_URL_PREFIX + ClassUtils.convertClassNameToResourcePath(basePackage) + RESOURCE_PATTERN;
				try {
					Resource[] resources = this.resourcePatternResolver.getResources(pattern);
					 for (Resource resource : resources) {
		                 if (resource.isReadable()) {
		                     MetadataReader reader = metadataReaderFactory.getMetadataReader(resource);
		                     if(reader.getAnnotationMetadata().hasAnnotation(EnableRepository.class.getName())){
			                     try {
			                    	 Class<?> repositoryClass = Thread.currentThread().getContextClassLoader().loadClass(reader.getClassMetadata().getClassName());
			                    	 if(!repositoryClass.isInterface()) throw new IllegalArgumentException(String.join(" ",repositoryClass.getName(),"is not interface."));
									 candidates.add(repositoryClass);
								} catch (ClassNotFoundException e) {
									throw new BeanDefinitionStoreException("Failed to read candidate component class.",e);
								}
		                     }
		                 }
					 }
				} catch (IOException e) {
					throw new BeanDefinitionStoreException("I/O failure during classpath scanning.", e);
				}
			}
		}
		return candidates;
	}

	public void setResourceLoader(ResourceLoader resourceLoader) {
		this.resourceLoader = resourceLoader;
	}
}
