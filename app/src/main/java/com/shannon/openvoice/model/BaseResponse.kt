package com.shannon.openvoice.model

/**
 *
 * @ProjectName:    OpenVoice
 * @Package:        com.shannon.openvoice.model
 * @ClassName:      BaseResponse
 * @Description:     java类作用描述
 * @Author:         czhen
 * @CreateDate:     2022/7/26 14:08
 */
data class BaseResponse<T>(val code: Int, val msg: String, val data: T?)
