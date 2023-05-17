package com.shannon.openvoice.initializer

import com.shannon.android.lib.util.CrashHandler
import com.shannon.android.lib.util.ToastUtil
import com.shannon.openvoice.FunApplication
import com.shannon.openvoice.db.AppDatabase

/**
 *
 * @ProjectName:    OpenVoice
 * @Package:        com.shannon.openvoice.initializer
 * @ClassName:      DbTask
 * @Description:     DB
 * @Author:         czhen
 * @CreateDate:     2022/7/20 15:37
 */
class DbTask : FunTask() {

    override fun run() {
        AppDatabase.getDatabase()
    }

    override fun isMustRunMainThread(): Boolean {
        return false
    }


    override fun isWaitOnMainThread(): Boolean {
        return false
    }
}