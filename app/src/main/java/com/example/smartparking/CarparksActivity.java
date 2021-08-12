package com.example.smartparking;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import com.example.smartparking.Model.Space;
import com.example.smartparking.Model.detail;
import com.example.smartparking.Util.FirestoreDBQueries;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.List;

import static android.content.ContentValues.TAG;

public class CarparksActivity extends AppCompatActivity {
    private TextView carparkName, address1, address2, postcode, openingTime;
    private ImageButton closeCarpark;
    private TableLayout tableLayout;
    private TableRow tr;
    private LinearLayout layout;
    private FirebaseFirestore db;
    int id;
    FirestoreDBQueries database;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.map_carpark);

        carparkName = (TextView) findViewById(R.id.carparkname);
        address1 = (TextView) findViewById(R.id.address1);
        address2 = (TextView) findViewById(R.id.address2);
        postcode = (TextView) findViewById(R.id.postcode);
        openingTime = (TextView) findViewById(R.id.openingTime);
        closeCarpark = (ImageButton) findViewById(R.id.closeCarparkOpenMap);
        tableLayout = (TableLayout) findViewById(R.id.tableButtons);
        layout = (LinearLayout) findViewById(R.id.linLayout);

        id = getIntent().getIntExtra("EXTRA_DETAIL_ID" ,1);

        getDetailByCarparkInRealTime(id);
        database = new FirestoreDBQueries();
        database.getTitleOfCarparkInRealTime(id, carparkName);

        closeCarpark.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent closeAndOpenMap = new Intent(v.getContext(), MapsActivity.class);
                startActivityForResult(closeAndOpenMap, 0);
            }
        });
    }

    public void getFloorButtons(int floors){
        int i = 0;

        while (i < floors) {
            if(i % 3 ==0){
                tr = new TableRow(this);
                tableLayout.addView(tr);
            }

            int num = i;
            num += 1;

            TableRow.LayoutParams params = new TableRow.LayoutParams(
                    TableRow.LayoutParams.WRAP_CONTENT,
                    TableRow.LayoutParams.WRAP_CONTENT);

            params.setMargins(12, 12, 0,0);

            MaterialButton b = new MaterialButton(this);
            b.setPadding(10, 0, 0, 0);
            b.setLayoutParams(params);
            b.setText("Floor " + num);
            b.setId(i);

            db = FirebaseFirestore.getInstance();

            db.collection("Carparks/carpark" + id + "/Info/spaces/Space")
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
                    Log.d(TAG, "v.getId is - " + v.getId());

                    int n = finalI +1;

                    Intent intent = new Intent(getBaseContext(), MapSpacesActivity.class);
                    intent.putExtra("EXTRA_FLOOR_NUMBER", floors);
                    intent.putExtra("FLOOR_CHOSEN", n);
                    intent.putExtra("EXTRA_DETAIL_ID", id);
                    startActivity(intent);
                }
            });

            tr.addView(b);
            i++;
        }
    }

    public void getDetailByCarparkInRealTime(int identification){
        db = FirebaseFirestore.getInstance();

        db.collection("Carparks/carpark" + identification + "/Info").document("detail")
                .addSnapshotListener(new EventListener<DocumentSnapshot>() {
                    @Override
                    public void onEvent(@Nullable DocumentSnapshot value, @Nullable FirebaseFirestoreException error) {
                        if(error != null){
                            System.err.println("Listen failed: " + error);
                        }

                        if(value != null && value.exists()){
                            address1.setText(value.getString("address1"));
                            address2.setText(value.getString("address2"));
                            postcode.setText(value.getString("postcode"));
                            openingTime.setText(value.getString("openingtime") + "am - " + value.getString("closingtime") + "pm");

                            detail detail = value.toObject(detail.class);
                            getFloorButtons(detail.getNumberfloors());
                        }
                    }
                });
    }
}