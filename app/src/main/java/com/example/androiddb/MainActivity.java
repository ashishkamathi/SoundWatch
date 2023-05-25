package com.example.androiddb;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.media.MediaRecorder;
import android.os.Bundle;

import lecho.lib.hellocharts.model.Axis;
import lecho.lib.hellocharts.model.AxisValue;
import lecho.lib.hellocharts.model.Line;
import lecho.lib.hellocharts.model.LineChartData;
import lecho.lib.hellocharts.model.PointValue;
import lecho.lib.hellocharts.util.ChartUtils;
import lecho.lib.hellocharts.view.LineChartView;

import android.os.Environment;
import android.util.Log;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.Glide;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.model.PlaceLikelihood;
import com.google.android.libraries.places.api.model.RectangularBounds;
import com.google.android.libraries.places.api.net.FindCurrentPlaceRequest;
import com.google.android.libraries.places.api.net.FindCurrentPlaceResponse;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.Viewport;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;


import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;


public class MainActivity extends AppCompatActivity {
    private MediaRecorder mRecorder;
    private TextView mTextView;
    private String[] permissions = {Manifest.permission.RECORD_AUDIO, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.READ_EXTERNAL_STORAGE};
    private FusedLocationProviderClient mFusedLocationProviderClient;
    private String mPlaceName = "";

    private GraphView mGraphView;
    private LineGraphSeries<DataPoint> mDataSeries;
    private int mXValue = 0;
    double latitude;
    AlertDialog dialog;
    double longitude ;
    Date lastnoted;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);


        ActivityCompat.requestPermissions(this, permissions, 200);
        mTextView = findViewById(R.id.textView);
        mGraphView = findViewById(R.id.graph);
        mDataSeries = new LineGraphSeries<>(new DataPoint[]{new DataPoint(0, 0)});
        mGraphView.getViewport().setMinX(0);
        mGraphView.getGridLabelRenderer().setHorizontalAxisTitle("Time");
        mGraphView.getGridLabelRenderer().setVerticalAxisTitle("Decibel(dB)");
        mGraphView.getViewport().setMaxX(100);
        mGraphView.addSeries(mDataSeries);
        mRecorder = new MediaRecorder();
        mRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
        mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
        mRecorder.setOutputFile(getExternalCacheDir().getAbsolutePath() + "/audio.3gp");
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        Button button = findViewById(R.id.button);
        button.setOnClickListener(view -> {
            Intent intent = new Intent(this, CheckHistory.class);
            startActivity(intent);
        });


        builder.setTitle("Warning.!");
        builder.setMessage("Sound Level Exceeded 90 db");
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // close the dialog
                dialog.dismiss();
            }
        });
        dialog = builder.create();



// Get the device's current location
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        mFusedLocationProviderClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null)
                .addOnSuccessListener(this, new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        if (location != null) {
                            double latitude = location.getLatitude();
                            double longitude = location.getLongitude();
                            // Use latitude and longitude to get place name from Places API
                            Log.e("LatLong", " " + latitude + longitude);
                            getName(latitude,longitude);
                        }
                    }
                });


        try {
            mRecorder.prepare();
        } catch (IOException ioe) {
            Log.e("[Monkey]", "IOException: " +
                    Log.getStackTraceString(ioe));

        } catch (SecurityException e) {
            Log.e("[Monkey]", "SecurityException: " +
                    Log.getStackTraceString(e));
        }
        try {
            mRecorder.start();
        } catch (SecurityException e) {
            Log.e("[Monkey]", "SecurityException: " +
                    Log.getStackTraceString(e));
        }

        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                while (!Thread.currentThread().isInterrupted()) {
                    try {
                        Thread.sleep(400);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            double amplitude = mRecorder.getMaxAmplitude() / 90.0;
                            double decibel = 40 * Math.log10(amplitude);
                            if (decibel < 0) {
                                decibel = 0;
                            }
                            else if (decibel >=90) {

                                if(dialog.isShowing()){

                                }else{



                                    dialog.show();
                                }



                                Date currentTime = Calendar.getInstance().getTime();
                                if(lastnoted==null || Math.abs(currentTime.getTime() - lastnoted.getTime())>60000 ){
                                    lastnoted = currentTime;
                                    SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault());
                                    String dateTime = formatter.format(currentTime);
                                    getLocation();
                                    getName(latitude,longitude);
                                    String data = dateTime + "#" + mPlaceName + "#" + (int)decibel+"dB"+"#"+latitude+"#"+longitude+"\n";
                                    // Save the data to a file
                                    try {
                                        File file = new File(getExternalCacheDir().getAbsolutePath(), "data.txt");
                                        if (!file.exists()) {
                                            file.getParentFile().mkdirs(); // Ensure that the necessary directories leading up to the file are created
                                            file.createNewFile(); // Create the file if it does not exist
                                        }
                                        if (!file.canWrite()) {
                                            // Handle the case where the file is read-only
                                        }
                                        FileOutputStream fos = new FileOutputStream(file, true);
                                        fos.write(data.getBytes());
                                        fos.close();
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }

                                }



                                // Create a string to store the data locally

                            }
                            else if(decibel<90){
                                dialog.dismiss();

                            }
                            mTextView.setText(String.valueOf((int)decibel)+" dB");
                            mXValue++;
                            mDataSeries.appendData(new DataPoint(mXValue, decibel), true, 100);
                            mGraphView.getViewport().scrollToEnd();
                            mGraphView.addSeries(mDataSeries);



                        }
                    });
                }
            }
        });
        thread.start();
    }

    public String getName(double latitude, double longitude) {

        String url = "https://maps.googleapis.com/maps/api/geocode/json?latlng=" + latitude + "," + longitude + "&key=";
        RequestQueue queue = Volley.newRequestQueue(this);
        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                response -> {
                    try {
                        JSONObject json = new JSONObject(response);
                        String status = json.getString("status");
                        if (status.equals("OK")) {
                            JSONArray results = json.getJSONArray("results");
                            JSONObject result = results.getJSONObject(0);
                            mPlaceName = result.getString("formatted_address");
                            Log.e("Place Name",mPlaceName);



                        } else {
                            // Handle error
                        }
                    } catch (JSONException e) {
                        Log.e("PLace Error",e.toString());
                    }
                },
                error -> {
                    // Handle error
                });
        queue.add(stringRequest);

        return mPlaceName;
    }

    public void getLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        mFusedLocationProviderClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null)
                .addOnSuccessListener(this, new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        if (location != null) {
                             latitude = location.getLatitude();
                             longitude = location.getLongitude();
                            // Use latitude and longitude to get place name from Places API
                            Log.e("LatLong", " " + latitude + longitude);
                        }
                    }
                });
    }




    @Override
    protected void onDestroy() {
        super.onDestroy();
        mRecorder.stop();
        mRecorder.release();
        mRecorder = null;
    }
}