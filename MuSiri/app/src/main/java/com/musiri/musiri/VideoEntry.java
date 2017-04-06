package com.musiri.musiri;

import android.content.Context;
import android.graphics.Bitmap;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

public class VideoEntry
{
    public static final int RESULTS = 5;
    public static int currResults = 0;

    private ImageView thumbnail;
    private TextView videoName;
    private CheckBox checkBox;
    private LinearLayout linearLayout;
    private String videoID;

    public VideoEntry(Context context, String videoNameStr, Bitmap bitmapThumbnail, String videoID)
    {
        this.videoID = videoID;

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
        videoName = new TextView(context);
        videoName.setText(videoNameStr);

        LinearLayout.LayoutParams videoTextViewLayoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        videoTextViewLayoutParams.setMargins(25, 0, 0, 50);
        videoName.setLayoutParams(videoTextViewLayoutParams);

        linearLayout.addView(checkBox);
        linearLayout.addView(thumbnail);
        linearLayout.addView(videoName);
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
