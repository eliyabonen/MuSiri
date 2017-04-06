package Network;

import android.view.View;

import com.musiri.musiri.VideoEntry;

import java.util.ArrayList;

import DataBase.DatabaseInterface;

public class SongDownloader implements View.OnClickListener
{
    private ArrayList<VideoEntry> videoEntries;
    private DatabaseInterface DB;

    public SongDownloader(ArrayList<VideoEntry> videoEntries, DatabaseInterface DB)
    {
        this.videoEntries = videoEntries;
        this.DB = DB;
    }

    @Override
    public void onClick(View v)
    {
        new Thread(new Runnable()
        {
            @Override
            public void run()
            {
                for(int i = 0; i < videoEntries.size(); i++)
                {
                    if(!videoEntries.get(i).isChecked())
                        continue;

                    

                }
            }
        }).start();
    }
}
