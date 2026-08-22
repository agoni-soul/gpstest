package com.haha.binding

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.haha.base.BaseViewModel
import com.haha.hahalearn.R
import com.haha.kmp.SharedStudy

class BindingAdapterViewModel(application: Application) : BaseViewModel(application) {

    private val _greeting = MutableLiveData<Pair<String, String>>()
    val greeting: LiveData<Pair<String, String>> = _greeting

    private val _detailVisible = MutableLiveData(false)
    val detailVisible: LiveData<Boolean> = _detailVisible

    private val _fibonacciHint = MutableLiveData<Pair<String, String>>()
    val fibonacciHint: LiveData<Pair<String, String>> = _fibonacciHint

    private val _tags = MutableLiveData<List<TagItem>>(emptyList())
    val tags: LiveData<List<TagItem>> = _tags

    fun loadDemoData() {
        _greeting.value = "平台" to SharedStudy.greeting().substringAfter("当前平台 ")
        _fibonacciHint.value = "斐波那契(10)" to SharedStudy.fibonacci(10).toString()
        _tags.value = listOf(
            TagItem("ViewBinding", R.color.teal_200),
            TagItem("BindingAdapter", R.color.purple_500),
            TagItem("DataBinding", R.color.orange),
        )
    }

    fun toggleDetailVisible() {
        _detailVisible.value = _detailVisible.value != true
    }
}

data class TagItem(
    val name: String,
    val colorRes: Int,
)
