package com.github.jzyu.library.seed.http;

/**
 * Author: jzyu
 * Date  : 2017/5/17
 */

public interface HttpRequestModifier {
     okhttp3.Request.Builder onRequestModify(okhttp3.Interceptor.Chain chain);
}
