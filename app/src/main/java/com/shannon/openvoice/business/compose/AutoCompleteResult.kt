package com.shannon.openvoice.business.compose

import com.shannon.openvoice.model.HashTag
import com.shannon.openvoice.model.TimelineAccount

/**
 *
 * @Package:        com.shannon.openvoice.business.compose
 * @ClassName:      AutoCompleteResult
 * @Description:     作用描述
 * @Author:         czhen
 * @CreateDate:     2022/8/10 11:34
 */

abstract class AutoCompleteResult {

}

data class AccountResult(val account: TimelineAccount) : AutoCompleteResult()

data class HashtagResult(val hashTag: HashTag) : AutoCompleteResult()


