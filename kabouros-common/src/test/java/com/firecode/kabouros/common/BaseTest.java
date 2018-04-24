package com.firecode.kabouros.common;

import java.util.function.Consumer;
/**
 * @author JIANG
 *
 */
public class BaseTest{
	
	
	public static final void p(Object o){
		
		System.err.println(o);
	}
	
	public static final void timeTest(Consumer<Object> consumer){
		long time = System.currentTimeMillis();
		consumer.accept(null);
		System.err.println(String.join("", "执行完毕耗时：",String.valueOf(System.currentTimeMillis() - time)));
	}
	

}
