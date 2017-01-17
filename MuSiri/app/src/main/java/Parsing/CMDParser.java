package Parsing;

import android.content.Context;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;

import DataBase.DatabaseInterface;

public class CMDParser
{
    private ArrayList<String> wordsList;
    private DatabaseInterface pathsDB;
    private Context mainAct;

    public CMDParser(ArrayList<String> wordsList, DatabaseInterface pathsDB, Context mainAct)
    {
        this.pathsDB = pathsDB;
        this.mainAct = mainAct;
        this.wordsList = wordsList;
        parseCommand();
    }

    private void parseCommand()
    {
        String command = wordsList.get(0);

        if(command.equals("play"))
            playCommand();
    }

    private void playCommand()
    {
        String musicFolderPath = pathsDB.getStringValue("music_path");


        // gets all the files in the music directory
        File musicFolder = new File(musicFolderPath);
        File[] files = musicFolder.listFiles();
        ArrayList<String> rightSongs = new ArrayList<String>();

        for(int i = 0; i < files.length; i++)
        {
            boolean allStringsFound = false;
            if(".mp3".equals(files[i].getName().substring(files[i].getName().length() - 4)))
            {
                String[] fileName = files[i].getName().split(" ");
                fileName[fileName.length - 1] = fileName[fileName.length - 1].substring(0,fileName.length - 5);
                if(this.wordsList.size() - 1 <= fileName.length)
                {
                    for(int j = 0; j <= (fileName.length - this.wordsList.size() - 1) && !allStringsFound; j++)
                    {
                        boolean wordFound = true;
                        for(int k = 0; k < this.wordsList.size() - 1 && wordFound; k++)
                        {
                            wordFound = compare((String)this.wordsList.get(k + 1),(String)fileName[j + k]);
                            if(wordFound && k == this.wordsList.size() - 2)
                                allStringsFound = true;
                        }
                    }
                }
            }
            if(allStringsFound)
                rightSongs.add(files[i].getName());
        }
        Uri u = Uri.parse(musicFolderPath + rightSongs.get(0).toString());
        MediaPlayer mediaPlayer = MediaPlayer.create(mainAct.getApplicationContext(), u);
        mediaPlayer.start();


    }
    private boolean compare(String w1, String w2)
    {
        boolean wrongWord = true;
        if(w1.length() != w2.length())
            return false;

        for(int i = 0; i < w1.length() && wrongWord; i++)
        {
            if(w1.substring(i,i).toUpperCase() != w2.substring(i,i).toUpperCase())
                wrongWord = false;

        }
        return wrongWord;
    }
}
