package com.musiri.musiri;

import android.content.Context;
import android.content.Intent;
import android.provider.ContactsContract;
import android.speech.RecognizerIntent;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;

import java.util.Locale;

public class GuiButtons
{
    private ImageView buttonPlayPause;
    private ImageView buttonStop;
    private AppCompatActivity context;
    private AudioController audioController;

    public GuiButtons(AppCompatActivity context, AudioController audioController)
    {
        buttonPlayPause = (ImageView) context.findViewById(R.id.pausePlayImageView);
        buttonStop = (ImageView) context.findViewById(R.id.stopImageView);

        this.context = context;
        this.audioController = audioController;
    }

    // hide/show the music buttons
    public void showMusicButtons(boolean bool)
    {
        if(bool)
        {
            buttonPlayPause.setVisibility(View.VISIBLE);
            buttonStop.setVisibility(View.VISIBLE);
        }
        else
        {
            buttonPlayPause.setVisibility(View.GONE);
            buttonStop.setVisibility(View.GONE);
        }
    }

    // when the user clicked the Speak button
    public void onSpeakButtonClick(View view)
    {
        // set it to the pause icon
        if(!audioController.isPaused())
            buttonPlayPause.setImageResource(R.mipmap.pause_icon);

        // creating the intent for the google speech api
        Intent speechRecognizerIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);

        speechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        speechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Start speaking");
        speechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1);
        speechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.ENGLISH);

        context.startActivityForResult(speechRecognizerIntent, 1);
    }

    // when the user clicked the Play/Pause button
    public void onPlayPauseButtonClick(View view)
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

    public AppCompatActivity getContext()
    {
        return context;
    }

    // when the user clicked the Stop button
    public void onStopButtonClick(View view)
    {
        audioController.stopMusic();
        showMusicButtons(false);
    }
}
