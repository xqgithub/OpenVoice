package com.shannon.openvoice.business.main.notification

import android.content.Context
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.text.Spanned
import android.text.TextUtils
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.DrawableRes
import androidx.appcompat.widget.AppCompatTextView
import com.blankj.utilcode.util.AppUtils
import com.blankj.utilcode.util.LogUtils
import com.bumptech.glide.Glide
import com.shannon.android.lib.base.adapter.BaseFunBindingRecyclerViewAdapter
import com.shannon.android.lib.extended.*
import com.shannon.android.lib.util.ThemeUtil
import com.shannon.openvoice.R
import com.shannon.openvoice.business.timeline.listener.LinkListener
import com.shannon.openvoice.components.MediaPreviewImageView
import com.shannon.openvoice.databinding.AdapterNotificationListBinding
import com.shannon.openvoice.databinding.ItemMediaPreviewBinding
import com.shannon.openvoice.extended.loadAvatar
import com.shannon.openvoice.model.Attachment
import com.shannon.openvoice.model.HashTag
import com.shannon.openvoice.model.Notification
import com.shannon.openvoice.model.StatusModel
import com.shannon.openvoice.util.BlurHashDecoder
import com.shannon.openvoice.util.TimestampUtils
import com.shannon.openvoice.util.UnitHelper
import com.shannon.openvoice.util.setClickableText
import me.jingbin.library.adapter.BaseByViewHolder
import java.util.*

/**
 * Date:2022/8/31
 * Time:16:50
 * author:dimple
 * 通知列表数据适配器
 */
class NotificationListAdapter(
    val mContext: Context,
    val linkListener: LinkListener
) :
    BaseFunBindingRecyclerViewAdapter<Notification, AdapterNotificationListBinding>() {

    private val mediaLabels = arrayListOf<TextView>()
    private val mediaPreviews = arrayListOf<MediaPreviewImageView>()
    private val mediaOverlays = arrayListOf<ImageView>()
    private lateinit var mediaPreviewUnloaded: Drawable

    private lateinit var mediaPreviewBinding: ItemMediaPreviewBinding

    override fun bindView(
        holder: BaseByViewHolder<Notification>,
        binding: AdapterNotificationListBinding,
        bean: Notification,
        position: Int
    ) {
        binding.apply {
            mediaPreviewBinding = ItemMediaPreviewBinding.bind(root)
            var voice_model_status = ""
            var voice_model_status2 = ""
            var voice_model_status_color = -1

            mediaPreviewBinding.run {
                mediaLabels.clear()
                mediaPreviews.clear()
                mediaOverlays.clear()
                mediaLabels.addArray(
                    statusMediaLabel0,
                    statusMediaLabel1,
                    statusMediaLabel2,
                    statusMediaLabel3
                )
                mediaPreviews.addArray(
                    statusMediaPreview0,
                    statusMediaPreview1,
                    statusMediaPreview2,
                    statusMediaPreview3
                )
                mediaOverlays.addArray(
                    statusMediaOverlay0,
                    statusMediaOverlay1,
                    statusMediaOverlay2,
                    statusMediaOverlay3
                )
            }
            mediaPreviewUnloaded =
                ColorDrawable(ThemeUtil.getColor(mContext, R.attr.colorBackgroundAccent))

            ivType.apply {
                if (bean.type == Notification.Type.REBLOG ||
                    bean.type == Notification.Type.FAVOURITE
                ) visible()
                else gone()
            }
            tvForwarderUsername.apply {
                if (bean.type == Notification.Type.REBLOG ||
                    bean.type == Notification.Type.FAVOURITE
                ) {
                    visible()
                } else gone()
            }
            tvTime.apply {
//                if (bean.type == Notification.Type.REBLOG ||
//                    bean.type == Notification.Type.FAVOURITE ||
//                    bean.type == Notification.Type.FOLLOW ||
//                    bean.type == Notification.Type.MENTION
//                ) visible()
//                else gone()
                visible()
            }
            clDuwenContent.apply {
                if (bean.type == Notification.Type.REBLOG ||
                    bean.type == Notification.Type.FAVOURITE ||
                    bean.type == Notification.Type.MENTION
                ) visible()
                else gone()
            }
            tvFollowTips.apply {
                if (bean.type == Notification.Type.FOLLOW) visible() else gone()
            }
            tvSoundModelStatus.apply {
                if (bean.type == Notification.Type.VOICEMODELCREATED ||
                    bean.type == Notification.Type.VOICEMODELGENERATED ||
                    bean.type == Notification.Type.VOICEMODELGENERATEFAILED ||
                    bean.type == Notification.Type.VOICEMODELBOUGHTED ||
                    bean.type == Notification.Type.VOICEMODELSOLDED
                ) visible()
                else gone()
            }


            tvTime.text = setTimestampInfo(bean.createdAt)

            when (bean.type) {
                Notification.Type.REBLOG -> {
                    ivType.setImageResource(R.drawable.icon_reblog_active)
                    tvForwarderUsername.text = "@${bean.status?.account?.username}"
                    tvTime.text = setTimestampInfo(bean.createdAt)
//                    LogUtils.i("tvTime 0 =-= ${setTimestampInfo(bean.createdAt)}")
                }
                Notification.Type.FAVOURITE -> {
                    ivType.setImageResource(R.drawable.icon_favourite_active)
                    tvForwarderUsername.text = "@${bean.status?.account?.localUsername}"
                    tvTime.text = setTimestampInfo(bean.createdAt)
                }
                Notification.Type.FOLLOW -> {
                    sivAvatar.loadAvatar(bean.account.avatar)
                    ivType.apply {
                        setImageResource(R.drawable.ic_person_add_24dp)
                    }
                    tvUserNameNickName.text = contentHighlight(
                        "${bean.account.name} @${bean.account.localUsername}",
                        arrayOf("@${bean.account.localUsername}"),
                        arrayOf(
                            ThemeUtil.getColor(
                                mContext,
                                android.R.attr.textColorSecondary
                            )
                        ),
                        arrayOf(14)
                    )
                    tvTime.text = setTimestampInfo(bean.createdAt)

                    tvFollowTips.apply {
                        visible()
                        val content =
                            "${mContext.getString(R.string.follow)} (${bean.account.name})${
                                mContext.getString(R.string.desc_attention_notice)
                            }"
                        text = contentHighlight(
                            content,
                            arrayOf(
                                "${mContext.getString(R.string.follow)}",
                                "(${bean.account.name})"
                            ),
                            arrayOf(
                                ThemeUtil.getColor(
                                    mContext,
                                    android.R.attr.textColorSecondary
                                ), ThemeUtil.getColor(
                                    mContext,
                                    android.R.attr.colorPrimary
                                )
                            )
                        )
                    }

                    tvForwarderUsername.apply {
                        text = ""
                    }
                }
                Notification.Type.MENTION -> {
                    tvForwarderUsername.apply {
                        text = ""
                    }
                }
                Notification.Type.VOICEMODELCREATED -> {//模型创建成功
                    voice_model_status = "${mContext.getString(R.string.button_submit)}"
                    voice_model_status2 = "${
                        mContext.getString(
                            R.string.desc_submit_notice,
                            "${bean.voiceModel?.name}"
                        )
                    }"
                    voice_model_status_color = R.attr.colormodelsuc
                }
                Notification.Type.VOICEMODELGENERATED -> {
                    voice_model_status = "${mContext.getString(R.string.notice_succ)}"
                    voice_model_status2 = "${
                        mContext.getString(
                            R.string.desc_generate_succ,
                            "${bean.voiceModel?.name}"
                        )
                    }"
                    voice_model_status_color = R.attr.colormodelsuc
                }
                Notification.Type.VOICEMODELGENERATEFAILED -> {
                    voice_model_status = "${mContext.getString(R.string.notice_fail)}"
                    voice_model_status2 = "${
                        mContext.getString(
                            R.string.desc_generate_fail,
                            "${bean.voiceModel?.name}"
                        )
                    }"
                    voice_model_status_color = R.attr.colormodelfail
                }
                Notification.Type.VOICEMODELBOUGHTED -> {
                    voice_model_status = "${mContext.getString(R.string.notice_purchased)}"
                    voice_model_status2 = "${
                        mContext.getString(
                            R.string.desc_buy_succ,
                            "${bean.voiceModel?.name}"
                        )
                    }"
                    voice_model_status_color = R.attr.colormodelsuc
                }
                Notification.Type.VOICEMODELSOLDED -> {
                    voice_model_status = "${mContext.getString(R.string.notice_sold)}"
                    voice_model_status2 = "${
                        mContext.getString(
                            R.string.desc_buy_fail,
                            "${bean.voiceModel?.name}"
                        )
                    }"
                    voice_model_status_color = R.attr.colormodelsuc
                }
            }

            if (!isBlankPlus(bean.voiceModel)) {
                sivAvatar.setImageResource(R.drawable.icon_launcher)
//                tvUserNameNickName.text = contentHighlight(
//                    "${AppUtils.getAppName()} ${setTimestampInfo(bean.createdAt)}",
//                    arrayOf("${setTimestampInfo(bean.createdAt)}"),
//                    arrayOf(
//                        ThemeUtil.getColor(
//                            mContext,
//                            android.R.attr.textColorSecondary
//                        )
//                    ),
//                    arrayOf(12)
//                )
                tvUserNameNickName.text = "${AppUtils.getAppName()}"


                tvSoundModelStatus.apply {
                    text = contentHighlight(
                        "$voice_model_status ${voice_model_status2}",
                        arrayOf("$voice_model_status"),
                        arrayOf(
                            ThemeUtil.getColor(
                                mContext,
                                voice_model_status_color
                            )
                        )
                    )
                }
            } else {
                bean.status?.let { status ->
                    if (!(bean.type == Notification.Type.REBLOG ||
                                bean.type == Notification.Type.FOLLOW
                                || bean.type == Notification.Type.FAVOURITE)
                    ) {
                        tvTime.text = setTimestampInfo(bean.status!!.createdAt)
//                        LogUtils.i("tvTime 1 =-= ${setTimestampInfo(bean.status!!.createdAt)}")
                    }

                    sivAvatar.loadAvatar(bean.account.avatar)
                    tvUserNameNickName.text = contentHighlight(
                        "${bean.account.name} @${bean.account.username}",
                        arrayOf("@${bean.account.username}"),
                        arrayOf(
                            ThemeUtil.getColor(
                                mContext,
                                android.R.attr.textColorSecondary
                            )
                        ),
                        arrayOf(14)
                    )

                    setStatusContent(
                        status.content.parseAsMastodonHtml(),
                        status.mentions,
                        status.tags,
                        tvStatusContent
                    )

                    val attachments = status.attachments
                    if (hasPreviewAttachment(attachments)) {
                        setMediaPreviews(attachments)
                        if (attachments.isEmpty()) {
                            hideSensitiveMediaWarning()
                        }
                        mediaLabels.forEach {
                            it.gone()
                        }
                    } else {
                        setMediaLabel(attachments)
                        mediaPreviews[0].visibility = View.GONE
                        mediaPreviews[1].visibility = View.GONE
                        mediaPreviews[2].visibility = View.GONE
                        mediaPreviews[3].visibility = View.GONE
                        hideSensitiveMediaWarning()
                    }

//                    replayCountView.text = UnitHelper.quantityConvert(status.repliesCount)
//                    reblogCountView.text = UnitHelper.quantityConvert(status.reblogCount)
//                    favouriteCountView.text = UnitHelper.quantityConvert(status.favouriteCount)
                }
            }
            holder.addOnClickListener(R.id.sivAvatar)
            holder.addOnClickListener(R.id.tvUserNameNickName)
            holder.addOnClickListener(R.id.clDuwenContent)
            holder.addOnClickListener(R.id.tvStatusContent)
            holder.addOnClickListener(R.id.tvForwarderUsername)
            holder.addOnClickListener(R.id.tvFollowTips)
        }
    }

    /**
     * 设置时间
     */
    private fun setTimestampInfo(createAt: Date): String? {
        val then = createAt.time
        val now = System.currentTimeMillis()
        return TimestampUtils.getRelativeTimeSpanString(mContext, then, now)
    }


    /**
     * 设置嘟文内容
     */
    private fun setStatusContent(
        content: Spanned,
        mentions: List<StatusModel.Mention>?,
        tags: List<HashTag>?,
        tvStatusContent: AppCompatTextView
    ) {
        setClickableText(tvStatusContent, content, mentions, tags, linkListener)
        if (TextUtils.isEmpty(tvStatusContent.text)) {
            tvStatusContent.gone()
        } else {
            tvStatusContent.visible()
        }
    }

    private fun hasPreviewAttachment(attachments: List<Attachment>): Boolean {
        for (attachment in attachments) {
            if (attachment.type === Attachment.Type.AUDIO || attachment.type === Attachment.Type.UNKNOWN) {
                return false
            }
        }
        return true
    }

    private fun setMediaPreviews(attachments: List<Attachment>) {
        val attachmentSize = Math.min(attachments.size, StatusModel.MAX_MEDIA_ATTACHMENTS)
        val mediaPreviewHeight = 100.dp
        if (attachmentSize <= 2) {
            mediaPreviews[0].layoutParams.height = mediaPreviewHeight * 2
            mediaPreviews[1].layoutParams.height = mediaPreviewHeight * 2
        } else {
            mediaPreviews[0].layoutParams.height = mediaPreviewHeight
            mediaPreviews[1].layoutParams.height = mediaPreviewHeight
            mediaPreviews[2].layoutParams.height = mediaPreviewHeight
            mediaPreviews[3].layoutParams.height = mediaPreviewHeight
        }

        if (attachmentSize > 0) {
            for (i in 0 until attachmentSize) {
                val attachment = attachments[i]
                val imageView = mediaPreviews[i]
                imageView.visible()

                loadImage(
                    imageView,
                    attachment.previewUrl,
                    attachment.meta,
                    null
                )
                val mediaType = attachment.type
                if ((mediaType == Attachment.Type.VIDEO || mediaType == Attachment.Type.GIFV)) {
                    mediaOverlays[i].visible()
                } else {
                    mediaOverlays[i].gone()
                }
//                setAttachmentClickListener(imageView, i, attachment, true)
            }
        }

        mediaPreviewBinding.statusSensitiveMediaWarning.gone()
        mediaPreviewBinding.statusSensitiveMediaButton.gone()

        for (i in attachmentSize until StatusModel.MAX_MEDIA_ATTACHMENTS) {
            mediaPreviews[i].gone()
        }
    }


    private fun loadImage(
        imageView: MediaPreviewImageView,
        previewUrl: String?,
        meta: Attachment.MetaData?,
        blur: String?
    ) {

        val placeholder = if (blur != null) decodeBlurHash(blur) else mediaPreviewUnloaded

        if (TextUtils.isEmpty(previewUrl)) {
            imageView.removeFocalPoint()
            Glide.with(imageView).load(placeholder).centerInside().into(imageView)
        } else {
            val focus = meta?.focus
            if (focus != null) {
                imageView.setFocalPoint(focus)
                Glide.with(imageView).load(previewUrl).placeholder(placeholder).centerInside()
                    .addListener(imageView).into(imageView)
            } else {
                imageView.removeFocalPoint()
                Glide.with(imageView).load(previewUrl).placeholder(placeholder).centerInside()
                    .into(imageView)
            }
        }

    }

    private fun decodeBlurHash(blur: String): BitmapDrawable {
        return BitmapDrawable(mContext.resources, BlurHashDecoder.decode(blur, 32, 32, 1f))
    }

    private fun hideSensitiveMediaWarning() {
        mediaPreviewBinding.statusSensitiveMediaWarning.gone()
        mediaPreviewBinding.statusSensitiveMediaButton.gone()
    }

    protected fun setMediaLabel(attachments: List<Attachment>) {
        for (i in mediaLabels.indices) {
            val mediaLabel = mediaLabels[i]
            if (i < attachments.size) {
                val attachment = attachments[i]
                mediaLabel.visibility = View.VISIBLE

                val drawableResId: Int = getLabelIcon(attachments[0].type)
                mediaLabel.setCompoundDrawablesWithIntrinsicBounds(drawableResId, 0, 0, 0)
//                setAttachmentClickListener(mediaLabel, i, attachment, false)
            } else {
                mediaLabel.visibility = View.GONE
            }
        }
    }

    @DrawableRes
    private fun getLabelIcon(type: Attachment.Type): Int {
        return when (type) {
            Attachment.Type.IMAGE -> R.drawable.icon_photo_24dp
            Attachment.Type.GIFV, Attachment.Type.VIDEO -> R.drawable.icon_video_24dp
            Attachment.Type.AUDIO -> R.drawable.icon_music_box_24dp
            else -> R.drawable.icon_attach_file_24dp
        }
    }


    override fun bindView(
        binding: AdapterNotificationListBinding,
        bean: Notification,
        position: Int
    ) {

    }

}