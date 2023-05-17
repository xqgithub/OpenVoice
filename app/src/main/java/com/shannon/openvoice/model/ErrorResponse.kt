package com.shannon.openvoice.model

import com.google.gson.annotations.SerializedName

/**
 *
 * @ProjectName:    OpenVoice
 * @Package:        com.shannon.openvoice.model
 * @ClassName:      ErrorResponse
 * @Description:     java类作用描述
 * @Author:         czhen
 * @CreateDate:     2022/7/27 10:57
 */
data class ErrorResponse(val error: String,@SerializedName("error_description") val errorDescription: String = "")