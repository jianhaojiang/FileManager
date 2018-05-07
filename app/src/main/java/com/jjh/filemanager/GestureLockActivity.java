package com.jjh.filemanager;

import android.content.Intent;
import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.syd.oden.gesturelock.view.GestureLockViewGroup;
import com.syd.oden.gesturelock.view.listener.GestureEventListener;
import com.syd.oden.gesturelock.view.listener.GesturePasswordSettingListener;
import com.syd.oden.gesturelock.view.listener.GestureUnmatchedExceedListener;

public class GestureLockActivity extends AppCompatActivity {
    private GestureLockViewGroup mGestureLockViewGroup;
    private boolean isReset = false;
    private static final String TAG = "GestureLockActivity";
    private TextView tv_state;
//    private TextView tv_reset;
    private final int PRIVATE_FILE   = 8;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gesture_lock);
        mGestureLockViewGroup = (GestureLockViewGroup) findViewById(R.id.gesturelock);
        tv_state = (TextView) findViewById(R.id.tv_state);
//        tv_reset = (TextView) findViewById(R.id.tv_reset);
        initGesture();
        setGestureWhenNoSet();

    }

    private void initGesture() {
        gestureEventListener();
        gesturePasswordSettingListener();
        gestureRetryLimitListener();
    }

    public void onClick(View v) {
        switch (v.getId()){
            case R.id.tv_reset:
                isReset = true;
                tv_state.setTextColor(Color.argb(255,97,97,97));
                tv_state.setText("请输入原手势密码");
                mGestureLockViewGroup.setRetryTimes(3);
                mGestureLockViewGroup.resetView();
                break;
            default:

        }
    }

    //判断手势密码是否正确
    private void gestureEventListener() {
        mGestureLockViewGroup.setGestureEventListener(new GestureEventListener() {
            @Override
            public void onGestureEvent(boolean matched) {
                Log.d(TAG, "onGestureEvent matched: " + matched);
                if (!matched) {
                    tv_state.setTextColor(Color.RED);
                    tv_state.setText("手势密码错误");
                } else {
                    if (isReset) {
                        isReset = false;
                        Toast.makeText(GestureLockActivity.this, "清除成功!", Toast.LENGTH_SHORT).show();
                        resetGesturePattern();
                    } else {
                        tv_state.setTextColor(Color.argb(255,97,97,97));
                        tv_state.setText("手势密码正确");
                        Intent privateIntent = new Intent(GestureLockActivity.this, ClassifyFileActivity.class);
                        privateIntent.putExtra("Type", PRIVATE_FILE);
                        startActivity(privateIntent);
                    }
                }
            }
        });
    }

    //设置手势密码
    private void gesturePasswordSettingListener() {
        mGestureLockViewGroup.setGesturePasswordSettingListener(new GesturePasswordSettingListener() {
            @Override
            public boolean onFirstInputComplete(int len) {
                if (len > 3) {
                    tv_state.setTextColor(Color.argb(255,97,97,97));
                    tv_state.setText("再次绘制手势密码");
                    return true;
                } else {
                    tv_state.setTextColor(Color.RED);
                    tv_state.setText("最少连接4个点，请重新输入!");
                    return false;
                }
            }

            @Override
            public void onSuccess() {
                tv_state.setTextColor(Color.argb(255,97,97,97));
                Toast.makeText(GestureLockActivity.this, "密码设置成功!", Toast.LENGTH_SHORT).show();
                tv_state.setText("请输入手势密码解锁!");
            }

            @Override
            public void onFail() {
                tv_state.setTextColor(Color.RED);
                tv_state.setText("与上一次绘制不一致，请重新绘制");
            }
        });
    }

    //清除手势密码
    private void resetGesturePattern() {
        mGestureLockViewGroup.removePassword();
        setGestureWhenNoSet();
        mGestureLockViewGroup.resetView();
    }

    //密码错误监听
    private void gestureRetryLimitListener() {
        mGestureLockViewGroup.setGestureUnmatchedExceedListener(3, new GestureUnmatchedExceedListener() {
            @Override
            public void onUnmatchedExceedBoundary() {
                tv_state.setTextColor(Color.RED);
                tv_state.setText("错误次数过多，请稍后再试!");
            }

        });
    }

    private void setGestureWhenNoSet() {
        if (!mGestureLockViewGroup.isSetPassword()){
            Log.d(TAG, "未设置密码，开始设置密码");
            tv_state.setTextColor(Color.argb(255,97,97,97));
            tv_state.setText("绘制新手势密码");
        }
    }


}
