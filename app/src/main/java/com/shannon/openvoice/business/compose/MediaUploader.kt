package com.shannon.openvoice.business.compose

import android.media.MediaMetadataRetriever
import android.net.Uri
import android.webkit.MimeTypeMap
import androidx.core.content.FileProvider
import com.abedelazizshe.lightcompressorlibrary.CompressionListener
import com.abedelazizshe.lightcompressorlibrary.VideoCompressor
import com.abedelazizshe.lightcompressorlibrary.VideoQuality
import com.abedelazizshe.lightcompressorlibrary.config.Configuration
import com.abedelazizshe.lightcompressorlibrary.config.StorageConfiguration
import com.abedelazizshe.lightcompressorlibrary.utils.CompressorUtils
import com.shannon.android.lib.extended.randomAlphanumericString
import com.shannon.openvoice.FunApplication
import com.shannon.openvoice.model.QueuedMedia
import com.shannon.openvoice.network.ProgressRequestBody
import com.shannon.openvoice.network.convertResponse
import com.shannon.openvoice.network.mediaUploadApi
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import timber.log.Timber
import java.io.File
import java.util.*
import kotlin.math.roundToInt


sealed class UploadEvent {
    data class ProgressEvent(val localId: Int, val percentage: Int) : UploadEvent()
    data class FinishedEvent(val localId: Int, val mediaId: String) : UploadEvent()
}

/**
 *
 * @Package:        com.shannon.openvoice.business.compose
 * @ClassName:      MediaUploader
 * @Description:     作用描述
 * @Author:         czhen
 * @CreateDate:     2022/8/11 16:12
 */
class MediaUploader {

    private val contentResolver = FunApplication.getInstance().contentResolver

    @OptIn(ExperimentalCoroutinesApi::class)
    suspend fun uploadMedia(media: QueuedMedia): Flow<UploadEvent> {
        return flow {
            emit(media)
        }
            .flatMapLatest { upload(media) }
            .flowOn(Dispatchers.IO)
    }

    @OptIn(DelicateCoroutinesApi::class)
    private suspend fun upload(media: QueuedMedia): Flow<UploadEvent> {
        return callbackFlow {
            val body = createMultipartBody(media) {
                GlobalScope.launch(Dispatchers.IO) {
                    send(it)
                }
            }
            val result = mediaUploadApi.uploadMedia(body).convertResponse().blockingSingle()
            send(UploadEvent.FinishedEvent(media.localId, result.id))
            awaitClose()
        }
    }

    private fun createMultipartBody(
        media: QueuedMedia,
        onResult: (UploadEvent) -> Unit
    ): MultipartBody.Part {
        Timber.e("uri = ${media.uri} ")
        var mimeType = contentResolver.getType(media.uri)
        val map = MimeTypeMap.getSingleton()
        val fileExtension = map.getExtensionFromMimeType(mimeType)
        val filename = "%s_%s_%s.%s".format(
            "OpenVoice",
            Date().time.toString(),
            randomAlphanumericString(10),
            fileExtension
        )

        val stream = contentResolver.openInputStream(media.uri)

        if (mimeType == null) mimeType = "multipart/form-data"

        var lastProgress = -1
        val fileBody = ProgressRequestBody(
            stream, media.mediaSize,
            mimeType.toMediaTypeOrNull()!!
        ) { percentage ->
            if (percentage != lastProgress) {
                onResult(UploadEvent.ProgressEvent(media.localId, percentage))
            }
            lastProgress = percentage
        }

        return MultipartBody.Part.createFormData("file", filename, fileBody)
    }

    private fun needCompress(media: QueuedMedia): Boolean {
        val mediaMetadataRetriever = MediaMetadataRetriever()

        try {
            mediaMetadataRetriever.setDataSource(FunApplication.getInstance(), media.uri)
        } catch (exception: IllegalArgumentException) {
            CompressorUtils.printException(exception)
            return false
        }

        val originalHeight: Double = CompressorUtils.prepareVideoHeight(mediaMetadataRetriever)

        val originalWidth: Double = CompressorUtils.prepareVideoWidth(mediaMetadataRetriever)

        val bitrateData =
            mediaMetadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_BITRATE)
        Timber.d("bitrateData = $bitrateData")

        var maxSize = originalWidth.coerceAtLeast(originalHeight)
        Timber.d("maxSize = $maxSize")
        val compressionsCount = if (maxSize > 1280) 4
        else if (maxSize > 854) {
            3
        } else if (maxSize > 640) {
            2
        } else {
            1
        }
        Timber.d("compressionsCount = $compressionsCount")

        var selectedCompression = (100 / (100f / compressionsCount)).roundToInt()
        selectedCompression =
            if (selectedCompression > compressionsCount) compressionsCount else selectedCompression
        Timber.d("selectedCompression = $selectedCompression")

        return (media.mediaSize < 1024L * 1024L * 1000L && (selectedCompression != compressionsCount
                || originalWidth.coerceAtLeast(originalHeight) > 1280))
    }

    fun compress(
        media: QueuedMedia,
        onSuccess: (Uri, Long) -> Unit,
    ) {
        if(media.type == QueuedMedia.Type.IMAGE){
            onSuccess(media.uri, media.mediaSize)
            return
        }

        if (!needCompress(media)) {
            Timber.d("skip compression")
            onSuccess(media.uri, media.mediaSize)
            return
        }

        VideoCompressor.start(
            FunApplication.getInstance(),
            arrayListOf(media.uri),
            isStreamable = true,
            StorageConfiguration(
                fileName = null,
                saveAt = null,
                isExternal = false
            ),
            Configuration(
                VideoQuality.LOW,
                isMinBitrateCheckEnabled = false
            ),
            object : CompressionListener {
                override fun onCancelled(index: Int) {
                }

                override fun onFailure(index: Int, failureMessage: String) {
                    Timber.d("onFailure ----> $failureMessage")
                    onSuccess(media.uri, media.mediaSize)
                }

                override fun onProgress(index: Int, percent: Float) {
                }

                override fun onStart(index: Int) {
                    Timber.d("onStart")
                }

                override fun onSuccess(index: Int, size: Long, path: String?) {
                    Timber.d("onSuccess ----> $path ; size = $size")
                    path?.let {
                        val context = FunApplication.getInstance()
                        val uri = FileProvider.getUriForFile(
                            context, context.packageName.plus(".fileprovider"),
                            File(it)
                        )
                        onSuccess(uri, size)
                    }
                }
            }
        )
    }

    private fun makeVideoBitrate(
        originalHeight: Int,
        originalWidth: Int,
        originalBitrate: Int,
        height: Int,
        width: Int
    ): Int {
        val compressFactor: Float
        val minCompressFactor: Float
        val maxBitrate: Int
        if (Math.min(height, width) >= 1080) {
            maxBitrate = 6800000
            compressFactor = 1f
            minCompressFactor = 1f
        } else if (Math.min(height, width) >= 720) {
            maxBitrate = 2600000
            compressFactor = 1f
            minCompressFactor = 1f
        } else if (Math.min(height, width) >= 480) {
            maxBitrate = 1000000
            compressFactor = 0.75f
            minCompressFactor = 0.9f
        } else {
            maxBitrate = 750000
            compressFactor = 0.6f
            minCompressFactor = 0.7f
        }
        var remeasuredBitrate = (originalBitrate / Math.min(
            originalHeight / height.toFloat(),
            originalWidth / width.toFloat()
        )).toInt()
        remeasuredBitrate *= compressFactor.toInt()
        val minBitrate: Int =
            (getVideoBitrateWithFactor(minCompressFactor) / (1280f * 720f / (width * height))).toInt()
        if (originalBitrate < minBitrate) {
            return remeasuredBitrate
        }
        return if (remeasuredBitrate > maxBitrate) {
            maxBitrate
        } else Math.max(remeasuredBitrate, minBitrate)
    }

    private fun getVideoBitrateWithFactor(f: Float): Int {
        return (f * 2000f * 1000f * 1.13f).toInt()
    }

    companion object {
        const val MEDIA_SIZE_UNKNOWN = -1L
    }
}