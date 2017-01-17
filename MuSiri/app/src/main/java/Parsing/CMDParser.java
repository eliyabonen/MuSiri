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
        String command = wordsList.get(0).toString();

        if(command.toLowerCase().equals("play"))
            playCommand();
    }

    private void playCommand()
    {
        String musicFolderPath = pathsDB.getStringValue("music_path");
        System.out.println("******IN THE PLAY*****");

        // gets all the files in the music directory
        File musicFolder = new File(musicFolderPath);
        File[] files = musicFolder.listFiles();
        ArrayList<String> rightSongs = new ArrayList<String>();

        for(int i = 0; i < files.length; i++)
        {
            boolean allStringsFound = false;
            if(".mp3".equals(files[i].getName().substring(files[i].getName().length() - 4)))
            {
                String[] fileNameWordsArray = files[i].getName().split(" ");
                fileNameWordsArray[fileNameWordsArray.length - 1] = fileNameWordsArray[fileNameWordsArray.length - 1].substring(0,fileNameWordsArray[fileNameWordsArray.length - 1].length() - 4);

                if(this.wordsList.size() - 1 <= fileNameWordsArray.length)
                {
                    for(int j = 0; j <= (fileNameWordsArray.length - this.wordsList.size() - 1) && !allStringsFound; j++)
                    {
                        boolean wordFound = true;
                        for(int k = 0; k < this.wordsList.size() - 1 && wordFound; k++)
                        {
                            wordFound = compare(this.wordsList.get(k + 1),fileNameWordsArray[j + k]);
                            if(wordFound && k == this.wordsList.size() - 2)
                                allStringsFound = true;
                        }
                    }
                }
            }
            if(allStringsFound)
            {
                System.out.println("************************* " + files[i].getName());
                rightSongs.add(files[i].getName());
            }
        }
        /*Uri u = Uri.parse(musicFolderPath + rightSongs.get(0).toString());
        MediaPlayer mediaPlayer = MediaPlayer.create(mainAct.getApplicationContext(), u);
        mediaPlayer.start();*/

    }
    private boolean compare(String w1, String w2)
    {
        for(int i = 0; i < w1.length(); i++)
        {
            if(w1.toLowerCase().charAt(i) != w2.toLowerCase().charAt(i))
                return false;
        }
        return true;
    }
}
