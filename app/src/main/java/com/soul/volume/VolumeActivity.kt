package com.soul.volume

import android.content.Context
import android.content.IntentFilter
import android.media.AudioManager
import android.media.MediaPlayer
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.recyclerview.widget.LinearLayoutManager
import com.soul.base.BaseMvvmActivity
import com.soul.gpstest.R
import com.soul.gpstest.databinding.ActivityVolumeBinding
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch


/**
 *     author : yangzy33
 *     time   : 2024-05-09
 *     desc   :
 *     version: 1.0
 */
class VolumeActivity : BaseMvvmActivity<ActivityVolumeBinding, VolumeViewModel>() {

    // 调节音量
    private lateinit var mAudioManager: AudioManager
    private lateinit var mVolumeBroadReceiver: VolumeBroadReceiver
    // 媒体播放器
    private lateinit var mMediaPlayer: MediaPlayer
    private val mOnPreparedListener: MediaPlayer.OnPreparedListener by lazy {
        object : MediaPlayer.OnPreparedListener {
            override fun onPrepared(mp: MediaPlayer?) {
            }
        }
    }

    private lateinit var mVolumeAdjustAdapter: VolumeAdjustAdapter

    override fun getViewModelClass(): Class<VolumeViewModel> = VolumeViewModel::class.java

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
            rvVolume.adapter = mVolumeAdjustAdapter

            tvPlayer.setOnClickListener {
                Log.d(TAG, "isPlaying = ${mMediaPlayer.isPlaying}")
                if (!mMediaPlayer.isPlaying) {
                    mMediaPlayer.start()
                }
            }
        }
    }

    override fun initData() {
        mAudioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager
        mMediaPlayer = MediaPlayer()
        mMediaPlayer.setOnPreparedListener(mOnPreparedListener)
        initMusic()
    }

    private fun initMusic() {
        MainScope().launch {
            try {
                val uri = "http://isure6.stream.qqmusic.qq.com/C200001KBxQw0PWq7Y.m4a?guid=2000000194&vkey=8F7A8529D0AA51DE9742C450949E9528AD5B7D98F851657AA06189E992067E2A28F51C0CC6B42C577C61264804DA728EC4DE743DFD583E9D&uin=0&fromtag=20194&trace=5cdf245b6863abb0"
                mMediaPlayer.setDataSource(mContext, Uri.parse(uri))
                mMediaPlayer.prepare()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(mVolumeBroadReceiver)
        mMediaPlayer.stop()
        mMediaPlayer.release()
    }
}