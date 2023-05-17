package com.shannon.openvoice.business.creation.record

import android.Manifest
import android.os.Build
import android.webkit.MimeTypeMap
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.activityViewModels
import com.permissionx.guolindev.PermissionX
import com.shannon.android.lib.base.activity.KBaseFragment
import com.shannon.android.lib.base.viewmodel.EmptyViewModel
import com.shannon.android.lib.extended.*
import com.shannon.android.lib.util.FileUtil
import com.shannon.openvoice.BuildConfig
import com.shannon.openvoice.R
import com.shannon.openvoice.business.creation.CreationViewModel
import com.shannon.openvoice.business.main.mine.account.AccountManager
import com.shannon.openvoice.business.player.PlayerHelper
import com.shannon.openvoice.databinding.FragmentRecordBinding
import com.shannon.openvoice.model.EventRecorder
import com.shannon.openvoice.util.audio.AudioPlayer
import com.shannon.openvoice.util.audio.AudioRecorder
import org.greenrobot.eventbus.EventBus
import timber.log.Timber
import java.io.File

/**
 *
 * @Package:        com.shannon.openvoice.business.creation.record
 * @ClassName:      RecorderFragment
 * @Description:     作用描述
 * @Author:         czhen
 * @CreateDate:     2022/8/16 10:41
 */
class RecorderFragment : KBaseFragment<FragmentRecordBinding, EmptyViewModel>() {

    private val mViewModel by activityViewModels<CreationViewModel>()
    private val audioRecorder by lazy { AudioRecorder() }
    private val audioPlayer by lazy { AudioPlayer() }
    private val voiceTextRes = arrayListOf(
        R.string.recording_text,
        R.string.recording_text2,
        R.string.recording_text3,
        R.string.recording_text4,
        R.string.recording_text5
    )

    override fun onInit() {
        binding.run {
            uploadButton.visibility(BuildConfig.FLAVOR == "internal")
            next()
            startRecordButton.singleClick {
                PlayerHelper.instance.onPause()
                val permissions = arrayListOf<String>()
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    permissions.addArray(
                        Manifest.permission.RECORD_AUDIO,
                        Manifest.permission.READ_MEDIA_AUDIO
                    )
                } else {
                    permissions.addArray(
                        Manifest.permission.RECORD_AUDIO,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE
                    )
                }
                PermissionX.init(this@RecorderFragment)
                    .permissions(permissions)
                    .request { allGranted, _, _ ->
                        if (allGranted) {
                            startRecord()
                        } else {
//                            FunApplication.appViewModel.toApplicationInfo(requireActivity())
                        }
                    }
            }
            stopRecordButton.singleClick { stopRecord() }
            uploadButton.singleClick { chooseAudioLocal() }
            nextButton.singleClick { next() }
            previousButton.singleClick { previous() }
            doneButton.singleClick { done() }
            playButton.singleClick {
                val path = mViewModel.voicePathList.last()
                if (playButton.isSelected) {
                    audioPlayer.stop()
                    playButton.isSelected = false
                } else {
                    audioPlayer.play(path) {
                        requireActivity().runOnUiThread { playButton.isSelected = false }
                    }
                    playButton.isSelected = true
                }
            }
        }
    }

    private var saveFilePath: String? = null

    private fun startRecord() {
        if (isUploadMode()) next()
        with(binding) {
            //先将试听停止掉
            stopPlayer()
            playLayer.gone()
            //mViewModel.countdown { stopRecord() }
            val isStarted = audioRecorder.startRecord(File(saveFilePath!!))
            doneButton.isEnabled = false
            nextButton.isEnabled = false
            previousButton.isEnabled = false
            if (isStarted) {
                stopRecordButton.visible()
            } else {
                startRecordButton.invisible()
            }
            recordHintLayer.invisible()
            recordingAnimationView.visible()
        }
    }

    private fun stopRecord() {
        //mViewModel.disposeCountdown()
        audioRecorder.stopRecord()
        with(binding) {
            if (mViewModel.currentProgress == MAX_STEP) {
                doneButton.isEnabled = true
            } else {
                nextButton.isEnabled = true
            }
            previousButton.isEnabled = true
            startRecordButton.visible()
            stopRecordButton.gone()
//            recordHintLayer.visible()
            recordingAnimationView.gone()
            playLayer.visible()
        }
    }

    private fun next() {
        mViewModel.nextProgress()
        stopPlayer()
        val currentProgress = mViewModel.currentProgress
        if (currentProgress == 1) mViewModel.voicePathList.clear()
        setVoiceHintText()
        val accountId =
            AccountManager.accountManager.getAccountId()
        saveFilePath = if (mViewModel.wholeVoicePathList.size >= currentProgress) {
            mViewModel.wholeVoicePathList[currentProgress - 1]
        } else {
            mViewModel.saveVoiceDir.plus(File.separator)
                .plus("voice_sample_android_${accountId}_${System.currentTimeMillis()}_${mViewModel.currentProgress}.pcm")
                .also {
                    mViewModel.wholeVoicePathList.add(it)
                }
        }
        mViewModel.voicePathList.add(saveFilePath!!)
        stepLogic()
    }

    private fun previous() {
        stopPlayer()
        mViewModel.previousProgress()
        setVoiceHintText()
        mViewModel.voicePathList.removeLast()
        saveFilePath = mViewModel.voicePathList.last()
        stepLogic()
    }

    private fun stopPlayer() {
        audioPlayer.stop()
        binding.playButton.isSelected = false
    }

    private fun stepLogic() {
        binding.apply {
            if (mViewModel.voiceHasContent()) {
                playLayer.visible()
                recordHintLayer.invisible()
                nextButton.isEnabled = true
            } else {
                playLayer.gone()
                recordHintLayer.visible()
                nextButton.isEnabled = false
            }
            previousButton.visibility(mViewModel.voicePathList.size >= 2)
            if (mViewModel.currentProgress == MAX_STEP) {
                nextButton.gone()
                doneButton.visible()
            } else {
                nextButton.visible()
                doneButton.gone()
            }
        }
    }

    private fun setVoiceHintText() {
        val currentProgress = mViewModel.currentProgress
        binding.voiceTextView.text = getString(voiceTextRes[currentProgress - 1])
    }


    private fun done() {
        mViewModel.nextProgress()
        stopPlayer()
//        binding.doneButton.isEnabled = false
//        mViewModel.uploadVoice() {
//            EventBus.getDefault().post(EventRecorder(it))
//            onBackPressed()
//        }
        val activity = requireActivity()
        if (activity is RecorderActivity) {
            activity.replaceCompleteFragment()
        }
        Timber.d(mViewModel.voicePathList.toString())
    }

    private val audioLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) {
        it?.run {
            Timber.e(scheme)
            val contentResolver = requireContext().contentResolver
            val mimeType = contentResolver.getType(this) ?: "tmp"
            val accountId =
                AccountManager.accountManager.getAccountId()

            val suffix = MimeTypeMap.getSingleton().getExtensionFromMimeType(mimeType) ?: "mp3"
            val saveFilePath =
                mViewModel.saveVoiceDir.plus(File.separator)
                    .plus("local_voice_sample_android_${accountId}_${System.currentTimeMillis()}_${mViewModel.currentProgress}.$suffix")
            FileUtil.copyToFile(contentResolver, this, File(saveFilePath)).also { isSuccess ->
                Timber.d("copy $isSuccess")
            }
            mViewModel.voicePathList.clear()
            mViewModel.voicePathList.add(saveFilePath)
            binding.nextButton.gone()
            binding.doneButton.visible()
            mViewModel.progressLive.value = 0
            stopRecord()
        }
    }

    private fun isUploadMode(): Boolean {
        return mViewModel.voicePathList.size == 1 && mViewModel.voicePathList.first()
            .startsWith("local")
    }

    private fun chooseAudioLocal() {
        audioLauncher.launch("audio/*")
    }

    override fun handleOnBackPressed(): Boolean {
        requireContext().showConfirmDialog(message = getString(R.string.abandon_submit)) {
            EventBus.getDefault().post(EventRecorder(null))
            onBackPressed()
        }
        return true
    }

    override fun onDestroy() {
        super.onDestroy()
        //mViewModel.disposeCountdown()
        audioRecorder.stopRecord()
    }


    override fun handleResponseError(errorCode: Int) {
        binding.doneButton.isEnabled = true
    }


    companion object {
        private const val MAX_STEP = 5
    }

}