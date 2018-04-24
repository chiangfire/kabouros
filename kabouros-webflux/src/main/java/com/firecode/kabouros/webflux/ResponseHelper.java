package com.firecode.kabouros.webflux;

import org.springframework.http.HttpStatus;

import io.netty.util.CharsetUtil;

public class ResponseHelper {
	
	public static final String STATUS_STR = "\"status\":";
	public static final String CODE_STR = "\"code\":";
	public static final String MSG_STR = "\"msg\":";
	public static final String RESULT_STR = "\"result\":";
	public static final String BRACES_RIGHT_STR = "}";
	public static final String QUOTATION_MARK = "\"";
	public static final byte[] BRACES_RIGHT_BYTES = BRACES_RIGHT_STR.getBytes(CharsetUtil.UTF_8);
	public static final byte[] SUCCESS_BYTES = getResultStr(HttpStatus.OK.value(),"").getBytes(CharsetUtil.UTF_8);
	public static final String SUCCESS_STR = getResultStr(HttpStatus.OK.value(),"");
	
	public static final byte[] getResultBytes(HttpStatus status){
		
	    return getResultStr(status.value(), status.name()).getBytes(CharsetUtil.UTF_8);
	}
	
	public static final String getResultStr(HttpStatus status){
		
	    return getResultStr(status.value(), status.name());
	}
	
	public static final String getResultStr(CharSequence statusResult){
		
		return getResultStr(statusResult,QUOTATION_MARK);
	}
	
	public static final String getResultStr(CharSequence statusResult,String mark){
		
		return String.join("",SUCCESS_STR,mark,statusResult,mark,"}");
	}
	
	public static final String getResultStr(int statusCode,String statusMsg,CharSequence statusResult){
		
		return getResultStr(statusCode,statusMsg,statusResult,QUOTATION_MARK);
	}
	
	public static final String getResultStr(int statusCode,String statusMsg,CharSequence statusResult,String mark){
		
		return String.join("", getResultStr(statusCode,statusMsg),mark,statusResult,mark,"}");
	}
	
	public static final String getResultStr(int statusCode,String statusMsg){
		
        return String.join("","{",STATUS_STR,"{",CODE_STR,String.valueOf(statusCode),",",MSG_STR,"\"",statusMsg,"\"},",RESULT_STR);
	}

}
