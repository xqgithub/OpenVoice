package com.shannon.openvoice.business.main.viewmedia

import android.Manifest
import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.app.DownloadManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Color
import android.net.Uri
import android.os.Environment
import android.transition.Transition
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.blankj.utilcode.util.LogUtils
import com.permissionx.guolindev.PermissionX
import com.shannon.android.lib.base.activity.KBaseActivity
import com.shannon.android.lib.extended.isBlankPlus
import com.shannon.android.lib.util.ToastUtil
import com.shannon.openvoice.R
import com.shannon.openvoice.databinding.ActivityViewMediaBinding
import com.shannon.openvoice.model.AttachmentViewData
import java.util.*


/**
 * Date:2022/8/23
 * Time:13:35
 * author:dimple
 * 视频或者图片展示页面
 */

typealias ToolbarVisibilityListener = (isVisible: Boolean) -> Unit

class ViewMediaActivity : KBaseActivity<ActivityViewMediaBinding, ViewMediaModel>(),
    ViewImageFragment.PhotoActionsListener {


    val toolbar: View
        get() = binding.toolbar

    var isToolbarVisible = true
        private set

    private var attachments: ArrayList<AttachmentViewData>? = null
    private val toolbarVisibilityListeners = mutableListOf<ToolbarVisibilityListener>()
    private var imageUrl: String? = null

    fun addToolbarVisibilityListener(listener: ToolbarVisibilityListener): Function0<Boolean> {
        this.toolbarVisibilityListeners.add(listener)
        listener(isToolbarVisible)
        return { toolbarVisibilityListeners.remove(listener) }
    }

    companion object {
        private const val EXTRA_ATTACHMENTS = "attachments"
        private const val EXTRA_ATTACHMENT_INDEX = "index"
        private const val EXTRA_SINGLE_IMAGE_URL = "single_image"
        private const val TAG = "ViewMediaActivity"

        @JvmStatic
        fun newIntent(
            context: Context?,
            attachments: List<AttachmentViewData>,
            index: Int
        ): Intent {
            val intent = Intent(context, ViewMediaActivity::class.java)
            intent.putParcelableArrayListExtra(EXTRA_ATTACHMENTS, ArrayList(attachments))
            intent.putExtra(EXTRA_ATTACHMENT_INDEX, index)
            return intent
        }

        @JvmStatic
        fun newSingleImageIntent(context: Context, url: String): Intent {
            val intent = Intent(context, ViewMediaActivity::class.java)
            intent.putExtra(EXTRA_SINGLE_IMAGE_URL, url)
            return intent
        }
    }

    override fun onInit() {
        supportPostponeEnterTransition()

        attachments = intent.getParcelableArrayListExtra(EXTRA_ATTACHMENTS)
        val initialPosition = intent.getIntExtra(EXTRA_ATTACHMENT_INDEX, 0)
        val adapter: ViewMediaAdapter = if (attachments != null) {
            val realAttachs = attachments!!.map(AttachmentViewData::attachment)
            ImagePagerAdapter(this, realAttachs, initialPosition)
        } else {
            imageUrl = intent.getStringExtra(EXTRA_SINGLE_IMAGE_URL)
                ?: throw IllegalArgumentException("attachment list or image url has to be set")
            SingleImagePagerAdapter(this, imageUrl!!)
        }

        binding.viewPager.adapter = adapter
        binding.viewPager.setCurrentItem(initialPosition, false)
        binding.viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                binding.toolbar.title = getPageTitle(position)
            }
        })

        // Setup the toolbar.
        setSupportActionBar(binding.toolbar)
        val actionBar = supportActionBar
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true)
            actionBar.setDisplayShowHomeEnabled(true)
            actionBar.title = getPageTitle(initialPosition)
        }
        binding.toolbar.setNavigationOnClickListener { supportFinishAfterTransition() }
        binding.toolbar.setOnMenuItemClickListener { item: MenuItem ->
            when (item.itemId) {
                R.id.action_download -> requestDownloadMedia()
//                R.id.action_open_status -> onOpenStatus()
//                R.id.action_share_media -> shareMedia()
//                R.id.action_copy_media_link -> copyLink()
            }
            true
        }

        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LOW_PROFILE
        window.statusBarColor = Color.BLACK
        window.sharedElementEnterTransition.addListener(object : NoopTransitionListener {
            override fun onTransitionEnd(transition: Transition) {
                adapter.onTransitionEnd(binding.viewPager.currentItem)
                window.sharedElementEnterTransition.removeListener(this)
            }
        })
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.view_media_toolbar, menu)
        // We don't support 'open status' from single image views
//        menu.findItem(R.id.action_open_status)?.isVisible = (attachments != null)
        return true
    }

    override fun onPrepareOptionsMenu(menu: Menu?): Boolean {
//        menu?.findItem(R.id.action_share_media)?.isEnabled = !isCreating
        return true
    }

    private fun getPageTitle(position: Int): CharSequence {
        if (attachments == null) {
            return ""
        }
        return String.format(Locale.getDefault(), "%d/%d", position + 1, attachments?.size)
    }


    private fun requestDownloadMedia() {
        PermissionX.init(this)
            .permissions(
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            )
            .request { allGranted, _, _ ->
                if (allGranted) {
                    downloadMedia()
                } else {
                    ToastUtil.showCenter(getString(R.string.tips_download_picture))
                }
            }
    }


    /**
     * 下载媒体文件
     */
    private fun downloadMedia() {
        val url = imageUrl ?: attachments!![binding.viewPager.currentItem].attachment.url
        val filename = Uri.parse(url).lastPathSegment
        ToastUtil.showCenter(getString(R.string.desc_updating, filename))
        val downloadManager = getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        val request = DownloadManager.Request(Uri.parse(url))
        request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, filename)
        val downloadId = downloadManager.enqueue(request)
        downloadListener(downloadId, filename)
    }

    /**
     * 下载监听
     */

    private var broadcastReceiver: BroadcastReceiver? = null

    private fun downloadListener(downloadId: Long, filename: String?) {
        // 注册广播监听系统的下载完成事件。
        val intentFilter = IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE)
        broadcastReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                val ID = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1)
                if (ID == downloadId) {
                    LogUtils.i("文件 $filename 下载完成")
                }
            }
        }
        registerReceiver(broadcastReceiver, intentFilter);
    }


    abstract class ViewMediaAdapter(activity: FragmentActivity) : FragmentStateAdapter(activity) {
        abstract fun onTransitionEnd(position: Int)
    }

    override fun onBringUp() {
        supportStartPostponedEnterTransition()
    }

    override fun onDismiss() {
        supportFinishAfterTransition()
    }

    override fun onPhotoTap() {
        isToolbarVisible = !isToolbarVisible
        for (listener in toolbarVisibilityListeners) {
            listener(isToolbarVisible)
        }

        val visibility = if (isToolbarVisible) View.VISIBLE else View.INVISIBLE
        val alpha = if (isToolbarVisible) 1.0f else 0.0f
        if (isToolbarVisible) {
            // If to be visible, need to make visible immediately and animate alpha
            binding.toolbar.alpha = 0.0f
            binding.toolbar.visibility = visibility
        }

        binding.toolbar.animate().alpha(alpha)
            .setListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) {
                    binding.toolbar.visibility = visibility
                    animation.removeListener(this)
                }
            })
            .start()
    }

    override fun onDestroy() {
        super.onDestroy()
        if (!isBlankPlus(broadcastReceiver)) {
            unregisterReceiver(broadcastReceiver)
        }
    }
}


interface NoopTransitionListener : Transition.TransitionListener {
    override fun onTransitionEnd(transition: Transition) {
    }

    override fun onTransitionResume(transition: Transition) {
    }

    override fun onTransitionPause(transition: Transition) {
    }

    override fun onTransitionCancel(transition: Transition) {
    }

    override fun onTransitionStart(transition: Transition) {
    }
}


