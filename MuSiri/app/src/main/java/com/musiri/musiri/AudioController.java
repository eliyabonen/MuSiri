package com.musiri.musiri;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.IBinder;
import android.support.annotation.Nullable;

import java.io.IOException;
import java.util.ArrayList;

public class AudioController extends Service
{
    private MediaPlayer mediaPlayer;
    private int currPlaylistCounter = 0;
    private ArrayList<String> playlistSongs;
    private Context context;
    private GuiButtons guiButtons;
    private boolean isPaused = false;

    public AudioController(Context context)
    {
        mediaPlayer = new MediaPlayer();

        this.context = context;
    }

    public void setGuiButtons(GuiButtons guiButtons)
    {
        this.guiButtons = guiButtons;
    }

    public void playSong(final String path)
    {
        // if it's already playing a song or if it's paused then don't play the song
        if(mediaPlayer.isPlaying() || isPaused)
            return;

        // preparing the meidaplayer for playing the song
        try {
            Uri uri = Uri.parse(path);
            mediaPlayer.setDataSource(context, uri);
            mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
            mediaPlayer.setOnPreparedListener(new SongPrepared());
            mediaPlayer.prepareAsync();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void playPlaylist(final ArrayList<String> songs)
    {
        // if it's already playing a song then quit
        if(mediaPlayer.isPlaying())
            return;

        // initializing playlist settings
        currPlaylistCounter = 0;
        playlistSongs = songs;

        // play the songs
        mediaPlayer.setOnCompletionListener(new SongCompleted());
        playSong(playlistSongs.get(currPlaylistCounter));
    }

    public void pauseMusic()
    {
        if(!mediaPlayer.isPlaying())
            return;

        mediaPlayer.pause();
        isPaused = true;
    }

    public void continueMusic()
    {
        if(mediaPlayer.isPlaying())
            return;

        mediaPlayer.start();
        isPaused = false;
    }

    public void stopMusic()
    {
        if(!mediaPlayer.isPlaying() && !isPaused)
            return;

        mediaPlayer.stop();
        mediaPlayer.reset();
        isPaused = false;

        guiButtons.showMusicButtons(false);
    }

    public boolean isPlaying()
    {
        return mediaPlayer.isPlaying();
    }

    public boolean isPaused()
    {
        return isPaused;
    }

    private class SongPrepared implements MediaPlayer.OnPreparedListener
    {
        @Override
        public void onPrepared(MediaPlayer mp)
        {
            // if it's prepared then start it
            mp.start();

            // display the music buttons
            guiButtons.showMusicButtons(true);
        }
    }

    private class SongCompleted implements MediaPlayer.OnCompletionListener
    {
        @Override
        public void onCompletion(MediaPlayer mp)
        {
            // increasing the playlist song counter
            currPlaylistCounter++;

            // if the counter reached the number of songs to play
            if(currPlaylistCounter == playlistSongs.size())
            {
                mediaPlayer.stop();
                mediaPlayer.reset();

                guiButtons.showMusicButtons(false);

                return;
            }

            // playing the next song by reseting the mediaplayer and playing the next song in the list
            mediaPlayer.reset();
            mediaPlayer.setOnCompletionListener(this);

            playSong(playlistSongs.get(currPlaylistCounter));
        }
    }

    @Override
    public void onCreate()
    {
        super.onCreate();

        MediaPlayer player = MediaPlayer.create(this, Uri.parse("/storage/emulated/0/MuSiriMusic/Cinema.mp3"));
        player.start();
    }

    @Override
    public void onDestroy() {
        System.out.println("********DESTROYYYYYYYYYYYYYYYYYY");
        //player.stop();
        // player.release();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
