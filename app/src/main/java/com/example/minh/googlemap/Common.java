package com.example.minh.googlemap;

import com.example.minh.googlemap.Model.MyPlaces;
import com.example.minh.googlemap.Model.Results;
import com.example.minh.googlemap.Remote.IGoogleAPIService;
import com.example.minh.googlemap.Remote.RetrofitClient;

/**
 * Created by Minh on 5/11/2018.
 */

public class Common {
    public static Results currentResult;
    private static final String GOOGLE_API_URL= "https://maps.googleapis.com/";
    public static IGoogleAPIService getGoogleAPIService(){
        return RetrofitClient.getClient(GOOGLE_API_URL).create(IGoogleAPIService.class);
    }
}
