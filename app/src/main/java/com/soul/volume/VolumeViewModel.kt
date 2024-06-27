package com.soul.volume

import android.app.Application
import android.content.Context
import android.media.AudioManager
import android.media.MediaPlayer
import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.soul.base.BaseViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import java.io.BufferedReader
import java.io.InputStreamReader
import java.util.*
import java.lang.*


/**
 *     author : yangzy33
 *     time   : 2024-05-14
 *     desc   :
 *     version: 1.0
 */
class VolumeViewModel(application: Application) : BaseViewModel(application) {
    companion object {
        private const val PLAY_MODE_SEQUENTIAL = 0
        private const val PLAY_MODE_LOOP = 1
        private const val PLAY_MODE_SHUFFLE = 2

        const val MEDIA_PLAYER_STATUS_INIT = 100
        const val MEDIA_PLAYER_STATUS_PREPARING = 101
        const val MEDIA_PLAYER_STATUS_PREPARED = 102
        const val MEDIA_PLAYER_STATUS_START = 103
        const val MEDIA_PLAYER_STATUS_PAUSE = 104
        const val MEDIA_PLAYER_STATUS_STOP = 105
        const val MEDIA_PLAYER_STATUS_COMPLETE = 106
        const val MEDIA_PLAYER_STATUS_ERROR = 107
        const val MEDIA_PLAYER_STATUS_END = 108

        private const val UPDATE_LRC_INTERNAL = 1000L
    }

    private var mAudioManager: AudioManager? = null
    // 媒体播放器
    private var mMediaPlayer: MediaPlayer? = null
    private var mTimerTask: TimerTask? = null
    private var mTimer: Timer? = null
    private val mMusicList = mutableListOf<SongInfo>()
    private val mRandomIndexList = mutableListOf<Int>()
    var mPlayingMusicIndex = 0
        private set

    var mPlayMode = PLAY_MODE_SEQUENTIAL
        private set

    var mLrcRows: MutableList<LrcRow>? = null
        private set

    var mCurrentLrcIndex: Int = 0
        private set

    private val mMediaPlayerStatusLiveData: MutableLiveData<Int> by lazy {
        MutableLiveData(MEDIA_PLAYER_STATUS_INIT)
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

    init {
        mAudioManager = mApplication.getSystemService(Context.AUDIO_SERVICE) as AudioManager
    }

    fun getMusicDuration(): MutableLiveData<Int> = mMusicDurationLiveData

    fun getMusicProgress(): MutableLiveData<Int> = mMusicProgressLiveData

    fun getMusicCacheProgress(): MutableLiveData<Int> = mMusicCacheProgressLiveData

    fun getMediaPlayerStatus(): MutableLiveData<Int> = mMediaPlayerStatusLiveData

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
                    Log.d(TAG, "setOnPreparedListener: prepared")
                    mMediaPlayerStatusLiveData.postValue(MEDIA_PLAYER_STATUS_PREPARED)
                    mMusicDurationLiveData.postValue(mMediaPlayer?.duration ?: 0)
                }
                setOnCompletionListener {
                    Log.d(TAG, "setOnCompletionListener: onCompletion")
                    mMusicProgressLiveData.postValue(duration)
                    mMediaPlayerStatusLiveData.postValue(MEDIA_PLAYER_STATUS_COMPLETE)
                    stopTimerTask()
                }
                // 网络链接歌曲，缓存进度百分比
                setOnBufferingUpdateListener { mp, percent ->
                    mMusicCacheProgressLiveData.postValue((mp.duration * 1L * percent / 100).toInt())
                }
                setOnErrorListener { mp, what, extra ->
                    Log.e(
                        TAG,
                        "MediaPlayer onError: what = ${mediaErrorWhatToStr(what)}, extra = ${
                            mediaErrorExtraToStr(extra)
                        }"
                    )
                    mp?.stop()
                    Log.d(TAG, "setOnErrorListener: onError")
                    mMediaPlayerStatusLiveData.postValue(MEDIA_PLAYER_STATUS_ERROR)
                    stopTimerTask()
                    mMusicProgressLiveData.postValue(0)
                    mMusicCacheProgressLiveData.postValue(0)
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
        mMusicList.add(
            SongInfo(
                "以后别做朋友",
                "周兴哲",
                "http://www.eev3.com/plug/down.php?ac=music&id=vnxcdmd&k=320kmp3",
                "http://www.eev3.com/plug/down.php?ac=music&lk=lrc&id=vnxcdmd"
            )
        )
        mMusicList.add(
            SongInfo(
                "骗子",
                "文夫",
                "http://www.eev3.com/plug/down.php?ac=music&id=mwckvdhdk&k=320kmp3",
                "http://www.eev3.com/plug/down.php?ac=music&lk=lrc&id=mwckvdhdk"
            )
        )
        mMusicList.add(
            SongInfo(
                "请先说你好",
                "贺一航",
                "http://www.eev3.com/plug/down.php?ac=music&id=vmhnccmk&k=320kmp3",
                "http://www.eev3.com/plug/down.php?ac=music&lk=lrc&id=vmhnccmk"
            )
        )
        mMusicList.add(
            SongInfo(
                "嘉宾",
                "张远",
                "http://www.eev3.com/plug/down.php?ac=music&id=wvxkdxvxm&k=320kmp3",
                "http://www.eev3.com/plug/down.php?ac=music&lk=lrc&id=wvxkdxvxm"
            )
        )
        mMusicList.add(
            SongInfo(
                "年少有为",
                "李荣浩",
                "http://www.eev3.com/plug/down.php?ac=music&id=vvnmxvxx&k=320kmp3",
                "http://www.eev3.com/plug/down.php?ac=music&lk=lrc&id=vvnmxvxx"
            )
        )
        mMusicList.add(
            SongInfo(
                "秘密海域",
                "深海鱼子酱",
                "http://www.eev3.com/plug/down.php?ac=music&id=whnmxhsww&k=320kmp3",
                "http://www.eev3.com/plug/down.php?ac=music&lk=lrc&id=whnmxhsww"
            )
        )
        mMusicList.add(
            SongInfo(
                "再见，再也不见",
                "欢子",
                "http://www.eev3.com/plug/down.php?ac=music&id=mmhwvxsn&k=320kmp3",
                "http://www.eev3.com/plug/down.php?ac=music&lk=lrc&id=mmhwvxsn"
            )
        )
        mMusicList.add(
            SongInfo(
                "可不可以",
                "张紫豪",
                "http://www.eev3.com/plug/down.php?ac=music&id=vmhnccmc&k=320kmp3",
                "http://www.eev3.com/plug/down.php?ac=music&lk=lrc&id=vmhnccmc"
            )
        )
        for (i in 0 until mMusicList.size) {
            mRandomIndexList.add(i)
        }
        playModeSequential()
    }

    fun getSongInfo(): SongInfo {
        return mMusicList[mRandomIndexList[mPlayingMusicIndex]]
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
                    if (getMediaPlayerStatus().value == MEDIA_PLAYER_STATUS_START &&
                        mMediaPlayer?.isPlaying == true) {
                        val currentPosition = mMediaPlayer?.currentPosition ?: 0
                        mMusicProgressLiveData.postValue(currentPosition)
                        updateLrcLinePosition(currentPosition)
                    }
                }
            }
        }
        mTimer!!.schedule(mTimerTask, 0, UPDATE_LRC_INTERNAL)
    }

    fun stopTimerTask() {
        mTimer?.cancel()
    }

    fun isMusicPlaying(): Boolean {
        return (mMediaPlayerStatusLiveData.value == MEDIA_PLAYER_STATUS_START ||
                mMediaPlayerStatusLiveData.value == MEDIA_PLAYER_STATUS_PAUSE) &&
                mMediaPlayer?.isPlaying ?: false
    }

    fun musicPause() {
        mMediaPlayer?.pause()
        mMediaPlayerStatusLiveData.postValue(MEDIA_PLAYER_STATUS_PAUSE)
    }

    fun musicStart() {
        mMediaPlayer?.start()
        mMediaPlayerStatusLiveData.postValue(MEDIA_PLAYER_STATUS_START)
    }

    fun musicSeekTo(progress: Int) {
        mMediaPlayer?.seekTo(progress)
        mMediaPlayer?.currentPosition?.let {
            updateLrcLinePosition(it)
        }
    }

    fun playPreviousMusic() {
        mPlayingMusicIndex--
        playOtherMusic(mPlayingMusicIndex)
    }

    fun playCurrentMusic() {
        playOtherMusic(mPlayingMusicIndex)
    }

    fun playNextMusic() {
        mPlayingMusicIndex++
        playOtherMusic(mPlayingMusicIndex)
    }

    fun playModeShuffle() {
        mRandomIndexList.shuffle()
        mMediaPlayer?.isLooping = false
        mPlayMode = PLAY_MODE_SHUFFLE
    }

    fun isShufflePlayMode(): Boolean = mPlayMode == PLAY_MODE_SHUFFLE

    fun playModeLoop() {
        playModeSequential()
        mMediaPlayer?.isLooping = true
        mPlayMode = PLAY_MODE_LOOP
    }

    fun isLoopPlayMode(): Boolean = mPlayMode == PLAY_MODE_LOOP

    fun playModeSequential() {
        for (i in 0 until mMusicList.size) {
            mRandomIndexList[i] = i
        }
        mMediaPlayer?.isLooping = false
        mPlayMode = PLAY_MODE_SEQUENTIAL
    }

    fun isSequentialPlayMode(): Boolean = mPlayMode == PLAY_MODE_SEQUENTIAL

    private fun playOtherMusic(index: Int) {
        var indexTemp = index % mMusicList.size
        if (indexTemp < 0) {
            indexTemp += mMusicList.size
        }
        mPlayingMusicIndex = indexTemp
        playNewMusic(mRandomIndexList[indexTemp])
    }

    private fun playNewMusic(index: Int) {
        stopTimerTask()
        mMediaPlayer?.reset()
        Log.d(TAG, "playNewMusic: reset")
        mMediaPlayerStatusLiveData.postValue(MEDIA_PLAYER_STATUS_INIT)
        playMusic(index)
    }

    private fun playMusic(index: Int) {
        mMediaPlayer?.setDataSource(mMusicList[index].songUrl)
        mMediaPlayer?.prepareAsync()
        Log.d(TAG, "playMusic: prepareAsync")
        mMediaPlayerStatusLiveData.postValue(MEDIA_PLAYER_STATUS_PREPARING)

        mMusicProgressLiveData.postValue(0)
        mMusicCacheProgressLiveData.postValue(0)
        obtainSongLrc()
    }

    fun calculateTime(time: Long): String {
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

    private fun updateLrcLinePosition(currentPosition: Int) {
        mLrcRows?.let {
            while (mCurrentLrcIndex < it.size - 1) {
                val lrcRow = it[mCurrentLrcIndex + 1]
//                Log.d(TAG, "lrcRowTime = ${calculateTime(lrcRow.time)}, songTime = ${calculateTime(currentPosition.toLong())}")
                if (lrcRow.time > currentPosition) {
                    return@let
                } else {
                    mCurrentLrcIndex++
                }
            }
        }
//        Log.d(TAG, "mCurrentLrcIndex = ${mCurrentLrcIndex + 1}")
    }

    private fun obtainSongLrc() {
        val currentSongInfo = getSongInfo()
        mCurrentLrcIndex = 0
        if (currentSongInfo.songName != "以后别做朋友") {
            mLrcRows = null
            return
        }
        // TODO 后续优化歌曲歌词相关逻辑
        val songLrc = getFromAssets("周兴哲-以后别做朋友.lrc")
        val builder = DefaultLrcBuilder()
        mLrcRows = builder.getLrcRows(songLrc)
    }

    private fun getFromAssets(fileName: String): String {
        try {
            val inputReader = InputStreamReader(mApplication.resources.assets.open(fileName))
            val bufReader = BufferedReader(inputReader)
            val result = StringBuilder()
            do {
                val line: String? = bufReader.readLine()
                if (!line?.trim().isNullOrEmpty()) {
                    result.append(line + "\r\n")
                }
            } while (line != null)
            return result.toString()
        } catch (e: Exception) {
            Log.e(TAG, "${e.message}")
        }
        return ""
    }

    override fun onDestroy() {
        super.onDestroy()
        mMediaPlayer?.release()
        mMediaPlayerStatusLiveData.postValue(MEDIA_PLAYER_STATUS_END)
        mMediaPlayer = null
        stopTimerTask()
    }

    fun setLeftChannel() {
        val volume = mAudioManager?.getStreamVolume(AudioManager.STREAM_MUSIC)?.toFloat() ?: 0F
        mMediaPlayer?.setVolume(volume, 0F)
    }

    fun setRightChannel() {
        val volume = mAudioManager?.getStreamVolume(AudioManager.STREAM_MUSIC)?.toFloat() ?: 0F
        mMediaPlayer?.setVolume(0f, volume)
    }

    private fun setStereoChannel() {
        val volume = mAudioManager?.getStreamVolume(AudioManager.STREAM_MUSIC)?.toFloat() ?: 0F
        mMediaPlayer?.setVolume(volume, volume)
    }
}