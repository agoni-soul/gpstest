package com.soul.volume

import android.annotation.SuppressLint
import android.content.Context
import android.media.AudioManager
import android.os.Build
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.SeekBar
import android.widget.SeekBar.OnSeekBarChangeListener
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
                        mAudioManager.setStreamVolume(streamType, progress, flag)
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
                mAudioManager.getStreamVolume(streamType)
                    .coerceAtLeast(mAudioManager.getStreamMinVolume(streamType))
            } else {
                mAudioManager.getStreamVolume(streamType).coerceAtLeast(0)
            }
            mAudioManager.setStreamVolume(streamType, volume, flag)
        }
        holder.mIvVolumeUp.setOnClickListener {
            val volume: Int = mAudioManager.getStreamVolume(streamType)
                .coerceAtMost(mAudioManager.getStreamMaxVolume(streamType))
            mAudioManager.setStreamVolume(streamType, volume, flag)
        }
    }
}