package cn.lockyluo.arcgis_test.activity;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatButton;
import android.support.v7.widget.AppCompatImageView;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.esri.arcgisruntime.ArcGISRuntimeEnvironment;
import com.esri.arcgisruntime.data.TileCache;
import com.esri.arcgisruntime.geometry.CoordinateFormatter;
import com.esri.arcgisruntime.geometry.Point;
import com.esri.arcgisruntime.layers.ArcGISTiledLayer;
import com.esri.arcgisruntime.mapping.ArcGISMap;
import com.esri.arcgisruntime.mapping.Basemap;
import com.esri.arcgisruntime.mapping.Viewpoint;
import com.esri.arcgisruntime.mapping.view.BackgroundGrid;
import com.esri.arcgisruntime.mapping.view.DefaultMapViewOnTouchListener;
import com.esri.arcgisruntime.mapping.view.Graphic;
import com.esri.arcgisruntime.mapping.view.GraphicsOverlay;
import com.esri.arcgisruntime.mapping.view.MapView;
import com.esri.arcgisruntime.symbology.SimpleMarkerSymbol;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;

import java.io.File;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import cn.lockyluo.arcgis_test.R;
import cn.lockyluo.arcgis_test.Utils.ClipboardUtil;
import cn.lockyluo.arcgis_test.Utils.FileUtils;
import cn.lockyluo.arcgis_test.Utils.LocationUtils;
import cn.lockyluo.arcgis_test.Utils.SharedPerfUtils;
import cn.lockyluo.arcgis_test.Utils.ToastUtils;
import cn.lockyluo.arcgis_test.Utils.UrlUtils;
import cn.lockyluo.arcgis_test.model.BaiduGeo;
import cn.lockyluo.arcgis_test.model.ContextData;

public class MapviewActivity extends AppCompatActivity {

    @BindView(R.id.mapview)
    MapView mapview;
    @BindView(R.id.btn_select)
    Button btnSelect;
    @BindView(R.id.btn_locate)
    AppCompatImageView btnLocate;
    @BindView(R.id.btn_add)
    AppCompatButton btnAdd;
    @BindView(R.id.btn_sub)
    AppCompatButton btnSub;


    private Context context;
    private int FILE_SELECT_CODE = 0x00000102;
    private static final String TAG = "SceneActivity";
    private String tpkPath = "";
    private Basemap basemap;
    private ArcGISMap map;
    private ArcGISTiledLayer tiledLayer;
    private Double scale = 65536.0;
    private Gson gson;
    private PopupWindow popupWin;
    private boolean onStart = true;
    private GraphicsOverlay graphicsOverlay;
    private Double latitude = 23.050644;
    private Double longitude = 113.393676;
    private String geoDetailInfo;
    private BaiduGeo bean;

    private Double lastLatitude;
    private Double lastLongitude;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mapview);
        ButterKnife.bind(this);
        context = this;
        setTitle(getString(R.string.mv));

        ArcGISRuntimeEnvironment.setLicense(ContextData.arcLicense);
        mapview.setAttributionTextVisible(false);//去除水印，仅用于技术研究

        BackgroundGrid backgroundGrid = new BackgroundGrid();//地图背景网格
        backgroundGrid.setColor(ContextCompat.getColor(context, R.color.white));
        backgroundGrid.setGridLineColor(ContextCompat.getColor(context, R.color.white));
        backgroundGrid.setGridLineWidth(1);
        mapview.setBackgroundGrid(backgroundGrid);

        tpkPath = SharedPerfUtils.getString("tpkPath");
        if (TextUtils.isEmpty(tpkPath)) {
            loadDefault();
        } else {
            loadTpk();
        }


        GsonBuilder gsonBuilder = new GsonBuilder();
        gsonBuilder.serializeNulls().serializeSpecialFloatingPointValues();
        gson = gsonBuilder.create();

        initView();
    }

    public void loadDefault() {//加载在线地图
        String mapServer = "https://services.arcgisonline.com/ArcGIS/rest/services/World_Imagery/MapServer";

        tiledLayer = new ArcGISTiledLayer(
                mapServer);
        Basemap basemap = new Basemap(tiledLayer);
        map = new ArcGISMap(basemap);
        mapview.setMap(map);
        moveToMyLocation();
    }

    public void loadTpk() {//加载本地tpk地图
        if (TextUtils.isEmpty(tpkPath))
            return;
        else if (!new File(tpkPath).exists()) {
            ToastUtils.show("未找到tpk文件");
            return;
        }
        ToastUtils.show(tpkPath);
        SharedPerfUtils.putString("tpkPath", tpkPath);
        TileCache tileCache = new TileCache(tpkPath);
        tiledLayer = new ArcGISTiledLayer(tileCache);

        basemap = new Basemap(tiledLayer);
        map = new ArcGISMap(basemap);
        mapview.setMap(map);

        moveToMyLocation();
    }

    private void initView() {
        btnSelect.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                loadDefault();
                return true;
            }
        });

        /**
         * 监听触控操作
         */
        mapview.setOnTouchListener(new DefaultMapViewOnTouchListener(context, mapview) {
            @Override
            public boolean onSingleTapConfirmed(MotionEvent motionEvent) {
                if (popupWin != null && popupWin.isShowing()) {
                    popupWin.dismiss();
                    return true;
                }

                android.graphics.Point screenPoint = new android.graphics.Point(Math.round(motionEvent.getX()), Math.round(motionEvent.getY()));
                final Point point = mapview.screenToLocation(screenPoint);

                final String pointString = CoordinateFormatter.toLatitudeLongitude(point, CoordinateFormatter.LatitudeLongitudeFormat.DECIMAL_DEGREES, 6);

                String[] sp = splitPoint(pointString);
                latitude = Double.parseDouble(sp[0]);
                longitude = Double.parseDouble(sp[1]);


                mapview.setViewpointAsync(new Viewpoint(latitude, longitude, mapview.getMapScale()),0.2f).addDoneListener(new Runnable() {
                    @Override
                    public void run() {
                        showPopup(pointString);
                        addPointToSurfaceView(point);

                    }
                });
                return true;
            }

            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                return super.onTouch(view, motionEvent);
            }
        });
    }

    public String[] splitPoint(String pointString) {
        pointString = pointString.replace("N", "").replace("S", "").replace("E", "").replace("S", "");
        Log.d(TAG, "splitPoint() returned: " + pointString);
        String[] ss = pointString.split(" ");
        return ss;
    }


    /**
     * 定位视图到指定位置，默认为广州大学城
     *
     * @param latitudeAndLongitude
     */
    private void changeViewpoint(double... latitudeAndLongitude) {

        if (latitudeAndLongitude.length == 2) {
            latitude = latitudeAndLongitude[0];
            longitude = latitudeAndLongitude[1];
            scale = mapview.getMapScale();

            scale = scale < 65536.0 ? scale : 65536.0;
        }
        if (!onStart) {
            mapview.setViewpointAsync(new Viewpoint(latitude, longitude, scale), 0.2f).addDoneListener(new Runnable() {
                @Override
                public void run() {
                    new Handler(getMainLooper()).post(new Runnable() {
                        @Override
                        public void run() {
                            addPointToSurfaceView(getCenterPoint());
                        }
                    });
                }
            });
        } else {
            onStart = false;
            new Handler(getMainLooper()).postDelayed(new Runnable() {
                @Override
                public void run() {
                    mapview.setViewpoint(new Viewpoint(latitude, longitude, scale));

                }
            }, 500);
        }
    }

    /**
     * 在指定坐标点添加标记
     *
     * @param point
     */
    private void addPointToSurfaceView(Point point) {
        mapview.getGraphicsOverlays().remove(graphicsOverlay);
        graphicsOverlay = new GraphicsOverlay();
        SimpleMarkerSymbol pointSymbol = new SimpleMarkerSymbol(SimpleMarkerSymbol.Style.CIRCLE, ContextCompat.getColor(context, R.color.colorPrimaryDark), 15);
        Graphic pointGraphic = new Graphic(point, pointSymbol);
        graphicsOverlay.getGraphics().add(pointGraphic);
        mapview.getGraphicsOverlays().add(graphicsOverlay);
    }

    private Point getCenterPoint() {
        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);
        android.graphics.Point point = new android.graphics.Point(dm.widthPixels / 2, dm.heightPixels / 2);
        Log.d(TAG, "getCenterPoint() returned: " + point);
        Point realPoint = mapview.screenToLocation(point);
        Log.d(TAG, "getCenterPoint() returned: " + realPoint);
        return realPoint;
    }


    private void chooseFile() {
        ToastUtils.show("请选择");
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("*/*");
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        try {
            startActivityForResult(Intent.createChooser(intent, "选择文件"), FILE_SELECT_CODE);
        } catch (ActivityNotFoundException ex) {
            ToastUtils.show("没有文件管理器，请安装");
        }
    }

    /**
     * 显示地址详情popupwindow
     *
     * @param title
     */
    public void showPopup(final String title) {
        if (popupWin != null && popupWin.isShowing()) {
            popupWin.dismiss();
        }

        View view = LayoutInflater.from(context).inflate(R.layout.popupwindow_layout, null);
        popupWin = new PopupWindow(view, LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT, false);
        popupWin.setAnimationStyle(R.style.popupwin_anim);
        final TextView tv1 = view.findViewById(R.id.tv_title);
        final TextView tv2 = view.findViewById(R.id.tv_content);
        final AppCompatImageView btn_dimiss = view.findViewById(R.id.iv_btn_colse_popup);
        popupWin.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        popupWin.setTouchable(true);

        btn_dimiss.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                popupWin.dismiss();
            }
        });

        tv1.setText(title);

        tv1.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                ClipboardUtil.putString("坐标", tv1.getText().toString());

                return true;
            }
        });
        tv2.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                ClipboardUtil.putString("详情", tv2.getText().toString());

                return true;
            }
        });

        popupWin.showAtLocation(mapview.getRootView(), Gravity.BOTTOM, 0, 0);

        final Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                //调用百度api获取坐标详细信息
                try {
//                    Log.i(TAG, "run() returned: current " + latitudeTemp + " " + longitudeTemp + "\n" +
//                            latitude + " " + longitude);
                    double y = 1;
                    double x = 1;
                    Log.d(TAG, "run() returned:last " + lastLatitude + "\n now" + latitude);
                    if (lastLatitude != null) {
                        y = latitude - lastLatitude;
                        x = longitude - lastLongitude;
                    }

                    if (Math.pow(y, 2) < 0.000001 &&
                            Math.pow(x, 2) < 0.000001 &&
                            bean != null) {
                        Log.i(TAG, "run: show cache last geo");//坐标变化较小时跳过访问百度api，直接显示缓存
                    } else {
                        lastLatitude = Double.valueOf(latitude);
                        lastLongitude = Double.valueOf(longitude);
                        bean=null;
                        geoDetailInfo = UrlUtils.sendGetRequest(ContextData.bdGeoUrl
                                .replace("666", ContextData.a)
                                .replace("latitude", latitude.toString())
                                .replace("longitude", longitude.toString()));

                        try {
                            bean = gson.fromJson(geoDetailInfo, BaiduGeo.class);
                        } catch (Exception e) {
                            e.printStackTrace();
                            bean=null;
                        }
                        Log.d(TAG, "run() returned: " + geoDetailInfo);
                    }
//
//                    lastLatitude=latitude;
//                    lastLongitude=longitude;

                    new Handler(getMainLooper()).post(new Runnable() {
                        @Override
                        public void run() {
                            if (tv2 != null&&bean!=null) {
                                StringBuffer stringBuffer = new StringBuffer();
                                stringBuffer.append(bean.getResult().getFormatted_address() + " " + bean.getResult().getBusiness());
                                if (bean.getResult().getPois().size() > 0) {//取出百度数据中附近第一个地址
                                    stringBuffer.append("\n" +
                                            bean.getResult().getPois().get(0).getName() +
                                            "\n" +
                                            bean.getResult().getPois().get(0).getAddr());
                                }
                                tv2.setText(stringBuffer);
                            }
                        }
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        thread.start();

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode != RESULT_OK) {
            super.onActivityResult(requestCode, resultCode, data);
        } else if (requestCode == FILE_SELECT_CODE) {
            Uri uri = data.getData();
            try {
                tpkPath = FileUtils.getPath(context, uri);
                if (tpkPath.endsWith(".tpk")) {
                    loadTpk();
                } else {
                    ToastUtils.show("请选择tpk格式");
                    tpkPath = "";
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onBackPressed() {
        if (popupWin != null && popupWin.isShowing()) {
            popupWin.dismiss();
            return;
        }
        super.onBackPressed();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mapview.resume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mapview.pause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mapview.dispose();
    }


    @OnClick(R.id.btn_select)
    public void onBtnSelectClicked() {
        chooseFile();
    }

    @OnClick(R.id.btn_locate)
    public void onBtnLocateClicked() {//定位到本机位置
        moveToMyLocation();

    }

    public boolean moveToMyLocation(){
        Location location = LocationUtils.getLocation(context);
        if (location == null) {
            ToastUtils.show("位置获取失败");
            return false;
        }
        changeViewpoint(location.getLatitude(), location.getLongitude());
        return true;
    }

    @OnClick(R.id.btn_add)
    public void onBtnAddClicked() {
        scale = mapview.getMapScale();
        scale /= 2;
        mapview.setViewpointScaleAsync(scale);
    }

    @OnClick(R.id.btn_sub)
    public void onBtnSubClicked() {
        scale = mapview.getMapScale();
        scale *= 2;
        mapview.setViewpointScaleAsync(scale);
    }
}
