package com.soul.ui.dialog

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.View
import android.widget.Button
import com.soul.gpstest.R

/**
 * <pre>
 *     author : yangzy33
 *     e-mail : yangzy33@midea.com
 *     time   : 2022/06/28
 *     desc   :
 *     version: 1.0
 * </pre>
 */
class CustomDialog(context: Context): Dialog(context, R.style.custom_dialog_style) {
    private lateinit var mButton: Button
    private var mListener: View.OnClickListener? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.dialog_button)
        mButton = findViewById(R.id.bt_dialog)

        mButton.setOnClickListener { v ->
            if (mListener != null) {
                mListener!!.onClick(v)
            }
            dismiss()
        }
    }

    fun setListener(listener: View.OnClickListener?) {
        mListener = listener
    }

    fun setContent(text: String?) {
        mButton.text = text ?: ""
    }
}