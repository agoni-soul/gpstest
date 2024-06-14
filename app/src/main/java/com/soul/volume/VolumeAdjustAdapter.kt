package com.soul.volume

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.media.AudioManager
import android.os.Build
import android.provider.Settings
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.SeekBar
import android.widget.SeekBar.OnSeekBarChangeListener
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationManagerCompat
import androidx.recyclerview.widget.RecyclerView
import com.soul.gpstest.R


/**
 *     author : yangzy33
 *     time   : 2024-05-10
 *     desc   :
 *     version: 1.0
 */
class VolumeAdjustAdapter(private val mContext: Context, volumeList: MutableList<Int>? = null) :
    RecyclerView.Adapter<VolumeAdjustViewHolder>() {
    private val TAG = javaClass.simpleName

    private lateinit var mAudioManager: AudioManager

    private val mVolumeList = mutableListOf<Int>()

    init {
        updateVolumeData(volumeList)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VolumeAdjustViewHolder {
        mAudioManager = mContext.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.include_volume_adjust, parent, false)
        return VolumeAdjustViewHolder(view)
    }

    fun updateVolumeData(volumeList: MutableList<Int>?) {
        volumeList?.let {
            mVolumeList.clear()
            mVolumeList.addAll(it)
        }
    }

    override fun getItemCount(): Int = mVolumeList.size

    override fun onBindViewHolder(
        holder: VolumeAdjustViewHolder,
        @SuppressLint("RecyclerView") position: Int
    ) {
        val streamType = mVolumeList[position]
        val flag: Int = when (streamType) {
            AudioManager.STREAM_MUSIC -> {
                AudioManager.FLAG_SHOW_UI
            }
            AudioManager.STREAM_ALARM -> {
                AudioManager.FLAG_SHOW_UI
            }
            AudioManager.STREAM_VOICE_CALL -> {
                AudioManager.FLAG_SHOW_UI
            }
            AudioManager.STREAM_RING -> {
                AudioManager.FLAG_REMOVE_SOUND_AND_VIBRATE
            }
            AudioManager.STREAM_SYSTEM -> {
                AudioManager.FLAG_REMOVE_SOUND_AND_VIBRATE
            }
            AudioManager.STREAM_NOTIFICATION -> {
                AudioManager.FLAG_REMOVE_SOUND_AND_VIBRATE
            }
            else -> {
                AudioManager.FLAG_SHOW_UI
            }
        }
        // 总音量差值
        val volumeDiffValue = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            mAudioManager.getStreamMaxVolume(streamType) - mAudioManager.getStreamMinVolume(streamType)
        } else  {
            mAudioManager.getStreamMaxVolume(streamType)
        }
        holder.mSbVolumeAdjust.apply {
            max = mAudioManager.getStreamMaxVolume(streamType)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                min = mAudioManager.getStreamMinVolume(streamType)
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                min = 0
            }
            progress = mAudioManager.getStreamVolume(streamType)
            setOnSeekBarChangeListener(object : OnSeekBarChangeListener {
                override fun onProgressChanged(
                    seekBar: SeekBar?,
                    progress: Int,
                    fromUser: Boolean
                ) {
                    if (fromUser) {
                        // TODO 后续处理这儿权限逻辑，详细处理
                        if (isHaveNotificationPermission()) {
                            mAudioManager.setStreamVolume(streamType, progress, flag)
                        } else {
                            Toast.makeText(mContext, "没有权限", Toast.LENGTH_SHORT).show()
                        }
                    }
                }

                override fun onStartTrackingTouch(seekBar: SeekBar?) {
                }

                override fun onStopTrackingTouch(seekBar: SeekBar?) {
                }

            })
        }
        holder.mIvVolumeDown.setOnClickListener {
            val volume: Int = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                (mAudioManager.getStreamVolume(streamType) - (0.05 * volumeDiffValue).toInt().coerceAtLeast(1))
                    .coerceAtLeast(mAudioManager.getStreamMinVolume(streamType))
            } else {
                (mAudioManager.getStreamVolume(streamType) - (0.05 * volumeDiffValue).toInt().coerceAtLeast(1)).coerceAtLeast(0)
            }
            Log.d(TAG, "setOnClickListener: volume = $volume, flag = $flag, streamType = $streamType")
            mAudioManager.setStreamVolume(streamType, volume, flag)
        }
        holder.mIvVolumeUp.setOnClickListener {
            val volume: Int = (mAudioManager.getStreamVolume(streamType) + (0.05 * volumeDiffValue).toInt().coerceAtLeast(1))
                .coerceAtMost(mAudioManager.getStreamMaxVolume(streamType))
            mAudioManager.setStreamVolume(streamType, volume, flag)
        }
    }

    private fun isHaveNotificationPermission(): Boolean {
        return NotificationManagerCompat.from(mContext).areNotificationsEnabled()
    }

    private fun requestNotificationPermission() {
        if (!isHaveNotificationPermission()) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                val intent = Intent(Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS)
                mContext.startActivity(intent)
            }
        }
    }
}