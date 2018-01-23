package com.jjh.filemanager;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;
import android.os.Bundle;
import android.view.View;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import com.jjh.filemanager.fragment.adapter.FragAdapter;
import com.jjh.filemanager.fragment.classifyFileFragment;
import com.jjh.filemanager.fragment.localFileFragment;

import java.util.ArrayList;
import java.util.List;

import permissions.dispatcher.NeedsPermission;
import permissions.dispatcher.OnShowRationale;
import permissions.dispatcher.PermissionRequest;
import permissions.dispatcher.RuntimePermissions;

@RuntimePermissions
public class MainActivity extends FragmentActivity implements View.OnClickListener{

    private ViewPager vp;
    private RadioGroup rgTabButtons;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //构造适配器
        List<Fragment> fragments = new ArrayList<Fragment>();
        fragments.add(new localFileFragment());
        fragments.add(new classifyFileFragment());
        FragAdapter adapter = new FragAdapter(getSupportFragmentManager(), fragments);

        //设定适配器，使用ViewPager实现滑动两个fragment.
        vp = (ViewPager)findViewById(R.id.vpContainer);
        vp.setAdapter(adapter);
        vp.addOnPageChangeListener(onPageChangeListener);

        rgTabButtons = (RadioGroup)findViewById(R.id.rgTabBtns);
        rgTabButtons.setOnCheckedChangeListener(onCheckedChangeListener);
//        //设置RadioGroup的第一个RadioButton为初始状态为选中状态
        ((RadioButton)rgTabButtons.getChildAt(0)).setChecked(true);

//        //调用该语句申请权限，申请权限后执行getMulti方法
        MainActivityPermissionsDispatcher.getMultiWithCheck(this);

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.inside_storage:
                Intent intent = new Intent(MainActivity.this, LocalFileActivity.class);
                startActivity(intent);
//                finish();
                break;
            case R.id.external_storage:
                break;
            default:

        }
    }

    private ViewPager.OnPageChangeListener onPageChangeListener=new ViewPager.OnPageChangeListener() {

        @Override
        public void onPageSelected(int arg0) {
            //viewPager的第arg0页处于当前屏幕的时候，将当前的arg0值赋值给记录Fragment的标签
//            mCurrentFragment=arg0;
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
            //mCurrentFragment=checkedItem;
        }
    };

    //以下是使用PermissionsDispatcher申请权限写的方法
    @NeedsPermission({Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE})
    void getMulti() {
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


}