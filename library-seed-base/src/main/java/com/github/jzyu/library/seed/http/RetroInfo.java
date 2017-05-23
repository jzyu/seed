package com.github.jzyu.library.seed.http;

import retrofit2.Retrofit;

/**
 * Author: jzyu
 * Date  : 2017/5/17
 */
public class RetroInfo<T> {
    public HttpCacheType cacheType;
    public Retrofit retrofit;
    public T apiSet;
}
