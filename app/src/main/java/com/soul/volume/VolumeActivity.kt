package com.soul.volume

import android.content.Context
import android.media.AudioManager
import android.os.Bundle
import android.util.Log
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

    private lateinit var mAudioManager: AudioManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_volume)
        mAudioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager
        mSbVolume.progress = mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC)
        mSbVolume.max = mAudioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
        mSbVolume.min = mAudioManager.getStreamMinVolume(AudioManager.STREAM_MUSIC)
        Log.d(TAG, "volume: min = ${mSbVolume.min}, max = ${mSbVolume.max}")
        mSbVolume.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser) {
                    mAudioManager.setStreamVolume(
                        AudioManager.STREAM_MUSIC,
                        progress,
                        AudioManager.FLAG_PLAY_SOUND
                    )
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
            }

        })
    }
}