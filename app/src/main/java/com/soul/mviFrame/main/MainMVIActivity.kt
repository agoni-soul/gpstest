package com.soul.mviFrame.main

import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.soul.gpstest.R
import com.soul.gpstest.databinding.ActivityMviBinding
import com.soul.mviFrame.base.BaseMVIActivity
import kotlinx.coroutines.launch

/**
 * @auther: soulagoni
 * @Date:   2025/12/7
 * @Detail:
 */
class MainMVIActivity: BaseMVIActivity<ActivityMviBinding, MainViewModel>() {

    // Initialize adapter with empty list
    private var adapter = MainAdapter(arrayListOf())

    // Lifecycle observer for monitoring activity lifecycle events
    private lateinit var myObserver: MyLifecycleObserver

    override fun getLayoutId(): Int = R.layout.activity_mvi

    override fun getViewModelClass(): Class<MainViewModel> = MainViewModel::class.java

    override fun initData() {
        observeViewModel() // Observe ViewModel state changes
        initObserver()
    }

    private fun initObserver() {
        //创建双察者
        myObserver = MyLifecycleObserver()
        //添加双察者
        lifecycle.addObserver(myObserver)
        //地可以使用 Lambda 表式
        lifecycle.addObserver(object : DefaultLifecycleObserver {
            override fun onResume (owner: LifecycleOwner) {
                // 处理恢复逻辑

            }
        })
    }

    override fun initView() {
        mViewDataBinding.recyclerView.layoutManager = LinearLayoutManager(this)
        mViewDataBinding.recyclerView.run {
            addItemDecoration(
                DividerItemDecoration(
                    mViewDataBinding.recyclerView.context,
                    (mViewDataBinding.recyclerView.layoutManager as LinearLayoutManager).orientation
                )
            )
        }
        mViewDataBinding.recyclerView.adapter = adapter

        mViewDataBinding.buttonFetchUser.setOnClickListener {
            lifecycleScope.launch {
                mViewModel.userIntent.send(MainIntent.FetchUser)
            }
        }
    }

    private fun observeViewModel() {
        lifecycleScope.launch {
            mViewModel.state.collect {
                when (it) {
                    is MainState.Idle -> {

                    }
                    is MainState.Loading -> {
                        mViewDataBinding.buttonFetchUser.visibility = View.GONE
                        mViewDataBinding.progressBar.visibility = View.VISIBLE
                    }

                    is MainState.Users -> {
                        mViewDataBinding.progressBar.visibility = View.GONE
                        mViewDataBinding.buttonFetchUser.visibility = View.GONE
                        renderList(it.user)
                    }
                    is MainState.Error -> {
                        mViewDataBinding.progressBar.visibility = View.GONE
                        mViewDataBinding.buttonFetchUser.visibility = View.VISIBLE
                        Toast.makeText(this@MainMVIActivity, it.error, Toast.LENGTH_LONG).show()
                    }
                }
            }
        }
    }

    private fun renderList(users: List<User>) {
        mViewDataBinding.recyclerView.visibility = View.VISIBLE
        users.let { listOfUsers -> listOfUsers.let { adapter.addData(it) } }
        adapter.notifyDataSetChanged()
    }
}

class MyLifecycleObserver: DefaultLifecycleObserver {
    private val TAG = javaClass.simpleName

    /**
     * Notifies that `ON_CREATE` event occurred.
     *
     *
     * This method will be called after the [LifecycleOwner]'s `onCreate`
     * method returns.
     *
     * @param owner the component, whose state was changed
     */
    override fun onCreate(owner: LifecycleOwner) {
        super.onCreate(owner)
        Log.d(TAG, "$TAG-onCreate")
    }

    /**
     * Notifies that `ON_DESTROY` event occurred.
     *
     *
     * This method will be called before the [LifecycleOwner]'s `onDestroy` method
     * is called.
     *
     * @param owner the component, whose state was changed
     */
    override fun onDestroy(owner: LifecycleOwner) {
        super.onDestroy(owner)
        Log.d(TAG, "$TAG-onDestroy")
    }

    /**
     * Notifies that `ON_PAUSE` event occurred.
     *
     *
     * This method will be called before the [LifecycleOwner]'s `onPause` method
     * is called.
     *
     * @param owner the component, whose state was changed
     */
    override fun onPause(owner: LifecycleOwner) {
        super.onPause(owner)
        Log.d(TAG, "$TAG-onPause")
    }

    /**
     * Notifies that `ON_RESUME` event occurred.
     *
     *
     * This method will be called after the [LifecycleOwner]'s `onResume`
     * method returns.
     *
     * @param owner the component, whose state was changed
     */
    override fun onResume(owner: LifecycleOwner) {
        super.onResume(owner)
        Log.d(TAG, "$TAG-onResume")
    }

    /**
     * Notifies that `ON_START` event occurred.
     *
     *
     * This method will be called after the [LifecycleOwner]'s `onStart` method returns.
     *
     * @param owner the component, whose state was changed
     */
    override fun onStart(owner: LifecycleOwner) {
        super.onStart(owner)
        Log.d(TAG, "$TAG-onStart")
    }

    /**
     * Notifies that `ON_STOP` event occurred.
     *
     *
     * This method will be called before the [LifecycleOwner]'s `onStop` method
     * is called.
     *
     * @param owner the component, whose state was changed
     */
    override fun onStop(owner: LifecycleOwner) {
        super.onStop(owner)
        Log.d(TAG, "$TAG-onStop")
    }
}

