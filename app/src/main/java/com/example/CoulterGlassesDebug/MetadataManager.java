package com.example.CoulterGlassesDebug;

import android.annotation.SuppressLint;
import android.content.Context;
import android.media.MediaMetadata;
import android.media.RemoteControlClient;
import android.media.session.MediaSession;
import android.media.session.PlaybackState;

import androidx.appcompat.app.AppCompatActivity;

public class MetadataManager {
    private static MetadataManager instance;

    MediaSession mediaSession;
    MetadataManager(AppCompatActivity context){
        mediaSession = new MediaSession(context , "AVA-Android");
    }
    @SuppressLint("MissingPermission")
    void destroy() {
    }
    public static MetadataManager getInstance(AppCompatActivity activity) {
        if (instance != null) {
            instance.destroy();
        }
        instance = new MetadataManager(activity);
        return instance;
    }
    public void send(String msg) {
        PlaybackState state = new PlaybackState.Builder()
                .setState(PlaybackState.STATE_PLAYING, 0, 1.0f)
                .build();
        mediaSession.setPlaybackState(state);
        //set the metadata to send, this is the text that will be displayed
        //if the strings are too long they might be cut off
        //you need to experiment with the receiving device to know max length
        MediaMetadata metadata = new MediaMetadata.Builder()
                .putString(MediaMetadata.METADATA_KEY_TITLE, msg)
                .build();
        //setting this active makes the metadata you pass show up
        //other metadata from apps will not be shown
        mediaSession.setActive(true);
        mediaSession.setMetadata(metadata);
    }
}
