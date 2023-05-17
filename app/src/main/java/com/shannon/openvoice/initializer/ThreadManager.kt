package com.shannon.openvoice.initializer

import android.os.Process
import java.util.concurrent.LinkedBlockingQueue
import java.util.concurrent.ThreadFactory
import java.util.concurrent.ThreadPoolExecutor
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger

/**
 *
 * @ProjectName:    OpenVoice
 * @Package:        com.shannon.openvoice.initializer
 * @ClassName:      ThreadManager
 * @Description:     线程池
 * @Author:         czhen
 * @CreateDate:     2022/7/20 15:50
 */
class ThreadManager() {

    companion object {
        fun getInstance() = Holder.sThreadManager
    }

    object Holder {
        val sThreadManager = ThreadManager()
    }

     val DEFAULT_PRIORITY = (Process.THREAD_PRIORITY_BACKGROUND
            + Process.THREAD_PRIORITY_MORE_FAVORABLE)

    private val ALL_THREAD_COUNT =
        3f.coerceAtLeast(Runtime.getRuntime().availableProcessors() * 1.5f + 1).toInt()
    private val DISK_IO_THREAD_COUNT = Math.max(1, (ALL_THREAD_COUNT * 1f / 4).toInt())
    private val NET_WORK_THREAD_COUNT = ALL_THREAD_COUNT - DISK_IO_THREAD_COUNT

    var WORK_EXECUTOR = ThreadPoolExecutor(NET_WORK_THREAD_COUNT,
        NET_WORK_THREAD_COUNT + 4,
        40,
        TimeUnit.SECONDS,
        LinkedBlockingQueue(),
        object : ThreadFactory {
            private val mThreadId = AtomicInteger(0)
            override fun newThread(r: Runnable): Thread {
                return object : Thread(r, "start-" + mThreadId.getAndIncrement()) {
                    override fun run() {
                        // why PMD suppression is needed: https://github.com/pmd/pmd/issues/808
                        Process.setThreadPriority(DEFAULT_PRIORITY) //NOPMD AccessorMethodGeneration
                        super.run()
                    }
                }
            }
        })

    init {
        WORK_EXECUTOR.allowCoreThreadTimeOut(true)
    }
}