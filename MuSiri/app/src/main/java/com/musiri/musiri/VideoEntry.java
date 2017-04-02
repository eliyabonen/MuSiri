package com.musiri.musiri;

import android.content.Context;
import android.media.Image;
import android.net.Uri;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.TextView;

import java.net.URL;

import Network.DownloadImageTask;

public class VideoEntry
{
    private ImageView thumbnail;
    private TextView videoName;
    private RadioButton radioButton;
    private LinearLayout linearLayout;

    public VideoEntry(Context context, String url, String videoNameStr)
    {
        // the layout
        linearLayout = new LinearLayout(context);
        linearLayout.setOrientation(LinearLayout.HORIZONTAL);

        // radio button
        radioButton = new RadioButton(context);

        // image view(thumbnail)
        thumbnail = new ImageView(context);
        new DownloadImageTask(thumbnail).execute(url);

        // video name(textview)
        videoName = new TextView(context);
        videoName.setText(videoNameStr);

        linearLayout.addView(radioButton);
        linearLayout.addView(thumbnail);
        linearLayout.addView(videoName);
    }

    public LinearLayout getLayout()
    {
        return linearLayout;
    }
}
