package com.soul.mviFrame.base

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

/**
 * @auther: soulagoni
 * @Date:   2025/12/10
 * @Detail:
 */
abstract class BaseViewModelFactory<VM: BaseMVIViewModel>: ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(getViewModelClass())) {
            return getViewModel() as T
        }
        throw IllegalArgumentException("Unknown class name")
    }

    abstract fun getViewModelClass(): Class<VM>

    abstract fun getViewModel(): VM
}