package com.example.smartparking.Util;

import android.util.Log;

import com.google.android.gms.maps.model.LatLng;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import static android.content.ContentValues.TAG;


/**
 *
 * Title: Android tutorial: How to get directions between 2 points using Google Map API
 * Author: tori san
 * Date: 18 Dec 2017
 * Version: 1.0
 * Availability: https://www.youtube.com/watch?v=jg1urt3FGCY&t=748s
 *
 */
public class URLRequest {

    public URLRequest(){
    }

    public String getRequestUrl(LatLng origin, LatLng dest, String k){
        String str_org = "origin=" + origin.latitude + "," + origin.longitude;

        String str_dest = "destination=" + dest.latitude + "," + dest.longitude;

        String sensor = "sensor=false";

        String mode = "mode=driving";

        String key = "key="+k;

        String param = str_org + "&" + str_dest + "&" + sensor + "&" + mode + "&" + key;

        String output = "json";

        String url = "https://maps.googleapis.com/maps/api/directions/" + output + "?" + param;

        Log.d(TAG, url);

        return url;
    }

    public String requestDirection(String reqUrl) throws IOException {
        String responseString = "";

        InputStream inputStream = null;
        HttpURLConnection httpURLConnection = null;

        try{
            URL url = new URL(reqUrl);
            httpURLConnection = (HttpURLConnection) url.openConnection();
            httpURLConnection.connect();

            inputStream = httpURLConnection.getInputStream();
            InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
            BufferedReader bufferedReader = new BufferedReader(inputStreamReader);

            StringBuffer stringBuffer = new StringBuffer();

            String line = "";

            while((line = bufferedReader.readLine()) != null){
                stringBuffer.append(line);
            }

            responseString = stringBuffer.toString();
            bufferedReader.close();
            inputStreamReader.close();

        } catch (Exception e){
            e.printStackTrace();

        } finally {

            if(inputStream != null){
                inputStream.close();
            }

            httpURLConnection.disconnect();
        }
        return responseString;
    }
}
