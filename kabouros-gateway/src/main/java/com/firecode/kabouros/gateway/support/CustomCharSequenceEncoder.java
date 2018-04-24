package com.firecode.kabouros.gateway.support;

import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.util.Map;

import org.reactivestreams.Publisher;
import org.springframework.core.ResolvableType;
import org.springframework.core.codec.AbstractEncoder;
import org.springframework.core.codec.CharSequenceEncoder;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferFactory;
import org.springframework.http.HttpStatus;
import org.springframework.lang.Nullable;
import org.springframework.util.MimeType;
import org.springframework.util.MimeTypeUtils;

import com.firecode.kabouros.common.annotation.ResponseWrap;
import com.firecode.kabouros.gateway.ResponseHelper;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * CharSequenceEncoder
 * 
 * @author JIANG
 */
public class CustomCharSequenceEncoder extends AbstractEncoder<CharSequence> {

	public CustomCharSequenceEncoder(MimeType... mimeTypes) {
		super(mimeTypes);
	}

	@Override
	public Flux<DataBuffer> encode(Publisher<? extends CharSequence> inputStream, DataBufferFactory bufferFactory,
			ResolvableType elementType, @Nullable MimeType mimeType, @Nullable Map<String, Object> hints) {

		Object wrap = hints.get(CustomJackson2JsonEncoder.RESPONSE_WRAP_HINT);

		if (wrap == null) {
			// old
			return Flux.from(inputStream).map(charSequence -> {
				CharBuffer charBuffer = CharBuffer.wrap(charSequence);
				ByteBuffer byteBuffer = getCharset(mimeType).encode(charBuffer);
				return bufferFactory.wrap(byteBuffer);
			});
		}

		if (inputStream instanceof Mono) {
			return Flux.from(inputStream).map(charSequence -> encodeValue(bufferFactory, hints, (ResponseWrap) wrap,
					charSequence, ResponseHelper.QUOTATION_MARK));
		}

		return Flux.from(inputStream)
				   .map(charSequence -> String.join("", ResponseHelper.QUOTATION_MARK, charSequence,ResponseHelper.QUOTATION_MARK))
				   .collectList().map(charSequences -> encodeValue(bufferFactory, hints, (ResponseWrap) wrap,charSequences.toString().replaceAll(" ",""),""))
				   .flux();
	}

	protected DataBuffer encodeValue(DataBufferFactory bufferFactory, Map<String, Object> hints,
			                        ResponseWrap wrap,CharSequence charSequence, String mark) {

		Object httpStatus = hints.get(CustomJackson2JsonEncoder.HTTP_STATUS_HINT);
		String result = ResponseHelper.getResultStr(charSequence, mark);
		if (httpStatus != null) {
			HttpStatus status = (HttpStatus) httpStatus;
			result = ResponseHelper.getResultStr(status.value(), status.name(), charSequence, mark);
		}
		return bufferFactory.wrap(result.getBytes(CharSequenceEncoder.DEFAULT_CHARSET));
	}

	@Override
	public boolean canEncode(ResolvableType elementType, @Nullable MimeType mimeType) {
		
		Class<?> clazz = elementType.resolve(Object.class);
		
		return super.canEncode(elementType, mimeType) && CharSequence.class.isAssignableFrom(clazz);
	}
	

	protected Charset getCharset(@Nullable MimeType mimeType) {
		
		if (mimeType != null && mimeType.getCharset() != null) {
			
			return mimeType.getCharset();
		}
		return CharSequenceEncoder.DEFAULT_CHARSET;
	}

	/**
	 * Create a {@code CharSequenceEncoder} that supports only "text/plain".
	 */
	public static CustomCharSequenceEncoder textPlainOnly() {
		
		return new CustomCharSequenceEncoder(new MimeType("text", "plain", CharSequenceEncoder.DEFAULT_CHARSET));
	}

	/**
	 * Create a {@code CharSequenceEncoder} that supports all MIME types.
	 */
	public static CustomCharSequenceEncoder allMimeTypes() {
		
		return new CustomCharSequenceEncoder(new MimeType("text", "plain", CharSequenceEncoder.DEFAULT_CHARSET), MimeTypeUtils.ALL);
	}
}
