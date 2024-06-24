package com.soul.volume

import android.content.Context
import android.content.IntentFilter
import android.media.AudioManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.ViewTreeObserver
import android.widget.SeekBar
import android.widget.SeekBar.OnSeekBarChangeListener
import android.widget.Toast
import androidx.core.content.res.ResourcesCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.soul.base.BaseMvvmActivity
import com.soul.gpstest.R
import com.soul.gpstest.databinding.ActivityVolumeBinding
import com.soul.util.DpToPxTransfer
import java.io.BufferedReader
import java.io.InputStreamReader
import java.lang.*


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

            ivSongPrevious.setOnClickListener {
                playPreviousMusic()
            }
            ivSongPlay.setOnClickListener {
                Log.d(
                    TAG,
                    "ivSongPlay: isMediaPrepare = ${mViewModel?.isMediaPrepare()?.value}"
                )
                if (mViewModel?.isMediaPrepare()?.value == false) {
                    playMusic()
                } else if (mViewModel?.isMusicPlaying() == true) {
                    mViewModel?.musicPause()
                    mViewModel?.stopTimerTask()
                    it.background =
                        ResourcesCompat.getDrawable(resources, R.drawable.ic_pause, null)
                }
            }
            ivSongNext.setOnClickListener {
                playNextMusic()
            }
            setPlayMode()

            sbMusicProgress.setOnSeekBarChangeListener(object : OnSeekBarChangeListener {
                override fun onProgressChanged(
                    seekBar: SeekBar?,
                    progress: Int,
                    fromUser: Boolean
                ) {
                }

                override fun onStartTrackingTouch(seekBar: SeekBar?) {
                }

                override fun onStopTrackingTouch(seekBar: SeekBar?) {
                    val progress = seekBar?.progress ?: return
                    mViewModel?.apply {
                        if (isMediaPrepare().value == true) {
                            musicSeekTo(progress)
                            mViewDataBinding?.tvLeavingTime?.text =
                                calculateTime(progress.toLong())
                            Log.d(TAG, "onProgressChanged: progress = $progress")
                        } else {
                            Toast.makeText(mContext, "音频没有准备好, 暂不可调节", Toast.LENGTH_SHORT)
                                .show()
                        }
                    }
                }

            })
        }
    }

    private fun setPlayMode() {
        mViewModel?.apply {
            if (isSequentialPlayMode()) {
                mViewDataBinding?.ivPlayMode?.setImageResource(R.drawable.ic_play_mode_sequential)
            } else if (isShufflePlayMode()) {
                mViewDataBinding?.ivPlayMode?.setImageResource(R.drawable.ic_play_mode_shuffle)
            } else if (isLoopPlayMode()) {
                mViewDataBinding?.ivPlayMode?.setImageResource(R.drawable.ic_play_mode_loop)
            } else {
                playModeSequential()
                mViewDataBinding?.ivPlayMode?.setImageResource(R.drawable.ic_play_mode_sequential)
            }
        }
        mViewDataBinding?.ivPlayMode?.setOnClickListener {
            mViewModel?.apply {
                if (isSequentialPlayMode()) {
                    playModeShuffle()
                    mViewDataBinding?.ivPlayMode?.setImageResource(R.drawable.ic_play_mode_shuffle)
                } else if (isShufflePlayMode()) {
                    playModeLoop()
                    mViewDataBinding?.ivPlayMode?.setImageResource(R.drawable.ic_play_mode_loop)
                } else if (isLoopPlayMode()) {
                    playModeSequential()
                    mViewDataBinding?.ivPlayMode?.setImageResource(R.drawable.ic_play_mode_sequential)
                } else {
                    playModeSequential()
                    mViewDataBinding?.ivPlayMode?.setImageResource(R.drawable.ic_play_mode_sequential)
                }
            }
        }
    }

    override fun initData() {
        mAudioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager

        mViewModel?.apply {
            initMusic()
            initMusicList()
            isMediaPrepare().observe(this@VolumeActivity) {
                Log.d(TAG, "observe: isMediaPrepare = $it")
                if (it) {
                    mViewDataBinding?.apply {
                        ivSongPlay.background =
                            ResourcesCompat.getDrawable(resources, R.drawable.ic_play, null)
                        musicStart()
                        startTimerTask()
                    }
                }
            }

            getMusicDuration().observe(this@VolumeActivity) {
                mViewDataBinding?.apply {
                    Log.d(TAG, "music duration = $it")
                    tvLeavingTime.text = calculateTime(0)
                    tvAmountTime.text = calculateTime(it.toLong())
                    sbMusicProgress.max = it
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        sbMusicProgress.min = 0
                    }
                }
            }

            getMusicProgress().observe(this@VolumeActivity) {
                mViewDataBinding?.apply {
                    tvLeavingTime.text = calculateTime(it.toLong())
                    sbMusicProgress.progress = it
                    if (it > 0 && it == getMusicDuration().value) {
                        Toast.makeText(mContext, "播放完成", Toast.LENGTH_SHORT).show()
                        Log.d(TAG, "playMode = ${mViewModel?.mPlayMode}")
                        if (!isLoopPlayMode()) {
                            playNextMusic()
                        }
                    }
                }
            }

            getMusicCacheProgress().observe(this@VolumeActivity) {
                mViewDataBinding?.apply {
                    sbMusicProgress.secondaryProgress = it
                }
            }

            isMediaPlayerError().observe(this@VolumeActivity) {
                mViewDataBinding?.apply {
                    ivSongPlay.background =
                        ResourcesCompat.getDrawable(resources, R.drawable.ic_pause, null)
                }
            }
        }
    }

    private fun playMusic() {
        mViewModel?.playCurrentMusic()
        resetObtainSongInfo()
    }

    private fun playNextMusic() {
        mViewModel?.playNextMusic()
        resetObtainSongInfo()
    }

    private fun playPreviousMusic() {
        mViewModel?.playPreviousMusic()
        resetObtainSongInfo()
    }

    private fun resetObtainSongInfo() {
        mViewDataBinding?.apply {
            val songName = mViewModel?.getSongInfo()?.let {
                "《${it.songName}》-${it.singer}"
            } ?: "播放音乐"
            tvSongName.text = songName
            ivSongPlay.background =
                ResourcesCompat.getDrawable(resources, R.drawable.ic_pause, null)

            val rows = mViewModel?.rows
            val sb = StringBuilder()
            if (!rows.isNullOrEmpty()) {
                rows.forEach {row ->
                    row.content?.let {
                        sb.append("$it\n")
                    }
                }
            }
            if (sb.isEmpty()) {
                nsvSongLrc.visibility = View.GONE
            } else {
                nsvSongLrc.visibility = View.VISIBLE
                tvSongLrc.text = sb.toString().trim()
                val vto = tvSongLrc.viewTreeObserver
                vto.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
                    override fun onGlobalLayout()  {
                        val lineCount = tvSongLrc.lineCount
                        val lineHeight = tvSongLrc.lineHeight
                        val layoutParams = nsvSongLrc.layoutParams
                        if (lineCount * lineHeight > layoutParams.height) {
                            layoutParams.height = DpToPxTransfer.dp2px(mContext, 100)
                        }
                        nsvSongLrc.layoutParams = layoutParams
                        nsvSongLrc.requestLayout()
                        tvSongLrc.viewTreeObserver.removeOnGlobalLayoutListener(this)
                    }
                })
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(mVolumeBroadReceiver)
        mViewModel?.onDestroy()
    }
}