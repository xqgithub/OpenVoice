package com.shannon.android.lib.base.viewmodel

import com.shannon.android.lib.exception.XException
import com.shannon.android.lib.util.ToastUtil
import io.reactivex.rxjava3.core.Observer
import io.reactivex.rxjava3.disposables.Disposable
import timber.log.Timber

/**
 *
 * @ClassName:      ViewObserver
 * @Description:     java类作用描述
 * @Author:         czhen
 */
class ViewObserver<T>(
    private val showLoading: Boolean,
    private val modelEvent: ModelEvent,
    private val onSubscribeNext: (T) -> Unit = {},
    private val onSubscribeError: (Throwable) -> Unit = {}
) : Observer<T> {
    override fun onSubscribe(d: Disposable) {
        Timber.d("onSubscribe.............................")
        modelEvent.saveDisposable(d)
        if (showLoading)
            modelEvent.showLoading()
    }

    override fun onNext(t: T) {
        Timber.d("onNext.............................")
        if (showLoading)
            modelEvent.dismissLoading()
        onSubscribeNext(t)
    }

    override fun onError(e: Throwable) {
        Timber.e(e, "onError.............................")
        if (showLoading)
            modelEvent.dismissLoading()
        if (e is XException) {
            modelEvent.responseError(e.errorCode)
            ToastUtil.showCenter(e.errorMessage)
        } else {
            modelEvent.responseError(500)
        }
        onSubscribeError(e)
    }

    override fun onComplete() {
        Timber.d("onComplete.............................")
    }
}