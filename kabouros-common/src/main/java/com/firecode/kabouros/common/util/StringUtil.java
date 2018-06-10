package com.firecode.kabouros.common.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.util.StringUtils;

public class StringUtil {
	
	public static final Pattern REGEX_A_B = Pattern.compile("[A-Z]");
	
	/**
	 * 驼峰转换
	 * @param camelStr   待转换字符串
	 * @param str        驼峰转换：_
	 * @return           aB -> a_b
	 */
	public static String camelConvert(String camelStr, String convertStr) {
		if (!StringUtils.isEmpty(camelStr)) {
			StringBuilder sb = new StringBuilder(camelStr);
			Matcher matcher = REGEX_A_B.matcher(camelStr);
			int i = 0;
			while (matcher.find()) {
				sb.replace(matcher.start() + i, matcher.end() + i,String.join("", convertStr,matcher.group().toLowerCase()));
				i++;
			}
			return sb.toString();
		}
		return "";
	}

	/**
	 * @param template    信息模板
	 * @param args        参数
	 * @return            装配后数据
	 */
	public static String format(String template, Object... args) {
		template = String.valueOf(template); // null -> "null"

		// start substituting the arguments into the '%s' placeholders
		StringBuilder builder = new StringBuilder(template.length() + 16 * args.length);
		int templateStart = 0;
		int i = 0;
		while (i < args.length) {
			int placeholderStart = template.indexOf("%s", templateStart);
			if (placeholderStart == -1) {
				break;
			}
			builder.append(template.substring(templateStart, placeholderStart));
			builder.append(args[i++]);
			templateStart = placeholderStart + 2;
		}
		builder.append(template.substring(templateStart));

		// if we run out of placeholders, append the extra args in square braces
		if (i < args.length) {
			builder.append(" [");
			builder.append(args[i++]);
			while (i < args.length) {
				builder.append(", ");
				builder.append(args[i++]);
			}
			builder.append(']');
		}

		return builder.toString();
	}
	
	public static void main(String[] args) {
		System.err.println(camelConvert("AaCCC","_"));
	}

}
