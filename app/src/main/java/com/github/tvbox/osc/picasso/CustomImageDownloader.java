package com.github.tvbox.osc.picasso;

import android.text.TextUtils;
import com.github.tvbox.osc.util.UA;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.squareup.picasso.Downloader;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import java.io.IOException;
import java.net.URLDecoder;

public class CustomImageDownloader implements Downloader {
    final OkHttpClient client;

    public CustomImageDownloader(OkHttpClient client) {
        this.client = client;
    }

    @Override
    public Response load(Request request) throws IOException {
        String url = request.url().toString();
        
        String header = null;
        String cookie = null;
        String referer = null;
        String userAgent = null;
        if (url.contains("@Headers=")){
            header =url.split("@Headers=")[1].split("@")[0];
            header =URLDecoder.decode(header,"UTF-8");
        }
        if (url.contains("@Cookie=")) cookie = url.split("@Cookie=")[1].split("@")[0];
        if (url.contains("@Referer=")) referer = url.split("@Referer=")[1].split("@")[0];
        if (url.contains("@User-Agent=")) userAgent = url.split("@User-Agent=")[1].split("@")[0];
        
        url = url.split("@")[0];
        Request.Builder builder = request.newBuilder().url(url);
        // takagen99 : Shift Douban referer to here instead
        if (url.contains("douban")) {
            userAgent = UA.random();
            referer = "https://movie.douban.com/";
            builder.addHeader("User-Agent", userAgent);
            builder.addHeader("Referer", referer);
        }

        if (!TextUtils.isEmpty(header)) {
            JsonObject jsonInfo = new Gson().fromJson(header, JsonObject.class);
            for (String key : jsonInfo.keySet()) {
                String val = jsonInfo.get(key).getAsString();
                builder.addHeader(key.toUpperCase(), removeDuplicateSlashes(val));
            }
        } else {
            if(!TextUtils.isEmpty(cookie)) {
                assert cookie != null;
                builder.addHeader("Cookie", cookie);
            }
            if(!TextUtils.isEmpty(userAgent)){
                assert userAgent != null;
                builder.addHeader("User-Agent", userAgent);
            }else {
                String mobile_UA = "Dalvik/2.1.0 (Linux; U; Android 13; M2102J2SC Build/TKQ1.220829.002)";
                builder.addHeader("User-Agent", mobile_UA);
            }
            if(!TextUtils.isEmpty(referer)){
                assert referer != null;
                builder.addHeader("Referer", referer);
            }
        }
        return client.newCall(builder.build()).execute();
    }
    
    private static String removeDuplicateSlashes(String paramValue) {
        return paramValue.replaceAll("//", "/");
    }

    @Override
    public void shutdown() {
        client.dispatcher().executorService().shutdown();
        client.connectionPool().evictAll();
    }

}
