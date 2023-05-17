package com.shannon.openvoice.business.main.trade

import android.content.Context
import androidx.core.content.ContextCompat
import com.shannon.android.lib.base.adapter.BaseFunBindingRecyclerViewAdapter
import com.shannon.android.lib.extended.*
import com.shannon.android.lib.util.ThemeUtil
import com.shannon.openvoice.R
import com.shannon.openvoice.business.creation.detail.ModelDetailActivity
import com.shannon.openvoice.business.main.mine.account.AccountManager
import com.shannon.openvoice.business.player.PlayerHelper
import com.shannon.openvoice.databinding.ItemTradeBinding
import com.shannon.openvoice.model.TradeList
import com.shannon.openvoice.util.UnitHelper
import timber.log.Timber
import kotlin.random.Random

/**
 *
 * @Package:        com.shannon.openvoice.business.main.trade
 * @ClassName:      TradeListAdapter
 * @Description:     作用描述
 * @Author:         czhen
 * @CreateDate:     2022/9/1 10:22
 */
class TradeListAdapter(
    val context: Context,
    val kind: TradeListFragment.Kind,
    val onLike: (Int) -> Unit,
    val onPlay: (Int) -> Unit
) :
    BaseFunBindingRecyclerViewAdapter<TradeList, ItemTradeBinding>() {
    private var playingPosition = -1
    private val colors = mutableListOf<Int>()
    private val random = Random(2)

    init {
        colors.addArray(
            ThemeUtil.getColor(context, android.R.attr.colorPrimary),
            ContextCompat.getColor(context, R.color.color_7997FF),
            ContextCompat.getColor(context, R.color.color_BF8CFF)
        )
    }

    fun playingStateChange(playingModelId: String, playStatus: Boolean) {
        try {
            val playingPosition = data.indexOfFirst { it.id.toString() == playingModelId }
            if (playingPosition < itemCount) {
                data[playingPosition] =
                    getItemData(playingPosition).copy(isPlaying = playStatus)
                refreshNotifyItemChanged(playingPosition)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun getAvatarBackgroundColor(bean: TradeList): Int {
        if (bean.backgroundColor == 0) {
            bean.backgroundColor = colors[random.nextInt(2)]
        }
        return bean.backgroundColor
    }

    override fun bindView(binding: ItemTradeBinding, bean: TradeList, position: Int) {
        binding.run {
            iconView.text = bean.voiceModel.name.replace("No.","No.\n")
            iconView.setBackgroundColor(getAvatarBackgroundColor(bean))
            tradeNumberView.text = UnitHelper.quantityConvert(bean.count)
            likeNumberView.text = UnitHelper.quantityConvert(bean.voiceModel.likeCount)
            iconLikeView.isSelected = bean.voiceModel.isLiked
            priceView.text = bean.voiceModel.price
            statusNumberView.text = String.format(
                "%,d %s",
                bean.count,
                root.context.getString(R.string.openvoiceover)
            )
            if ((bean.voiceModel.price?.length ?: 0) > 7) {
                ownerView.maxEms = 7
            } else {
                ownerView.maxEms = 10
            }
            ownerView.text = root.context.getString(R.string.owner).plus(" ")
                .plus(bean.voiceModel.ownerAccount.username)
            modelNameView.text = bean.voiceModel.name
            playButton.isSelected = bean.isPlaying
            playButton.singleClick {
                playingPosition = position
                onPlay(position)
            }
            root.singleClick {
                it.context.startActivity(
                    ModelDetailActivity.newIntent(
                        it.context,
                        bean.voiceModel.id
                    )
                )
            }
            likeContainer.singleClick {
                onLike(position)
            }
            if (kind == TradeListFragment.Kind.SELL) {
                likeContainer.visible()
                tradeIconView.visible()
                tradeNumberView.visible()
                statusNumberView.gone()
            } else {
                likeContainer.gone()
                tradeIconView.gone()
                tradeNumberView.gone()
                statusNumberView.visible()
            }
        }
    }
}