package Parsing;

import android.widget.Toast;
import com.musiri.musiri.AudioControllerProxy;
import com.musiri.musiri.R;

import java.io.File;
import java.util.ArrayList;

import DataBase.DatabaseInterface;

public class CMDParser
{
    private ArrayList<String> wordsList;
    private DatabaseInterface pathsDB;
    private AudioControllerProxy audioControllerProxy;

    public CMDParser(ArrayList<String> wordsList, DatabaseInterface pathsDB, AudioControllerProxy audioControllerProxy)
    {
        this.pathsDB = pathsDB;
        this.wordsList = wordsList;
        this.audioControllerProxy = audioControllerProxy;

        if(parseCommand() == -1)
            Toast.makeText(audioControllerProxy.getContext(), "There is something wrong", Toast.LENGTH_SHORT).show();
    }

    private int parseCommand()
    {
        String command = wordsList.get(0);
        // add gangnam style to pop
        if(command.equals("play"))
        {
            if(wordsList.size() < 3)
                return -1;

            if(wordsList.get(1).equals("song"))
                return playSongCommand(pathsDB.getStringValue("music_path"));

            else if(wordsList.get(1).equals("playlist"))
                return playPlaylistCommand(pathsDB.getStringValue("music_path"));
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
            return addSongToPlaylist(pathsDB.getStringValue("music_path"));
        }
        // when there is something wrong with the command
        else
            return -1;

        return 0;
    }

    private int addSongToPlaylist(String path)
    {
        if(wordsList.size() < 4 || wordsList.contains("to"))
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
        for(int i = 0; i < wordsList.size(); i++)
        {
            if(i < toIndex)
                songName.add(wordsList.get(i));
            else
                playlistName.add(wordsList.get(i));
        }

        // gets the most matched song name and playlist name
        mostMatchedSong = getMostMatchedSongPath(new File(path), songName);

        if(mostMatchedSong == null)
            return -1;

        mostMatchedPlaylist = getMostMatchedSongPath(new File(path), playlistName);

        if(mostMatchedPlaylist == null)
            return -1;


        /* TODO: add mostMatchedSong to mostMatchedPlaylist */

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
            if(".mp3".equals(files[i].getName().substring(files[i].getName().length() - 4)))
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
                            if(fileNameWordsArray[k].toLowerCase().contains(songName.get(j)) && (songName.get(j).length() > 1))
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
                            if(fileNameWordsArray[k].toLowerCase().contains(playlistName.get(j)) && (playlistName.get(j).length() > 1))
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

        audioControllerProxy.continuePauseMusic(audioControllerProxy.getContext().findViewById(R.id.pausePlayImageView));

        return 0;
    }
}
