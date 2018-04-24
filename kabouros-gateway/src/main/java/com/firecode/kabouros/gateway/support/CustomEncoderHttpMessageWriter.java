package com.firecode.kabouros.gateway.support;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.springframework.core.MethodParameter;
import org.springframework.core.ResolvableType;
import org.springframework.core.codec.Encoder;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.codec.EncoderHttpMessageWriter;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.lang.Nullable;

import com.firecode.kabouros.common.annotation.ResponseWrap;
/**
 * 处理String 类型返回值
 * @author JIANG
 *
 * @param <T>
 */
public class CustomEncoderHttpMessageWriter<T> extends EncoderHttpMessageWriter<T>{
	
	private final Encoder<T> encoder;

	public CustomEncoderHttpMessageWriter(Encoder<T> encoder) {
		super(encoder);
		this.encoder = encoder;
	}
	
	protected Map<String, Object> getWriteHints(ResolvableType streamType, ResolvableType elementType,
			@Nullable MediaType mediaType, ServerHttpRequest request, ServerHttpResponse response) {

		if (this.encoder instanceof CustomCharSequenceEncoder) {
			MethodParameter source = (MethodParameter)streamType.getSource();
			ResponseWrap wrap = source.getMethodAnnotation(ResponseWrap.class);
			if(wrap != null || (wrap = source.getDeclaringClass().getAnnotation(ResponseWrap.class)) != null){
				Map<String,Object> map = new HashMap<>(4);
				map.put(CustomJackson2JsonEncoder.RESPONSE_WRAP_HINT, wrap);
				HttpStatus httpStatus = response.getStatusCode();
				if(httpStatus != null) map.put(CustomJackson2JsonEncoder.HTTP_STATUS_HINT, httpStatus);
				response.getHeaders().setContentType(MediaType.APPLICATION_JSON_UTF8);
				return map;
			}
		}
		return Collections.emptyMap();
	}

}
