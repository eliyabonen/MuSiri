package com.musiri.musiri;

import android.content.Context;
import android.graphics.Bitmap;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;

public class VideoEntry
{
    public static final int RESULTS = 5;
    public static int currResults = 0;

    private ImageView thumbnail;
    private TextView videoNameTextView;
    private CheckBox checkBox;
    private LinearLayout linearLayout;
    private String videoID;
    private String videoName;

    public VideoEntry(Context context, String videoNameStr, Bitmap bitmapThumbnail, String videoID)
    {
        this.videoID = videoID;
        this.videoName = videoNameStr;

        // the layout
        linearLayout = new LinearLayout(context);
        linearLayout.setOrientation(LinearLayout.HORIZONTAL);

        LinearLayout.LayoutParams linearLayoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        linearLayoutParams.setMargins(0, 50, 0, 0);
        linearLayout.setLayoutParams(linearLayoutParams);

        // radio button
        checkBox = new CheckBox(context);
        LinearLayout.LayoutParams checkBoxLayoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        checkBoxLayoutParams.setMargins(0, 0, 80, 0);
        checkBox.setLayoutParams(checkBoxLayoutParams);

        // image view(thumbnail)
        thumbnail = new ImageView(context);
        thumbnail.setImageBitmap(bitmapThumbnail);

        // video name(textview)
        videoNameTextView = new TextView(context);
        videoNameTextView.setText(videoNameStr);

        LinearLayout.LayoutParams videoTextViewLayoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        videoTextViewLayoutParams.setMargins(25, 0, 0, 50);
        videoNameTextView.setLayoutParams(videoTextViewLayoutParams);

        linearLayout.addView(checkBox);
        linearLayout.addView(thumbnail);
        linearLayout.addView(videoNameTextView);
    }

    // when one checkbox is checked then all the other are disabled and vise versa
    public void setCheckBoxOnCheckedChangeListener(final int currVideoEntry, final ArrayList<VideoEntry> videoEntries)
    {
        checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener()
        {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked)
            {
                for(int i = 0; i < videoEntries.size(); i++)
                {
                    if(currVideoEntry != i)
                    {
                        if(isChecked)
                            videoEntries.get(i).getCheckBox().setEnabled(false);
                        else
                            videoEntries.get(i).getCheckBox().setEnabled(true);
                    }
                }
            }
        });
    }

    public CheckBox getCheckBox()
    {
        return checkBox;
    }

    public String getVideoName()
    {
        return videoName;
    }

    public boolean isChecked()
    {
        return checkBox.isChecked();
    }

    public String getVideoID()
    {
        return videoID;
    }

    public LinearLayout getLayout()
    {
        return linearLayout;
    }
}
