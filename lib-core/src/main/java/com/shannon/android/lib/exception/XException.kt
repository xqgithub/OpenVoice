package com.shannon.android.lib.exception

/**
 *
 * @ClassName:      NettServerException
 * @Description:     java类作用描述
 * @Author:         czhen
 */
class XException(val errorCode: Int, val errorMessage: String) : Exception() {

    override fun toString(): String {
        return super.toString().plus(" errorCode: $errorCode; errorMessage: $errorMessage")
    }
}