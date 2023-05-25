package com.example.androiddb;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class CheckHistory extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_check_history);

        List<DataItem> dataItems = new ArrayList<>();


        try {
            File file = new File(getExternalCacheDir().getAbsolutePath(), "data.txt");
            FileInputStream fis = new FileInputStream(file);
            BufferedReader br = new BufferedReader(new InputStreamReader(fis));
            String line;
            while ((line = br.readLine()) != null) {

                String[] parts = line.split("#");
                if (parts.length >= 4) {
                    String dateTime = parts[0];
                    String placeName = parts[1];
                    String decibel =parts[2];
                    if(placeName!=""){
                        Log.d("FileContents", dataItems+","+placeName+","+decibel);
                        dataItems.add(new DataItem(dateTime, placeName, decibel));
                    }

                }
            }
            br.close();
        } catch (IOException e) {
            e.printStackTrace();
        }


        RecyclerView recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(new RecyclerView.Adapter() {
            @NonNull
            @Override
            public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_item, parent, false);
                return new ViewHolder(view);
            }

            @Override
            public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
                DataItem dataItem = dataItems.get(position);
                ViewHolder viewHolder = (ViewHolder) holder;
                viewHolder.dateTimeTextView.setText(dataItem.getDateTime());
                viewHolder.placeNameTextView.setText(dataItem.getPlaceName());
                viewHolder.decibelTextView.setText(String.valueOf(dataItem.getDecibel()));
            }

            @Override
            public int getItemCount() {
                return dataItems.size();
            }

        });

    }

    // Define a ViewHolder for the RecyclerView items
    private class ViewHolder extends RecyclerView.ViewHolder {
        TextView dateTimeTextView;
        TextView placeNameTextView;
        TextView decibelTextView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            dateTimeTextView = itemView.findViewById(R.id.tvDateTime);
            placeNameTextView = itemView.findViewById(R.id.tvPlaceName);
            decibelTextView = itemView.findViewById(R.id.tvDecibel);
        }
    }
}