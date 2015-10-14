package com.mehmetakiftutuncu.muezzin.utilities;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import com.squareup.okhttp.Callback;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

public class Web {
    private static Web instance;
    private OkHttpClient client;

    private Web() {
        client = new OkHttpClient();
        client.setConnectTimeout(Conf.Timeout.connectTimeout, TimeUnit.SECONDS);
        client.setReadTimeout(Conf.Timeout.readTimeout, TimeUnit.SECONDS);
    }

    public static Web instance() {
        if (instance == null) {
            instance = new Web();
        }

        return instance;
    }

    public void get(String url, final Callback requestCallbackListener) {
        Request request = new Request.Builder().url(url).build();

        client.newCall(request).enqueue(requestCallbackListener);
    }

    /**
     * Checks whether or not the device is connected to internet
     *
     * @param context {@link Context} to access {@link Context#CONNECTIVITY_SERVICE}
     *
     * @return true if the device is connected to internet or false otherwise
     */
    public static boolean hasInternet(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = cm.getActiveNetworkInfo();

        return networkInfo != null && networkInfo.isConnected();
    }

    public static boolean isResponseSuccessfulAndJson(Response response) {
        return response != null && response.isSuccessful() && response.header("Content-Type", "").contains("application/json");
    }
}
