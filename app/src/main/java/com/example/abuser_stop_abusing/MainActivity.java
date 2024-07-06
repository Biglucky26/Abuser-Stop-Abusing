package com.example.abuser_stop_abusing;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import android.Manifest;
import android.content.pm.PackageManager;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.Toast;
import android.media.MediaRecorder;
import android.os.Handler;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.List;

public class MainActivity extends AppCompatActivity{

    private Spinner spinner;
    private final IntentFilter intentFilter = new IntentFilter();
    private String spinnerChoice;

    //Audio recording variables
    private static final int REQUEST_RECORD_AUDIO = 1;
    private static final String LOG_TAG = "AudioRecordTest";
    private String fileName = null;
    private MediaRecorder recorder = null;
    private Handler handler = new Handler();    //Handler class to export error logs for crash handling & debugging
    private boolean recording = false;
    private boolean emergencyPressed = false;   // variable to determine if emergency button is pressed
    private int recordingDuration = 20000;      //recording duration = 20s/ 20000ms
    private ImageButton emergencyButton;
    private Button stopRecordingButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        //assign spinner button
        final List<String> levels = Arrays.asList("Level 1", "Level 2", "Level 3");
        spinner = findViewById(R.id.spinner);

        ArrayAdapter adapter = new ArrayAdapter(this, R.layout.selectedspinneritem, levels);
        adapter.setDropDownViewResource(R.layout.spinneritem);

        spinner.setAdapter(adapter);

        //assign buttons to its corresponding IDs
        emergencyButton = findViewById(R.id.emergencyButton);           //assigns emergencyButton in the xml file to Java's counterpart
        stopRecordingButton = findViewById(R.id.stopRecordingButton);   //assigns stopRecordingButton in the xml file to Java's counterpart

        //code to determine what happens when emergencyButton is pressed
        emergencyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(MainActivity.this, "Saving recording...", Toast.LENGTH_SHORT).show();
                emergencyPressed = true;
            }
        });

        stopRecordingButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                recording = false;
                if (recorder != null) {
                    recorder.stop();
                    recorder.release();
                    recorder = null;

                    Toast.makeText(MainActivity.this, "Recording Stopped", Toast.LENGTH_SHORT).show();
                }
            }
        });

        findViewById(R.id.recordButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //check if recording permission is already granted
                if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
                    //if permission was not granted, app will ask for permission here
                    ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.RECORD_AUDIO}, REQUEST_RECORD_AUDIO);
                }
                else {
                    //start the 20s recording cycle
                    startRecordingCycle();
                }
            }
        });


        // Indicates a change in the Wi-Fi Direct status.
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION);

        // Indicates a change in the list of available peers.
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION);

        // Indicates the state of Wi-Fi Direct connectivity has changed.
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);

        // Indicates this device's details have changed.
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION);

        Button btn = findViewById(R.id.button);

        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(MainActivity.this, MainActivity2.class));
            }
        });

        WifiP2pManager manager = (WifiP2pManager) getSystemService(Context.WIFI_P2P_SERVICE);
        WifiP2pManager.Channel channel = manager.initialize(this, getMainLooper(), null);
    }

    @Override // overrides existing onRequestPermissionsResult to customize what happens
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults); // This line calls the superclass' onRequestPermissionsResult method.
        if (requestCode == REQUEST_RECORD_AUDIO) { // This line checks if the request code matches the microphone permission request.
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                //if permission is granted, recording cycle will start
                startRecordingCycle();
            }
            //if permission isn't granted, show a small notification bar to notify the user
            else {
                Toast.makeText(this, "Permission denied to record audio", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void startRecordingCycle() {
        recording = true;
        handler.post(recordingRunnable);
    }

    private Runnable recordingRunnable = new Runnable() {
        @Override
        public void run() {
            if (recording) {
                fileName = getExternalCacheDir().getAbsolutePath() + "/audiorecordtest.mp3";
                startRecording();

                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        if (recorder != null) {
                            stopRecording();

                            if (emergencyPressed) {
                                emergencyPressed = false;
                                saveRecording();
                            } else {
                                deleteRecording();
                            }

                            if (recording) {
                                handler.post(recordingRunnable);
                            }
                        }
                    }
                }, recordingDuration);
            }
        }
    };

    private void startRecording() {
        if (recorder != null)
            //releases content of recorder if there's any to prepare it for next cycle
            recorder.release();
        recorder = new MediaRecorder();
        recorder.setAudioSource(MediaRecorder.AudioSource.MIC);         //sets phone's microphone as recording source
        recorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);    //mp4 as audio file format
        recorder.setOutputFile(fileName);                               //sets fileName as the output file name
        recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);       //uses AAC encoder

        try {
            //Prepare the recording to start
            recorder.prepare();
            //start the microphone recording
            recorder.start();
            //announce the recording has started to the user
            Toast.makeText(this, "Recording started", Toast.LENGTH_SHORT).show();
        } catch (IOException e) {               //catch device error to prevent app crashing in case of microphone malfunction
            Log.e(LOG_TAG, "Prepare() failed: "+e.getMessage());    //print out error message
        } catch (IllegalStateException e) {     //catch the error that method is called unexpectedly to prevent app crashing
            Log.e(LOG_TAG, "Start() failed: "+e.getMessage());      //print out error message
            //Notify the user that there's an error to start the recording
            Toast.makeText(this, "Recording has failed to start", Toast.LENGTH_SHORT).show();
        }
    }

    private void stopRecording() {
        try {
            recorder.stop();
            recorder.release();
            recorder = null;
            Toast.makeText(this, "Recording stopped for this session", Toast.LENGTH_SHORT).show();
        } catch (IllegalStateException e) {
            Log.e(LOG_TAG, "Stop() failed: "+e.getMessage());
        }
    }

    private void saveRecording() {
        if (fileName != null) {
            // Defining a new file name and path to save the recording permanently
            String savedFileName = getExternalFilesDir(null).getAbsolutePath() + "/saved_recording_" + System.currentTimeMillis() + ".mp3";
            File tempFile = new File(fileName);
            File savedFile = new File(savedFileName);

            try {
                if (tempFile.exists()) {
                    // Moving the file to the new location
                    if (tempFile.renameTo(savedFile)) {
                        Toast.makeText(this, "Recording saved: " + savedFileName, Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(this, "Failed to save recording", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(this, "No temporary recording found", Toast.LENGTH_SHORT).show();
                }
            } catch (Exception e) {
                Toast.makeText(this, "Error saving recording: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(this, "File name is null", Toast.LENGTH_SHORT).show();
        }
    }

    private void deleteRecording() {
        if (fileName != null) {
            File file = new File(fileName);
            if (file.exists()) {
                if (file.delete()) {
                    Toast.makeText(this, "Recording deleted", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, "Failed to delete recording", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(this, "No recording found", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(this, "File name is null found", Toast.LENGTH_SHORT).show();
        }
    }
}