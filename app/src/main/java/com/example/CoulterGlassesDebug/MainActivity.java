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

public class MainActivity extends AppCompatActivity {
    private final static int REQUEST_CAMERA = 0;
    public static MainActivity instance = null;
    private SoundManager soundManager;
    private MetadataManager metadataManager;
    private ActivityMainBinding viewBinding;
    private final String LOG_TAG = "MAIN_LOG";
    private int targetId = 0;
    private static double TURN_THRESHOLD = 5;
    private Handler handler;
    private Runnable sendRunnable;

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

        EditText targetIdEt = (EditText) findViewById(R.id.atTargetID);
        targetIdEt.setText(targetId + "");
        targetIdEt.addTextChangedListener(new TextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {
                char[] newId = {'0'};
                s.getChars(0, s.length(), newId, 0);
                targetId = Character.getNumericValue(newId[0]);
            }
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}
        });

        // Initialize the Handler and Runnable to call sendToESP periodically
        handler = new Handler();
        sendRunnable = new Runnable() {
            @Override
            public void run() {
                sendToESP();  // Call sendToESP
                handler.postDelayed(this, 33); // Delay of ~33ms (~30 calls per second)
            }
        };
    }
    int num=0;
    @RequiresApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
    @SuppressLint("MissingPermission")
    public void sendToESP() {
        if (soundManager.timeSinceCompleted() > 0) {
            soundManager.play(R.raw.turn_left);
        }
//        metadataManager.send(num + " " + num + " " + num + " " + num );

        long currentMillis = System.currentTimeMillis();
        String timeString = String.valueOf(currentMillis);
        metadataManager.send(timeString);
        num+=255/30;
        num%=255;
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Start sending data when the activity is resumed
        handler.post(sendRunnable);
    }

    @Override
    protected void onPause() {
        super.onPause();
        // Stop sending data when the activity is paused
        handler.removeCallbacks(sendRunnable);
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
