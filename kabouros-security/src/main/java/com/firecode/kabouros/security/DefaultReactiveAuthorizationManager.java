package com.firecode.kabouros.security;

import org.springframework.security.authorization.AuthorizationDecision;
import org.springframework.security.authorization.ReactiveAuthorizationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.server.authorization.AuthorizationContext;

import reactor.core.publisher.Mono;
/**
 * 权限验证
 * 以拦截 Request 请求的方式过滤权限。
 * 如果想以拦截 "函数" 的方式过滤权限，可使用 @EnableReactiveMethodSecurity 拦截所有函数。
 * 然后自定义 MethodSecurityMetadataSource <权限资源加载器> 和  MethodInterceptor <函数过滤器>。
 * 针对 Reactive API Spring Security 有默认实现结构如下：
 * 
 * DelegatingMethodSecurityMetadataSource extends AbstractMethodSecurityMetadataSource implements MethodSecurityMetadataSource{}
 * 
 * PrePostAdviceReactiveMethodInterceptor implements MethodInterceptor{
 *     ExpressionBasedPreInvocationAdvice   --> 函数执行之前处理
 *     ExpressionBasedPostInvocationAdvice  --> 函数执行之后处理
 * }
 * 详细配置可参考 ReactiveMethodSecurityConfiguration 配置类 securityMethodInterceptor（）函数。
 * 
 * 
 * @author JIANG
 *
 */
public class DefaultReactiveAuthorizationManager implements ReactiveAuthorizationManager<AuthorizationContext> {
	

	@Override
	public Mono<AuthorizationDecision> check(Mono<Authentication> authentication, AuthorizationContext context) {
		/**
		 * 是否登录
		 */
		return authentication
				.map(a -> new AuthorizationDecision(a.isAuthenticated()))
				.defaultIfEmpty(new AuthorizationDecision(false));
	}

}
