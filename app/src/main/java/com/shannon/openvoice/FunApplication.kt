package com.shannon.openvoice

import android.app.Activity
import android.app.Application
import android.content.Context
import android.content.res.Configuration
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.ViewModelStoreOwner
import androidx.multidex.MultiDex
import com.blankj.utilcode.util.AppUtils
import com.blankj.utilcode.util.LogUtils
import com.blankj.utilcode.util.Utils
import com.caij.app.startup.Config
import com.caij.app.startup.DGAppStartup
import com.shannon.android.lib.constant.ConfigConstants
import com.shannon.android.lib.constant.ConfigConstants.VARIABLE.globalApplication
import com.shannon.android.lib.constant.ConfigConstants.VARIABLE.localeManager
import com.shannon.android.lib.life.ActivityLifecycleCallback
import com.shannon.openvoice.initializer.*
import com.shannon.android.lib.util.LocaleManager
import com.shannon.android.lib.util.PreferencesUtil
import com.shannon.openvoice.extended.AppViewModel
import io.reactivex.rxjava3.plugins.RxJavaPlugins

/**
 *
 * @ProjectName:    OpenVoice
 * @Package:        com.shannon.openvoice
 * @ClassName:      FunApplication
 * @Description:     Application
 * @Author:         czhen
 * @CreateDate:     2022/7/20 15:24
 */
class FunApplication : Application(), ViewModelStoreOwner, Utils.OnAppStatusChangedListener {

    private lateinit var mAppViewModelStore: ViewModelStore

    companion object {
        private lateinit var instance: Application

        fun getInstance() = instance

        lateinit var appViewModel: AppViewModel
    }

    override fun onCreate() {
        super.onCreate()
        instance = this
        globalApplication = this
        ActivityLifecycleCallback.register(this)
        AppUtils.registerAppStatusChangedListener(this)
        loadAppConfig()
        RxJavaPlugins.setErrorHandler { it.printStackTrace() }
    }

    private fun loadAppConfig() {
        val startupConfig = Config()
        startupConfig.isStrictMode = BuildConfig.DEBUG
        val startupBuilder = DGAppStartup.Builder()
        startupBuilder.add(ThemeTask())
        startupBuilder.add(LibsTask())
        startupBuilder.add(DbTask())
        startupBuilder.add(TimberTask())
        startupBuilder.add(PlayerTask())

        startupBuilder.setConfig(startupConfig)
            .setExecutorService(ThreadManager.getInstance().WORK_EXECUTOR)
            .addTaskListener(MonitorTaskListener("initializerTag", true))
            .create().start().await()

        mAppViewModelStore = ViewModelStore()
        appViewModel = ViewModelProvider(this, getAppFactory()).get(AppViewModel::class.java)
    }

    override fun attachBaseContext(base: Context) {
//        super.attachBaseContext(base)
        MultiDex.install(this)
        PreferencesUtil.initialize(base)
        localeManager = LocaleManager(base)
        super.attachBaseContext(localeManager.setLocale(base))
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        localeManager.setLocale(this)
    }

    override fun getViewModelStore(): ViewModelStore {
        return mAppViewModelStore
    }

    private fun getAppFactory(): ViewModelProvider.Factory {
        return ViewModelProvider.AndroidViewModelFactory.getInstance(this)
    }

    //APP切换到前台
    override fun onForeground(activity: Activity) {
//        LogUtils.i("=-=  onResume APP是否处在前端  ${AppUtils.isAppForeground()}")
        appViewModel.isAPPForegroundHeartBeat(true)
    }

    //APP切换到后台
    override fun onBackground(activity: Activity) {
//        LogUtils.i("=-=  onStop APP是否处在前端  ${AppUtils.isAppForeground()}")
        appViewModel.isAPPForegroundHeartBeat(false)
    }


}