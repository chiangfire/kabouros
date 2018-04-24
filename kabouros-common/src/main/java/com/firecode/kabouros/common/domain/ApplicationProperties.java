package com.firecode.kabouros.common.domain;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class ApplicationProperties {
	/**
	 * 域名
	 */
	private String host;
	/**
	 * 首页地址
	 */
	private String indexLocation;
	/**
	 * 登陆地址
	 */
	private String loginLocation;
	/**
	 * 登陆失败跳转地址
	 */
	private String authErrorLocation;
	/**
	 * 白名单
	 */
	private List<String> whitelists;
	
}
