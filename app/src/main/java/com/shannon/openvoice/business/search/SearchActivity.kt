package com.shannon.openvoice.business.search

import android.content.Context
import android.content.Intent
import android.content.res.ColorStateList
import android.view.LayoutInflater
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.viewpager.widget.ViewPager
import com.google.android.material.tabs.TabLayout
import com.shannon.android.lib.base.activity.KBaseActivity
import com.shannon.android.lib.extended.dp
import com.shannon.android.lib.extended.gone
import com.shannon.android.lib.util.ThemeUtil
import com.shannon.android.lib.util.ToastUtil
import com.shannon.openvoice.FunApplication
import com.shannon.openvoice.R
import com.shannon.openvoice.business.main.mine.account.AccountActivity
import com.shannon.openvoice.business.player.PlayerHelper
import com.shannon.openvoice.databinding.ActivitySearchBinding

/**
 *
 * @ProjectName:    OpenVoice
 * @Package:        com.shannon.openvoice.business.search
 * @ClassName:      SearchActivity
 * @Description:     java类作用描述
 * @Author:         czhen
 * @CreateDate:     2022/7/27 16:19
 */
class SearchActivity : KBaseActivity<ActivitySearchBinding, SearchModel>() {

    //搜索类型 默认是嘟文
    private var searchType: SearchType = SearchType.topics

    //tabitem名称
    private lateinit
    var tab_titles: MutableList<String>
    private var fragments: MutableList<Fragment> = mutableListOf()


    //搜索嘟文
    private lateinit var searchPostes: SearchItemFragment

    //搜索用户
    private lateinit var searchAccount: SearchItemFragment

    //搜索话题
    private lateinit var searchTopics: SearchItemFragment

    //搜索模型
    private lateinit var searchModel: SearchItemFragment
    private var searchCriteria = ""

    override fun onInit() {
        binding.apply {

            toolsbar.apply {
                titleView.text = ""
                FunApplication.appViewModel.setTopViewHeight(
                    this@SearchActivity,
                    clMain,
                    R.id.toolsbar,
                    5.dp
                )
                vDividingLine.gone()
            }

            etSearchContent.apply {
                hint = "${getString(R.string.search)}..."

                //软键盘搜索监听
                setOnEditorActionListener { v, actionId, event ->
                    if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                        val temp = v.text.toString().trim()
                        if(temp!= searchCriteria)
                            PlayerHelper.instance.onPause()
                        searchCriteria = temp
                        when (searchType) {
                            SearchType.postes -> {
//                                ToastUtil.showCenter("开始搜索嘟文")
                                searchPostes.refreshData(searchCriteria)
                            }
                            SearchType.account -> {
                                searchAccount.refreshData(searchCriteria)
                            }
                            SearchType.topics -> {
                                searchTopics.refreshData(searchCriteria)
                            }
                            SearchType.model -> {
//                                ToastUtil.showCenter("开始搜模型")
                                searchModel.refreshData(searchCriteria)
                            }
                        }
                    }
                    false
                }

            }
        }
        initTabLayout()


    }

    /**
     * 初始化TabLayout
     */
    private fun initTabLayout() {
        binding.apply {
            /** 初始化tab **/
            tab_titles = mutableListOf(
                getString(R.string.tab_topics),
                getString(R.string.tab_postes),
                getString(R.string.tab_model),
                getString(R.string.desc_account)
            )

            /** 初始化fragments数据 **/
            searchPostes = SearchItemFragment.newInstance(postes)
            searchAccount = SearchItemFragment.newInstance(account)
            searchTopics = SearchItemFragment.newInstance(topics)
            searchModel = SearchItemFragment.newInstance(model)

            fragments.add(
                searchTopics
            )
            fragments.add(
                searchPostes
            )
            fragments.add(
                searchModel
            )
            fragments.add(
                searchAccount
            )

            /** 初始化viewpager **/
            vpFragmentContainer.apply {
                adapter =
                    AccountActivity.PageAdapter(supportFragmentManager, fragments, tab_titles)
                //预加载多少页
                offscreenPageLimit = 3
                addOnPageChangeListener(onPageChangeListener)
            }

            /** 初始化TabLayout **/
            tlSearchTags.apply {
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
                        this@SearchActivity,
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
                                    this@SearchActivity,
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
                                    this@SearchActivity,
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
                    0 -> {
                        searchType = SearchType.topics
                        searchTopics.refreshData(
                            binding.etSearchContent.text.toString().trim()
                        )
                    }
                    1 -> {
                        searchType = SearchType.postes
//                        ToastUtil.showCenter("开始搜索嘟文")
                        searchPostes.refreshData(binding.etSearchContent.text.toString().trim())
                    }
                    2 -> {
                        searchType = SearchType.model
//                        ToastUtil.showCenter("开始搜模型")
                        searchModel.refreshData(binding.etSearchContent.text.toString().trim())
                    }
                    3 -> {
                        searchType = SearchType.account
                        searchAccount.refreshData(
                            binding.etSearchContent.text.toString().trim()
                        )
                    }
                }
//                LogUtils.iTag(ConfigConstants.CONSTANT.TAG_ALL, "被选择的是页面号码 =-= $position")
            }

            override fun onPageScrollStateChanged(state: Int) {

            }
        }

    /**
     * 自定义TabLayout
     */
    private fun getTabView(position: Int): View {
        val view =
            LayoutInflater.from(this@SearchActivity).inflate(R.layout.item_tablayout_account, null)
        val tv_title_name = view.findViewById<TextView>(R.id.tvTitleName)
        val iv_title_line = view.findViewById<ImageView>(R.id.ivTitleLine)

        tv_title_name.apply {
            text = tab_titles[position]
        }
        iv_title_line.setImageResource(R.drawable.ic_opv_tab_line)

        if (position == 0) {
            tv_title_name.setTextColor(
                ThemeUtil.getColor(
                    this@SearchActivity,
                    android.R.attr.textColorLink
                )
            )
            iv_title_line.visibility = view.visibility
        } else {
            tv_title_name.setTextColor(
                ThemeUtil.getColor(
                    this@SearchActivity,
                    android.R.attr.textColorSecondary
                )
            )
            iv_title_line.visibility = View.INVISIBLE
        }
        return view
    }

    enum class SearchType {
        postes, account, topics, model
    }


    companion object {
        const val searchContent = "searchContent"
        const val postes = "statuses"
        const val account = "accounts"
        const val topics = "hashtags"
        const val model = "voice_models"

        fun newIntent(context: Context) = Intent(context, SearchActivity::class.java)
    }
}