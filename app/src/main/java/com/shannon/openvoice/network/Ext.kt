package com.shannon.openvoice.network

import com.google.gson.Gson
import com.shannon.android.lib.exception.XException
import com.shannon.android.lib.extended.isBlankPlus
import com.shannon.openvoice.model.BaseResponse
import com.shannon.openvoice.model.ErrorResponse
import com.shannon.openvoice.util.HttpHeaderLink
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.schedulers.Schedulers
import retrofit2.Response
import timber.log.Timber
import kotlin.jvm.Throws

/**
 *
 * @ProjectName:    OpenVoice
 * @Package:        com.shannon.openvoice.network
 * @ClassName:      Ext
 * @Description:     java类作用描述
 * @Author:         czhen
 * @CreateDate:     2022/7/26 14:12
 */

inline fun <reified T> Observable<BaseResponse<T>>.convert2Code(): Observable<Int> {
    return subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread())
        .map {
            Timber.d("errorCode = ${it.code}")
            if (it.code != 200) {
                throw XException(it.code, it.msg)
            }
            return@map it.code
        }
}


inline fun <reified T : Any> Observable<BaseResponse<T>>.convert(): Observable<T> {
    return subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread())
        .map {
            if (it.code != 200 || it.data == null) {
                throw XException(it.code, it.msg)
            }
            Timber.d("errorCode = ${it.code}; data = ${it.data}")
            return@map it.data
        }
}

val gson = Gson()
inline fun <reified T : Any> Observable<Response<T>>.convertResponseCode(): Observable<Int> {
    return subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread())
        .map {
            it.convertError()
            return@map it.raw().code
        }
}

inline fun <reified T : Any> Observable<Response<T>>.convertLink(crossinline onResult: (String?) -> Unit): Observable<Response<T>> {
    return subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread())
        .map {
            it.convertError()
            val linkHeader = it.headers()["Link"]
            val link = HttpHeaderLink.parse(linkHeader)
            val next = HttpHeaderLink.findByRelationType(link, "next")
            onResult(next?.uri?.getQueryParameter("max_id"))
            return@map it
        }
}

inline fun <reified T : Any> Observable<Response<T>>.convertResponse(): Observable<T> {
    return subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread())
        .map {
            it.convertError()
            return@map it.body()!!
        }
}

@Throws(XException::class)
inline fun <reified T : Any> Response<T>.convertError() {
    val code = raw().code
    val errorBody = errorBody()?.string() ?: ""
    Timber.d("errorCode = $code ; errorBody = $errorBody")
    if (!isSuccessful || body() == null || errorBody.isNotEmpty()) {
        val errorMessage = if (errorBody.isNotEmpty()) {
            val errorResponse = gson.fromJson(errorBody, ErrorResponse::class.java)
            val errorDescription = errorResponse.errorDescription
            val error = errorResponse.error
            if (isBlankPlus(errorDescription)) error else errorDescription
        } else {
            raw().message
        }
        throw XException(code, errorMessage)
    }
}


val apiService: ApiService by lazy { NetworkModule.instance.api() }

val mediaUploadApi: MediaUploadApi by lazy { NetworkModule.instance.mediaUploadApi() }