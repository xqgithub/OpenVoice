package com.shannon.openvoice.business.creation.models

import android.content.Context
import androidx.core.content.ContextCompat
import com.shannon.android.lib.base.adapter.BaseFunBindingRecyclerViewAdapter
import com.shannon.android.lib.extended.gone
import com.shannon.android.lib.extended.holderPosition
import com.shannon.android.lib.extended.singleClick
import com.shannon.android.lib.extended.visible
import com.shannon.openvoice.R
import com.shannon.openvoice.business.creation.detail.ModelDetailActivity
import com.shannon.openvoice.business.player.PlayerHelper
import com.shannon.openvoice.databinding.AdapterOfficalmodelListBinding
import com.shannon.openvoice.model.VoiceModelResult
import me.jingbin.library.adapter.BaseByViewHolder
import timber.log.Timber

/**
 * Date:2022/11/7
 * Time:9:51
 * author:dimple
 * 官方模型列表 适配器
 */
class OfficialModelListAdapter(private val allowOperation: Boolean, val mContext: Context) :
    BaseFunBindingRecyclerViewAdapter<VoiceModelResult, AdapterOfficalmodelListBinding>() {

    //激活该模型
    private var onActivation: (Int) -> Unit = {}

    //播放该模型
    private var onPlay: (Int) -> Unit = {}



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

    override fun bindView(
        holder: BaseByViewHolder<VoiceModelResult>,
        binding: AdapterOfficalmodelListBinding,
        bean: VoiceModelResult,
        position: Int
    ) {
        binding.apply {
            tvModelName.text = bean.name
            tvPlayStats.text = String.format(
                "%,d %s",
                bean.usageCount,
                root.context.getString(R.string.openvoiceover)
            )

            checkView.singleClick {
                onActivation(holder.holderPosition())
            }

            playButton.apply {
                if (allowOperation) this.visible() else this.gone()
                isSelected = bean.isPlaying == true
                singleClick {
                    onPlay(holder.holderPosition())
                }
            }

            val activated = (bean.activated == false)
            checkView.apply {
                if (activated) {
                    text = mContext.getString(R.string.button_use)
                    isEnabled = true
                    setTextColor(ContextCompat.getColor(context, R.color.color_030E0D))
                } else {
                    text = mContext.getString(R.string.status_used)
                    isEnabled = false
                    setTextColor(ContextCompat.getColor(context, R.color.white))
                }
            }

            sclOfficialModel.singleClick {
                if (allowOperation) mContext.startActivity(
                    ModelDetailActivity.newIntent(
                        mContext,
                        bean.id
                    )
                )
            }
        }
    }

    fun setOnActivation(onActivation: (Int) -> Unit) {
        this.onActivation = onActivation
    }

    fun setOnPlay(onPlay: (Int) -> Unit) {
        this.onPlay = onPlay
    }


    override fun bindView(
        binding: AdapterOfficalmodelListBinding,
        bean: VoiceModelResult,
        position: Int
    ) {

    }
}