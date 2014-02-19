package com.atomicrobot.demo.network;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.google.common.io.CharStreams;

import java.io.InputStreamReader;
import java.lang.reflect.Type;

import retrofit.Callback;
import retrofit.RestAdapter;
import retrofit.RetrofitError;
import retrofit.client.Response;
import retrofit.converter.ConversionException;
import retrofit.converter.Converter;
import retrofit.mime.TypedByteArray;
import retrofit.mime.TypedInput;
import retrofit.mime.TypedOutput;

public class MainActivity extends Activity {

    private RestAdapter restAdapter;
    private ZenService zenService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        findViewById(R.id.view_find_zen).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                findZen();
            }
        });

        restAdapter = new RestAdapter.Builder()
                .setEndpoint("https://api.github.com")
                .setConverter(new StringConverter())
                .build();
        zenService = restAdapter.create(ZenService.class);
    }

    private void findZen() {
        zenService.findZen(new Callback<String>() {
            @Override
            public void success(String zen, Response response) {
                Toast.makeText(MainActivity.this, zen, Toast.LENGTH_LONG).show();
            }

            @Override
            public void failure(RetrofitError retrofitError) {
                Log.e("APP", "Network failure", retrofitError);
            }
        });
    }

    public static class StringConverter implements Converter {
        @Override
        public Object fromBody(TypedInput body, Type type) throws ConversionException {
            try {
                return CharStreams.toString(new InputStreamReader(body.in(), "UTF-8" ) );
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
