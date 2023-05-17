package com.shannon.openvoice.network

import com.shannon.openvoice.model.MediaUploadResult
import io.reactivex.rxjava3.core.Observable
import okhttp3.MultipartBody
import retrofit2.Response
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part

/** endpoints defined in this interface will be called with a higher timeout than usual
 * which is necessary for media uploads to succeed on some servers
 */
interface MediaUploadApi {
    @Multipart
    @POST("api/v2/media")
     fun uploadMedia(
        @Part file: MultipartBody.Part,
        @Part description: MultipartBody.Part? = null
    ): Observable<Response<MediaUploadResult>>
}
