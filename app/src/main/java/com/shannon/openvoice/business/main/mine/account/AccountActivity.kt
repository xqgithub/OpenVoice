package com.shannon.openvoice.business.main.mine.account

import android.content.Context
import android.content.Intent
import android.content.res.ColorStateList
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.viewModels
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.viewpager.widget.ViewPager
import com.blankj.utilcode.util.LogUtils
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.tabs.TabLayout
import com.shannon.android.lib.base.activity.KBaseActivity
import com.shannon.android.lib.constant.ConfigConstants
import com.shannon.android.lib.extended.*
import com.shannon.android.lib.util.ThemeUtil
import com.shannon.android.lib.util.ToastUtil
import com.shannon.openvoice.FunApplication.Companion.appViewModel
import com.shannon.openvoice.R
import com.shannon.openvoice.business.main.mine.FragmentPagerSaveAdapter
import com.shannon.openvoice.business.main.mine.MineSettingActivity
import com.shannon.openvoice.business.main.mine.MineViewModel
import com.shannon.openvoice.business.main.mine.account.AccountManager.Companion.accountManager
import com.shannon.openvoice.business.main.mine.editprofile.EditProfileActivity
import com.shannon.openvoice.business.main.notification.NotificationActivity
import com.shannon.openvoice.business.player.PlayerHelper
import com.shannon.openvoice.business.player.listener.OnDataSourceListener
import com.shannon.openvoice.business.player.listener.OnPlayerViewShownListener
import com.shannon.openvoice.business.report.ReportActivity
import com.shannon.openvoice.business.timeline.TimelineFragment
import com.shannon.openvoice.business.timeline.TimelineViewModel
import com.shannon.openvoice.business.timeline.menu.StatusMenuItem
import com.shannon.openvoice.components.TextAndTextUi
import com.shannon.openvoice.databinding.ActivityAccountBinding
import com.shannon.openvoice.dialog.ActionMenuDialog
import com.shannon.openvoice.event.UniversalEvent
import com.shannon.openvoice.extended.loadAvatar
import com.shannon.openvoice.extended.startActivityIntercept
import com.shannon.openvoice.model.AccountBean
import com.shannon.openvoice.model.EventBlackList
import com.shannon.openvoice.model.EventFollow
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import kotlin.math.abs

/**
 * Date:2022/8/9
 * Time:17:16
 * author:dimple
 * 个人主页（我的/他人）
 */
class AccountActivity : KBaseActivity<ActivityAccountBinding, MineViewModel>(),
    OnPlayerViewShownListener, OnDataSourceListener {


    private lateinit var accountId: String

    //tabitem名称
    private lateinit var tab_titles: MutableList<String>

    private var fragments: MutableList<Fragment> = mutableListOf()

    private val timelineViewModel: TimelineViewModel by viewModels()

    override fun onInit() {
        accountId = intent.getStringExtra(EXTRA_ACCOUNT_ID)!!

        EventBus.getDefault().register(this)

        binding.apply {
            PlayerHelper.instance.addPlayerListener(miniPlayerView)
            miniPlayerView.dataSourceListener = this@AccountActivity
            ibBack.singleClick {
                onBackPressed()
            }

            //设置折叠状态的时候颜色
            collapsing.setContentScrimColor(
                ThemeUtil.getColor(
                    this@AccountActivity,
                    R.attr.navigationBarBackground
                )
            )

            /** 初始化tab **/
            tab_titles = mutableListOf(
                resources.getString(R.string.voiceover),
                resources.getString(R.string.reply),
                resources.getString(R.string.topping),
                resources.getString(R.string.media)
            )

            /** 初始化fragments数据 **/
            fragments.add(
                TimelineFragment.newInstanceWithId(
                    TimelineViewModel.Kind.USER,
                    accountId
                )
            )
            fragments.add(
                TimelineFragment.newInstanceWithId(
                    TimelineViewModel.Kind.USER_WITH_REPLIES,
                    accountId
                )
            )
            fragments.add(
                TimelineFragment.newInstanceWithId(
                    TimelineViewModel.Kind.USER_PINNED,
                    accountId
                )
            )
            fragments.add(
                AccountMediaFragment.newInstance(
                    accountId
                )
            )

            /** 初始化viewpager **/
            vpFragmentContainer.apply {
                adapter =
                    PageAdapter(supportFragmentManager, fragments, tab_titles)
                //预加载多少页
                offscreenPageLimit = 1
                addOnPageChangeListener(onPageChangeListener)
            }

            /** 初始化TabLayout **/
            tlAccountFunctionItem.apply {
                setupWithViewPager(vpFragmentContainer)
                for (i in 0 until vpFragmentContainer.adapter!!.count) {
                    val tab: TabLayout.Tab? = getTabAt(i)
                    tab?.let {
                        it.customView = getTabView(i)
                    }
                }

                //去掉tab点击效果
                tabRippleColor = ColorStateList.valueOf(
                    ContextCompat.getColor(
                        this@AccountActivity,
                        R.color.transparent
                    )
                )
                //关联ViewPager
                addOnTabSelectedListener(
                    TabLayout.ViewPagerOnTabSelectedListener(
                        vpFragmentContainer
                    )
                )
                tabMode = TabLayout.MODE_FIXED
                tabGravity = TabLayout.GRAVITY_FILL

                vpFragmentContainer.addOnPageChangeListener(
                    TabLayout.TabLayoutOnPageChangeListener(
                        this
                    )
                )

                addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
                    override fun onTabSelected(tab: TabLayout.Tab?) {
                        val view = tab?.customView
                        view?.let {
                            val tv_title_name = view.findViewById<TextView>(R.id.tvTitleName)
                            val iv_title_line = view.findViewById<ImageView>(R.id.ivTitleLine)
                            tv_title_name.setTextColor(
                                ThemeUtil.getColor(
                                    this@AccountActivity,
                                    android.R.attr.textColorLink
                                )
                            )
                            iv_title_line.visibility = view.visibility
                        }
                    }

                    override fun onTabUnselected(tab: TabLayout.Tab?) {
                        val view = tab?.customView
                        view?.let {
                            val tv_title_name = view.findViewById<TextView>(R.id.tvTitleName)
                            val iv_title_line = view.findViewById<ImageView>(R.id.ivTitleLine)
                            tv_title_name.setTextColor(
                                ThemeUtil.getColor(
                                    this@AccountActivity,
                                    android.R.attr.textColorSecondary
                                )
                            )
                            iv_title_line.visibility = View.INVISIBLE
                        }
                    }

                    override fun onTabReselected(tab: TabLayout.Tab?) {
                    }
                })
            }

            /** AppBarLayout 滑动监听 **/
            accountAppBarLayout.addOnOffsetChangedListener(AppBarLayout.OnOffsetChangedListener { appBarLayout, verticalOffset ->
                val offset = abs(verticalOffset)
                when {
                    offset < appBarLayout.totalScrollRange.div(2) -> {
                        llUserInfo.gone()
                    }
                    offset == appBarLayout.totalScrollRange -> {
                        llUserInfo.visible()
                    }
                }
            })

        }

        dataEcho()
        viewModel.getAccountData(accountId, this)
    }


    /**
     * 数据回显
     */
    private fun dataEcho() {
        viewModel.accountLive.observe(this) { bean ->
            initUIData(bean)
            if (bean.suspended) {
                showDialogOnlyConfirmWithIcon(
                    getString(R.string.tips_account_cancellation),
                    R.drawable.ic_opv_warning
                ) {
                    onBackPressed()
                }
            } else {
                if (!accountManager.accountIsMe(bean.id) && accountManager.isLogin()) {
                    viewModel.relationships(listOf(bean.id), this@AccountActivity)
                }
            }

        }
        viewModel.relationshipsLive.observe(this) { beans ->
            val bean = beans[0]
            if (bean.followedBy) binding.stFollowLogo.visible() else binding.stFollowLogo.invisible()

            binding.sbEditAndFollow.apply {
                text =
                    if (bean.following) getString(R.string.button_unfollow) else getString(R.string.follow)
                background = ContextCompat.getDrawable(
                    this@AccountActivity,
                    if (bean.following) R.drawable.shape_fourth_background2 else R.drawable.shape_fourth_background
                )

                setTextColor(
                    ContextCompat.getColor(
                        this@AccountActivity,
                        if (bean.following) R.color.white else R.color.color_030E0D
                    )
                )

                visible()
            }
        }
        viewModel.followAccountLive.observe(this) { bean ->
            viewModel.getAccountData(bean.id, this)
        }

        viewModel.unfollowAccountLive.observe(this) { bean ->
            viewModel.getAccountData(bean.id, this)
        }
    }

    /**
     * 初始化数据
     */
    private fun initUIData(bean: AccountBean) {
        binding.apply {
            sivUserAvatar.loadAvatar(bean.avatar)
            sivUserBackgroundImage.loadAvatar(
                bean.header,
                R.drawable.opv_mine_default_user_bg,
                R.drawable.opv_mine_default_user_bg
            )

            ivMore.apply {
                if (accountManager.accountIsMe(accountId)) this.gone() else this.visible()
                singleClick {
                    startActivityIntercept {
//                        with(AccountMorePopView(this@AccountActivity)) {
//                            XPopup.Builder(this@AccountActivity)
//                                .isDestroyOnDismiss(true)
//                                .popupAnimation(PopupAnimation.ScrollAlphaFromRightTop)
//                                .atView(ivMore)
//                                .asCustom(this)
//                                .show()
//                            AddBlacklist {
//                                showConfirmDialog(
//                                    message = getString(
//                                        R.string.remind_black_list,
//                                        bean.username
//                                    )
//                                ) {
//                                    timelineViewModel.blockAccount(accountId) {
//                                        ToastUtil.showCenter(getString(R.string.tips_succ_blocked))
//                                        EventBus.getDefault().post(EventBlackList(accountId))
//                                        dismiss()
//                                    }
//                                }
//                            }
//                            report {
//                                startActivity(
//                                    ReportActivity.newIntent(
//                                        this@AccountActivity,
//                                        accountId,
//                                        bean.username
//                                    )
//                                )
//                                dismiss()
//                            }
//                        }

                        val itemList = arrayListOf<StatusMenuItem>()
                        itemList.add(StatusMenuItem.report())
                        itemList.add(StatusMenuItem.blackList())
                        ActionMenuDialog(this@AccountActivity, itemList) { item ->
                            when (item.type) {
                                StatusMenuItem.Type.REPORT -> {
                                    startActivity(
                                        ReportActivity.newIntent(
                                            this@AccountActivity,
                                            accountId,
                                            bean.username
                                        )
                                    )
                                }
                                StatusMenuItem.Type.BLACK_LIST -> {
                                    timelineViewModel.blockAccount(accountId) {
                                        ToastUtil.showCenter(getString(R.string.tips_succ_blocked))
                                        EventBus.getDefault().post(EventBlackList(accountId))
                                    }
                                }
                            }
                        }.show()
                    }
                }
            }

            ivMineSetting.apply {
                if (accountManager.accountIsMe(accountId)) this.visible() else this.gone()
                singleClick {
                    intentToJump(this@AccountActivity, MineSettingActivity::class.java)
                }
            }

            ivMineNotification.apply {
                if (accountManager.accountIsMe(accountId)) this.visible() else this.gone()
                singleClick {
                    intentToJump(this@AccountActivity, NotificationActivity::class.java)
                }
            }


            sbEditAndFollow.apply {
                if (accountManager.accountIsMe(bean.id)) {
                    text = resources.getString(R.string.edit)
                    background = ContextCompat.getDrawable(
                        this@AccountActivity,
                        R.drawable.shape_primary_background3
                    )
                    setTextColor(
                        ContextCompat.getColor(
                            this@AccountActivity,
                            R.color.white
                        )
                    )
                }
                visible()
                singleClick {
                    startActivityIntercept {
                        if (accountManager.accountIsMe(bean.id)) {
                            intentToJump(this@AccountActivity, EditProfileActivity::class.java)
                        } else {
                            if (text.toString() == getString(R.string.button_unfollow)) {
                                showConfirmDialog(
                                    message = getString(R.string.desc_sure_unfollow)
                                ) {
                                    viewModel.unfollowAccount(bean.id, this@AccountActivity)
                                }
                            } else {
                                viewModel.followAccount(bean.id, owner = this@AccountActivity)
                            }
                        }
                    }
                }
            }

            tvProfileinFormation.apply {
                if (!isBlankPlus(bean.note)) {
                    visible()
                    text = bean.note.parseAsMastodonHtml()
                } else {
                    gone()
                }
            }
            tvNickname.text = bean.name
            tvUserAccount.text = "@${bean.username}"
            tvNickname2.text = bean.name
            tvUserAccount2.text = "@${bean.username}"



            ttDuWen.apply {
                setUIFormatType(TextAndTextUi.TextAndTextType.top_bottom)
                setTextOneData(
                    content = resources.getString(R.string.voiceover),
                    _textColorInt = ThemeUtil.getColor(
                        this@AccountActivity,
                        android.R.attr.textColorSecondary
                    )
                )
                setTextData(
                    content = appViewModel.dataShowingConversions(bean.statusesCount),
                    _textSize = 18f,
                    _textColorInt = ThemeUtil.getColor(
                        this@AccountActivity,
                        R.attr.textColorPrimary
                    )
                )
            }

            ttFollowing.apply {
                setUIFormatType(TextAndTextUi.TextAndTextType.top_bottom)
                setTextOneData(
                    content = resources.getString(R.string.following),
                    _textColorInt = ThemeUtil.getColor(
                        this@AccountActivity,
                        android.R.attr.textColorSecondary
                    )
                )
                setTextData(
                    content = appViewModel.dataShowingConversions(bean.followingCount),
                    _textSize = 18f,
                    _textColorInt = ThemeUtil.getColor(
                        this@AccountActivity,
                        R.attr.textColorPrimary
                    )
                )

                singleClick {
                    startActivityIntercept {
                        intentToJump(this@AccountActivity, FollowingAndFollowerActivity::class.java,
                            bundle = Bundle().apply {
                                putSerializable(
                                    ConfigConstants.CONSTANT.userIdentitType,
                                    FollowingAndFollowerActivity.UserIdentitType.Following
                                )
                                putString("accountId", bean.id)
                            }
                        )
                    }
                }
            }


            ttFollowers.apply {
                setUIFormatType(TextAndTextUi.TextAndTextType.top_bottom)
                setTextOneData(
                    content = resources.getString(R.string.followers),
                    _textColorInt = ThemeUtil.getColor(
                        this@AccountActivity,
                        android.R.attr.textColorSecondary
                    )
                )
                setTextData(
                    content = appViewModel.dataShowingConversions(bean.followersCount),
                    _textSize = 18f,
                    _textColorInt = ThemeUtil.getColor(
                        this@AccountActivity,
                        R.attr.textColorPrimary
                    )
                )

                singleClick {
                    startActivityIntercept {
                        intentToJump(this@AccountActivity, FollowingAndFollowerActivity::class.java,
                            bundle = Bundle().apply {
                                putSerializable(
                                    ConfigConstants.CONSTANT.userIdentitType,
                                    FollowingAndFollowerActivity.UserIdentitType.Follower
                                )
                                putString("accountId", bean.id)
                            }
                        )
                    }
                }
            }

            ttModels.apply {
                setUIFormatType(TextAndTextUi.TextAndTextType.top_bottom)
                setTextOneData(
                    content = resources.getString(R.string.model),
                    _textColorInt = ThemeUtil.getColor(
                        this@AccountActivity,
                        android.R.attr.textColorSecondary
                    )
                )
                setTextData(
                    content = appViewModel.dataShowingConversions(bean.voiceModelCount),
                    _textSize = 18f,
                    _textColorInt = ThemeUtil.getColor(
                        this@AccountActivity,
                        R.attr.textColorPrimary
                    )
                )

                singleClick {
                    startActivityIntercept {
                        intentToJump(
                            this@AccountActivity,
                            ModelListActivity::class.java,
                            bundle = Bundle().apply {
                                putString("accountId", accountId)
                            })
                    }
                }
            }
        }
    }


    /**
     * 个人中心功能项item 配合ViewPager的Fragment的适配器
     */
    class PageAdapter(
        fm: FragmentManager,
        private val mFragment: MutableList<Fragment>,
        private val titles: MutableList<String>
    ) : FragmentPagerSaveAdapter(fm) {
        override fun getCount(): Int {
            return mFragment.size
        }

        override fun getItem(position: Int): Fragment {
            return mFragment[position]
        }

        override fun getPageTitle(position: Int): CharSequence {
            return titles[position]
        }
    }

    /**
     * ViewPager 监听
     */
    private var onPageChangeListener: ViewPager.OnPageChangeListener =
        object : ViewPager.OnPageChangeListener {

            override fun onPageScrolled(
                position: Int,
                positionOffset: Float,
                positionOffsetPixels: Int
            ) {

            }

            /**
             *  当currentItem 初始化为0 的时候  是不会走该方法的，其余的页码是会走的
             */
            override fun onPageSelected(position: Int) {
                when (position) {
                }
                LogUtils.iTag(ConfigConstants.CONSTANT.TAG_ALL, "被选择的是页面号码：${position}")
            }

            override fun onPageScrollStateChanged(state: Int) {

            }
        }


    /**
     * 自定义TabLayout
     */
    private fun getTabView(position: Int): View {
        val view =
            LayoutInflater.from(this@AccountActivity).inflate(R.layout.item_tablayout_account, null)
        val tv_title_name = view.findViewById<TextView>(R.id.tvTitleName)
        val iv_title_line = view.findViewById<ImageView>(R.id.ivTitleLine)

        tv_title_name.apply {
            text = tab_titles[position]
        }
        iv_title_line.setImageResource(R.drawable.ic_opv_tab_line)

        if (position == 0) {
            tv_title_name.setTextColor(
                ThemeUtil.getColor(
                    this@AccountActivity,
                    android.R.attr.textColorLink
                )
            )
            iv_title_line.visibility = view.visibility
        } else {
            tv_title_name.setTextColor(
                ThemeUtil.getColor(
                    this@AccountActivity,
                    android.R.attr.textColorSecondary
                )
            )
            iv_title_line.visibility = View.INVISIBLE
        }
        return view
    }

    override fun onDestroy() {
        super.onDestroy()
        EventBus.getDefault().unregister(this)
        PlayerHelper.instance.removePlayerListener(binding.miniPlayerView)
    }

    /**
     * 接收修改个人信息数据通知
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    fun eventBusAccountBean(event: AccountBean) {
        binding.apply {
            sivUserAvatar.loadAvatar(event.avatar)
            sivUserBackgroundImage.loadAvatar(
                event.header, R.drawable.opv_mine_default_user_bg,
                R.drawable.opv_mine_default_user_bg
            )

            tvProfileinFormation.apply {
                if (!isBlankPlus(event.note)) {
                    visible()
                    text = event.note.parseAsMastodonHtml()
                } else {
                    gone()
                }
            }
            tvNickname.text = event.name
            tvUserAccount.text = "@${event.username}"
        }
    }

    /**
     * 接收加入黑名单信息数据通知
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    fun eventBusBlackList(event: EventBlackList) {
        if (accountId == event.accountId) {
            finish()
        }

    }

    /**
     * 接收登录成功信息数据
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    fun eventBusLogin(event: UniversalEvent) {
        if (event.actionType == UniversalEvent.loginSuccess) {
            viewModel.getAccountData(accountId, this)
        }
    }


    /**
     * 关注/取消关注
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    fun subscribeStatus(event: EventFollow) {
        binding.sbEditAndFollow.apply {
            text =
                if (event.following) getString(R.string.button_unfollow) else getString(R.string.follow)
            background = ContextCompat.getDrawable(
                this@AccountActivity,
                if (event.following) R.drawable.shape_fourth_background2 else R.drawable.shape_fourth_background
            )

            setTextColor(
                ContextCompat.getColor(
                    this@AccountActivity,
                    if (event.following) R.color.white else R.color.color_030E0D
                )
            )
        }
    }

    override fun handleResponseError(errorCode: Int) {
        if (errorCode == 401 || errorCode == 403) {
            appViewModel.jump2MainPage(this@AccountActivity)
        }
    }

    companion object {
        private const val EXTRA_ACCOUNT_ID = "accountId"

        fun newIntent(context: Context, accountId: String) =
            Intent(context, AccountActivity::class.java).apply {
                putExtra(EXTRA_ACCOUNT_ID, accountId)
            }
    }

    override fun onShown(show: Boolean) {
        binding.miniPlayerView.visibility(show)
    }

    override fun bindPlaylist(type: Int): Boolean {
        val fragment = fragments[binding.vpFragmentContainer.currentItem]
        return if (fragment is OnDataSourceListener) {
            fragment.bindPlaylist(type)
        } else {
            ToastUtil.showCenter(getString(R.string.tips_no_content))
            false
        }
    }
}