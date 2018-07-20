package cn.lockyluo.arcgis_test;

import android.app.Application;
import android.content.Context;

/**
 * Created by LockyLuo on 2018/7/18.
 */

public class App extends Application {
    public static Application application;

    @Override
    public void onCreate() {
        super.onCreate();
        application = this;
    }

    public static Context getInstance() {
        return application;
    }
}
