package com.example.pallab.travelgoapp.Remote;

import retrofit2.Retrofit;
import retrofit2.converter.scalars.ScalarsConverterFactory;

/**
 * Created by PALLAB on 3/16/2018.
 */

public class GoogleMapAPI {
    private static Retrofit retrofit = null;
    public static Retrofit getClient(String baseURL){
        if (retrofit == null){

            retrofit = new Retrofit.Builder()
                    .baseUrl(baseURL)
                    .addConverterFactory(ScalarsConverterFactory.create())
                    .build();
        }
        return retrofit;
    }
}
