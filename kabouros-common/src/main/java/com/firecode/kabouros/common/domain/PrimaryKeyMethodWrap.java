package com.firecode.kabouros.common.domain;

import java.lang.reflect.Method;

import com.firecode.kabouros.common.GenerationType;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class PrimaryKeyMethodWrap {
	
	private Method readMethod;
	
	private Method writeMethod;
	
	private GenerationType strategy;

}
