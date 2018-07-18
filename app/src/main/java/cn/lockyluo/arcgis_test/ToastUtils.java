package cn.lockyluo.arcgis_test;

import android.util.Log;
import android.widget.Toast;

/**
 * Created by BassXS on 2018/7/18.
 */

public class ToastUtils {
    private static Toast toast;
    private static final String TAG = "ToastUtils";
    public static void show(String s){
        if (toast!=null) {
        toast.cancel();
        }
        toast=Toast.makeText(App.getInstance(), s, Toast.LENGTH_SHORT);
        toast.show();
        Log.i(TAG, "show: "+s);
    }
}
