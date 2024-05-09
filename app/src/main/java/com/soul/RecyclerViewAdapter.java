package com.soul;

import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

/**
 * <pre>
 *     author : yangzy33
 *     e-mail : yangzy33@midea.com
 *     time   : 2022/07/21
 *     desc   :
 *     version: 1.0
 * </pre>
 */
public class RecyclerViewAdapter extends RecyclerView.Adapter {
    private List<Integer> userList;
    private List<String> timeList;
    private View.OnClickListener listener = null;

    public RecyclerViewAdapter(List<Integer> userList, List<String> timeList, View.OnClickListener listener) {
        this.userList = userList;
        this.timeList = timeList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        return null;
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {

    }

    @Override
    public int getItemCount() {
        return 0;
    }
}
