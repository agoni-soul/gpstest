package com.soul.volume

import android.content.Context
import android.content.IntentFilter
import android.media.AudioManager
import android.media.Image
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import android.widget.SeekBar
import androidx.appcompat.app.AppCompatActivity
import com.soul.gpstest.R


/**
 *     author : yangzy33
 *     time   : 2024-05-09
 *     desc   :
 *     version: 1.0
 */
class VolumeActivity : AppCompatActivity() {
    private val TAG = javaClass.simpleName

    private val mSbVolume: SeekBar by lazy {
        findViewById(R.id.sb_volume_adjust)
    }
    private val mIvVolumeDown: ImageView by lazy {
        findViewById(R.id.iv_volume_down)
    }
    private val mIvVolumeUp: ImageView by lazy {
        findViewById(R.id.iv_volume_up)
    }

    private lateinit var mAudioManager: AudioManager
    private lateinit var mVolumeBroadReceiver: VolumeBroadReceiver

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_volume)
        mAudioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager
        initVolumeReceiver()
        initView()
    }

    private fun initView() {
        mSbVolume.progress = mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC)
        mSbVolume.max = mAudioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            mSbVolume.min = mAudioManager.getStreamMinVolume(AudioManager.STREAM_MUSIC)
        } else {
            mSbVolume.min = 0
        }
        Log.d(TAG, "volume: min = ${mSbVolume.min}, max = ${mSbVolume.max}")
        mSbVolume.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser) {
                    mAudioManager.setStreamVolume(
                        AudioManager.STREAM_MUSIC,
                        progress,
                        AudioManager.STREAM_MUSIC
                    )
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
            }

        })
        mIvVolumeDown.setOnClickListener {
            val volume = 0.coerceAtLeast(mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC) - 5)
            mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, volume, AudioManager.STREAM_MUSIC)
        }
        mIvVolumeUp.setOnClickListener {
            val volume = mAudioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
                .coerceAtMost(mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC) + 5)
            mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, volume, AudioManager.STREAM_MUSIC)
        }
    }

    private fun initVolumeReceiver() {
        mVolumeBroadReceiver = VolumeBroadReceiver()
        mVolumeBroadReceiver.setCallback(object : VolumeBroadReceiver.VolumeCallback {
            override fun handleVolumeChange() {
                mSbVolume.progress = mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC)
            }
        })
        val intentFilter = IntentFilter()
        intentFilter.addAction(VolumeBroadReceiver.VOLUME_CHANGED_ACTION)
        registerReceiver(mVolumeBroadReceiver, intentFilter)
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(mVolumeBroadReceiver)
    }
}