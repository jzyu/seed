package com.github.jzyu.library.seed.util;

import com.alibaba.fastjson.JSON;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.nio.charset.Charset;

import okhttp3.MediaType;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Converter;
import retrofit2.Retrofit;

/**
 * Created by dayaa on 16/1/20.
 */
public class FastjsonConverterFactory extends Converter.Factory {

    private Charset charset;
    private static final Charset UTF_8  = Charset.forName("UTF-8");

    public static FastjsonConverterFactory create() {
        return create(UTF_8);
    }

    public static FastjsonConverterFactory create(Charset charset) {
        return new FastjsonConverterFactory(charset);
    }

    public FastjsonConverterFactory(Charset charset) {
        this.charset = charset;
    }

    @Override
    public Converter<?, RequestBody> requestBodyConverter(Type type, Annotation[] parameterAnnotations,
        Annotation[] methodAnnotations, Retrofit retrofit) {
        return new FastjsonRequestBodyConverter<>(type, charset);
    }

    @Override
    public Converter<ResponseBody, ?> responseBodyConverter(Type type, Annotation[] annotations, Retrofit retrofit) {
        return new FastjsonResponseBodyConverter<>(type, charset);
    }

    private static class FastjsonResponseBodyConverter<T> implements Converter<ResponseBody, T> {

        private Type type;
        private Charset charset;

        public FastjsonResponseBodyConverter() {
        }

        public FastjsonResponseBodyConverter(Type type, Charset charset) {
            this.type = type;
            this.charset = charset;
        }

        @Override
        public T convert(ResponseBody value) throws IOException {
            try {
                return JSON.parseObject(value.string(), type);
            } finally {
                value.close();
            }
        }
    }

    private static class FastjsonRequestBodyConverter<T> implements Converter<T, RequestBody> {

        private Type type;
        private Charset charset;
        private static final MediaType MEDIA_TYPE = MediaType.parse("application/json; charset=UTF-8");

        public FastjsonRequestBodyConverter(Type type, Charset charset) {
            this.type = type;
            this.charset = charset;
        }

        @Override
        public RequestBody convert(T value) throws IOException {
            return RequestBody.create(MEDIA_TYPE, JSON.toJSONString(value).getBytes(charset));
        }
    }
}
