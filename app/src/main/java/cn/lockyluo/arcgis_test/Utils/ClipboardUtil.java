package cn.lockyluo.arcgis_test.Utils;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;

import cn.lockyluo.arcgis_test.App;

/**
 * Created by LockyLuo on 2018/7/20.
 */

public class ClipboardUtil {
    public static void putString(String label,String text){
        ClipboardManager clipboardManager = (ClipboardManager) App.getInstance().getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clipData = ClipData.newPlainText(label, text + "");
        clipboardManager.setPrimaryClip(clipData);
        ToastUtils.show("已复制"+label);
    }
}
