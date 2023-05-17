package com.shannon.openvoice.business.creation.record

import android.Manifest
import android.content.Context
import android.content.Intent
import android.os.Build
import com.permissionx.guolindev.PermissionX
import com.shannon.android.lib.base.activity.KBaseActivity
import com.shannon.android.lib.extended.addArray
import com.shannon.android.lib.extended.singleClick
import com.shannon.openvoice.R
import com.shannon.openvoice.business.compose.ComposeActivity
import com.shannon.openvoice.business.creation.CreationViewModel
import com.shannon.openvoice.databinding.ActivityRecordingNotesBinding
import com.shannon.openvoice.model.EventRecorder
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import timber.log.Timber

/**
 *
 * @Package:        com.shannon.openvoice.business.creation.record
 * @ClassName:      RecordingNotesActivity
 * @Description:     java类作用描述
 * @Author:         czhen
 * @CreateDate:     2022/7/28 14:49
 */
class RecordingNotesActivity : KBaseActivity<ActivityRecordingNotesBinding, CreationViewModel>() {


    private var invitationCodeID = -1

    override fun onInit() {
        intent?.getIntExtra(INVITATIONCODEID, -1)?.let {
            invitationCodeID = it
        }

        EventBus.getDefault().register(this)
        setTitleText(R.string.recording_considerations)
        binding.startButton.singleClick { requestPermissions() }
    }

    private fun requestPermissions() {
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
        PermissionX.init(this)
            .permissions(permissions)
            .request { allGranted, _, deniedList ->
                Timber.e(deniedList.toString())
                if (allGranted) {
                    toRecord()
                } else {
//                    FunApplication.appViewModel.toApplicationInfo(this)
                }
            }
    }

    private fun toRecord() {
        startActivity(RecorderActivity.newIntent(context, invitationCodeID))
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun subscribeEvent(event: EventRecorder) {
        onBackPressed()
    }

    override fun onDestroy() {
        super.onDestroy()
        EventBus.getDefault().unregister(this)
    }

    companion object {
        const val INVITATIONCODEID = "INVITATIONCODEID"

        fun newIntent(context: Context, invitationCodeID: Int = -1): Intent {
            val intent = Intent(context, RecordingNotesActivity::class.java)
            if (invitationCodeID > 0) {
                intent.putExtra(INVITATIONCODEID, invitationCodeID)
            }
            return intent
        }
//        fun newIntent(context: Context) = Intent(context, RecordingNotesActivity::class.java)
    }
}