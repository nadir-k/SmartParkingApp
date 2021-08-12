package com.example.smartparking;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.UriMatcher;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.INotificationSideChannel;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.directions.route.AbstractRouting;
import com.directions.route.Route;
import com.directions.route.RouteException;
import com.directions.route.Routing;
import com.directions.route.RoutingListener;
import com.example.smartparking.Model.Space;
import com.example.smartparking.Util.DirectionsParser;
import com.example.smartparking.Util.FirestoreDBQueries;
import com.example.smartparking.Util.URLRequest;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.PhotoMetadata;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.model.TypeFilter;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.android.libraries.places.widget.AutocompleteSupportFragment;
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.database.core.Tag;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Array;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import static android.content.ContentValues.TAG;

public class SetDestinationActivity extends FragmentActivity implements OnMapReadyCallback {
    private GoogleMap mMap;
    private static final int LOCATION_REQUEST = 500;

    ImageButton getDirections, cancelRoute, goBackToMaps, relocateMe;

    ArrayList<LatLng> listPoints;

    private List<Marker> allMarkers;
    private List<Polyline> allPolylines;

    AutocompleteSupportFragment end;

    private URLRequest requester;

    private FirestoreDBQueries database;
    private HashMap<String, Integer> markers;

    private FusedLocationProviderClient fusedLocationClient;

    private LatLng currentLocation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.set_destination);

        getDirections = (ImageButton) findViewById(R.id.getDirections);
        cancelRoute = (ImageButton) findViewById(R.id.cancel_route);
        goBackToMaps = (ImageButton) findViewById(R.id.backToMainMap);
        relocateMe = (ImageButton) findViewById(R.id.relocateMe);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        listPoints = new ArrayList<>();
        allMarkers = new ArrayList<>();
        allPolylines = new ArrayList<>();
        requester = new URLRequest();
        database = new FirestoreDBQueries();
        markers = new HashMap<String, Integer>();
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);


        /**
         *
         * Title: Add a Place Autocomplete widget to your Android app - Geocasts
         * Author: Google Maps Platform
         * Date: 6 Mar 2020
         * Version: 1.0
         * Availability: https://developers.google.com/maps/documentation/places/android-sdk/autocomplete
         *
         */
        Places.initialize(getApplicationContext(), getResources().getString(R.string.google_api_key));
        PlacesClient placesClient = Places.createClient(this);

        end = (AutocompleteSupportFragment) getSupportFragmentManager().findFragmentById(R.id.autocomplete_fragment_finish);

        end.setTypeFilter(TypeFilter.ESTABLISHMENT)
                .setCountries("UK")
                .setPlaceFields(Arrays.asList(Place.Field.ID, Place.Field.NAME, Place.Field.LAT_LNG))
                .setOnPlaceSelectedListener(new PlaceSelectionListener() {
                    @Override
                    public void onPlaceSelected(@NonNull Place place) {
                        mMap.animateCamera(CameraUpdateFactory.newLatLng(place.getLatLng()));
                    }

                    @Override
                    public void onError(@NonNull Status status) {
                        Log.i(TAG, "An error occurred: " + status);
                    }
                });
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {

        //setting up the variables for the map like the coordinates
        mMap = googleMap;
        mMap.setMapType(GoogleMap.MAP_TYPE_TERRAIN);

        mMap.getUiSettings().setZoomControlsEnabled(true);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(this, new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        if(location != null){
                            mMap.moveCamera(CameraUpdateFactory.newLatLng(new LatLng(location.getLatitude(), location.getLongitude())));
                            currentLocation = new LatLng(location.getLatitude(), location.getLongitude());
                            setRouteSpaces(currentLocation);
                            setRouteCarPark(currentLocation);

                            relocateMe.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    mMap.animateCamera(CameraUpdateFactory.newLatLng(new LatLng(location.getLatitude(), location.getLongitude())));
                                }
                            });
                        }
                    }
                });


        mMap.setMinZoomPreference(15f);

        markers = new HashMap<String, Integer>();
        database.retrieveAllCarparks(mMap, markers);

        /**
         *
         * Title: Android tutorial: How to get directions between 2 points using Google Map API
         * Author: tori san
         * Date: 18 Dec 2017
         * Version: 1.0
         * Availability: https://www.youtube.com/watch?v=jg1urt3FGCY&t=748s
         *
         */
        if(listPoints.isEmpty()){
            cancelRoute.setVisibility(View.INVISIBLE);
            cancelRoute.setClickable(false);

        } else {
            cancelRoute.setVisibility(View.VISIBLE);
            cancelRoute.setClickable(true);

            cancelRoute.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    routeCancel();
                }
            });
        }

        goBackToMaps.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(v.getContext(), MapsActivity.class);
                startActivity(intent);
            }
        });

        getDirections.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(listPoints.isEmpty()){
                    Toast.makeText(v.getContext(), "No destination set!", Toast.LENGTH_SHORT).show();
                } else {
                    openGoogleMaps(listPoints.get(1).latitude, listPoints.get(1).longitude);
                }

            }
        });
    }

    /**
     *
     * Title: Open standard Google Maps application from my application
     * Author: David Thompson
     * Date: 02 Nov 2018
     * Version: 1.0
     * Availability: https://stackoverflow.com/questions/6205827/how-to-open-standard-google-map-application-from-my-application
     *
     */
    public void openGoogleMaps(double endLat, double endLon){
        String uri = "http://maps.google.com/maps?daddr=" + endLat + "," + endLon;

        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
        intent.setPackage("com.google.android.apps.maps");
        startActivity(intent);
    }

    /**
     *
     * Title: Android tutorial: How to get directions between 2 points using Google Map API
     * Author: tori san
     * Date: 18 Dec 2017
     * Version: 1.0
     * Availability: https://www.youtube.com/watch?v=jg1urt3FGCY&t=748s
     *
     */
    public void setRouteCarPark(LatLng start){
        mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(@NonNull Marker marker) {
                for(String s : markers.keySet()){

                    if(marker.getId().equals(s)){
                        if(listPoints.size() == 2){
                            listPoints.clear();
                            mMap.clear();
                        }

                        cancelRoute.setVisibility(View.VISIBLE);
                        cancelRoute.setClickable(true);

                        cancelRoute.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                routeCancel();
                            }
                        });

                        listPoints.add(start);
                        MarkerOptions options = new MarkerOptions();
                        options.position(start).title("START");

                        if(listPoints.size() == 1){
                            options.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE));
                        }

                        allMarkers.add(mMap.addMarker(options));

                        LatLng selectedSpace = new LatLng(marker.getPosition().latitude, marker.getPosition().longitude);
                        listPoints.add(selectedSpace);
                        MarkerOptions options2 = new MarkerOptions();

                        options2.position(selectedSpace).title("DESTINATION");
                        allMarkers.add(mMap.addMarker(options2));
                        allMarkers.get(1).showInfoWindow();

                        if(listPoints.size() > 1){
                            String url = requester.getRequestUrl(listPoints.get(0), listPoints.get(1), getResources().getString(R.string.google_api_key));
                            TaskRequestDirections taskRequestDirections = new TaskRequestDirections();
                            taskRequestDirections.execute(url);
                        }
                    }
                }
                return false;
            }
        });
    }

    public void setRouteSpaces(LatLng start){
        if (getIntent().hasExtra("SELECTED_SPACE")) {
            Space space = getIntent().getParcelableExtra("SELECTED_SPACE");

            if(listPoints.size() == 2){
                listPoints.clear();
                mMap.clear();
            }

            cancelRoute.setVisibility(View.VISIBLE);
            cancelRoute.setClickable(true);

            cancelRoute.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    routeCancel();
                }
            });

            //start marker
            listPoints.add(start);
            MarkerOptions options = new MarkerOptions();
            options.position(start).title("START");

            if(listPoints.size() == 1){
                options.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE));
            }

            allMarkers.add(mMap.addMarker(options));

            //end marker
            LatLng selectedSpace = new LatLng(space.getLatitude(), space.getLongitude());
            listPoints.add(selectedSpace);
            MarkerOptions options2 = new MarkerOptions();

            options2.position(selectedSpace).title("DESTINATION");
            allMarkers.add(mMap.addMarker(options2));
            allMarkers.get(1).showInfoWindow();

            if(listPoints.size() > 1){
                String url = requester.getRequestUrl(listPoints.get(0), listPoints.get(1), getResources().getString(R.string.google_api_key));
                TaskRequestDirections taskRequestDirections = new TaskRequestDirections();
                taskRequestDirections.execute(url);
            }
        }
    }

    public void routeCancel(){
        listPoints.clear();

        for(Marker m : allMarkers){
            m.remove();
        }

        for(Polyline p : allPolylines){
            p.remove();
        }

        allMarkers.clear();
        allPolylines.clear();

        database.retrieveAllCarparks(mMap, markers);

        cancelRoute.setVisibility(View.INVISIBLE);
        cancelRoute.setClickable(false);
    }

    /**
     *
     * Title: Android tutorial: How to get directions between 2 points using Google Map API
     * Author: tori san
     * Date: 18 Dec 2017
     * Version: 1.0
     * Availability: https://www.youtube.com/watch?v=jg1urt3FGCY&t=748s
     *
     */

    @SuppressLint("MissingPermission")
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch (requestCode){
            case LOCATION_REQUEST:
                if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    mMap.setMyLocationEnabled(true);
                }
                break;
        }
    }

    /**
     *
     * Title: Android tutorial: How to get directions between 2 points using Google Map API
     * Author: tori san
     * Date: 18 Dec 2017
     * Version: 1.0
     * Availability: https://www.youtube.com/watch?v=jg1urt3FGCY&t=748s
     *
     */
    public class TaskRequestDirections extends AsyncTask<String, Void, String>{

        @Override
        protected String doInBackground(String... strings) {

            String responseString = "";
            try{
                responseString = requester.requestDirection(strings[0]);
            } catch (IOException e) {
                e.printStackTrace();
            }

            return responseString;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            TaskParser taskParser = new TaskParser();
            taskParser.execute(s);
        }
    }

    public class TaskParser extends AsyncTask<String, Void, List<List<HashMap<String, String>>>>{

        @Override
        protected List<List<HashMap<String, String>>> doInBackground(String... strings) {
            JSONObject jsonObject = null;
            List<List<HashMap<String, String>>> routes = null;

            try {
                jsonObject = new JSONObject(strings[0]);
                DirectionsParser directionsParser = new DirectionsParser();
                routes = directionsParser.parse(jsonObject);

            } catch (JSONException e){
                e.printStackTrace();
            }

            return routes;
        }

        @Override
        protected void onPostExecute(List<List<HashMap<String, String>>> lists) {
            super.onPostExecute(lists);

            ArrayList<LatLng> points = null;
            PolylineOptions polylineOptions = null;

            for(List<HashMap<String, String>> path : lists){
                points = new ArrayList<LatLng>();

                polylineOptions = new PolylineOptions();


                for(HashMap<String, String> point : path){

                    double lat = Double.parseDouble(point.get("lat"));
                    double lon = Double.parseDouble(point.get("lon"));

                    points.add(new LatLng(lat, lon));
                }

                polylineOptions.addAll(points);
                polylineOptions.width(15);
                polylineOptions.color(Color.BLUE);
                polylineOptions.geodesic(true);
            }

            if(polylineOptions!=null){
                Polyline p = mMap.addPolyline(polylineOptions);
                allPolylines.add(p);

            } else {
                Toast.makeText(getApplicationContext(), "DIRECTION NOT FOUND!", Toast.LENGTH_SHORT).show();
            }
        }
    }
}