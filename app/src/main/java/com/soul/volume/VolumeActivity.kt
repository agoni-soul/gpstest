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
                if (mViewModel?.getIsMediaPrepare()?.value == true && !mMediaPlayer.isPlaying) {
                    mMediaPlayer.start()
                }
            }
        }
    }

    override fun initData() {
        mAudioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager
        mMediaPlayer = MediaPlayer.create(this, R.raw.raw_music)
        mMediaPlayer.setOnPreparedListener {
            mViewModel?.getIsMediaPrepare()?.postValue(true)
        }
        mMediaPlayer.setOnErrorListener(object : MediaPlayer.OnErrorListener {
            override fun onError(mp: MediaPlayer?, what: Int, extra: Int): Boolean {
                Log.e(TAG, "MediaPlayer onError")
                return true
            }
        })
        initMusic()

        mViewModel?.apply {
            getIsMediaPrepare().postValue(false)
            getIsMediaPrepare().observe(this@VolumeActivity) {
                Log.d(TAG, "observe: isMediaPrepare = $it, isPlaying = ${mMediaPlayer.isPlaying}")
                if (!it) {
                    mMediaPlayer.start()
                }
            }
        }
    }

    private fun initMusic() {
        try {
            mMediaPlayer.reset()
            mMediaPlayer = MediaPlayer.create(this, R.raw.raw_music)
            mMediaPlayer.prepareAsync()
        } catch (e: Exception) {
            e.printStackTrace()
            Log.d(TAG, "initMusic: exception = ${e.message}")
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(mVolumeBroadReceiver)
        mMediaPlayer.stop()
        mMediaPlayer.release()
    }
}