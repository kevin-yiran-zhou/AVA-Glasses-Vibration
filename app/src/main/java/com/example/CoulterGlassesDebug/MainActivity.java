package com.example.CoulterGlassesDebug;

import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.util.Log;
import android.widget.SeekBar;
import android.widget.Switch;
import androidx.appcompat.app.AppCompatActivity;

import com.example.CoulterGlassesDebug.databinding.ActivityMainBinding;

public class MainActivity extends AppCompatActivity {
    public static MainActivity instance = null;
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

        instance = this;
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
}
