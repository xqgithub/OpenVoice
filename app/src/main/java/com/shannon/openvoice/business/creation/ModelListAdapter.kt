package com.shannon.openvoice.business.creation

import android.content.Context
import android.text.SpannableString
import android.text.TextUtils
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import com.shannon.android.lib.base.adapter.BaseFunBindingRecyclerViewAdapter
import com.shannon.android.lib.extended.*
import com.shannon.openvoice.R
import com.shannon.openvoice.business.creation.detail.ModelDetailActivity
import com.shannon.openvoice.business.player.PlayerHelper
import com.shannon.openvoice.databinding.FragmentCreationItemBinding
import com.shannon.openvoice.model.VoiceModelResult
import me.jingbin.library.adapter.BaseByViewHolder
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
class ModelListAdapter(
    private val allowOperation: Boolean,
    private val onRegistration: (Int) -> Unit = {},
    private val onActivation: (Int) -> Unit
) :
    BaseFunBindingRecyclerViewAdapter<VoiceModelResult, FragmentCreationItemBinding>() {

    private var activatedPosition = -1

    private var onDelete: (Int) -> Unit = {}
    private var onPlay: (Int) -> Unit = {}

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

    fun setOnDeleteAction(onDelete: (Int) -> Unit) {
        this.onDelete = onDelete
    }

    fun setOnPlayAction(onPlay: (Int) -> Unit) {
        this.onPlay = onPlay
    }

    fun playingStateChange(playingModelId: String, playStatus: Boolean) {
        Timber.tag("ModelListFragment").d("playingModelId = $playingModelId")
        try {
            val playingPosition = data.indexOfFirst { it.id.toString() == playingModelId }
            Timber.tag("ModelListFragment").d("playingPosition = $playingPosition")
            if (playingPosition < itemCount) {
                data[playingPosition] =
                    getItemData(playingPosition).copy(isPlaying = playStatus)
                refreshNotifyItemChanged(playingPosition)
            }
        } catch (e: Exception) {
            e.printStackTrace()
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

    override fun bindView(
        holder: BaseByViewHolder<VoiceModelResult>,
        binding: FragmentCreationItemBinding,
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

            checkView.apply {
                isSelected = !bean.payload.isNullOrEmpty()//payload 非空为可出售状态
                text = context.getString(if (isSelected) R.string.cancel else R.string.button_sell)
                setTextColor(
                    ContextCompat.getColor(
                        context,
                        if (isSelected) R.color.white else R.color.color_030E0D
                    )
                )
                singleClick {
                    onRegistration(position)
                }
            }
            ivUseLogo.visibility(bean.activated ?: false)
            modelNameView.text = bean.name
            playButton.gone()
            deleteDirButton.gone()
            if (TextUtils.equals(bean.status, "in_progress")) {
                checkView.invisible()
                tvModelState.text = getStateText(
                    context,
                    R.string.model_generating,
                    R.color.color_FFBA5D
                )
                ivModelState.setImageResource(getSourceIconState(bean.status))

            } else if (TextUtils.equals(bean.status, "failed")) {
                checkView.invisible()
                deleteDirButton.visible()
                tvModelState.text = getStateText(
                    context,
                    R.string.model_fail,
                    R.color.color_FF947B
                )
                ivModelState.setImageResource(getSourceIconState(bean.status))
            } else if (TextUtils.equals(bean.status, "unused") || TextUtils.equals(
                    bean.status,
                    "used"
                )
            ) {
                checkView.visible()
                val activated = (bean.activated == false)
                tvUse.text =
                    if (activated) context.getString(R.string.button_use) else context.getString(R.string.status_used)
                playButton.visibility(allowOperation)
                playButton.isSelected = bean.isPlaying == true
            }else{
                checkView.invisible()
//                deleteDirButton.visible()
            }

            val canDelete = TextUtils.equals(bean.modelType, "user") && TextUtils.equals(
                bean.modelSource,
                "original"
            ) && bean.beUsable() && bean.payload.isNullOrEmpty()
            val beUsable = bean.beUsable()
            deleteButton.visibility(canDelete)
            swipeLayout.isSwipeEnable = allowOperation && (canDelete || beUsable)
            tvUse.visibility(beUsable)
            tvUse.singleClick {
                if (bean.activated == false) {
                    swipeLayout.quickClose()
                    onActivation(holder.holderPosition())
                }
            }
            playButton.singleClick {
                onPlay(holder.holderPosition())
            }
            deleteButton.singleClick {
                swipeLayout.quickClose()
                onDelete(holder.holderPosition())
            }
            deleteDirButton.singleClick { onDelete(holder.holderPosition()) }

            contentLayout.singleClick {
                if (bean.beUsable() && allowOperation) {
                    context.startActivity(ModelDetailActivity.newIntent(context, bean.id))
                } else if (!allowOperation && checkView.isVisible && checkView.isEnabled) {
                    onActivation(holder.holderPosition())
                }

            }
        }
    }

    override fun bindView(
        binding: FragmentCreationItemBinding,
        bean: VoiceModelResult,
        position: Int
    ) {
    }

    companion object {
        fun getSourceBackgroundColor(modelSource: String): Int {
            Timber.d(modelSource)
            return when (modelSource) {
                "official" -> R.color.color_7FF7EC
                "original" -> R.color.color_FFBA5D
                "purchase" -> R.color.color_FF947B
                else -> R.color.color_FFBA5D
            }
        }

        fun getSourceText(modelSource: String): Int {
            return when (modelSource) {
                "official" -> R.string.official
                "original" -> R.string.original
                "purchase" -> R.string.notice_purchased
                else -> R.string.official
            }
        }

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