package com.campus.zhihu.data.remote;

import com.campus.zhihu.ZhihuApp;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Retrofit 网络客户端单例
 * 自动添加 JWT Token 到请求头
 */
public class RetrofitClient {

    private static volatile Retrofit retrofit;

    public static Retrofit getInstance() {
        if (retrofit == null) {
            synchronized (RetrofitClient.class) {
                if (retrofit == null) {
                    HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
                    logging.setLevel(HttpLoggingInterceptor.Level.BODY);

                    OkHttpClient client = new OkHttpClient.Builder()
                            .connectTimeout(15, TimeUnit.SECONDS)
                            .readTimeout(15, TimeUnit.SECONDS)
                            .writeTimeout(15, TimeUnit.SECONDS)
                            .addInterceptor(chain -> {
                                Request original = chain.request();
                                String token = ZhihuApp.getInstance().getToken();
                                if (token != null && !token.isEmpty()) {
                                    Request newRequest = original.newBuilder()
                                            .header("Authorization", "Bearer " + token)
                                            .build();
                                    return chain.proceed(newRequest);
                                }
                                return chain.proceed(original);
                            })
                            .addInterceptor(logging)
                            .build();

                    retrofit = new Retrofit.Builder()
                            .baseUrl(ZhihuApp.BASE_URL)
                            .client(client)
                            .addConverterFactory(GsonConverterFactory.create())
                            .build();
                }
            }
        }
        return retrofit;
    }

    public static <T> T createService(Class<T> serviceClass) {
        return getInstance().create(serviceClass);
    }
}
