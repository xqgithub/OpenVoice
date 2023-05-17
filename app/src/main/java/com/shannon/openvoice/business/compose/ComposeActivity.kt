package com.shannon.openvoice.business.compose

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.text.Editable
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import androidx.activity.viewModels
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.core.widget.doAfterTextChanged
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.blankj.utilcode.util.FileUtils
import com.blankj.utilcode.util.KeyboardUtils
import com.gyf.immersionbar.ImmersionBar
import com.luck.picture.lib.config.PictureMimeType
import com.luck.picture.lib.entity.LocalMedia
import com.luck.picture.lib.interfaces.OnResultCallbackListener
import com.shannon.android.lib.base.activity.KBaseActivity
import com.shannon.android.lib.constant.ConfigConstants
import com.shannon.android.lib.extended.*
import com.shannon.android.lib.util.PreferencesUtil
import com.shannon.android.lib.util.ThemeUtil
import com.shannon.android.lib.util.ToastUtil
import com.shannon.openvoice.R
import com.shannon.openvoice.business.compose.models.ChooseModelActivity
import com.shannon.openvoice.business.creation.CreationViewModel
import com.shannon.openvoice.business.creation.ModelListAdapter
import com.shannon.openvoice.databinding.ActivityComposeBinding
import com.shannon.openvoice.dialog.*
import com.shannon.openvoice.event.UniversalEvent
import com.shannon.openvoice.model.*
import com.shannon.openvoice.util.highlightSpans
import kotlinx.coroutines.launch
import me.jingbin.library.decoration.SpacesItemDecoration
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import timber.log.Timber
import java.util.*
import java.util.regex.Pattern

/**
 *
 * @Package:        com.shannon.openvoice.business.compose
 * @ClassName:      ComposeActivity
 * @Description:     发布声文
 * @Author:         czhen
 * @CreateDate:     2022/8/9 16:39
 */
class ComposeActivity : KBaseActivity<ActivityComposeBinding, ComposeViewModel>(),
    AutoCompletionProvider, KeyboardUtils.OnSoftInputChangedListener,OnResultCallbackListener<LocalMedia> {

    private val keyboardMode =
        WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE or WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE
    private var voiceModelId = -1L
    private var mCurrentVisibility = StatusModel.Visibility.PUBLIC
    private val mMediaAdapter by lazy { ComposeMediaAdapter(onDelete = this::deleteMediaItem) }
    private var mComposeOptions: ComposeOptions? = null

    private val creationViewModel: CreationViewModel by viewModels()

    override fun onInit() {
        setNavigationBarColor(ThemeUtil.getColor(this, R.attr.navigationBarBackground))
        ImmersionBar.with(this)
            .keyboardEnable(true, keyboardMode)
            .init()
        EventBus.getDefault().register(this)
        if (intent.hasExtra(EXTRA_OPTIONS)) {
            mComposeOptions = intent.getParcelableExtra(EXTRA_OPTIONS)
            setup()
        }
        KeyboardUtils.registerSoftInputChangedListener(this, this)
        binding.run {
            setComposeContentEdit()
            setComposeMediaListView()
            mediaButton.singleClick(this@ComposeActivity::mediaButtonClick)
            atButton.singleClick(this@ComposeActivity::atButtonClick)
            hashButton.singleClick(this@ComposeActivity::hashButtonClick)
            modifyModelButton.singleClick(this@ComposeActivity::showModelChooseActivity)
            visibilityButton.singleClick(this@ComposeActivity::visibilityButtonClick)
            visibilityShownLayout.singleClick(this@ComposeActivity::visibilityButtonClick)
            postButton.singleClick(this@ComposeActivity::sendVoiceCover)
        }
//        viewModel.fetchVoiceModels(this::loadVoiceModels)
        creationViewModel.voiceModelsActivited() {
            initVoiceModel(it)
        }

        //判断是否需要显示协议弹框
        if (PreferencesUtil.getBool(
                PreferencesUtil.Constant.RELEASE_AGREEMENT_DISPLAYED_STATUS,
                true
            )
        ) {
            showAgreementDialog()
        } else {
            showSoftInput()
        }
    }

    override fun onForeground() {
        showSoftInput()
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun setComposeContentEdit() {
        binding.contentEditView.run {
//            filters = arrayOf(InputFilter.LengthFilter(DEFAULT_CHARACTER_LIMIT))
            setAdapter(AutoCompleteAdapter(this@ComposeActivity))
            setTokenizer(AutoCompleteTokenizer())
            updateContentLength()
            setOnTouchListener { v, event ->
                if (event.action == MotionEvent.ACTION_MOVE || event.action == MotionEvent.ACTION_DOWN) {
                    v.parent.requestDisallowInterceptTouchEvent(true)
                } else if (event.action == MotionEvent.ACTION_UP) {
                    v.parent.requestDisallowInterceptTouchEvent(false)
                }
                false
            }
            val linkColor = linkTextColors.defaultColor
            highlightSpans(text, linkColor)
            doAfterTextChanged {
                it?.run {
                    highlightSpans(this, linkColor)
                    updateContentLength()
                }
            }
            if (Build.VERSION.SDK_INT == Build.VERSION_CODES.O ||
                Build.VERSION.SDK_INT == Build.VERSION_CODES.O_MR1
            ) {
                setLayerType(View.LAYER_TYPE_SOFTWARE, null)
            }

            setDropDownBackgroundDrawable(
                ColorDrawable(
                    ThemeUtil.getColor(
                        context,
                        R.attr.bulletFrameBackground
                    )
                )
            )
//            dropDownVerticalOffset = 104.dp
        }
    }

    private fun setup() {
        mComposeOptions?.run {
            var contentText = content
            if (mentionedUsernames != null) {
                val builder = StringBuilder()
                mentionedUsernames?.forEach {
                    builder.append('@')
                    builder.append(it)
                    builder.append(' ')
                }
                contentText = builder.toString()
            }
            binding.contentEditView.text =
                Editable.Factory.getInstance().newEditable(contentText ?: "")
            binding.contentEditView.setSelection(binding.contentEditView.length())
            if (replyingStatusAuthor != null) {
                binding.replayInfoView.visible()
                binding.replayInfoView.text = getString(R.string.tips_reply, replyingStatusAuthor)
            }
            mCurrentVisibility = visibility ?: StatusModel.Visibility.PUBLIC
            changeVisibilityView()
            viewModel.setup(this)
        }
    }

    private fun setComposeMediaListView() {
        binding.mediaListView.run {
            adapter = mMediaAdapter
            layoutManager = LinearLayoutManager(context, RecyclerView.HORIZONTAL, false)
            itemDivider(SpacesItemDecoration.HORIZONTAL, R.color.transparent, 10.dp)
        }
        lifecycleScope.launch {
            viewModel.mediaLive.collect(::observeMediaList)
        }
        viewModel.mediaButtonEnabled.observe(this, ::observeMediaButtonEnabled)
    }

    private fun observeMediaList(list: List<QueuedMedia>) {
        mMediaAdapter.setNewDataIgnoreSize(list)
    }

    private fun deleteMediaItem(item: QueuedMedia) {
        viewModel.removeMediaJob(item.localId)
    }

    private fun observeMediaButtonEnabled(enabled: Boolean) {
        binding.mediaButton.isEnabled = enabled
    }

    private fun showModelChooseDialog(v: View) {
        val voiceModels = viewModel.voiceModels
        if (voiceModels.isNotEmpty()) {
            VoiceModelChooseDialog(this, voiceModels) { position ->
                val model = voiceModels[position]
                viewModel.activateVoiceModel(model.id) {
                    if (it.isModelOwner || it.isOfficial) {
                        EventBus.getDefault().post(EventActivateVoice(model.id, hashCode()))
                        initVoiceModel(model)
                        voiceModels.forEach { m ->
                            m.activated = m.id == model.id
                        }
                    } else {
                        voiceModels.removeAt(position)
                    }
                }
            }.show()
        }
    }

    /**
     * 显示模型选择页面
     */
    private fun showModelChooseActivity(v: View) {
//        intentToJump(
//            this@ComposeActivity,
//            ModelComprehensiveActivity::class.java,
//            bundle = Bundle().apply {
//                putString(
//                    ModelComprehensiveActivity.MODELWORKSWAY,
//                    ModelComprehensiveActivity.MODELWORKSWAY_MODIFY
//                )
//            },
//            enterAnimID = R.anim.activity_right_in,
//            exitAnimID = R.anim.activity_right_out
//        )
        startActivity(ChooseModelActivity.newIntent(context))
    }

    private fun loadVoiceModels(list: List<VoiceModelResult>) {
        val activatedIndex = list.indexOfFirst { it.activated == true }
        if (activatedIndex != -1) initVoiceModel(list[activatedIndex])
    }

    private fun initVoiceModel(model: VoiceModelResult) {
        binding.run {
            sourceView.visibility = View.VISIBLE
            modelNameView.visibility = View.VISIBLE
            sourceView.text = context.getString(ModelListAdapter.getSourceText(model.modelSource))
            sourceView.newShapeDrawable(ModelListAdapter.getSourceBackgroundColor(model.modelSource))
            modelNameView.text = model.name
            voiceModelId = model.id
        }
    }

    override fun search(token: String): List<AutoCompleteResult> {
        return viewModel.searchAutocompleteSuggestions(token)
    }

    private fun mediaButtonClick(v: View) {
        val maxSize = MAX_MEDIA_IMAGE_SIZE - viewModel.mediaLive.value.size
        MediaChooseDialog(context, maxSize,this).show()
    }

    private fun atButtonClick(v: View) {
        val text = if (calculateTextLength() == 0) "@" else " @"
        prependSelectedWordsWith(text)
        showSoftInput()
    }

    private fun hashButtonClick(v: View) {
        val text = if (calculateTextLength() == 0) "#" else " #"
        prependSelectedWordsWith(text)
        showSoftInput()
    }

    private fun visibilityButtonClick(v: View) {
        VisibilityChooseDialog(context, mCurrentVisibility) {
            mCurrentVisibility = it
            changeVisibilityView()
        }.show()
    }

    private fun changeVisibilityView() {
        val visibilityIcon = when (mCurrentVisibility) {
            StatusModel.Visibility.PUBLIC -> R.drawable.icon_visibility_public_tag
            StatusModel.Visibility.UNLISTED -> R.drawable.icon_visibility_unlisted_tag
            StatusModel.Visibility.PRIVATE -> R.drawable.icon_visibility_private_tag
            StatusModel.Visibility.DIRECT -> R.drawable.icon_visibility_direct_tag
            else -> R.drawable.icon_visibility_public_tag
        }
        binding.visibilityButton.setImageResource(visibilityIcon)
    }


    private fun prependSelectedWordsWith(text: CharSequence) {
        // If you select "backward" in an editable, you get SelectionStart > SelectionEnd
        val start =
            binding.contentEditView.selectionStart.coerceAtMost(binding.contentEditView.selectionEnd)
        val end =
            binding.contentEditView.selectionStart.coerceAtLeast(binding.contentEditView.selectionEnd)
        val editorText = binding.contentEditView.text

        if (start == end) {
            // No selection, just insert text at caret
            editorText.insert(start, text)
            // Set the cursor after the inserted text
            binding.contentEditView.setSelection(
                Math.min(
                    start + text.length,
                    DEFAULT_CHARACTER_LIMIT
                )
            )
        } else {
            var wasWord: Boolean
            var isWord = end < editorText.length && !Character.isWhitespace(editorText[end])
            var newEnd = end

            // Iterate the selection backward so we don't have to juggle indices on insertion
            var index = end - 1
            while (index >= start - 1 && index >= 0) {
                wasWord = isWord
                isWord = !Character.isWhitespace(editorText[index])
                if (wasWord && !isWord) {
                    // We've reached the beginning of a word, perform insert
                    editorText.insert(index + 1, text)
                    newEnd += text.length
                }
                --index
            }

            if (start == 0 && isWord) {
                editorText.insert(0, text)
                newEnd += text.length
            }

            binding.contentEditView.setSelection(start, newEnd)
        }
    }

    private fun updateContentLength() {
        val remainingLength = DEFAULT_CHARACTER_LIMIT - calculateTextLength()
        binding.contentLengthView.text =
            String.format(Locale.getDefault(), "%d", remainingLength)

        val textColor = if (remainingLength < 0) {
            ContextCompat.getColor(this, R.color.color_EC5143)
        } else {
            ThemeUtil.getColor(this, android.R.attr.textColorPrimary)
        }
        binding.contentLengthView.setTextColor(textColor)
    }

    private fun calculateTextLength(): Int {
        var offset = 0
        val urlSpans = binding.contentEditView.urls
        if (urlSpans != null) {
            for (span in urlSpans) {
                offset += (span.url.length - DEFAULT_CHARACTERS_RESERVED_PER_URL)
            }
        }
        return binding.contentEditView.length() - offset
    }

    private val loadingDialog by lazy { ComposeLoadingDialog(context) }

    private fun sendVoiceCover(v: View) {
        enableButtons(false)
        val contentText = binding.contentEditView.text.toString()
        val characterCount = calculateTextLength()
        //有效的字符数量，不计算标点符号、特殊符号
        val validCharactersCount = countPunctuationNumber(contentText)
        if ((characterCount == 0 || contentText.isBlank()) && viewModel.mediaLive.value.isEmpty()) {
            ToastUtil.showCenter(getString(R.string.tips_Input_content))
            enableButtons(true)
        } else if (characterCount - validCharactersCount < 5) {
            ToastUtil.showCenter(getString(R.string.audio_failure))
            enableButtons(true)
        } else if (characterCount > 300) {
            ToastUtil.showCenter(getString(R.string.character_exceed))
            enableButtons(true)
        } else if (viewModel.waitingContinueUpload()) {
            ToastUtil.showCenter(getString(R.string.tips_file_upload))
            enableButtons(true)
        } else if (voiceModelId == -1L) {
            enableButtons(true)
        } else {
//            showComposeLoading(R.string.desc_audio_converting)
            viewModel.sendVoiceCover(contentText, voiceModelId, mCurrentVisibility) {
//                dismissComposeLoading()
                if (it.isNotEmpty()) {
                    ToastUtil.showCenter(getString(R.string.publish_complete))
                    finish()
                } else {
                    enableButtons(true)
                    ToastUtil.showCenter(getString(R.string.tips_publish_fail))
                }
            }
        }
    }

    private fun showComposeLoading(resId: Int = -1) {
        loadingDialog.run {
            if (!isShowing) {
                show()
                setLoadingMessage(resId)
            }
        }
    }

    private fun dismissComposeLoading() {
        loadingDialog.run {
            if (isShowing) dismiss()
        }
    }

    private fun enableButtons(enabled: Boolean) {
        binding.run {
            modifyModelButton.isClickable = enabled
            mediaButton.isClickable = enabled
            visibilityButton.isClickable = enabled
            hashButton.isClickable = enabled
            atButton.isClickable = enabled
            postButton.isClickable = enabled
        }
    }

    private fun countPunctuationNumber(src: CharSequence): Int {
        var count = 0
        src.forEach { if (isPunctuation(it)) count++ }
        return count
    }

    private val pattern: Pattern = Pattern.compile("^[a-zA-Z0-9]+\$") //匹配字母
    private fun isPunctuation(c: Char): Boolean {
        if (pattern.matcher(c.toString()).matches()) return false
        val targetUnicodeBlock = Character.UnicodeBlock.of(c)
        return (targetUnicodeBlock == Character.UnicodeBlock.GENERAL_PUNCTUATION
                || targetUnicodeBlock == Character.UnicodeBlock.CJK_SYMBOLS_AND_PUNCTUATION
                || targetUnicodeBlock == Character.UnicodeBlock.HALFWIDTH_AND_FULLWIDTH_FORMS
                || targetUnicodeBlock == Character.UnicodeBlock.LOW_SURROGATES
                || targetUnicodeBlock == Character.UnicodeBlock.SUPPLEMENTAL_PUNCTUATION
                || targetUnicodeBlock == Character.UnicodeBlock.CURRENCY_SYMBOLS
                || targetUnicodeBlock == Character.UnicodeBlock.BASIC_LATIN)
    }

    private fun showSoftInput() {
        binding.run {
            contentEditView.postDelayed({
                contentEditView.requestFocus()
                KeyboardUtils.showSoftInput(contentEditView)
            }, 288)
        }
    }

    override fun onBackPressed() {
        if (hasContent()) {
            showConfirmDialog(
                message = getString(R.string.tips_save_draft),
                okText = getString(R.string.save),
                cancelText = getString(R.string.delete),
                doConfirm = {
                    val contentText = binding.contentEditView.text.toString()
                    viewModel.saveDraft(contentText, voiceModelId, mCurrentVisibility)
                    super.onBackPressed()
                }, doCancel = {
                    viewModel.deleteDraft()
                    super.onBackPressed()
                }
            )
        } else {
            super.onBackPressed()
        }

    }

    private fun hasContent(): Boolean {
        val contentText = binding.contentEditView.text.toString()
        val mediaList = viewModel.mediaLive.value
        return contentText.isNotEmpty() || mediaList.isNotEmpty()
    }

    /**
     * 显示协议弹框
     */
    private fun showAgreementDialog() {
        ConfirmDialog(
            this@ComposeActivity,
            width = 300.dp,
            height = WindowManager.LayoutParams.WRAP_CONTENT
        ).apply {
            setBG(
                R.attr.bulletFrameBackground
            )
            setTitleText(
                _visibility = View.GONE,
                content = getString(R.string.title_kindly_reminder),
                _textSize = 14f,
                _textColorInt = ThemeUtil.getColor(
                    this@ComposeActivity,
                    R.attr.textColorPrimary
                )
            )
            setContentText(_visibility = View.GONE)
            setWebViewContent(url = ConfigConstants.CONSTANT.reminderValue)
            setConfirmText(
                content = getString(R.string.refuse),
                _textSize = 14f,
                _textColorInt = ThemeUtil.getColor(
                    this@ComposeActivity,
                    android.R.attr.textColorSecondary
                )
            ) {
                dismissDialog()
                showAgreementSecondConfirmDialog()
            }
            setConfirm2Text(
                content = getString(R.string.agree),
                _textSize = 14f,
                _textColorInt = ThemeUtil.getColor(
                    this@ComposeActivity,
                    android.R.attr.colorPrimary
                )
            ) {
                dismissDialog()
                PreferencesUtil.putBool(
                    PreferencesUtil.Constant.RELEASE_AGREEMENT_DISPLAYED_STATUS,
                    false
                )
            }
            show()
        }
    }

    /**
     * 显示协议二次确认弹框
     */
    private fun showAgreementSecondConfirmDialog() {
        ConfirmDialog(
            this@ComposeActivity,
            width = 248.dp,
            height = WindowManager.LayoutParams.WRAP_CONTENT
        ).apply {
            setBG(
                R.attr.bulletFrameBackground
            )
            setContentText(
                content = getString(R.string.content_refuse_rules),
                _textSize = 14f,
                _textColorInt = ThemeUtil.getColor(
                    this@ComposeActivity,
                    R.attr.textColorPrimary
                )
            )
            setConfirmText(
                content = getString(R.string.sure),
                _textSize = 14f,
                _textColorInt = ThemeUtil.getColor(
                    this@ComposeActivity,
                    android.R.attr.textColorSecondary
                )
            ) {
                dismissDialog()
                finish()
            }
            setConfirm2Text(
                content = getString(R.string.cancel),
                _textSize = 14f,
                _textColorInt = ThemeUtil.getColor(
                    this@ComposeActivity,
                    android.R.attr.colorPrimary
                )
            ) {
                showAgreementDialog()
                dismissDialog()
            }
            show()
        }
    }


//    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
//        super.onActivityResult(requestCode, resultCode, data)
//        if (resultCode == RESULT_OK && requestCode == MediaChooseDialog.REQUEST_CAMERA_MEDIA) {
//            val localMedia = PictureSelector.obtainMultipleResult(data)
//            if (localMedia != null && localMedia.size > 0) {
//                val media = localMedia[0]
//                val path = if (TextUtils.isEmpty(media.realPath)) media.path else media.realPath
//                val picPathUri = FileUtils.getFileByPath(path).toUri()
//                viewModel.addMediaToQueue(
//                    QueuedMedia.Type.IMAGE,
//                    picPathUri,
//                    localMedia[0].size
//                )
//                Timber.e("REQUEST_CAMERA_MEDIA ; $picPathUri")
//            }
//        } else if (resultCode == RESULT_OK && requestCode == MediaChooseDialog.REQUEST_IMAGE_VIDEO_MEDIA) {
//            val localMedia = PictureSelector.obtainMultipleResult(data)
//
//        }
//    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun eventBusCloseTheOfficialModelPage(event: UniversalEvent) {
        if (event.actionType == UniversalEvent.modifySelectedSoundModel) {
            val bean = event.message as VoiceModelSelected
            val voiceModels = bean.models
            val model = voiceModels[bean.selectedPosition]
            viewModel.activateVoiceModel(model.id) {
                if (it.isModelOwner || it.isOfficial) {
                    EventBus.getDefault()
                        .post(EventActivateVoice(model.id, hashCode()))
                    EventBus.getDefault()
                        .post(UniversalEvent(UniversalEvent.closeModelComprehensiveActivity, null))
                    initVoiceModel(model)
                    voiceModels.forEach { m ->
                        m.activated = m.id == model.id
                    }
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        EventBus.getDefault().unregister(this)
        KeyboardUtils.unregisterSoftInputChangedListener(window)
    }

    companion object {
        const val DEFAULT_CHARACTER_LIMIT = 300
        const val DEFAULT_CHARACTERS_RESERVED_PER_URL = 23
        const val MAX_MEDIA_IMAGE_SIZE = 4
        const val MAX_MEDIA_VIDEO_SIZE = 1
        const val EXTRA_OPTIONS = "extraOptions"

        fun newIntent(context: Context) = Intent(context, ComposeActivity::class.java)

        fun newIntentWith(context: Context, options: ComposeOptions): Intent {
            val intent = Intent(context, ComposeActivity::class.java)
            intent.putExtra(EXTRA_OPTIONS, options)
            return intent
        }
    }

    override fun onSoftInputChanged(height: Int) {
        binding.run {
            val editContainerLp = editContainer.layoutParams
            if (height == 0) {
                editContainerLp.height = 400.dp
            } else {
                editContainerLp.height = nestedScrollView.height - height - 24.dp
            }
            editContainer.requestLayout()
        }
    }

    override fun onResult(result: ArrayList<LocalMedia>?) {
        if (result != null && result.size > 0) {
            result.forEach {
                if (it.mimeType == PictureMimeType.MP4_Q ||
                    it.mimeType == PictureMimeType.AVI_Q
                ) {
                    val maxFileSize = 40 * 1024 * 1024
                    if (it.size > maxFileSize) {
                        ToastUtil.showCenter(getString(R.string.tips_video_transfinite))
                        return
                    }
                }
                val picPathUri = FileProvider.getUriForFile(
                    context,
                    packageName.plus(".fileprovider"),
                    FileUtils.getFileByPath(it.compressPath ?: it.realPath)
                )
                val fileSize =
                    if (it.compressPath != null) FileUtils.getLength(it.compressPath) else it.size

                viewModel.addMediaToQueue(
                    if (PictureMimeType.isHasImage(it.mimeType))
                        QueuedMedia.Type.IMAGE else QueuedMedia.Type.VIDEO,
                    picPathUri,
                    fileSize
                )
                Timber.e("REQUEST_IMAGE_VIDEO_MEDIA ; $picPathUri ; $fileSize")
            }
        }
    }

    override fun onCancel() {
    }

}