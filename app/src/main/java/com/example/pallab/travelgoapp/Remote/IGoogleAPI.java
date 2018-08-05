package com.example.pallab.travelgoapp.Remote;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Url;

/**
 * Created by PALLAB on 3/9/2018.
 */

public interface IGoogleAPI {

    @GET
    Call<String> getPath(@Url String url);

}
