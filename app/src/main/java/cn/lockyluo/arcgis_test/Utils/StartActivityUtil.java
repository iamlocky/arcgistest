package cn.lockyluo.arcgis_test.Utils;

import android.content.Context;
import android.content.Intent;

/**
 * Created by LockyLuo on 2018/7/20.
 */

public class StartActivityUtil {
    public static void start(Context context,Class clazz){
        Intent intent=new Intent(context,clazz);
        context.startActivity(intent);
    }
}
