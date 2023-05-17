package com.shannon.openvoice.event

/**
 * Date:2022/9/8
 * Time:15:33
 * author:dimple
 * EventBUG 对象传递
 */
data class UniversalEvent(
    var actionType: Int,
    var message: Any?
) {

    companion object {
        //退出登录的时候，关闭正在播放的声文
        val closeAudioPlayer = 0x001

        //引导页的类型
        val guidePageKind = 0x002

        //模型列表更新官方模型置顶数据
        val officialModelTopData = 0x003

        //关闭官方模型页面
        val closeTheOfficialModelPage = 0x004

        //修改选择的声音模型
        val modifySelectedSoundModel = 0x005

        //关闭ModelComprehensiveActivity页面
        val closeModelComprehensiveActivity = 0x006

        //我的模型官方置顶模型播放按钮状态
        val officalModelPlayButtonState = 0x007

        //登录成功
        val loginSuccess = 0x008

        //退出登录
        val signOut = 0x009

        val payment = 0x010

        //google接收消息 发送通知
        val sendNotification = 0x011
    }
}