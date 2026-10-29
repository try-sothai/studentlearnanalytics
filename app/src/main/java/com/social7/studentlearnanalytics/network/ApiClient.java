package com.social7.studentlearnanalytics.network;

import android.content.Context;
import com.social7.studentlearnanalytics.storage.SessionManager;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

public class ApiClient {

    private static final String BASE_URL = "http://192.168.1.100:8000/api/";
    private static Retrofit retrofit = null;
    private static ApiService apiService = null;

    public static synchronized ApiService getApiService(Context context) {
        if (apiService == null || retrofit == null) {
            final Context appContext = context.getApplicationContext();

            HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor();
            if (com.social7.studentlearnanalytics.BuildConfig.DEBUG) {
                loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
            } else {
                loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.NONE);
            }

            Interceptor authInterceptor = new Interceptor() {
                @Override
                public Response intercept(Chain chain) throws IOException {
                    Request originalRequest = chain.request();
                    SessionManager sessionManager = new SessionManager(appContext);
                    String token = sessionManager.getToken();

                    Request.Builder builder = originalRequest.newBuilder()
                            .header("Accept", "application/json");

                    if (token != null && !token.trim().isEmpty()) {
                        builder.header("Authorization", "Bearer " + token.trim());
                    }

                    Request newRequest = builder.build();
                    return chain.proceed(newRequest);
                }
            };

            OkHttpClient okHttpClient = new OkHttpClient.Builder()
                    .addInterceptor(authInterceptor)
                    .addInterceptor(loggingInterceptor)
                    .connectTimeout(30, TimeUnit.SECONDS)
                    .readTimeout(30, TimeUnit.SECONDS)
                    .writeTimeout(30, TimeUnit.SECONDS)
                    .build();

            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .client(okHttpClient)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();

            apiService = retrofit.create(ApiService.class);
        }
        return apiService;
    }

    public static String getBaseUrl() {
        return BASE_URL;
    }
}
