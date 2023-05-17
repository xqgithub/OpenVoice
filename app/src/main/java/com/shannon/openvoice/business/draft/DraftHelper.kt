package com.shannon.openvoice.business.draft

import android.content.Context
import android.net.Uri
import android.webkit.MimeTypeMap
import androidx.core.content.FileProvider
import androidx.core.net.toFile
import androidx.core.net.toUri
import androidx.lifecycle.LiveData
import com.shannon.android.lib.util.FileUtil
import com.shannon.openvoice.BuildConfig
import com.shannon.openvoice.FunApplication
import com.shannon.openvoice.business.main.mine.account.AccountManager
import com.shannon.openvoice.db.AppDatabase
import com.shannon.openvoice.db.DraftAttachment
import com.shannon.openvoice.db.DraftModel
import com.shannon.openvoice.model.QueuedMedia
import com.shannon.openvoice.model.StatusModel
import timber.log.Timber
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import kotlin.concurrent.thread

/**
 *
 * @Package:        com.shannon.openvoice.business.draft
 * @ClassName:      DraftHelper
 * @Description:     作用描述
 * @Author:         czhen
 * @CreateDate:     2022/8/12 16:36
 */
class DraftHelper {

    private val draftDao = AppDatabase.getDatabase().draftDao()
    private val contentResolver = FunApplication.getInstance().contentResolver
    private val saveVoiceDir by lazy { createSaveDir() }

    fun saveDraft(
        draftId: Int,
        accountId: String,
        inReplyToId: String?,
        inReplyAuthor: String?,
        content: String?,
        visibility: StatusModel.Visibility,
        voiceModelId: Long,
        medias: List<QueuedMedia>,
        failedToSend: Boolean
    ): Int {

        val uris = medias.map { it.uri }
            .map {
                if (it.isNotInFolder()) {
                    it.copyToFolder(File(saveVoiceDir))
                } else {
                    it
                }
            }
        val types = uris.mapIndexed { index, uri ->
            val mimeType = contentResolver.getType(uri)
            when (mimeType?.substring(0, mimeType.indexOf('/'))) {
                "video" -> DraftAttachment.Type.VIDEO
                "image" -> DraftAttachment.Type.IMAGE
                else -> if (medias[index].type == QueuedMedia.Type.IMAGE) DraftAttachment.Type.IMAGE else DraftAttachment.Type.VIDEO
            }
        }

        val attachments: MutableList<DraftAttachment> = mutableListOf()
        for (i in medias.indices) {
            attachments.add(
                DraftAttachment(
                    uriString = uris[i].toString(),
                    type = types[i],
                    mediaSize = medias[i].mediaSize,
                    serverId = medias[i].id
                )
            )
        }

        val draft = DraftModel(
            id = draftId,
            accountId = accountId,
            inReplyToId = inReplyToId,
            inReplyAuthor = inReplyAuthor,
            content = content,
            visibility = visibility,
            voiceModelId = voiceModelId,
            attachments = attachments,
            failedToSend = failedToSend
        )
        return draftDao.insertOrReplace(draft).toInt()
    }

    fun loadDrafts(): LiveData<List<DraftModel>> {
        return draftDao.loadDrafts(AccountManager.accountManager.getAccountId())
    }

    fun deleteDraft(id: Int) {
        deleteAttachments(id)
        draftDao.delete(id)
    }

    private fun deleteAttachments(id: Int) {
        draftDao.find(id)?.let {
            it.attachments.forEach { attachment ->
                contentResolver.delete(attachment.uri, null, null)
            }
        }
    }


    private fun createSaveDir(): String {
        val dir = FunApplication.getInstance().getExternalFilesDir(null)?.path.plus(File.separator)
            .plus("drafts")
        FileUtil.createDir(dir, false)
        return dir
    }

    private fun Uri.isNotInFolder(): Boolean {
        val filePath = path ?: return true
        val parentPath = File(filePath).parentFile?.path ?: return true
        return !parentPath.endsWith("drafts")
    }

    private fun Uri.copyToFolder(folder: File): Uri {
        val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmssSSS", Locale.US).format(Date())

        val mimeType = contentResolver.getType(this)
        val map = MimeTypeMap.getSingleton()
        val fileExtension = map.getExtensionFromMimeType(mimeType)
        val filename = String.format("OpenVoice_Draft_Media_%s.%s", timeStamp, fileExtension)
        val file = File(folder, filename)
        FileUtil.copyToFile(contentResolver, this, file)
        return FileProvider.getUriForFile(
            FunApplication.getInstance(),
            BuildConfig.APPLICATION_ID + ".fileprovider",
            file
        )
    }
}