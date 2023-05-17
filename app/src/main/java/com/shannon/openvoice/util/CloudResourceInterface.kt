package com.shannon.openvoice.util

/**
 *
 * @Package:        com.shannon.openvoice.util
 * @ClassName:      CloudResourceInterface
 * @Description:     作用描述
 * @Author:         czhen
 * @CreateDate:     2022/10/11 19:33
 */
interface CloudResourceInterface {

    fun getResourceUrl(key: String): String
}