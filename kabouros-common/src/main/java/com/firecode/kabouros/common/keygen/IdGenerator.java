package com.firecode.kabouros.common.keygen;

import java.io.Serializable;

/**
 * @author jiang
 */
public interface IdGenerator {
	
	/**
	 * 生成 Id
	 * @return 可序列化对象
	 */
	Serializable generate();

}
