package com.musiri.musiri;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.view.View;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Locale;

import Parsing.CMDParser;

public class MainActivity extends AppCompatActivity
{
    TextView textView;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        textView = (TextView) findViewById(R.id.textView);
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
            new CMDParser(wordsList);

            // converting the words to list and sending it to the textview
            for(int i = 0; i < wordsList.size(); i++)
            {
                text += wordsList.get(i);
                text += " ";
            }

            textView.setText(text);
        }
    }
}
