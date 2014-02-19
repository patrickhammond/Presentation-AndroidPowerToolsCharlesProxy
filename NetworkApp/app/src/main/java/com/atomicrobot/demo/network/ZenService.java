package com.atomicrobot.demo.network;

import retrofit.Callback;
import retrofit.http.GET;

public interface ZenService {
    @GET("/zen")
    public void findZen(Callback<String> zen);
}
