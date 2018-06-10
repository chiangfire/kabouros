package com.firecode.kabouros.jdbc.domain;

import java.util.Arrays;
import java.util.List;

import org.springframework.util.Assert;
/**
 * @author jiang
 */
public class ProperHelper {
	
	private final Export type;
	
	private final List<String> propertys;
	
	private final List<?> content;
	

	private ProperHelper(Object content,Export type, String... propertys) {
		Assert.notNull(content, "Content is null.");
		Assert.notNull(type, "Export type is empty.");
		Assert.notNull(propertys, "Propertys is empty.");
		this.type = type;
		this.propertys = Arrays.asList(propertys);
		if(content instanceof List){
			this.content = (List<?>)content;
		}else{
			this.content = Arrays.asList(content);
		}
	}
	
	//is effective
	public boolean isEffective(String properName){
		if(this.propertys.contains(properName)){
			return this.type.equals(Export.INCLUDE) ? true : false;
		}
		return this.type.equals(Export.INCLUDE) ? false : true;
	}

	public Export getType() {
		return type;
	}

	public List<String> getPropertys() {
		return propertys;
	}
	
	public List<?> getContent() {
		return content;
	}

	/**
	 * include propertys
	 * @param propertys
	 * @return
	 */
	public static final ProperHelper include(Object object,String... propertys){
		
		return new ProperHelper(object,Export.INCLUDE,propertys);
	}
	
	/**
	 * gnore propertys
	 * @param propertys
	 * @return
	 */
	public static final ProperHelper gnore(Object object,String... propertys){
		
		return new ProperHelper(object,Export.GNORE,propertys);
	}
	
}
