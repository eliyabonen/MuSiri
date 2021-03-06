package com.musiri.musiri;

import android.Manifest;
import android.content.Intent;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import java.io.File;
import java.util.ArrayList;
import java.util.Locale;
import audio_handling.AudioController;
import audio_handling.AudioControllerProxy;
import DataBase.Database;
import Parsing.CMDParser;

public class MainActivity extends AppCompatActivity
{
    private Database DB;
    private AudioControllerProxy audioControllerProxy;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.INTERNET, Manifest.permission.ACCESS_NETWORK_STATE}, 1);

        initializeDB();

        audioControllerProxy = new AudioControllerProxy(this);

        startService(new Intent(this, AudioController.class));
    }

    private void initializeDB()
    {
        // initializing the default music path database
        DB = new Database(this);
        DB.saveStringPreference(Database.PATHS_DATABASE, "default_path", Environment.getExternalStorageDirectory().getAbsolutePath());

        if (DB.getStringValue(Database.PATHS_DATABASE, "music_path").equals("null"))
            buildDirectoryChooserDialog();

        // initializing the recent played songs database
        if(DB.getIntValue("recentSongs", "songsCount") == 0)
            DB.saveIntPreference("recentSongs", "songsCount", 0);
    }

    public void onSpeakButtonClick(View view)
    {
        // creating the intent for the google speech api
        Intent speechRecognizerIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);

        speechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        speechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Start speaking");
        speechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1);
        speechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.ENGLISH);

        ArrayList<String> ParsedWordsList = new ArrayList<>();
        ParsedWordsList.add("create");
        ParsedWordsList.add("playlist");
        ParsedWordsList.add("hello");

        //new CMDParser(ParsedWordsList, DB, audioControllerProxy);

        this.startActivityForResult(speechRecognizerIntent, 1);
    }

    public void onPlayPauseButtonClick(View view) { audioControllerProxy.continuePauseMusic(view); }
    public void onStopButtonClick(View view) { audioControllerProxy.stopMusic(view); }

    public void onSendTextClick(View view)
    {
        ArrayList<String> ParsedWordsList = new ArrayList<>();
        String text = ((EditText) findViewById(R.id.editTextCommands)).getText().toString();
        String[] words;

        if(text.length() == 0)
            return;

        // cleaning the text input area
        ((EditText) findViewById(R.id.editTextCommands)).setText("");

        // converting the string to a list of words
        words = text.split(" ");
        for (int i = 0; i < words.length; i++)
            ParsedWordsList.add(words[i].toLowerCase());

        // executing the new command
        new CMDParser(ParsedWordsList, DB, audioControllerProxy);

        // creating a new text view on the screen
        createNewTextView(text);
    }

    private void buildDirectoryChooserDialog() {
        DirectoryChooserDialog chooserDialog = new DirectoryChooserDialog(this,
                new DirectoryChooserDialog.DirectoryChooserInterface() {
                    @Override
                    public void onChosenDir(String path) {
                        DB.saveStringPreference(Database.PATHS_DATABASE, "music_path", path);
                    }

                    @Override
                    public void onCancelClicked() {
                        // if already exists path for the music folder then exit
                        if (!DB.getStringValue(Database.PATHS_DATABASE, "music_path").equals("null"))
                            return;

                        // if not exists it creates new Music folder
                        File folder = new File((DB.getStringValue(Database.PATHS_DATABASE, "default_path")) + "/MuSiriMusic");

                        if (!folder.exists())
                            folder.mkdirs();

                        // set it as the music_path
                        DB.saveStringPreference(Database.PATHS_DATABASE, "music_path", folder.getAbsolutePath());
                    }
                });

        chooserDialog.ChooseDirectory();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent intent)
    {
        if (intent == null)
            return;

        if (requestCode == 1)
        {
            ArrayList<String> APIWordsList;
            ArrayList<String> ParsedWordsList = new ArrayList<>();
            String[] words;

            // getting a word list from the intent and sending it to the command parser
            APIWordsList = intent.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);

            // converting the string to a list of words
            words = APIWordsList.get(0).split(" ");
            for (int i = 0; i < words.length; i++)
                ParsedWordsList.add(words[i].toLowerCase());

            new CMDParser(ParsedWordsList, DB, audioControllerProxy);

            createNewTextView(APIWordsList.get(0));
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
        int id = item.getItemId();

        if (id == R.id.action_change_music_folder)
        {
            buildDirectoryChooserDialog();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    // creates new text view(the commands) on the screen
    private void createNewTextView(String text)
    {
        final ScrollView scrollView = (ScrollView) findViewById(R.id.textViewsScrollView);

        LinearLayout linearLayout = (LinearLayout) findViewById(R.id.activity_main_textviews_linear_layout);

        TextView textView = new TextView(this);
        textView.setText(text);
        textView.setTextAppearance(this, android.R.style.TextAppearance_Large);

        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        layoutParams.setMargins(0, 0, 0, 200);
        textView.setLayoutParams(layoutParams);

        linearLayout.addView(textView);

        // set the scrollview to go to the bottom whenever there is a new textview
        scrollView.post(new Runnable()
        {
            @Override
            public void run()
            {
                scrollView.fullScroll(View.FOCUS_DOWN);
            }
        });
    }
}
