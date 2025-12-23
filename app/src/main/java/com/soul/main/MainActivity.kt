package com.soul.main

import android.Manifest
import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.AppOpsManager
import android.app.Service
import android.app.usage.NetworkStatsManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Shader
import android.net.ConnectivityManager
import android.net.LinkProperties
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.net.wifi.WifiManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.os.RemoteException
import android.os.SystemClock
import android.provider.Settings
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.TextPaint
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.text.style.ForegroundColorSpan
import android.transition.Slide
import android.util.Log
import android.view.View
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityManager
import android.view.accessibility.AccessibilityNodeInfo
import android.view.accessibility.AccessibilityNodeProvider
import android.view.animation.AnimationUtils
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.splashscreen.SplashScreen
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.fragment.app.DialogFragment
import com.blankj.utilcode.util.GsonUtils
import com.soul.animation.AnimationActivity
import com.soul.base.BaseMvvmActivity
import com.soul.base.BaseViewModel
import com.soul.bean.SubDeviceResultBean
import com.soul.binder.TestService
import com.soul.bluetooth.BluetoothActivity
import com.soul.coroutineScope.CoroutineScopeActivity
import com.soul.coroutineScope.EatGame
import com.soul.dynamicTextView.DynamicTextViewActivity
import com.soul.easyswipemenulayout.EasySwipeMenuActivity
import com.soul.gps.GpsActivity
import com.soul.gpstest.IProcessStub
import com.soul.gpstest.R
import com.soul.gpstest.databinding.ActivityMainBinding
import com.soul.liveData.LiveDataActivity
import com.soul.log.DOFLogUtil
import com.soul.main.logMonitor.UiPerfMonitor
import com.soul.main.network.NetworkIp
import com.soul.main.pieChartView.PieChartBean
import com.soul.main.timeMonitor.TimeMonitorConfig
import com.soul.main.timeMonitor.TimeMonitorManager
import com.soul.mviFrame.main.MainMVIActivity
import com.soul.recyclerview.RecyclerViewActivity
import com.soul.scene.CustomSceneFirstActivity
import com.soul.scene.SceneFirstActivity
import com.soul.selector.SelectorActivity
import com.soul.service.accessibility.CustomAccessibilityService
import com.soul.transparency.TransparencyActivity
import com.soul.util.DpOrSpToPxTransfer
import com.soul.util.PermissionUtils
import com.soul.volume.ui.VolumeActivity
import com.soul.waterfall.WaterFallActivity
import com.soul.wifi.WifiActivity


class MainActivity : BaseMvvmActivity<ActivityMainBinding, BaseViewModel>(), View.OnClickListener {

    companion object {
        private const val SPLASH_DURATION = 1500L
    }

    /**
     * A native method that is implemented by the 'GPSTest' native library,
     * which is packaged with this application.
     */
    external fun stringFromJNI(): String?

    private var mCustomAccessibilityService: CustomAccessibilityService? = null

    private val mAccessibilityManager: AccessibilityManager by lazy {
        getSystemService(Context.ACCESSIBILITY_SERVICE) as AccessibilityManager
    }

    private val mConnectivityManager: ConnectivityManager by lazy {
        getSystemService(CONNECTIVITY_SERVICE) as ConnectivityManager
    }

    private var mNetworkIp: NetworkIp? = null

    private var keepSplashOnScreen = true

    private var mSplashScreen: SplashScreen? = null

    override fun getViewModelClass(): Class<BaseViewModel> = BaseViewModel::class.java
    override fun getLayoutId(): Int = R.layout.activity_main

    override fun onCreate(savedInstanceState: Bundle?) {
        mSplashScreen = installSplashScreen()
        super.onCreate(savedInstanceState)
    }

    override fun hideTitleAndActionBar() {
        // Android 12 以上，SplashScreen会自行处理隐藏顶部状态栏的逻辑
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) {
            super.hideTitleAndActionBar()
        }
    }

    override fun extraConfig() {
        super.extraConfig()

        TimeMonitorManager.getInstance()
            .getTimeMonitor(TimeMonitorConfig.TIME_MONITOR_ID_APPLICATION_START)
            .recodingTimeTag("AppStartActivity_create")

        // 设置保持条件，当keepSplashOnScreen为false时，闪屏页会消失
        mSplashScreen?.setKeepOnScreenCondition { keepSplashOnScreen }

        // 模拟一些初始化工作
        simulateInitialization {
            val processors = Runtime.getRuntime().availableProcessors()
            keepSplashOnScreen = false
        }
    }

    private fun simulateInitialization(onInitializationComplete: () -> Unit) {
        // 模拟延迟，比如网络请求或数据加载
        Handler(Looper.getMainLooper()).postDelayed({
            onInitializationComplete()
        }, 2000)
    }

    override fun initView() {
        mViewDataBinding.btnSkipGps.setOnClickListener(this)
        mViewDataBinding.btnSkipRemoteView.setOnClickListener(this)
        mViewDataBinding.btnSkipNetwork.setOnClickListener(this)
        mViewDataBinding.btnRefresh.setOnClickListener(this)
        mViewDataBinding.btnTest.setOnClickListener(this)
        mViewDataBinding.btnTest1.setOnClickListener(this)
        mViewDataBinding.btnPermission.setOnClickListener(this)
        mViewDataBinding.btnDialogFragment.setOnClickListener(this)
        mViewDataBinding.btnAnimation.setOnClickListener(this)
        mViewDataBinding.btnActivityScene.setOnClickListener(this)
        mViewDataBinding.btnActivityCoroutineScope.setOnClickListener(this)
        mViewDataBinding.btnActivityCustomScene.setOnClickListener(this)
        mViewDataBinding.btnActivityCustomScene2.setOnClickListener(this)
        mViewDataBinding.btnActivityRecyclerview.setOnClickListener(this)
        mViewDataBinding.btnActivityWaterfall.setOnClickListener(this)
        mViewDataBinding.btnActivitySelector.setOnClickListener(this)
        mViewDataBinding.btnActivityVolume.setOnClickListener(this)
        mViewDataBinding.btnActivitySlide.setOnClickListener {
            val intent = Intent(mContext, EasySwipeMenuActivity::class.java)
            startActivity(intent)
        }
        mViewDataBinding.btnActivityBluetooth.setOnClickListener {
            val intent = Intent(mContext, BluetoothActivity::class.java)
            intent.putExtras(Intent())
            startActivity(intent)
        }
        mViewDataBinding.cpv.apply {
            setProgress(20f)
            setCircleBgColor(Color.RED)
            setProgressColor(Color.BLACK)
            setCenterText("哈哈哈哈")
            setCenterTextColor(Color.GREEN)
            setCenterTextSize(DpOrSpToPxTransfer.sp2px(mContext, 18).toFloat())
            invalidate()
        }
        mViewDataBinding.btnActivityDynamic.setOnClickListener {
            val intent = Intent(mContext, DynamicTextViewActivity::class.java)
            startActivity(intent)
        }
        val picList = mutableListOf<PieChartBean>()
        val colors: IntArray = intArrayOf(ContextCompat.getColor(mContext, R.color.circle_gradual_end),
            ContextCompat.getColor(mContext, R.color.circle_gradual_end))
        val gradient = LinearGradient(0f, 0f, 100f, 100f, colors, null, Shader.TileMode.CLAMP)
        picList.add(PieChartBean("0", 25f, gradient))
        picList.add(PieChartBean("1", 25f, gradient))
        picList.add(PieChartBean("2", 25f, gradient))
        picList.add(PieChartBean("3", 25f, gradient))
        mViewDataBinding.csv.setDate(picList)
        mViewDataBinding.btnActivityLiveData.setOnClickListener {
            startActivity(Intent(mContext, LiveDataActivity::class.java))
        }
        mViewDataBinding.btnActivityRecyclerView.setOnClickListener {
            startActivity(Intent(mContext, RecyclerViewActivity::class.java))
        }
        mViewDataBinding.btnActivityRecyclerView.post {
            val options = BitmapFactory.Options()
            options.inMutable = true
            val bitmap = BitmapFactory.decodeResource(resources, R.drawable.net_ic_phone, options)
            bitmap.config = Bitmap.Config.RGB_565
            val byteCount = bitmap.byteCount // 直接获取内存占用字节数
            Log.d("Memory", "Bitmap size: $byteCount bytes")
        }
        mViewDataBinding.btnActivityPlugin.setOnClickListener(this)
        mViewDataBinding.btnUiMonitor.setOnClickListener {
            //TODO 目前版本问题，导致获取权限不到，无法使用，后续优化
            changeMonitorPerf()
        }
        mViewDataBinding.btnActivityMviFrame.setOnClickListener(this)

        testService()
        /**
        if (isSatisfiedAndroidVersion(Build.VERSION_CODES.R)) {
        mConnectivityDiagnosticsManager = getSystemService(Context.CONNECTIVITY_DIAGNOSTICS_SERVICE) as ConnectivityDiagnosticsManager
        val request = NetworkRequest.Builder().addTransportType(NetworkCapabilities.TRANSPORT_WIFI).build()
        mConnectivityDiagnosticsManager.registerConnectivityDiagnosticsCallback(
        request,
        Executors.newSingleThreadExecutor(),
        mConnectivityDiagnodsticsCallback
        )

        mNetworkCallback = object : ConnectivityManager.NetworkCallback() {
        override fun onBlockedStatusChanged(network: Network, blocked: Boolean) {
        super.onBlockedStatusChanged(network, blocked)
        Log.d("haha", "onBlockedStatusChanged Network = $network \t blocked = $blocked")
        }

        override fun onAvailable(network: Network) {
        super.onAvailable(network)
        Log.d("haha", "onAvailable Network = $network")
        }

        override fun onCapabilitiesChanged(
        network: Network,
        networkCapabilities: NetworkCapabilities
        ) {
        super.onCapabilitiesChanged(network, networkCapabilities)
        Log.d(
        "haha",
        "onCapabilitiesChanged Network = $network \t networkCapabilities = $networkCapabilities"
        )
        }

        override fun onLinkPropertiesChanged(
        network: Network,
        linkProperties: LinkProperties
        ) {
        super.onLinkPropertiesChanged(network, linkProperties)
        val addresses = linkProperties.linkAddresses;
        var hostAddress: String? = null
        for (address in addresses) {
        if (address.address.hostAddress.contains(".") == true) {
        hostAddress = address.address.hostAddress
        Log.d(TAG, "hostAddress = $hostAddress")
        break
        }
        }
        Log.d(TAG, "linkProperties: ${linkProperties.linkAddresses}")
        pingforInetAddresss(hostAddress ?: "")
        //                pingForCMD("163.177.151.110")
        Log.d(
        "haha",
        "onLinkPropertiesChanged Network = $network \t linkProperties = $linkProperties"
        )
        }

        override fun onLosing(network: Network, maxMsToLive: Int) {
        super.onLosing(network, maxMsToLive)
        Log.d("haha", "onLosing Network = $network \t maxMsToLive = $maxMsToLive")
        }

        override fun onLost(network: Network) {
        super.onLost(network)
        Log.d("haha", "onLost Network = $network")
        }

        override fun onUnavailable() {
        super.onUnavailable()
        Log.d("haha", "onUnavailable")
        }
        }

        mConnectivityManager.registerNetworkCallback(request, mNetworkCallback)
        }

        val locationPermissionRequest = registerForActivityResult(
        ActivityResultContracts.RequestPermissions()
        ) { permissions ->
        when {
        permissions.getOrDefault(Manifest.permission.ACCESS_FINE_LOCATION, false) -> {
        // Precise location access granted.
        }
        permissions.getOrDefault(Manifest.permission.ACCESS_COARSE_LOCATION, false) -> {
        // Only approximate location access granted.
        } else -> {
        // No location access granted.
        }
        }
        }
        locationPermissionRequest.launch(arrayOf(
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.ACCESS_COARSE_LOCATION))
         **/
    }

    private fun testService() {
        val intent = Intent(this, TestService::class.java)
        bindService(intent, object : ServiceConnection {
            override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
                val stub = IProcessStub.Stub.asInterface(service)
                try {
                    stub.request(" ")
                } catch (e: RemoteException) {
                    e.printStackTrace()
                }
            }

            override fun onServiceDisconnected(name: ComponentName?) {

            }

        }, Context.MODE_PRIVATE.or(Context.BIND_AUTO_CREATE))
    }

    private fun changeMonitorPerf() {
        if (UiPerfMonitor.getInstance().isMonitoring()) {
            UiPerfMonitor.getInstance().stopMonitor()
            mViewDataBinding.btnUiMonitor.text = resources.getText(R.string.monitor_control_start)
        } else {
            UiPerfMonitor.getInstance().startMonitor()
            mViewDataBinding.btnUiMonitor.text = resources.getText(R.string.monitor_control_stop)
        }
    }

    val config: EatGame by lazy(LazyThreadSafetyMode.NONE) {
        EatGame() // 非线程安全，但初始化更快
    }

    override fun isUsedEncapsulatedPermissions(): Boolean = true

    override fun requestPermissionArray(): Array<String> {
        return if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.S_V2) {
            arrayOf(
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            )
        } else {
            arrayOf(
                Manifest.permission.READ_MEDIA_VIDEO,
                Manifest.permission.READ_MEDIA_AUDIO,
                Manifest.permission.READ_MEDIA_IMAGES
            )
        }
    }

    override fun handlePermissionResult(permissionResultMap: Map<String, Boolean>) {
        permissionResultMap.forEach { (k, v) ->
            Log.d(TAG, "$k ----->>>>>  $v")
        }
    }

    override fun initData() {
        setupWindowAnimations()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            accessibilityTest()
        }

        val builder =
            SpannableStringBuilder("请注意将温度传感器靠近网关\n若长时间未搜索到，可尝试重新操作设备")
        val str = "重新操作设备"
        val span = ForegroundColorSpan(Color.RED)
        builder.setSpan(object : ClickableSpan() {
            override fun updateDrawState(ds: TextPaint) {
                super.updateDrawState(ds)
                ds.color = Color.RED
                ds.isUnderlineText = false
            }

            override fun onClick(widget: View) {
                Toast.makeText(this@MainActivity, "nihao", Toast.LENGTH_SHORT).show()
            }

        }, builder.length - str.length, builder.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)

        mViewDataBinding.tvSpan.text = builder
        mViewDataBinding.tvSpan.movementMethod = LinkMovementMethod.getInstance()
        Log.d(TAG, "C++ = ${stringFromJNI()}")
//        Thread {
//            synchronizedTest()
//        }.start()

        TimeMonitorManager.getInstance()
            .getTimeMonitor(TimeMonitorConfig.TIME_MONITOR_ID_APPLICATION_START)
            .recodingTimeTag("AppStartActivity_createOver")
    }

    @Synchronized
    private fun synchronizedTest() {
        synchronized(this) {
            val startTime = System.currentTimeMillis()
            Log.d(TAG, "synchronizedTest: start time = $startTime")
            Thread.sleep(21000)
            Log.d(TAG, "synchronizedTest: end time = ${System.currentTimeMillis() - startTime}")
        }
    }

    override fun onStart() {
        super.onStart()
        TimeMonitorManager.getInstance()
            .getTimeMonitor(TimeMonitorConfig.TIME_MONITOR_ID_APPLICATION_START)
            .end("AppStartActivity_start", false)
    }

    override fun onResume() {
//        synchronizedTest()
        super.onResume()

        TestLearnUtils.test(mContext)
        TestLearnUtils.test(mViewDataBinding.btnSkipGps)
        TestLearnUtils.test()
    }

    override fun getStatusBarColor(): Int {
        return R.color.transparent
    }

    override fun getNavigationBarColor(): Int {
        return R.color.transparent
    }

    override fun isBlackStatusText(): Boolean {
        return true
    }

    override fun isShowStatus(): Boolean {
        return false
    }

    override fun isShowNavigation(): Boolean {
        return false
    }

    override fun getRootViewId(): Int {
        return R.id.cl_main
    }

    @RequiresApi(Build.VERSION_CODES.R)
    private fun accessibilityTest() {
//        mViewDataBinding.btnSkipGps.isEnabled = true
//        mViewDataBinding.btnSkipNetwork.isEnabled = true
//        mViewDataBinding.btnSkipRemoteView.isEnabled = true
//        mViewDataBinding.btnSkipRefresh.isEnabled = true
//        mViewDataBinding.btnSkipGps.isFocusable = true
//        mViewDataBinding.btnSkipNetwork.isFocusable = true
//        mViewDataBinding.btnSkipRemoteView.isFocusable = true
//        mViewDataBinding.btnSkipRefresh.isFocusable = true
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//            mViewDataBinding.btnSkipGps.focusable = View.FOCUSABLE
//            mViewDataBinding.btnSkipNetwork.focusable = View.FOCUSABLE
//            mViewDataBinding.btnSkipRemoteView.focusable = View.FOCUSABLE
//            mViewDataBinding.btnSkipRefresh?Network.focusable = View.FOCUSABLE

//            val event = AccessibilityEvent()
//            event.eventType = AccessibilityEvent.TYPE_VIEW_FOCUSED
//            mViewDataBinding.btnSkipGps.onInitializeAccessibilityEvent(event)

//            val btnSkipGPSNode = mViewDataBinding.btnSkipGps.createAccessibilityNodeInfo()
//            val btnSkipNetwork = mViewDataBinding.btnSkipNetwork.createAccessibilityNodeInfo()
//            val btnSkipRemoteView = mViewDataBinding.btnSkipRemoteView.createAccessibilityNodeInfo()
//            val btnRefreshNetwork = mViewDataBinding.btnSkipRefresh.createAccessibilityNodeInfo()
//
//            btnSkipGPSNode.setTraversalAfter(mViewDataBinding.btnSkipNetwork?)
//            btnSkipNetwork.setTraversalAfter(mViewDataBinding.btnSkipRemoteView?)
//            btnSkipRemoteView.setTraversalAfter(mViewDataBinding.btnSkipRefresh?)

//            mViewDataBinding.btnSkipGps.accessibilityTraversalBefore = R.id.btn_skip_network
//            mViewDataBinding.btnSkipNetwork.accessibilityTraversalBefore = R.id.btn_skip_remote_view
//            mViewDataBinding.btnSkipRemoteView.accessibilityTraversalBefore = R.id.btn_refresh

//            if (mAccessibilityManager.isEnabled) {
//                mViewDataBinding.btnSkipGps.sendAccessibilityEventUnchecked(event)
//            }
        }

//        mViewDataBinding.btnSkipGps.findUserSetNextFocus(mViewDataBinding.clMain?, View.FOCUS_DOWN)


//        mViewDataBinding.btnSkipGps.nextFocusDownId = R.id.btn_skip_network
//        mViewDataBinding.btnSkipGps.nextFocusDownId = R.id.btn_skip_network
//        mViewDataBinding.btnSkipNetwork.nextFocusDownId = R.id.btn_skip_remote_view
//        mViewDataBinding.btnSkipNetwork.nextFocusDownId = R.id.btn_skip_remote_view
//        mViewDataBinding.btnSkipNetwork.nextFocusUpId = R.id.btn_skip_gps
//        mViewDataBinding.btnSkipRemoteView.nextFocusUpId = R.id.btn_skip_network
//        mViewDataBinding.btnSkipRemoteView.nextFocusDownId = R.id.btn_refresh

//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
//            val btnSkipGPSNodeInfo = AccessibilityNodeInfo(mViewDataBinding.btnSkipGps?)
//            val btnSkipRemoteViewNodeInfo = AccessibilityNodeInfo(mViewDataBinding.btnSkipRemoteView?)
//            val btnSkipNetworkNodeInfo = AccessibilityNodeInfo(mViewDataBinding.btnSkipNetwork?)
//            btnSkipGPSNodeInfo.setTraversalAfter(mViewDataBinding.btnSkipNetwork?)
//            btnSkipNetworkNodeInfo.setTraversalBefore(mViewDataBinding.btnSkipGps?)
//            btnSkipNetworkNodeInfo.setTraversalAfter(mViewDataBinding.btnSkipRemoteView?)
//            btnSkipRemoteViewNodeInfo.setTraversalBefore(mViewDataBinding.btnSkipNetwork?)
//
//            DOFLogUtil.d(TAG, "btnSkipGPSNodeInfo = $btnSkipGPSNodeInfo")
//        } else {
//            DOFLogUtil.d(TAG, "version = ${Build.VERSION.SDK_INT}")
//        }

        /*
        val instance: BaseService = BaseService.getInstance()
        instance.init(this)
        if (!instance.checkAccessibilityEnabled("暗黑魔心的无障碍服务")) {
            instance.goAccess()
        }
         */
    }

    @RequiresApi(Build.VERSION_CODES.R)
    private fun test1() {
        mViewDataBinding ?: return
        val accessibilityNodeInfo = AccessibilityNodeInfo(mViewDataBinding!!.clMain)
        val list = accessibilityNodeInfo.actionList

        list.let {
            for (accessibilityAction in list) {
                DOFLogUtil.d(
                    TAG,
                    "id = ${accessibilityAction.id}, label = ${accessibilityAction.label}"
                )
            }
        }

        val mBtnGPSProvider = object : AccessibilityNodeProvider() {
            override fun createAccessibilityNodeInfo(virtualViewId: Int): AccessibilityNodeInfo? {
                return mViewDataBinding.btnSkipGps.let { AccessibilityNodeInfo(it) }
            }

            override fun findFocus(focus: Int): AccessibilityNodeInfo? {
                return mViewDataBinding.btnSkipGps.let { AccessibilityNodeInfo(it) }
            }
        }
        mBtnGPSProvider.createAccessibilityNodeInfo(AccessibilityNodeProvider.HOST_VIEW_ID)
    }

    /**
     * 启动无障碍服务
     */
    private fun startAccessibilityService() {
        val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
        startActivity(intent)
    }

    private fun setupWindowAnimations() {
//        val slide: Slide =
//            TransitionInflater.from(this).inflateTransition(R.transition.activity_slide) as Slide
        val slide = Slide()
        slide.duration = 1000
        window.exitTransition = slide
    }

    @SuppressLint("WrongConstant")
    fun getUid(context: Context): Int? {
        try {
            val pm = context.packageManager
            val ai = pm.getApplicationInfo(context.packageName, PackageManager.GET_ACTIVITIES)
            Log.d("UID", "getUid:" + ai.uid + "\t ${context.packageName}")
            return ai.uid
        } catch (e: PackageManager.NameNotFoundException) {
            e.printStackTrace()
        }
        return null
    }

    @RequiresApi(Build.VERSION_CODES.M)
    private fun checkPermission() {
//        val requestPermissionLauncher: RequestPermissionLauncher? = null
//        when {
//            ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
//                    == PackageManager.PERMISSION_GRANTED -> {
//
//            }
//            shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION) -> {
//
//            }
//            else -> {
//                requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
//            }
//        }
    }

    private fun wifiInfo() {
        if (ActivityCompat.checkSelfPermission(
                this,
                GpsActivity.ACCESS_FIND_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            val dialog = AlertDialog.Builder(this)
            dialog.apply {
                setTitle("请求精准定位权限")
                setMessage("获取位置经纬度需要获取精准权限")
                setPositiveButton("同意") { _, _ ->
                    ActivityCompat.requestPermissions(
                        this@MainActivity,
                        arrayOf(GpsActivity.ACCESS_FIND_LOCATION),
                        200
                    )
                }
                setNegativeButton("拒绝", null)
            }
            dialog.show()
            return
        }

        val wifiManager = applicationContext.getSystemService(WIFI_SERVICE) as WifiManager
        val list = wifiManager.scanResults
        for (i in list.indices) {
            val scanResult = list[i]
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                Log.d(
                    "haha", "ssid = " + scanResult.SSID +
                            ", capabilities = " + scanResult.capabilities + ", wifiStandard = " + scanResult.wifiStandard
                )
            }
        }
    }

    private fun connect(ssid: String, context: Context) {
        Log.i("haha", "try connect to $ssid")
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val nr = NetworkRequest.Builder()
            .addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
            .setNetworkSpecifier(ssid)
            .build()

        val callback = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                Log.i("haha", "onAvailable $network ${network.javaClass.name}")
            }

            override fun onCapabilitiesChanged(
                network: Network,
                networkCapabilities: NetworkCapabilities
            ) {
                try {
                    Log.i(
                        "haha",
                        "onCapabilitiesChanged ${networkCapabilities.linkDownstreamBandwidthKbps}"
                    )
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }

            override fun onLinkPropertiesChanged(network: Network, linkProperties: LinkProperties) {
                try {
                    Log.i("haha", "onLinkPropertiesChanged " + linkProperties.interfaceName)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
        Log.i("haha", "requestNetwork!")
        cm.registerNetworkCallback(nr, callback)
    }

    private fun inspectNetworks() {
        val connectivity = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val ns = connectivity.allNetworks
        for (n in ns) {
            val c = connectivity.getNetworkCapabilities(n)
            Log.i("haha", "inspectNetworks: network = $n \t capabilities = $c")
        }
    }

    @RequiresApi(Build.VERSION_CODES.R)
    override fun onClick(v: View?) {
        v?.let {
            when (v.id) {
                R.id.btn_skip_gps -> {
                    val intent = Intent(this, GpsActivity::class.java);
                    startActivity(intent)
                    val resultList =
                        "{\"uid\":\"469f7d2494b94b28ad5ce5edb61ef632\",\"level\":\"0\",\"subDevices\":\"[{\\\"enterpriseCode\\\":\\\"0000\\\",\\\"modelId\\\":\\\"midea.switch.011.003\\\",\\\"errorCode\\\":0,\\\"subType\\\":\\\"1104\\\",\\\"sn\\\":\\\"9035EAFFFE842AEC\\\",\\\"type\\\":\\\"0x21\\\",\\\"spid\\\":10001698,\\\"deviceId\\\":177021372099829,\\\"deviceName\\\":\\\"midea\\\",\\\"errorMsg\\\":null}]\",\"transId\":\"DFB2190D5220A53DC35AAF2A1F4FD13B\",\"appId\":\"900\",\"pubTs\":\"1688024614\",\"targetUid\":\"469f7d2494b94b28ad5ce5edb61ef632\",\"exp\":\"2023-07-01 15:43:34\",\"pushTime\":\"2023-06-29 15:43:34\",\"gatewayId\":\"177021372100423\",\"pushType\":\"gateway\\/subAppliance\\/bind\"}"
                    var bean: SubDeviceResultBean? = null
                    try {
                        bean = GsonUtils.fromJson(resultList, SubDeviceResultBean::class.java)
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                    DOFLogUtil.d(TAG, "bean = $bean")
                    startActivity(Intent(this, TestActivity::class.java))
                }

                R.id.btn_skip_remote_view -> {
//                    val intent = Intent(this, RemoteViewActivity::class.java);
//                    startActivity(intent)
//                    val c = getSystemService(NETWORK_POLICY_SERVICE) as NetworkPolicyManager

                    mViewDataBinding.ivSuccess.visibility = View.VISIBLE
                    val animation = AnimationUtils.loadAnimation(this, R.anim.success_up_anim)
                    mViewDataBinding.ivSuccess.startAnimation(animation)
                }

                R.id.btn_skip_network -> {
                    /*
                    val intent = Intent(this, NetworkActivity::class.java);
                    startActivity(intent)
                     */
                    mViewDataBinding.btnSkipGps.sendAccessibilityEvent(AccessibilityEvent.TYPE_VIEW_CLICKED)

                    val constructor = AccessibilityEvent::class.java.getDeclaredConstructor()
                    constructor.isAccessible = true
                    val accessibilityEvent = constructor.newInstance()
                    accessibilityEvent.eventType = AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED
                    accessibilityEvent.className = mViewDataBinding.btnSkipGps.javaClass.name
                    DOFLogUtil.d(TAG, "accessibilityEvent = $accessibilityEvent")
                    DOFLogUtil.d(
                        TAG,
                        "accessibilityEvent = ${mViewDataBinding.btnSkipGps.accessibilityTraversalAfter}"
                    )
                }

                R.id.btn_refresh -> {
//                    val panelIntent = Intent(Settings.Panel.ACTION_INTERNET_CONNECTIVITY)
//                    startActivityForResult(panelIntent, -1)
//                    runOnUiThread {
//                        isNetworkOnline()
//                    }
                    /*
                    val connectivityManager =
                        getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
                    val wifiManager =
                        applicationContext.getSystemService(WIFI_SERVICE) as WifiManager
                    val wifiInfo = wifiManager.connectionInfo
                    val wifiName = wifiInfo.ssid
                    Log.d("haha", wifiName)
                    Log.d("haha", "speed: ${wifiInfo.linkSpeed}")
                     */

                    val intent = Intent(this, CustomAccessibilityService::class.java)
                    startService(intent)
                }

                R.id.btn_test -> {
                    if (!isSatisfiedAndroidVersion(Build.VERSION_CODES.M)) return@let

                    val uid = getUid(this)

                    val wifiManager =
                        applicationContext.getSystemService(WIFI_SERVICE) as WifiManager
                    uid?.let {
                        val networkInfo =
                            mConnectivityManager.getNetworkInfo(mConnectivityManager.activeNetwork)
                        networkInfo?.let {
                            Log.d("haha", "${it.isConnectedOrConnecting} \t ${it.detailedState}")
                        }
                        val networkCapabilities = mConnectivityManager.getNetworkCapabilities(
                            mConnectivityManager.activeNetwork
                        )

                        val b = mConnectivityManager.isDefaultNetworkActive
                        Log.d("haha", "isDefaultNetworkActive = $b")

                        val networkInfo1 = mConnectivityManager.activeNetworkInfo
                        networkInfo1?.let { info ->
                            Log.d(TAG, "detailedState = " + info.detailedState.toString())
                        }

                        if (isSatisfiedAndroidVersion(Build.VERSION_CODES.M)) {
                            mConnectivityManager.activeNetwork?.let { network ->
                                Log.d(TAG, "netId = $network")
                            }
                        }

                        val proxy = mConnectivityManager.defaultProxy
                        proxy?.let { proxy ->
                            Log.d(
                                TAG,
                                "proxy: host = ${proxy.host} \t port = ${proxy.port} \t isValid = ${proxy.isValid}"
                            )
                        }
                    }
                    val nsm = getSystemService(NETWORK_STATS_SERVICE) as NetworkStatsManager
//                    if (hasPermissionToReadNetworkStats()) {
//                        Thread {
//                            var bucket: NetworkStats.Bucket? = null
//                            try{
//                                bucket = nsm.querySummaryForDevice(ConnectivityManager.TYPE_WIFI, "", 0, System.currentTimeMillis())
//                            } catch (e: RemoteException) {
//                                e.printStackTrace()
//                            }
//                            bucket.let {
//                                val total = it.rxBytes + it.txBytes
//                                Log.d("haha", "Total = $total")
//                                Log.d("haha", "rxBytes = ${it.rxBytes}")
//                                Log.d("haha", "txBytes = ${it.txBytes}")
//                            }
//                        }.start()
//                    }


                    Thread {
                        val wifiManager =
                            getApplicationContext().getSystemService(WIFI_SERVICE) as WifiManager
                        connect(wifiManager.connectionInfo.ssid, this)
                    }.start()

//        Thread {
//            val isConnect = cm.getConnectionOwnerUid(
//                IPPROTO_TCP,
//                InetSocketAddress(InetAddress.getByName("10.74.35.6"), 80),
//                InetSocketAddress(InetAddress.getByName("163.177.151.110"), 80)
//            )
//            Log.d("haha", "isConnect = $isConnect")
//        }.start()

                    Thread {
                        val network = mConnectivityManager.boundNetworkForProcess
                        Log.d("haha", "boundNetworkForProcess = $network")

                        mNetworkIp?.pingforInetAddresss("163.177.151.110")
                        mNetworkIp?.pingForCMD("163.177.151.110")
                    }.start()
                }

                R.id.btn_test1 -> {
//                    if (mNetworkIp == null) {
//                        mNetworkIp = NetworkIp()
//                    }
//                    mNetworkIp?.isWifiConnected(this, mConnectivityManager.activeNetworkInfo)
//
//                    NetWorkUtils.requestNetwork(this)
//                    val b =
//                        mConnectivityManager.bindProcessToNetwork(mConnectivityManager.activeNetwork)
//                    Log.d("haha", "bindProcessToNetwork: $b")
                    createMemoryChurn()
//                    testANRService()
//                    createRunnableChurn()
                }

                R.id.btn_permission -> {
                    val intent = Intent(this, TransparencyActivity::class.java)
                    val permissions = Array(1) { Manifest.permission.ACCESS_FINE_LOCATION }
                    intent.putExtra("permissions", permissions)
                    startActivity(intent)
                }

                R.id.btn_dialog_fragment -> {
                    val dialog = DialogFragment()
                    dialog.let {
                        it.isCancelable = false
                        it.isCancelable = false
                        it.show(supportFragmentManager, "")
                    }
                }

                R.id.btn_animation -> {
                    val intent = Intent(this, AnimationActivity::class.java)
//                    startActivity(
//                        intent,
//                        ActivityOptions.makeSceneTransitionAnimation(this).toBundle()
//                    )
                    startActivity(intent)
                }

                R.id.btn_activity_scene -> {
                    val intent = Intent(this, SceneFirstActivity::class.java)
                    startActivity(intent)
                }

                R.id.btn_activity_coroutineScope -> {
                    val intent = Intent(this, CoroutineScopeActivity::class.java)
                    startActivity(intent)
                }

                R.id.btn_activity_custom_scene -> {
                    val intent = Intent(this, CustomSceneFirstActivity::class.java)
                    startActivity(intent)
                }

                R.id.btn_activity_custom_scene2 -> {
//                    val intent = Intent(this, CustomSceneSecondActivity::class.java)
//                    startActivity(intent)
//                    PermissionUtil.gotoPermission(this)
                    com.blankj.utilcode.util.PermissionUtils.launchAppDetailsSettings()
                }

                R.id.btn_activity_recyclerview -> {
                    if (!PermissionUtils.checkGPSPermission()) {
                        PermissionUtils.requestPermissions(
                            this,
                            Manifest.permission.ACCESS_COARSE_LOCATION,
                            Manifest.permission.ACCESS_FINE_LOCATION
                        )
                    } else {

                        val wifiManager =
                            applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager?
                        wifiManager?.let {
                            val result = it.scanResults
                        }

                        val intent = Intent(this, WifiActivity::class.java)
                        startActivity(intent)
                    }
                }

                R.id.btn_activity_waterfall -> {
                    val intent = Intent(this, WaterFallActivity::class.java)
                    startActivity(intent)
                }

                R.id.btn_activity_selector -> {
                    val intent = Intent(this, SelectorActivity::class.java)
                    startActivity(intent)
                }

                R.id.btn_activity_volume -> {
                    val intent = Intent(this, VolumeActivity::class.java)
                    startActivity(intent)
                }

                R.id.btn_activity_plugin -> {
                    val intent = Intent()
                    intent.setComponent(ComponentName("com.soul.pluginapp", "com.soul.pluginapp.PluginActivity"))
                    startActivity(intent)
                }

                R.id.btn_activity_mvi_frame -> {
                    val intent = Intent(this, MainMVIActivity::class.java)
                    startActivity(intent)
                }

                else -> {

                }
            }
        }
    }

    private fun createMemoryChurn() {
        for (i in 0..10000) {
            var result: String? = ""
            for (j in 0..99) {
                result += j // 每次+=都会创建新的StringBuilder和String
            }
        }
    }

    private fun badCollectionUsage() {
        val data: MutableList<String> = ArrayList()
        for (i in 0..9999) {
            data.add("item$i")


            // 创建临时List进行筛选
            val filtered: MutableList<String> = ArrayList()
            for (item in data) {
                if (item.contains("5")) {
                    filtered.add(item)
                }
            }
        }
    }

    private fun createRunnableChurn() {
        for (i in 0..999) {
            // 每次循环都创建新Runnable
            Handler().postDelayed({
                // do something
            }, 1000)
        }
    }

    private fun testOOM() {
        val list = mutableListOf<ByteArray>()
        try {
            while (true) {
                // 每次循环分配1MB内存
                list.add(ByteArray(1024 * 1024))
            }
        } catch (e: OutOfMemoryError) {
            // 捕获到OOM，进行后续处理或日志记录
            Log.e("OOM_TEST", "OutOfMemoryError caught!")
        }
    }

    private fun TestANRSleep() {
        Thread.sleep(20 * 1000)
    }

    private fun testANRLock() {
        synchronizedTest2()
    }

    private fun synchronizedTest2() {
        Thread { synchronizedInThread() }.start()
        runOnUiThread { synchronizedInMain() }
    }

    @Synchronized
    fun synchronizedInThread() {
        SystemClock.sleep(30000)
    }

    @Synchronized
    fun synchronizedInMain() {
    }

    private fun testANRService() {
        produceANRByService()
    }

    /**
     * 点击后，启动一个 Service，在 Service 内阻塞主线程
     *
     * 20s 左右会有 ANR 的log，再过 10s 左右有 ANR 的弹框
     */
    private fun produceANRByService(view: View?) {
        val intent = Intent(this, MyService::class.java)
        startService(intent)
    }

    /**
     * 点击后，阻塞主线程，再启动一个 Service
     *
     * 20s 左右会有 ANR 的log，再过 10s 左右有 ANR 的弹框
     */
    private fun produceANRByService() {
        Thread {
            SystemClock.sleep(3000)
            val intent = Intent(this, MyService::class.java)
            startService(intent)
        }.start()

        sleepTest()
    }

    private fun sleepTest() {
        SystemClock.sleep(100000)
    }


    private fun accessibilityManagerTest() {
        val accessibilityManager =
            getSystemService(Context.ACCESSIBILITY_SERVICE) as AccessibilityManager
        accessibilityManager.addAccessibilityStateChangeListener { enabled ->
            if (enabled) {
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        val intent = Intent(this, CustomAccessibilityService::class.java)
        stopService(intent)
//        if (isSatisfiedAndroidVersion(Build.VERSION_CODES.R)) {
//            mConnectivityDiagnosticsManager.unregisterConnectivityDiagnosticsCallback(
//                mConnectivityDiagnosticsCallback
//            )
//        }
//        mConnectivityManager.unregisterNetworkCallback(mNetworkCallback)
    }

    fun hasPermissionToReadNetworkStats(): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            return true
        }
        val appOps = getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager
        var mode = 0
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            mode = appOps.unsafeCheckOpRaw(
                AppOpsManager.OPSTR_GET_USAGE_STATS,
                android.os.Process.myUid(),
                packageName
            )
        }
        if (mode == AppOpsManager.MODE_ALLOWED) {
            return true
        }

        requestReadNetworkStats()
        return false
    }

    private fun requestReadNetworkStats() {
        val intent = Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS)
        startActivity(intent)
    }

    private fun isSatisfiedAndroidVersion(version: Int): Boolean = Build.VERSION.SDK_INT >= version

}

class MyService: Service() {

    override fun onCreate() {
        super.onCreate()
        SystemClock.sleep(100000)
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }
}