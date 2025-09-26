package com.example.CoulterGlassesDebug;

import static android.Manifest.permission.CAMERA;
import static android.Manifest.permission.RECORD_AUDIO;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;
import android.annotation.SuppressLint;
import android.bluetooth.BluetoothGattCharacteristic;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.PermissionChecker;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.example.CoulterGlassesDebug.databinding.ActivityMainBinding;

import edu.wpi.first.math.geometry.Transform3d;

import android.os.Handler;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {
    private final static int REQUEST_CAMERA = 0;
    public static MainActivity instance = null;
    private SoundManager soundManager;
    private MetadataManager metadataManager;
    private ActivityMainBinding viewBinding;
    private final String LOG_TAG = "MAIN_LOG";

    private int[] motor_status = {0,0,0,0};
    private int motor_strength=0;
    private int frequency=0;
    private double duty_cycle=0;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewBinding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(viewBinding.getRoot());
        getSupportActionBar().hide();
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

//        replaceDemoFragment(new cameraFragment());

        instance = this;
        soundManager = SoundManager.getInstance(this);
        metadataManager = MetadataManager.getInstance(this);

        setupSwitches();
        setupSeekBars();
    }
    private void setupSeekBars(){
        setupSeekBar(R.id.seekbar_motor_strength, 200, (progress, fromUser) -> {
            motor_strength = progress;
            sendESP();
        });

        setupSeekBar(R.id.seekbar_frequency, 10, (progress, fromUser) -> {
            frequency = progress;
            sendESP();
        });

        setupSeekBar(R.id.seekbar_duty_cycle, 100, (progress, fromUser) -> {
            duty_cycle = progress;
            sendESP();
        });

    }
    public interface OnSeekBarChanged {
        void onProgressChanged(int progress, boolean fromUser);
    }
    private void setupSeekBar(int seekBarId, int maxValue, OnSeekBarChanged callback) {
        SeekBar seekBar = findViewById(seekBarId);
        seekBar.setMax(maxValue);

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                callback.onProgressChanged(progress, fromUser);
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });
    }
    private void setupSwitches(){
        Switch front_left_switch = findViewById(R.id.front_left_switch),
            front_right_switch = findViewById(R.id.front_right_switch),
            back_left_switch = findViewById(R.id.back_left_switch),
            back_right_switch = findViewById(R.id.back_right_switch);

        setupSwitch(front_left_switch,3);
        setupSwitch(front_right_switch,2);
        setupSwitch(back_left_switch,1);
        setupSwitch(back_right_switch,0);
    }
    private void setupSwitch(Switch sw, int index) {
        sw.setOnCheckedChangeListener((buttonView, isChecked) -> {
            motor_status[index]=isChecked?1:0;
            sendESP();
        });
    }
    private void sendESP() {
        String output = java.util.Arrays.stream(motor_status)
                .mapToObj(String::valueOf)
                .collect(java.util.stream.Collectors.joining(" "));
        output+=" "+motor_strength+" "+frequency+" "+duty_cycle;
        metadataManager.send(output);
        Log.d(LOG_TAG, "SEND: " + output);
    }

    public void replaceDemoFragment(Fragment fragment) {
        int hasCameraPermission = PermissionChecker.checkSelfPermission(this, CAMERA);
        if (hasCameraPermission != PermissionChecker.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, CAMERA)) {
            }
            ActivityCompat.requestPermissions(
                    this,
                    new String[]{CAMERA, WRITE_EXTERNAL_STORAGE, RECORD_AUDIO},
                    REQUEST_CAMERA
            );
            return;
        }
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
//        transaction.replace(R.id.fragment_container, fragment);
        transaction.commitAllowingStateLoss();
    }

    @Override
    public void onRequestPermissionsResult(
            int requestCode,
            String[] permissions,
            int[] grantResults
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch (requestCode) {
            case REQUEST_CAMERA:
                int hasCameraPermission = PermissionChecker.checkSelfPermission(this, CAMERA);
                if (hasCameraPermission == PermissionChecker.PERMISSION_DENIED) {
                    return;
                }
//                replaceDemoFragment(new cameraFragment());
                break;
            // Handle other request codes if needed
        }
    }
}
