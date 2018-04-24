package com.firecode.kabouros.common;

import java.io.Serializable;

import com.firecode.kabouros.common.keygen.IPIdGenerator;
import com.firecode.kabouros.common.keygen.IdGenerator;
/**
 * @author JIANG
 */
public enum GenerationType implements IdGenerator{
	/**
	 * Twitter Snowflake 算法
	 */
	SNOWFLAKE(){
		@Override
		public Serializable generate() {
			
			return IPIdGenerator.getInstance().generate();
		}
	};
}
