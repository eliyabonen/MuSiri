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
        if(mediaPlayer.isPlaying())
            return;

        mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);

        try {
            Uri uri = Uri.parse(path);
            mediaPlayer.setDataSource(context, uri);
            mediaPlayer.setOnPreparedListener(new SongPrepared());
            mediaPlayer.prepareAsync();

            guiButtons.showMusicButtons(true);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void playPlaylist(final ArrayList<String> songs)
    {
        if(mediaPlayer.isPlaying())
            return;

        currPlaylistCounter = 0;
        playlistSongs = songs;

        mediaPlayer.setOnCompletionListener(new SongCompleted());
        playSong(playlistSongs.get(currPlaylistCounter));
    }

    public void pauseMusic()
    {
        if(mediaPlayer.isPlaying())
            mediaPlayer.pause();
    }

    public void continueMusic()
    {
        if(!mediaPlayer.isPlaying())
            mediaPlayer.start();
    }

    public void stopMusic()
    {
        if(mediaPlayer.isPlaying())
        {
            mediaPlayer.stop();
            mediaPlayer.reset();

            guiButtons.showMusicButtons(false);
        }
    }

    public boolean isPlaying()
    {
        return mediaPlayer.isPlaying();
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

    private class SongPrepared implements MediaPlayer.OnPreparedListener
    {
        @Override
        public void onPrepared(MediaPlayer mp)
        {
            mp.start();
        }
    }

    private class SongCompleted implements MediaPlayer.OnCompletionListener
    {
        @Override
        public void onCompletion(MediaPlayer mp)
        {
            currPlaylistCounter++;

            if(currPlaylistCounter == playlistSongs.size())
            {
                mediaPlayer.stop();
                mediaPlayer.reset();

                guiButtons.showMusicButtons(false);

                return;
            }

            mediaPlayer.reset();
            mediaPlayer.setOnCompletionListener(this);

            playSong(playlistSongs.get(currPlaylistCounter));
        }
    }
}
