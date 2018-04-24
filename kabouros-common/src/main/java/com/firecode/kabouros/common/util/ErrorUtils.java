package com.firecode.kabouros.common.util;

import java.io.PrintWriter;
import java.io.StringWriter;
/**
 * @author JIANG
 */
public class ErrorUtils {
	
	/**
	 * 获取错误信息
	 * @param error   异常
	 * @return        错误详细信息
	 */
	public static final String getPrintStackTrace(Throwable error){
		StringWriter stackTrace = new StringWriter();
		error.printStackTrace(new PrintWriter(stackTrace));
		stackTrace.flush();
		return stackTrace.toString();
	}

}
