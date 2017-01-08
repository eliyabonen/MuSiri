package DataBase;

import android.content.Context;
import android.content.SharedPreferences;

public class DatabaseInterface
{
    private SharedPreferences sharedPref;

    public DatabaseInterface(String fileName, Context context)
    {
        sharedPref = context.getSharedPreferences(fileName, Context.MODE_PRIVATE);
    }

    public void savePreference(String key, String value)
    {
        SharedPreferences.Editor editor = sharedPref.edit();

        editor.putString(key, value);
        editor.apply();
    }

    public String getStringValue(String key)
    {
        return (sharedPref.getString(key, "null"));
    }
}
