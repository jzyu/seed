package com.github.jzyu.library.seed.http;

/**
 * Author: jzyu
 * Date  : 2017/5/17
 */
public enum HttpCacheType {
    NONE,   // no cache
    ONLY,   // read from cache only, if no cache, callback with response code=504
    SHORT,  // cache short time (24h)
}
