package com.soul.main;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.haha.annotation.BindView;
import com.haha.annotation.OnClick;
import com.itbird.bindViewruntime.ItbirdBindView;
import com.itbird.bindview.annotation.ItbirdAopBinderView;
import com.itbird.bindview.annotation.ItbirdOnclick;
import com.soul.gpstest.R;

/**
 * @author : haha
 * @date : 2024-09-06
 * @desc : 测试BindView和onClick的逻辑
 * @version: 1.0
 */
//@ItbirdAopBinderView(R.layout.activity_main)
public class TestActivity extends AppCompatActivity {
    String TAG = this.getClass().getSimpleName();

//    @BindView(R.id.btn_skip_gps)
    @ItbirdAopBinderView(R.id.btn_skip_gps)
    TextView mTv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ItbirdBindView.bind(this);
//        BindUtils.bind(this);
        Log.d(TAG, "mTv == null: " + (mTv == null));
        if (mTv != null) {
            mTv.setText("not crash");
        }
    }

//    @OnClick(R.id.btn_skip_gps)
//    @ItbirdOnclick(R.id.btn_skip_gps)
    void onViewClick(View v){
        Toast.makeText(this, "button on click", Toast.LENGTH_SHORT).show();
    }
}
