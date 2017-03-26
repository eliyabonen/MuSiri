package com.musiri.musiri;

import android.content.Context;
import android.content.Intent;
import android.provider.ContactsContract;
import android.speech.RecognizerIntent;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;

import java.util.ArrayList;
import java.util.Locale;

public class AudioControllerProxy
{
    private AppCompatActivity context;
    private AudioController audioController;

    // to send a "method pointer" to audiocontroller to make the buttons disappear when the playlist ends
    public interface hideButtonsInterface
    {
        void hideMusicButtons();
    }

    public AudioControllerProxy(AppCompatActivity context)
    {
        this.context = context;
        this.audioController = new AudioController(context);
    }

    // hide/show the music buttons
    public void showMusicButtons(boolean bool)
    {
        if(bool)
        {
            ((ImageView) context.findViewById(R.id.pausePlayImageView)).setVisibility(View.VISIBLE);
            ((ImageView) context.findViewById(R.id.stopImageView)).setVisibility(View.VISIBLE);
        }
        else
        {
            ((ImageView) context.findViewById(R.id.pausePlayImageView)).setVisibility(View.GONE);
            ((ImageView) context.findViewById(R.id.stopImageView)).setVisibility(View.GONE);
        }
    }

    // when the user clicked the Play/Pause button
    public void continuePauseMusic(View view)
    {
        if(audioController.isPlaying())
        {
            ((ImageView)view).setImageResource(R.mipmap.play_icon);
            audioController.pauseMusic();
        }
        else
        {
            ((ImageView)view).setImageResource(R.mipmap.pause_icon);
            audioController.continueMusic();
        }
    }

    // when the user clicked the Stop button
    public void stopMusic(View view)
    {
        audioController.stopMusic();
        showMusicButtons(false);
    }

    public void playSong(final String path)
    {
        audioController.playSong(path);
        showMusicButtons(true);
    }

    public void playPlaylist(final ArrayList<String> songs)
    {
        audioController.playPlaylist(songs, new hideButtonsInterface() {
            @Override
            public void hideMusicButtons()
            {
                showMusicButtons(false);
            }
        });
        showMusicButtons(true);
    }

    public AppCompatActivity getContext()
    {
        return context;
    }
}
