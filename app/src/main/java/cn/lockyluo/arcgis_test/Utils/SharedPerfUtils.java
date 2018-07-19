package cn.lockyluo.arcgis_test.Utils;

import android.content.Context;
import android.content.SharedPreferences;

import cn.lockyluo.arcgis_test.App;

/**
 * Created by LockyLuo on 2018/7/18.
 */

public class SharedPerfUtils {
    private static SharedPreferences preferences= App.getInstance().getSharedPreferences("cache", Context.MODE_PRIVATE);
    public static void putString(String key,String value){
        SharedPreferences.Editor editor=preferences.edit();
        editor.putString(key,value);
        editor.commit();
    }

    public static String getString(String key){
        return preferences.getString(key,"");
    }
}
