package com.soul.volume

import android.content.Context
import android.content.IntentFilter
import android.media.AudioManager
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.soul.gpstest.R


/**
 *     author : yangzy33
 *     time   : 2024-05-09
 *     desc   :
 *     version: 1.0
 */
class VolumeActivity : AppCompatActivity() {
    private val TAG = javaClass.simpleName

    private val mVolumeRecyclerView: RecyclerView by lazy {
        findViewById(R.id.rv_volume)
    }

    private lateinit var mAudioManager: AudioManager
    private lateinit var mVolumeBroadReceiver: VolumeBroadReceiver
    private lateinit var mVolumeAdjustAdapter: VolumeAdjustAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_volume)
        mAudioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager
        initVolumeReceiver()
        initView()
    }

    private fun initView() {
        val layoutManager = LinearLayoutManager(this)
        layoutManager.orientation = LinearLayoutManager.VERTICAL
        mVolumeRecyclerView.layoutManager = layoutManager
        mVolumeAdjustAdapter = VolumeAdjustAdapter(this)
        val volumeList = mutableListOf(
            AudioManager.STREAM_MUSIC,
            AudioManager.STREAM_ALARM,
            AudioManager.STREAM_VOICE_CALL,
            AudioManager.STREAM_RING,
            AudioManager.STREAM_SYSTEM,
            AudioManager.STREAM_NOTIFICATION,
        )
        mVolumeAdjustAdapter.updateVolumeData(volumeList)
        mVolumeAdjustAdapter.setHasStableIds(true)
        mVolumeRecyclerView.adapter = mVolumeAdjustAdapter
    }

    private fun initVolumeReceiver() {
        mVolumeBroadReceiver = VolumeBroadReceiver()
        mVolumeBroadReceiver.setCallback(object : VolumeBroadReceiver.VolumeCallback {
            override fun handleVolumeChange() {
                mVolumeAdjustAdapter.notifyDataSetChanged()
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