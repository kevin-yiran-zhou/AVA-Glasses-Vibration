package com.example.CoulterGlassesDebug;

import android.annotation.SuppressLint;
import android.content.ComponentName;
import android.content.Context;
import android.media.AudioAttributes;
import android.media.MediaPlayer;
import android.media.RemoteControlClient;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;

import androidx.appcompat.app.AppCompatActivity;
import androidx.media.session.MediaButtonReceiver;

public class MetadataManager {
    private static MetadataManager instance;

    MediaSessionCompat mediaSession;
    private MediaPlayer silentPlayer;
    private AppCompatActivity activityContext;
    private static final String[] METADATA_KEYS = new String[]{
            MediaMetadataCompat.METADATA_KEY_TITLE,
            MediaMetadataCompat.METADATA_KEY_ARTIST,
            MediaMetadataCompat.METADATA_KEY_ALBUM,
            MediaMetadataCompat.METADATA_KEY_AUTHOR,
            MediaMetadataCompat.METADATA_KEY_COMPOSER,
            MediaMetadataCompat.METADATA_KEY_WRITER
    };
    private int currentKeyIndex = 0;
    MetadataManager(AppCompatActivity context){
        this.activityContext = context;
        ComponentName mediaButtonReceiver = new ComponentName(context, MediaButtonReceiver.class);
        mediaSession = new MediaSessionCompat(context, "AVA-Android", mediaButtonReceiver, null);
        mediaSession.setFlags(MediaSessionCompat.FLAG_HANDLES_MEDIA_BUTTONS | 
                             MediaSessionCompat.FLAG_HANDLES_TRANSPORT_CONTROLS);
        
        // Play silent audio to establish Bluetooth audio route for AVRCP on Android 16
        startSilentAudio();
    }
    
    private void startSilentAudio() {
        try {
            silentPlayer = MediaPlayer.create(activityContext, R.raw.stop);
            if (silentPlayer != null) {
                AudioAttributes audioAttributes = new AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .build();
                silentPlayer.setAudioAttributes(audioAttributes);
                silentPlayer.setLooping(true);
                silentPlayer.setVolume(0.01f, 0.01f); // Very low volume
                silentPlayer.start();
            }
        } catch (Exception e) {
            // Ignore errors - the app will still work, just might not trigger AVRCP
        }
    }
    @SuppressLint("MissingPermission")
    void destroy() {
        if (silentPlayer != null) {
            silentPlayer.stop();
            silentPlayer.release();
            silentPlayer = null;
        }
        mediaSession.setActive(false);
        mediaSession.release();
    }
    public static MetadataManager getInstance(AppCompatActivity activity) {
        if (instance != null) {
            instance.destroy();
        }
        instance = new MetadataManager(activity);
        return instance;
    }
    public void send(String msg) {
        PlaybackStateCompat state = new PlaybackStateCompat.Builder()
                .setState(PlaybackStateCompat.STATE_PLAYING, 0, 1.0f)
                .build();
        mediaSession.setPlaybackState(state);
        //set the metadata to send, this is the text that will be displayed
        //if the strings are too long they might be cut off
        //you need to experiment with the receiving device to know max length
        String currentKey = METADATA_KEYS[currentKeyIndex];
        MediaMetadataCompat metadata = new MediaMetadataCompat.Builder()
                .putString(MediaMetadataCompat.METADATA_KEY_TITLE, msg)
                .build();
        //setting this active makes the metadata you pass show up
        //other metadata from apps will not be shown
        mediaSession.setActive(true);
        mediaSession.setMetadata(metadata);
        currentKeyIndex = (currentKeyIndex + 1) % METADATA_KEYS.length;
    }
}
