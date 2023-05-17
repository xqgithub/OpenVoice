package com.shannon.android.lib.base.viewmodel

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.rxjava.rxlife.life
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.disposables.Disposable

/**
 *
 * @ClassName:      BaseViewModel
 * @Description:     java类作用描述
 * @Author:         czhen
 */
abstract class BaseViewModel : ViewModel(), ModelEvent {

    /**
     * 响应错误码
     */
    val responseErrorLiveData = MutableLiveData<Int>()

    /**
     * 弹出等待框
     */
    val showLoadingLiveData = MutableLiveData<Boolean>()

    /**
     * 取消等待框
     */
    val dismissLoadingLiveData = MutableLiveData<Boolean>()

    /**
     * 保存订阅的任务，在ViewModel销毁时如果任务没有执行完毕就给取消掉
     */
    private val disposableList = arrayListOf<Disposable>()

    override fun onCleared() {
        super.onCleared()
        for (disposable in disposableList) {
            if (!disposable.isDisposed) {
                disposable.dispose()
            }
        }
        disposableList.clear()
    }

    /**
     * 绑定观察者
     * @param onNext Function1<T, Unit> 接收到消息的回调
     * @return ViewObserver<T>
     */
    protected fun <T> bindObserver(showLoading: Boolean, onNext: (T) -> Unit) =
        ViewObserver<T>(showLoading, this, onSubscribeNext = onNext)

    /**
     *  绑定观察者
     * @param onNext Function1<T, Unit> 接收到消息的回调
     * @param onError Function1<Throwable, Unit> 任务异常的回调
     * @return ViewObserver<T>
     */
    protected fun <T> bindObserver(
        showLoading: Boolean,
        onNext: (T) -> Unit,
        onError: (Throwable) -> Unit
    ) =
        ViewObserver<T>(showLoading, this, onSubscribeNext = onNext, onSubscribeError = onError)


    protected inline fun <reified T> Observable<T>.funSubscribe(
        showLoading: Boolean = true,
        noinline onNext: (T) -> Unit
    ) {
        subscribe(bindObserver(showLoading, onNext))
    }

    protected inline fun <reified T> Observable<T>.funSubscribe(
        showLoading: Boolean = true,
        noinline onError: (Throwable) -> Unit = {},
        noinline onNext: (T) -> Unit
    ) {
        subscribe(bindObserver(showLoading, onNext, onError))
    }

    protected inline fun <reified T> Observable<T>.funSubscribeRxLife(
        showLoading: Boolean = true,
        owner: LifecycleOwner,
        event: Lifecycle.Event = Lifecycle.Event.ON_DESTROY,
        noinline onError: (Throwable) -> Unit = {},
        noinline onNext: (T) -> Unit
    ) {
        life(owner, event).subscribe(bindObserver(showLoading, onNext, onError))
    }

    protected inline fun <reified T> Observable<T>.funSubscribeNotLoading(
        noinline onError: (Throwable) -> Unit = {},
        noinline onNext: (T) -> Unit
    ) {
        subscribe(bindObserver(false, onNext, onError))
    }


    override fun saveDisposable(disposable: Disposable) {
        disposableList += disposable
    }

    override fun showLoading() {
        showLoadingLiveData.postValue(true)
    }

    override fun dismissLoading() {
        dismissLoadingLiveData.postValue(true)
    }

    override fun responseError(errorCode: Int) {
        responseErrorLiveData.postValue(errorCode)
    }
}