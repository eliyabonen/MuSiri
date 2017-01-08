package Parsing;

import android.content.SharedPreferences;
import android.widget.Toast;

import java.util.ArrayList;

import DataBase.DatabaseInterface;

public class CMDParser
{
    private ArrayList<String> wordsList;
    private DatabaseInterface pathsDB;

    public CMDParser(ArrayList<String> wordsList, DatabaseInterface pathsDB)
    {
        this.pathsDB = pathsDB;

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
    }
}
