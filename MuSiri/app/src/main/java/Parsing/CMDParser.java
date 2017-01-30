package Parsing;

import android.content.Context;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.provider.Settings;
import android.widget.Toast;

import com.musiri.musiri.AudioController;
import com.musiri.musiri.GuiButtons;
import com.musiri.musiri.R;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import DataBase.DatabaseInterface;

public class CMDParser
{
    private ArrayList<String> wordsList;
    private DatabaseInterface pathsDB;
    private AudioController audioController;
    private GuiButtons guiButtons;

    public CMDParser(ArrayList<String> wordsList, DatabaseInterface pathsDB, AudioController audioController, GuiButtons guiButtons)
    {
        this.pathsDB = pathsDB;
        this.wordsList = wordsList;
        this.audioController = audioController;
        this.guiButtons = guiButtons;

        parseCommand();
    }

    private void parseCommand()
    {
        String command = wordsList.get(0).toString();

        if(command.toLowerCase().equals("play"))
        {
            if(wordsList.size() < 3)
                return;

            if(wordsList.get(1).equals("song"))
                playSongCommand(pathsDB.getStringValue("music_path"));

            else if(wordsList.get(1).equals("playlist"))
                playPlaylistCommand((pathsDB.getStringValue("music_path") + "/" + wordsList.get(2)));
        }
        else if(command.toLowerCase().equals("pause"))
            pauseCommand();
        else if(command.toLowerCase().equals("stop"))
            stopCommand();
        else if(command.toLowerCase().equals("continue"))
            continueCommand();
    }


    // getting the most matched song
    private void playSongCommand(String path)
    {
        // gets all the files in the music directory
        File musicFolder = new File(path);

        // if it doesn't exists or it is not a directory then get out
        if(!musicFolder.exists() || !musicFolder.isDirectory())
            return;

        File[] files = musicFolder.listFiles();

        // if it has songs in it
        if(files.length == 0)
            return;

        //Map<String, Integer> matchedSongs = new HashMap<String, Integer>();
        int matchedWords, maxMatchedWords = 0, biggestMatchIndex = 0;

        // loops through all the files that in the default directory
        for(int i = 0; i < files.length; i++)
        {
            matchedWords = 0;

            // checks if the file is mp3 formatted
            if(".mp3".equals(files[i].getName().substring(files[i].getName().length() - 4)))
            {
                // parsing the file name to array of words
                String[] fileNameWordsArray = files[i].getName().split(" ");
                fileNameWordsArray[fileNameWordsArray.length - 1] = fileNameWordsArray[fileNameWordsArray.length - 1].substring(0,fileNameWordsArray[fileNameWordsArray.length - 1].length() - 4);

                // checks if the the given song has more words than the checked song(if so it doesn't check if it is matching)
                if(this.wordsList.size() - 2 <= fileNameWordsArray.length)
                {
                    // loop through every word and checking if there is matched words
                    for(int j = 2; j < wordsList.size(); j++)
                    {
                        for(int k = 0; k < fileNameWordsArray.length; k++)
                        {
                            // checks if the file name word contains a word from the user, the word from the user must be greater than one char for preventing checking issues
                            if(fileNameWordsArray[k].toLowerCase().contains(wordsList.get(j).toLowerCase()) && (wordsList.get(j).length() > 1))
                            {
                                matchedWords++;
                            }
                        }
                    }

                    //matchedSongs.put(files[i].getName().toLowerCase(), matchedWords);

                    // getting the maximum words match and saving it's index
                    if(maxMatchedWords <= matchedWords)
                    {
                        biggestMatchIndex = i;
                        maxMatchedWords = matchedWords;
                    }
                }
            }
        }

        System.out.println("************Best match: " + files[biggestMatchIndex].getName());

        // play the most matched song, and if there is no match then it doesn't play anything
        if(maxMatchedWords != 0)
            audioController.playSong(files[biggestMatchIndex].getAbsolutePath());
    }

    private void playPlaylistCommand(String path)
    {
        // gets all the files in the music directory
        File musicFolder = new File(path);

        // if it doesn't exists or it is not a directory then get out
        if(!musicFolder.exists() || !musicFolder.isDirectory())
            return;

        File[] files = musicFolder.listFiles();

        // if it files songs in it
        if(files.length == 0)
            return;

        ArrayList<String> songs = new ArrayList<>();

        // gets all the mp3 formatted songs from the directory into the songs array
        for(int i = 0; i < files.length; i++)
        {
            if(files[i].getName().substring(files[i].getName().length() - 4).equals(".mp3"))
                songs.add(files[i].getAbsolutePath());
        }

        // if there is songs to play then play them
        if(songs.size() > 0)
            audioController.playPlaylist(songs);
    }

    private void pauseCommand()
    {
        if(wordsList.size() == 1)
            guiButtons.onPlayPauseButtonClick(guiButtons.getContext().findViewById(R.id.pausePlayImageView));
    }

    private void stopCommand()
    {
        if(wordsList.size() == 1)
            guiButtons.onStopButtonClick(guiButtons.getContext().findViewById(R.id.stopImageView));
    }

    private void continueCommand()
    {
        if(wordsList.size() == 1)
            guiButtons.onPlayPauseButtonClick(guiButtons.getContext().findViewById(R.id.pausePlayImageView));
    }
}
