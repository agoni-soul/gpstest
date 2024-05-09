package com.soul.remoteviews

import android.app.Activity
import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.Drawable
import android.os.Build
import android.widget.RemoteViews
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.SimpleTarget
import com.bumptech.glide.request.transition.Transition
import com.soul.gpstest.R

/**
 * <pre>
 *     author : yangzy33
 *     e-mail : yangzy33@midea.com
 *     time   : 2022/01/16
 *     desc   :
 *     version: 1.0
 * </pre>
 */
class CustomRemoteViews(context: Context, packageName: String, layoutId: Int): RemoteViews(packageName, layoutId) {
    private val mContext = context
    private val mRemoteViews: RemoteViews by lazy {
        this
    }

    fun build(icon: String?, name: String?): RemoteViews {
        Glide.with(mContext)
            .load(icon!!)
            .into(object : SimpleTarget<Drawable?>() {
                @RequiresApi(Build.VERSION_CODES.CUPCAKE)
                override fun onResourceReady(
                    resource: Drawable?,
                    transition: Transition<in Drawable?>?
                ) {
                    (mContext as Activity).runOnUiThread {
                        Toast.makeText(mContext, "haha", Toast.LENGTH_SHORT).show()
                        resource?.let {
                            val bitmap = drawable2Bitmap(it)
                            mRemoteViews.setImageViewBitmap(R.id.iv_device_icon, bitmap)

                            val notificationCompatBuilder = NotificationCompat.Builder(mContext, "custom_notification_1")
                            notificationCompatBuilder.setCustomContentView(mRemoteViews)
                                .setContentIntent(toNewActivity())
                                .setWhen(System.currentTimeMillis())
                                .setPriority(Notification.PRIORITY_DEFAULT)
                                .setOngoing(false)
                                .setSmallIcon(R.drawable.me_ic_message)
                                .setOnlyAlertOnce(true)

                            val notification = notificationCompatBuilder.build()
                            (mContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager).notify(1, notification)
                        }
                    }
                }
            })

        mRemoteViews.setTextViewText(R.id.tv_device_name, name ?: "")
        return this
    }

    fun toNewActivity(): PendingIntent {
        val intent = Intent(mContext, ShowNotificationActivity::class.java)
        return PendingIntent.getBroadcast(mContext, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)
    }

    private fun drawable2Bitmap(drawable: Drawable?): Bitmap? {
        if (drawable == null) return null
        val bitmap: Bitmap? = Bitmap.createBitmap(drawable.intrinsicWidth, drawable.intrinsicHeight, Bitmap.Config.ARGB_8888)
                ?: return null
        val canvas = Canvas(bitmap!!)
        drawable.setBounds(0, 0, canvas.width, canvas.height)
        drawable.draw(canvas)
        return bitmap
    }
 }