package cn.lockyluo.arcgis_test;

import android.Manifest;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatButton;
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
import com.esri.arcgisruntime.mapping.view.SceneView;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
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
    private Context context;
    private int requestCode = 0x00000011;
    private String[] permssions = new String[]{
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.INTERNET};
    private int FILE_SELECT_CODE = 0x00000101;
    private static final String TAG = "MainActivity";
    private String path = "";
    private Basemap basemap;
    private ArcGISScene scene;
    private ArcGISTiledLayer tiledLayer;
    private double scale = 65536;
    private Gson gson;
    private PopupWindow popupWin;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        context = this;
        requestPermission();
        ArcGISRuntimeEnvironment.setLicense("runtimelite,1000,rud4163659509,none,1JPJD4SZ8L4HC2EN0229");
        path=SharedPerfUtils.getString("path");
        if (TextUtils.isEmpty(path)) {
            loadDefault();
        }else {
            loadTpk();
        }

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

        sceneview.setOnTouchListener(new DefaultSceneViewOnTouchListener(sceneview) {
            @Override
            public boolean onSingleTapConfirmed(MotionEvent motionEvent) {

                android.graphics.Point screenPoint = new android.graphics.Point(Math.round(motionEvent.getX()), Math.round(motionEvent.getY()));
                Point point = sceneview.screenToBaseSurface(screenPoint);
                String s = CoordinateFormatter.toLatitudeLongitude(point, CoordinateFormatter.LatitudeLongitudeFormat.DECIMAL_DEGREES, 4);
                showPopup(s);
                sceneview.setViewpointAsync(new Viewpoint(point.getY(), point.getX(), scale));
                return true;
            }
        });
    }

    public void loadDefault() {
        tiledLayer = new ArcGISTiledLayer(
                "https://services.arcgisonline.com/ArcGIS/rest/services/World_Imagery/MapServer");
        Basemap basemap = new Basemap(tiledLayer);
        scene = new ArcGISScene(basemap);
        sceneview.setScene(scene);
        sceneview.setViewpointAsync(new Viewpoint(23.050644, 113.393676, scale));
    }

    public void loadTpk() {
        if (TextUtils.isEmpty(path))
            return;
        ToastUtils.show(path);
        scale=32270712.0;
        SharedPerfUtils.putString("path",path);
        TileCache tileCache = new TileCache(path);
        tiledLayer = new ArcGISTiledLayer(tileCache);

        basemap = new Basemap(tiledLayer);
        scene = new ArcGISScene(basemap);
        sceneview.setScene(scene);

        sceneview.setViewpointAsync(new Viewpoint(23.050644, 113.393676, scale));
    }

    public void requestPermission() {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(context, Manifest.permission.INTERNET) != PackageManager.PERMISSION_GRANTED) {
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
//                Log.i(TAG, "onActivityResult: " + FileUtils.getPath(context,uri));
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
        Geo geo = null;
        try {
            geo = gson.fromJson(sceneview.getCurrentViewpoint(Viewpoint.Type.CENTER_AND_SCALE).toJson(), Geo.class);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return geo;
    }

    public void showPopup(String s) {
        if (popupWin != null && popupWin.isShowing()) {
            popupWin.dismiss();
        }

        View view = LayoutInflater.from(context).inflate(R.layout.popupwindow_layout, null);
        popupWin = new PopupWindow(view, LinearLayout.LayoutParams.MATCH_PARENT,LinearLayout.LayoutParams.WRAP_CONTENT,true);
        TextView tv1 = view.findViewById(R.id.tv_title);
        TextView tv2 = view.findViewById(R.id.tv_content);
        popupWin.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        popupWin.setTouchable(true);
        tv1.setText(s);
        popupWin.showAtLocation(sceneview.getRootView(), Gravity.BOTTOM,0,0);
//        popupWin.showAsDropDown(sceneview.getRootView());
//        if (Build.VERSION.SDK_INT == 24) {
//            View anchor=sceneview.getRootView();
//            Rect rect = new Rect();
//            anchor.getGlobalVisibleRect(rect);
//            int h = anchor.getResources().getDisplayMetrics().heightPixels - rect.bottom;
//            popupWin.setHeight(h);
//        }
    }
}
