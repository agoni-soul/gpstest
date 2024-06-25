package com.soul.volume

import android.app.Activity
import android.media.AudioFormat
import android.media.AudioManager
import android.media.AudioTrack
import android.util.Log
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.io.InputStream


/**
 *     author : yangzy33
 *     time   : 2024-06-25
 *     desc   :
 *     version: 1.0
 */
class PlayThread() : Thread() {
    private val TAG: String = this.javaClass.simpleName.toString()

    private val mSampleRateInHz = 16000

    // 单声道
    private val mChannelConfig = AudioFormat.CHANNEL_OUT_MONO

    // 双声道（立体声）
//    private val mChannelConfig = AudioFormat.CHANNEL_OUT_STEREO
    private var mActivity: Activity? = null
    private var mAudioTrack: AudioTrack? = null
    private var data: ByteArray? = null
    private var mFileName: String? = null

    constructor(activity: Activity?, fileName: String?) : this() {
        mActivity = activity
        mFileName = fileName

        val bufferSize = AudioTrack.getMinBufferSize(
            mSampleRateInHz,
            mChannelConfig,
            AudioFormat.ENCODING_PCM_16BIT
        )
        mAudioTrack = AudioTrack(
            AudioManager.STREAM_MUSIC,
            mSampleRateInHz,
            mChannelConfig,
            AudioFormat.ENCODING_PCM_16BIT,
            bufferSize,
            AudioTrack.MODE_STREAM
        )
    }

    override fun run() {
        super.run()
        try {
            mAudioTrack?.play()
            val byteArrayOutputStream = ByteArrayOutputStream()

            val inputStream: InputStream? = mFileName?.let {
                mActivity?.resources?.assets?.open(it)
            }
            val buffer: ByteArray = byteArrayOf()
            var playIndex = 0
            var isLoaded = false
            while (mAudioTrack != null && AudioTrack.PLAYSTATE_STOPPED != mAudioTrack?.playState) {
                val len = inputStream?.read(buffer) ?: -1
                if (-1 != len) {
                    byteArrayOutputStream.write(buffer, 0, len)
                    data = byteArrayOutputStream.toByteArray()
                    Log.i(TAG, "run: 已缓冲 = ${data?.size}")
                } else {
                    isLoaded = true
                }
                if (mAudioTrack?.playState == AudioTrack.PLAYSTATE_PAUSED) {
                    // TODO 已经暂停
                } else if (data != null && mAudioTrack?.playState == AudioTrack.PLAYSTATE_PLAYING) {
                    Log.i(TAG, "run: 开始从 $playIndex 播放")
                    playIndex += mAudioTrack!!.write(data!!, playIndex, data!!.size - playIndex)
                    Log.i(TAG, "run: 播放到了 playIndex = $playIndex")
                    if (isLoaded && playIndex == data!!.size) {
                        Log.i(TAG, "run: 播放完了")
                        mAudioTrack!!.stop()
                    }
                    if (playIndex < 0) {
                        Log.i(TAG, "run: 播放出错")
                        mAudioTrack!!.stop()
                        break
                    }
                }
            }
            Log.i(TAG, "run: play end")
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    /**
     * 设置左右声道
     *
     * @param max 最大值
     * @param balance 当前值
     */
    fun setBalance(max: Int, balance: Int) {
        val b = balance.toFloat() / max.toFloat()
        Log.i(TAG, "setBalance: b = $b")
        mAudioTrack?.setStereoVolume(1 - b, b)
    }

    fun setChannel(left: Boolean, right: Boolean) {
        mAudioTrack?.setStereoVolume(if (left) 1F else 0F, if (right) 1F else 0F)
        mAudioTrack?.play()
    }

    fun pause() {
        mAudioTrack?.pause()
    }

    fun play() {
        mAudioTrack?.play()
    }

    fun stopp() {
        mAudioTrack?.stop()
    }

    private fun releaseAudioTrack() {
        if (mAudioTrack != null) {
            mAudioTrack!!.stop()
            mAudioTrack!!.release()
            mAudioTrack = null
        }
    }
}