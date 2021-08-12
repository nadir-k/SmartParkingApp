package com.example.smartparking;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.Toast;

import com.example.smartparking.Model.Preference;
import com.example.smartparking.Util.DatabaseHelper;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.checkbox.MaterialCheckBox;

public class SpacePreferenceActivity extends AppCompatActivity {

    private MaterialCheckBox lowestFloor, disabledSpace;

    private int lowestF, disabledS;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_space_prefrerence);

        lowestFloor = (MaterialCheckBox) findViewById(R.id.lowestFloorLevel);
        disabledSpace = (MaterialCheckBox) findViewById(R.id.disabledSpace);

        ImageButton closePreference = (ImageButton) findViewById(R.id.preference_close);

        MaterialButton submit = (MaterialButton) findViewById(R.id.submitPreferences);

        lowestFloor.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(lowestFloor.isChecked()){
                    lowestF = 1;
                } else {
                    lowestF = 0;
                }
            }
        });

        disabledSpace.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(disabledSpace.isChecked()){
                    disabledS = 1;
                } else {
                    disabledS = 0;
                }
            }
        });

        closePreference.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent closeAndOpenMap = new Intent(v.getContext(), MapsActivity.class);
                startActivityForResult(closeAndOpenMap, 0);
            }
        });

        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                submitPreferences(v);
            }
        });

    }

    public void submitPreferences(View v){
        Preference preference = new Preference(lowestF, disabledS);

        boolean empty = DatabaseHelper.getInstance(this).getPreferenceRecordExist();

        if(empty){
            DatabaseHelper.getInstance(this).createPreference(preference);

            Intent submitAndOpenMap = new Intent(v.getContext(), MapsActivity.class);
            startActivityForResult(submitAndOpenMap, 0);

        } else {
            Preference p = DatabaseHelper.getInstance(this).getPreference(1);

            p.setLowestFloor(lowestF);
            p.setDisabledSpace(disabledS);


            DatabaseHelper.getInstance(this).updatePreference(p);

            Intent submitAndOpenMap = new Intent(v.getContext(), MapsActivity.class);
            startActivityForResult(submitAndOpenMap, 0);
        }

    }

}