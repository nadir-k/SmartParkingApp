package com.example.smartparking;

import android.content.Intent;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import com.example.smartparking.Model.Preference;
import com.example.smartparking.Model.Space;
import com.example.smartparking.Util.DatabaseHelper;
import com.example.smartparking.Util.FirestoreDBQueries;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;

import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.List;


public class MapSpacesActivity extends FragmentActivity implements OnMapReadyCallback {
    private int carparkSelected, floorSelected;

    private TextView selectedFloorSpaces, spaceAvailable, spaceTaken, spaceDisabled;
    private TableLayout layout;
    private TableRow tr;

    private GoogleMap map;
    private LatLng loc;

    private Space spaceSelected;
    private FirebaseFirestore db;

    private FirestoreDBQueries database;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.map_spaces_available);

        database = new FirestoreDBQueries();
        MarkerOptions mo = new MarkerOptions();

        layout = (TableLayout) findViewById(R.id.tableSpacesAvailable);
        selectedFloorSpaces = (TextView) findViewById(R.id.floorSpace);
        TextView nameCarpark = (TextView) findViewById(R.id.namecarpark);
        spaceAvailable = (TextView) findViewById(R.id.spaceAvailable);
        spaceTaken = (TextView) findViewById(R.id.spaceTaken);
        spaceDisabled = (TextView) findViewById(R.id.disabled);
        ImageButton closeSpaceMap = (ImageButton) findViewById(R.id.closeSpacesOpenMap);
        ImageButton backToCarPark = (ImageButton) findViewById(R.id.backtocarpark);
        MaterialButton setRoute = (MaterialButton) findViewById(R.id.setRoute);

        int floorNum = getIntent().getIntExtra("EXTRA_FLOOR_NUMBER", 1);
        floorSelected = getIntent().getIntExtra("FLOOR_CHOSEN", 1);
        carparkSelected = getIntent().getIntExtra("EXTRA_DETAIL_ID", 1);

        database.getTitleOfCarparkInRealTime(carparkSelected, nameCarpark);
        getCarparkPos(carparkSelected);
        getFloorButtons(floorNum);

        selectedFloorSpaces.setText("Spaces on floor " + floorSelected);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);

        mapFragment.getMapAsync(this);

        closeSpaceMap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(v.getContext(), MapsActivity.class);
                startActivityForResult(intent, 0);
            }
        });

        backToCarPark.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(v.getContext(), CarparksActivity.class);
                intent.putExtra("EXTRA_DETAIL_ID", carparkSelected);
                startActivity(intent);
            }
        });

        setRoute.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                spaceSelection(v);
            }
        });
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {

        map = googleMap;

        DatabaseHelper.getInstance(this).getPreferenceRecordExist();

        database.getSpacesByFloorInRealTime(carparkSelected, floorSelected, spaceAvailable, spaceTaken, spaceDisabled);

        map.setMinZoomPreference(17.5f);

        googleMap.getUiSettings().setScrollGesturesEnabled(false);

        map.moveCamera(CameraUpdateFactory.newLatLng(loc));

        map.addMarker(
                new MarkerOptions()
                    .position(loc)
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_YELLOW))
        );
    }

    public void getCarparkPos(int identification){
        db = FirebaseFirestore.getInstance();

        db.collection("Carparks").document("carpark" + identification)
                .addSnapshotListener(new EventListener<DocumentSnapshot>() {
                    @Override
                    public void onEvent(@Nullable DocumentSnapshot value, @Nullable FirebaseFirestoreException error) {
                        if(error != null){
                            System.err.println("Listen failed: " + error);
                        }

                        if(value != null && value.exists()){
                            loc = new LatLng(value.getDouble("latitude"), value.getDouble("longitude"));
                        }
                    }
                });
    }



    public void getFloorButtons(int floors) {
        /**
         *
         * Title: Add Buttons to TableRow Dynamically
         * Author: Dipak Keshariya
         * Date: 30 May 2012
         * Version: 1.0
         * Availability: https://stackoverflow.com/questions/8700427/add-buttons-to-tablerow-dynamically
         *
         */
        int i = 0;

        while(i < floors){
            if(i % 3 == 0){
                tr = new TableRow(this);
                tr.setGravity(Gravity.CENTER_HORIZONTAL);
                layout.addView(tr);
            }

            int num = i;
            num += 1;

            TableRow.LayoutParams params = new TableRow.LayoutParams(
                    TableRow.LayoutParams.WRAP_CONTENT,
                    TableRow.LayoutParams.WRAP_CONTENT);

            params.setMargins(12, 12, 0,0);

            MaterialButton b = new MaterialButton(this);
            b.setLayoutParams(params);
            b.setText("Floor " + num);
            b.setId(i);

            db = FirebaseFirestore.getInstance();

            db.collection("Carparks/carpark" + carparkSelected + "/Info/spaces/Space")
                    .whereEqualTo("floornumber", num)
                    .addSnapshotListener(new EventListener<QuerySnapshot>() {
                        @Override
                        public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                            if(error != null){
                                System.err.println("Listen failed: " + error);
                            }

                            int available = 0;
                            List<Space> spaces = value.toObjects(Space.class);

                            for(int j=0; j < spaces.size(); j++){
                                if(spaces.get(j).isAvailable()){
                                    available++;
                                    if(available > 0){
                                        b.setBackgroundTintList(ContextCompat.getColorStateList(getApplicationContext(), R.color.quantum_googgreen));
                                    }
                                }

                                if(available == 0){
                                    b.setBackgroundTintList(ContextCompat.getColorStateList(getApplicationContext(), R.color.quantum_googred));
                                }
                            }
                        }
                    });

            int finalI = i;
            b.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int selected = finalI + 1;
                    selectedFloorSpaces.setText("Spaces on floor " + selected);

                    database.getSpacesByFloorInRealTime(carparkSelected, selected, spaceAvailable, spaceTaken, spaceDisabled);

                    b.setBackgroundTintList(b.getBackgroundTintList().withAlpha(255));
                }
            });

            tr.addView(b);
            i++;

        }
    }

    public void spaceSelection(View v){
        //If there is a record in the database
        if(!DatabaseHelper.getInstance(this).getPreferenceRecordExist()){
            Preference preference = DatabaseHelper.getInstance(this).getPreference(1);

            if(preference.getDisabledSpace() == 1){
                getDisabledSpaces(v);

            } else if(preference.getDisabledSpace() == 1 && preference.getLowestFloor() == 1){
                getDisabledAndLowestFloorSpace(v);

            } else if(preference.getLowestFloor() == 1 && preference.getDisabledSpace() == 0){
                getGeneralSpace(v);
            }

            //If there isn't a record in the database
        } else if(DatabaseHelper.getInstance(this).getPreferenceRecordExist()){
            getGeneralSpace(v);
        }
    }

    public void getDisabledSpaces(View v){
        db = FirebaseFirestore.getInstance();

        db.collection("Carparks/carpark" + carparkSelected + "/Info/spaces/Space")
                .whereEqualTo("disabled", true)
                .whereEqualTo("available", true)
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                        int foundI = 0;
                        if(error != null){
                            System.err.println("Listen failed: " + error);
                        }

                        List<Space> spaces = value.toObjects(Space.class);

                        if(spaces.size() != 0){

                                spaceSelected = new Space(spaces.get(0).isAvailable(),
                                        spaces.get(0).getCarparkid(),
                                        spaces.get(0).isDisabled(),
                                        spaces.get(0).getFloornumber(),
                                        spaces.get(0).getLatitude(),
                                        spaces.get(0).getLongitude());

                                Intent chooseSpace = new Intent(v.getContext(), SetDestinationActivity.class);
                                chooseSpace.putExtra("SELECTED_SPACE", spaceSelected);
                                chooseSpace.putExtra("CAPARK_CHOSEN", carparkSelected);
                                startActivityForResult(chooseSpace, 0);

                        } else {
                            getGeneralSpace(v);
                            Toast.makeText(getApplicationContext(), "No disabled spaces were found, searching for lowest floor", Toast.LENGTH_LONG)
                                    .show();
                        }
                    }
                });
    }

    public void getDisabledAndLowestFloorSpace(View v){
        db = FirebaseFirestore.getInstance();

        db.collection("Carparks/carpark" + carparkSelected + "/Info/spaces/Space")
                .whereEqualTo("available", true)
                .whereEqualTo("disabled", true)
                .orderBy("floornumber", Query.Direction.ASCENDING)
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                        if(error != null){
                            System.err.println("Listen failed: " + error);
                        }

                        List<Space> spaces = value.toObjects(Space.class);

                        if(spaces.size() != 0){

                                spaceSelected = new Space(spaces.get(0).isAvailable(),
                                        spaces.get(0).getCarparkid(),
                                        spaces.get(0).isDisabled(),
                                        spaces.get(0).getFloornumber(),
                                        spaces.get(0).getLatitude(),
                                        spaces.get(0).getLongitude());

                                Intent chooseSpace = new Intent(v.getContext(), SetDestinationActivity.class);
                                chooseSpace.putExtra("SELECTED_SPACE", spaceSelected);
                                chooseSpace.putExtra("CAPARK_CHOSEN", carparkSelected);
                                startActivityForResult(chooseSpace, 0);

                        } else {
                            getGeneralSpace(v);
                            Toast.makeText(getApplicationContext(), "No disabled spaces were found, searching for lowest floor", Toast.LENGTH_LONG)
                                    .show();
                        }
                    }
                });
    }

    public void getGeneralSpace(View v){
        db = FirebaseFirestore.getInstance();

        db.collection("Carparks/carpark" + carparkSelected + "/Info/spaces/Space")
                .whereEqualTo("available", true)
                .whereEqualTo("disabled", false)
                .orderBy("floornumber", Query.Direction.ASCENDING)
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                        if(error != null){
                            System.err.println("Listen failed: " + error);
                        }

                        List<Space> spaces = value.toObjects(Space.class);

                        if(spaces.size() != 0){
                            spaceSelected = new Space(spaces.get(0).isAvailable(),
                                    spaces.get(0).getCarparkid(),
                                    spaces.get(0).isDisabled(),
                                    spaces.get(0).getFloornumber(),
                                    spaces.get(0).getLatitude(),
                                    spaces.get(0).getLongitude());

                            Intent chooseSpace = new Intent(v.getContext(), SetDestinationActivity.class);
                            chooseSpace.putExtra("SELECTED_SPACE", spaceSelected);
                            chooseSpace.putExtra("CAPARK_CHOSEN", carparkSelected);
                            startActivityForResult(chooseSpace, 0);

                        } else {
                            Toast.makeText(getApplicationContext(), "No spaces are available, try again later or view a different carpark", Toast.LENGTH_LONG)
                                    .show();
                        }
                    }
                });
    }
}