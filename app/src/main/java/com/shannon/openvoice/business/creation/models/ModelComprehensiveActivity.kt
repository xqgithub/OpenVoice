package com.shannon.openvoice.business.creation.models

import android.annotation.SuppressLint
import android.view.KeyEvent
import android.view.View
import android.widget.AdapterView
import android.widget.ListPopupWindow
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import com.google.android.material.shape.RoundedCornerTreatment
import com.google.android.material.shape.ShapeAppearanceModel
import com.shannon.android.lib.base.activity.KBaseActivity
import com.shannon.android.lib.extended.*
import com.shannon.openvoice.FunApplication
import com.shannon.openvoice.R
import com.shannon.openvoice.business.creation.CreationViewModel
import com.shannon.openvoice.business.creation.ModelListFragment
import com.shannon.openvoice.databinding.ActivityModelcomprehensiveBinding
import com.shannon.openvoice.event.UniversalEvent
import com.shannon.openvoice.extended.getDrawableKt
import com.shannon.openvoice.model.OfficalModelLableBean
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

/**
 * Date:2022/11/4
 * Time:10:44
 * author:dimple
 * 模型综合页面，包括 官方模型 和 我的模型
 */
class ModelComprehensiveActivity :
    KBaseActivity<ActivityModelcomprehensiveBinding, CreationViewModel>() {

    private lateinit var officalModelLabelAdapter: OfficalModelLabelAdapter
    private lateinit var listPopupWindow: ListPopupWindow
    private var modelworksway: String = MODELWORKSWAY_SETUP

    companion object {
        const val MODELWORKSWAY = "modelWorksWay"
        const val MODELWORKSWAY_SETUP = "modelWorksWaySetUp"
        const val MODELWORKSWAY_MODIFY = "modelWorksWayModify"

    }


    @SuppressLint("ResourceType")
    override fun onInit() {
        EventBus.getDefault().register(this)

        intent?.getStringExtra(MODELWORKSWAY)?.let {
            modelworksway = it
        }

        binding.apply {
            toolsbar.apply {
                titleView.text = getString(R.string.model)
                FunApplication.appViewModel.setTopViewHeight(
                    this@ModelComprehensiveActivity,
                    clMain,
                    R.id.toolsbar,
                    5.dp
                )
                vDividingLine.gone()

                navigationButton.singleClick {
                    EventBus.getDefault()
                        .post(UniversalEvent(UniversalEvent.closeTheOfficialModelPage, null))
                    finish()
                }
            }
            initLabelData()

            sbLabelOfficeModel.apply {
                if (modelworksway == MODELWORKSWAY_SETUP) {
                    sbLabelOfficeModel.shapeAppearanceModel = ShapeAppearanceModel.builder()
                        .setAllCorners(RoundedCornerTreatment())
                        .setAllCornerSizes(2f.dp)
                        .build()
                }
                isEnabled = false
                singleClick {
                    isEnabled = false
                    sbLabelMyModel.isEnabled = true
//                    sclLabelClassification.visible()
                    replaceFragmentLayout(
                        ModelListFragment.newInstance(
                            ModelListFragment.MODELLIST_MODIFY_OFFICAL)
                    )
                }
            }

            sbLabelMyModel.apply {
                if (modelworksway == MODELWORKSWAY_MODIFY) visible() else gone()
                isEnabled = true
                singleClick {
                    isEnabled = false
                    sbLabelOfficeModel.isEnabled = true
//                    sclLabelClassification.gone()
                    replaceFragmentLayout(ModelListFragment.newInstance(ModelListFragment.MODELLIST_MODIFY_MYMODEL))
                }
            }

//            sclLabelClassification.apply {
//                singleClick {
//                    listPopupWindow.show()
//                }
//            }
        }
    }

    /**
     * 初始化标签集合
     */
    val datas = mutableListOf<OfficalModelLableBean>()

    private fun initLabelData() {
//        viewModel.voiceModelsOfficialCategoires(this@ModelComprehensiveActivity) { beans ->
//            //创建 OfficalModelLableBeans
//            beans.forEach {
//                datas.add(it)
//            }
//            binding.tvLabelName.text = datas[0].name
//            officalModelLabelAdapter =
//                OfficalModelLabelAdapter(this@ModelComprehensiveActivity, datas)
//            listPopupWindow = ListPopupWindow(this@ModelComprehensiveActivity, null).apply {
//                setAdapter(officalModelLabelAdapter)
//                anchorView = binding.sclLabelClassification
//                verticalOffset = -binding.sclLabelClassification.layoutParams.height
//                setOnItemClickListener(this@ModelComprehensiveActivity::onPopupItemClick)
//                setBackgroundDrawable(this@ModelComprehensiveActivity.getDrawableKt(R.drawable.shap_trade_time_popup_background))
//            }
//            initFragmentLayout(datas[0].id)
//        }
        initFragmentLayout()
    }

    /**
     * 标签点击事件
     */
    private fun onPopupItemClick(parent: AdapterView<*>, view: View, position: Int, id: Long) {
        listPopupWindow.dismiss()
        officalModelLabelAdapter.setSelected(position)
        binding.tvLabelName.text = officalModelLabelAdapter.getItem(position).name
        val categoryId = officalModelLabelAdapter.getItem(position).id

        //更新fragment
        replaceFragmentLayout(
            if (modelworksway == MODELWORKSWAY_SETUP) ModelListFragment.newInstance(
                ModelListFragment.MODELLIST_SETUP, categoryId
            ) else ModelListFragment.newInstance(
                ModelListFragment.MODELLIST_MODIFY_OFFICAL,
                categoryId
            )
        )
    }


    /**
     * 初始化Fragment
     */
    private fun initFragmentLayout(categoryId: Int = -1) {
        val transaction = supportFragmentManager.beginTransaction()
        val fragment = if (modelworksway == MODELWORKSWAY_SETUP) ModelListFragment.newInstance(
            ModelListFragment.MODELLIST_SETUP, categoryId
        ) else ModelListFragment.newInstance(ModelListFragment.MODELLIST_MODIFY_OFFICAL, categoryId)
        transaction.add(
            R.id.fragmentContainer,
            fragment
        )
        transaction.commit()
    }

    /**
     * 替换Fragment页面
     */
    private fun replaceFragmentLayout(fragment: Fragment) {
        val transaction = supportFragmentManager.beginTransaction()
        transaction.replace(
            R.id.fragmentContainer,
            fragment
        )
        transaction.commit()
    }


    override fun onKeyDown(keyCode: Int, event: KeyEvent): Boolean {
        when (keyCode) {
            KeyEvent.KEYCODE_BACK -> {
                return true
            }
        }
        return super.onKeyDown(keyCode, event)
    }


    @Subscribe(threadMode = ThreadMode.MAIN)
    fun eventBusCloseModelComprehensiveActivity(event: UniversalEvent) {
        if (event.actionType == UniversalEvent.closeModelComprehensiveActivity) {
            finish()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        datas.clear()
        EventBus.getDefault().unregister(this)
    }

}