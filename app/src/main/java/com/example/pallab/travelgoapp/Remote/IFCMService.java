package com.example.pallab.travelgoapp.Remote;

import com.example.pallab.travelgoapp.Model.FCMResponse;
import com.example.pallab.travelgoapp.Model.Sender;


import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;

/**
 * Created by PALLAB on 3/12/2018.
 */

public interface IFCMService {
    @Headers({
            "Content-Type: application/json",
            "Authorization:key=AAAAnyKZbGM:APA91bG4vfvniwfG1zPjHAkQ8CjLJiM_Xsz9fiE1FhGLXJW6NhwrXl-5DupMWUBVwvI5JjuPxY98S6uBbZ-lHrCwXsSlWttwcwaEhGr9uSAj8R99q7l3bSl1mMywcM2txikb_oRY_wca"

    })

    @POST("fcm/send")
    Call<FCMResponse> sendMessage(@Body Sender body);

}
