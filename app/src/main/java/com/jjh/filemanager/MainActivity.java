package com.jjh.filemanager;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Environment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.os.Bundle;
import android.view.View;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.jjh.filemanager.bean.FileBean;
import com.jjh.filemanager.fragment.adapter.FragAdapter;
import com.jjh.filemanager.fragment.classifyFileFragment;
import com.jjh.filemanager.fragment.localFileFragment;

import org.litepal.tablemanager.Connector;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Formatter;
import java.util.List;

import permissions.dispatcher.NeedsPermission;
import permissions.dispatcher.OnNeverAskAgain;
import permissions.dispatcher.OnPermissionDenied;
import permissions.dispatcher.OnShowRationale;
import permissions.dispatcher.PermissionRequest;
import permissions.dispatcher.RuntimePermissions;

@RuntimePermissions
public class MainActivity extends FragmentActivity{

    private ViewPager vp;
    private RadioGroup rgTabButtons;
    private int flagPath = -1;
    private int INSIDE_STORAGE = 0;
    private int EXTERNAL_STORAGE = 1;
    private final int TYPE_MUSIC = 1;
    private final int TYPE_IMAGE = 2;
    private final int TYPE_TXT   = 3;
    private final int TYPE_VIDEO = 4;
    private final int TYPE_APK   = 5;
    private final int TYPE_ZIP   = 6;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //首次启动创建我的数据库，用于加密的时候使用
        Connector.getDatabase();

        //两个权限为同一个危险权限组，只需要判断其中一个权限，整组权限都可以通过
        if(ContextCompat.checkSelfPermission(MainActivity.this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){
            //调用该语句申请权限
            MainActivityPermissionsDispatcher.getMultiWithCheck(this);
        }
        else {
            initView();
        }
}

    public void initView(){
        //构造适配器
        List<Fragment> fragments = new ArrayList<Fragment>();
        localFileFragment localFileFragment = new localFileFragment();
        classifyFileFragment classifyFileFragment = new classifyFileFragment();
        fragments.add(localFileFragment);
        fragments.add(classifyFileFragment);
        FragAdapter adapter = new FragAdapter(getSupportFragmentManager(), fragments);

        //设定适配器，使用ViewPager实现滑动两个fragment.
        vp = (ViewPager)findViewById(R.id.vpContainer);
        vp.setAdapter(adapter);
        vp.addOnPageChangeListener(onPageChangeListener);

        rgTabButtons = (RadioGroup)findViewById(R.id.rgTabBtns);
        rgTabButtons.setOnCheckedChangeListener(onCheckedChangeListener);
        //设置RadioGroup的第一个RadioButton为初始状态为选中状态
        ((RadioButton)rgTabButtons.getChildAt(0)).setChecked(true);
        //不建议在onCreate刷新UI,而且这里刷新不了ViewPager里的Fragment子控件，
        // 只有在PagerView显示后才行即onCreate运行结束，于是在AsyncTask里面刷新
    }

    //在xml文件指明的点击方法，并非重写的监听事件
    public void onClick(View v) {
        String rootPath = Environment.getExternalStorageDirectory().getAbsolutePath();//手机自带外部存储根目录;
        String SDPath = FileUtil.getExtendedMemoryPath(MainActivity.this);
        switch (v.getId()){
            case R.id.inside_storage:
                flagPath = INSIDE_STORAGE;
                Intent insideIntent = new Intent(MainActivity.this, LocalFileActivity.class);
                insideIntent.putExtra("Path", rootPath);
                insideIntent.putExtra("flagPath", flagPath);
                startActivity(insideIntent);
//                finish();
                break;
            case R.id.external_storage:
                flagPath = EXTERNAL_STORAGE;
                Intent externalIntent = new Intent(MainActivity.this, LocalFileActivity.class);
                externalIntent.putExtra("Path", SDPath);
                externalIntent.putExtra("flagPath", flagPath);
                startActivity(externalIntent);
                break;
            case R.id.private_storage:
                Intent privateIntent = new Intent(MainActivity.this, GestureLockActivity.class);
                startActivity(privateIntent);
                break;
            case R.id.classify_music:
                Intent musicsIntent = new Intent(MainActivity.this, ClassifyFileActivity.class);
                musicsIntent.putExtra("Type", TYPE_MUSIC);
                startActivity(musicsIntent);
                break;
            case R.id.classify_image:
                Intent imagesIntent = new Intent(MainActivity.this, ClassifyFileActivity.class);
                imagesIntent.putExtra("Type", TYPE_IMAGE);
                startActivity(imagesIntent);
                break;
            case R.id.classify_txt:
                Intent txtsIntent = new Intent(MainActivity.this, ClassifyFileActivity.class);
                txtsIntent.putExtra("Type", TYPE_TXT);
                startActivity(txtsIntent);
                break;
            case R.id.classify_video:
                Intent videosIntent = new Intent(MainActivity.this, ClassifyFileActivity.class);
                videosIntent.putExtra("Type", TYPE_VIDEO);
                startActivity(videosIntent);
                break;
            case R.id.classify_installpackage:
                Intent apksIntent = new Intent(MainActivity.this, ClassifyFileActivity.class);
                apksIntent.putExtra("Type", TYPE_APK);
                startActivity(apksIntent);
                break;
            case R.id.classify_zip:
                Intent zipsIntent = new Intent(MainActivity.this, ClassifyFileActivity.class);
                zipsIntent.putExtra("Type", TYPE_ZIP);
                startActivity(zipsIntent);
                break;
            default:

        }
    }

    private ViewPager.OnPageChangeListener onPageChangeListener=new ViewPager.OnPageChangeListener() {

        @Override
        public void onPageSelected(int arg0) {
            //viewPager的第arg0页处于当前屏幕的时候，将当前的arg0值赋值给记录Fragment的标签
            //mCurrentFragment=arg0;

            //将下面tab的RadioGroup的第arg0个radioButton设为选中状态
            ((RadioButton)rgTabButtons.getChildAt(arg0)).setChecked(true);
        }

        @Override
        public void onPageScrolled(int arg0, float arg1, int arg2) {

        }

        @Override
        public void onPageScrollStateChanged(int arg0) {

        }
    };

    private RadioGroup.OnCheckedChangeListener onCheckedChangeListener=
            new RadioGroup.OnCheckedChangeListener() {

                @Override
                public void onCheckedChanged(RadioGroup group, int checkedId) {
                    //设置一个标签，用来记录当前所选的radioButton值
                    int checkedItem=0;
                    switch (checkedId) {
                        case R.id.rb_localFile:
                            checkedItem=0;
                            break;
                        case R.id.rb_classifyFile:
                            checkedItem=1;
                            break;
                    }
                    //将ViewPager的第checkedItem个页面设为当前屏幕的展示页面
                    vp.setCurrentItem(checkedItem);
                }
            };

    //以下是使用PermissionsDispatcher申请权限写的方法,当获取到权限时执行getMulti()方法
    @NeedsPermission({Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE})
    void getMulti() {
        initView();

    }


    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions,int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        MainActivityPermissionsDispatcher.onRequestPermissionsResult(this, requestCode, grantResults);
    }

    @OnShowRationale({Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE})
    void tt(final PermissionRequest request) {
        new AlertDialog.Builder(this)
                .setMessage("使用此功能需要同意读写文件的权限，否则无法正常使用")
                .setPositiveButton("继续", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        request.proceed();//继续执行请求
                    }
                }).setNegativeButton("取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                request.cancel();//取消执行请求
                finish();
            }
        }).show();
    }

    @OnPermissionDenied({Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE})
    void StorageDenied() {
        Toast.makeText(this, "已拒绝读写文件权限,将无法使用本应用", Toast.LENGTH_SHORT).show();
        finish();
    }

    @OnNeverAskAgain({Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE})
    void StorageNeverAsk() {
        Toast.makeText(this, "已拒绝读写文件权限,将无法使用本应用", Toast.LENGTH_SHORT).show();
        finish();
    }

}
