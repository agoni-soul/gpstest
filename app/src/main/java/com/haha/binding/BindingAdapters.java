package com.haha.binding;

import android.view.View;
import android.widget.TextView;

import androidx.core.content.ContextCompat;
import androidx.databinding.BindingAdapter;

import kotlin.Pair;

public final class BindingAdapters {

    private BindingAdapters() {
    }

    @BindingAdapter("visibleGone")
    public static void setVisibleGone(View view, Boolean visible) {
        view.setVisibility(Boolean.TRUE.equals(visible) ? View.VISIBLE : View.GONE);
    }

    @BindingAdapter("labelText")
    public static void setLabelText(TextView textView, Pair<String, String> pair) {
        if (pair == null) {
            textView.setText("");
            return;
        }
        textView.setText(pair.getFirst() + "：" + pair.getSecond());
    }

    @BindingAdapter("tagColorRes")
    public static void setTagColor(TextView textView, int colorRes) {
        if (colorRes == 0) {
            return;
        }
        int color = ContextCompat.getColor(textView.getContext(), colorRes);
        textView.setBackgroundColor(color);
    }
}
