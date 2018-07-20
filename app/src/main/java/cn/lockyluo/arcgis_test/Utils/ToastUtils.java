package cn.lockyluo.arcgis_test.Utils;

import android.util.Log;
import android.widget.Toast;

import cn.lockyluo.arcgis_test.App;

/**
 * Created by LockyLuo on 2018/7/18.
 */

public class ToastUtils {
    private static Toast toast;
    private static final String TAG = "ToastUtils";

    public static void show(String s) {
        if (toast != null) {
            toast.cancel();
        }
        toast = Toast.makeText(App.getInstance(), s + "", Toast.LENGTH_SHORT);
        toast.show();
        Log.i(TAG, "show: " + s);
    }
}
