package com.shannon.openvoice.business.player.ui

import android.content.Context
import android.os.Build
import android.os.Bundle
import android.text.Spanned
import android.text.TextUtils
import android.text.method.MovementMethod
import android.text.method.ScrollingMovementMethod
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.widget.AdapterView
import android.widget.ListPopupWindow
import android.widget.SeekBar
import com.blankj.utilcode.util.ScreenUtils
import com.shannon.android.lib.base.activity.BottomSheetFixedHeightDialog
import com.shannon.android.lib.extended.*
import com.shannon.android.lib.player.media.MediaController
import com.shannon.android.lib.util.PreferencesUtil
import com.shannon.android.lib.util.ToastUtil
import com.shannon.openvoice.R
import com.shannon.openvoice.business.main.mine.account.AccountActivity
import com.shannon.openvoice.business.main.trade.TimeAdapter
import com.shannon.openvoice.business.player.MediaInfo
import com.shannon.openvoice.business.player.PlayerHelper
import com.shannon.openvoice.business.player.listener.PlayerListener
import com.shannon.openvoice.business.status.StatusListActivity
import com.shannon.openvoice.business.timeline.TimelineViewModel
import com.shannon.openvoice.business.timeline.listener.LinkListener
import com.shannon.openvoice.databinding.DialogPlayerBinding
import com.shannon.openvoice.extended.getDrawableKt
import com.shannon.openvoice.model.HashTag
import com.shannon.openvoice.model.StatusModel
import com.shannon.openvoice.util.looksLikeMastodonUrl
import com.shannon.openvoice.util.openLink
import com.shannon.openvoice.util.setClickableText
import java.util.*

/**
 *
 * @Package:        com.shannon.openvoice.dialog
 * @ClassName:      ActionMenuDialog
 * @Author:         czhen
 */
class PlayerDialog(
    val mContext: Context
) :
    BottomSheetFixedHeightDialog(mContext, R.style.TransparentBottomDialog), PlayerListener,
    LinkListener, SeekBar.OnSeekBarChangeListener {
    private val speedFilterArray = arrayListOf(
        "0.50 X",
        "0.75 X",
        "1.00 X",
        "1.25 X",
        "1.50 X",
        "2.00 X"
    )
    private val speedArray = arrayListOf(
        0.5f,
        0.75f,
        1.00f,
        1.25f,
        1.50f,
        2.00f
    )
    private val binding by inflate<DialogPlayerBinding>()
    private val formatBuilder: StringBuilder
    private val formatter: Formatter
    private var currentPlayMode = MediaController.PLAY_MODE_ORDER
    private var currentSpeed = 1.0f

    private val speedAdapter by lazy { SpeedAdapter(context, speedFilterArray) }
    private val listPopupWindow by lazy {
        ListPopupWindow(context, null).apply {
            setAdapter(speedAdapter)
            anchorView = binding.viewPlayerControllerSpeed
            verticalOffset = 8.dp
            width = 141.dp
            isModal = true
            setOnItemClickListener(this@PlayerDialog::onPopupItemClick)
            setBackgroundDrawable(context.getDrawableKt(R.drawable.shap_speed_popup_background))
        }
    }

    override fun getFixedHeight(): Int {
        return (ScreenUtils.getScreenHeight() * 0.5).toInt()
    }

    init {
        setContentView(binding.root)

        formatBuilder = StringBuilder()
        formatter = Formatter(formatBuilder, Locale.getDefault())
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        PlayerHelper.instance.addPlayerListener(this)
        setPlaybackParams()
        binding.apply {
            viewPlayerControllerPlay.singleClick { PlayerHelper.instance.play() }
            viewPlayerControllerPause.singleClick { PlayerHelper.instance.onPause() }
            viewPlayerControllerPrevious.singleClick { PlayerHelper.instance.seekToPreviousItem() }
            viewPlayerControllerNext.singleClick { PlayerHelper.instance.seekToNextItem() }
            viewPlayerControllerMode.singleClick {
                if (currentPlayMode == MediaController.PLAY_MODE_ORDER) {
                    currentPlayMode = MediaController.PLAY_MODE_SHUFFLE
                    viewPlayerControllerMode.setImageResource(R.drawable.icon_play_mode_shuffle)
                } else {
                    currentPlayMode = MediaController.PLAY_MODE_ORDER
                    viewPlayerControllerMode.setImageResource(R.drawable.icon_play_mode_order)
                }
                PlayerHelper.instance.switchPlayMode(currentPlayMode)
                PreferencesUtil.putInt(PreferencesUtil.Constant.KEY_PLAY_MODE, currentPlayMode)
            }
            viewPlayerControllerSpeed.singleClick { listPopupWindow.show() }
            viewPlayerSeekBar.setOnSeekBarChangeListener(this@PlayerDialog)
        }
        setOnDismissListener {
            PlayerHelper.instance.removePlayerListener(this)
        }
    }

    private fun onPopupItemClick(parent: AdapterView<*>, view: View, position: Int, id: Long) {
        listPopupWindow.dismiss()
        currentSpeed = speedArray[position]
        speedAdapter.setSelected(position)
        PlayerHelper.instance.setPlaybackSpeed(currentSpeed)
        PreferencesUtil.putFloat(PreferencesUtil.Constant.KEY_SPEED, currentSpeed)
        setSpeedDrawable()
    }


    override fun onMediaItemTransition(item: MediaInfo) {
        item.statusModel?.run {
            this.actionableStatus.run {
                setStatusContent(
                    content.parseAsAppHtml(),
                    mentions,
                    tags
                )
            }
        }
    }

    override fun onIsPlayingChanged(isPlaying: Boolean) {
        binding.viewPlayerControllerPlay.visibility(!isPlaying, View.INVISIBLE)
        binding.viewPlayerControllerPause.visibility(isPlaying, View.INVISIBLE)
    }

    override fun onPlayState(isSkipToPreviousEnabled: Boolean, isSkipToNextEnabled: Boolean) {
        setViewEnabled(binding.viewPlayerControllerPrevious, isSkipToPreviousEnabled)
        setViewEnabled(binding.viewPlayerControllerNext, isSkipToNextEnabled)
    }

    private fun setViewEnabled(view: View, isEnabled: Boolean) {
        view.isEnabled = isEnabled
        view.alpha = if (isEnabled) 1.0f else 0.5f
    }


    override fun onIsLoading(isLoading: Boolean) {
        binding.viewPlayerControllerPlay.isEnabled = !isLoading
    }

    override fun onMediaDuration(mediaDuration: Long) {
        binding.viewPlayerSeekBar.max = mediaDuration.toInt()
        binding.viewPlayerTotalTime.text = getStringForTime(mediaDuration)
    }

    override fun onMediaProgress(mediaProgress: Int) {
        binding.viewPlayerCurrentTime.text = getStringForTime(mediaProgress.toLong())
        if (binding.viewPlayerSeekBar.isDragging) return
        binding.viewPlayerSeekBar.progress = mediaProgress
    }


    private fun setStatusContent(
        content: Spanned,
        mentions: List<StatusModel.Mention>?,
        tags: List<HashTag>?
    ) {
        setClickableText(binding.statusContent, content, mentions, tags, this)
        if (TextUtils.isEmpty(binding.statusContent.text)) {
            binding.statusContent.gone()
        } else {
            binding.statusContent.visible()
        }
    }


    private fun getStringForTime(timeMs: Long): String {
        var timeMsVar = timeMs
        val prefix = if (timeMsVar < 0) "-" else ""
        timeMsVar = Math.abs(timeMsVar)
        val totalSeconds = (timeMsVar + 500) / 1000
        val seconds = totalSeconds % 60
        val minutes = totalSeconds / 60 % 60
        val hours = totalSeconds / 3600
        formatBuilder.setLength(0)
        return if (hours > 0) formatter.format("%s%d:%02d:%02d", prefix, hours, minutes, seconds)
            .toString() else formatter.format("%s%02d:%02d", prefix, minutes, seconds).toString()
    }

    private fun setPlaybackParams() {
        currentPlayMode = PreferencesUtil.getInt(
            PreferencesUtil.Constant.KEY_PLAY_MODE,
            MediaController.PLAY_MODE_ORDER
        )
        if (currentPlayMode == MediaController.PLAY_MODE_SHUFFLE) {
            binding.viewPlayerControllerMode.setImageResource(R.drawable.icon_play_mode_shuffle)
        } else {
            binding.viewPlayerControllerMode.setImageResource(R.drawable.icon_play_mode_order)
        }
        currentSpeed = PreferencesUtil.getFloat(PreferencesUtil.Constant.KEY_SPEED, 1.0f)
        setSpeedDrawable()
        speedAdapter.setSelected(setSpeedPosition())
    }

    private fun setSpeedDrawable() {
        val resId = when (currentSpeed) {
            0.5f -> R.drawable.icon_speed_050
            0.75f -> R.drawable.icon_speed_075
            1.0f -> R.drawable.icon_speed_normal
            1.25f -> R.drawable.icon_speed_125
            1.5f -> R.drawable.icon_speed_150
            2.0f -> R.drawable.icon_speed_200
            else -> R.drawable.icon_speed_normal
        }
        binding.viewPlayerControllerSpeed.setImageResource(resId)
    }

    private fun setSpeedPosition(): Int {
        return when (currentSpeed) {
            0.5f -> 0
            0.75f -> 1
            1.0f -> 2
            1.25f -> 3
            1.5f -> 4
            2.0f -> 5
            else -> 1
        }
    }

    override fun onViewTag(tag: String) {
        if (PlayerHelper.instance.decodeTimelineKindValue() == TimelineViewModel.Kind.TAG.ordinal) {
            dismiss()
        } else {
            context.startActivity(
                StatusListActivity.newHashtagIntent(
                    context,
                    tag
                )
            )
        }

    }

    override fun onViewAccount(id: String) {
        if (PlayerHelper.instance.decodeTimelineKindValue() == TimelineViewModel.Kind.USER.ordinal ||
            PlayerHelper.instance.decodeTimelineKindValue() == TimelineViewModel.Kind.USER_PINNED.ordinal ||
            PlayerHelper.instance.decodeTimelineKindValue() == TimelineViewModel.Kind.USER_WITH_REPLIES.ordinal
        ) {
            dismiss()
        } else {
            context.startActivity(AccountActivity.newIntent(context, id))
        }
    }

    override fun onViewUrl(url: String) {
        if (!looksLikeMastodonUrl(url)) {
            context.openLink(url)
        }
    }

    override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
    }

    override fun onStartTrackingTouch(seekBar: SeekBar?) {
    }

    override fun onStopTrackingTouch(seekBar: SeekBar) {
        PlayerHelper.instance.seekTo(seekBar.progress.toLong())
    }

    override fun playListEmpty() {
        ToastUtil.showCenter(context.getString(R.string.tips_noaudition))
    }

    override fun onCleared() {

    }
}