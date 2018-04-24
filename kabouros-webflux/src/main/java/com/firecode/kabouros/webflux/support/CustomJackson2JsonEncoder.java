package com.firecode.kabouros.webflux.support;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.reactivestreams.Publisher;
import org.springframework.core.MethodParameter;
import org.springframework.core.ResolvableType;
import org.springframework.core.codec.CodecException;
import org.springframework.core.codec.EncodingException;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.codec.json.Jackson2CodecSupport;
import org.springframework.http.codec.json.Jackson2JsonEncoder;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;
import org.springframework.util.MimeType;

import com.fasterxml.jackson.annotation.JsonView;
import com.fasterxml.jackson.core.JsonEncoding;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.exc.InvalidDefinitionException;
import com.firecode.kabouros.common.annotation.ResponseWrap;
import com.firecode.kabouros.webflux.ResponseHelper;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * AbstractJackson2Encoder
 * @author JIANG
 */
public class CustomJackson2JsonEncoder extends Jackson2JsonEncoder{
	
	private static final byte[] NEWLINE_SEPARATOR = {'\n'};

	private static final Map<MediaType, byte[]> STREAM_SEPARATORS;
	
	public static final String RESPONSE_WRAP_HINT = Jackson2CodecSupport.class.getName() + ".responseWrap";
	public static final String HTTP_STATUS_HINT = Jackson2CodecSupport.class.getName() + ".httpStatus";

	static {
		STREAM_SEPARATORS = new HashMap<>();
		STREAM_SEPARATORS.put(MediaType.APPLICATION_STREAM_JSON, NEWLINE_SEPARATOR);
		STREAM_SEPARATORS.put(MediaType.parseMediaType("application/stream+x-jackson-smile"), new byte[0]);
	}
	
	private final List<MediaType> streamingMediaTypes;
	
	public CustomJackson2JsonEncoder(){
		
		this.streamingMediaTypes = Collections.singletonList(MediaType.APPLICATION_STREAM_JSON);
	}
	
	@Override
	public Flux<DataBuffer> encode(Publisher<?> inputStream, DataBufferFactory bufferFactory,
			ResolvableType elementType, @Nullable MimeType mimeType, @Nullable Map<String, Object> hints) {
		
		Object wrap = hints.get(RESPONSE_WRAP_HINT);
		if(wrap == null) {
			return super.encode(inputStream, bufferFactory, elementType, mimeType, hints);
		}
		Assert.notNull(inputStream, "'inputStream' must not be null");
		Assert.notNull(bufferFactory, "'bufferFactory' must not be null");
		Assert.notNull(elementType, "'elementType' must not be null");

		JsonEncoding encoding = getJsonEncoding(mimeType);

		if (inputStream instanceof Mono) {
			return Flux.from(inputStream).map(value ->
					encodeValue(value, mimeType, bufferFactory, elementType, hints, encoding,(ResponseWrap)wrap));
		}

		for (MediaType streamingMediaType : this.streamingMediaTypes) {
			if (streamingMediaType.isCompatibleWith(mimeType)) {
				byte[] separator = STREAM_SEPARATORS.getOrDefault(streamingMediaType, NEWLINE_SEPARATOR);
				return Flux.from(inputStream).map(value -> {
					DataBuffer buffer = encodeValue(value, mimeType, bufferFactory, elementType, hints, encoding,(ResponseWrap)wrap);
					if (separator != null) {
						buffer.write(separator);
					}
					return buffer;
				});
			}
		}

		ResolvableType listType = ResolvableType.forClassWithGenerics(List.class, elementType);
		return Flux.from(inputStream).collectList().map(list ->
				encodeValue(list, mimeType, bufferFactory, listType, hints, encoding,(ResponseWrap)wrap)).flux();
	}
	
	private DataBuffer encodeValue(Object value, @Nullable MimeType mimeType, DataBufferFactory bufferFactory,
			ResolvableType elementType, @Nullable Map<String, Object> hints, JsonEncoding encoding,ResponseWrap wrap) {

		JavaType javaType = getJavaType(elementType.getType(), null);
		Class<?> jsonView = (hints != null ? (Class<?>) hints.get(Jackson2CodecSupport.JSON_VIEW_HINT) : null);
		ObjectWriter writer = (jsonView != null ? getObjectMapper().writerWithView(jsonView) : getObjectMapper().writer());

		if (javaType.isContainerType()) {
			writer = writer.forType(javaType);
		}

		writer = customizeWriter(writer, mimeType, elementType, hints);

		DataBuffer buffer = bufferFactory.allocateBuffer();

		try(OutputStream outputStream = buffer.asOutputStream();) {
			
			Object httpStatus = hints.get(HTTP_STATUS_HINT);
			outputStream.write((httpStatus == null) ? ResponseHelper.SUCCESS_BYTES : ResponseHelper.getResultBytes((HttpStatus)httpStatus));
			JsonGenerator generator = getObjectMapper().getFactory().createGenerator(outputStream, encoding);
			writer.writeValue(generator, value);
			outputStream.write(ResponseHelper.BRACES_RIGHT_BYTES);
		}
		catch (InvalidDefinitionException ex) {
			throw new CodecException("Type definition error: " + ex.getType(), ex);
		}
		catch (JsonProcessingException ex) {
			throw new EncodingException("JSON encoding error: " + ex.getOriginalMessage(), ex);
		}
		catch (IOException ex) {
			throw new IllegalStateException("Unexpected I/O error while writing to data buffer", ex);
		}

		return buffer;
	}
	
	/**
	 * 获取 jsonView responseWrap httpStatus
	 */
	@Override
	public Map<String, Object> getEncodeHints(@Nullable ResolvableType resolvableType, ResolvableType elementType,
			@Nullable MediaType mediaType, ServerHttpRequest request, ServerHttpResponse response) {
		
		MethodParameter param = getParameter(resolvableType);
		Map<String,Object> map = new HashMap<>(4);
		JsonView annotation = getAnnotation(param, JsonView.class);
		if (annotation != null) {
			Class<?>[] classes = annotation.value();
			Assert.isTrue(classes.length == 1,String.join(": ","@JsonView only supported for write hints with exactly 1 class argument",param.toString()));
			map.put(JSON_VIEW_HINT, classes[0]);
			//return Collections.singletonMap();
		}
		ResponseWrap wrap = getAnnotation(param, ResponseWrap.class);
		if(wrap != null || (wrap = param.getDeclaringClass().getAnnotation(ResponseWrap.class)) != null){
			map.put(RESPONSE_WRAP_HINT, wrap);
			HttpStatus status = response.getStatusCode();
			if(status != null) map.put(HTTP_STATUS_HINT, status);
		}
		return map;
		//return Collections.emptyMap();
	}
	
	protected JsonEncoding getJsonEncoding(@Nullable MimeType mimeType) {
		if (mimeType != null && mimeType.getCharset() != null) {
			Charset charset = mimeType.getCharset();
			for (JsonEncoding encoding : JsonEncoding.values()) {
				if (charset.name().equals(encoding.getJavaName())) {
					return encoding;
				}
			}
		}
		return JsonEncoding.UTF8;
	}
	
}
