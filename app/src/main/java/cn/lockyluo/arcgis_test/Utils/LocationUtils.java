package cn.lockyluo.arcgis_test.Utils;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.support.v4.app.ActivityCompat;

import java.util.List;

/**
 * Created by LockyLuo on 2018/7/19.
 */

public class LocationUtils {
    private static LocationManager locationManager;

    /**
     *
     * @param context
     * @return 当前的位置坐标
     */
    public static Location getLocation(Context context){
         locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        return beginLocation(context);
    }

    private static String judgeProvider(LocationManager locationManager) {
        List<String> prodiverlist = locationManager.getProviders(true);
        if(prodiverlist.contains(LocationManager.NETWORK_PROVIDER)){
            return LocationManager.NETWORK_PROVIDER;//网络定位
        }else if(prodiverlist.contains(LocationManager.GPS_PROVIDER)) {
            return LocationManager.GPS_PROVIDER;//GPS定位
        }else{
            ToastUtils.show("没有可用的位置提供器");
        }
        return null;
    }

    private static Location beginLocation(Context context) {
        //获得位置服务

        String provider = judgeProvider(locationManager);
        //有位置提供器的情况
        if (provider != null) {
            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED
                    && ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED) {
                return null;
            }
            return locationManager.getLastKnownLocation(provider);
        }else{
            //不存在位置提供器的情况
            ToastUtils.show("没有可用的位置提供器");
        }
        return null;
    }
}
