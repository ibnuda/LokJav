package com.ijauradunbi.pegel.lokjav.lok;

import android.util.Log;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import cz.msebera.android.httpclient.Header;

import java.util.Locale;

/**
 * Created by Ibnu on 08/02/2016.
 */
public class LoopjHttpClient {
    private static AsyncHttpClient client = new AsyncHttpClient();

    public static void get(String url, RequestParams params, AsyncHttpResponseHandler responseHandler){
        client.get(url, params, responseHandler);
    }

    public static void post(String url, RequestParams requestParams, AsyncHttpResponseHandler responseHandler){
        client.post(url, requestParams, responseHandler);
    }

    public static void debugLoop(String tag, String method, String url, RequestParams params, byte[] response, cz.msebera.android.httpclient.Header[] headers, int status, Throwable t){
        Log.d(tag, client.getUrlWithQueryString(false, url, params));

        if (headers == null) return;
        Log.e(tag, method);
        Log.d(tag, "Return Headers : ");
        for (Header h : headers) {
            String _h = String.format(Locale.US, "%s: %s", h.getName(), h.getValue());
            Log.d(tag, _h);
        }

        if (t != null) Log.d(tag, "Throwable : " + t);
        Log.e(tag, "status kode : " + status);
        if (response != null) Log.d(tag, "response : " + new String(response));
    }
}
