package Network;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class HTTPRequests
{
    private static final String USER_AGENT = "Mozilla/5.0";

    // HTTP GET request
    public static void sendGet(String url) throws Exception
    {
        URL urlObj = new URL(url);
        HttpURLConnection con = (HttpURLConnection) urlObj.openConnection();
        String inputLine, response = "";
        int responseCode;
        BufferedReader in;

        con.setRequestMethod("GET");

        //add request header
        con.setRequestProperty("User-Agent", USER_AGENT);

        // taking care of the response code
        responseCode = con.getResponseCode();

        if(responseCode != 200)
        {
            throw (new Exception("response code is not 200"));
        }

        // getting the data from the url
        in = new BufferedReader(new InputStreamReader(con.getInputStream()));

        // reading from the buffer to the string
        while ((inputLine = in.readLine()) != null)
            response += inputLine;

        in.close();
    }
}
