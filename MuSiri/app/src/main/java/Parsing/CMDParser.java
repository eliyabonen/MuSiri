package Parsing;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import audio_handling.AudioControllerProxy;
import com.musiri.musiri.R;
import com.musiri.musiri.VideoEntry;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.TreeMap;

import DataBase.Database;
import Network.HTTPRequests;
import Network.SongDownloader;

public class CMDParser
{
    public static final int MAX_RESULTS = 5;

    private ArrayList<String> wordsList;
    private Database DB;
    private AudioControllerProxy audioControllerProxy;

    public CMDParser(ArrayList<String> wordsList, Database DB, AudioControllerProxy audioControllerProxy)
    {
        this.DB = DB;
        this.wordsList = wordsList;
        this.audioControllerProxy = audioControllerProxy;

        if(parseCommand() == -1)
            Toast.makeText(audioControllerProxy.getContext(), "There is something wrong", Toast.LENGTH_SHORT).show();
        else
            Toast.makeText(audioControllerProxy.getContext(), "Success", Toast.LENGTH_SHORT).show();
    }

    private int parseCommand()
    {
        String command = wordsList.get(0);
        String music_path = DB.getStringValue(Database.PATHS_DATABASE, "music_path");

        if(command.equals("play"))
        {
            if(audioControllerProxy.isPlaying() || audioControllerProxy.isPaused())
                return -1;

            // minimum 2 words play commands
            if(wordsList.size() < 2)
                return -1;

            if(wordsList.get(1).equals("recent"))
                return playRecentSongsCommand();

            else if(wordsList.get(1).equals("popular"))
                return playPopularSongsCommand();

            // minimum 3 words play commands
            if(wordsList.size() < 3)
                return -1;

            if(wordsList.get(1).equals("song"))
                return playSongCommand(music_path);

            else if(wordsList.get(1).equals("playlist"))
                return playPlaylistCommand(music_path);

            else if(wordsList.get(1).equals("random"))
                return playRandom(music_path);
        }
        else if(command.equals("pause"))
        {
            return pauseCommand();
        }
        else if(command.equals("stop"))
        {
            return stopCommand();
        }
        else if(command.equals("continue"))
        {
            return continueCommand();
        }
        else if(command.equals("add"))
        {
            return addSongToPlaylistCommand(music_path);
        }
        else if(command.equals("remove"))
        {
            return removeSongFromPlaylistCommand(music_path);
        }
        else if(command.equals("delete"))
        {
            return deleteSongPlaylistCommand(music_path);
        }
        else if(command.equals("help"))
        {
            return helpCommand();
        }
        else if(command.equals("search"))
        {
            return searchSongCommand();
        }

        // when there is something wrong with the command

        return -1;
    }

    private int searchSongCommand()
    {
        if(wordsList.size() < 2)
            return -1;

        if(isNetworkAvailable() == false)
            return -1;

        String searchedSong = new String();
        HTTPRequests httpRequests = new HTTPRequests();
        // main dialog layout
        LinearLayout rootLinearLayout = new LinearLayout(audioControllerProxy.getContext());
        Dialog dialog;
        TextView textView;

        // root layout configuration
        rootLinearLayout.setOrientation(LinearLayout.VERTICAL);
        rootLinearLayout.setMinimumHeight(300);
        rootLinearLayout.setMinimumWidth(300);

        // the textview that displays to the user that it's loading his request
        textView = new TextView(audioControllerProxy.getContext());
        textView.setText("Loading...");
        rootLinearLayout.addView(textView);

        // dialog
        dialog = new Dialog(audioControllerProxy.getContext());
        dialog.setContentView(rootLinearLayout);
        dialog.setTitle("Select songs");

        dialog.show();

        // getting the name of the searched song
        for(int i = 1; i < wordsList.size(); i++)
        {
            searchedSong += wordsList.get(i);

            if(i != wordsList.size()-1)
                searchedSong += "%20";
        }

        // sending a get request to the API(it is inside a new thread)
        try {
            httpRequests.sendGet("https://www.googleapis.com/youtube/v3/search?q=" + searchedSong + "&key=AIzaSyD8-00C9TCYwe95ID1yJvDOSsBh8mCUC1c&part=snippet&type=video&maxResults=" + MAX_RESULTS, new searchDialogUpdater(dialog));
        } catch (Exception e) {
            e.printStackTrace();
            return -1;
        }

        return 0;
    }

    private class searchDialogUpdater implements HTTPRequests.updateGUIInterface
    {
        private Dialog dialog;

        public searchDialogUpdater(Dialog dialog)
        {
            this.dialog = dialog;
        }

        @Override
        public void updateGUI(final String jsonResponse, final ArrayList<Bitmap> thumbnails)
        {
            audioControllerProxy.getContext().runOnUiThread(new Runnable()
            {
                public void run()
                {
                    // if there is something wrong then let the user know and quit the search dialog
                    if(jsonResponse == null)
                    {
                        dialog.cancel();
                        Toast.makeText(audioControllerProxy.getContext(), "There is something wrong", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    LinearLayout rootLinearLayout = new LinearLayout(audioControllerProxy.getContext());
                    JSONParser jsonParser = new JSONParser(jsonResponse);
                    final ArrayList<VideoEntry> videoEntries = new ArrayList<>();

                    rootLinearLayout.setOrientation(LinearLayout.VERTICAL);

                    // video entries
                    for(int i = 0; i < MAX_RESULTS; i++)
                        videoEntries.add(new VideoEntry(audioControllerProxy.getContext(), jsonParser.getFieldValue("title", i), thumbnails.get(i), jsonParser.getFieldValue("videoId", i)));

                    // buttons relative layout
                    RelativeLayout relativeLayout = new RelativeLayout(audioControllerProxy.getContext());

                    // the two buttons(Download and Cancel)
                    Button downloadButton = new Button(audioControllerProxy.getContext());
                    downloadButton.setText("Download");

                    RelativeLayout.LayoutParams downloadBtnLayoutParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
                    downloadBtnLayoutParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
                    downloadButton.setLayoutParams(downloadBtnLayoutParams);

                    downloadButton.setOnClickListener(new SongDownloader(videoEntries, DB, dialog));

                    Button cancelButton = new Button(audioControllerProxy.getContext());
                    cancelButton.setText("Cancel");

                    RelativeLayout.LayoutParams cancelBtnLayoutParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
                    cancelBtnLayoutParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
                    cancelButton.setLayoutParams(cancelBtnLayoutParams);

                    cancelButton.setOnClickListener(new View.OnClickListener()
                    {
                        @Override
                        public void onClick(View v)
                        {
                            dialog.cancel();
                        }
                    });

                    relativeLayout.addView(cancelButton);
                    relativeLayout.addView(downloadButton);

                    // adding all the video entries and adding the them checked listener to disable all the others when one is checked
                    for(int i = 0; i < videoEntries.size(); i++)
                    {
                        videoEntries.get(i).setCheckBoxOnCheckedChangeListener(i, videoEntries);
                        rootLinearLayout.addView(videoEntries.get(i).getLayout());
                    }

                    rootLinearLayout.addView(relativeLayout);

                    dialog.setContentView(rootLinearLayout);
                }
            });
        }
    }

    private boolean isNetworkAvailable()
    {
        ConnectivityManager connectivityManager = (ConnectivityManager) audioControllerProxy.getContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return (activeNetworkInfo != null && activeNetworkInfo.isConnected());
    }

    private int helpCommand()
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(audioControllerProxy.getContext());
        AlertDialog dialog;

        builder.setTitle("Help command").setMessage(R.string.help_command);

        builder.setPositiveButton("OK", new DialogInterface.OnClickListener()
        {
            public void onClick(DialogInterface dialog, int id)
            {
                dialog.cancel();
            }
        });

        dialog = builder.create();

        dialog.show();

        return 0;
    }

    private int playRecentSongsCommand()
    {
        ArrayList<String> songs = new ArrayList<>();
        String currSong;

        // gets the songs from the database
        for(int i = DB.getIntValue(Database.SONGS_DATABASE, "songsCount"); i >= 1; i--)
        {
            currSong = DB.getStringValue(Database.SONGS_DATABASE, "Song" + i);

            // to avoid duplicates
            if(!songs.contains(currSong))
                songs.add(currSong);
        }

        if(songs.size() == 0)
            return -1;

        for(int i = 0; i < songs.size(); i++)
            System.out.println("************* " + songs.get(i));

        // play all the song like it is a playlist
        audioControllerProxy.playPlaylist(songs);

        return 0;
    }

    private int playPopularSongsCommand()
    {
        String currSong, currPopularSong;
        HashMap<String, Integer> songsCount = new HashMap<>();
        ArrayList<String> songs = new ArrayList<>();
        TreeMap<String, Integer> sortedSongs = new TreeMap<String, Integer>(new ValueComparator(songsCount));
        int tmpValue, currPopularIndex = 0, currPopularCount = 0;

        // gets the songs and their occurences from the database
        for(int i = 1; i <= DB.getIntValue(Database.SONGS_DATABASE, "songsCount"); i++)
        {
            currSong = DB.getStringValue(Database.SONGS_DATABASE, "Song" + i);

            // if the songs doesn't exists in the map then initialize it with zero, else add 1 to the counter
            if(!songsCount.containsKey(currSong))
                songsCount.put(currSong, 1);
            else
            {
                tmpValue = songsCount.get(currSong);
                songsCount.remove(currSong);
                songsCount.put(currSong, tmpValue+1);
            }
        }

        if(songsCount.size() == 0)
            return -1;

        // sorting the map
        sortedSongs.putAll(songsCount);

        // listing it into an arraylist
        for (Map.Entry<String, Integer> entry : sortedSongs.entrySet())
            songs.add(entry.getKey());

        audioControllerProxy.playPlaylist(songs);

        return 0;
    }

    // a comparator class for the treemap in popular command
    private class ValueComparator implements Comparator<String>
    {
        Map<String, Integer> base;

        public ValueComparator(Map<String, Integer> base)
        {
            this.base = base;
        }

        public int compare(String a, String b)
        {
            if (base.get(a) >= base.get(b))
                return -1;
            else
                return 1;
        }
    }

    private int playRandom(String music_path)
    {
        // get all the files from the music directory
        Random rand = new Random();
        File music_dir = new File(music_path);
        File[] files = music_dir.listFiles();

        if(files.length == 0)
            return -1;

        if(wordsList.get(2).equals("song"))
        {
            int randomNum;
            ArrayList<File> songs = new ArrayList<>();

            // gets only the songs(mp3 formatted)
            for(int i = 0; i < files.length; i++)
            {
                if(files[i].isFile() && files[i].getName().endsWith(".mp3"))
                    songs.add(files[i]);
            }

            if(songs.size() == 0)
                return -1;

            // playing random song from the songs list
            randomNum = rand.nextInt(songs.size());
            audioControllerProxy.playSong(songs.get(randomNum).getAbsolutePath());

            return 0;
        }
        else if(wordsList.get(2).equals("playlist"))
        {
            int randomNum;
            ArrayList<File> playlists = new ArrayList<>();
            ArrayList<String> songs = new ArrayList<>();
            File randomPlaylist;
            File[] filesInPlaylist;

            // getting all the playlists(directories) from the main music directory
            for(int i = 0; i < files.length; i++)
            {
                if(files[i].isDirectory())
                    playlists.add(files[i]);
            }

            if(playlists.size() == 0)
                return -1;

            // picking a random playlist
            randomNum = rand.nextInt(playlists.size());
            randomPlaylist = playlists.get(randomNum);
            filesInPlaylist = randomPlaylist.listFiles();

            // getting all the songs from that random playlist
            for(int i = 0; i < filesInPlaylist.length; i++)
            {
                if(filesInPlaylist[i].isFile() && filesInPlaylist[i].getName().endsWith(".mp3"))
                    songs.add(filesInPlaylist[i].getAbsolutePath());
            }

            if(songs.size() == 0)
                return -1;

            // play the songs from the random playlist
            audioControllerProxy.playPlaylist(songs);

            return 0;
        }
        else
            return -1;
    }

    private int deleteSongPlaylistCommand(String path)
    {
        if(wordsList.size() < 3 && !(wordsList.get(1).equals("song") || wordsList.get(1).equals("playlist")))
            return -1;

        ArrayList<String> fileName = new ArrayList<>();
        File file;
        String mostMatchedFilePath;

        // getting the file to delete(song or playlist)
        for(int i = 2; i < wordsList.size(); i++)
            fileName.add(wordsList.get(i));

        if(wordsList.get(1).equals("song"))
        {
            // searching the most matched song and creating the file object of that song
            mostMatchedFilePath = getMostMatchedSongPath(new File(path), fileName);

            if(mostMatchedFilePath == null)
                return -1;

            file = new File(mostMatchedFilePath);
        }
        else
        {
            // searching the most matched playlist and creating the file object of that directory(playlist)
            mostMatchedFilePath = getMostMatchedPlaylistPath(new File(path), fileName);

            if(mostMatchedFilePath == null)
                return -1;

            file = new File(mostMatchedFilePath);
        }

        // deleting that file and if it's a directory then all it content and itself is deleted
        if(!deleteRecursive(file))
            return -1;

        return 0;
    }

    /* deletes a file or a directory recursively */
    private boolean deleteRecursive(File fileOrDirectory) {
        if (fileOrDirectory.isDirectory())
            for (File child : fileOrDirectory.listFiles())
                deleteRecursive(child);

        return fileOrDirectory.delete();
    }

    private int removeSongFromPlaylistCommand(String path)
    {
        if(wordsList.size() < 4 || !wordsList.contains("from"))
            return -1;

        ArrayList<String> songName = new ArrayList<String>(), playlistName = new ArrayList<String>();
        String mostMatchedSong, mostMatchedPlaylist;
        int fromIndex = -1;

        /* gets song and playlist names */

        // get the index to "from" keyword
        for(int i = wordsList.size()-1; i >= 0; i--)
        {
            if (wordsList.get(i).equals("from"))
                fromIndex = i;
        }

        // get the names of the song and the playlist
        for(int i = 1; i < wordsList.size(); i++)
        {
            // if it is the "to" index then don't add it to either the song name or playlist name
            if(i == fromIndex)
                continue;

            if(i < fromIndex)
                songName.add(wordsList.get(i));
            else
                playlistName.add(wordsList.get(i));
        }

        // gets the most matched song name and playlist name
        mostMatchedSong = getMostMatchedSongPath(new File(path), songName);

        if(mostMatchedSong == null)
            return -1;

        mostMatchedPlaylist = getMostMatchedPlaylistPath(new File(path), playlistName);

        if(mostMatchedPlaylist == null)
            return -1;

        // deleting the file
        File file = new File(mostMatchedPlaylist + "/" + (new File(mostMatchedSong).getName()));
        if(!file.delete())
            return -1;

        return 0;
    }

    private int addSongToPlaylistCommand(String path)
    {
        if(wordsList.size() < 4 || !wordsList.contains("to"))
            return -1;

        ArrayList<String> songName = new ArrayList<String>(), playlistName = new ArrayList<String>();
        String mostMatchedSong, mostMatchedPlaylist;
        int toIndex = -1;

        /* gets song and playlist names */

        // get the index to "to" keyword
        for(int i = wordsList.size()-1; i >= 0; i--)
        {
            if (wordsList.get(i).equals("to"))
                toIndex = i;
        }

        // get the names of the song and the playlist
        for(int i = 1; i < wordsList.size(); i++)
        {
            // if it is the "to" index then don't add it to either the song name or playlist name
            if(i == toIndex)
                continue;

            if(i < toIndex)
                songName.add(wordsList.get(i));
            else
                playlistName.add(wordsList.get(i));
        }

        // gets the most matched song name and playlist name
        mostMatchedSong = getMostMatchedSongPath(new File(path), songName);

        if(mostMatchedSong == null)
            return -1;

        mostMatchedPlaylist = getMostMatchedPlaylistPath(new File(path), playlistName);

        if(mostMatchedPlaylist == null)
            return -1;

        // Copy the source song to the destination playlist
        File source = new File(mostMatchedSong);
        File dest = new File(mostMatchedPlaylist + "/" + source.getName());

        try {
            FileInputStream in = new FileInputStream(source);
            FileOutputStream out = new FileOutputStream(dest);

            byte[] buffer = new byte[1024];
            int read;
            while ((read = in.read(buffer)) != -1)
            {
                out.write(buffer, 0, read);
            }
            in.close();

            out.flush();
            out.close();

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return 0;
    }

    // getting the most matched song
    private int playSongCommand(String path)
    {
        // gets all the files in the music directory
        File musicFolder = new File(path);
        String mostMatchedSong;
        ArrayList<String> songName = new ArrayList<String>();

        // if it doesn't exists or it is not a directory then get out
        if(!musicFolder.exists() || !musicFolder.isDirectory())
            return -1;

        // gets only the song name(without the "play" and the "song"
        for(int i = 2; i < wordsList.size(); i++)
            songName.add(wordsList.get(i));

        // play the most matched song
        mostMatchedSong = getMostMatchedSongPath(musicFolder, songName);

        if(mostMatchedSong == null)
            return -1;

        // adding the song to the songs playlist
        DB.saveIntPreference(Database.SONGS_DATABASE, "songsCount", DB.getIntValue(Database.SONGS_DATABASE, "songsCount")+1);
        DB.saveStringPreference(Database.SONGS_DATABASE, "Song" + DB.getIntValue(Database.SONGS_DATABASE, "songsCount"), mostMatchedSong);

        // after 500 songs it resets itself
        if(DB.getIntValue(Database.SONGS_DATABASE, "songsCount") > 500)
        {
            DB.clearPreference(Database.SONGS_DATABASE);
            DB.saveIntPreference("recentSongs", "songsCount", 0);
        }

        // playing the song
        audioControllerProxy.playSong(mostMatchedSong);

        return 0;
    }

    private String getMostMatchedSongPath(File musicFolder, ArrayList<String> songName)
    {
        File[] files = musicFolder.listFiles();

        // if it has songs in it
        //if(files.length == 0)
        //return -1;

        int matchedWords, maxMatchedWords = 0, biggestMatchIndex = 0;

        // loops through all the files that in the music directory
        for(int i = 0; i < files.length; i++)
        {
            matchedWords = 0;

            // checks if the file is mp3 formatted
            if(files[i].isFile() && files[i].getName().endsWith(".mp3"))
            {
                // parsing the file name to array of words
                String[] fileNameWordsArray = files[i].getName().split(" ");
                fileNameWordsArray[fileNameWordsArray.length - 1] = fileNameWordsArray[fileNameWordsArray.length - 1].substring(0,fileNameWordsArray[fileNameWordsArray.length - 1].length() - 4);

                // checks if the the given song has more words than the checked song(if so it doesn't check if it is matching)
                if(songName.size() <= fileNameWordsArray.length)
                {
                    // loop through every word and checking if there is matched words
                    for(int j = 0; j < songName.size(); j++)
                    {
                        for(int k = 0; k < fileNameWordsArray.length; k++)
                        {
                            // checks if the file name word contains a word from the user, the word from the user must be greater than one char for preventing checking issues
                            if(fileNameWordsArray[k].toLowerCase().equals(songName.get(j)) && (songName.get(j).length() > 1))
                            {
                                matchedWords++;
                            }
                        }
                    }

                    // getting the maximum words match and saving it's index
                    if(maxMatchedWords <= matchedWords)
                    {
                        biggestMatchIndex = i;
                        maxMatchedWords = matchedWords;
                    }
                }
            }
        }

        // return the most matched song
        if(maxMatchedWords != 0)
            return files[biggestMatchIndex].getAbsolutePath();
        return null;
    }

    private String getMostMatchedPlaylistPath(File musicFolder, ArrayList<String> playlistName)
    {
        File[] files = musicFolder.listFiles();

        // if it has songs in it
        //if(files.length == 0)
        //return -1;

        int matchedWords, maxMatchedWords = 0, biggestMatchIndex = 0;

        // loops through all the files that in the music directory
        for(int i = 0; i < files.length; i++)
        {
            matchedWords = 0;

            // checks if the file is a directory
            if(files[i].isDirectory())
            {
                // parsing the file name to array of words
                String[] fileNameWordsArray = files[i].getName().split(" ");

                // checks if the the given song has more words than the checked song(if so it doesn't check if it is matching)
                if(playlistName.size() <= fileNameWordsArray.length)
                {
                    // loop through every word and checking if there is matched words
                    for(int j = 0; j < playlistName.size(); j++)
                    {
                        for(int k = 0; k < fileNameWordsArray.length; k++)
                        {
                            // checks if the file name word contains a word from the user, the word from the user must be greater than one char for preventing checking issues
                            if(fileNameWordsArray[k].toLowerCase().equals(playlistName.get(j)) && (playlistName.get(j).length() > 1))
                            {
                                matchedWords++;
                            }
                        }
                    }

                    // getting the maximum words match and saving it's index
                    if(maxMatchedWords <= matchedWords)
                    {
                        biggestMatchIndex = i;
                        maxMatchedWords = matchedWords;
                    }
                }
            }
        }

        // return the most matched song
        if(maxMatchedWords != 0)
            return files[biggestMatchIndex].getAbsolutePath();
        return null;
    }

    private int playPlaylistCommand(String path)
    {
        ArrayList<String> playlistName = new ArrayList<String>(), songs = new ArrayList<String>();
        String mostMatchedPlaylist;
        File musicFolder;
        File[] files;

        // getting the playlistName
        for(int i = 2; i < wordsList.size(); i++)
            playlistName.add(wordsList.get(i));

        // gets all the files in the music directory
        mostMatchedPlaylist = getMostMatchedPlaylistPath(new File(path), playlistName);

        if(mostMatchedPlaylist == null)
            return -1;

        musicFolder = new File(mostMatchedPlaylist);

        // if it doesn't exists or it is not a directory then get out
        if(!musicFolder.exists() || !musicFolder.isDirectory())
            return -1;

        files = musicFolder.listFiles();

        // if it files songs in it
        //if(files.length == 0)
            //return -1;

        // gets all the mp3 formatted songs from the directory into the songs array
        for(int i = 0; i < files.length; i++)
        {
            if(files[i].getName().substring(files[i].getName().length() - 4).equals(".mp3"))
                songs.add(files[i].getAbsolutePath());
        }

        // if there is songs to play then play them
        if(songs.size() > 0)
            audioControllerProxy.playPlaylist(songs);

        return 0;
    }

    private int pauseCommand()
    {
        if(wordsList.size() != 1)
            return -1;

        if(audioControllerProxy.isPaused())
            return -1;

        audioControllerProxy.continuePauseMusic(audioControllerProxy.getContext().findViewById(R.id.pausePlayImageView));

        return 0;
    }

    private int stopCommand()
    {
        if(wordsList.size() != 1)
            return -1;

        audioControllerProxy.stopMusic(audioControllerProxy.getContext().findViewById(R.id.stopImageView));

        return 0;
    }

    private int continueCommand()
    {
        if(wordsList.size() != 1)
            return -1;

        if(audioControllerProxy.isPlaying())
            return -1;

        audioControllerProxy.continuePauseMusic(audioControllerProxy.getContext().findViewById(R.id.pausePlayImageView));

        return 0;
    }
}
