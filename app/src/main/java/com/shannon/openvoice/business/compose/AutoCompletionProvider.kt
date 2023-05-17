package com.shannon.openvoice.business.compose

/**
 *
 * @Package:        com.shannon.openvoice.business.compose
 * @ClassName:      AutoCompletionProvider
 * @Description:     作用描述
 * @Author:         czhen
 * @CreateDate:     2022/8/10 11:28
 */
interface AutoCompletionProvider {
    fun search(token: String): List<AutoCompleteResult>
}