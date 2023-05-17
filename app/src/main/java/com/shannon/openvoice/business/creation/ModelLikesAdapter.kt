package com.shannon.openvoice.business.creation

import com.shannon.android.lib.base.adapter.BaseFunBindingRecyclerViewAdapter
import com.shannon.android.lib.extended.addComma
import com.shannon.android.lib.extended.singleClick
import com.shannon.openvoice.R
import com.shannon.openvoice.business.creation.detail.ModelDetailActivity
import com.shannon.openvoice.business.main.mine.account.AccountManager
import com.shannon.openvoice.databinding.ItemModelLikeBinding
import com.shannon.openvoice.model.Likes
import com.shannon.openvoice.model.VoiceModelResult
import timber.log.Timber

/**
 *
 * @Package:        com.shannon.openvoice.business.creation
 * @ClassName:      ModelLikesAdapter
 * @Description:     作用描述
 * @Author:         czhen
 * @CreateDate:     2022/8/24 11:32
 */
class ModelLikesAdapter(
    private val otherUser: Boolean,
    private val adapterHandler: AdapterHandler
) :
    BaseFunBindingRecyclerViewAdapter<Likes, ItemModelLikeBinding>() {
    private var activatedPosition = -1

    override fun setNewData(data: List<Likes>) {
        super.setNewData(data)
        activatedPosition = data.indexOfFirst { it.activated == true }
    }

    fun setActivatedPosition(position: Int) {
        if (activatedPosition != -1) {
            getItemData(activatedPosition).activated = false
            refreshNotifyItemChanged(activatedPosition)
        }

        if (position != -1) {
            getItemData(position).activated = true
            refreshNotifyItemChanged(position)
            activatedPosition = position
        }
    }

    override fun bindView(binding: ItemModelLikeBinding, bean: Likes, position: Int) {
        binding.run {
            val context = root.context
            likeButton.isSelected = bean.isLiked

            nameView.text = bean.modelName
            operationButton.isEnabled = true
            operationButton.text =
                context.getString(if (bean.isModelOwner) R.string.own else R.string.buy)
            if (otherUser) {
                operationButton.isEnabled = !bean.isModelOwner
            } else if (bean.isModelOwner) {
                val activated = (bean.activated == false)
                operationButton.isEnabled = activated
                operationButton.text =
                    context.getString(if (activated) R.string.button_use else R.string.status_used)
            }
            likeButton.singleClick {
                if (bean.isLiked) adapterHandler.unlike(position) else adapterHandler.like(position)
            }

            operationButton.singleClick {
                if (otherUser) {
                    adapterHandler.buy(position)
                } else if (!bean.isModelOwner) {
                    adapterHandler.buy(position)
                } else if (bean.activated == false) {
                    adapterHandler.activate(position)
                }
            }
            root.singleClick {
                context.startActivity(ModelDetailActivity.newIntent(context, bean.modelId))
            }

            tvPlayStats.text =
                "${addComma("${bean.usageCount}")} ${context.getString(R.string.openvoiceover)}"
        }
    }

    interface AdapterHandler {

        fun buy(position: Int)

        fun activate(position: Int)

        fun like(position: Int)

        fun unlike(position: Int)
    }
}