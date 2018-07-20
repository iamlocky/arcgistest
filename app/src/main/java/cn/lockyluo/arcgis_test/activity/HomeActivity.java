package cn.lockyluo.arcgis_test.activity;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatButton;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import cn.lockyluo.arcgis_test.R;
import cn.lockyluo.arcgis_test.Utils.StartActivityUtil;
import cn.lockyluo.arcgis_test.Utils.ToastUtils;

public class HomeActivity extends AppCompatActivity {

    private Context context;
    private int requestCode = 0x00000011;
    private String[] permssions = new String[]{
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.INTERNET,
            Manifest.permission.ACCESS_FINE_LOCATION};

    @BindView(R.id.btn_gotomv)
    AppCompatButton btnGotomv;
    @BindView(R.id.btn_gotosv)
    AppCompatButton btnGotosv;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        ButterKnife.bind(this);
        context=this;
        requestPermission();

    }

    @OnClick(R.id.btn_gotomv)
    public void onBtnGotomvClicked() {
        StartActivityUtil.start(this,MapviewActivity.class);
    }

    @OnClick(R.id.btn_gotosv)
    public void onBtnGotosvClicked() {
        StartActivityUtil.start(this,SceneActivity.class);
    }

    public void requestPermission() {//申请权限
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
}
