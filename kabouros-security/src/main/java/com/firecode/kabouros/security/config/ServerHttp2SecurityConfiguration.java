package com.firecode.kabouros.security.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;
import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.security.authentication.UserDetailsRepositoryReactiveAuthenticationManager;
import org.springframework.security.core.userdetails.ReactiveUserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import com.firecode.kabouros.security.support.ServerHttp2Security;
import static com.firecode.kabouros.security.support.ServerHttp2Security.http;

/**
 * ServerHttpSecurityConfiguration
 */
@Configuration
public class ServerHttp2SecurityConfiguration{
	
	@Autowired(required = false)
	private ReactiveAuthenticationManager authenticationManager;
	
	@Autowired(required = false)
	private ReactiveUserDetailsService reactiveUserDetailsService;
	
	@Autowired(required = false)
	private PasswordEncoder passwordEncoder;
	
	@Bean("com.benefire.antplatform.gateway.security.support.ServerIntensifyHttpSecurityConfiguration")
	@Scope("prototype")
	public ServerHttp2Security httpSecurity() {
		return http()
			.authenticationManager(authenticationManager())
			.headers().and()
			.logout().and();
	}
	
	private ReactiveAuthenticationManager authenticationManager() {
		if(this.authenticationManager != null) {
			return this.authenticationManager;
		}
		if(this.reactiveUserDetailsService != null) {
			UserDetailsRepositoryReactiveAuthenticationManager manager =
				new UserDetailsRepositoryReactiveAuthenticationManager(this.reactiveUserDetailsService);
			if(this.passwordEncoder != null) {
				manager.setPasswordEncoder(this.passwordEncoder);
			}
			return manager;
		}
		return null;
	}

}
