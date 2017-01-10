package com.musiri.musiri;

import android.app.AlertDialog;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;
import java.util.Locale;

import DataBase.DatabaseInterface;
import Parsing.CMDParser;

public class MainActivity extends AppCompatActivity
{
    TextView textView;
    DatabaseInterface DB;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        textView = (TextView) findViewById(R.id.textView);

        initializeDB();
    }

    private void initializeDB()
    {
        DB = new DatabaseInterface("paths", this);
        DB.savePreference("default_path", Environment.getExternalStorageDirectory().getAbsolutePath());

        if(DB.getStringValue("music_path").equals("null"))
            buildDirectoryChooserDialog();
    }

    // when the speak button is clicked
    public void onSpeakButtonClick(View view)
    {
        // creating the intent for the google speech api
        Intent speechRecognizerIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        speechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        speechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Start speaking bitch!");
        speechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1);
        speechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.ENGLISH);

        // start the activity with that intent
        startActivityForResult(speechRecognizerIntent, 1);
    }

    private void buildDirectoryChooserDialog()
    {
        DirectoryChooserDialog chooserDialog = new DirectoryChooserDialog(this, new DirectoryChooserDialog.DirectoryChooserInterface()
        {
            @Override
            public void onChosenDir(String path)
            {
                DB.savePreference("music_path", path);
            }

            @Override
            public void onCancelClicked()
            {
                // if already exists path for the music folder then exit
                if(!DB.getStringValue("music_path").equals("null"))
                    return;

                // if not exists it creates new Music folder
                File folder = new File((DB.getStringValue("default_path")) + "/MuSiriMusic");

                if(!folder.exists())
                    folder.mkdirs();

                // set it as the music_path
                DB.savePreference("music_path", folder.getAbsolutePath());
            }
        });

        chooserDialog.ChooseDirectory();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent intent)
    {
        if(intent == null)
            return;

        if(requestCode == 1)
        {
            ArrayList<String> wordsList;
            String text = "";

            // getting a word list from the intent and sending it to the command parser
            wordsList = intent.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
            new CMDParser(wordsList, DB);

            Toast.makeText(this, DB.getStringValue("music_path"), Toast.LENGTH_LONG).show();

            // converting the words to list and sending it to the textview
            for(int i = 0; i < wordsList.size(); i++)
            {
                text += wordsList.get(i);
                text += " ";
            }

            textView.setText(text);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if (id == R.id.action_change_music_folder)
        {
            buildDirectoryChooserDialog();
            return true;
        }

        return super.onOptionsItemSelected(item);

        MediaPlayer p = new MediaPlayer("");
    }
}
