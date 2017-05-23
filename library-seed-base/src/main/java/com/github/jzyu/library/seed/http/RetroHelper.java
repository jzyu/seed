package com.github.jzyu.library.seed.http;

import android.content.Context;
import android.util.Log;

import com.github.jzyu.library.seed.util.FastjsonConverterFactory;
import com.github.jzyu.library.seed.util.SeedUtil;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import okhttp3.Cache;
import okhttp3.CacheControl;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import retrofit2.Retrofit;

/**
 * Author: jzyu
 * Date  : 2017/5/17
 */

public class RetroHelper<T> {

    public static final String TAG = RetroHelper.class.getSimpleName();
    private static final long SECONDS_ONE_DAY = 24 * 60 * 60;

    private List<RetroInfo<T>> retroInfos = new ArrayList<>();
    private Cache cache;
    private HttpRequestModifier requestModifier;

    final private Config config;
    final private Context appContext;
    final private String apiUrlBase;
    final private Class<T> apiSetClass;

    public RetroHelper(Context appContext, String apiUrlBase, Class<T> apiSetClass, Config config) {
        this.appContext = appContext;
        this.apiUrlBase = apiUrlBase;
        this.apiSetClass = apiSetClass;
        this.config = config == null ? new Config() : config;
    }

    public void setRequestModifier(HttpRequestModifier requestModifier) {
        this.requestModifier = requestModifier;
    }

    private Request.Builder getRequestBuilder(Interceptor.Chain chain) {
        if (this.requestModifier != null) {
            return this.requestModifier.onRequestModify(chain);
        } else {
            return chain.request().newBuilder();
        }
    }

    private RetroInfo<T> createRetroInfo(final HttpCacheType cacheType) {
        RetroInfo<T> info = new RetroInfo<>();
        Interceptor interceptor;
        boolean isNetworkInterceptor = false;

        switch (cacheType) {
            default:
            case NONE:
                interceptor = new Interceptor() {
                    @Override
                    public Response intercept(Chain chain) throws IOException {
                        Request request = getRequestBuilder(chain).build();
                        Log.d(TAG, "url = " + request.url());
                        return chain.proceed(request);
                    }
                };
                break;

            case ONLY:
                interceptor = new Interceptor() {
                    @Override
                    public Response intercept(Chain chain) throws IOException {
                        Request request = getRequestBuilder(chain)
                                .cacheControl(CacheControl.FORCE_CACHE)
                                .build();
                        Log.d(TAG, "FORCE_CACHE, url = " + request.url());
                        return chain.proceed(request);
                    }
                };
                break;

            case SHORT:
                isNetworkInterceptor = true;
                interceptor = new Interceptor() {
                    @Override
                    public Response intercept(Chain chain) throws IOException {
                        Request request;

                        if (SeedUtil.isConnected(appContext)) {
                            request = getRequestBuilder(chain)
                                    .build();
                            Log.d(TAG, "url = " + request.url());
                        } else {
                            request = getRequestBuilder(chain)
                                    .cacheControl(CacheControl.FORCE_CACHE)
                                    .build();
                            Log.d(TAG, "FORCE_CACHE, url = " + request.url());
                        }

                        Response response = chain.proceed(request);

                        if (request.method().equalsIgnoreCase("GET")) {
                            return response.newBuilder()
                                    .header("Cache-Control", "max-age=" + SECONDS_ONE_DAY)
                                    .build();
                        } else {
                            return response;
                        }
                    }
                };
                break;
        }

        info.cacheType = cacheType;
        info.retrofit = createRetrofit(interceptor, isNetworkInterceptor);
        info.apiSet = info.retrofit.create(apiSetClass);

        return info;
    }

    private Retrofit createRetrofit(Interceptor interceptor, boolean isNetworkInterceptor) {
        if (cache == null) {
            cache = new Cache(new File(appContext.getCacheDir(), "responses"), config.cacheSizeMB * 1024 * 1024);
        }

        OkHttpClient.Builder okHttpBuilder = new OkHttpClient.Builder()
                .cache(cache)
                .connectTimeout(config.timeoutConnect, TimeUnit.SECONDS)
                .readTimeout(config.timeoutRead, TimeUnit.SECONDS)
                //.writeTimeout(10, TimeUnit.SECONDS)
                .retryOnConnectionFailure(false);

        if (isNetworkInterceptor) {
            okHttpBuilder.addNetworkInterceptor(interceptor);
        } else {
            okHttpBuilder.addInterceptor(interceptor);
        }

        if (config.loadStetho) {
            // Stetho没有ReleaseStub，这里用Class.forName + newInstance方式动态创建对象，
            // 这样代码在debug和release都可以编译
            try {
                Class clsStethoInterceptor = Class.forName("com.facebook.stetho.okhttp3.StethoInterceptor");
                okHttpBuilder.addNetworkInterceptor((Interceptor) clsStethoInterceptor.newInstance());
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            } catch (InstantiationException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }

        return new Retrofit.Builder()
                .client(okHttpBuilder.build())
                .addConverterFactory(FastjsonConverterFactory.create())
                .baseUrl(apiUrlBase)
                .build();
    }

    //Refer: http://www.jianshu.com/p/ab41007f95c5
    private static void cancelCallsWithTag(Retrofit retrofit, Object tag) {
        if (retrofit == null || tag == null) {
            return;
        }

        OkHttpClient okHttpClient = null;
        if (retrofit.callFactory() instanceof OkHttpClient) {
            okHttpClient = (OkHttpClient) retrofit.callFactory();
        }
        if (okHttpClient == null) return;

        synchronized (okHttpClient.dispatcher().getClass()) {
            for (okhttp3.Call call : okHttpClient.dispatcher().queuedCalls()) {
                if (tag.equals(call.request().tag())) {
                    Log.e(TAG, "cancelCall! url=" + call.request().url());
                    call.cancel();
                }
            }

            for (okhttp3.Call call : okHttpClient.dispatcher().runningCalls()) {
                if (tag.equals(call.request().tag())) {
                    Log.e(TAG, "cancelCall! url=" + call.request().url());
                    call.cancel();
                }
            }
        }
    }

    public void cancelRequests(List<okhttp3.Request> requests) {
        for (okhttp3.Request req: requests) {
            for (RetroInfo retroInfo : retroInfos) {
                cancelCallsWithTag(retroInfo.retrofit, req);
            }
        }
    }

    public RetroInfo<T> getRetroInfo(HttpCacheType cacheType) {
        for (RetroInfo<T> info: retroInfos) {
            if (info.cacheType == cacheType) {
                return info;
            }
        }

        RetroInfo<T> info = createRetroInfo(cacheType);
        retroInfos.add(info);

        return info;
    }

    public void clearCache() {
        if (cache != null) {
            try {
                cache.evictAll();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static final class Config {
        private int timeoutConnect = 10;
        private int timeoutRead = 5;
        private boolean retryOnFailure = false;
        private long cacheSizeMB = 10;
        private boolean loadStetho;
    }

    public static final class Builder {
        Config config = new Config();

        public Builder timeoutConnect(int seconds) {
            config.timeoutConnect = seconds;
            return this;
        }
        public Builder timeoutRead(int seconds) {
            config.timeoutRead = seconds;
            return this;
        }
        public Builder retry(boolean retry) {
            config.retryOnFailure = retry;
            return this;
        }
        public Builder cacheSize(long mb) {
            config.cacheSizeMB = mb;
            return this;
        }
        public Builder loadStetho() {
            config.loadStetho = true;
            return this;
        }
        public Config build() {
            return config;
        }
    }
}
