package com.soul.volume

import android.content.Context
import android.content.IntentFilter
import android.media.AudioAttributes
import android.media.AudioManager
import android.media.MediaPlayer
import android.media.MediaPlayer.OnBufferingUpdateListener
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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import java.util.*


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
    private val mTimerTask: TimerTask by lazy {
        object : TimerTask() {
            override fun run() {
                MainScope().launch(Dispatchers.Main) {
                    if (mViewModel?.getIsMediaPrepare()?.value == true && mMediaPlayer.isPlaying) {
                        mViewDataBinding?.tvLeavingTime?.text = calculateTime(mMediaPlayer.currentPosition.toLong())
                        mViewDataBinding?.sbMusicProgress?.progress = mMediaPlayer.currentPosition
                    }
                }
            }
        }
    }
    private val mTimer: Timer by lazy {
        Timer()
    }
    private val musicList = mutableListOf(
        "https://audio04.dmhmusic.com/71_53_T10052914726_128_4_1_0_sdk-cpm/cn/0209/M00/E0/68/ChR47F3qO9CAUEXuAEDUqVV8yoM106.mp3?xcode=f20e00a6d434b53fe095eedbde0a3bf617e5502",
        "http://www.eev3.com/plug/down.php?ac=music&id=mwckvdhdk&k=320kmp3",
        "http://www.eev3.com/plug/down.php?ac=music&id=vmhnccmk&k=320kmp3"
    )
    private var mPlayingMusicIndex = 0

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
                Log.d(
                    TAG,
                    "isMediaPrepare = ${mViewModel?.getIsMediaPrepare()?.value}, isPlaying = ${mMediaPlayer.isPlaying}"
                )
                if (mViewModel?.getIsMediaPrepare()?.value == true && !mMediaPlayer.isPlaying) {
                    mMediaPlayer.start()
                    startTimerTask()
                    Log.d(TAG, "setOnClickListener: duration = ${mMediaPlayer.duration}")
                    it.background = ResourcesCompat.getDrawable(resources, R.drawable.ic_play, null)
                } else if (mMediaPlayer.isPlaying) {
                    mMediaPlayer.pause()
                    stopTimerTask()
                    it.background =
                        ResourcesCompat.getDrawable(resources, R.drawable.ic_pause, null)
                } else {
                    stopTimerTask()
                    it.background =
                        ResourcesCompat.getDrawable(resources, R.drawable.ic_pause_unable, null)
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
                        mViewDataBinding?.tvLeavingTime?.text = calculateTime(progress.toLong())
                        Log.d(TAG, "onProgressChanged: progress = $progress")
                    }
                }

                override fun onStartTrackingTouch(seekBar: SeekBar?) {
                }

                override fun onStopTrackingTouch(seekBar: SeekBar?) {
                }

            })
            sbMusicProgress.isEnabled = false
        }
    }

    private fun startTimerTask() {
        mTimer.schedule(mTimerTask, 0, 1000)
    }

    private fun stopTimerTask() {
        mTimer.cancel()
    }

    override fun initData() {
        initMusic()

        mViewModel?.apply {
            getIsMediaPrepare().observe(this@VolumeActivity) {
                Log.d(TAG, "observe: isMediaPrepare = $it, isPlaying = ${mMediaPlayer.isPlaying}")
                if (it) {
                    mViewDataBinding?.apply {
                        ivVolumePlay.isEnabled = true
                        tvLeavingTime.text = calculateTime(0)
                        tvAmountTime.text = calculateTime(mMediaPlayer.duration.toLong())

                        sbMusicProgress.max = mMediaPlayer.duration
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                            sbMusicProgress.min = 0
                        }
                        sbMusicProgress.isEnabled = true
                    }
                }
            }
        }
    }

    private fun initMusic() {
        try {
            mAudioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager
            mMediaPlayer = MediaPlayer().apply {
                setAudioAttributes(
                    AudioAttributes.Builder()
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .setUsage(AudioAttributes.USAGE_ALARM)
                        .build()
                )
                setDataSource(musicList[mPlayingMusicIndex])
            }
            mMediaPlayer.setOnPreparedListener {
                mViewModel?.getIsMediaPrepare()?.postValue(true)
            }
            mMediaPlayer.setOnCompletionListener {
                Toast.makeText(mContext, "播放完成", Toast.LENGTH_SHORT).show()
                mViewDataBinding?.ivVolumePlay?.background =
                    ResourcesCompat.getDrawable(resources, R.drawable.ic_pause, null)
                stopTimerTask()
                mViewDataBinding?.tvLeavingTime?.text = calculateTime(mMediaPlayer.duration.toLong())
            }
            mMediaPlayer.setOnBufferingUpdateListener(object : OnBufferingUpdateListener {
                override fun onBufferingUpdate(mp: MediaPlayer?, percent: Int) {
                    Log.d(TAG, "onBufferingUpdate: percent = $percent")
                }
            })
            mMediaPlayer.isLooping = true
            mMediaPlayer.setOnErrorListener(object : MediaPlayer.OnErrorListener {
                override fun onError(mp: MediaPlayer?, what: Int, extra: Int): Boolean {
                    Log.e(TAG, "MediaPlayer onError")
                    mp?.release()
                    stopTimerTask()
                    return true
                }
            })
            mMediaPlayer.prepareAsync()
        } catch (e: Exception) {
            e.printStackTrace()
            Log.e(TAG, "initMusic: exception = ${e.message}")
        }
    }

    private fun calculateTime(time: Long): String {
        val secondTime = (time / 1000) % 60
        val minuteTime = (time / 1000 / 60) % 60
        val hourTime = time / 1000 / 60 / 60
        val stringFormat = "%02d"
        return if (hourTime == 0L) {
            "${stringFormat.format(minuteTime)}:${stringFormat.format(secondTime)}"
        } else {
            "${stringFormat.format(hourTime)}:" +
                    "${stringFormat.format(minuteTime)}:" +
                    stringFormat.format(secondTime)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(mVolumeBroadReceiver)
        mMediaPlayer.release()
        stopTimerTask()
    }
}