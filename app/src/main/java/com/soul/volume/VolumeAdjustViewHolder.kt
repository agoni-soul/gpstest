package com.soul.volume

import android.view.View
import android.widget.ImageView
import android.widget.SeekBar
import androidx.recyclerview.widget.RecyclerView
import com.soul.gpstest.R


/**
 *     author : yangzy33
 *     time   : 2024-05-10
 *     desc   :
 *     version: 1.0
 */
class VolumeAdjustViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    val mIvVolumeDown: ImageView by lazy {
        itemView.findViewById(R.id.iv_volume_down)
    }

    val mIvVolumeUp: ImageView by lazy {
        itemView.findViewById(R.id.iv_volume_up)
    }

    val mSbVolumeAdjust: SeekBar by lazy {
        itemView.findViewById(R.id.sb_volume_adjust)
    }
}