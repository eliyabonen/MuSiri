package Network;

import android.app.Dialog;
import android.view.View;

import com.musiri.musiri.VideoEntry;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

import DataBase.Database;

public class SongDownloader implements View.OnClickListener
{
    private static final int SECONDS_TO_WAIT = 10;

    private ArrayList<VideoEntry> videoEntries;
    private Database DB;
    private Dialog dialog;

    public SongDownloader(ArrayList<VideoEntry> videoEntries, Database DB, Dialog dialog)
    {
        this.videoEntries = videoEntries;
        this.DB = DB;
        this.dialog = dialog;
    }

    @Override
    public void onClick(View v)
    {
        dialog.cancel();

        new Thread(new Runnable()
        {
            @Override
            public void run()
            {
                for(int i = 0; i < videoEntries.size(); i++)
                {
                    if (!videoEntries.get(i).isChecked())
                        continue;

                    try {
                        String urlString = "http://www.youtubeinmp3.com/fetch/?video=https://www.youtube.com/watch?v=" + videoEntries.get(i).getVideoID();
                        String stringData;

                        System.out.println("URL1: " + urlString);

                        if ((stringData = downloadSong(urlString, videoEntries.get(i).getVideoName())) != null)
                        {
                            String url2 = "http://www.youtubeinmp3.com" + getDownloadURL(stringData);

                            System.out.println("URL2: " + url2);
                            downloadSong(url2, videoEntries.get(i).getVideoName());
                        }
                    } catch (Exception e) {
                    e.printStackTrace();
                    }
                }
            }
        }).start();
    }

    private String downloadSong(String url, String videoName) throws Exception
    {
        String htmlData, line = null;
        BufferedInputStream in;
        FileOutputStream fout;
        int count;
        byte[] byteTmp = new byte[1024];
        BufferedReader reader;
        StringBuilder sb;

        // opening the streams and getting the data from the url server
        in = new BufferedInputStream(new URL(url).openStream());
        fout = new FileOutputStream(DB.getStringValue(Database.PATHS_DATABASE, "music_path") + "/" + videoName + ".mp3");

        // writing from data from the stream to the array
        while ((count = in.read(byteTmp, 0, 1024)) > 0)
            fout.write(byteTmp, 0, count);

        in.close();
        fout.close();

        // reading the contents to see if it is a mp3 file or html file
        reader = new BufferedReader(new InputStreamReader(new FileInputStream(DB.getStringValue(Database.PATHS_DATABASE, "music_path") + "/" + videoName + ".mp3")));
        sb = new StringBuilder();

        while ((line = reader.readLine()) != null)
            sb.append(line);

        reader.close();

        htmlData = sb.toString();

        // if it is a html file then we return the contents so the correct url will be extracted from there, else return null
        if(htmlData.contains("id=\"download\""))
            return htmlData;
        else
            return null;
    }

    private String getDownloadURL(String stringData)
    {
        String prefix = "\"download\" href=", urlStr = new String();
        boolean startReading = false;
        int count = 0, quotes = 0;

        for(int i = 0; i < stringData.length(); i++)
        {
            if(startReading)
            {
                if(stringData.charAt(i) == '"')
                {
                    quotes++;

                    if(quotes >= 2)
                        break;

                    continue;
                }

                urlStr += stringData.charAt(i);

                continue;
            }

            if(stringData.charAt(i) == prefix.charAt(count))
                count++;
            else
                count = 0;

            if(count == prefix.length())
                startReading = true;
        }

        return urlStr;
    }
}
