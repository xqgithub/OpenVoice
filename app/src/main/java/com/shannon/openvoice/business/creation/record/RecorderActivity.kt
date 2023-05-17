package com.shannon.openvoice.business.creation.record

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.commit
import com.shannon.android.lib.base.activity.KBaseActivity
import com.shannon.android.lib.util.FileUtil
import com.shannon.openvoice.R
import com.shannon.openvoice.business.creation.CreationViewModel
import com.shannon.openvoice.databinding.ActivityRecorderBinding

/**
 *
 * @ProjectName:    OpenVoice
 * @Package:        com.shannon.openvoice.business.creation.record
 * @ClassName:      RecorderActivity
 * @Description:     java类作用描述
 * @Author:         czhen
 * @CreateDate:     2022/7/29 15:35
 */
class RecorderActivity : KBaseActivity<ActivityRecorderBinding, CreationViewModel>() {

    private var invitationCodeID = -1

    private val recorderFragment by lazy { RecorderFragment() }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel.createSaveVoiceDir(context)
        if (savedInstanceState == null) {
            FileUtil.deleteChildFiles(viewModel.saveVoiceDir)
        }
    }

    override fun onInit() {
        intent?.getIntExtra(RecordingNotesActivity.INVITATIONCODEID, -1)?.let {
            invitationCodeID = it
        }

        setTitleText(R.string.sound_recording)
        viewModel.progressLive.observe(this) {
            binding.run {
                progressTextView.text = if (it <= 5) getString(
                    R.string.recording_progress,
                    it
                ) else getString(R.string.recording_complete)
                progressView.progress = it
            }
        }
        supportFragmentManager.commit {
            replace(R.id.containerView, recorderFragment)
        }
    }

    fun replaceCompleteFragment() {
        supportFragmentManager.commit {
//            remove(recorderFragment)
            replace(R.id.containerView, CompleteFragment.newInstance(invitationCodeID))
        }
    }

    companion object {
        fun newIntent(context: Context, invitationCodeID: Int = -1): Intent {
            val intent = Intent(context, RecorderActivity::class.java)
            if (invitationCodeID > 0) {
                intent.putExtra(RecordingNotesActivity.INVITATIONCODEID, invitationCodeID)
            }
            return intent
        }
//        fun newIntent(context: Context) = Intent(context, RecorderActivity::class.java)
    }
}