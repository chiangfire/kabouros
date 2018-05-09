package com.firecode.kabouros.security;

import org.springframework.security.web.server.ServerAuthenticationEntryPoint;
import org.springframework.security.web.server.authentication.ServerAuthenticationFailureHandler;
import org.springframework.security.web.server.authentication.ServerAuthenticationSuccessHandler;
/**
 * commence() 认证
 * onAuthenticationFailure() 验证失败处理
 * onAuthenticationSuccess() 验证成功处理
 * 
 * @author JIANG
 */
public interface SecurityAuthenticationHandler extends ServerAuthenticationFailureHandler,
                                                       ServerAuthenticationSuccessHandler,
													   ServerAuthenticationEntryPoint {
	
}
