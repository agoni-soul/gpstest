package com.soul.main;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.haha.service.annotation.BindView;
import com.haha.service.annotation.OnClick;
import com.soul.gpstest.R;
import com.haha.service.annotation.runtime.BindViewUtils;

/**
 * @author : haha
 * @date : 2024-09-06
 * @desc : 测试BindView和onClick的逻辑
 * @version: 1.0
 */
@BindView(R.layout.activity_main)
public class TestActivity extends AppCompatActivity {
    String TAG = this.getClass().getSimpleName();

    @BindView(R.id.btn_skip_gps)
    TextView mTv;

    @BindView(R.id.btn_skip_remote_view)
    Button mBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        BindViewUtils.bind(this);
        Log.d(TAG, "mTv == null: " + (mTv == null));
        if (mTv != null) {
            mTv.setText("not crash");
        }
        if (mBtn != null) {
            mBtn.setText("haha");
        }
    }

    @OnClick({R.id.btn_skip_gps, R.id.btn_skip_remote_view, R.id.btn_skip_network})
    void onViewClick(View v){
        if (v.getId() == R.id.btn_skip_gps) {
            Toast.makeText(this, "gps", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "hahanihao", Toast.LENGTH_SHORT).show();
        }
    }
}
