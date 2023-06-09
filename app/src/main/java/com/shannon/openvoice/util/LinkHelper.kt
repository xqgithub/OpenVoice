/* Copyright 2017 Andrew Dawson
 *
 * This file is a part of Tusky.
 *
 * This program is free software; you can redistribute it and/or modify it under the terms of the
 * GNU General Public License as published by the Free Software Foundation; either version 3 of the
 * License, or (at your option) any later version.
 *
 * Tusky is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even
 * the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General
 * Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with Tusky; if not,
 * see <http://www.gnu.org/licenses>. */
@file:JvmName("LinkHelper")

package com.shannon.openvoice.util

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.text.style.URLSpan
import android.view.View
import android.widget.TextView
import androidx.annotation.VisibleForTesting
import androidx.core.net.toUri
import com.shannon.openvoice.business.timeline.listener.LinkListener
import com.shannon.openvoice.model.HashTag
import com.shannon.openvoice.model.StatusModel
import timber.log.Timber
import java.net.URI
import java.net.URISyntaxException

fun getDomain(urlString: String?): String {
    val host = urlString?.toUri()?.host
    return when {
        host == null -> ""
        host.startsWith("www.") -> host.substring(4)
        else -> host
    }
}

/**
 * Finds links, mentions, and hashtags in a piece of text and makes them clickable, associating
 * them with callbacks to notify when they're clicked.
 *
 * @param view the returned text will be put in
 * @param content containing text with mentions, links, or hashtags
 * @param mentions any '@' mentions which are known to be in the content
 * @param listener to notify about particular spans that are clicked
 */
fun setClickableText(view: TextView, content: CharSequence, mentions: List<StatusModel.Mention>?, tags: List<HashTag>?, listener: LinkListener) {
    view.text = SpannableStringBuilder.valueOf(content).apply {
        getSpans(0, content.length, URLSpan::class.java).forEach {
            setClickableText(it, this, mentions, tags, listener)
        }
    }
    view.movementMethod = LinkMovementMethod.getInstance()
}

@VisibleForTesting
fun setClickableText(
    span: URLSpan,
    builder: SpannableStringBuilder,
    mentions: List<StatusModel.Mention>?,
    tags: List<HashTag>?,
    listener: LinkListener
) = builder.apply {
    val start = getSpanStart(span)
    val end = getSpanEnd(span)
    val flags = getSpanFlags(span)
    val text = subSequence(start, end)

    val customSpan = when (text[0]) {
        '#' -> getCustomSpanForTag(text, tags, span, listener)
        '@' -> getCustomSpanForMention(mentions, span, listener)
        else -> null
    } ?: object : NoUnderlineURLSpan(span.url) {
        override fun onClick(view: View) = listener.onViewUrl(url)
    }

    removeSpan(span)
    setSpan(customSpan, start, end, flags)

    /* Add zero-width space after links in end of line to fix its too large hitbox.
     * See also : https://github.com/tuskyapp/Tusky/issues/846
     *            https://github.com/tuskyapp/Tusky/pull/916 */
    if (end >= length || subSequence(end, end + 1).toString() == "\n") {
        insert(end, "\u200B")
    }
}

@VisibleForTesting
fun getTagName(text: CharSequence, tags: List<HashTag>?): String? {
    val scrapedName = text.subSequence(1, text.length).toString()
    return when (tags) {
        null -> scrapedName
        else -> tags.firstOrNull { it.name.equals(scrapedName, true) }?.name
    }
}

private fun getCustomSpanForTag(text: CharSequence, tags: List<HashTag>?, span: URLSpan, listener: LinkListener): ClickableSpan? {
    return getTagName(text, tags)?.let {
        object : NoUnderlineURLSpan(span.url) {
            override fun onClick(view: View) = listener.onViewTag(it)
        }
    }
}

private fun getCustomSpanForMention(mentions: List<StatusModel.Mention>?, span: URLSpan, listener: LinkListener): ClickableSpan? {
    // https://github.com/tuskyapp/Tusky/pull/2339
    return mentions?.firstOrNull { it.url == span.url }?.let {
        getCustomSpanForMentionUrl(span.url, it.id, listener)
    }
}

private fun getCustomSpanForMentionUrl(url: String, mentionId: String, listener: LinkListener): ClickableSpan {
    return object : NoUnderlineURLSpan(url) {
        override fun onClick(view: View) = listener.onViewAccount(mentionId)
    }
}

/**
 * Put mentions in a piece of text and makes them clickable, associating them with callbacks to
 * notify when they're clicked.
 *
 * @param view the returned text will be put in
 * @param mentions any '@' mentions which are known to be in the content
 * @param listener to notify about particular spans that are clicked
 */
fun setClickableMentions(view: TextView, mentions: List<StatusModel.Mention>?, listener: LinkListener) {
    if (mentions?.isEmpty() != false) {
        view.text = null
        return
    }

    view.text = SpannableStringBuilder().apply {
        var start = 0
        var end = 0
        var flags: Int
        var firstMention = true

        for (mention in mentions) {
            val customSpan = getCustomSpanForMentionUrl(mention.url, mention.id, listener)
            end += 1 + mention.localUsername.length // length of @ + username
            flags = getSpanFlags(customSpan)
            if (firstMention) {
                firstMention = false
            } else {
                append(" ")
                start += 1
                end += 1
            }

            append("@")
            append(mention.localUsername)
            setSpan(customSpan, start, end, flags)
            append("\u200B") // same reasoning as in setClickableText
            end += 1 // shift position to take the previous character into account
            start = end
        }
    }
    view.movementMethod = LinkMovementMethod.getInstance()
}

fun createClickableText(text: String, link: String): CharSequence {
    return SpannableStringBuilder(text).apply {
        setSpan(NoUnderlineURLSpan(link), 0, text.length, Spanned.SPAN_INCLUSIVE_EXCLUSIVE)
    }
}

/**
 * Opens a link, depending on the settings, either in the browser or in a custom tab
 *
 * @receiver the Context to open the link from
 * @param url a string containing the url to open
 */
fun Context.openLink(url: String) {
    val uri = url.toUri().normalizeScheme()
    openLinkInBrowser(uri, this)
}

/**
 * opens a link in the browser via Intent.ACTION_VIEW
 *
 * @param uri the uri to open
 * @param context context
 */
private fun openLinkInBrowser(uri: Uri?, context: Context) {
    val intent = Intent(Intent.ACTION_VIEW, uri)
    try {
        context.startActivity(intent)
    } catch (e: ActivityNotFoundException) {
        Timber.w("Activity was not found for intent, $intent")
    }
}

fun looksLikeMastodonUrl(urlString: String): Boolean {
    val uri: URI
    try {
        uri = URI(urlString)
    } catch (e: URISyntaxException) {
        return false
    }

    if (uri.query != null ||
        uri.fragment != null ||
        uri.path == null
    ) {
        return false
    }

    val path = uri.path
    return path.matches("^/@[^/]+$".toRegex()) ||
            path.matches("^/@[^/]+/\\d+$".toRegex()) ||
            path.matches("^/users/\\w+$".toRegex()) ||
            path.matches("^/notice/[a-zA-Z0-9]+$".toRegex()) ||
            path.matches("^/objects/[-a-f0-9]+$".toRegex()) ||
            path.matches("^/notes/[a-z0-9]+$".toRegex()) ||
            path.matches("^/display/[-a-f0-9]+$".toRegex()) ||
            path.matches("^/profile/\\w+$".toRegex()) ||
            path.matches("^/p/\\w+/\\d+$".toRegex()) ||
            path.matches("^/\\w+$".toRegex())
}

private const val TAG = "LinkHelper"
