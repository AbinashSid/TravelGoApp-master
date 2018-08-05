package com.example.pallab.travelgoapp;

import android.Manifest;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.pallab.travelgoapp.Common.Common;
import com.example.pallab.travelgoapp.Helper.CustomInfoWindow;
import com.example.pallab.travelgoapp.Model.FCMResponse;
import com.example.pallab.travelgoapp.Model.Notification;
import com.example.pallab.travelgoapp.Model.Rider;
import com.example.pallab.travelgoapp.Model.Sender;
import com.example.pallab.travelgoapp.Model.Token;
import com.example.pallab.travelgoapp.Remote.IFCMService;
import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.GeoQuery;
import com.firebase.geofire.GeoQueryEventListener;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.AutocompleteFilter;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlaceAutocompleteFragment;
import com.google.android.gms.location.places.ui.PlaceSelectionListener;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.google.gson.Gson;
import com.google.maps.android.SphericalUtil;
import com.rengwuxian.materialedittext.MaterialEditText;
import com.squareup.picasso.Picasso;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import de.hdodenhof.circleimageview.CircleImageView;
import dmax.dialog.SpotsDialog;
import io.paperdb.Paper;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import android.telephony.SmsManager;
import android.provider.Settings;

public class Home extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener,
        OnMapReadyCallback {

    SupportMapFragment mapFragment;

    //sos
    Button smsButton;
    StringBuffer loc = new StringBuffer();
    LocationManager locationManager;
    LocationListener locationListener;
    double lat, lon;
    String phoneNo = "+91 9835127633";
    String message = "I need Help. I'm sending My location. ";


    FusedLocationProviderClient fusedLocationProviderClient;
    LocationCallback locationCallback;


    //location
    private GoogleMap mMap;
    //play services
    private static final int MY_PERMISSION_REQUEST_CODE = 7000;
    private static final int PLAY_SERVICE_RES_REQUEST = 7001;

    private LocationRequest mLocationRequest;
    private GoogleApiClient mGoogleApiClient;
    private Location mLastLocation;

    private static int UPDATE_INTERVAL = 5000;
    private static int FATEST_INTERVAL = 3000;
    private static int DISPLACEMENT = 10;

    DatabaseReference ref;
    GeoFire geoFire;

    Marker mUserMarker, markerDestination;

    //BottomSheet
    ImageView imgExpandable;
    BottomSheetRiderFragment mBottomSheet;
    Button btnPickupRequest;


    int radius = 1; //radius of 1 km
    int distance = 1; //upto 3 km
    private static final int LIMIT = 3;

    //send alert
    IFCMService mServices;

    //presence system
    DatabaseReference driversAvailable;

    PlaceAutocompleteFragment place_location, place_destination;
    AutocompleteFilter typeFilter;

    String mPlaceLocation, mPlaceDestination;

    //new update information
    CircleImageView imageAvatar;
    TextView txtRiderName, txtStars;


    //declare update information
    FirebaseStorage storage;
    StorageReference storageReference;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);


        mServices = Common.getFCMService();

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);


        //init stotage
        storage = FirebaseStorage.getInstance();
        storageReference = storage.getReference();

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        //Add file view by id for ....

        View navigationHeaderView = navigationView.getHeaderView(0);
        txtRiderName = navigationHeaderView.findViewById(R.id.txtRiderName);
        txtRiderName.setText(String.format("%s", Common.currentUser1.getName()));

        txtStars = navigationHeaderView.findViewById(R.id.txtStars);
        txtStars.setText(String.format("%s", Common.currentUser1.getRates()));
        imageAvatar = navigationHeaderView.findViewById(R.id.imageAvatar);


        //Load Avatar
        if (Common.currentUser1.getAvatarUrl() != null && !TextUtils.isEmpty(Common.currentUser1.getAvatarUrl())) {

            Picasso.with(this)
                    .load(Common.currentUser1.getAvatarUrl())
                    .into(imageAvatar);

        }


        //Maps
        mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);


        //Init View
        imgExpandable = (ImageView) findViewById(R.id.imgExpandable);


        btnPickupRequest = (Button) findViewById(R.id.btnPickupRequest);
        btnPickupRequest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!Common.isDriverFound)
                    requestPickupHere(FirebaseAuth.getInstance().getCurrentUser().getUid());

                else
                    sendRequestToDriver(Common.driverId);
            }
        });

        place_destination = (PlaceAutocompleteFragment) getFragmentManager().findFragmentById(R.id.place_destination);
        place_location = (PlaceAutocompleteFragment) getFragmentManager().findFragmentById(R.id.place_location);
        typeFilter = new AutocompleteFilter.Builder()
                .setTypeFilter(AutocompleteFilter.TYPE_FILTER_ADDRESS)
                .setTypeFilter(3)
                .build();


        //Event
        place_location.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(Place place) {

                mPlaceLocation = place.getAddress().toString();
                //remove old markerr
                mMap.clear();

                //Add marker at new location
                mUserMarker = mMap.addMarker(new MarkerOptions().position(place.getLatLng())
                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.marker))
                        .title("Pickup Here"));
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(place.getLatLng(), 15.0f));
            }

            @Override
            public void onError(Status status) {

            }
        });

        place_destination.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(Place place) {
                mPlaceDestination = place.getAddress().toString();
                mMap.addMarker(new MarkerOptions().position(place.getLatLng())
                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.destination_marker))
                        .title("Destination"));
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(place.getLatLng(), 15.0f));

                //show infomormation in bottom
                BottomSheetRiderFragment mBottomSheet = BottomSheetRiderFragment.newInstance(mPlaceLocation, mPlaceDestination, false);
                Home.super.onPostResume();
                mBottomSheet.show(getSupportFragmentManager(), mBottomSheet.getTag());
            }

            @Override
            public void onError(Status status) {

            }
        });

        setUpLocation();

        updateFirebaseToken();
    }

    private void updateFirebaseToken() {

        FirebaseDatabase db = FirebaseDatabase.getInstance();
        DatabaseReference tokens = db.getReference(Common.token_tb1);

        Token token = new Token(FirebaseInstanceId.getInstance().getToken());

        tokens.child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                .setValue(token);
    }


    private void sendRequestToDriver(String driverId) {
        DatabaseReference tokens = FirebaseDatabase.getInstance().getReference(Common.token_tb1);
        tokens.orderByKey().equalTo(driverId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        for (DataSnapshot postSnapShot : dataSnapshot.getChildren()) {
                            Token token = postSnapShot.getValue(Token.class); // Get Token object from database with key

                            //Make raw payload - convert LatLng to json
                            String json_lat_lng = new Gson().toJson(new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude()));

                            String riderToken = FirebaseInstanceId.getInstance().getToken();
                            Notification data = new Notification(riderToken, json_lat_lng); //send it to Driver app and we will convert
                            Sender content = new Sender(token.getToken(), data);//send this data to token

                            mServices.sendMessage(content)
                                    .enqueue(new Callback<FCMResponse>() {
                                        @Override
                                        public void onResponse(Call<FCMResponse> call, Response<FCMResponse> response) {
                                            if (response.body().success == 1)
                                                Toast.makeText(Home.this, "Request sent !", Toast.LENGTH_SHORT).show();
                                            else
                                                Toast.makeText(Home.this, "Failed !", Toast.LENGTH_SHORT).show();
                                        }

                                        @Override
                                        public void onFailure(Call<FCMResponse> call, Throwable t) {
                                            Log.e("ERROR", t.getMessage());

                                        }
                                    });


                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
    }

    private void requestPickupHere(String uid) {

        DatabaseReference dbRequest = FirebaseDatabase.getInstance().getReference(Common.pickup_request_tb1);
        GeoFire mGeoFire = new GeoFire(dbRequest);
        mGeoFire.setLocation(uid, new GeoLocation(mLastLocation.getLatitude(), mLastLocation.getLongitude()), new GeoFire.CompletionListener() {
            @Override
            public void onComplete(String key, DatabaseError error) {
                if (mUserMarker.isVisible())
                    mUserMarker.remove();
                //adding new marker
                mUserMarker = mMap.addMarker(new MarkerOptions()
                        .title("Pickup Here")
                        .snippet("")
                        .position(new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude()))
                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)));

                mUserMarker.showInfoWindow();
                btnPickupRequest.setText("Getting your Bus Driver..");
            }
        });

        findDriver();

    }

    private void findDriver() {
        final DatabaseReference drivers = FirebaseDatabase.getInstance().getReference(Common.driver_tb1);
        GeoFire gfDrivers = new GeoFire(drivers);

        final GeoQuery geoQuery = gfDrivers.queryAtLocation(new GeoLocation(mLastLocation.getLatitude(), mLastLocation.getLongitude()),
                radius);

        geoQuery.removeAllListeners();
        geoQuery.addGeoQueryEventListener(new GeoQueryEventListener() {
            @Override
            public void onKeyEntered(String key, GeoLocation location) {

                //If found
                if (!Common.isDriverFound) {
                    Common.isDriverFound = true;
                    Common.driverId = key;
                    btnPickupRequest.setText("CALL DRIVER");
                    //Toast.makeText(Home.this, "", Toast.LENGTH_SHORT).show();
                }

            }

            @Override
            public void onKeyExited(String key) {

            }

            @Override
            public void onKeyMoved(String key, GeoLocation location) {

            }

            @Override
            public void onGeoQueryReady() {

                //if drivers is not found , increase the radius
                if (!Common.isDriverFound && radius < LIMIT) {
                    radius++;
                    findDriver();
                } else {

                    if (!Common.isDriverFound) {

                        Toast.makeText(Home.this, "No Driver is Available Near  you !", Toast.LENGTH_SHORT).show();
                        btnPickupRequest.setText("REQUEST PICKUP");
                        geoQuery.removeAllListeners();
                    }

                }

            }

            @Override
            public void onGeoQueryError(DatabaseError error) {

            }
        });

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSION_REQUEST_CODE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    setUpLocation();
                }
                break;

            case 10:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)

                    Toast.makeText(this, "SOS SENT", Toast.LENGTH_SHORT).show();

                return;

        }

    }

    private void setUpLocation() {
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            //request runtime permission
            ActivityCompat.requestPermissions(this, new String[]{
                    android.Manifest.permission.ACCESS_COARSE_LOCATION,
                    android.Manifest.permission.ACCESS_FINE_LOCATION
            }, MY_PERMISSION_REQUEST_CODE);
        } else {
                buildLocationCallBack();
                createLocationRequest();
                displayLocation();

            }
    }

    private void buildLocationCallBack() {
        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                Common.mLastLocation1 = locationResult.getLocations().get(locationResult.getLocations().size() - 1);
                displayLocation();
            }
        };
    }

    private void displayLocation() {
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        fusedLocationProviderClient.getLastLocation().addOnSuccessListener(new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(Location location) {
                Common.mLastLocation1 = location;


                if (Common.mLastLocation1 != null) {

                    //create Latlng from mlastlocaiton and this is center point
                    LatLng center = new LatLng(Common.mLastLocation1.getLatitude(), Common.mLastLocation1.getLongitude());
                    //distance in meter
                    //heading 0 is northside ,90 is east 180 is south and 270 is west
                    //base on compact
                    LatLng northSide = SphericalUtil.computeOffset(center, 10000, 0);
                    LatLng southSide = SphericalUtil.computeOffset(center, 10000, 180);

                    LatLngBounds bounds = LatLngBounds.builder()
                            .include(northSide)
                            .include(southSide)
                            .build();


                    place_location.setBoundsBias(bounds);
                    place_location.setFilter(typeFilter);


                    place_destination.setBoundsBias(bounds);
                    place_destination.setFilter(typeFilter);


                    //presence system
                    driversAvailable = FirebaseDatabase.getInstance().getReference(Common.driver_tb1);
                    driversAvailable.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            //if any changes from driver table the we will reload all driver savailable
                            loadAllAvailableDriver(new LatLng(Common.mLastLocation1.getLatitude(), Common.mLastLocation1.getLongitude()));
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });

                    final double latitude = Common.mLastLocation1.getLatitude();
                    final double longitude = Common.mLastLocation1.getLongitude();


                    loadAllAvailableDriver(new LatLng(Common.mLastLocation1.getLatitude(), Common.mLastLocation1.getLongitude()));

                    Log.d("knowsnoalgo", String.format("Your loaction was changed: %f %f", latitude, longitude));

                } else {
                    Log.d("ERROR", "Cannot get your location");
                }
            }
        });
    }

    private void loadAllAvailableDriver(final LatLng location) {

        //Add marker
        //here we wil  clear all map to delte old position

        mMap.clear();
        mUserMarker = mMap.addMarker(new MarkerOptions()
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.marker))
                .position(location)
                .title("Your Location"));
        //move camera to this position
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(location, 15.0f));


        //Load all available drivers in 3 km distance
        DatabaseReference driverLocation = FirebaseDatabase.getInstance().getReference(Common.driver_tb1);
        GeoFire gf = new GeoFire(driverLocation);

        GeoQuery geoQuery = gf.queryAtLocation(new GeoLocation(location.latitude, location.longitude), distance);
        geoQuery.removeAllListeners();

        geoQuery.addGeoQueryEventListener(new GeoQueryEventListener() {
            @Override
            public void onKeyEntered(String key, final GeoLocation location) {
                //Using key to get email from table Users
                //Table users is table when drivers register account and update information
                FirebaseDatabase.getInstance().getReference(Common.user_driver_tb1)
                        .child(key)
                        .addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                //because rider and user model is same properties
                                //so we can use Rider model to get User here
                                Rider rider = dataSnapshot.getValue(Rider.class);

                                //Add driver to map
                                mMap.addMarker(new MarkerOptions()
                                        .position(new LatLng(location.latitude, location.longitude))
                                        .flat(true)
                                        .snippet("Phone :")
                                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.car)));

                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {

                            }
                        });
            }

            @Override
            public void onKeyExited(String key) {

            }

            @Override
            public void onKeyMoved(String key, GeoLocation location) {

            }

            @Override
            public void onGeoQueryReady() {
                if (distance <= LIMIT) { //distance find upto  3 km
                    distance++;
                    loadAllAvailableDriver(location);

                }

            }

            @Override
            public void onGeoQueryError(DatabaseError error) {

            }
        });
    }


    private void createLocationRequest() {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(UPDATE_INTERVAL);
        mLocationRequest.setFastestInterval(FATEST_INTERVAL);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setSmallestDisplacement(DISPLACEMENT);
    }


    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.home, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_signOut) {

            signout();

        } else if (id == R.id.nav_updateInformation) {
            showUpdateInformationDialog();
        } else if (id == R.id.nav_sosMenu) {
            sosMenu();
        }

        return true;
    }

    private void sosMenu() {

        final AlertDialog.Builder dialog = new AlertDialog.Builder(Home.this);
        dialog.setTitle("SOS");
       // dialog.setMessage("SOS");

        LayoutInflater inflater = this.getLayoutInflater();
        View send_sos = inflater.inflate(R.layout.sos,null);


        final Button smsbutton = send_sos.findViewById(R.id.smsButton);


        dialog.setView(send_sos);


        smsbutton.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.M)
            @Override
            public void onClick(View view) {
                sendSos();
            }
        });

        dialog.setView(send_sos);


        dialog.setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        });

        dialog.show();


    }

    @RequiresApi(api = Build.VERSION_CODES.M)

    private void sendSos() {

        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                lat = location.getLatitude();
                lon = location.getLongitude();

                loc.append(message);

                loc.append("http://maps.google.com?q=");
                loc.append(lat);
                loc.append(",");
                loc.append(lon);

                SmsManager sms = SmsManager.getDefault();
                sms.sendTextMessage(phoneNo, null, loc.toString(), null, null);


            }

            @Override
            public void onStatusChanged(String s, int i, Bundle bundle) {

            }

            @Override
            public void onProviderEnabled(String s) {

            }

            @Override
            public void onProviderDisabled(String s) {

                Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                startActivity(intent);

            }

           /* @Override
            public void onStatusChanged(String s, int i, Bundle bundle) {

            }

            @Override
            public void onProviderEnabled(String s) {

            }

            @Override
            public void onProviderDisabled(String s) {

                Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                startActivity(intent);

            }*/
        };

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED) {


            //Changed By Abinash
            requestPermissions(new String[]{
                    Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.INTERNET, Manifest.permission.SEND_SMS
            }, 10);
            return;
        }

        configureButton();

    }

   /* @Override
     public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {

            case 10:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
                    configureButton();
                return;
        }
    }*/


    private void configureButton() {

        smsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {


                //Changed By Abinash
                if (ActivityCompat.checkSelfPermission(Home.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(Home.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

                    return;
                }
                locationManager.requestLocationUpdates("gps", 5000, 0, (android.location.LocationListener) locationListener);


            }
        });

    }





    private void signout() {

        //reset remember values
        Paper.init(this);
        Paper.book().destroy();

        FirebaseAuth.getInstance().signOut();
        Intent intent = new Intent(Home.this, MainActivity.class);
        startActivity(intent);
        finish();

    }

    private void showUpdateInformationDialog() {

        final AlertDialog.Builder dialog = new AlertDialog.Builder(Home.this);
        dialog.setTitle("Update Information");
        dialog.setMessage("Please fill full information");

        LayoutInflater inflater = this.getLayoutInflater();
        View update_info_layout = inflater.inflate(R.layout.layout_update_information1, null);

        final MaterialEditText edtName = update_info_layout.findViewById(R.id.edtName);
        final MaterialEditText edtPhone = update_info_layout.findViewById(R.id.edtPhone);
        final ImageView imgAvatar = update_info_layout.findViewById(R.id.imgAvatar);


        dialog.setView(update_info_layout);


        imgAvatar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                chooseImageAndUpload();
            }
        });

        dialog.setView(update_info_layout);

        //setButton
        dialog.setPositiveButton("UPDATE", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();

                final AlertDialog waitingDialog = new SpotsDialog(Home.this);
                waitingDialog.show();

                String name = edtName.getText().toString();
                String phone = edtPhone.getText().toString();

                Map<String, Object> update = new HashMap<>();
                if (!TextUtils.isEmpty(name)) {
                    update.put("name", name);
                }
                if (!TextUtils.isEmpty(phone)) {
                    update.put("phone", phone);
                }


                //update

                DatabaseReference riderInformation = FirebaseDatabase.getInstance().getReference(Common.user_rider_tb1);

                riderInformation.child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                        .updateChildren(update).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        waitingDialog.dismiss();
                        if (task.isSuccessful()) {
                            Toast.makeText(Home.this, "Information updated ", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(Home.this, "Information wasnt updated ", Toast.LENGTH_SHORT).show();
                        }
                    }
                });

            }
        });

        dialog.setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        });

        //show dialog
        dialog.show();


    }

    private void chooseImageAndUpload() {
        //Start intent to chose image
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), Common.PICK_IMAGE_REQUEST);

    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == Common.PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {


            Uri saveUri = data.getData();

            if (saveUri != null) {
                final ProgressDialog progressDialog = new ProgressDialog(this);
                progressDialog.setMessage("Uploading...");
                progressDialog.show();

                String imageName = UUID.randomUUID().toString();
                final StorageReference imageFolder = storageReference.child("images/" + imageName);
                imageFolder.putFile(saveUri)
                        .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                            @Override
                            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                progressDialog.dismiss();

                                imageFolder.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {

                                    @Override
                                    public void onSuccess(Uri uri) {
                                        //save url to user informaiton table
                                        Map<String, Object> update = new HashMap<>();
                                        update.put("avatarUrl", uri.toString());

                                        //Made Update
                                        DatabaseReference riderInformation = FirebaseDatabase.getInstance().getReference(Common.user_rider_tb1);

                                        riderInformation.child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                                                .updateChildren(update).addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                if (task.isSuccessful())

                                                    Toast.makeText(Home.this, "Avatar was uploaded ", Toast.LENGTH_SHORT).show();
                                                else

                                                    Toast.makeText(Home.this, "Avatar wasnt updated ", Toast.LENGTH_SHORT).show();
                                            }
                                        })
                                                .addOnFailureListener(new OnFailureListener() {
                                                    @Override
                                                    public void onFailure(@NonNull Exception e) {
                                                        Toast.makeText(Home.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                                                    }
                                                });


                                    }
                                });
                            }
                        }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                        double progress = (100.0 * taskSnapshot.getBytesTransferred() / taskSnapshot.getTotalByteCount());
                        progressDialog.setMessage("Uploaded " + progress + "%");
                    }
                });
            }
        }
    }



    @Override
    public void onMapReady(GoogleMap googleMap) {


        try {
            boolean isSuccess = googleMap.setMapStyle(
                    MapStyleOptions.loadRawResourceStyle(this, R.raw.my_map_style)
            );
            if (!isSuccess)
                Log.e("ERROR", "Map style load failed!!!");
        } catch (Resources.NotFoundException ex) {

            ex.printStackTrace();
        }


        mMap = googleMap;
        mMap.getUiSettings().setZoomControlsEnabled(true);
        mMap.getUiSettings().setZoomControlsEnabled(true);
        mMap.setInfoWindowAdapter(new CustomInfoWindow(this));

        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                //First ,Check marker destination
                //If is not null , just remove available marker
                if (markerDestination != null)
                    markerDestination.remove();
                markerDestination = mMap.addMarker(new MarkerOptions()
                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.destination_marker))
                        .position(latLng)
                        .title("Destination"));

                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15.0f));
                //show  bottom sheet

                BottomSheetRiderFragment mBottomSheet = BottomSheetRiderFragment.newInstance(String.format("%f,%f", mLastLocation.getLatitude(), mLastLocation.getLongitude()),
                        String.format("%f,%f", latLng.latitude, latLng.longitude),
                        true);
                Home.super.onPostResume();
                mBottomSheet.show(getSupportFragmentManager(), mBottomSheet.getTag());
            }
        });

        if (ActivityCompat.checkSelfPermission(Home.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(Home.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            return;
        }
        fusedLocationProviderClient.requestLocationUpdates(mLocationRequest, locationCallback, Looper.myLooper());


    }

}
