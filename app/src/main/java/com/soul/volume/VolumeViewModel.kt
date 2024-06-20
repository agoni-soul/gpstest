package com.soul.volume

import android.app.Application
import android.media.MediaPlayer
import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.soul.base.BaseViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import java.util.*


/**
 *     author : yangzy33
 *     time   : 2024-05-14
 *     desc   :
 *     version: 1.0
 */
class VolumeViewModel(application: Application): BaseViewModel(application) {
    // 媒体播放器
    private var mMediaPlayer: MediaPlayer? = null
    private var mTimerTask: TimerTask? = null
    private var mTimer: Timer? = null
    private val mMusicList = mutableListOf<String>()
    private val mRandomIndexList = mutableListOf<Int>()
    var mPlayingMusicIndex = 0
        private set

    private val mIsMediaPrepareLiveData: MutableLiveData<Boolean> by lazy {
        MutableLiveData(false)
    }

    private val mMusicDurationLiveData: MutableLiveData<Int> by lazy {
        MutableLiveData(0)
    }

    private val mMusicProgressLiveData: MutableLiveData<Int> by lazy {
        MutableLiveData(0)
    }

    private val mMusicCacheProgressLiveData: MutableLiveData<Int> by lazy {
        MutableLiveData(0)
    }

    fun getIsMediaPrepare(): MutableLiveData<Boolean> = mIsMediaPrepareLiveData

    fun getMusicDuration(): MutableLiveData<Int> = mMusicDurationLiveData

    fun getMusicProgress(): MutableLiveData<Int> = mMusicProgressLiveData

    fun getMusicCacheProgress(): MutableLiveData<Int> = mMusicCacheProgressLiveData

    fun initMusic() {
        try {
            mMediaPlayer = MediaPlayer().apply {
//                setAudioAttributes(
//                    AudioAttributes.Builder()
//                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
//                        .setUsage(AudioAttributes.USAGE_ALARM)
//                        .build()
//                )
            }
            mMediaPlayer!!.apply {
                setOnPreparedListener {
                    mIsMediaPrepareLiveData.postValue(true)
                    mMusicDurationLiveData.postValue(mMediaPlayer?.duration ?: 0)
                }
                setOnCompletionListener {
                    mMusicProgressLiveData.postValue(duration)
                    stopTimerTask()
                }
                // 网络链接歌曲，缓存进度百分比
                setOnBufferingUpdateListener { mp, percent ->
                    Log.d(
                        TAG,
                        "onBufferingUpdate: percent = $percent"
                    )
                    mMusicCacheProgressLiveData.postValue(((mMediaPlayer?.duration ?: 0) * 1L * percent / 100).toInt())
                }
                setOnErrorListener { mp, what, extra ->
                    Log.e(
                        TAG,
                        "MediaPlayer onError: what = ${mediaErrorWhatToStr(what)}, extra = ${
                            mediaErrorExtraToStr(extra)
                        }"
                    )
                    mp?.release()
                    stopTimerTask()
                    true
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Log.e(TAG, "initMusic: exception = ${e.message}")
        }
    }

    fun initMusicList() {
        mMusicList.clear()
        mMusicList.add("http://www.eev3.com/plug/down.php?ac=music&id=mwckvdhdk&k=320kmp3")
        mMusicList.add("http://www.eev3.com/plug/down.php?ac=music&id=vmhnccmk&k=320kmp3")
        playSequentialMode()
    }

    private fun mediaErrorWhatToStr(what: Int): String {
        return when (what) {
            MediaPlayer.MEDIA_ERROR_UNKNOWN -> "MEDIA_ERROR_UNKNOWN"
            MediaPlayer.MEDIA_ERROR_SERVER_DIED -> "MEDIA_ERROR_SERVER_DIED"
            else -> "MEDIA_ERROR_UNKNOWN"
        }
    }

    private fun mediaErrorExtraToStr(extra: Int): String {
        return when (extra.toLong()) {
            MediaPlayer.MEDIA_ERROR_IO.toLong() -> "MEDIA_ERROR_IO"
            MediaPlayer.MEDIA_ERROR_MALFORMED.toLong() -> "MEDIA_ERROR_MALFORMED"
            MediaPlayer.MEDIA_ERROR_UNSUPPORTED.toLong() -> "MEDIA_ERROR_UNSUPPORTED"
            MediaPlayer.MEDIA_ERROR_TIMED_OUT.toLong() -> "MEDIA_ERROR_TIMED_OUT"
            -2147483648L -> "MEDIA_ERROR_SYSTEM"
            else -> "NOT_IN_MEDIA_ERROR_UNKNOWN"
        }
    }

    fun startTimerTask() {
        mTimer = Timer()
        mTimerTask = object : TimerTask() {
                override fun run() {
                    MainScope().launch(Dispatchers.Main) {
                        if (getIsMediaPrepare().value == true && mMediaPlayer?.isPlaying == true) {
                            mMusicProgressLiveData.postValue(mMediaPlayer?.currentPosition ?: 0)
                        }
                    }
                }
            }
        mTimer!!.schedule(mTimerTask, 0, 1000)
    }

    fun stopTimerTask() {
        mTimer?.cancel()
    }

    fun isMusicPlaying(): Boolean {
        return mMediaPlayer?.isPlaying ?: false
    }

    fun musicPause() {
        mMediaPlayer?.pause()
    }

    fun musicStart() {
        mMediaPlayer?.start()
    }

    fun musicSeekTo(progress: Int) {
        mMediaPlayer?.seekTo(progress)
    }

    fun playPreviousMusic() {
        Log.d(
            TAG,
            "playPreviousMusic: isMediaPrepare = ${mIsMediaPrepareLiveData.value}"
        )
        mPlayingMusicIndex--
        playOtherMusic(mPlayingMusicIndex)
    }

    fun playNextMusic() {
        Log.d(
            TAG,
            "playNextMusic: isMediaPrepare = ${mIsMediaPrepareLiveData.value}"
        )
        mPlayingMusicIndex++
        playOtherMusic(mPlayingMusicIndex)
    }

    fun playRandomMode() {
        val random = Random()
        mRandomIndexList.shuffle()
    }

    fun playSequentialMode() {
        mRandomIndexList.clear()
        var i = 0
        mMusicList.forEach { _ ->
            mRandomIndexList.add(i ++)
        }
    }

    private fun playOtherMusic(index: Int) {
        var indexTemp = index % mMusicList.size
        if (indexTemp < 0) {
            indexTemp += mMusicList.size
        }
        playNewMusic(mRandomIndexList[indexTemp])
    }

    private fun playNewMusic(index: Int) {
        stopTimerTask()
        mIsMediaPrepareLiveData.postValue(false)
        mMediaPlayer?.reset()
        playMusic(index)
    }

    fun playMusic(index: Int) {
        mMediaPlayer?.setDataSource(mMusicList[index])
        mMediaPlayer?.prepareAsync()
    }

    override fun onDestroy() {
        super.onDestroy()
        mMediaPlayer?.release()
        mMediaPlayer = null
        stopTimerTask()
    }
}