package audio_handling;

import android.app.Notification;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;

import java.io.IOException;
import java.util.ArrayList;

public class AudioController extends Service
{
    private MediaPlayer mediaPlayer;
    private int currPlaylistCounter = 0;
    private ArrayList<String> playlistSongs;
    private Context context;
    private boolean isPaused = false;

    private Object[] mSetForegroundArgs = new Object[1];
    private Object[] mStartForegroundArgs = new Object[2];

    public AudioController()
    {

    }

    public AudioController(Context context)
    {
        mediaPlayer = new MediaPlayer();

        this.playlistSongs = null;
        this.context = context;
    }

    public void playSong(final String path, AudioControllerProxy.hideButtonsInterface hideButtons)
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
            mediaPlayer.setOnCompletionListener(new SongCompleted(hideButtons));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void playPlaylist(final ArrayList<String> songs, AudioControllerProxy.hideButtonsInterface hideButtons)
    {
        // if it's already playing a song then quit
        if(mediaPlayer.isPlaying())
            return;

        // initializing playlist settings
        currPlaylistCounter = 0;
        playlistSongs = songs;

        // play the songs
        playSong(playlistSongs.get(currPlaylistCounter), hideButtons);
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
        }
    }

    private class SongCompleted implements MediaPlayer.OnCompletionListener
    {
        private AudioControllerProxy.hideButtonsInterface hideButtons;

        public SongCompleted(AudioControllerProxy.hideButtonsInterface hideButtons)
        {
            this.hideButtons = hideButtons;
        }

        @Override
        public void onCompletion(MediaPlayer mp)
        {
            /* IF IT IS A SINGLE SONG COMPLETION */
            if(playlistSongs == null)
            {
                hideButtons.hideMusicButtons();

                mp.stop();
                mp.reset();

                return;
            }

            /* IF IT IS A PLAYLIST SONG COMPLETION */

            // increasing the playlist song counter
            currPlaylistCounter++;

            // if the counter reached the number of songs to play
            if(currPlaylistCounter == playlistSongs.size())
            {
                mediaPlayer.stop();
                mediaPlayer.reset();

                hideButtons.hideMusicButtons();
                playlistSongs = null;

                return;
            }

            // playing the next song by reseting the mediaplayer and playing the next song in the list
            mediaPlayer.reset();

            playSong(playlistSongs.get(currPlaylistCounter), hideButtons);
        }
    }

    @Override
    public void onCreate()
    {
        super.onCreate();
        System.out.println("service created");

        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(this)
                        .setContentTitle("My notification")
                        .setContentText("Hello World!");
        this.startForeground(1, mBuilder.build());
    }

    @Override
    public void onDestroy()
    {
        System.out.println("service destroyed");
        this.stopForeground(true);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
