package com.shannon.openvoice.business.draft

import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import com.shannon.android.lib.base.viewmodel.BaseViewModel
import com.shannon.openvoice.db.DraftModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 *
 * @Package:        com.shannon.openvoice.business.draft
 * @ClassName:      DraftViewModel
 * @Description:     作用描述
 * @Author:         czhen
 * @CreateDate:     2022/8/15 9:25
 */
class DraftViewModel : BaseViewModel() {

    private val draftHelper by lazy { DraftHelper() }


    fun fetchDraftList(): LiveData<List<DraftModel>> {
        return draftHelper.loadDrafts()
    }

    fun deleteDraft(id: Int) {
        viewModelScope.launch(Dispatchers.IO) { draftHelper.deleteDraft(id) }
    }

}