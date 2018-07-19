package cn.lockyluo.arcgis_test;

import android.Manifest;
import android.content.ActivityNotFoundException;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatButton;
import android.support.v7.widget.AppCompatImageView;
import android.text.TextUtils;
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
import com.esri.arcgisruntime.mapping.ArcGISScene;
import com.esri.arcgisruntime.mapping.Basemap;
import com.esri.arcgisruntime.mapping.Viewpoint;
import com.esri.arcgisruntime.mapping.view.DefaultSceneViewOnTouchListener;
import com.esri.arcgisruntime.mapping.view.Graphic;
import com.esri.arcgisruntime.mapping.view.GraphicsOverlay;
import com.esri.arcgisruntime.mapping.view.SceneView;
import com.esri.arcgisruntime.symbology.SimpleMarkerSymbol;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import cn.lockyluo.arcgis_test.Utils.FileUtils;
import cn.lockyluo.arcgis_test.Utils.LocationUtils;
import cn.lockyluo.arcgis_test.Utils.SharedPerfUtils;
import cn.lockyluo.arcgis_test.Utils.ToastUtils;
import cn.lockyluo.arcgis_test.Utils.UrlUtils;
import cn.lockyluo.arcgis_test.model.BaiduGeo;
import cn.lockyluo.arcgis_test.model.ContextData;
import cn.lockyluo.arcgis_test.model.Geo;

public class MainActivity extends AppCompatActivity {
    @BindView(R.id.sceneview)
    SceneView sceneview;
    @BindView(R.id.btn_select)
    Button btnSelect;
    @BindView(R.id.btn_add)
    AppCompatButton btnAdd;
    @BindView(R.id.btn_sub)
    AppCompatButton btnSub;
    @BindView(R.id.btn_locate)
    AppCompatImageView btnLocate;
    private Context context;
    private int requestCode = 0x00000011;
    private String[] permssions = new String[]{
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.INTERNET,
            Manifest.permission.ACCESS_FINE_LOCATION};
    private int FILE_SELECT_CODE = 0x00000101;
    private static final String TAG = "MainActivity";
    private String path = "";
    private Basemap basemap;
    private ArcGISScene scene;
    private ArcGISTiledLayer tiledLayer;
    private Double scale = 65536.0;
    private Gson gson;
    private PopupWindow popupWin;
    private boolean onStart = true;
    GraphicsOverlay graphicsOverlay;
    private Double latitude = 23.050644;
    private Double longitude = 113.393676;
    private String geoDetailInfo;
    private BaiduGeo bean;
    private Geo lastGeo;
    private Geo currentGeo;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        context = this;
        requestPermission();
        ArcGISRuntimeEnvironment.setLicense("runtimelite,1000,rud4163659509,none,1JPJD4SZ8L4HC2EN0229");
        path = SharedPerfUtils.getString("path");
        if (TextUtils.isEmpty(path)) {
            loadDefault();
        } else {
            loadTpk();
        }
        sceneview.postDelayed(new Runnable() {
            @Override
            public void run() {
                Log.i(TAG, "onCreate: scale " + scale + " getScale" + getCurrentGeoPoint().getScale());
            }
        }, 1000);


        GsonBuilder gsonBuilder = new GsonBuilder();
        gsonBuilder.serializeNulls().serializeSpecialFloatingPointValues();
        gson = gsonBuilder.create();

        initView();
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
        sceneview.setOnTouchListener(new DefaultSceneViewOnTouchListener(sceneview) {
            @Override
            public boolean onSingleTapConfirmed(MotionEvent motionEvent) {
                if (popupWin!=null&&popupWin.isShowing()){
                    popupWin.dismiss();
                    return true;
                }

                android.graphics.Point screenPoint = new android.graphics.Point(Math.round(motionEvent.getX()), Math.round(motionEvent.getY()));
                Point point = sceneview.screenToBaseSurface(screenPoint);
                String s = CoordinateFormatter.toLatitudeLongitude(point, CoordinateFormatter.LatitudeLongitudeFormat.DECIMAL_DEGREES, 4);

                addPointToSurfaceView(point);

                showPopup(s);

                sceneview.setViewpoint(new Viewpoint(point.getY(), point.getX(), scale));
                return true;
            }

            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                return super.onTouch(view, motionEvent);
            }
        });
    }

    private void addPointToSurfaceView(Point point) {
        latitude=point.getY();
        longitude=point.getX();
        sceneview.getGraphicsOverlays().remove(graphicsOverlay);
        graphicsOverlay = new GraphicsOverlay();
        SimpleMarkerSymbol pointSymbol = new SimpleMarkerSymbol(SimpleMarkerSymbol.Style.CIRCLE, ContextCompat.getColor(context, R.color.colorPrimaryDark), 15);
        Graphic pointGraphic = new Graphic(point, pointSymbol);
        graphicsOverlay.getGraphics().add(pointGraphic);
        sceneview.getGraphicsOverlays().add(graphicsOverlay);
    }

    /**
     * 定位到指定位置，默认为广州大学城
     *
     * @param latitudeAndLongitude
     */
    private void changeViewpoint(double... latitudeAndLongitude) {

        if (latitudeAndLongitude.length == 2) {
            latitude = latitudeAndLongitude[0];
            longitude = latitudeAndLongitude[1];
            Geo geo = getCurrentGeoPoint();
            if (geo != null) {
                scale = geo.getScale();
            }
            scale = scale < 65536.0 ? scale : 65536.0;
        }
        if (!onStart) {
            sceneview.setViewpointAsync(new Viewpoint(latitude, longitude, scale));
        } else {
            onStart = false;
            sceneview.postDelayed(new Runnable() {
                @Override
                public void run() {
                    Point point = new Point(113.393676, 23.050644);
                    addPointToSurfaceView(point);
                    sceneview.setViewpoint(new Viewpoint(point.getY(), point.getX(), scale));
                }
            }, 500);
        }
    }


    public void loadDefault() {//加载在线地图
        tiledLayer = new ArcGISTiledLayer(
                "https://services.arcgisonline.com/ArcGIS/rest/services/World_Imagery/MapServer");
        Basemap basemap = new Basemap(tiledLayer);
        scene = new ArcGISScene(basemap);
        scale = 65536.0;
        sceneview.setScene(scene);
        changeViewpoint();
    }

    public void loadTpk() {//加载本地tpk地图
        if (TextUtils.isEmpty(path))
            return;
        ToastUtils.show(path);
        scale = 32270712.0;
        SharedPerfUtils.putString("path", path);
        TileCache tileCache = new TileCache(path);
        tiledLayer = new ArcGISTiledLayer(tileCache);

        basemap = new Basemap(tiledLayer);
        scene = new ArcGISScene(basemap);
        sceneview.setScene(scene);

        changeViewpoint();
    }

    public void requestPermission() {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(context, Manifest.permission.INTERNET) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, permssions, requestCode);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == this.requestCode) {
            for (int i = 0; i < permissions.length; i++) {
                if (grantResults[i] == PackageManager.PERMISSION_GRANTED) {
                    ToastUtils.show("权限" + permissions[i] + "申请成功");
                } else {
                    ToastUtils.show("权限" + permissions[i] + "申请失败");
                }
            }
        }
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

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode != RESULT_OK) {
            super.onActivityResult(requestCode, resultCode, data);
        } else if (requestCode == FILE_SELECT_CODE) {
            Uri uri = data.getData();
            try {
                path = FileUtils.getPath(context, uri);
                if (path.endsWith(".tpk")) {
                    loadTpk();
                } else {
                    ToastUtils.show("请选择tpk格式");
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }


    @OnClick(R.id.btn_select)
    public void onBtnSelectClicked() {
        chooseFile();
    }

    @OnClick(R.id.btn_add)
    public void onBtnAddClicked() {
        Geo geo = getCurrentGeoPoint();

        if (geo != null && geo.getTargetGeometry() != null) {
            scale = geo.getScale();
            scale /= 2;

            sceneview.setViewpointAsync(new Viewpoint(geo.getTargetGeometry().getY(), geo.getTargetGeometry().getX(), scale));
        }


    }

    @OnClick(R.id.btn_sub)
    public void onBtnSubClicked() {
        Geo geo = getCurrentGeoPoint();
        if (geo != null && geo.getTargetGeometry() != null) {
            scale = geo.getScale();
            scale *= 2;

            sceneview.setViewpointAsync(new Viewpoint(geo.getTargetGeometry().getY(), geo.getTargetGeometry().getX(), scale));
            Log.i(TAG, "onBtnSubClicked: " + scale + "\n" + sceneview.getCurrentViewpoint(Viewpoint.Type.CENTER_AND_SCALE).toJson());
        }
    }

    public Geo getCurrentGeoPoint() {
        currentGeo = null;
        try {
            currentGeo = gson.fromJson(sceneview.getCurrentViewpoint(Viewpoint.Type.CENTER_AND_SCALE).toJson(), Geo.class);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return currentGeo;
    }

    /**
     * 显示地址详情
     * @param title
     */
    public void showPopup(String title) {
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
                ClipboardManager clipboardManager = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
                ClipData clipData = ClipData.newPlainText("坐标", tv1.getText().toString() + "");
                clipboardManager.setPrimaryClip(clipData);
                ToastUtils.show("已复制");
                return true;
            }
        });
        tv2.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                ClipboardManager clipboardManager = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
                ClipData clipData = ClipData.newPlainText("城市", tv2.getText().toString() + "");
                clipboardManager.setPrimaryClip(clipData);
                ToastUtils.show("已复制");
                return true;
            }
        });

        popupWin.showAtLocation(sceneview.getRootView(), Gravity.BOTTOM, 0, 0);

        Thread thread = new Thread(new Runnable() {//调用百度api获取坐标详细信息
            @Override
            public void run() {

                try {
                    Geo geo = getCurrentGeoPoint();
                    Double latitudeTemp = geo.getTargetGeometry().getY();
                    Double longitudeTemp = geo.getTargetGeometry().getX();
                    latitude=latitudeTemp;
                    longitude=longitudeTemp;
                    Log.i(TAG, "run() returned: current " +latitudeTemp+" "+longitudeTemp+"\n"+
                    latitude+" "+longitude);
                    double y=0;
                    double x=0;
                    if (lastGeo!=null){
                        y=latitudeTemp-lastGeo.getTargetGeometry().getY();
                        x=longitudeTemp-lastGeo.getTargetGeometry().getX();
                    }

                    if (Math.pow(y,2)<0.000001 &&
                            Math.pow(x,2)<0.000001  &&
                            bean != null) {
                        Log.i(TAG, "run: show cache geo "+Math.pow(y,2));//坐标变化较小时显示缓存
                    } else {

                        geoDetailInfo = UrlUtils.sendGetRequest(ContextData.bdGeoUrl
                                .replace("666", ContextData.a)
                                .replace("latitude", latitude.toString())
                                .replace("longitude", longitude.toString()));

                        bean = gson.fromJson(geoDetailInfo, BaiduGeo.class);
                    }

                    if (currentGeo!=null){
                        lastGeo=new Geo();
                        lastGeo.setTargetGeometry(currentGeo.getTargetGeometry());
                    }


                    sceneview.post(new Runnable() {
                        @Override
                        public void run() {
                            if (tv2 != null) {
                                StringBuffer stringBuffer = new StringBuffer();
                                stringBuffer.append(bean.getResult().getFormatted_address()+" "+bean.getResult().getBusiness());
                                if (bean.getResult().getPois().size() > 0) {
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
        sceneview.resume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        sceneview.pause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        sceneview.dispose();
    }

    @OnClick(R.id.btn_locate)
    public void onBtnLocateClicked() {
        Location location = LocationUtils.getLocation(context);
        if (location == null) {
            ToastUtils.show("位置获取失败");
            return;
        }
        addPointToSurfaceView(new Point(location.getLongitude(), location.getLatitude()));
        changeViewpoint(location.getLatitude(), location.getLongitude());
    }

}
