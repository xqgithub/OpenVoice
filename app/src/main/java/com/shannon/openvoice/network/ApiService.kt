package com.shannon.openvoice.network

import android.accounts.Account
import com.shannon.openvoice.business.main.chat.ChatMessageResponse
import com.shannon.openvoice.business.main.chat.MessageBean
import com.shannon.openvoice.model.*
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.Single
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.*
import retrofit2.http.Field

/**
 *
 * @ProjectName:    OpenVoice
 * @Package:        com.shannon.openvoice.network
 * @ClassName:      ApiService
 * @Description:     java类作用描述
 * @Author:         czhen
 * @CreateDate:     2022/7/26 15:20
 */
@JvmSuppressWildcards
interface ApiService {

    companion object {
        const val ENDPOINT_AUTHORIZE = "oauth/authorize"
        const val DOMAIN_HEADER = "domain"
        const val PLACEHOLDER_DOMAIN = "dummy.placeholder"
    }

    /**
     * 校验用户证书
     */
    @GET("api/v1/accounts/verify_credentials")
    fun accountVerifyCredentials(): Observable<Response<AccountBean>>

    /**
     * 公告列表
     */
    @GET("api/v1/announcements")
    fun listAnnouncements(
        @Query("with_dismissed") withDismissed: Boolean = true
    ): Observable<Response<List<AnnouncementBean>>>

    /**
     * 黑名单列表
     */
    @GET("api/v1/blocks")
    fun listblocks(
        @Query("max_id") maxId: String?
    ): Observable<Response<List<TimelineAccount>>>

    /**
     * 解锁黑名单
     */
    @POST("api/v1/accounts/{id}/unblock")
    fun unblockAccount(@Path("id") accountId: String): Observable<Response<RelationshipBean>>


    /**
     * 通过ID获取Account数据
     */
    @GET("api/v1/accounts/{id}")
    fun account(@Path("id") accountId: String): Observable<Response<AccountBean>>

    /**
     * 检查与给定账号的关系
     */
    @GET("api/v1/accounts/relationships")
    fun relationships(
        @Query("id[]") accountIds: List<String>
    ): Observable<Response<List<RelationshipBean>>>

    /**
     * 关注用户
     */
    @FormUrlEncoded
    @POST("api/v1/accounts/{id}/follow")
    fun followAccount(
        @Path("id") accountId: String,
        @Field("reblogs") showReblogs: Boolean? = null,
        @Field("notify") notify: Boolean? = null
    ): Observable<Response<RelationshipBean>>

    /**
     * 取消关注用户
     */
    @POST("api/v1/accounts/{id}/unfollow")
    fun unfollowAccount(
        @Path("id") accountId: String
    ): Observable<Response<RelationshipBean>>

    /**
     * 获取用户关注的账号列表
     */
    @GET("api/v1/accounts/{id}/following")
    fun accountFollowing(
        @Path("id") accountId: String,
        @Query("max_id") maxId: String?
    ): Observable<Response<List<TimelineAccount>>>

    /**
     * 获取用户的粉丝列表
     */
    @GET("api/v1/accounts/{id}/followers")
    fun accountFollowers(
        @Path("id") accountId: String,
        @Query("max_id") maxId: String?
    ): Observable<Response<List<TimelineAccount>>>


    /**
     * 创建oauth2应用
     */
    @FormUrlEncoded
    @POST("api/v1/apps")
    fun authenticateApp(
        @Header(DOMAIN_HEADER) domain: String,
        @Field("client_name") clientName: String,
        @Field("redirect_uris") redirectUris: String,
        @Field("scopes") scopes: String,
        @Field("website") website: String
    ): Observable<Response<oauth2Bean>>

    /**
     * 获取oauth2应用token
     */
    @FormUrlEncoded
    @POST("oauth/token")
    fun fetchOAuthToken(
        @Header(DOMAIN_HEADER) domain: String,
        @Field("client_id") clientId: String,
        @Field("client_secret") clientSecret: String,
        @Field("redirect_uri") redirectUri: String,
        @Field("scope") scopes: String,
        @Field("grant_type") grantType: String
    ): Observable<Response<OAuthToken>>

    /**
     * 用户登录接口
     */
    @FormUrlEncoded
    @POST("oauth/token")
    fun userLogin(
        @Header(DOMAIN_HEADER) domain: String,
        @Field("client_id") clientId: String,
        @Field("client_secret") clientSecret: String,
        @Field("redirect_uri") redirectUri: String,
        @Field("scope") scopes: String,
        @Field("grant_type") grantType: String,
        @Field("username") username: String,
        @Field("password") password: String,
    ): Observable<Response<OAuthToken>>

    /**
     * 邮箱注册
     */
    @FormUrlEncoded
    @POST("api/v1/accounts")
    fun emailRegistration(
        @Field("username") username: String,
        @Field("email") email: String,
        @Field("password") password: String,
        @Field("birthday") birthday: String,
        @Field("agreement") agreement: Boolean,
        @Field("locale") locale: String
    ): Observable<Response<OAuthToken>>

    /**
     * 邮箱注册 v2
     */
    @FormUrlEncoded
    @POST("api/v2/accounts")
    fun emailRegistrationV2(
        @Field("username") username: String,
        @Field("email") email: String,
        @Field("password") password: String,
        @Field("birthday") birthday: String,
        @Field("agreement") agreement: Boolean,
        @Field("locale") locale: String,
        @Field("email_code") emailCode: String
    ): Observable<Response<OAuthToken>>

    /**
     * 注册验证码发送邮件
     */
    @POST("api/v1/auth/email_code")
    fun registerSendEmail(@Body user: RequestBody): Observable<Response<Any>>

    /**
     * 重置密码发邮件
     */
    @POST("api/v1/auth/forgot")
    fun forgotPwdSendEmail(@Body user: RequestBody): Observable<Response<Any>>

    /**
     * 重置密码
     */
    @PATCH("api/v1/auth/reset_password")
    fun resetPassword(@Body user: RequestBody): Observable<Response<Any>>


    /**
     * 修改用户信息
     */
    @Multipart
    @PATCH("api/v1/accounts/update_credentials")
    fun accountUpdateCredentials(
        @Part(value = "display_name") displayName: RequestBody?,
        @Part avatar: MultipartBody.Part?,
        @Part header: MultipartBody.Part?,
        @Part(value = "note") note: RequestBody?,
        @Part(value = "birthday") birthday: RequestBody?
    ): Observable<Response<AccountBean>>

    /**
     * 通知列表信息
     */
    @GET("api/v1/notifications")
    fun notifications(
        @Query("max_id") maxId: String?,
        @Query("since_id") sinceId: String?,
        @Query("limit") limit: Int = 20,
        @Query("exclude_types[]") excludes: Set<Notification.Type>?
    ): Observable<Response<List<Notification>>>

    /**
     * 清空通知信息
     */
    @POST("api/v1/notifications/clear")
    fun clearNotifications(): Observable<Response<Any>>

    /**
     * 修改密码
     */
    @POST("api/v1/auth/update_password")
    fun updatePassword(@Body user: RequestBody): Observable<Response<Any>>

    /**
     * 检查版本更新
     */
    @GET("api/v1/versions")
    fun updateVersions(@Query("platform") platform: String): Observable<Response<UpdateBean>>

    /**
     * 获取系统功能配置项
     */
    @GET("api/v1/settings")
    fun systemConfiguration(): Observable<Response<SystemConfigurationBean>>

    /**
     * 验证模型邀请码
     */
    @POST("api/v1/voice_models/verify_invite_code")
    fun verifyInviteCode(@Body inviteCode: RequestBody): Observable<Response<InviteCodeBean>>

    /**
     * 获取官方语音模型分类列表
     */
    @GET("api/v1/voice_models/categories")
    fun voiceModelsOfficialCategoires(): Observable<Response<List<OfficalModelLableBean>>>

    /**
     * 获取官方语音模型列表
     */
    @GET("api/v1/voice_models/officials")
    fun voiceModelsOfficialList(
        @Query("offset") offset: Int,
        @Query("limit") limit: Int = 10,
    ): Observable<Response<List<VoiceModelResult>>>

    /**
     * 获取当前使用中语音模型
     */
    @GET("api/v1/voice_models/activited")
    fun voiceModelsActivited(): Observable<Response<VoiceModelResult>>

    /**
     * 心跳数据上报
     */
    @POST("api/v1/pings")
    fun pings(@Body params: RequestBody): Observable<Response<Any>>

    /**
     * 用户注销
     */
    @POST("api/v1/accounts/suspend")
    fun accountsSuspend(): Observable<Response<Any>>

    /**
     * 上报设备 google fcm token
     */
    @POST("api/v1/push/device")
    fun reportDeviceToken(
        @Query("token") token: String,
        @Query("platform") platform: String = "android"
    ): Observable<Response<Any>>

    @GET("api/v1/timelines/home")
    fun homeTimeline(
        @Query("max_id") maxId: String? = null,
        @Query("since_id") sinceId: String? = null,
        @Query("limit") limit: Int? = null
    ): Observable<Response<List<StatusModel>>>

    @GET("api/v1/timelines/public")
    fun publicTimeline(
        @Query("local") local: Boolean? = null,
        @Query("max_id") maxId: String? = null,
        @Query("since_id") sinceId: String? = null,
        @Query("limit") limit: Int? = null
    ): Observable<Response<List<StatusModel>>>

    @GET("api/v1/favourites")
    fun favourites(
        @Query("max_id") maxId: String?,
        @Query("since_id") sinceId: String?,
        @Query("limit") limit: Int?
    ): Observable<Response<List<StatusModel>>>

    @GET("api/v1/timelines/tag/{hashtag}")
    fun hashtagTimeline(
        @Path("hashtag") hashtag: String,
        @Query("local") local: Boolean?,
        @Query("max_id") maxId: String?,
        @Query("since_id") sinceId: String?,
        @Query("limit") limit: Int?
    ): Observable<Response<List<StatusModel>>>

    @GET("api/v1/voice_models")
    fun getVoiceModels(
        @Query("offset") offset: Int,
        @Query("limit") limit: Int = 10,
        @Query("status") status: String = ""
    ): Observable<Response<List<VoiceModelResult>>>

    @POST("/api/v1/voice_models/{voice_model_id}/activate")
    fun activateVoiceModel(
        @Path("voice_model_id") id: Long
    ): Observable<Response<VoiceModelResult>>

    @DELETE("/api/v1/voice_models/{voice_model_id}")
    fun deleteVoiceModel(
        @Path("voice_model_id") id: Long
    ): Observable<Response<VoiceModelResult>>

    @POST("api/v1/voice_models")
    fun postVoiceModel(
        @Body oss_objects: VoiceModelRequest
    ): Observable<Response<VoiceModelResult>>

    @POST("api/v1/statuses/{id}/reblog")
    fun reblogStatus(
        @Path("id") statusId: String
    ): Observable<Response<StatusModel>>

    @POST("api/v1/statuses/{id}/unreblog")
    fun unreblogStatus(
        @Path("id") statusId: String
    ): Observable<Response<StatusModel>>

    @POST("api/v1/statuses/{id}/favourite")
    fun favouriteStatus(
        @Path("id") statusId: String
    ): Observable<Response<StatusModel>>

    @POST("api/v1/statuses/{id}/unfavourite")
    fun unfavouriteStatus(
        @Path("id") statusId: String
    ): Observable<Response<StatusModel>>

    @POST("api/v1/statuses/{id}/bookmark")
    fun bookmarkStatus(
        @Path("id") statusId: String
    ): Observable<Response<StatusModel>>

    @POST("api/v1/statuses/{id}/unbookmark")
    fun unbookmarkStatus(
        @Path("id") statusId: String
    ): Observable<Response<StatusModel>>

    @POST("api/v1/statuses/{id}/pin")
    fun pinStatus(
        @Path("id") statusId: String
    ): Observable<Response<StatusModel>>

    @POST("api/v1/statuses/{id}/unpin")
    fun unpinStatus(
        @Path("id") statusId: String
    ): Observable<Response<StatusModel>>

    @DELETE("api/v1/statuses/{id}")
    fun deleteStatus(
        @Path("id") statusId: String
    ): Observable<Response<DeletedStatus>>

    @POST("api/v1/accounts/{id}/block")
    fun blockAccount(
        @Path("id") accountId: String
    ): Observable<Response<Any>>

    @GET("api/v1/accounts/search")
    fun searchAccounts(
        @Query("q") query: String,
        @Query("resolve") resolve: Boolean? = null,
        @Query("limit") limit: Int? = null,
        @Query("following") following: Boolean? = null
    ): Observable<Response<List<TimelineAccount>>>

    /**
     * 搜索
     */
    @GET("api/v2/search")
    fun searchObservable(
        @Query("q") query: String?,
        @Query("type") type: String? = null,
        @Query("resolve") resolve: Boolean? = null,
        @Query("limit") limit: Int? = null,
        @Query("offset") offset: Int? = null,
        @Query("following") following: Boolean? = null
    ): Observable<Response<SearchResult>>

    /**
     * 获取搜索推荐
     */
    @GET("api/v2/search/recommend")
    fun searchRecommend(
        @Query("type") type: String? = null,
        @Query("limit") limit: Int? = null,
    ): Observable<Response<SearchResult>>

    @POST("/api/v1/voice_models/{voice_model_id}/bid")
    fun purchaseVoiceModel(
        @Path("voice_model_id") id: Long
    ): Observable<Response<VoiceModelResult>>

    @FormUrlEncoded
    @POST("/api/v1/voice_models/{voice_model_id}/bid")
    fun buyVoiceModel(
        @Path("voice_model_id") id: Long,
        @Field("address") address: String,
    ): Observable<Response<VoiceModelResult>>

    @FormUrlEncoded
    @POST("/api/v1/voice_models/{voice_model_id}/transaction_callback")
    fun buyVoiceModelDone(
        @Path("voice_model_id") id: Long,
        @Field("transaction_hash") hash: String,
        @Field("address") address: String,
    ): Observable<Response<Any>>

    @POST("api/v1/statuses")
    fun createStatus(
        @Header("domain") domain: String,
        @Header("Idempotency-Key") idempotencyKey: String,
        @Body status: NewVoiceCover
    ): Observable<Response<StatusModel>>

    @GET("api/v1/accounts/{id}/statuses")
    fun accountStatuses(
        @Path("id") accountId: String,
        @Query("max_id") maxId: String?,
        @Query("since_id") sinceId: String?,
        @Query("limit") limit: Int?,
        @Query("exclude_replies") excludeReplies: Boolean?,
        @Query("only_media") onlyMedia: Boolean?,
        @Query("pinned") pinned: Boolean?,
        @Query("exclude_reblogs") excludeReblogs: Boolean?
    ): Observable<Response<List<StatusModel>>>

    @GET("api/v1/statuses/{id}/context")
    fun statusContext(
        @Path("id") statusId: String
    ): Observable<Response<StatusContext>>

    @GET("api/v1/accounts/{id}/statuses")
    fun accountStatusesReport(
        @Path("id") accountId: String,
        @Query("max_id") maxId: String?,
        @Query("since_id") sinceId: String?,
        @Query("min_id") minId: String?,
        @Query("limit") limit: Int?,
        @Query("exclude_reblogs") excludeReblogs: Boolean?
    ): Observable<Response<List<StatusModel>>>

    @FormUrlEncoded
    @POST("api/v1/reports")
    fun report(
        @Field("account_id") accountId: String,
        @Field("status_ids[]") statusIds: List<String>,
        @Field("comment") comment: String
    ): Observable<Response<Unit>>

    @GET("/api/v1/accounts/{id}/likes")
    fun accountLikes(
        @Path("id") accountId: String,
        @Query("offset") offset: Int,
        @Query("limit") limit: Int = 10
    ): Observable<Response<List<WorkLikes>>>

    @GET("/api/v1/accounts/{id}/voice_models")
    fun accountVoiceModel(
        @Path("id") accountId: String,
        @Query("offset") offset: Int,
        @Query("limit") limit: Int = 10
    ): Observable<Response<List<StatusModel.VoiceModel>>>

    @POST("/api/v1/voice_models/{id}/like")
    fun likeVoiceModel(
        @Path("id") id: Long,
    ): Observable<Response<Unit>>

    @POST("/api/v1/voice_models/{id}/unlike")
    fun unlikeVoiceModel(
        @Path("id") id: Long,
    ): Observable<Response<Unit>>

    @FormUrlEncoded
    @PUT("/api/v1/voice_models/{id}")
    fun updateVoiceModel(
        @Path("id") modelId: Long,
        @Field("price") price: String?,
        @Field("currency") currency: String?,
        @Field("royalties_fee") royaltiesFee: String?,
        @Field("payload") payload: String?,
        @Field("sale_cycle") saleCycle: String?,
    ): Observable<Response<VoiceModelResult>>

    @GET("api/v1/voice_models/{id}/statuses")
    fun modelStatuses(
        @Path("id") modelId: Long,
        @Query("max_id") maxId: String?,
        @Query("since_id") sinceId: String?,
        @Query("min_id") minId: String?,
        @Query("limit") limit: Int?
    ): Observable<Response<List<StatusModel>>>

    @GET("/api/v1/voice_models/{id}")
    fun modelDetail(
        @Path("id") modelId: Long,
    ): Observable<Response<ModelDetail>>

    @GET("/api/v1/statuses/{id}")
    fun getStatus(
        @Path("id") statusId: String,
    ): Observable<Response<StatusModel>>

    @GET("/api/v1/markets/explore")
    fun getTradeList(
        @Query("time_range") timeRang: Int?,
        @Query("type") type: Int,
        @Query("offset") offset: Int,
        @Query("limit") limit: Int = 10
    ): Observable<Response<List<TradeList>>>

    @GET("/api/v1/statuses/{id}/tts")
    fun ttsGenerated(
        @Path("id") modelId: String
    ): Observable<Response<StatusModel>>

    @GET("/api/v1/statuses/tts")
    fun ttsGenerated(
        @Query("status_ids[]") modelId: List<String>
    ): Observable<Response<List<GenerateResult>>>

    @GET("/api/v1/voice_models/{id}/likes")
    fun voiceModelLikes(
        @Path("id") modelId: Long,
        @Query("offset") offset: Int,
        @Query("limit") limit: Int = 10
    ): Observable<Response<List<ModelLikes>>>

    @GET("/api/v1/accounts/{id}/tradings")
    fun accountTradings(
        @Path("id") accountId: String,
        @Query("offset") offset: Int,
        @Query("limit") limit: Int = 10
    ): Observable<Response<List<Tradings>>>

    @GET("/api/v1/voice_models/{id}/tradings")
    fun voiceModelTradings(
        @Path("id") modelId: Long,
        @Query("offset") offset: Int,
        @Query("limit") limit: Int = 10
    ): Observable<Response<List<Tradings>>>

    @GET("/api/v1/chain/currency")
    fun chainCurrency(): Observable<Response<List<String>>>

    @GET("/api/v1/accounts/nonce")
    fun nonce(): Observable<Response<NonceResult>>

    @FormUrlEncoded
    @POST("/api/v1/accounts/signin")
    fun signIn(
        @Field("message") message: String,
        @Field("signature") signature: String,
    ): Observable<Response<OAuthToken>>

    @FormUrlEncoded
    @PUT("/api/v1/accounts/update_account_info")
    fun updateAccountInfo(
        @Field("username") username: String,
        @Field("birthday") birthday: String,
    ): Observable<Response<Unit>>

    /**
     * 黑名单列表
     */
    @GET("/api/v1/accounts/gpt")
    fun chatGPT(
        @Query("message") message: String
    ): Observable<Response<ChatMessageResponse>>
}