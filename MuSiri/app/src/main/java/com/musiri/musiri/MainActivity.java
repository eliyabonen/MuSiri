package com.musiri.musiri;

import android.content.Intent;
import android.os.Environment;
import android.provider.ContactsContract;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import java.io.File;
import java.util.ArrayList;
import java.util.Locale;

import AudioHandling.AudioController;
import AudioHandling.AudioControllerProxy;
import DataBase.DatabaseInterface;
import Parsing.CMDParser;

public class MainActivity extends AppCompatActivity {
    private DatabaseInterface DB;
    private AudioController audioController;
    private AudioControllerProxy audioControllerProxy;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initializeDB();

        audioControllerProxy = new AudioControllerProxy(this);

        //startService(new Intent(this, AudioController.class));
    }

    private void initializeDB()
    {
        // initializing the default music path database
        DB = new DatabaseInterface(this);
        DB.saveStringPreference(DatabaseInterface.PATHS_DATABASE, "default_path", Environment.getExternalStorageDirectory().getAbsolutePath());

        if (DB.getStringValue(DatabaseInterface.PATHS_DATABASE, "music_path").equals("null"))
            buildDirectoryChooserDialog();

        // initializing the recent played songs database
        if(DB.getIntValue("recentSongs", "songsCount") == 0)
            DB.saveIntPreference("recentSongs", "songsCount", 0);
    }

    public void onSpeakButtonClick(View view)
    {
        /*// set it to the pause icon
        if(!audioController.isPaused())
            buttonPlayPause.setImageResource(R.mipmap.pause_icon);*/

        // creating the intent for the google speech api
        Intent speechRecognizerIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);

        speechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        speechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Start speaking");
        speechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1);
        speechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.ENGLISH);

        ArrayList<String> ParsedWordsList = new ArrayList<>();
        ParsedWordsList.add("search");
        ParsedWordsList.add("gangnam");
        ParsedWordsList.add("style");

        //new CMDParser(ParsedWordsList, DB, audioControllerProxy);

        this.startActivityForResult(speechRecognizerIntent, 1);
    }

    public void onPlayPauseButtonClick(View view) { audioControllerProxy.continuePauseMusic(view); }
    public void onStopButtonClick(View view) { audioControllerProxy.stopMusic(view); }

    private void buildDirectoryChooserDialog() {
        DirectoryChooserDialog chooserDialog = new DirectoryChooserDialog(this,
                new DirectoryChooserDialog.DirectoryChooserInterface() {
                    @Override
                    public void onChosenDir(String path) {
                        DB.saveStringPreference(DatabaseInterface.PATHS_DATABASE, "music_path", path);
                    }

                    @Override
                    public void onCancelClicked() {
                        // if already exists path for the music folder then exit
                        if (!DB.getStringValue(DatabaseInterface.PATHS_DATABASE, "music_path").equals("null"))
                            return;

                        // if not exists it creates new Music folder
                        File folder = new File((DB.getStringValue(DatabaseInterface.PATHS_DATABASE, "default_path")) + "/MuSiriMusic");

                        if (!folder.exists())
                            folder.mkdirs();

                        // set it as the music_path
                        DB.saveStringPreference(DatabaseInterface.PATHS_DATABASE, "music_path", folder.getAbsolutePath());
                    }
                });

        chooserDialog.ChooseDirectory();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        if (intent == null)
            return;

        if (requestCode == 1) {
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
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_change_music_folder) {
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
