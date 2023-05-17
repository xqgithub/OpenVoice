package com.shannon.openvoice.business.report

import com.shannon.android.lib.base.viewmodel.BaseViewModel
import com.shannon.openvoice.model.StatusModel
import com.shannon.openvoice.network.apiService
import com.shannon.openvoice.network.convertResponse
import timber.log.Timber

/**
 *
 * @Package:        com.shannon.openvoice.business.report
 * @ClassName:      ReportViewModel
 * @Description:     作用描述
 * @Author:         czhen
 * @CreateDate:     2022/8/23 9:25
 */
class ReportViewModel : BaseViewModel() {
    private var status: StatusModel? = null
    private lateinit var accountId: String
    private lateinit var userName: String
    private val innerData = arrayListOf<StatusModel>()
    private val selectedList = arrayListOf<String>()

    fun onInit(status: StatusModel?, accountId: String, userName: String) {
        this.status = status
        this.accountId = accountId
        this.userName = userName
        status?.let { selectedList.add(it.actionableId) }
    }

    fun accountStatusesReport(isRefresh: Boolean, onResult: (List<StatusModel>) -> Unit) {
        val minId = if (isRefresh && innerData.isNotEmpty()) innerData.first().id else null
        val maxId =
            if (!isRefresh && innerData.isNotEmpty()) innerData.last().id else status?.id
        apiService.accountStatusesReport(
            accountId = accountId,
            maxId = if (isRefresh) null else maxId,
            sinceId = null,
            minId = minId,
            limit = 30,
            excludeReblogs = true
        ).convertResponse()
            .funSubscribeNotLoading {
                val temp = arrayListOf<StatusModel>().apply { addAll(it) }
                if (isRefresh) {
                    innerData.addAll(0, temp)
                } else {
                    status?.let {
                        if (maxId == it.id && !innerData.contains(it)) temp.add(0, it)
                    }
                    innerData.addAll(temp)
                }
                onResult(temp)
            }
    }


    fun report(comment: String, onResult: (String) -> Unit) {
        apiService.report(accountId, selectedList.toList(), comment)
            .convertResponse()
            .funSubscribe {
                onResult(userName)
            }
    }

    fun setStatusChecked(id: String, isChecked: Boolean) {
        if (isChecked) selectedList.add(id) else selectedList.remove(id)
    }

    fun isStatusChecked(id: String): Boolean {
        return selectedList.contains(id)
    }

    fun hasChecked() = selectedList.isNotEmpty()

}