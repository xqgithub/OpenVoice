package com.shannon.openvoice.model

import com.google.gson.annotations.JsonAdapter
import com.google.gson.annotations.SerializedName
import com.shannon.openvoice.business.main.mine.blacklist.GuardedBooleanAdapter

/**
 * Date:2022/8/15
 * Time:14:53
 * author:dimple
 */
data class RelationshipBean(
    val id: String,
    val following: Boolean,
    @SerializedName("followed_by")
    val followedBy: Boolean,
    val blocking: Boolean,
    val muting: Boolean,
    @SerializedName("muting_notifications")
    val mutingNotifications: Boolean,
    val requested: Boolean,
    @SerializedName("showing_reblogs")
    val showingReblogs: Boolean,

    /* Pleroma extension, same as 'notifying' on Mastodon.
     * Some instances like qoto.org have a custom subscription feature where 'subscribing' is a json object,
     * so we use the custom GuardedBooleanAdapter to ignore the field if it is not a boolean.
     */
    @JsonAdapter(GuardedBooleanAdapter::class)
    val subscribing: Boolean? = null,
    @SerializedName("domain_blocking")
    val blockingDomain: Boolean,
    val note: String?, // nullable for backward compatibility / feature detection
    val notifying: Boolean? // since 3.3.0rc)
)