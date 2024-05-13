package com.soul.volume

import android.content.Context
import android.content.IntentFilter
import android.media.AudioManager
import android.media.MediaPlayer
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.soul.base.BaseActivity
import com.soul.base.BaseMvvmActivity
import com.soul.base.BaseViewModel
import com.soul.gpstest.R
import com.soul.gpstest.databinding.ActivityVolumeBinding


/**
 *     author : yangzy33
 *     time   : 2024-05-09
 *     desc   :
 *     version: 1.0
 */
class VolumeActivity : BaseMvvmActivity<ActivityVolumeBinding, BaseViewModel>() {

    private val mVolumeRecyclerView: RecyclerView by lazy {
        findViewById(R.id.rv_volume)
    }

    private lateinit var mAudioManager: AudioManager
    private lateinit var mMediaPlayer: MediaPlayer
    private lateinit var mVolumeBroadReceiver: VolumeBroadReceiver
    private lateinit var mVolumeAdjustAdapter: VolumeAdjustAdapter

    override fun getViewModelClass(): Class<BaseViewModel> = BaseViewModel::class.java

    override fun getLayoutId(): Int = R.layout.activity_volume

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initVolumeReceiver()
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

    override fun initView() {
        mViewDataBinding?.apply {
            val layoutManager = LinearLayoutManager(mContext)
            layoutManager.orientation = LinearLayoutManager.VERTICAL
            rvVolume.layoutManager = layoutManager
            mVolumeAdjustAdapter = VolumeAdjustAdapter(mContext)
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
    }

    override fun initData() {
        mAudioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager
        mMediaPlayer = MediaPlayer()

    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(mVolumeBroadReceiver)
    }
}