package com.shannon.openvoice.business.compose.models

import android.content.Context
import android.text.SpannableString
import androidx.core.content.ContextCompat
import com.shannon.android.lib.base.adapter.BaseFunBindingRecyclerViewAdapter
import com.shannon.android.lib.extended.contentHighlight
import com.shannon.android.lib.extended.singleClick
import com.shannon.android.lib.extended.visibility
import com.shannon.openvoice.R
import com.shannon.openvoice.databinding.FragmentChooseModelItemBinding
import com.shannon.openvoice.model.VoiceModelResult
import timber.log.Timber

/**
 *
 * @ProjectName:    OpenVoice
 * @Package:        com.shannon.openvoice.business.creation
 * @ClassName:      ModelListAdapter
 * @Description:     java类作用描述
 * @Author:         czhen
 * @CreateDate:     2022/7/28 11:14
 */
class ModelsAdapter(
    private val onActivation: (Int) -> Unit
) :
    BaseFunBindingRecyclerViewAdapter<VoiceModelResult, FragmentChooseModelItemBinding>() {

    private var activatedPosition = -1

    override fun setNewData(data: List<VoiceModelResult>) {
        super.setNewData(data)
        activatedPosition = data.indexOfFirst { it.activated == true }
    }

    override fun addData(datas: MutableList<VoiceModelResult>) {
        super.addData(datas)
        activatedPosition = data.indexOfFirst { it.activated == true }
        Timber.d("activatedPosition = $activatedPosition")
    }

    fun setActivatedPosition(position: Int) {
        if (activatedPosition != -1) {
            getItemData(activatedPosition).activated = false
            refreshNotifyItemChanged(activatedPosition)
        }

        getItemData(position).activated = true
        refreshNotifyItemChanged(position)
        activatedPosition = position
    }

    override fun bindView(
        binding: FragmentChooseModelItemBinding,
        bean: VoiceModelResult,
        position: Int
    ) {
        val context = binding.root.context
        binding.run {
            ivModelState.setImageResource(getSourceIconState(bean.modelSource))
            tvModelState.text =
                if (bean.payload.isNullOrEmpty()) getStateText(
                    context,
                    R.string.model_available,
                    R.color.color_52D094
                ) else getStateText(
                    context,
                    R.string.model_selling,
                    R.color.color_00B2FF
                )
            val activated = (bean.activated == false)
            checkView.apply {
                if (activated) {
                    text = context.getString(R.string.button_use)
                    isEnabled = true
                    setTextColor(ContextCompat.getColor(context, R.color.color_030E0D))
                } else {
                    text = context.getString(R.string.status_used)
                    isEnabled = false
                    setTextColor(ContextCompat.getColor(context, R.color.white))
                }
            }
            ivUseLogo.visibility(bean.activated ?: false)
            modelNameView.text = bean.name
            checkView.singleClick { onActivation(position) }
        }
    }

    private fun getStateText(context: Context, textRes: Int, colorRes: Int): SpannableString {
        val text = context.getString(textRes)
        return contentHighlight(
            "${context.getString(R.string.model_state)}${text}",
            arrayOf(text),
            arrayOf(ContextCompat.getColor(context, colorRes))
        )
    }

    companion object {
        fun getSourceIconState(params: String): Int {
            return when (params) {
                "original" -> R.drawable.ic_opv_model_state_original
                "purchase" -> R.drawable.ic_opv_model_state_bought
                "in_progress" -> R.drawable.ic_opv_model_state_generating
                "failed" -> R.drawable.ic_opv_model_state_fail
                else -> R.drawable.ic_opv_model_state_original
            }
        }
    }


}