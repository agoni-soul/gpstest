package com.soul.volume

import android.content.Context
import android.content.IntentFilter
import android.media.AudioManager
import android.media.MediaPlayer
import android.media.MediaPlayer.OnBufferingUpdateListener
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.SeekBar
import android.widget.SeekBar.OnSeekBarChangeListener
import android.widget.Toast
import androidx.core.content.res.ResourcesCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.soul.base.BaseMvvmActivity
import com.soul.gpstest.R
import com.soul.gpstest.databinding.ActivityVolumeBinding
import com.soul.log.DOFLogUtil


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

            ivVolumePlay.isEnabled = false
            ivVolumePlay.setOnClickListener {
                Log.d(TAG, "isMediaPrepare = ${mViewModel?.getIsMediaPrepare()?.value}, isPlaying = ${mMediaPlayer.isPlaying}")
                if (mViewModel?.getIsMediaPrepare()?.value == true && !mMediaPlayer.isPlaying) {
                    mMediaPlayer.start()
                    Log.d(TAG, "setOnClickListener: duration = ${mMediaPlayer.duration}")
                    it.background = ResourcesCompat.getDrawable(resources, R.drawable.ic_play, null)
                } else if (mMediaPlayer.isPlaying) {
                    mMediaPlayer.pause()
                    it.background = ResourcesCompat.getDrawable(resources, R.drawable.ic_pause, null)
                } else {
                    it.background = ResourcesCompat.getDrawable(resources, R.drawable.ic_pause_unable, null)
                }
            }

            sbMusicProgress.setOnSeekBarChangeListener(object : OnSeekBarChangeListener {
                override fun onProgressChanged(
                    seekBar: SeekBar?,
                    progress: Int,
                    fromUser: Boolean
                ) {
                    if (fromUser) {
                        mMediaPlayer.seekTo(progress)
                        Log.d(TAG, "onProgressChanged: progress = $progress")
                    }
                }

                override fun onStartTrackingTouch(seekBar: SeekBar?) {
                }

                override fun onStopTrackingTouch(seekBar: SeekBar?) {
                }

            })
        }
    }

    override fun initData() {
        initMusic()
        mViewDataBinding?.sbMusicProgress?.max = mMediaPlayer.duration
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            mViewDataBinding?.sbMusicProgress?.min = 0
        }

        mViewModel?.apply {
            getIsMediaPrepare().observe(this@VolumeActivity) {
                Log.d(TAG, "observe: isMediaPrepare = $it, isPlaying = ${mMediaPlayer.isPlaying}")
                if (it) {
                    mViewDataBinding?.ivVolumePlay?.isEnabled = true
                }
            }
        }
    }

    private fun initMusic() {
        try {
            mAudioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager
            mMediaPlayer = MediaPlayer.create(this, R.raw.raw_music)
            mMediaPlayer.setOnPreparedListener {
                mViewModel?.getIsMediaPrepare()?.postValue(true)
            }
            mMediaPlayer.setOnCompletionListener {
                Log.d(TAG, "initMusic: duration = ${mMediaPlayer.duration}")
                Toast.makeText(mContext, "播放完成", Toast.LENGTH_SHORT).show()
                mViewDataBinding?.ivVolumePlay?.background = ResourcesCompat.getDrawable(resources, R.drawable.ic_pause, null)
            }
            mMediaPlayer.setOnBufferingUpdateListener(object : OnBufferingUpdateListener {
                override fun onBufferingUpdate(mp: MediaPlayer?, percent: Int) {
                    Log.d(TAG, "onBufferingUpdate: percent = $percent")
                }
            })
            mMediaPlayer.setOnErrorListener(object : MediaPlayer.OnErrorListener {
                override fun onError(mp: MediaPlayer?, what: Int, extra: Int): Boolean {
                    Log.e(TAG, "MediaPlayer onError")
                    mp?.release()
                    return true
                }
            })
            DOFLogUtil.d(TAG, "1")
//            mMediaPlayer.reset()
            DOFLogUtil.d(TAG, "2")
            mMediaPlayer.prepare()
            DOFLogUtil.d(TAG, "3")
        } catch (e: Exception) {
            e.printStackTrace()
            Log.e(TAG, "initMusic: exception = ${e.message}")
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(mVolumeBroadReceiver)
        mMediaPlayer.stop()
        mMediaPlayer.release()
    }
}