package Parsing;

import java.util.ArrayList;

public class CMDParser
{
    private ArrayList<String> wordsList;

    public CMDParser(ArrayList<String> wordsList)
    {
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

    }
}
