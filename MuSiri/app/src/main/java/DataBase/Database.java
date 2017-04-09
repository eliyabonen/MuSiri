package DataBase;

import android.content.Context;
import android.content.SharedPreferences;

public class Database
{
    public static String SONGS_DATABASE = "recentSongs";
    public static String PATHS_DATABASE = "paths";

    private Context context;

    public Database(Context context)
    {
        this.context = context;
    }

    public void saveStringPreference(String fileName, String key, String value)
    {
        context.getSharedPreferences(fileName, Context.MODE_PRIVATE).edit().putString(key, value).apply();
    }

    public void saveIntPreference(String fileName, String key, int value)
    {
        context.getSharedPreferences(fileName, Context.MODE_PRIVATE).edit().putInt(key, value).apply();
    }

    public String getStringValue(String fileName, String key)
    {
        return context.getSharedPreferences(fileName, Context.MODE_PRIVATE).getString(key, "null");
    }

    public int getIntValue(String fileName, String key)
    {
        return context.getSharedPreferences(fileName, Context.MODE_PRIVATE).getInt(key, 0);
    }

    public void clearPreference(String fileName)
    {
        context.getSharedPreferences(fileName, Context.MODE_PRIVATE).edit().clear().apply();
    }
}
