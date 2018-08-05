package com.example.pallab.travelgoapp.Common;

import android.location.Location;

import com.example.pallab.travelgoapp.Model.Rider;
import com.example.pallab.travelgoapp.Model.User;
import com.example.pallab.travelgoapp.Remote.FCMClient;
import com.example.pallab.travelgoapp.Remote.GoogleMapAPI;
import com.example.pallab.travelgoapp.Remote.IFCMService;
import com.example.pallab.travelgoapp.Remote.IGoogleAPI;
import com.example.pallab.travelgoapp.Remote.RetrofitClient;

/**
 * Created by PALLAB on 3/9/2018.
 */

public class Common {

    public static boolean isDriverFound = false;
    public static String driverId = "";

    public  static Location mLastLocation1;
    public  static  Rider currentUser1 = new Rider();

    public static  final String driver_tb1 = "Drivers";
    public static  final String user_driver_tb1 = "DriversInformation";
    public static  final String user_rider_tb1 = "RidersInformation";
    public static  final String pickup_request_tb1 = "PickupRequest";
    public static  final String token_tb1 = "Tokens";
    public static  final String rate_detail_tb1 = "RateDetails";

    public  static User currentUser;

    public static Location mLastLocation = null;

    public static final String baseURL = "https://maps.googleapis.com";
    public static final String fcmURL = "https://fcm.googleapis.com/";
    public static final String googleAPIUrl = "https://maps.googleapis.com";
    public static  final String user_field = "usr";
    public static  final String pwd_field = "pwd";


    public static  final String user_field1= "rider_usr";
    public static  final String pwd_field1= "rider_pwd";


    public static final int PICK_IMAGE_REQUEST = 9999;


    public   static double base_fare = 5;
    private  static double time_rate = .25;
    private  static double distance_rate = 1 ;

    public static  double getPrice(double km,int min){

        return  (base_fare +(time_rate*min)+(distance_rate*km));

    }

    public  static double formulaPrice(double km,double min){

        return (base_fare +(time_rate*min)+(distance_rate*km));
    }



    public static IGoogleAPI getGoogleAPI(){
        return RetrofitClient.getClient(baseURL).create(IGoogleAPI.class);
    }

    public static IFCMService getFCMService(){
        return FCMClient.getClient(fcmURL).create(IFCMService.class);
    }

    public static IGoogleAPI getGoogleService(){
        return GoogleMapAPI.getClient(googleAPIUrl).create(IGoogleAPI.class);
    }

}
