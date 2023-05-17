package com.shannon.openvoice.business.main.mine.account

import com.google.gson.Gson
import com.shannon.android.lib.extended.isBlankPlus
import com.shannon.android.lib.extended.isNotNull
import com.shannon.android.lib.util.PreferencesUtil
import com.shannon.android.lib.util.PreferencesUtil.Constant.ACCOUNT_DATA
import com.shannon.openvoice.model.AccountBean

/**
 * Date:2022/8/17
 * Time:14:23
 * author:dimple
 * 用户管理类
 */
class AccountManager {

    companion object {
        val accountManager: AccountManager by
        lazy(mode = LazyThreadSafetyMode.SYNCHRONIZED) {
            AccountManager()
        }
    }

    /**
     * 更新用户中的Token
     */
    fun upDateToken(accessToken: String) {
        saveAccountData(AccountBean(accessToken = accessToken))
    }

    /**
     * 更新用户数据
     */
    fun updateAccount(accountBean: AccountBean) {
        val resultaccount = getAccountData().let {
            it.id = accountBean.id
            it.username = accountBean.username
            it.note = accountBean.note
            it.url = accountBean.url
            it.avatar = accountBean.avatar
            it.header = accountBean.header
            it.locked = accountBean.locked
            it.followersCount = accountBean.followersCount
            it.followingCount = accountBean.followingCount
            it.statusesCount = accountBean.statusesCount
            it.source = accountBean.source
            it.bot = accountBean.bot
            it.emojis = accountBean.emojis
            it.fields = accountBean.fields
            it.moved = accountBean.moved
            it.voiceModelCount = accountBean.voiceModelCount
            it.birthday = accountBean.birthday
            it.displayName = accountBean.displayName
            it.email = accountBean.email
            it.suspended = accountBean.suspended
            it.profileCompleted = accountBean.profileCompleted
            it.provider = accountBean.provider
            it.walletAddress = accountBean.walletAddress
            it
        }
        saveAccountData(resultaccount)
    }

    /**
     * 判断用户是否登录
     */
    fun isLogin(): Boolean {
        if (isBlankPlus(getAccountData(), getAccountData().accessToken)) return false
        return true
    }

    /**
     * 保存用户数据
     */
    private fun saveAccountData(accountBean: AccountBean?) {
        accountBean.isNotNull({
            PreferencesUtil.putString(ACCOUNT_DATA, Gson().toJson(it))
        }, {
            PreferencesUtil.putString(ACCOUNT_DATA, "")
        })
    }

    /**
     * 获取用户数据
     */
    fun getAccountData(): AccountBean {
        val accountBean = PreferencesUtil.getString(ACCOUNT_DATA, "")
        accountBean.isNotNull({
            return Gson().fromJson(accountBean, AccountBean::class.java)
        }, {
            return AccountBean()
        })
        return AccountBean()
    }

    /**
     * 判断用户是否是本人
     */
    fun accountIsMe(accountId: String): Boolean {
        return getAccountData().id == accountId
    }

    fun getAccountId(): String {
        return getAccountData().id
    }
}