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

    private final Handler pwmHandler = new Handler();
    private boolean pwmRunning = false;
    private final Runnable pwmRunnable = new Runnable() {
        @Override
        public void run() {
            if (!pwmRunning || frequency <= 0) return;

            double T = 1.0 / frequency;
            long t1 = (long) (duty_cycle * T * 1000); // milliseconds
            long t2 = (long) ((1.0 - duty_cycle) * T * 1000);

            // Send signal during t1
            sendMotorSignal();

            // After t1, stop signal
            pwmHandler.postDelayed(() -> {
                sendNoSignal();  // Or skip sending if ESP maintains previous state
                pwmHandler.postDelayed(pwmRunnable, t2);
            }, t1);
        }
    };

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewBinding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(viewBinding.getRoot());
        getSupportActionBar().hide();
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);

        replaceDemoFragment(new cameraFragment());

        instance = this;
        soundManager = SoundManager.getInstance(this);
        metadataManager = MetadataManager.getInstance(this);

        setupSwitches();
        setupSeekBars();
    }
    private void setupSeekBars(){
        setupSeekBar(R.id.seekbar_motor_strength, 200, (progress, fromUser) -> {
            motor_strength = progress;
            restartPWM();
        });

        setupSeekBar(R.id.seekbar_frequency, 10, (progress, fromUser) -> {
            frequency = progress;
            restartPWM();
        });

        setupSeekBar(R.id.seekbar_duty_cycle, 100, (progress, fromUser) -> {
            duty_cycle = progress/100.0;
            restartPWM();
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

        setupSwitch(front_left_switch,0);
        setupSwitch(front_right_switch,1);
        setupSwitch(back_left_switch,2);
        setupSwitch(back_right_switch,3);
    }
    private void setupSwitch(Switch sw, int index) {
        sw.setOnCheckedChangeListener((buttonView, isChecked) -> {
            motor_status[index]=isChecked?1:0;
            restartPWM();
        });
    }
    private void sendMotorSignal() {
        String output = java.util.Arrays.stream(motor_status)
                .map(i -> i * motor_strength)
                .mapToObj(String::valueOf)
                .collect(java.util.stream.Collectors.joining(" "));
        metadataManager.send(output);
        Log.d(LOG_TAG, "SEND: " + output);
    }

    private void sendNoSignal() {
        String output = "0 0 0 0";
        metadataManager.send(output);
        Log.d(LOG_TAG, "SEND: " + output);
    }
    private void restartPWM() {
        pwmHandler.removeCallbacksAndMessages(null);
        if (frequency > 0 && duty_cycle > 0) {
            pwmRunning = true;
            pwmHandler.post(pwmRunnable);
        }
        Log.d(LOG_TAG, "Restart PWM: freq=" + frequency + "Hz, duty=" + duty_cycle);
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
        transaction.replace(R.id.fragment_container, fragment);
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
                replaceDemoFragment(new cameraFragment());
                break;
            // Handle other request codes if needed
        }
    }
}
