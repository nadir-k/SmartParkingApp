package com.example.smartparking.Util;

import android.widget.TextView;

import androidx.annotation.Nullable;

import com.example.smartparking.Model.Carparks;
import com.example.smartparking.Model.Space;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.HashMap;
import java.util.List;

public class FirestoreDBQueries {

    FirebaseFirestore db;

    public FirestoreDBQueries(){
    }

    public void getTitleOfCarparkInRealTime(int identification, TextView title){
        db = FirebaseFirestore.getInstance();

        db.collection("Carparks")
                .whereEqualTo("id", identification)
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                        if(error != null){
                            System.err.println("Listen failed: " + error);
                        }

                        for(DocumentSnapshot doc : value){
                            if(doc.getString("name") != null){
                                title.setText(doc.getString("name"));
                            }
                        }
                    }
                });
    }

    public void retrieveAllCarparks(GoogleMap map, HashMap<String, Integer> markersList){
        db = FirebaseFirestore.getInstance();

        db.collection("Carparks")
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                        if(error != null){
                            System.err.println("Listen failed:" + error);
                        }

                        List<Carparks> objects = value.toObjects(Carparks.class);

                        for(int i = 0; i < objects.size(); i++){
                            LatLng loc = new LatLng(objects.get(i).getLatitude(), objects.get(i).getLongitude());

                            MarkerOptions mo = new MarkerOptions()
                                    .position(loc)
                                    .title(objects.get(i).getName());

                            Marker m = map.addMarker(mo);
                            m.setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_YELLOW));

                            m.setTag(objects.get(i).getId());

                            markersList.put(m.getId(), objects.get(i).getId());
                        }
                    }
                });
    }


    public void getSpacesByFloorInRealTime(int carparkid, int floorNumber, TextView availableSpace, TextView takenSpace, TextView disabledSpace){
        final int[] numAvailable = {0};
        final int[] numTaken = {0};
        final int[] numDisabled = {0};

        db = FirebaseFirestore.getInstance();

        db.collection("Carparks/carpark" + carparkid + "/Info/spaces/Space")
                .whereEqualTo("floornumber", floorNumber)
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                        numAvailable[0] = 0;
                        numTaken[0] = 0;
                        numDisabled[0] = 0;

                        if(error != null){
                            System.err.println("Listen failed: " + error);
                        }

                        List<Space> spaces = value.toObjects(Space.class);
                        for(int i = 0; i < spaces.size(); i++){

                            if(spaces.get(i).isAvailable() && spaces.get(i).isDisabled()) {
                                numDisabled[0]++;
                            }

                            if(!spaces.get(i).isAvailable() && spaces.get(i).isDisabled()){
                                numTaken[0]++;
                            }

                            if(spaces.get(i).isAvailable() && !spaces.get(i).isDisabled()){
                                numAvailable[0]++;
                            }

                            if(!spaces.get(i).isAvailable() && !spaces.get(i).isDisabled()){
                                numTaken[0]++;
                            }
                        }

                        availableSpace.setText("Available: " + numAvailable[0]);
                        takenSpace.setText("Taken: " + numTaken[0]);
                        disabledSpace.setText("Disabled: " + numDisabled[0]);
                    }
                });
    }
}
