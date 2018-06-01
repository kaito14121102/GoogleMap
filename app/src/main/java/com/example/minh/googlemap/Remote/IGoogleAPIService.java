package com.example.minh.googlemap.Remote;

import com.example.minh.googlemap.Model.MyPlaces;
import com.example.minh.googlemap.Model.PlaceDetail;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Url;

/**
 * Created by Minh on 5/11/2018.
 */

public interface IGoogleAPIService {
    @GET
    Call<MyPlaces> getNearByPlaces(@Url String url);

    @GET
    Call<PlaceDetail> getDetailPlace(@Url String url);
}
