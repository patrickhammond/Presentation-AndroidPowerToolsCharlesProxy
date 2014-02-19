package com.atomicrobot.demo.network;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.google.common.io.CharStreams;
import com.squareup.okhttp.OkHttpClient;

import java.io.InputStreamReader;
import java.lang.reflect.Type;
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
import retrofit.converter.ConversionException;
import retrofit.converter.Converter;
import retrofit.mime.TypedByteArray;
import retrofit.mime.TypedInput;
import retrofit.mime.TypedOutput;

public class MainActivity extends Activity {

    private RestAdapter directRestAdapter;
    private ZenService directZenService;

    private RestAdapter proxyRestAdapter;
    private ZenService proxyZenService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Not going through a reverse proxy
        directRestAdapter = new RestAdapter.Builder()
                .setEndpoint("https://api.github.com")
                .setConverter(new StringConverter())
                .build();
        directZenService = directRestAdapter.create(ZenService.class);

        OkHttpClient proxyClient = new OkHttpClient();
        //setupSSLTrustAll(proxyClient);
        //proxyClient.setReadTimeout(5, TimeUnit.SECONDS);

        // Going through a reverse proxy listening on the localhost:3001 on the emulator's host
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
    private void setupSSLTrustAll(OkHttpClient client) {
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

    // Needed for the demo, but not important for the demo
    public static class StringConverter implements Converter {
        @Override
        public Object fromBody(TypedInput body, Type type) throws ConversionException {
            try {
                return CharStreams.toString(new InputStreamReader(body.in(), "UTF-8"));
            } catch (Exception ex) {
                throw new ConversionException("Couldn't convert it to a string", ex);
            }
        }

        @Override
        public TypedOutput toBody(Object object) {
            try {
                return new TypedByteArray("text/plain", ((String) object).getBytes("UTF-8"));
            } catch (Exception ex) {
                throw new IllegalArgumentException();
            }
        }
    }
}
