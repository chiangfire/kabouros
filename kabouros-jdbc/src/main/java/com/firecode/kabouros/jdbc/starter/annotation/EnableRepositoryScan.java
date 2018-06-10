package com.firecode.kabouros.jdbc.starter.annotation;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import org.springframework.context.annotation.Import;


@Documented
@Target(TYPE)
@Retention(RUNTIME)
@Import(EnableRepositoryScannerRegistrar.class)
public @interface EnableRepositoryScan {
	/**
	 * repository packages
	 * @return
	 */
	String[] basePackages() default {};
	
	/**
	 * default rely on
	 * @return
	 */
	String repositoryProxyFactoryRef() default "";
	
}
