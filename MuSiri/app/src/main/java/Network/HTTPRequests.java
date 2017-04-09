package Network;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

import Parsing.CMDParser;
import Parsing.JSONParser;

public class HTTPRequests
{
    private static final String USER_AGENT = "Mozilla/5.0";

    public static interface updateGUIInterface
    {
        void updateGUI(String jsonResponse, ArrayList<Bitmap> thumbnails);
    }

    public HTTPRequests()
    {
    }

    // HTTP GET request
    public void sendGet(final String url, final updateGUIInterface UGI)
    {
        new Thread(new Runnable()
        {
            @Override
            public void run()
            {
                try
                {
                    URL urlObj = new URL(url);
                    String inputLine, jsonResponse = "";
                    int responseCode;
                    BufferedReader in;
                    HttpURLConnection con = (HttpURLConnection) urlObj.openConnection();
                    JSONParser jsonParser = new JSONParser();
                    ArrayList<Bitmap> thumbnails = new ArrayList<>();

                    con.setRequestMethod("GET");

                    //add request header
                    con.setRequestProperty("User-Agent", USER_AGENT);

                    // taking care of the response code
                    responseCode = con.getResponseCode();

                    if(responseCode != 200)
                    {
                        System.out.println("************ ResponseCode is: " + responseCode);

                        UGI.updateGUI(null, null);
                        return;
                    }

                    // getting the data from the url
                    in = new BufferedReader(new InputStreamReader(con.getInputStream()));

                    // reading from the buffer to the string
                    while ((inputLine = in.readLine()) != null)
                        jsonResponse += inputLine;

                    in.close();

                    // add the thumbnails of the videos to the arraylist
                    jsonParser.setJSONString(jsonResponse);

                    for(int i = 0; i < CMDParser.MAX_RESULTS*3; i++)
                    {
                        if(i % 3 == 0)
                        {
                            try {
                                Bitmap bitmap;
                                InputStream inputStream;

                                // gets the image from the web
                                inputStream = new URL(jsonParser.getFieldValue("url", i)).openStream();
                                bitmap = BitmapFactory.decodeStream(inputStream);

                                // scaling it to a fixed size and adding it to the arraylist
                                bitmap = Bitmap.createScaledBitmap(bitmap, 280, 130, false);
                                thumbnails.add(bitmap);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }

                    UGI.updateGUI(jsonResponse, thumbnails);
                } catch (Exception e) {
                    e.printStackTrace();
                    UGI.updateGUI(null, null);
                }
            }
        }).start();
    }
}
