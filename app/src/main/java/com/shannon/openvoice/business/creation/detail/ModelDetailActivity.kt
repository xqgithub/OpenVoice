package com.shannon.openvoice.business.creation.detail

import android.content.Context
import android.content.Intent
import android.graphics.Typeface
import android.view.MotionEvent
import android.view.View
import android.widget.TextView
import androidx.core.view.get
import androidx.fragment.app.Fragment
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.shannon.android.lib.base.activity.KBaseActivity
import com.shannon.android.lib.base.adapter.SimpleActivityPageAdapter
import com.shannon.android.lib.extended.*
import com.shannon.android.lib.util.ThemeUtil
import com.shannon.android.lib.util.ToastUtil
import com.shannon.openvoice.R
import com.shannon.openvoice.business.creation.CreationViewModel
import com.shannon.openvoice.business.main.mine.account.AccountActivity
import com.shannon.openvoice.business.main.mine.account.AccountManager
import com.shannon.openvoice.business.pay.SellActivity
import com.shannon.openvoice.business.timeline.TimelineFragment
import com.shannon.openvoice.business.timeline.TimelineViewModel
import com.shannon.openvoice.databinding.ActivityModelDetailBinding
import com.shannon.openvoice.dialog.PaymentDialog
import com.shannon.openvoice.event.UniversalEvent
import com.shannon.openvoice.extended.startActivityIntercept
import com.shannon.openvoice.model.*
import com.shannon.openvoice.util.UnitHelper
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import timber.log.Timber
import kotlin.properties.Delegates

/**
 *
 * @Package:        com.shannon.openvoice.business.creation.detail
 * @ClassName:      ModelDetailActivity
 * @Description:     作用描述
 * @Author:         czhen
 * @CreateDate:     2022/8/25 17:01
 */
class ModelDetailActivity : KBaseActivity<ActivityModelDetailBinding, CreationViewModel>(),
    TabLayoutMediator.TabConfigurationStrategy, TabLayout.OnTabSelectedListener {
    private var modelId by Delegates.notNull<Long>()
    private val tabTitles =
        arrayListOf(
            R.string.details,
            R.string.voiceover,
            R.string.transaction_history,
            R.string.tab_likes
        )
    private var fragments = mutableListOf<Fragment>()
    private val accountId = AccountManager.accountManager.getAccountId()

    override fun onInit() {
        EventBus.getDefault().register(this)
        setTitleText(R.string.model_details)
        modelId = intent.getLongExtra(MODEL_ID, 0L)
        loadFragments()
        binding.run {
            viewPager.offscreenPageLimit = 3
            viewPager.isUserInputEnabled = AccountManager.accountManager.isLogin()
            viewPager.adapter = SimpleActivityPageAdapter(this@ModelDetailActivity, fragments)
            tabLayout.addOnTabSelectedListener(this@ModelDetailActivity)
            TabLayoutMediator(tabLayout, viewPager, this@ModelDetailActivity).attach()

            likeButton.singleClick(this@ModelDetailActivity::likeClick)
            buyButton.singleClick(this@ModelDetailActivity::buyClick)
        }

        viewModel.modelDetail(modelId)
        viewModel.modelDetailLive.observe(this, this::bindViewData)
    }

    private fun bindViewData(data: ModelDetail) {
        binding.run {
            val textColor = if (data.isOfficial) ThemeUtil.getColor(
                context,
                android.R.attr.textColorSecondary
            ) else ThemeUtil.getColor(context, android.R.attr.textColorLink)
            modelNameView.text = data.name.replace("No.", "No.\n")
            priceView.text = data.price
            modelSourceView.text = data.creatorAccount.username
            modelOwnerView.text = data.account.username
            modelSourceView.setTextColor(textColor)
            modelOwnerView.setTextColor(textColor)
            val isDeleted = data.status == "deleted"
            val own = accountId == data.account.id
            likeButton.isSelected = data.isLiked
            buyButton.visibility(!own && !data.isOfficial && !isDeleted && data.allowTransaction())
            likeButton.isEnabled = data.beUsable()
            buyButton.isEnabled = data.beUsable() && data.allowTransaction() && data.legalModel()
            sourceLayout.singleClick { if (!data.isOfficial) toAccount(data.creatorAccount.id) }
            ownerLayout.singleClick { if (!data.isOfficial) toAccount(data.account.id) }
            tradeNumberView.text = UnitHelper.quantityConvert(data.tradingCount)
            usageNumberView.text = UnitHelper.quantityConvert(data.usageCount)
            likeButton.text = UnitHelper.quantityConvert(data.likeCount)
            rankNumberView.text =
                if (data.tradingRank == 0L) "-" else UnitHelper.quantityConvert(data.tradingRank)
            if (own && !isDeleted) {
                buyButton.isEnabled = true
                buyButton.visible()
                if (data.allowTransaction()) {
                    buyButton.text = getString(R.string.cancel)
                } else {
                    buyButton.text = getString(R.string.button_sell)
                }
            }
        }
    }

    private fun likeClick(view: View) {
        startActivityIntercept {
            val data = viewModel.modelDetailLive.value
            data?.run {
                if (isLiked) {
                    viewModel.unlikeVoiceModel(modelId) {
                        viewModel.changeModelDetail(
                            data.copy(
                                isLiked = false,
                                likeCount = likeCount - 1
                            )
                        )
                        EventBus.getDefault().post(EventLikeVoice(modelId, false, hashCode()))
                    }
                } else {
                    viewModel.likeVoiceModel(modelId) {
                        viewModel.changeModelDetail(
                            data.copy(
                                isLiked = true,
                                likeCount = likeCount + 1
                            )
                        )
                        EventBus.getDefault().post(EventLikeVoice(modelId, true, hashCode()))
                    }
                }
            }
        }
    }

    private fun buyClick(view: View) {
        val data = viewModel.modelDetailLive.value
        data?.run {
            startActivityIntercept {
                val own = accountId == data.account.id
                if (own) {
                    if (data.allowTransaction()) {
                        showConfirmDialog(
                            message = getString(R.string.unsell_model),
                            doConfirm = {
                                viewModel.updateVoiceModel(id, payload = "", saleCycle = "") {
                                    viewModel.modelDetailLive.postValue(this.copy(payload = ""))
                                    EventBus.getDefault().post(EventSellModel(id, "", price))
                                }
                            }
                        )
                    } else {
                        startActivity(SellActivity.newIntent(context, id))
                    }
                } else {
                    viewModel.modelDetail(id) {
                        if (it.status == "deleted") {
                            viewModel.modelDetailLive.postValue(it)
                            ToastUtil.showCenter(getString(R.string.tips_deleted_model))
                        } else if (it.payload.isNullOrEmpty()) {
                            viewModel.modelDetailLive.postValue(it)
                            ToastUtil.showCenter(getString(R.string.tips_unsell_model))
                        } else {
                            PaymentDialog(context, this).show(
                                supportFragmentManager,
                                this::class.simpleName
                            )
                        }
                    }
                }
            }
        }

    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun subscribePurchasedModel(event: EventPurchasedModel) {
        val data = viewModel.modelDetailLive.value
        data?.let {
            val account = AccountManager.accountManager.getAccountData()
            viewModel.changeModelDetail(
                it.copy(
                    account = it.account.copy(
                        id = account.id,
                        displayName = account.name,
                        username = account.username,
                        avatar = account.avatar,
                    ),
                    payload = ""
                )
            )
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun subscribeSignEvent(event: EventSign) {
        if (event.modelId != modelId) return
        val data = viewModel.modelDetailLive.value ?: return
        viewModel.changeModelDetail(data.copy(payload = event.sign))
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun subscribeEvent(event: EventSellModel) {
        if (event.modelId != modelId) return
        val data = viewModel.modelDetailLive.value ?: return
        viewModel.changeModelDetail(data.copy(price = event.price, payload = event.sign))
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun subscribePaymentEvent(event: UniversalEvent) {
        if (event.actionType != UniversalEvent.payment) return
        val detail = event.message as ModelDetail
        if (detail.id != modelId) return
        viewModel.changeModelDetail(detail)
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun subscribePurchasedModel(event: ModelDetail) {
        viewModel.modelDetailLive.postValue(event)
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun eventBusLogin(event: UniversalEvent) {
        if (event.actionType == UniversalEvent.loginSuccess) {
            binding.viewPager.isUserInputEnabled = true
        }
    }

    private fun loadFragments() {
        fragments.addArray(
            ModelInfoFragment.newInstance(modelId),
            TimelineFragment.newInstanceWithModel(TimelineViewModel.Kind.MODEL_ASS, modelId),
            TransactionRecordFragment.newInstance(modelId),
            LikeRecordFragment.newInstance(modelId)
        )
    }

    private fun toAccount(id: String) {
        startActivity(AccountActivity.newIntent(context, id))
    }


    override fun onConfigureTab(tab: TabLayout.Tab, position: Int) {
        tab.text = getString(tabTitles[position])
        tab.view.setOnTouchListener { v, event ->
            if (event.action == MotionEvent.ACTION_UP) {
                if (position <= 1 || AccountManager.accountManager.isLogin()) v.performClick()

                return@setOnTouchListener startActivityIntercept(position <= 1) { }
            }
            return@setOnTouchListener true
        }
    }

    override fun onTabSelected(tab: TabLayout.Tab) {
        Timber.d("onTabSelected-----------------------------")
        val index = (tab.view.childCount - 1).coerceAtLeast(0)
        val childView = tab.view[index]
        if (childView is TextView) {
            childView.typeface = Typeface.DEFAULT_BOLD
        }

    }

    override fun onTabUnselected(tab: TabLayout.Tab) {
        val index = (tab.view.childCount - 1).coerceAtLeast(0)
        val childView = tab.view[index]
        if (childView is TextView) {
            childView.typeface = Typeface.DEFAULT
        }
    }

    override fun onTabReselected(tab: TabLayout.Tab) {
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun subscribeLikeEvent(event: EventLikeVoice) {
        if (event.hashCode == hashCode() || event.modelId != modelId) return
        binding.likeButton.isSelected = event.isLike
    }

    override fun onDestroy() {
        super.onDestroy()
        EventBus.getDefault().unregister(this)
    }

    companion object {
        private const val MODEL_ID = "modelId"

        fun newIntent(context: Context, modelId: Long) =
            Intent(context, ModelDetailActivity::class.java).apply {
                putExtra(MODEL_ID, modelId)
            }
    }
}