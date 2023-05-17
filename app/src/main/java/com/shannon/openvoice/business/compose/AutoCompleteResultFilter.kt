package com.shannon.openvoice.business.compose

import android.widget.Filter

/**
 *
 * @Package:        com.shannon.openvoice.business.compose
 * @ClassName:      AutoCompleteResultFilter
 * @Description:     作用描述
 * @Author:         czhen
 * @CreateDate:     2022/8/10 14:04
 */
class AutoCompleteResultFilter(
    private val autocompletionProvider: AutoCompletionProvider,
    private val onResult: (List<AutoCompleteResult>) -> Unit
) :
    Filter() {

    override fun convertResultToString(resultValue: Any?): CharSequence {
        return when (resultValue) {
            is AccountResult -> formatUsername(resultValue)
            is HashtagResult -> formatHashtag(resultValue)
            else -> ""
        }
    }

    override fun performFiltering(constraint: CharSequence?): FilterResults {
        val filterResults = FilterResults()
        constraint?.run {
            val results = autocompletionProvider.search(this.toString())
            filterResults.values = results
            filterResults.count = results.size
        }
        return filterResults
    }

    override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
        if (constraint != null && results != null && results.count > 0) {
            onResult(results.values as List<AutoCompleteResult>)
        } else {
            onResult(emptyList())
        }
    }

    private fun formatUsername(result: AccountResult): String {
        return String.format("@%s", result.account.username)
    }

    private fun formatHashtag(result: HashtagResult): String {
        return String.format("#%s", result.hashTag.name)
    }
}