package com.shannon.android.lib.base.viewmodel

import io.reactivex.rxjava3.disposables.Disposable

/**
 *
 * @ClassName:      ModelEvent
 * @Description:     java类作用描述
 * @Author:         czhen
 */
interface ModelEvent {

    /**
     * 保存订阅的任务以便于在合适的时机取消
     * @param disposable Disposable
     */
    fun saveDisposable(disposable: Disposable)

    /**
     *  显示等待框
     */
    fun showLoading()

    /**
     *  取消等待框
     */
    fun dismissLoading()

    /**
     * 响应错误码
     * @param errorCode Int
     */
    fun responseError(errorCode: Int)

}