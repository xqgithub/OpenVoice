package com.shannon.openvoice.business.player.ui

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import com.shannon.android.lib.extended.singleClick
import com.shannon.android.lib.extended.visibility
import com.shannon.android.lib.util.ToastUtil
import com.shannon.openvoice.R
import com.shannon.openvoice.business.player.MediaInfo
import com.shannon.openvoice.business.player.PlayerHelper
import com.shannon.openvoice.business.player.listener.OnDataSourceListener
import com.shannon.openvoice.business.player.listener.PlayerListener
import com.shannon.openvoice.databinding.LayoutMiniPlayerBinding
import com.shannon.openvoice.extended.loadAvatar
import com.shannon.openvoice.model.StatusModel

/**
 *
 * @Package:        com.shannon.openvoice.business.player.ui
 * @ClassName:      MiniPlayerView
 * @Description:     作用描述
 * @Author:         czhen
 * @CreateDate:     2023/3/13 9:53
 */
class MiniPlayerView : FrameLayout, PlayerListener {

    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    )

    constructor(
        context: Context,
        attrs: AttributeSet?,
        defStyleAttr: Int,
        defStyleRes: Int
    ) : super(context, attrs, defStyleAttr, defStyleRes)

    private val binding: LayoutMiniPlayerBinding =
        LayoutMiniPlayerBinding.inflate(LayoutInflater.from(context), this, false)

    var dataSourceListener: OnDataSourceListener? = null


    init {
        addView(binding.root)
        singleClick {
            if (canOperate(0)) {
                PlayerDialog(context).show()
            }
        }
        binding.apply {
            playButton.singleClick {
                if (canOperate(0)) {
                    PlayerHelper.instance.play()
                }
            }
            pauseButton.singleClick {
                if (canOperate(0)) {
                    PlayerHelper.instance.onPause()
                }

            }
            nextButton.singleClick {
                if (canOperate(0)) {
                    PlayerHelper.instance.seekToNextItem()
                }
            }
            locationButton.singleClick {
                if (canOperate(1)) {
                    PlayerHelper.instance.navigateTo()
                }

            }
        }
    }

    private fun canOperate(type: Int): Boolean {
        var can = true
        if (PlayerHelper.instance.isPlaylistEmpty()) {
            can = dataSourceListener?.bindPlaylist(type) ?: false
        }
        return can
    }

    override fun onMediaItemTransition(item: MediaInfo) {
        binding.avatarView.loadAvatar(item.avatar)
    }

    override fun onIsPlayingChanged(isPlaying: Boolean) {
        binding.playButton.visibility(!isPlaying, View.INVISIBLE)
        binding.pauseButton.visibility(isPlaying, View.INVISIBLE)
    }

    private fun setViewEnabled(view: View, isEnabled: Boolean) {
        view.isEnabled = isEnabled
        view.alpha = if (isEnabled) 1.0f else 0.5f
    }

    override fun onPlayState(isSkipToPreviousEnabled: Boolean, isSkipToNextEnabled: Boolean) {
        binding.nextButton.isEnabled = isSkipToNextEnabled
        binding.nextButton.alpha = if (isSkipToNextEnabled) 1.0f else 0.5f
    }

    override fun onIsLoading(isLoading: Boolean) {
    }

    override fun onMediaDuration(mediaDuration: Long) {
    }

    override fun onMediaProgress(mediaProgress: Int) {
    }

    override fun playListEmpty() {
        ToastUtil.showCenter(context.getString(R.string.tips_no_content))
    }

    override fun onCleared() {
        binding.avatarView.loadAvatar("")
    }

}