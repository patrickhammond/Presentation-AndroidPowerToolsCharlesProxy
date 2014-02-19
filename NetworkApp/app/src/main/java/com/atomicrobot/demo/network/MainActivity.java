package com.atomicrobot.demo.network;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.squareup.okhttp.OkHttpClient;

import java.security.cert.X509Certificate;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import retrofit.Callback;
import retrofit.RestAdapter;
import retrofit.RetrofitError;
import retrofit.client.OkClient;
import retrofit.client.Response;

public class MainActivity extends Activity {

    private RestAdapter directRestAdapter;
    private ZenService directZenService;

    private RestAdapter proxyRestAdapter;
    private ZenService proxyZenService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Adapter not going through a reverse proxy
        directRestAdapter = new RestAdapter.Builder()
                .setEndpoint("https://api.github.com")
                .setConverter(new StringConverter())
                .build();
        directZenService = directRestAdapter.create(ZenService.class);

        OkHttpClient proxyClient = new OkHttpClient();
        //setupSSLToTrustEverything(proxyClient);
        //proxyClient.setReadTimeout(5, TimeUnit.SECONDS);

        // Adapter going through a reverse proxy listening on localhost:3001
        // Emulator maps 10.0.2.2 to the physical localhost interface
        proxyRestAdapter = new RestAdapter.Builder()
                .setEndpoint("https://10.0.2.2:3001")
                .setClient(new OkClient(proxyClient))
                .setConverter(new StringConverter())
                .build();
        proxyZenService = proxyRestAdapter.create(ZenService.class);


        setContentView(R.layout.activity_main);

        findViewById(R.id.view_find_zen).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                findZen(directZenService);
            }
        });

        findViewById(R.id.view_find_zen_proxy).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                findZen(proxyZenService);
            }
        });
    }

    private void findZen(ZenService zenService) {
        zenService.findZen(new Callback<String>() {
            @Override
            public void success(String zen, Response response) {
                Toast.makeText(MainActivity.this, zen, Toast.LENGTH_LONG).show();
            }

            @Override
            public void failure(RetrofitError retrofitError) {
                Toast.makeText(MainActivity.this, retrofitError.getMessage(), Toast.LENGTH_LONG).show();
                Log.e("APP", "Network failure", retrofitError);
            }
        });
    }

    /**
     * If you ship this turned on in production bad things will happen to you...
     */
    private void setupSSLToTrustEverything(OkHttpClient client) {
        try {
            client.setHostnameVerifier(new HostnameVerifier() {
                public boolean verify(String string, SSLSession ssls) {
                    return true;
                }
            });

            TrustManager[] trustAllCerts = new TrustManager[]{new X509TrustManager() {
                @Override
                public void checkClientTrusted(final X509Certificate[] chain, final String authType) {
                }

                @Override
                public void checkServerTrusted(final X509Certificate[] chain, final String authType) {
                }

                @Override
                public X509Certificate[] getAcceptedIssuers() {
                    return new java.security.cert.X509Certificate[]{};
                }
            }};

            SSLContext sslContext = SSLContext.getInstance("SSL");
            sslContext.init(null, trustAllCerts, new java.security.SecureRandom());
            SSLSocketFactory sslSocketFactory = sslContext.getSocketFactory();
            client.setSslSocketFactory(sslSocketFactory);

        } catch (Exception ex) {
            throw new IllegalStateException();
        }
    }
}
