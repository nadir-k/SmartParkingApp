package com.example.smartparking;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.example.smartparking.Util.DatabaseHelper;

/**
 *
 * Title: Android tutorial: Choose starting Activity based on condition in Android
 * Author: earthw0mjim
 * Date: 03 Dec 2017
 * Version: 1.0
 * Availability: https://stackoverflow.com/questions/37730207/how-to-choose-starting-activity-based-on-condition-in-android
 *
 */
public class NavigatorActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //checks if there is a record
        boolean empty = DatabaseHelper.getInstance(this).getPreferenceRecordExist();
        Intent intent;

        if(!empty){
            intent = new Intent(NavigatorActivity.this, MapsActivity.class);
        } else {
            intent = new Intent(NavigatorActivity.this, SpacePreferenceActivity.class);
        }

        startActivity(intent);
        finish();
    }
}